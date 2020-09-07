package me.sul.customentity.goal.scav.phasegoal;

import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.goal.EasilyModifiedPathfinderGoal;
import me.sul.customentity.goal.scav.ScavBattlePhase;

import java.lang.reflect.Field;

public class PathfinderGoalScavMovementWhileShootingTarget extends EasilyModifiedPathfinderGoal {
    private final EntityScav nmsEntity;

    private boolean strafingClockwise = true;
    private Field speedField;

    public PathfinderGoalScavMovementWhileShootingTarget(EntityScav nmsEntity) {
        super(nmsEntity);
        this.nmsEntity = nmsEntity;
    }

    @Override
    public boolean canUse() {
        return nmsEntity.scavBattlePhase == ScavBattlePhase.SHOOT_TARGET;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        stopNavigation();
    }


    @Override
    public void tick() {
        if (getRandom().nextInt(20) == 0) {
            if (getRandom().nextFloat() < 0.3D)
                strafingClockwise = !strafingClockwise;

            try {
                nmsEntity.getControllerMove().a(0.0F, strafingClockwise ? 0.5F : -0.5F);  // strafe.
                if (speedField == null) {
                    speedField = nmsEntity.getControllerMove().getClass().getDeclaredField("e");
                    speedField.setAccessible(true);
                }
                speedField.setDouble(nmsEntity.getControllerMove(), 0.6D);
            } catch (NoSuchFieldException | IllegalAccessException ignored) { }

//            nmsEntity.a(goalTarget, 30.0F, 30.0F); // lookAt - 얘가 더 빠르게 고개를 돌림
        }
    }
}