package me.sul.customentity.goal;

import me.sul.customentity.spawnarea.Area;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;

// PathfinderGoalMoveThroughVillage 기반. private 메소드 오버라이딩 못하는 문제로 상속하지 않았음.
// TODO: 길이 막히는 등으로 갈 수 없는 경우면 다른 곳으로 위치 재설정
public class PathfinderGoalStrollInSpecificArea extends PathfinderGoal {
    private final EntityCreature nmsCreature;
    private final Area area;
    private final double speed;
    private PathEntity goalPath;
    private final int randomInterval;

    public PathfinderGoalStrollInSpecificArea(EntityCreature nmsCreature, Area area, double speed, int randomInterval) {
        this.nmsCreature = nmsCreature;
        this.area = area;
        this.speed = speed;
        this.randomInterval = randomInterval;
        this.a(1); // setFlag() ? 모든게 다 a(1)임
        if (!(nmsCreature.getNavigation() instanceof Navigation)) {
            throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
        }
    }


    // TODO: 전투상황에서는 true를 반환하지 못하게 해야 함
    @Override
    public boolean a() {  // canUse()
        Bukkit.getServer().broadcastMessage("§a[GOAL] StrollInSpecificArea - a()");
        if (nmsCreature.getRandom().nextInt(randomInterval) != 0) {
            Bukkit.getServer().broadcastMessage(" §a -> return false");
            return false;
        }

        org.bukkit.Location entityLoc = nmsCreature.getBukkitEntity().getLocation();
        org.bukkit.Location randLoc = entityLoc;
        for (int i=0; i<=100; i++) { // 거의 무한
            randLoc = area.getRandomLocation();
            if (randLoc.distance(entityLoc) <= 7) {
                break;
            }
        }
        Navigation navigation = (Navigation) nmsCreature.getNavigation();
        navigation.a(true); // canOpenDoors. b(): canPassDoors, c(): canFloat   .  PathfinderGoalOpenDoor과 함께 있어야 작동하는 듯?
        goalPath = navigation.a(randLoc.getX(), randLoc.getY(), randLoc.getZ());
        return true;
    }

    @Override
    public void c() {  // start()
        Bukkit.getServer().broadcastMessage("");
        Bukkit.getServer().broadcastMessage("§a§l[GOAL] StrollInSpecificArea - c()");
        Bukkit.getServer().broadcastMessage("");
        nmsCreature.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(60);
        this.nmsCreature.getNavigation().a(this.goalPath, this.speed);
        nmsCreature.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(20);
    }

    @Override
    public boolean b() {  // canContinueToUse()
        Bukkit.getServer().broadcastMessage("§a[GOAL] StrollInSpecificArea - b()");
        return !this.nmsCreature.getNavigation().o();
    }
}
