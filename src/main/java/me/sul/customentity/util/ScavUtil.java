package me.sul.customentity.util;

import me.sul.customentity.Main;
import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.goal.scav.ScavCombatPhase;
import me.sul.customentity.goal.scav.ScavCombatPhaseManager;
import me.sul.customentity.goal.scav.phasegoal.PathfinderGoalScavChaseTargetLastSeen;
import net.minecraft.server.v1_12_R1.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class ScavUtil {
    public static Random random = new Random();

    // TODO: 총 쏘면, 플레이어 주변 몹들 반응
    public static void alertOthers(EntityLiving target, int radiusAroundTarget) {
        alertOthers(target, radiusAroundTarget, null, null);
    }

    public static void alertOthers(EntityLiving target, int radiusAroundTarget, @Nullable EntityScav me , @Nullable Integer radiusAroundMe) {
        Set<Entity> nearBukkitEntityList = new HashSet<>(target.getBukkitEntity().getNearbyEntities(radiusAroundTarget, radiusAroundTarget, radiusAroundTarget));  // 타겟 주변
        if (me != null && radiusAroundMe != null) {
            nearBukkitEntityList.addAll(me.getBukkitEntity().getNearbyEntities(radiusAroundMe, radiusAroundMe, radiusAroundMe));  // me 주변
            nearBukkitEntityList.removeIf(e -> !(((CraftEntity) e).getHandle() instanceof EntityScav) || e.equals(me.getBukkitEntity())); // Scav 자신은 삭제
        }

        alertOthers(target, nearBukkitEntityList);
    }

    public static void alertOthers(EntityLiving target, Set<Entity> nearBukkitEntityList) {
        for (EntityScav nearEntityScav : nearBukkitEntityList.stream()
                .filter(e -> ((CraftEntity) e).getHandle() instanceof EntityScav)
                .map(e -> (EntityScav) ((CraftEntity) e).getHandle())
                .collect(Collectors.toList())) {

            // 처음 보는 적
            if (nearEntityScav.scavCombatPhase == ScavCombatPhase.NOT_IN_COMBAT) {
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {  // 아래의 코드를 실행할 때는 일정 시간이 흘렀음을 주의해야 함 !
                    if (nearEntityScav.isAlive() && nearEntityScav.getGoalTarget() == null
                            && PathfinderUtil.isInTargetableState(nearEntityScav.getBukkitEntity(), target, PathfinderUtil.getFollowDistance(nearEntityScav))) {
                        ScavCombatPhaseManager.forceEntityToChaseTarget(nearEntityScav, target);
                    }
                }, random.nextInt(15) + 5);
            }

            // 똑같은 적에 대한 위치 업데이트
            else if (nearEntityScav.scavCombatPhase == ScavCombatPhase.CHASE_TARGET_LASTSEEN && Objects.requireNonNull(nearEntityScav.getGoalTarget()).equals(target)) {
                Location currentDestinationLoc = PathfinderUtil.getNavigationDestinationLoc(target, nearEntityScav.getNavigation());
                Location targetLoc = target.getBukkitEntity().getLocation();
                if (currentDestinationLoc.distance(targetLoc) >= 12) {  // 현재 몹이 가고 있는 목적지와 거리가 12칸 이상 차이날 경우에만 실행
                    PathfinderUtil.moveToLoc(nearEntityScav, targetLoc, PathfinderGoalScavChaseTargetLastSeen.CHASE_SPEED, true);
                }
            }

            // 다른 적을 상대하고 있었다면 그냥 빠져나감
        }
    }
}
