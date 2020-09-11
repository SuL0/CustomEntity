package me.sul.customentity.goal;

import me.sul.customentity.spawnarea.Area;
import me.sul.customentity.spawnarea.AreaUtil;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.Navigation;
import org.bukkit.entity.Entity;

// PathfinderGoalMoveThroughVillage 기반
public class PathfinderGoalStrollInSpecificArea<T extends EntityCreature> extends EasilyModifiedPathfinderGoal {
    private final T nmsEntity;
    private final Entity bukkitEntity;
    private final Area area;
    private final double speed;
    private final int randomInterval;

    public PathfinderGoalStrollInSpecificArea(T nmsEntity, Area area, double speed, int randomInterval) {
        super(nmsEntity);
        this.nmsEntity = nmsEntity;
        this.bukkitEntity = nmsEntity.getBukkitEntity();
        this.area = area;
        this.speed = speed;
        this.randomInterval = randomInterval;
        setFlag(Flag.LOOK);
        if (!(getNavigation() instanceof Navigation)) {
            throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
        }
    }

    @Override
    public boolean canUse() {
        if (!isNavigationDone() || getGoalTarget() != null) return false;
        return getRandom().nextInt(randomInterval) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return !isNavigationDone() && getGoalTarget() == null;
    }

    @Override
    public void start() {
        org.bukkit.Location destinationLoc;
        // 지정 구역에서 벗어났을 때
        if (AreaUtil.isAwayFromArea(area, bukkitEntity.getLocation())) {
            destinationLoc = AreaUtil.getClosestLocation(area, bukkitEntity.getLocation());
            if (destinationLoc.distance(bukkitEntity.getLocation()) >= nmsEntity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).getValue() * 0.9) {
                nmsEntity.killEntity();
                return;
            }
            moveToLoc(destinationLoc, speed, true);
        } else {
            destinationLoc = AreaUtil.getLocationForStroll(area, bukkitEntity.getLocation(), 5,10);
            moveToLoc(destinationLoc, speed*1.2, true);
        }
    }

    @Override
    public void stop() {
        stopNavigation();
    }
}
