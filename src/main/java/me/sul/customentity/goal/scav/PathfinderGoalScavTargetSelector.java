package me.sul.customentity.goal.scav;

import com.sun.istack.internal.NotNull;
import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.goal.EasilyModifiedPathfinderGoal;
import me.sul.customentity.util.DistanceComparator;
import me.sul.customentity.util.PathfinderUtil;
import net.minecraft.server.v1_12_R1.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.List;

public class PathfinderGoalScavTargetSelector extends EasilyModifiedPathfinderGoal {
    private static final int TARGET_UPDATE_PERIOD = 10;
    private static final int MAX_UNSEEN_TICK = 300;

    private final EntityScav nmsEntity;
    private final Entity bukkitEntity;
    private final DistanceComparator.Bukkit bukkitDistanceComparator;

    private int tickCnt = 0;
    private int checkedHurtTimestamp = 0;

    public PathfinderGoalScavTargetSelector(EntityScav nmsEntity) {
        super(nmsEntity);
        this.nmsEntity = nmsEntity;
        this.bukkitEntity = nmsEntity.getBukkitEntity();

        this.bukkitDistanceComparator = new DistanceComparator.Bukkit(bukkitEntity);
    }

    @Override
    public boolean canUse() { return true; }

    @Override
    public boolean canContinueToUse() { return true; }

    @Override
    public void tick() {
        if (tickCnt == Integer.MAX_VALUE) tickCnt = 0;
        if (tickCnt++ % TARGET_UPDATE_PERIOD == 0 || (getGoalTarget() != null && !isInTargetableState(getGoalTarget()) )) { // , 원래 유지하던 타겟이 공격할 수 없는 상태가 되면, 바로 다른 타겟을 찾아봄.
            selectTarget();
            nmsEntity.battlePhaseManager.updateBattlePhase();
        }
    }


    // NOTE: 0. 나를 공격한 엔티티
    //       1. 보이는 가장 가까운 플레이어
    //       2. 보이거나 없어진 현재 타게팅된 엔티티
    //       3. 6칸 안의 가장 가까운 엔티티
    //       4. 보이는 가장 가까운 몹
    private void selectTarget() {
        double followDistance = getFollowDistance();

        List<Entity> nearBukkitEntityList = bukkitEntity.getNearbyEntities(followDistance, followDistance, followDistance);
        nearBukkitEntityList.sort(bukkitDistanceComparator);

        // 타게팅 대상이 될 수 없는 값들은 리스트에서 모두 제거
        nearBukkitEntityList.removeIf(nearBukkitEntity -> !(nearBukkitEntity instanceof Monster || nearBukkitEntity instanceof Player));
        nearBukkitEntityList.removeIf(nearBukkitEntity -> (((CraftEntity) nearBukkitEntity).getHandle().getClass().isInstance(nmsEntity)) || !isInTargetableState((EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle()));

        // TODO: 어디로 옮기지. Chase 끝나고 전투 상황 종료시키기 위한 것임
        if (nmsEntity.unseenTick > MAX_UNSEEN_TICK) {
            removeGoalTarget();
        }


        // 0. 나를 공격한 엔티티
        if (nmsEntity.hurtTimestamp != checkedHurtTimestamp) {
            checkedHurtTimestamp = nmsEntity.hurtTimestamp;
            if (nmsEntity.getLastDamager() != null) {
                if (isInTargetableState(nmsEntity.getLastDamager())) {
                    setGoalTarget(nmsEntity.getLastDamager(), true, true);
                    return;
                }
            }
        }
        // 1. 보이는 가장 가까운 플레이어
        for (Entity nearBukkitEntity : nearBukkitEntityList) {
            if (nearBukkitEntity instanceof Player) {
                if (isInSight(nearBukkitEntity)) {
                    setGoalTarget((EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle(), true, true);
                    return;
                }
            }
        }
        // 2. 보이거나 없어진 현재 타게팅된 엔티티
        EntityLiving currentNmsTarget = getGoalTarget();
        if (currentNmsTarget != null && isInTargetableState(currentNmsTarget)) {
            if (isInSight(currentNmsTarget.getBukkitEntity())) {
                // 현재 타겟 계속 유지
                setGoalTarget(currentNmsTarget, true, false);
                return;
            } else {
                // 현재 타겟을 볼 수 없음
                setGoalTarget(currentNmsTarget, false, false);
                return;
            }
        }
        // 3. 6칸 안의 가장 가까운 엔티티
        for (Entity nearBukkitEntity : nearBukkitEntityList) {
            if (nearBukkitEntity instanceof Monster || nearBukkitEntity instanceof Player) {
                if (bukkitEntity.getLocation().distance(nearBukkitEntity.getLocation()) <= 6) {
                    if (!((LivingEntity) bukkitEntity).hasLineOfSight(nearBukkitEntity)) { // 블럭에 가려져있다면, 6칸 안에 있는 적에게 쫒아가게끔
                        setGoalTarget((EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle(), false, true);
                        return;
                    }
                    setGoalTarget((EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle(), true, true);
                    return;
                }
            }
        }
        // 4. 보이는 15칸 안의 몹
        for (Entity nearBukkitEntity : nearBukkitEntityList) {
            if (nearBukkitEntity instanceof Monster) {
                if (bukkitEntity.getLocation().distance(nearBukkitEntity.getLocation()) <= 15 && isInSight(nearBukkitEntity)) {
                    setGoalTarget((EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle(), true, false);
                    return;
                }
            }
        }

        // 타게팅 할 적이 없음
        if (getGoalTarget() != null) {
            removeGoalTarget();
        }
    }



    private void setGoalTarget(@NotNull EntityLiving nmsTarget, boolean canSeeTarget, boolean alertOther) {
        if (canSeeTarget || !nmsTarget.equals(getGoalTarget())) {  // ,안보이는 적을 처음부터 타게팅(5칸 안의 적) 했을 때 unseenTick을 초기화 해줄 곳이 없기 때문
            nmsEntity.unseenTick = 0;
        }
        if (!canSeeTarget) {
            nmsEntity.unseenTick += TARGET_UPDATE_PERIOD;

            // unseen > maxUnseenTick 일 시 전투상태 해제
            if (nmsEntity.unseenTick > MAX_UNSEEN_TICK) {
                removeGoalTarget();
                return;
            }
        }

        setGoalTarget(nmsTarget, EntityTargetEvent.TargetReason.CUSTOM);
        if (alertOther) {
            PathfinderUtil.alertOthers(nmsEntity, 10, 20);
        }
    }

    @Override
    public void removeGoalTarget() {
        super.removeGoalTarget();
        nmsEntity.unseenTick = 0;
        Bukkit.getServer().broadcastMessage("§c- REMOVE GOAL TARGET");
    }
}
