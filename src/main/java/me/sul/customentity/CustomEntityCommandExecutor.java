package me.sul.customentity;

import me.sul.customentity.entity.EntityScav;
import me.sul.customentity.spawnarea.AreaMap;
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

        p.sendMessage("§c§l엔티티: §f몹이 소환 되었습니다.");

        EntityScav entityScav = new EntityScav(AreaMap.getSpawnArea("스폰"), p.getTargetBlock(Collections.singleton(Material.AIR), 40).getLocation().add(0,1,0));

//        entityScav.getBukkitEntity().setGlowing(true);

        return true;
    }

}
