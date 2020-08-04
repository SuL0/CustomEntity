package me.sul.customentity.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomEntityShootEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Entity entity;
    private Projectile projectile;

    public CustomEntityShootEvent(Entity entity, Projectile projectile) {
        this.entity = entity;
        this.projectile = projectile;
    }

    public Entity getEntity() {
        return entity;
    }
    public Projectile getProjectile() { return projectile; }

    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}