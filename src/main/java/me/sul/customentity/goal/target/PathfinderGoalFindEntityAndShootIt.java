package me.sul.customentity.goal.target;

import me.sul.customentity.entity.CustomEntity;
import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.entityweapon.EntityCrackShotWeapon;
import me.sul.customentity.util.DistanceComparator;
import net.minecraft.server.v1_12_R1.*;
import net.sf.cglib.asm.$ByteVector;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;

// PathfinderGoalNearestAttackableTarget 기반
public class PathfinderGoalFindEntityAndShootIt<T extends EntityCreature & CustomEntity> extends PathfinderGoal {
    private final T nmsEntity;
    private final Entity bukkitEntity;
    private int checkedHurtTimestamp;
    private final int randomInterval;

    private final int fireDelay;
    private final float projDamage;
    private final float projSpread;
    private final int projSpeed;

    private final DistanceComparator.Bukkit bukkitDistanceComparator;

    private final int maxUnseenMemoryTicks;
    private int unseenTicks = 0;
    private int fireDelayCnt = 0;
    private int cntForCanContinueToUse = 0;

    public PathfinderGoalFindEntityAndShootIt(T nmsCreature, int fireDelay, float projDamage, float projSpread, int projSpeed) {
        this(nmsCreature, fireDelay, projDamage, projSpread, projSpeed, 100, 3);
    }
    public PathfinderGoalFindEntityAndShootIt(T nmsCreature, int fireDelay, float projDamage, float projSpread, int projSpeed, int maxUnseenMemoryTicks, int randomInterval) {
        this.nmsEntity = nmsCreature;
        this.bukkitEntity = nmsCreature.getBukkitEntity();
        this.fireDelay = fireDelay;
        this.projDamage = projDamage;
        this.projSpread = projSpread;
        this.projSpeed = projSpeed;
        this.randomInterval = randomInterval;
        this.maxUnseenMemoryTicks = maxUnseenMemoryTicks;

        this.bukkitDistanceComparator = new DistanceComparator.Bukkit(nmsEntity.getBukkitEntity());
        this.a(1);
    }

    // 항상 a()가 돌고있음
    @Override
    public boolean a() {  // canUse()
        if (nmsEntity.getRandom().nextInt(randomInterval) != 0) return false;

        TargetEntity targetEntity = getAppropriateTarget();
        setGoalTarget(targetEntity);
        return (targetEntity != null);
    }

    public boolean b() {  // canContinueToUse();
        if (cntForCanContinueToUse == Integer.MAX_VALUE) cntForCanContinueToUse = 0;
        if (++cntForCanContinueToUse % 10 == 0 || !isInTargetableState(nmsEntity.getGoalTarget())) { // + 원래 유지하던 타겟이 공격할 수 없는 상태가 되면, 바로 다른 타겟을 찾아봄.
            TargetEntity targetEntity = getAppropriateTarget();
            setGoalTarget(targetEntity);
            return (targetEntity != null);
        }
        return true;
    }

    @Override
    public void c() {  // start()
        if (isHoldingBow()) {
            nmsEntity.c(EnumHand.MAIN_HAND); // startUsingItem()
        }
    }


    @Override
    public void d() {  // stop()
        setGoalTarget(null);
        fireDelayCnt = 0;
        if (isHoldingBow()) {
            this.nmsEntity.cN(); // stopUsingItem()
        }
    }

    @Override
    public void e() {  // tick()
        EntityLiving target = nmsEntity.getGoalTarget(); // canContinueToUse()에서 걸러주기때문에 null일 수가 없음
        if (unseenTicks == 0) { // 시야에 타겟이 있는가
            nmsEntity.setSeeingTarget(true);
            if (fireDelayCnt % fireDelay == 0) {
                EntityCrackShotWeapon.fireProjectile(nmsEntity.getBukkitEntity(), target.getBukkitEntity(), projSpread, projSpeed, projDamage);
            }
        } else {
            nmsEntity.setSeeingTarget(false);  // ? 이거 왜넣은거지
        }
        fireDelayCnt++;
    }


    private void setGoalTarget(@Nullable TargetEntity targetEntity) {
        if (targetEntity == null || !targetEntity.isTargetingReasonHaveToMaintainUnseenTicks()) {
            unseenTicks = 0;
        }
        if (targetEntity == null) {
            if (nmsEntity.getGoalTarget() != null) {
                nmsEntity.setGoalTarget(null, EntityTargetEvent.TargetReason.CUSTOM, true);
                nmsEntity.setSeeingTarget(false);
            }
        } else if (!targetEntity.getNmsEntity().equals(nmsEntity.getGoalTarget())) {
            nmsEntity.setGoalTarget(targetEntity.getNmsEntity(), EntityTargetEvent.TargetReason.CUSTOM, true);
        }
    }


    private boolean isHoldingBow() { return !this.nmsEntity.getItemInMainHand().isEmpty() && this.nmsEntity.getItemInMainHand().getItem() == Items.BOW; }

    private double getFollowDistance() {
        AttributeInstance attribute = this.nmsEntity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        return attribute == null ? 16.0D : attribute.getValue();
    }


    // 벡터 내적. p의 벡터에 대해서 파라미터의 벡터가 얼마만큼의 도움을 줄 수 있는가?
    // aVec와 bVec 내적 = aVec크기 * bVec크기 * cos(세타)
    // -> 세타 = acos(aVec와 bVec내적 / aVec크기 * bVec크기)
    // NOTE: 0. 나를 공격한 엔티티
    //       1. 보이는 가장 가까운 플레이어
    //       2. 보이거나 없어진 현재 타게팅된 엔티티
    //       3. 5칸 안의 가장 가까운 몹
    //       4. 보이는 가장 가까운 몹
    private TargetEntity getAppropriateTarget() {
        double followDistance = getFollowDistance();

        List<Entity> nearBukkitEntityList = bukkitEntity.getNearbyEntities(followDistance, followDistance, followDistance);
        nearBukkitEntityList.sort(bukkitDistanceComparator);
        // 0. 나를 공격한 엔티티
        if (nmsEntity.hurtTimestamp != checkedHurtTimestamp) {
            checkedHurtTimestamp = nmsEntity.hurtTimestamp;
            if (nmsEntity.getLastDamager() != null) {
                if (isInTargetableState(nmsEntity.getLastDamager())) {
                    return new TargetEntity(0, false, nmsEntity.getLastDamager());
                }
            }
        }
        // 1. 보이는 가장 가까운 플레이어
        for (Entity nearBukkitEntity : nearBukkitEntityList) {
            if (nearBukkitEntity instanceof Player) {
                if (isInSight(nearBukkitEntity)) {
                    return new TargetEntity(1, false, (EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle());
                }
            }
        }
        // 2. 보이거나 없어진 현재 타게팅된 엔티티 // TODO: 따라가게 해야 함
        EntityLiving currentNmsTarget = nmsEntity.getGoalTarget();
        if (currentNmsTarget != null &&isInTargetableState(currentNmsTarget)) {
            unseenTicks += 10;
            if (isInSight(currentNmsTarget.getBukkitEntity())) {
                unseenTicks = 0;
                return new TargetEntity(2, true, currentNmsTarget);
            } else if (unseenTicks <= maxUnseenMemoryTicks) {
                return new TargetEntity(2, true, currentNmsTarget);
            }
        }
        // 3. 5칸 안의 가장 가까운 몹
        for (Entity nearBukkitEntity : nearBukkitEntityList) {
            if (nearBukkitEntity instanceof Monster) {
                if (bukkitEntity.getLocation().distance(nearBukkitEntity.getLocation()) <= 5 && ((LivingEntity)bukkitEntity).hasLineOfSight(nearBukkitEntity)) {
                    return new TargetEntity(3, false, (EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle());
                }
            }
        }
        
        // 4. 보이는 가장 가까운 몹
        for (Entity nearBukkitEntity : nearBukkitEntityList) {
            if (nearBukkitEntity instanceof Monster) {
                if (isInSight(nearBukkitEntity)) {
                    return new TargetEntity(4, false, (EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle());
                }
            }
        }
        // 타겟 없음
        return null;
    }
    private boolean isInTargetableState(EntityLiving nmsOpponent) {
        if (nmsOpponent == null || !nmsOpponent.isAlive() || nmsOpponent.getClass().equals(this.nmsEntity.getClass())) return false;
        if (nmsOpponent instanceof EntityHuman && (((EntityHuman)nmsOpponent).abilities.isInvulnerable || ((Player)nmsOpponent.getBukkitEntity()).getGameMode() != GameMode.SURVIVAL)) return false;
        if (nmsOpponent.getBukkitEntity().getLocation().distance(bukkitEntity.getLocation()) > getFollowDistance()) return false;
        return true;
    }

    private boolean isInSight(Entity bukkitOpponent) {
        if (!(bukkitOpponent instanceof LivingEntity)) return false;
        Vector sightVector = bukkitEntity.getLocation().getDirection();
        Vector toOpponentVector = bukkitOpponent.getLocation().toVector().subtract(bukkitEntity.getLocation().toVector());
        double angle = getAngleBetweenTwoVectors(sightVector, toOpponentVector);

        if (angle <= 70 && ((LivingEntity)bukkitEntity).hasLineOfSight(bukkitOpponent)) {
            if (isInTargetableState((EntityLiving)((CraftEntity)bukkitOpponent).getHandle())) {
                return true;
            }
        }
        return false;
    }
    private double getAngleBetweenTwoVectors(Vector aVec, Vector bVec) {
        double cosAngle = (aVec.clone().dot(bVec)) / (aVec.length() * bVec.length());
        return Math.toDegrees(Math.acos(cosAngle)); // acos만 하면 라디안이 나와서 각도로 변환해야 함
    }
}