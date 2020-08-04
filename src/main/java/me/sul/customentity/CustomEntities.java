package me.sul.customentity;

import me.sul.customentity.entity.EntityManager;
import me.sul.customentity.entityweapon.ProjectileListener;
import me.sul.customentity.spawnarea.AreaMap;
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
        Bukkit.getPluginManager().registerEvents(new ProjectileListener(), this);
    }

    public static CustomEntities getInstance() {
        return instance;
    }
}
