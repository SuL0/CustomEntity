package me.sul.customentity.entityweapon;

import com.shampaggon.crackshot.CSDirector;
import me.sul.customentity.entity.EntityScav;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class ProjectileListener implements Listener {
    // 엔티티가 쏜 총알에 맞았을 때
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity damager = e.getDamager();
        Entity victim = e.getEntity();
        if (damager instanceof Projectile && damager.hasMetadata(EntityCrackShotWeapon.PROJ_DAMAGE_META) && victim instanceof Damageable) {
            Entity attacker = (((Projectile) damager).getShooter() instanceof Entity) ? ((Entity)((Projectile) damager).getShooter()) : null;
            if (attacker != null && attacker.getType() == EntityType.SKELETON && victim.getType() == EntityType.SKELETON) {
                e.setCancelled(true);
                return;
            }

            Projectile projectile = (Projectile) damager;
            double damage = projectile.getMetadata(EntityCrackShotWeapon.PROJ_DAMAGE_META).get(0).asDouble();
            Vector knockbackVector = victim.getLocation().toVector().subtract(((Entity) projectile.getShooter()).getLocation().toVector()).normalize().multiply(0.3);
            CSDirector.getInstance().setTempVulnerability((LivingEntity) victim);
            e.setDamage(damage);
            victim.setVelocity(knockbackVector);
        }
    }
}
