package me.sul.customentity.goal;

import net.minecraft.server.v1_12_R1.EntityInsentient;

import java.util.Random;

public class PathfinderGoalRandomLookaround extends net.minecraft.server.v1_12_R1.PathfinderGoalRandomLookaround {
    private final EntityInsentient nmsEntity;
    private final float chance;

    private Random random = new Random();
    public PathfinderGoalRandomLookaround(EntityInsentient entityInsentient, float chance) {
        super(entityInsentient);
        this.nmsEntity = entityInsentient;
        this.chance = chance;
    }

    @Override
    public boolean a() { // canUse()
        return random.nextFloat() < chance && nmsEntity.getGoalTarget() == null;
    }
}
