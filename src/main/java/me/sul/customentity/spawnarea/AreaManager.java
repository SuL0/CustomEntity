package me.sul.customentity.spawnarea;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class AreaManager {
    private static final Map<String, Area> spawnAreaMap = new HashMap<>();
    public AreaManager() {
        spawnAreaMap.put("warn1", new Area(Bukkit.getWorld("warn"), -500, 70, -500, 500, 100, 500));
    }
    public static Area getSpawnArea(String spawnAreaName) {
        if (!spawnAreaMap.containsKey(spawnAreaName)) return null;
        return spawnAreaMap.get(spawnAreaName);
    }
}
