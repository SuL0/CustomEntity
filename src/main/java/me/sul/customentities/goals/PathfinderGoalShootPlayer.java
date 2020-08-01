package me.sul.customentities.goals;

import com.google.common.base.Predicate;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;

// PathfinderGoalNearestAttackableTarget 기반
public class PathfinderGoalShootPlayer extends PathfinderGoalTarget {
    private final EntityCreature entityCreature;
    private final int randomInterval;
    private EntityLiving target;
    private final Predicate<EntityHuman> predicate;
    private BukkitRunnable weaponRunnable = null;


    public PathfinderGoalShootPlayer(EntityCreature nmsCreature, int randomInterval, boolean mustSee, boolean mustReach) { // mustSee: 무조건 시야각에 들어와야 인식, mustReach: 때릴 수 있는 범위 안에 들어와야 인식
        super(nmsCreature, mustSee, mustReach);
        this.entityCreature = this.e;
        this.randomInterval = randomInterval;
        this.predicate = new Predicate() {
            public boolean a(@Nullable EntityHuman entityHuman) {
                return entityHuman != null && IEntitySelector.e.apply(entityHuman) && (PathfinderGoalShootPlayer.this.a(entityHuman, false));
            }

            public boolean apply(@Nullable Object object) {
                return this.a((EntityHuman) object);
            }
        };
        this.a(1);
    }

    @Override
    public boolean a() {  // canUse()
        if (weaponRunnable != null && !weaponRunnable.isCancelled()) weaponRunnable.cancel();
        if (entityCreature.getRandom().nextInt(randomInterval) != 0) return false;
        this.target = this.e.world.a(this.e.locX, this.e.locY + (double)this.e.getHeadHeight(), this.e.locZ, this.i(), this.i(), null, predicate);
        return this.target != null;
    }

    @Override
    public void c() {  // start() - 타게팅 되면 딱 한번 실행되고, 그동안 canUse() 중단됨
        this.entityCreature.setGoalTarget(target, EntityTargetEvent.TargetReason.CUSTOM, true);
        super.c();
//        weaponRunnable = new BukkitRunnable() {
//            @Override
//            public void run() {
//                // 총쏘기
//            }
//        };
    }

    // b()는 PathfinderGoalTarget에 있음.
}
