package me.sul.customentity.goal.scav.phasegoal;

import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.entityweapon.GunUtil;
import me.sul.customentity.goal.EasilyModifiedPathfinderGoal;
import me.sul.customentity.goal.scav.ScavBattlePhase;

public class PathfinderGoalScavHandleGun extends EasilyModifiedPathfinderGoal {
    private static final int fireDelay = 3;
    private static final float projDamage = 4F;
    private static final float projSpread = 5F;
    private static final int projSpeed = 7;

    private final EntityScav nmsEntity;

    private int fireDelayCnt = 0;

    public PathfinderGoalScavHandleGun(EntityScav nmsEntity) {
        super(nmsEntity);
        this.nmsEntity = nmsEntity;
    }

    @Override
    public boolean canUse() {
        return nmsEntity.scavBattlePhase == ScavBattlePhase.SHOOT_TARGET && isInTargetableState(getGoalTarget());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        if (fireDelayCnt++ % fireDelay == 0) {
            GunUtil.fireProjectile(bukkitEntity, getGoalTarget().getBukkitEntity(), projSpread, projSpeed, projDamage);
        }
    }
}
