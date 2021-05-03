package me.sul.customentity.goal;

import me.sul.customentity.util.PathfinderUtil;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.NavigationAbstract;
import net.minecraft.server.v1_12_R1.PathfinderGoal;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.Random;

public abstract class EasilyModifiedPathfinderGoal extends PathfinderGoal {
    public final EntityCreature nmsEntity;
    public final Entity bukkitEntity;

    public EasilyModifiedPathfinderGoal(EntityCreature nmsEntity) {
        this.nmsEntity = nmsEntity;
        this.bukkitEntity = nmsEntity.getBukkitEntity();
    }

    @Override
    public boolean a() { return canUse(); }
    @Override
    public boolean b() { return canContinueToUse(); }
    @Override
    public void c() { start(); }
    @Override
    public void d() { stop(); }
    @Override
    public void e() { tick(); }

    public boolean canUse() { return false; } // 4틱마다 반복
    public boolean canContinueToUse() { return false; } // 1틱마다 반복
    public void start() { }
    public void stop() { }
    public void tick() { }

    public enum Flag {
        MOVE_OR_TARGET,
        LOOK,
        JUMP
    }
    public void setFlag(Flag ...flags) { // (중복되는 goal 방지 하지 않으려면 없어도 됨)
        int value = 0;
        for (Flag flag : flags) {
            if (flag == Flag.MOVE_OR_TARGET) value += 1;
            else if (flag == Flag.LOOK) value += 2;
            else if (flag == Flag.JUMP) value += 4;
        }
        this.a(value);
    }

    // 기본
    public Random getRandom() { return nmsEntity.getRandom(); }
    public EntityLiving getGoalTarget() { return nmsEntity.getGoalTarget(); }
    public void setGoalTarget(EntityLiving nmsTarget, EntityTargetEvent.TargetReason targetReason) { nmsEntity.setGoalTarget(nmsTarget, (nmsTarget != null) ? targetReason : EntityTargetEvent.TargetReason.FORGOT_TARGET, true); }
    public void removeGoalTarget() { PathfinderUtil.removeGoalTarget(nmsEntity); }

    public NavigationAbstract getNavigation() { return nmsEntity.getNavigation(); }
    public void stopNavigation() { PathfinderUtil.stopNavigation(nmsEntity); }
    public boolean isNavigationDone() { return PathfinderUtil.isNavigationDone(nmsEntity); }

    // 커스텀  // 이걸 상속한 타겟과 관련된 클래스를 생성해서 거기로 옮겨야하려나
    public void moveToLoc(Location location, double speed, boolean canOpenDoors) { PathfinderUtil.moveToLoc(nmsEntity, location, speed, canOpenDoors); }

    public boolean isInTargetableState(EntityLiving nmsOpponent) { return PathfinderUtil.isInTargetableState(bukkitEntity, nmsOpponent, PathfinderUtil.getFollowDistance(nmsEntity)); }

    public boolean isInSight(Entity bukkitOpponent) { return PathfinderUtil.isInSight(bukkitEntity, bukkitOpponent, 130); }

    public double getFollowDistance() { return PathfinderUtil.getFollowDistance(nmsEntity); }
}
