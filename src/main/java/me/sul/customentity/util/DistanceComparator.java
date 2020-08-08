package me.sul.customentity.util;

import net.minecraft.server.v1_12_R1.Entity;

import java.util.Comparator;

public class DistanceComparator {
    public static class Nms implements Comparator<Entity> {
        private final Entity entity;
        public Nms(Entity entity) {
            this.entity = entity;
        }
        public int compare(Entity entity1, Entity entity2) {
            double distance1 = this.entity.h(entity1);
            double distance2 = this.entity.h(entity2);
            return Double.compare(distance1, distance2);
        }
    }

    public static class Bukkit implements Comparator<org.bukkit.entity.Entity> {
        private final org.bukkit.entity.Entity entity;
        public Bukkit(org.bukkit.entity.Entity entity) {
            this.entity = entity;
        }
        public int compare(org.bukkit.entity.Entity entity1, org.bukkit.entity.Entity entity2) {
            double distance1 = entity.getLocation().distance(entity1.getLocation());
            double distance2 = entity.getLocation().distance(entity2.getLocation());
            return Double.compare(distance1, distance2);
        }
    }
}
