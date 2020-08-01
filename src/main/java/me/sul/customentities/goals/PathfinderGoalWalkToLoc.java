package me.sul.customentities.goals;

import net.minecraft.server.v1_12_R1.*;

// https://www.spigotmc.org/threads/creating-custom-entitys-with-pathfindergoals-1-11.236443/
// 다른 방법: https://bukkit.org/threads/custom-pathfinder-exemple-nms-v1_12_r1-1-12.460404/
public class PathfinderGoalWalkToLoc extends PathfinderGoal {
    private EntityCreature nmsCreature;  // NMS Entity
    protected double speed;  // speed
    private double x;  // random PosX
    private double y;  // random PosY
    private double z;  // random PosZ

    public PathfinderGoalWalkToLoc(EntityCreature entitycreature, double d0, double x, double y, double z) {
        this.nmsCreature = entitycreature;
        this.speed = d0;
        this.y = y;
        this.x = x;
        this.z = z;
//        this.a(1);
    }

    // b(움직일 때 b만 실행) -> a -> c
    @Override
    public boolean a() {  // canUse()
        Vec3D vec3d = RandomPositionGenerator.a(this.nmsCreature, 5, 4);  // 자신 근처 아무데나 위치?
        if (vec3d == null) { return false; }  // IN AIR?
        return true;  // execute c()
    }

    @Override
    public void c() {  // start()
        Vec3D vec3d = RandomPositionGenerator.a(this.nmsCreature, 5, 4);
        if (vec3d == null) return;

        nmsCreature.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(60);
        this.nmsCreature.getNavigation().a(x, y, z, 2);
        nmsCreature.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(35);
    }

    @Override
    public boolean b() { // canContinueToUse()
        return !this.nmsCreature.getNavigation().o();  // isDone()
    }
}