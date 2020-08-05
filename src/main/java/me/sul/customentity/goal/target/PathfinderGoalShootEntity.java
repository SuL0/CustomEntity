package me.sul.customentity.goal.target;

import com.google.common.base.Predicate;
import me.sul.customentity.entity.CustomEntity;
import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.entityweapon.EntityCrackShotWeapon;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

// PathfinderGoalNearestAttackableTarget 기반
public class PathfinderGoalShootEntity<T extends EntityCreature & CustomEntity, U extends EntityLiving> extends PathfinderGoal {
    private final T entity;
    protected final Class<U> targetType;
    private final boolean mustSee = true;
    private final int fireDelay;
    private final float projDamage;
    private final float projSpread;
    private final int projSpeed;
    private final int randomInterval;

    private final Predicate<U> predicate;
    private final DistanceComparator distanceComparator;

    private final int unseenMemoryTicks;
    private int unseenTicks = 0;
    private int fireDelayCnt = 0;

    public PathfinderGoalShootEntity(T nmsCreature, Class<U> targetType, int fireDelay, float projDamage, float projSpread, int projSpeed) {
        this(nmsCreature, targetType, fireDelay, projDamage, projSpread, projSpeed, 100, 3);
    }
    public PathfinderGoalShootEntity(T nmsCreature, Class<U> targetType, int fireDelay, float projDamage, float projSpread, int projSpeed, int unseenMemoryTicks, int randomInterval) {
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

        this.distanceComparator = new DistanceComparator(entity);
        this.a(1);
    }

    // 항상 a()가 돌고있음
    @Override
    public boolean a() {  // canUse()
        if (entity.getRandom().nextInt(randomInterval) != 0) return false;

        EntityLiving target = null;
        if (targetType == EntityPlayer.class) {
            target = entity.world.a(entity.locX, entity.locY + (double) entity.getHeadHeight(), entity.locZ, getFollowDistance(), getFollowDistance(), null, (Predicate<EntityHuman>) predicate); // getNearestPlayer
        } else {
            List<U> entityList = entity.world.a(targetType, getTargetSearchArea(getFollowDistance()), predicate);  // getNearestLoadedEntity. i(): getFollowRange()
            if (!entityList.isEmpty()) {
                entityList.sort(distanceComparator);
                target = entityList.get(0);
            }
        }

        if (target != null) {
            if (entity.getEntitySenses().a(target)) {
                if (targetType == EntityPlayer.class) {
                    entity.setGoalTarget(target, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
                } else {
                    entity.setGoalTarget(target, EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
                }
                return true;
            }
        }
        return false;
    }

    public boolean b() {  // canContinueToUse();
        EntityLiving target = this.entity.getGoalTarget();
        if (target == null || !target.isAlive()) return false;
        if (target instanceof EntityHuman && ((EntityHuman)target).abilities.isInvulnerable) return false;

        double followRange = getFollowDistance();
        if (this.entity.h(target) > followRange * followRange) return false; // distanceToSqr

        if (mustSee) {
            if (this.entity.getEntitySenses().a(target)) {
                unseenTicks = 0;
            } else if (++unseenTicks > unseenMemoryTicks) {
                return false;
            }
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
        this.entity.setGoalTarget(null, EntityTargetEvent.TargetReason.FORGOT_TARGET, true);
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

    private static class DistanceComparator implements Comparator<Entity> {
        private final Entity entity;
        public DistanceComparator(Entity entity) {
            this.entity = entity;
        }
        public int compare(Entity entity1, Entity entity2) {
            double distance1 = this.entity.h(entity1);
            double distance2 = this.entity.h(entity2);
            return Double.compare(distance1, distance2);
        }
    }

    private boolean isHoldingBow() { return !this.entity.getItemInMainHand().isEmpty() && this.entity.getItemInMainHand().getItem() == Items.BOW; }

    private double getFollowDistance() {
        AttributeInstance attribute = this.entity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        return attribute == null ? 16.0D : attribute.getValue();
    }
}