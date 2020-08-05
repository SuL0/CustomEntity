package me.sul.customentity.spawnarea;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class AreaMap {
    private static Map<String, Area> spawnAreaMap = new HashMap();
    public AreaMap() {
        spawnAreaMap.put("테스트장", new Area(new Location(Bukkit.getWorld("world"), 16, 64, -41), new Location(Bukkit.getWorld("world"), 37, 64, -20)));
        spawnAreaMap.put("테스트장2", new Area(new Location(Bukkit.getWorld("world"), -78, 64, 30), new Location(Bukkit.getWorld("world"), -59, 64, 49)));
        spawnAreaMap.put("스폰", new Area(new Location(Bukkit.getWorld("world"), 95, 64, 38), new Location(Bukkit.getWorld("world"), 141, 64, -7)));
    }
    public static Area getSpawnArea(String spawnAreaName) {
        if (!spawnAreaMap.containsKey(spawnAreaName)) return null;
        return spawnAreaMap.get(spawnAreaName);
    }

//    public static SpawnArea getNearSpawnArea(Location loc) {
//
//    }
}
