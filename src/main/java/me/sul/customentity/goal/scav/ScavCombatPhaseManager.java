package me.sul.customentity.goal.scav;

import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.util.PathfinderUtil;
import net.minecraft.server.v1_12_R1.EntityLiving;
import org.bukkit.event.entity.EntityTargetEvent;

public class ScavCombatPhaseManager {
    private static final int MAX_UNSEEN_TICK = 200;

    private final EntityScav nmsEntity;


    public ScavCombatPhaseManager(EntityScav nmsEntity) {
        this.nmsEntity = nmsEntity;
        nmsEntity.setScavCombatPhase(ScavCombatPhase.NOT_IN_COMBAT);
    }

    public void updateUnseenTicks(boolean canSeeTarget, int targetUpdatePeriod) {
        if (canSeeTarget) {
            nmsEntity.setUnseenTick(0);
        } else {
            nmsEntity.setUnseenTick(nmsEntity.getUnseenTick() + targetUpdatePeriod);
        }

        if (nmsEntity.getUnseenTick() > MAX_UNSEEN_TICK) {
            nmsEntity.setUnseenTick(0);
            PathfinderUtil.removeGoalTarget(nmsEntity);
        }
    }

    public void updateCombatPhase() {
        if (nmsEntity.getGoalTarget() == null) {
            nmsEntity.setScavCombatPhase(ScavCombatPhase.NOT_IN_COMBAT);
        }
        else if (nmsEntity.getUnseenTick() == 0) {
            nmsEntity.setScavCombatPhase(ScavCombatPhase.SHOOT_TARGET);
        }
        else if (nmsEntity.getUnseenTick() >= 20) {
            nmsEntity.setScavCombatPhase(ScavCombatPhase.CHASE_TARGET_LASTSEEN);
        }
    }

    // 즉각 반응하는게 아니라, 간접적으로 TargetSelector에 의해서 1틱 뒤에 실행된다고 생각해야 함
    public static void forceEntityToGetOutOfCombat(EntityScav me) {
//        me.setGoalTarget(null);  // 이건 절대 있으면 안됨. scavCombatPhase를 바꾸지 않은 상태에서 goal을 제거해버리면 뒤늦은 goal에서 오류가 나게됨. -> 그렇다고 해서 scavCombatPhase를 직접적으로 바꾸는 것도 안됨.
        me.setUnseenTick(10000);
    }
    public static void forceEntityToChaseTarget(EntityScav me, EntityLiving target) {
        me.setGoalTarget(target, EntityTargetEvent.TargetReason.CUSTOM, true);
        me.setUnseenTick(20);
    }
}
