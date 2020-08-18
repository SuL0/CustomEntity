package me.sul.customentity.goal;

import net.minecraft.server.v1_12_R1.EntityLiving;

public class TargetEntity {
    private int priority;
    private boolean resetUnseenTicks;
    private boolean haveToAlertOther;
    private EntityLiving nmsEntity;
    public TargetEntity(int priority, boolean resetUnseenTicks, boolean haveToAlertOther, EntityLiving nmsEntity) {
        this.priority = priority;
        this.resetUnseenTicks = resetUnseenTicks;
        this.haveToAlertOther = haveToAlertOther;
        this.nmsEntity = nmsEntity;
    }
    public int getPriority() {
        return priority;
    }
    public EntityLiving getNmsEntity() {
        return nmsEntity;
    }
    public boolean resetUnseenTicks() {
        return resetUnseenTicks;
    }
    public boolean haveToAlertOther() { return haveToAlertOther; }
}
