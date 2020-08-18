package me.sul.customentity.goal;

import me.sul.customentity.entity.CustomEntity;
import me.sul.customentity.spawnarea.Area;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.entity.Entity;

// PathfinderGoalMoveThroughVillage 기반. private 메소드 오버라이딩 못하는 문제로 상속하지 않았음.
// TODO: 길이 막히는 등으로 갈 수 없는 경우면 다른 곳으로 위치 재설정
public class PathfinderGoalStrollInSpecificArea<T extends EntityCreature & CustomEntity> extends PathfinderGoal {
    private final T nmsEntity;
    private final Entity bukkitEntity;
    private final Area area;
    private final double speed;
    private PathEntity goalPath;
    private final int randomInterval;

    public PathfinderGoalStrollInSpecificArea(T nmsEntity, Area area, double speed, int randomInterval) {
        this.nmsEntity = nmsEntity;
        this.bukkitEntity = nmsEntity.getBukkitEntity();
        this.area = area;
        this.speed = speed;
        this.randomInterval = randomInterval;
        this.a(1); // setFlag() ? 모든게 다 a(1)임
        if (!(nmsEntity.getNavigation() instanceof Navigation)) {
            throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
        }
    }


    @Override
    public boolean a() {  // canUse()
        if (!nmsEntity.getNavigation().o() || nmsEntity.getGoalTarget() != null) return false;
        return nmsEntity.getRandom().nextInt(randomInterval) == 0;
    }

    @Override
    public boolean b() {  // canContinueToUse()
        return !nmsEntity.getNavigation().o() && nmsEntity.getGoalTarget() == null;
    }

    @Override
    public void c() {  // start()
        org.bukkit.Location destinationLoc;
        boolean isAwayFromArea;
        // 지정 구역에서 벗어났을 때
        if (area.isAwayFromArea(bukkitEntity.getLocation())) {
            destinationLoc = area.getClosestLocation(bukkitEntity.getLocation());
            if (destinationLoc.distance(bukkitEntity.getLocation()) >= nmsEntity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).getValue() * 0.9) {
                nmsEntity.killEntity();
                return;
            }
            isAwayFromArea = true;
        } else {
            destinationLoc = area.getLocationForStroll(bukkitEntity.getLocation(), 5,10);
            isAwayFromArea = false;
        }
        Navigation navigation = (Navigation) nmsEntity.getNavigation();
        navigation.a(true); // canOpenDoors. b(): canPassDoors, c(): canFloat.  PathfinderGoalOpenDoor과 함께 있어야 작동하는 듯?
        goalPath = navigation.a(destinationLoc.getX(), destinationLoc.getY(), destinationLoc.getZ());

        if (!isAwayFromArea) {
            nmsEntity.getNavigation().a(goalPath, speed);
        } else {
            nmsEntity.getNavigation().a(goalPath, speed*1.2);
        }
    }

    @Override
    public void d() {  // stop()
        nmsEntity.getNavigation().p(); // stop
    }
}
