package me.sul.customentity.entityweapon;

import com.shampaggon.crackshot2.CSDirector;
import me.sul.customentity.Main;
import me.sul.customentity.entityweapon.event.CEWeaponHitBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class ProjectileListener implements Listener {
    private static final String PROJ_COLLIDED = "CE.Collided";

    // 엔티티가 쏜 총알에 맞았을 때
    // damager과 attacker 헷갈리지 않게 주의
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity damager = e.getDamager();
        Entity victim = e.getEntity();
        if (damager instanceof Projectile && damager.hasMetadata(GunUtil.PROJ_DAMAGE_META) && victim instanceof Damageable) {
            Entity attacker = (((Projectile) damager).getShooter() instanceof Entity) ? ((Entity)((Projectile) damager).getShooter()) : null;
            if (attacker != null && ((CraftEntity)attacker).getHandle().getClass().isInstance(((CraftEntity)victim).getHandle())) {
                e.setCancelled(true);
                return;
            }
            // deal damage
            Projectile projectile = (Projectile) damager;
            double damage = projectile.getMetadata(GunUtil.PROJ_DAMAGE_META).get(0).asDouble();
            Vector knockbackVector = victim.getLocation().toVector().subtract(((Entity) projectile.getShooter()).getLocation().toVector()).normalize().multiply(0.3);
            CSDirector.getInstance().setTempVulnerability((LivingEntity) victim);
            e.setDamage(damage);
            victim.setVelocity(knockbackVector);
            damager.setMetadata(PROJ_COLLIDED, new FixedMetadataValue(Main.getInstance(), true));
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();
        if (projectile.hasMetadata(GunUtil.PROJ_DAMAGE_META) && projectile.getShooter() instanceof Entity && !projectile.hasMetadata(PROJ_COLLIDED)) {
            Entity shooter = (Entity) e.getEntity().getShooter();

            CEWeaponHitBlockEvent blockEvent = new CEWeaponHitBlockEvent(shooter, projectile);
            Bukkit.getServer().getPluginManager().callEvent(blockEvent);
        }
    }
}
