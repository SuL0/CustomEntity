package me.sul.customentity.goal.scav.phasegoal;

import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.goal.EasilyModifiedPathfinderGoal;
import me.sul.customentity.goal.scav.ScavCombatPhase;

public class PathfinderGoalScavLookForwardInCombat extends EasilyModifiedPathfinderGoal {
    private final EntityScav nmsEntity;

    public PathfinderGoalScavLookForwardInCombat(EntityScav nmsEntity) {
        super(nmsEntity);
        this.nmsEntity = nmsEntity;
    }

    @Override
    public boolean canUse() {
        return nmsEntity.scavCombatPhase == ScavCombatPhase.SHOOT_TARGET || nmsEntity.scavCombatPhase == ScavCombatPhase.CHASE_TARGET_LASTSEEN;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        if (nmsEntity.scavCombatPhase == ScavCombatPhase.SHOOT_TARGET) {
            nmsEntity.getControllerLook().a(getGoalTarget(), 30.0F, 30.0F);  // setLookAt
        }
        else if (nmsEntity.scavCombatPhase == ScavCombatPhase.CHASE_TARGET_LASTSEEN) {
            // TODO: 가고있는 방향으로
            nmsEntity.getControllerLook().a(getGoalTarget(), 30.0F, 30.0F);  // setLookAt
        }
        // nmsEntity.a(goalTarget, 30.0F, 30.0F); // lookAt - 얘가 더 빠르게 고개를 돌림
    }

}
