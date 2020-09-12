package me.sul.customentity.goal.scav;

import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.goal.EasilyModifiedPathfinderGoal;
import me.sul.customentity.util.DistanceComparator;
import me.sul.customentity.util.ScavUtil;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.List;

public class PathfinderGoalScavTargetSelector extends EasilyModifiedPathfinderGoal {
    private static final int TARGET_UPDATE_PERIOD = 10;

    private final EntityScav nmsEntity;
    private final Entity bukkitEntity;
    private final DistanceComparator.Bukkit bukkitDistanceComparator;

    private boolean canSeeTarget = true; // TARGET_UPDATE_PERIOD마다 업데이트
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
        if (tickCnt++ % TARGET_UPDATE_PERIOD == 0 || (getGoalTarget() != null && !isInTargetableState(getGoalTarget()))) { // , 원래 유지하던 타겟이 공격할 수 없는 상태가 되면, 바로 다른 타겟을 찾아봄.
            searchAndSelectTarget();
            nmsEntity.getScavCombatPhaseManager().updateUnseenTicks(canSeeTarget, TARGET_UPDATE_PERIOD);
            nmsEntity.getScavCombatPhaseManager().updateCombatPhase();
        }
    }


    // NOTE: 0. 나를 공격한 엔티티
    //       1. 보이는 가장 가까운 플레이어
    //       2. 보이거나 없어진 현재 타게팅된 엔티티
    //       3. 6칸 안의 가장 가까운 엔티티
    //       4. 보이는 가장 가까운 몹
    private void searchAndSelectTarget() {
        double followDistance = getFollowDistance();

        List<Entity> nearBukkitEntityList = bukkitEntity.getNearbyEntities(followDistance, followDistance, followDistance);
        nearBukkitEntityList.sort(bukkitDistanceComparator);

        // 타게팅 대상이 될 수 없는 값들은 리스트에서 모두 제거
        nearBukkitEntityList.removeIf(nearBukkitEntity -> !(nearBukkitEntity instanceof Monster || nearBukkitEntity instanceof Player));
        nearBukkitEntityList.removeIf(nearBukkitEntity -> (((CraftEntity) nearBukkitEntity).getHandle().getClass().isInstance(nmsEntity)) || !isInTargetableState((EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle()));

        // 0. 나를 공격한 엔티티
        if (nmsEntity.hurtTimestamp != checkedHurtTimestamp) {
            checkedHurtTimestamp = nmsEntity.hurtTimestamp;
            if (nmsEntity.getLastDamager() != null) {
                if (isInTargetableState(nmsEntity.getLastDamager())) {
                    setGoalTarget(nmsEntity.getLastDamager(), true, (nmsEntity.getLastDamager() instanceof EntityPlayer));
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
                setGoalTarget(currentNmsTarget, true, (currentNmsTarget instanceof EntityPlayer));
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
                        ScavCombatPhaseManager.forceEntityToChaseTarget(nmsEntity, null); // unseenTick 설정용
                        setGoalTarget((EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle(), false, (nearBukkitEntity instanceof Player));
                        return;
                    }
                    setGoalTarget((EntityLiving) ((CraftEntity) nearBukkitEntity).getHandle(), true, (nearBukkitEntity instanceof Player));
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
        if (getGoalTarget() != null) removeGoalTarget();
    }



    private void setGoalTarget(EntityLiving nmsTarget, boolean canSeeTarget, boolean alertOther) {
        this.canSeeTarget = canSeeTarget;

        setGoalTarget(nmsTarget, EntityTargetEvent.TargetReason.CUSTOM);
        if (alertOther && getGoalTarget() != null) {
            ScavUtil.alertOthers(getGoalTarget(), 25, nmsEntity, 25);
        }
    }

    @Override
    public void removeGoalTarget() {
        super.removeGoalTarget();
        nmsEntity.setUnseenTick(0);
    }
}
