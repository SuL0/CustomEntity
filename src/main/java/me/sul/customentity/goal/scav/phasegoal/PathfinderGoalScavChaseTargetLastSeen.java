package me.sul.customentity.goal.scav.phasegoal;

import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.goal.EasilyModifiedPathfinderGoal;
import me.sul.customentity.goal.scav.ScavCombatPhase;
import me.sul.customentity.goal.scav.ScavCombatPhaseManager;
import me.sul.customentity.util.EntityAnimation;
import net.minecraft.server.v1_12_R1.EnumHand;

public class PathfinderGoalScavChaseTargetLastSeen extends EasilyModifiedPathfinderGoal {
    public static double CHASE_SPEED = 1.3D;

    private final EntityScav nmsEntity;

    public PathfinderGoalScavChaseTargetLastSeen(EntityScav nmsEntity) {
        super(nmsEntity);
        this.nmsEntity = nmsEntity;
    }

    @Override
    public boolean canUse() {
        return nmsEntity.scavCombatPhase == ScavCombatPhase.CHASE_TARGET_LASTSEEN && isInTargetableState(getGoalTarget());
    }

    @Override
    public boolean canContinueToUse() {
        if (isNavigationDone()) {
            ScavCombatPhaseManager.forceEntityToGetOutOfCombat(nmsEntity);
            return false;
        }
        return canUse();
    }

    @Override
    public void start() {
        // nmsEntity.getControllerMove().a(0.0F, 0.0F);
        // 모든 움직임은 사실 ControllerMove에서 관리함(Navigation 포함).
        // 그렇기에 위의 메소드를 호출하고, Navigation의 moveTo()를 호출하면 위의 메소드가 씹히게됨. 그러므로 아래의 메소드를 직접 사용해줘야함.
        nmsEntity.p(0.0F);  // strafeForwards
        nmsEntity.n(0.0F);  // strafeRight

        EntityAnimation.startUsingItem(nmsEntity, EnumHand.OFF_HAND); // 방패 사용
        moveToLoc(getGoalTarget().getBukkitEntity().getLocation().add(0, 1, 0), CHASE_SPEED, true);
    }

    @Override
    public void stop() {
        stopNavigation();
        EntityAnimation.stopUsingItem(nmsEntity); // 방패 사용
    }
}
