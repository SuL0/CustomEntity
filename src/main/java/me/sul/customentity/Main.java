package me.sul.customentity;

import me.sul.customentity.entity.EntityManager;
import me.sul.customentity.entityweapon.ProjectileListener;
import me.sul.customentity.spawnarea.AreaManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        registerClasses();
    }

    private void registerClasses() {
        Bukkit.getPluginManager().registerEvents(new EntityManager(), this);
        getCommand("scav").setExecutor(new CustomEntityCommandExecutor());
        new AreaManager();
        Bukkit.getPluginManager().registerEvents(new ProjectileListener(), this);
    }

    public static Main getInstance() {
        return instance;
    }
}
