package me.sul.customentity.entity;

import me.sul.customentity.util.CustomEntityRegistry;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.world.ChunkLoadEvent;

// NOTE: 몹 번호: https://pastebin.com/gPci2Kt0
public class EntityManager implements Listener {
    public EntityManager() {
        CustomEntityRegistry.registerCustomEntity(51, "Skeleton", EntityScav.class);
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

    // NOTE: 이런거 EntityScavListener로 분리해야 하나?
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && ((CraftEntity)e.getEntity()).getHandle() instanceof EntityScav) {
            e.setCancelled(true);
        }
    }
}
