package me.sul.customentity.goal;

import me.sul.customentity.entity.CustomEntity;
import me.sul.customentity.spawnarea.Area;
import me.sul.customentity.util.DebugUtil;
import net.minecraft.server.v1_12_R1.*;

// PathfinderGoalMoveThroughVillage 기반. private 메소드 오버라이딩 못하는 문제로 상속하지 않았음.
// TODO: 길이 막히는 등으로 갈 수 없는 경우면 다른 곳으로 위치 재설정
public class PathfinderGoalStrollInSpecificArea<T extends EntityCreature & CustomEntity> extends PathfinderGoal {
    private final T entity;
    private final Area area;
    private final double speed;
    private PathEntity goalPath;
    private final int randomInterval;
    private boolean isAwayFromArea = false;

    public PathfinderGoalStrollInSpecificArea(T entity, Area area, double speed, int randomInterval) {
        this.entity = entity;
        this.area = area;
        this.speed = speed;
        this.randomInterval = randomInterval;
        this.a(1); // setFlag() ? 모든게 다 a(1)임
        if (!(entity.getNavigation() instanceof Navigation)) {
            throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
        }
    }


    @Override
    public boolean a() {  // canUse()
        if (entity.getRandom().nextInt(randomInterval) != 0) return false;
        if (entity.getGoalTarget() != null) return false;

        org.bukkit.Location entityLoc = entity.getBukkitEntity().getLocation();
        org.bukkit.Location targetLoc = entityLoc;
        // 지정 구역에서 벗어났을 때
        if (area.isAwayFromArea(entityLoc)) {
            isAwayFromArea = true;
            targetLoc = area.getClosestLocation(entityLoc);
            if (targetLoc.distance(entityLoc) > entity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).getValue()) {
                entity.killEntity();
            }
        } else {
            isAwayFromArea = false;
            for (int i = 0; i <= 100; i++) { // 거의 무한
                targetLoc = area.getRandomLocation();
                if (entityLoc.distance(targetLoc) <= 10 && !area.isAwayFromArea(targetLoc)) { // TODO: 이부분 보완. 랜덤으로 주변 위치 얻는 메소드로 변경해야 할 듯. Vec3D vec3d = RandomPositionGenerator.a(this.entity, 5, 4);  // 자신 근처 아무데나 위치?
                    break;
                }
                if (i == 100) DebugUtil.printStackTrace();
            }
        }
        Navigation navigation = (Navigation) entity.getNavigation();
        navigation.a(true); // canOpenDoors. b(): canPassDoors, c(): canFloat   .  PathfinderGoalOpenDoor과 함께 있어야 작동하는 듯?
        goalPath = navigation.a(targetLoc.getX(), targetLoc.getY(), targetLoc.getZ());
        return true;
    }

    // TODO: 전투상황에서는 true를 반환하지 못하게 해야 함
    @Override
    public boolean b() {  // canContinueToUse()
        return !entity.getNavigation().o() && entity.getGoalTarget() == null;
    }

    @Override
    public void c() {  // start()
        entity.getNavigation().a(goalPath, speed);
    }

    @Override
    public void d() {  // stop()
        entity.getNavigation().p(); // stop
    }
}
