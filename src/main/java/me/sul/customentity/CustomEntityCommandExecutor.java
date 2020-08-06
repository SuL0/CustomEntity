package me.sul.customentity;

import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.spawnarea.AreaMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collections;

public class CustomEntityCommandExecutor implements Listener, CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

//        if (args != null && args.length > 0 && args[0].equalsIgnoreCase("hand")) {
//            Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
//                EntityPlayer nmsPlayer = ((CraftPlayer)p).getHandle();
//                nmsPlayer.c(EnumHand.MAIN_HAND); // startUsingItem()
//            },0L, 1L);
//            return true;
//        }

        p.sendMessage("몹이 소환되었습니다.");

        Location playerLoc = p.getLocation();
        Location targetedLoc  = p.getTargetBlock(Collections.singleton(Material.AIR), 40).getLocation().add(0, 1, 0);
        EntityScav entityScav = new EntityScav(AreaMap.getSpawnArea("스폰"));
//        entityScav.getBukkitEntity().setGlowing(true);

        return true;
    }
}
