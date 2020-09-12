package me.sul.customentity.util;

import me.sul.customentity.Main;
import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.goal.scav.ScavCombatPhase;
import me.sul.customentity.goal.scav.ScavCombatPhaseManager;
import me.sul.customentity.goal.scav.phasegoal.PathfinderGoalScavChaseTargetLastSeen;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class ScavUtil {
    public static Random random = new Random();

    // TODO: 총 쏘면, 플레이어 주변 몹들 반응
    public static void alertOthers(EntityCreature me, int radiusAroundMe, int radiusAroundTarget) {
        if (me.getGoalTarget() == null) return;
        EntityLiving targetToAlert = me.getGoalTarget();
        Set<Entity> nearBukkitEntityList = new HashSet<>();

        nearBukkitEntityList.addAll(me.getBukkitEntity().getNearbyEntities(radiusAroundMe, radiusAroundMe, radiusAroundMe));  // nmsEntity 주변
        nearBukkitEntityList.addAll(me.getGoalTarget().getBukkitEntity().getNearbyEntities(radiusAroundTarget, radiusAroundTarget, radiusAroundTarget));  // 플레이어 주변
        nearBukkitEntityList.removeIf(e -> !(((CraftEntity)e).getHandle() instanceof EntityScav) || e.equals(me.getBukkitEntity()));

        for (EntityScav nearEntityScav : nearBukkitEntityList.stream()
                .filter (e -> ((CraftEntity)e).getHandle() instanceof EntityScav)
                .map(e -> (EntityScav) ((CraftEntity)e).getHandle())
                .collect(Collectors.toList())) {

            // 처음 보는 적
            if (nearEntityScav.getGoalTarget() == null) {
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {  // 아래의 코드를 실행할 때는 일정 시간이 흘렀음을 주의해야 함 !
                    if (nearEntityScav.isAlive() && nearEntityScav.getGoalTarget() == null
                            && PathfinderUtil.isInTargetableState(me.getBukkitEntity(), targetToAlert, PathfinderUtil.getFollowDistance(me))) {
                        ScavCombatPhaseManager.forceEntityToChaseTarget(nearEntityScav, targetToAlert);
                    }
                }, random.nextInt(15) + 5);
            }

            // 똑같은 적에 대한 위치 업데이트
            else if (nearEntityScav.getGoalTarget() != null && nearEntityScav.getGoalTarget().equals(targetToAlert) && nearEntityScav.scavCombatPhase == ScavCombatPhase.CHASE_TARGET_LASTSEEN) {
                Location currentDestinationLoc = PathfinderUtil.getNavigationDestinationLoc(targetToAlert, nearEntityScav.getNavigation());
                Location targetLoc = targetToAlert.getBukkitEntity().getLocation();
                if (currentDestinationLoc.distance(targetLoc) >= 12) {  // 현재 몹이 가고 있는 목적지와 거리가 12칸 이상 차이날 경우에만 실행
                    PathfinderUtil.moveToLoc(nearEntityScav, targetLoc, PathfinderGoalScavChaseTargetLastSeen.CHASE_SPEED, true);
                }
            }

            // 다른 적을 상대하고 있었다면 그냥 빠져나감
        }
    }
}
