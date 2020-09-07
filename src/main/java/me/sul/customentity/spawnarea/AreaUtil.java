package me.sul.customentity.spawnarea;

import org.bukkit.Location;

import java.util.Random;

public class AreaUtil {
    private static Random random = new Random();

    public static Location getRandomLocation(Area area) {
        double randX = area.getMinX() + (area.getMaxX()-area.getMinX())*random.nextFloat();
        double randY = area.getMinY() + (area.getMaxY()-area.getMinY())*random.nextFloat();
        double randZ = area.getMinZ() + (area.getMaxZ()-area.getMinZ())*random.nextFloat();
        return new Location(area.getWorld(), randX, randY, randZ);
    }

    
    // TODO: y축 지원 추가해야 함. e.g. 2층 이상의 건물 및 지하
    public static Location getLocationForStroll(Area area, Location currentLoc, int minLength, int maxLength) {
        for (int i=0; i<10; i++) {
            double randX = ((random.nextBoolean()) ? 1:-1) * (random.nextFloat() * maxLength + minLength);
            double randZ = ((random.nextBoolean()) ? 1:-1) * (random.nextFloat() * maxLength + minLength);
            Location randLoc = currentLoc.clone().add(randX, 0, randZ);
            if (!isAwayFromArea(area, randLoc)) {
                return randLoc;
            }
        }
        return currentLoc;
    }


    public static boolean isAwayFromArea(Area area, Location givenLoc) {
        return (!(area.getMinX() <= givenLoc.getX() && givenLoc.getX() <= area.getMaxX() &&
                area.getMinY() <= givenLoc.getY() && givenLoc.getY() <= area.getMaxY()) &&
                area.getMinZ() <= givenLoc.getZ() && givenLoc.getZ() <= area.getMaxZ());
    }


    public static Location getClosestLocation(Area area, Location givenLoc) {
        double[] location = new double[3];
        double[] min = { area.getMinX(), area.getMinY(), area.getMinZ() }; // x, y, z
        double[] max = { area.getMaxX(), area.getMaxY(), area.getMaxZ() }; // x, y, z
        for (int i=0; i<3; i++) {
            if (min[i] <= givenLoc.getX() && givenLoc.getX() <= max[i]) {
                location[i] = givenLoc.getX();
            } else {
                if (min[i] > givenLoc.getX()) {
                    location[i] = min[i];
                } else {
                    location[i] = max[i];
                }
            }
        }
        return new Location(givenLoc.getWorld(), location[0], location[1], location[2]);
    }
}
