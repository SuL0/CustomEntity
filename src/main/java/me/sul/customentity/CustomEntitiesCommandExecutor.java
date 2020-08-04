package me.sul.customentity;

import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.spawnarea.AreaMap;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.Collections;

public class CustomEntitiesCommandExecutor implements Listener, CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        p.sendMessage("몹이 소환되었습니다.");

        Location playerLoc = p.getLocation();
        Location targetedLoc  = p.getTargetBlock(Collections.singleton(Material.AIR), 40).getLocation().add(0, 1, 0);
        new EntityScav(AreaMap.getSpawnArea("테스트장"));

        return true;
    }

    public void setTarget(LivingEntity entity, LivingEntity target) {
        if (entity instanceof Creature) {
            ((Creature)entity).setTarget(target);
        } else {
            ((EntityInsentient)((CraftLivingEntity)entity).getHandle()).setGoalTarget(((CraftLivingEntity)target).getHandle(), EntityTargetEvent.TargetReason.CUSTOM, true);
        }
    }


//    public boolean a() {
//        try {
//            AbstractLocation eLocation = new AbstractLocation(this.destination.getWorld(), this.entity.locX, this.entity.locY, this.entity.locZ);
//            if (eLocation.distanceSquared(this.destination) > 1.0D) {
//                this.entity.getNavigation().a(this.destination.getX(), this.destination.getY(), this.destination.getZ(), this.speed);
//                return true;
//            } else {
//                return false;
//            }
//        } catch (Exception var2) {
//            var2.printStackTrace();
//            return false;
//        }
//    }
}
