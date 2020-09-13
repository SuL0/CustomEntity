package me.sul.customentity.entityweapon.event;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class CEWeaponHitBlockEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final Entity projectile;
    // block을 추가하려면 꽤나 골치가 아파짐. 어차피 CSA에 calcProjectileStruckLocation()에 있으니 그거 쓰는게 좋음

    public CEWeaponHitBlockEvent(Entity entity, Entity projectile) {
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
