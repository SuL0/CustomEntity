package me.sul.customentity.entity;

import me.sul.customentity.goal.scav.EntityScavListener;
import me.sul.customentity.util.CustomEntityRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;

// NOTE: 몹 번호: https://pastebin.com/gPci2Kt0
public class EntityManager implements Listener {
    private final Plugin plugin;
    public EntityManager(Plugin plugin) {
        this.plugin = plugin;
        registerEntityScav();
        registerEntityZombie();
    }

    private void registerEntityScav() {
        CustomEntityRegistry.registerCustomEntity(51, "Skeleton", EntityScav.class);
        Bukkit.getPluginManager().registerEvents(new EntityScavListener(), plugin);
    }
    private void registerEntityZombie() {
        CustomEntityRegistry.registerCustomEntity(54, "Zombie", EntityZombie.class);
    }




    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        // TODO: 청크 로딩 시 몹이 있다면 제거 (disguise가 풀려있을 확률도 있고 해서)
//        for (Entity entity : e.getChunk().getEntities()) {
//           if (entity instanceof Monster) {
//               Bukkit.getServer().broadcastMessage("로딩된 청크에 있던 몹 제거");
//               entity.remove();
//           }
//        }
    }


    // TODO: 추후에 제거. 테스트용으로 모두 불에 타지 않게 해놓았음
    @EventHandler
    public void onCombust(EntityCombustEvent e) {
//        if (((CraftEntity)e.getEntity()).getHandle() instanceof EntityScav) {
            e.setCancelled(true);
//        }
    }

}
