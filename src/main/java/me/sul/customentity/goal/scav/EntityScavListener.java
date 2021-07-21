package me.sul.customentity.goal.scav;

import com.shampaggon.crackshot2.events.WeaponShootEvent;
import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.util.ScavUtil;
import net.minecraft.server.v1_12_R1.EntityLiving;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityScavListener implements Listener {
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && ((CraftEntity)e.getEntity()).getHandle() instanceof EntityScav) {
            e.setCancelled(true);
        }
    }

    // NOTE: 총 소리 듣고 AI들이 달려오게되면, 일부러 유인해서 죽이는게 너무 쉬워지는게 아닐까?
    @EventHandler
    public void onShoot(WeaponShootEvent e) {
        Player p = e.getPlayer();
        ScavUtil.alertOthers((EntityLiving) ((CraftEntity)p).getHandle(), 25);
    }
}
