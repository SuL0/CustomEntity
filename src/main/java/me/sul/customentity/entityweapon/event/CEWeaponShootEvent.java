package me.sul.customentity.entityweapon.event;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class CEWeaponShootEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final Projectile projectile;

    public CEWeaponShootEvent(Entity entity, Projectile projectile) {
        this.entity = entity;
        this.projectile = projectile;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}