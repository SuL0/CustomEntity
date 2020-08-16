package me.sul.customentity.goal.target;

import net.minecraft.server.v1_12_R1.EntityLiving;

public class TargetEntity {
    private int priority;
    boolean haveToMaintainUnseenTicks;
    private EntityLiving nmsEntity;
    public TargetEntity(int priority, boolean haveToMaintainUnseenTicks, EntityLiving nmsEntity) {
        this.priority = priority;
        this.haveToMaintainUnseenTicks = haveToMaintainUnseenTicks;
        this.nmsEntity = nmsEntity;
    }
    public int getPriority() {
        return priority;
    }
    public EntityLiving getNmsEntity() {
        return nmsEntity;
    }
    public boolean isTargetingReasonHaveToMaintainUnseenTicks() {
        return haveToMaintainUnseenTicks;
    }
}
