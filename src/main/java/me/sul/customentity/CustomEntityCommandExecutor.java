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
        p.sendMessage("§c§l엔티티: §f몹이 소환 되었습니다.");

        Location playerLoc = p.getLocation();
        Location targetedLoc  = p.getTargetBlock(Collections.singleton(Material.AIR), 40).getLocation().add(0, 1, 0);
        EntityScav entityScav = new EntityScav(AreaMap.getSpawnArea("스폰"), p.getTargetBlock(Collections.singleton(Material.AIR), 40).getLocation().add(0,1.5,0));

//        entityScav.getBukkitEntity().setGlowing(true);


//        Location entityLoc = new Location(p.getWorld(), 0.5, p.getLocation().getY(), 0.5);
//        Vector entitySightVector = new Location(p.getWorld(), 5, 0, 0).toVector();  // 방향이니까 y는 0
//
//        Vector entityToPlayerVector = p.getLocation().toVector().subtract(entityLoc.toVector());
//
//        double cosAngle = (entitySightVector.clone().dot(entityToPlayerVector)) / (entitySightVector.length() * entityToPlayerVector.length());
//        double angle = Math.toDegrees(Math.acos(cosAngle)); // acos만 하면 라디안이 나와서 각도로 변환해야 함
        return true;
    }

}
