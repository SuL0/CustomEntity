package me.sul.customentity.goal;

import me.sul.customentity.Main;
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
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    boolean strafingClockwise = false;


    private static final int INTERVAL_OF_RE_SEARCH_TARGET = 10;
    private static final int CHASE_TICK = 30;

    public PathfinderGoalFindEntityAndShootIt(T nmsCreature, int fireDelay, float projDamage, float projSpread, int projSpeed) {
        this(nmsCreature, fireDelay, projDamage, projSpread, projSpeed, 250, 3);
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
    @Override
    public boolean a() { return canUse(); }
    @Override
    public boolean b() { return canContinueToUse(); }
    @Override
    public void c() { start(); }
    @Override
    public void d() { stop(); }
    @Override
    public void e() { tick(); }

    // 4틱마다 반복
    public boolean canUse() {
        if (nmsEntity.getGoalTarget() != null) return true;  // alertOthers()에 의해서 GoalTarget이 정해질 수도 있음
        if (nmsEntity.getRandom().nextInt(randomInterval) != 0) return false;

        TargetEntity targetEntity = getAppropriateTarget();
        setGoalTarget(targetEntity);
        return (targetEntity != null);
    }

    // 1틱마다 반복
    public boolean canContinueToUse() {
        if (cntForCanContinueToUse == Integer.MAX_VALUE) cntForCanContinueToUse = 0;
        if (++cntForCanContinueToUse % INTERVAL_OF_RE_SEARCH_TARGET == 0 || !isInTargetableState(nmsEntity.getGoalTarget())) { // + 원래 유지하던 타겟이 공격할 수 없는 상태가 되면, 바로 다른 타겟을 찾아봄.
            TargetEntity targetEntity = getAppropriateTarget();
            setGoalTarget(targetEntity);
            return (targetEntity != null);
        }
        return true;
    }

    public void start() {
        if (isHoldingBow()) {
            nmsEntity.c(EnumHand.MAIN_HAND); // startUsingItem()
        }
    }

    public void stop() {
        setGoalTarget(null);
        nmsEntity.getNavigation().p();
        fireDelayCnt = 0;
        if (isHoldingBow()) {
            this.nmsEntity.cN(); // stopUsingItem()
        }
    }

    public void tick() {  // goalTarget이 무조건 있음.
        tick_handleWeapon();
        tick_moving();
    }
    private void tick_handleWeapon() {
        EntityLiving target = nmsEntity.getGoalTarget(); // canContinueToUse()에서 걸러주기때문에 null일 수가 없음
        if (unseenTicks == 0) { // 시야에 타겟이 있을 때
            if (fireDelayCnt % fireDelay == 0) {
                EntityCrackShotWeapon.fireProjectile(nmsEntity.getBukkitEntity(), target.getBukkitEntity(), projSpread, projSpeed, projDamage);
            }
        }
        fireDelayCnt++;
    }
    private void tick_moving() {
        EntityLiving goalTarget = nmsEntity.getGoalTarget();
        nmsEntity.getControllerLook().a(goalTarget, 30.0F, 30.0F);  // setLookAt
        
        // 몹 탐색 주기(unseenTicks 바뀌는 주기)랑 똑같은 주기를 주었음.
        if (cntForCanContinueToUse % INTERVAL_OF_RE_SEARCH_TARGET == 0) {
            if (unseenTicks == 0) {
                nmsEntity.getNavigation().p();
                if (nmsEntity.getRandom().nextFloat() < 0.3D) {
                    strafingClockwise = !strafingClockwise;
                }
                try {
                    nmsEntity.getControllerMove().a(0.0F, strafingClockwise ? 0.5F : -0.5F);  // strafe.
                    Field speed = nmsEntity.getControllerMove().getClass().getDeclaredField("e");
                    speed.setAccessible(true);
                    speed.setDouble(nmsEntity.getControllerMove(), 0.6D);
                } catch (NoSuchFieldException | IllegalAccessException ignored) { }
                nmsEntity.a(goalTarget, 30.0F, 30.0F); // lookAt - 얘가 더 빠르게 고개를 돌림
            } else if (unseenTicks == CHASE_TICK) { // 목표를 놓치고 딱 한 번 실행 - 문에서 치고 빠지는거 안쪽에 있을 때의 좌표를 찍으려고 약간 텀을 두었음
                // nmsEntity.getControllerMove().a(0.0F, 0.0F);
                // 모든 움직임은 사실 ControllerMove에서 관리함(Navigation 포함).
                // 그렇기에 위의 메소드를 호출하고, Navigation의 moveTo()를 호출하면 위의 메소드가 씹히게됨. 그러므로 아래의 메소드를 직접 사용해줘야함.
                nmsEntity.p(0.0F);  // strafeForwards
                nmsEntity.n(0.0F);  // strafeRight
                moveToLocConsideringDoor(goalTarget.getBukkitEntity().getLocation(), 1.3D);  // TODO: 방벽 들고 돌격

            } else if (unseenTicks > CHASE_TICK && nmsEntity.getNavigation().o()) {
                unseenTicks = maxUnseenMemoryTicks;
            }
        }
    }


    private void setGoalTarget(@Nullable TargetEntity targetEntity) {
        if (targetEntity == null || targetEntity.resetUnseenTicks()) {
            unseenTicks = 0;
        }
        if (targetEntity == null) {
            if (nmsEntity.getGoalTarget() != null) {
                nmsEntity.setGoalTarget(null, EntityTargetEvent.TargetReason.CUSTOM, true);
            }
        } else if (!targetEntity.getNmsEntity().equals(nmsEntity.getGoalTarget())) {
            nmsEntity.setGoalTarget(targetEntity.getNmsEntity(), EntityTargetEvent.TargetReason.CUSTOM, true);
            if (targetEntity.haveToAlertOther())
                alertOthers();
        }
    }

    private void moveToLocConsideringDoor(Location destinationLoc, double speed) {
        Navigation navigation = (Navigation) nmsEntity.getNavigation();
        navigation.a(true); // canOpenDoors. b(): canPassDoors, c(): canFloat.  위치선정을 할 때 문을 열고 가는 것을 고려해서 찍어줌
        PathEntity goalPath = navigation.a(destinationLoc.getX(), destinationLoc.getY(), destinationLoc.getZ());
        nmsEntity.getNavigation().a(goalPath, speed);
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
    //       3. 6칸 안의 가장 가까운 엔티티
    //       4. 보이는 가장 가까운 몹
    private TargetEntity getAppropriateTarget() {
        double followDistance = getFollowDistance();

        List<Entity> nearBukkitEntityList = bukkitEntity.getNearbyEntities(followDistance, followDistance, followDistance);
        nearBukkitEntityList.sort(bukkitDistanceComparator);

        // 타게팅 대상이 될 수 없는 값들은 리스트에서 모두 제거
        nearBukkitEntityList.removeIf(nearBukkitEntity -> !(nearBukkitEntity instanceof LivingEntity));
        nearBukkitEntityList.removeIf(nearBukkitEntity -> nmsEntity.getClass().equals(((CraftEntity) nearBukkitEntity).getHandle().getClass()) || !isInTargetableState((EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle()));


        // 0. 나를 공격한 엔티티
        if (nmsEntity.hurtTimestamp != checkedHurtTimestamp) {
            checkedHurtTimestamp = nmsEntity.hurtTimestamp;
            if (nmsEntity.getLastDamager() != null) {
                if (isInTargetableState(nmsEntity.getLastDamager())) {
                    return new TargetEntity(0, true, true, nmsEntity.getLastDamager());
                }
            }
        }
        // 1. 보이는 가장 가까운 플레이어
        for (Entity nearBukkitEntity : nearBukkitEntityList) {
            if (nearBukkitEntity instanceof Player) {
                if (isInSight(nearBukkitEntity)) {
                    return new TargetEntity(1, true, true, (EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle());
                }
            }
        }
        // 2. 보이거나 없어진 현재 타게팅된 엔티티
        EntityLiving currentNmsTarget = nmsEntity.getGoalTarget();
        if (currentNmsTarget != null && isInTargetableState(currentNmsTarget)) {
            unseenTicks += INTERVAL_OF_RE_SEARCH_TARGET;
            if (isInSight(currentNmsTarget.getBukkitEntity())) {
                unseenTicks = 0;
                return new TargetEntity(2, false, true, currentNmsTarget);
            } else if (unseenTicks <= maxUnseenMemoryTicks) {
                return new TargetEntity(2, false, false, currentNmsTarget);
            }
        }
        // 3. 6칸 안의 가장 가까운 엔티티
        for (Entity nearBukkitEntity : nearBukkitEntityList) {
            if (nearBukkitEntity instanceof LivingEntity) {
                if (bukkitEntity.getLocation().distance(nearBukkitEntity.getLocation()) <= 6) {
                    if (!((LivingEntity)bukkitEntity).hasLineOfSight(nearBukkitEntity)) { // 블럭에 가려져있다면, 6칸 안에 있는 적에게 쫒아가게끔
                        unseenTicks = CHASE_TICK;
                        return new TargetEntity(3, false, nearBukkitEntity instanceof Player, (EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle());
                    }
                    return new TargetEntity(3, true, nearBukkitEntity instanceof Player, (EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle());
                }
            }
        }
        // 4. 보이는 15칸 안의 몹
        for (Entity nearBukkitEntity : nearBukkitEntityList) {
            if (nearBukkitEntity instanceof Monster) {
                if (bukkitEntity.getLocation().distance(nearBukkitEntity.getLocation()) <= 15 && isInSight(nearBukkitEntity)) {
                    return new TargetEntity(4, true, false, (EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle());
                }
            }
        }

        // 타겟 없음
        return null;
    }

    private boolean isInTargetableState(EntityLiving nmsOpponent) {
        if (nmsOpponent == null || !nmsOpponent.isAlive() || nmsOpponent.getClass().equals(nmsEntity.getClass())) return false;
        if (nmsOpponent instanceof EntityHuman && (((EntityHuman)nmsOpponent).abilities.isInvulnerable || ((Player)nmsOpponent.getBukkitEntity()).getGameMode() != GameMode.SURVIVAL)) return false;
        if (nmsOpponent.getBukkitEntity().getLocation().distance(bukkitEntity.getLocation()) > getFollowDistance()) return false;
        return true;
    }

    private boolean isInSight(Entity bukkitOpponent) {
        if (!(bukkitOpponent instanceof LivingEntity)) return false;
        Vector sightVector = bukkitEntity.getLocation().getDirection();
        Vector toOpponentVector = bukkitOpponent.getLocation().toVector().subtract(bukkitEntity.getLocation().toVector());
        double angle = getAngleBetweenTwoVectors(sightVector, toOpponentVector);

        return angle <= 70 && ((LivingEntity) bukkitEntity).hasLineOfSight(bukkitOpponent);
    }
    private double getAngleBetweenTwoVectors(Vector aVec, Vector bVec) {
        double cosAngle = (aVec.clone().dot(bVec)) / (aVec.length() * bVec.length());
        return Math.toDegrees(Math.acos(cosAngle)); // acos만 하면 라디안이 나와서 각도로 변환해야 함
    }


    private void alertOthers() {
        if (nmsEntity.getGoalTarget() == null) return;
        EntityLiving target = nmsEntity.getGoalTarget();
        Set<Entity> nearBukkitEntityList = new HashSet<>();

        nearBukkitEntityList.addAll(nmsEntity.getBukkitEntity().getNearbyEntities(10, 10, 10));  // nmsEntity 주변
        nearBukkitEntityList.addAll(nmsEntity.getGoalTarget().getBukkitEntity().getNearbyEntities(20, 20, 20));  // 플레이어 주변

        for (Entity nearBukkitEntity : nearBukkitEntityList) {
            net.minecraft.server.v1_12_R1.Entity nearNmsEntity = ((CraftEntity)nearBukkitEntity).getHandle();
            if (nearNmsEntity.getClass().equals(nmsEntity.getClass())) {

                EntityScav nearEntityScav = (EntityScav)nearNmsEntity;
                if (nearEntityScav.getGoalTarget() == null) {
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                        if (nearEntityScav.getGoalTarget() == null && isInTargetableState(target)) {
                            nearEntityScav.getPathfinderGoalFindEntityAndShootIt().setGoalTarget(new TargetEntity(-1, true, false, target));
                        }
                    }, nmsEntity.getRandom().nextInt(15) + 5);
                }
            }

        }
    }
}