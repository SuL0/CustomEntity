package me.sul.customentity.util;

import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EnumHand;

public class EntityAnimation {
    public static void startUsingItem(EntityLiving nmsEntity, EnumHand hand) {
        nmsEntity.c(hand);
    }
    public static void stopUsingItem(EntityLiving nmsEntity) {
        nmsEntity.cN();
    }
}
