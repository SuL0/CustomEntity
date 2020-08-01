package me.sul.customentities;

import me.sul.customentities.entities.EntityManager;
import me.sul.customentities.spawnarea.AreaMap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomEntities extends JavaPlugin {
    private static CustomEntities instance;

    @Override
    public void onEnable() {
        instance = this;
        registerClasses();
    }

    private void registerClasses() {
        Bukkit.getPluginManager().registerEvents(new EntityManager(), this);
        getCommand("scav").setExecutor(new CustomEntitiesCommandExecutor());
        new AreaMap();
    }

    public static CustomEntities getInstance() {
        return instance;
    }
}
