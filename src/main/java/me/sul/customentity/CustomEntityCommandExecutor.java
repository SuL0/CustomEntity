package me.sul.customentity;

import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.spawnarea.AreaManager;
import me.sul.customentity.util.PathfinderUtil;
import net.minecraft.server.v1_12_R1.EntityCreature;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.stream.Collectors;

public class CustomEntityCommandExecutor implements Listener, CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (!sender.isOp()) return true;
        Player p = (Player) sender;
        if (args != null && args.length != 0 && args[0].equalsIgnoreCase("getDestination")) {
            for (EntityCreature entity : p.getNearbyEntities(10, 10, 10).stream()
                    .filter(e -> e instanceof Monster).map(e -> (EntityCreature) ((CraftEntity)e).getHandle()).collect(Collectors.toList())) {
                Bukkit.getServer().broadcastMessage("");
                Bukkit.getServer().broadcastMessage("ENTITY : " + entity.getClass().getSimpleName());
                Bukkit.getServer().broadcastMessage("get Destination Loc : " + PathfinderUtil.getNavigationDestinationLoc(entity, entity.getNavigation()));
            }
            return true;
        }

        p.sendMessage("§c§l엔티티: §f몹이 소환 되었습니다.");

        EntityScav entityScav = new EntityScav(AreaManager.getSpawnArea("warn1"), p.getTargetBlock(Collections.singleton(Material.AIR), 40).getLocation().add(0,1,0));
        entityScav.getBukkitEntity().setGlowing(true);
//        for (int i=0; i<10; i++)
//            new EntityZombie(p.getTargetBlock(Collections.singleton(Material.AIR), 40).getLocation().add(0,1,0));
        return true;
    }

}
