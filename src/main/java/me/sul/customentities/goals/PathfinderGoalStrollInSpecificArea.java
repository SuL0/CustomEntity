package me.sul.customentities.goals;

import me.sul.customentities.spawnarea.Area;
import net.minecraft.server.v1_12_R1.*;

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

    @Override
    public boolean a() {  // canUse()
        if (nmsCreature.getRandom().nextInt(randomInterval) != 0) return false;

        org.bukkit.Location entityLoc = nmsCreature.getBukkitEntity().getLocation();
        org.bukkit.Location randLoc = entityLoc;
        for (int i=0; i<=100; i++) { // 거의 무한
            randLoc = area.getRandomLocation();
            if (randLoc.distance(entityLoc) <= 7) {
                break;
            }
        }
        goalPath = nmsCreature.getNavigation().a(randLoc.getX(), randLoc.getY(), randLoc.getZ());
        return true;
    }

    @Override
    public void c() {  // start()
        this.nmsCreature.getNavigation().a(this.goalPath, this.speed);
    }

    @Override
    public boolean b() {  // canContinueToUse()
        return !this.nmsCreature.getNavigation().o();
    }
}
