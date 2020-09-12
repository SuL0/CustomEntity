package me.sul.customentity.entityweapon.event;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class CEWeaponHitBlockEvent extends Event {
    private static final @Getter HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final Entity projectile;
    private final Block block;

    public CEWeaponHitBlockEvent(Entity entity, Entity projectile, Block block) {
        this.entity = entity;
        this.projectile = projectile;
        this.block = block;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
