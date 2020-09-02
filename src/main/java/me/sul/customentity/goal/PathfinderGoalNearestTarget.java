package me.sul.customentity.goal;

import me.sul.customentity.util.DistanceComparator;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.List;
import java.util.function.Predicate;

public class PathfinderGoalNearestTarget extends EasilyModifiedPathfinderGoal {
    private final double maxDistance;
    private final int randomInterval;
    private final Predicate<? super Entity> targetFilter;

    private final DistanceComparator.Bukkit bukkitDistanceComparator;

    public PathfinderGoalNearestTarget(EntityCreature nmsEntity, double maxDistance, int randomInterval, Predicate<? super Entity> targetFilter) {
        super(nmsEntity);
        this.maxDistance = Math.min(maxDistance, getFollowDistance());
        this.randomInterval = randomInterval;
        this.targetFilter = targetFilter;

        bukkitDistanceComparator = new DistanceComparator.Bukkit(bukkitEntity);
    }


    @Override
    public boolean canUse() { // 4틱마다 반복
        if (getRandom().nextInt(randomInterval) != 0) return false;
        EntityLiving nmsTarget = findTarget();
        if (nmsTarget != null) {
            setGoalTarget(nmsTarget, (nmsTarget instanceof EntityPlayer) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() { // 1틱마다 반복
        if (getRandom().nextInt(randomInterval*4) == 0 || !isInTargetableState(getGoalTarget())) {
            EntityLiving nmsTarget = findTarget();
            setGoalTarget(nmsTarget, (nmsTarget instanceof EntityPlayer) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
            return (nmsTarget != null);
        }
        return true;
    }

    private EntityLiving findTarget() {
        List<Entity> nearEntities = bukkitEntity.getNearbyEntities(maxDistance, maxDistance, maxDistance);
        if (nearEntities == null || nearEntities.size() == 0) return null;
        nearEntities.removeIf(entity -> !(entity instanceof LivingEntity) || !isInTargetableState((EntityLiving) ((CraftEntity)entity).getHandle()));
        nearEntities.removeIf(targetFilter);
        nearEntities.sort(bukkitDistanceComparator);
        if (nearEntities.size() < 1) return null;
        return (EntityLiving) ((CraftEntity)nearEntities.get(0)).getHandle();
    }

    @Override
    public void stop() {
        removeGoalTarget();
    }
}
