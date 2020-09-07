package me.sul.customentity.spawnarea;

import org.bukkit.Location;
import org.bukkit.World;


// TODO: y좌표는 없앨까?
public class Area {
    private final Location minLocation;
    private final Location maxLocation;

    public Area(Location loc1, Location loc2) {
        this(loc1.getWorld(), loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());
    }
    public Area(World world, double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minLocation = new Location(world, Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
        this.maxLocation = new Location(world, Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    public World getWorld() {
        return minLocation.getWorld();
    }
    public double getMinX() {
        return minLocation.getX();
    }
    public double getMinY() {
        return minLocation.getY();
    }
    public double getMinZ() {
        return minLocation.getZ();
    }
    public double getMaxX() {
        return maxLocation.getX();
    }
    public double getMaxY() {
        return maxLocation.getY();
    }
    public double getMaxZ() {
        return maxLocation.getZ();
    }
    public Location getCenterLocation() {
        return (minLocation.clone().add(maxLocation)).multiply(0.5);
    }
}
