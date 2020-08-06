package me.sul.customentity.entityweapon;

import com.rhetorical.soundscape.SoundScapeAPI;
import me.sul.customentity.Main;
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
        Location fireLoc = entity.getLocation().add(0, 1.5, 0).add(entity.getLocation().getDirection());
        Vector projVector = target.getLocation().add(0, target.getHeight()/2, 0).toVector().subtract(fireLoc.toVector()).normalize().multiply(projSpeed);
        double[] spread = new double[] {1.0D, 1.0D, 1.0D};
        for(int i = 0; i < 3; ++i) {
            spread[i] = (r.nextDouble() - r.nextDouble()) * projSpread * 0.1D;
        }
        projVector.add(new Vector(spread[0], spread[1], spread[2]));


        Projectile projBullet = (Projectile) entity.getWorld().spawnEntity(fireLoc, EntityType.SNOWBALL);
        projBullet.setShooter((ProjectileSource) entity);
        projBullet.setVelocity(projVector);
        projBullet.setMetadata(PROJ_DAMAGE_META, new FixedMetadataValue(Main.getInstance(), damage));
        SoundScapeAPI.playSound(fireLoc, "akshoot", 1, 1, 50);
        Bukkit.getPluginManager().callEvent(new CustomEntityShootEvent(entity, projBullet));
    }

}
