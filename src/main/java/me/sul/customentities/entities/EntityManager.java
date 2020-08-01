package me.sul.customentities.entities;

import me.sul.customentities.utils.CustomEntityRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;

// 몹 번호: https://pastebin.com/gPci2Kt0
public class EntityManager implements Listener {
    public EntityManager() {
        CustomEntityRegistry.registerCustomEntity(51, "Skeleton", EntityScav.class);
    }
    
    // NOTE: FOR TEST
    @EventHandler
    public void onCombust(EntityCombustEvent e) {
        if (e.getEntityType() == EntityType.SKELETON) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        // TODO: 몹이 있다면 제거 (disguise가 풀려있을 확률도 있고 해서)
    }
    
    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        Bukkit.getServer().broadcastMessage("EntityType: " + e.getEntityType());
    }
}
