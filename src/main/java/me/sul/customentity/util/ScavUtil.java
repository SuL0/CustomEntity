package me.sul.customentity.util;

import me.sul.customentity.Main;
import me.sul.customentity.entity.EntityScav;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class ScavUtil {
    public static Random random = new Random();

    public static void alertOthers(EntityCreature me, int radiusAroundMe, int radiusAroundTarget) {
        if (me.getGoalTarget() == null) return;
        EntityLiving targetToAlert = me.getGoalTarget();
        Set<Entity> nearBukkitEntityList = new HashSet<>();

        nearBukkitEntityList.addAll(me.getBukkitEntity().getNearbyEntities(radiusAroundMe, radiusAroundMe, radiusAroundMe));  // nmsEntity 주변
        nearBukkitEntityList.addAll(me.getGoalTarget().getBukkitEntity().getNearbyEntities(radiusAroundTarget, radiusAroundTarget, radiusAroundTarget));  // 플레이어 주변
        nearBukkitEntityList.removeIf(e -> !(((CraftEntity)e).getHandle() instanceof EntityScav) || e.equals(me.getBukkitEntity()));

        for (EntityScav nearEntityScav : nearBukkitEntityList.stream()
                .filter(e -> ((CraftEntity)e).getHandle() instanceof EntityScav)
                .map(e -> (EntityScav) ((CraftEntity)e).getHandle())
                .collect(Collectors.toList())) {

            // TODO: unseenTicks를 어떻게 0으로 만들지?

            // 처음 보는 적
            if (nearEntityScav.getGoalTarget() == null) {
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {  // 아래의 코드를 실행할 때는 일정 시간이 흘렀음을 주의해야 함 !
                    if (nearEntityScav.getGoalTarget() == null && PathfinderUtil.isInTargetableState(me.getBukkitEntity(), targetToAlert, PathfinderUtil.getFollowDistance(me))) {
                        Bukkit.getServer().broadcastMessage("처음 보는 적을 alertOther에 의해 타겟으로 설정");
                        nearEntityScav.unseenTick = 20;  // 이거 코드 안좋은데 ㅋㅋ;
                        nearEntityScav.setGoalTarget(targetToAlert, EntityTargetEvent.TargetReason.CUSTOM, true);
                    }
                }, random.nextInt(15) + 5);
            }
            // 똑같은 적에 대한 정보 업데이트
            else if (nearEntityScav.getGoalTarget() != null && nearEntityScav.getGoalTarget().equals(targetToAlert)) {

                nearEntityScav.setGoalTarget(null, EntityTargetEvent.TargetReason.CUSTOM, true); // 타겟 제거 (goal들 모두 작동 정지시키기)

                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    if (nearEntityScav.getGoalTarget() == null && PathfinderUtil.isInTargetableState(me.getBukkitEntity(), targetToAlert, PathfinderUtil.getFollowDistance(me)))
                        Bukkit.getServer().broadcastMessage("alertOther에 의해 타겟 위치 업데이트");
                        nearEntityScav.unseenTick = 20;
                        nearEntityScav.setGoalTarget(targetToAlert, EntityTargetEvent.TargetReason.CUSTOM, true);
                }, 1);
            }
            // 다른 적을 상대하고 있었다면 그냥 빠져나감
        }
    }
}
