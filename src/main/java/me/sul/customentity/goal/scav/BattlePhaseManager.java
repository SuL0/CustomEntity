package me.sul.customentity.goal.scav;

import me.sul.customentity.entity.EntityScav;

public class BattlePhaseManager {
    private final EntityScav nmsEntity;

    public BattlePhaseManager(EntityScav nmsEntity) {
        this.nmsEntity = nmsEntity;
        nmsEntity.scavBattlePhase = ScavBattlePhase.NOT_IN_BATTLE;
    }

    public void updateBattlePhase() {
        if (nmsEntity.getGoalTarget() == null) {
            nmsEntity.scavBattlePhase = ScavBattlePhase.NOT_IN_BATTLE;
        }
        else if (nmsEntity.unseenTick == 0) {
            nmsEntity.scavBattlePhase = ScavBattlePhase.SHOOT_TARGET;
        }
        else if (nmsEntity.unseenTick >= 20) {
            nmsEntity.scavBattlePhase = ScavBattlePhase.CHASE_TARGET_LASTSEEN;
        }
    }
}
