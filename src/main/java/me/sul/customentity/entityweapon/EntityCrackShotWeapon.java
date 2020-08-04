package me.sul.customentity.entityweapon;

import com.rhetorical.soundscape.SoundScapeAPI;
import me.sul.customentity.CustomEntities;
import me.sul.customentity.event.CustomEntityShootEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Random;

public class EntityCrackShotWeapon {
    public final static String PROJ_DAMAGE_META = "EntityCrackShotWeapon.Damage";

    // TODO: CrackShotProjectileBreakEvent 호출하게끔 CSA 변경
    public static void fireProjectile(CraftEntity entity, CraftEntity target, float projSpread, int projSpeed, double damage) {
        Random r = new Random();
        Vector projVector = target.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(projSpeed);
        double[] spread = new double[] {1.0D, 1.0D, 1.0D};
        for(int i = 0; i < 3; ++i) {
            spread[i] = (r.nextDouble() - r.nextDouble()) * projSpread * 0.1D;
        }
        projVector.add(new Vector(spread[0], spread[1], spread[2]));


        Location projLoc = entity.getLocation().add(0, 1.5, 0).add(entity.getLocation().getDirection());
        Projectile proj = (Projectile) entity.getWorld().spawnEntity(projLoc, EntityType.SNOWBALL);
        proj.setShooter((ProjectileSource) entity);
        proj.setVelocity(projVector);
        proj.setMetadata(PROJ_DAMAGE_META, new FixedMetadataValue(CustomEntities.getInstance(), damage));
        SoundScapeAPI.playSound(projLoc, "akshoot", 1, 1, 50);
        Bukkit.getPluginManager().callEvent(new CustomEntityShootEvent(entity, proj));
    }

}
