package me.sul.customentity.goal.target;

import com.google.common.base.Predicate;
import me.sul.customentity.entity.CustomEntity;
import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.entityweapon.EntityCrackShotWeapon;
import me.sul.customentity.util.DistanceComparator;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;

// PathfinderGoalNearestAttackableTarget 기반
public class PathfinderGoalFindEntityAndShootIt<T extends EntityCreature & CustomEntity, U extends EntityLiving> extends PathfinderGoal {
    private final T entity;
    protected final Class<U> targetType;
    private final int fireDelay;
    private final float projDamage;
    private final float projSpread;
    private final int projSpeed;
    private final int randomInterval;

    private final Predicate<U> predicate;
    private final DistanceComparator.Nms nmsDistanceComparator;
    private final DistanceComparator.Bukkit bukkitDistanceComparator;

    private final int unseenMemoryTicks;
    private int unseenTicks = 0;
    private int fireDelayCnt = 0;

    public PathfinderGoalFindEntityAndShootIt(T nmsCreature, Class<U> targetType, int fireDelay, float projDamage, float projSpread, int projSpeed) {
        this(nmsCreature, targetType, fireDelay, projDamage, projSpread, projSpeed, 100, 3);
    }
    public PathfinderGoalFindEntityAndShootIt(T nmsCreature, Class<U> targetType, int fireDelay, float projDamage, float projSpread, int projSpeed, int unseenMemoryTicks, int randomInterval) {
        this.entity = nmsCreature;
        this.targetType = targetType;
        this.fireDelay = fireDelay;
        this.projDamage = projDamage;
        this.projSpread = projSpread;
        this.projSpeed = projSpeed;
        this.randomInterval = randomInterval;
        this.unseenMemoryTicks = unseenMemoryTicks;
        this.predicate = new Predicate() {
            public boolean a(@Nullable U entity) {
                if (entity instanceof EntityScav) return false;
                return entity != null && IEntitySelector.e.apply(entity);
            }

            public boolean apply(@Nullable Object object) {
                return this.a((U)object);
            }
        };

        this.nmsDistanceComparator = new DistanceComparator.Nms(entity);
        this.bukkitDistanceComparator = new DistanceComparator.Bukkit(entity.getBukkitEntity());
        this.a(1);
    }

    // 항상 a()가 돌고있음
    @Override
    public boolean a() {  // canUse()
        if (entity.getRandom().nextInt(randomInterval) != 0) return false;

        EntityLiving target = getAppropriateTarget(entity);
        if (target != null) {
            entity.setGoalTarget(target, (targetType == EntityPlayer.class) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
            return true;
        }
        return false;
    }

    public boolean b() {  // canContinueToUse();
        EntityLiving target = this.entity.getGoalTarget();
        if (target == null || !target.isAlive()) { return false; }
        if (target instanceof EntityHuman && ((EntityHuman)target).abilities.isInvulnerable) { return false; } // 이거 위에 타게팅 기준에도 넣어야하나?

        double followRange = getFollowDistance();
        if (this.entity.h(target) > followRange * followRange) return false; // distanceToSqr

        if (this.entity.getEntitySenses().a(target)) {
            unseenTicks = 0;
        } else if (++unseenTicks > unseenMemoryTicks) {
            return false;
        }

        return true;
    }

    @Override
    public void c() {  // start()
        unseenTicks = 0;
        if (isHoldingBow()) {
            entity.c(EnumHand.MAIN_HAND); // startUsingItem()
        }
    }


    @Override
    public void d() {  // stop()
        entity.setGoalTarget(null, EntityTargetEvent.TargetReason.FORGOT_TARGET, true);
        entity.setSeeingTarget(false);
        fireDelayCnt = 0;
        if (isHoldingBow()) {
            this.entity.cN(); // stopUsingItem()
        }
    }

    @Override
    public void e() {  // tick()
        EntityLiving target = entity.getGoalTarget(); // canContinueToUse()에서 걸러주기때문에 null일 수가 없음
        if (entity.getEntitySenses().a(target)) { // 시야에 타겟이 있는가
            entity.setSeeingTarget(true);
            if (fireDelayCnt % fireDelay == 0) {
                EntityCrackShotWeapon.fireProjectile(entity.getBukkitEntity(), target.getBukkitEntity(), projSpread, projSpeed, projDamage);
            }
        } else {
            entity.setSeeingTarget(false);
        }
        fireDelayCnt++;
    }

    private AxisAlignedBB getTargetSearchArea(double d) {
        return this.entity.getBoundingBox().grow(d, 4.0D, d);
    }



    private boolean isHoldingBow() { return !this.entity.getItemInMainHand().isEmpty() && this.entity.getItemInMainHand().getItem() == Items.BOW; }

    private double getFollowDistance() {
        AttributeInstance attribute = this.entity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        return attribute == null ? 16.0D : attribute.getValue();
    }


    // 벡터 내적. p의 벡터에 대해서 파라미터의 벡터가 얼마만큼의 도움을 줄 수 있는가?
    // aVec와 bVec 내적 = aVec크기 * bVec크기 * cos(세타)
    // -> 세타 = acos(aVec와 bVec내적 / aVec크기 * bVec크기)
    // TODO: 우선순위 - 나를 때린 사람 -> 7칸 안 엔티티(시야각 무시?, 사람 > 몹) -> 사람 -> 몹
    private EntityLiving getAppropriateTarget(EntityLiving nmsEntity) {
        Entity bukkitEntity = nmsEntity.getBukkitEntity();
        Location entityLoc = bukkitEntity.getLocation();
        Vector entitySightVector = bukkitEntity.getLocation().getDirection();
        double followDistance = getFollowDistance();

        List<Entity> nearEntityList = bukkitEntity.getNearbyEntities(followDistance, followDistance, followDistance);
        nearEntityList.sort(bukkitDistanceComparator);
        Entity appropriateTarget = null;
        for (Entity nearEntity : nearEntityList) {
            if (!(nearEntity instanceof LivingEntity)) continue;

            Vector entityToNearEntityVector = nearEntity.getLocation().toVector().subtract(entityLoc.toVector());
            double angle = getAngleBetweenTwoVectors(entitySightVector, entityToNearEntityVector);

            if (angle <= 65 && ((LivingEntity)bukkitEntity).hasLineOfSight(nearEntity)) {
                if (nearEntity.isDead() || ((CraftEntity)nearEntity).getHandle() instanceof EntityScav || nearEntity.isInvulnerable()) break;
                if (nearEntity instanceof Player && ((Player)nearEntity).getGameMode() != GameMode.SURVIVAL) break;

                appropriateTarget = nearEntity;
            }
        }
        return (appropriateTarget != null) ? (EntityLiving) ((CraftEntity)appropriateTarget).getHandle() : null;
    }
    private double getAngleBetweenTwoVectors(Vector aVec, Vector bVec) {
        double cosAngle = (aVec.clone().dot(bVec)) / (aVec.length() * bVec.length());
        return Math.toDegrees(Math.acos(cosAngle)); // acos만 하면 라디안이 나와서 각도로 변환해야 함
    }
}