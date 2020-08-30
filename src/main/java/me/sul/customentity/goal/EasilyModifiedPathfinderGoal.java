package me.sul.customentity.goal;

import net.minecraft.server.v1_12_R1.PathfinderGoal;

public class EasilyModifiedPathfinderGoal extends PathfinderGoal {
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

    protected boolean canUse() { return false; }

    protected boolean canContinueToUse() { return canUse(); }

    protected void start() { }

    protected void stop() { }

    protected void tick() { }
}
