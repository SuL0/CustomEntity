package me.sul.customentity.util;

import me.sul.customentity.Main;
import me.sul.customentity.entity.EntityScav;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class PathfinderUtil {
    public static Random random = new Random();

    // Navigation은 목적지를 찍으면 엔티티가 갈 수 있는 가장 근접한 위치로 목적지를 바꿔줌. -> y축 신경쓸거 없이 그냥 대충 목적지 찍으면 알아서 계산해줌
    public static void moveToLoc(EntityCreature me, Location destinationLoc, double speed, boolean canOpenDoors) {
        Navigation navigation = (Navigation) me.getNavigation();
        navigation.a(canOpenDoors); // canOpenDoors. b(): canPassDoors, c(): canFloat.  위치선정을 할 때 문을 열고 가는 것을 고려해서 찍어줌
        PathEntity goalPath = navigation.a(destinationLoc.getX(), destinationLoc.getY(), destinationLoc.getZ());
        me.getNavigation().a(goalPath, speed);
    }
    public static void stopNavigation(EntityCreature me) {
        me.getNavigation().p();
    }
    public static boolean isNavigationDone(EntityCreature me) {
        return me.getNavigation().o();
    }

    public static boolean isInTargetableState(Entity me, EntityLiving nmsOpponent, double maxDistance) {
        if (nmsOpponent == null || !nmsOpponent.isAlive()) return false;
        if (nmsOpponent instanceof EntityPlayer && (((EntityHuman)nmsOpponent).abilities.isInvulnerable || ((Player)nmsOpponent.getBukkitEntity()).getGameMode() != GameMode.SURVIVAL)) return false;
        if (nmsOpponent.getBukkitEntity().getLocation().distance(me.getLocation()) > maxDistance) return false;
        return true;
    }

    // 벡터 내적. p의 벡터에 대해서 파라미터의 벡터가 얼마만큼의 도움을 줄 수 있는가
    // aVec와 bVec 내적 = aVec크기 * bVec크기 * cos(세타)
    // -> 세타 = acos(aVec와 bVec내적 / aVec크기 * bVec크기)
    public static boolean isInSight(Entity me, Entity bukkitOpponent, double viewingAngle) {
        if (!(bukkitOpponent instanceof LivingEntity)) return false;
        Vector sightVector = me.getLocation().getDirection();
        Vector toOpponentVector = bukkitOpponent.getLocation().toVector().subtract(me.getLocation().toVector());
        double angle = getAngleBetweenTwoVectors(sightVector, toOpponentVector);

        return angle <= (viewingAngle/2) && ((LivingEntity) me).hasLineOfSight(bukkitOpponent);
    }
    private static double getAngleBetweenTwoVectors(Vector aVec, Vector bVec) {
        double cosAngle = (aVec.clone().dot(bVec)) / (aVec.length() * bVec.length());
        return Math.toDegrees(Math.acos(cosAngle));
    }

    public static double getFollowDistance(EntityCreature me) {
        AttributeInstance attribute = me.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        return attribute == null ? 16.0D : attribute.getValue();
    }

    public static boolean isHoldingBow(EntityCreature me) { return !me.getItemInMainHand().isEmpty() && me.getItemInMainHand().getItem() == Items.BOW; }

    public static void alertOthers(EntityCreature me) { alertOthers(me, 10, 20); }
    public static void alertOthers(EntityCreature me, int radiusAroundMe, int radiusAroundTarget) {
        if (me.getGoalTarget() == null) return;
        EntityLiving targetToAlert = me.getGoalTarget();
        Set<Entity> nearBukkitEntityList = new HashSet<>();

        nearBukkitEntityList.addAll(me.getBukkitEntity().getNearbyEntities(radiusAroundMe, radiusAroundMe, radiusAroundMe));  // nmsEntity 주변
        nearBukkitEntityList.addAll(me.getGoalTarget().getBukkitEntity().getNearbyEntities(radiusAroundTarget, radiusAroundTarget, radiusAroundTarget));  // 플레이어 주변
        nearBukkitEntityList.removeIf(e -> !(e instanceof Monster));

        for (EntityCreature nearNmsEntity : nearBukkitEntityList.stream().map(e -> (EntityCreature) ((CraftEntity)e).getHandle()).collect(Collectors.toList())) {
            if (nearNmsEntity.getClass().isInstance(me)) {
                if (nearNmsEntity.getGoalTarget() != null) return;
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    if (nearNmsEntity.getGoalTarget() == null && PathfinderUtil.isInTargetableState(me.getBukkitEntity(), targetToAlert, getFollowDistance(me))) {
                        nearNmsEntity.setGoalTarget(targetToAlert, EntityTargetEvent.TargetReason.CUSTOM, true);
                    }
                }, random.nextInt(15) + 5);
            }
        }
    }
}
