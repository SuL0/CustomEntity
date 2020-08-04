package me.sul.customentity.entityweapon;

import com.shampaggon.crackshot.CSDirector;
import me.sul.customentity.entity.EntityScav;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class ProjectileListener implements Listener {
    // 엔티티가 쏜 총알에 맞았을 때
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity victim = e.getEntity();
        if (e.getDamager() instanceof Projectile && victim instanceof Damageable && !(victim instanceof EntityScav) &&
                e.getDamager().hasMetadata(EntityCrackShotWeapon.PROJ_DAMAGE_META)) {
            Projectile projectile = (Projectile) e.getDamager();
            double damage = projectile.getMetadata(EntityCrackShotWeapon.PROJ_DAMAGE_META).get(0).asDouble();
            Vector knockbackVector = victim.getLocation().toVector().subtract(((Entity)projectile.getShooter()).getLocation().toVector()).normalize().multiply(0.5);
            CSDirector.getInstance().setTempVulnerability((LivingEntity)victim);
            ((Damageable)victim).damage(damage);
            victim.setVelocity(knockbackVector);
        }
    }
//    @EventHandler
//    public void onProjectileHit(ProjectileHitEvent e) {
//        if (e.getHitEntity() != null && e.getHitEntity() instanceof Damageable &&
//                e.getEntity().hasMetadata(EntityCrackShotWeapon.PROJ_DAMAGE_META)) {
//            Entity shooter = (Entity)e.getEntity().getShooter();
//            Entity victim = e.getHitEntity();
//            double damage = e.getEntity().getMetadata(EntityCrackShotWeapon.PROJ_DAMAGE_META).get(0).asDouble();
//            Vector knockbackVector = e.getEntity().getVelocity().normalize().multiply(new Vector(1, 0, 1).multiply(0.1));
//            ((Damageable)victim).damage(damage);
//            victim.setVelocity(knockbackVector);
//        }
//    }
}
