package me.sul.customentity.goal;

import net.minecraft.server.v1_12_R1.EntityInsentient;

import java.util.Random;

public class PathfinderGoalRandomLookaround extends net.minecraft.server.v1_12_R1.PathfinderGoalRandomLookaround {
    private Random random = new Random();
    private final float chance;
    public PathfinderGoalRandomLookaround(EntityInsentient entityInsentient, float chance) {
        super(entityInsentient);
        this.chance = chance;
    }

    @Override
    public boolean a() { // canUse()
        return random.nextFloat() < chance;
    }
}
