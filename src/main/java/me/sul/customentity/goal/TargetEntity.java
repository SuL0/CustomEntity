package me.sul.customentity.goal;

import net.minecraft.server.v1_12_R1.EntityLiving;

public class TargetEntity {
    private int priority;
    private boolean haveToMaintainUnseenTicks;
    private boolean haveToAlertOther;
    private EntityLiving nmsEntity;
    public TargetEntity(int priority, boolean haveToMaintainUnseenTicks, boolean haveToAlertOther, EntityLiving nmsEntity) {
        this.priority = priority;
        this.haveToMaintainUnseenTicks = haveToMaintainUnseenTicks;
        this.haveToAlertOther = haveToAlertOther;
        this.nmsEntity = nmsEntity;
    }
    public int getPriority() {
        return priority;
    }
    public EntityLiving getNmsEntity() {
        return nmsEntity;
    }
    public boolean haveToMaintainUnseenTicks() {
        return haveToMaintainUnseenTicks;
    }
    public boolean haveToAlertOther() { return haveToAlertOther; }
}
