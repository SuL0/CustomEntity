package me.sul.customentity.entity;

import me.sul.customentity.util.CustomEntityRegistry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;

// 몹 번호: https://pastebin.com/gPci2Kt0
public class EntityManager implements Listener {
    public EntityManager() {
        CustomEntityRegistry.registerCustomEntity(51, "Skeleton", EntityScav.class);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        // TODO: 몹이 있다면 제거 (disguise가 풀려있을 확률도 있고 해서)
    }

    
    // TODO: 아래 두개 주석 제거 (테스트용으로 모두 불에 타지 않게 해놓았음)
    @EventHandler
    public void onCombust(EntityCombustEvent e) {
//        if (e.getEntityType() == EntityType.SKELETON) { // 왜 getEntity instanceof EntityScav하면 안되지? -> BukkitEntity로 바뀌었기 때문임.
            e.setCancelled(true);
//        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
//        if (e.getEntityType() == EntityType.SKELETON) { // 죽은 몹은 Entity를 가져올 수가 없음
            e.getDrops().clear();
//        }
    }

}
