package me.sul.customentity.goal;

import me.sul.customentity.spawnarea.Area;
import me.sul.customentity.spawnarea.AreaUtil;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

// PathfinderGoalMoveThroughVillage 기반
// TODO: EasilyModifiedPathfinderGoal를 상속하도록 코드 변경
public class PathfinderGoalStrollInSpecificArea<T extends EntityCreature> extends PathfinderGoal {
    private final T nmsEntity;
    private final Entity bukkitEntity;
    private final Area area;
    private final double speed;
    private final int randomInterval;

    public PathfinderGoalStrollInSpecificArea(T nmsEntity, Area area, double speed, int randomInterval) {
        this.nmsEntity = nmsEntity;
        this.bukkitEntity = nmsEntity.getBukkitEntity();
        this.area = area;
        this.speed = speed;
        this.randomInterval = randomInterval;
        this.a(1); // setFlag() ? 모든게 다 a(1)임
        if (!(nmsEntity.getNavigation() instanceof Navigation)) {
            throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
        }
    }


    @Override
    public boolean a() {  // canUse()
        if (!nmsEntity.getNavigation().o() || nmsEntity.getGoalTarget() != null) return false;
        return nmsEntity.getRandom().nextInt(randomInterval) == 0;
    }

    @Override
    public boolean b() {  // canContinueToUse()
        return !nmsEntity.getNavigation().o() && nmsEntity.getGoalTarget() == null;
    }

    @Override
    public void c() {  // start()
        org.bukkit.Location destinationLoc;
        // 지정 구역에서 벗어났을 때
        if (AreaUtil.isAwayFromArea(area, bukkitEntity.getLocation())) {
            destinationLoc = AreaUtil.getClosestLocation(area, bukkitEntity.getLocation());
            if (destinationLoc.distance(bukkitEntity.getLocation()) >= nmsEntity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).getValue() * 0.9) {
                nmsEntity.killEntity();
                return;
            }
            moveToLocConsideringDoor(destinationLoc, speed);
        } else {
            destinationLoc = AreaUtil.getLocationForStroll(area, bukkitEntity.getLocation(), 5,10);
            moveToLocConsideringDoor(destinationLoc, speed*1.2);
        }
    }

    private void moveToLocConsideringDoor(Location destinationLoc, double speed) {
        Navigation navigation = (Navigation) nmsEntity.getNavigation();
        navigation.a(true);
        PathEntity goalPath = navigation.a(destinationLoc.getX(), destinationLoc.getY(), destinationLoc.getZ());
        nmsEntity.getNavigation().a(goalPath, speed);
    }

    @Override
    public void d() {  // stop()
        nmsEntity.getNavigation().p(); // stop
    }
}
