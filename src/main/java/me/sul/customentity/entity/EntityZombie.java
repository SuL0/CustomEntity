package me.sul.customentity.entity;

import me.sul.customentity.goal.PathfinderGoalNearestTarget;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.HumanEntity;


// TODO: 30칸 안의 플레이어 자동 추적.
// 문 두드릴 때 5%확률로 문 열림
public class EntityZombie extends net.minecraft.server.v1_12_R1.EntityZombie implements CustomEntity {
    private static final int FOLLOW_RANGE  = 30;

    public EntityZombie(Location loc) {
        super(((CraftWorld)loc.getWorld()).getHandle());
        initializeEntity(loc);
        getWorld().addEntity(this);
    }

    private void initializeEntity(Location loc) {
        setPosition(loc.getX(), loc.getY(), loc.getZ());
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(FOLLOW_RANGE);  // initAttributes()이 FOLLOW_RANGE를 정하는데, 이건 EntityLiving의 생성자에서 호출됨
        registerGoalSelector();
    }

    private void registerGoalSelector() {
        goalSelector.a(0, new PathfinderGoalFloat(this));
        goalSelector.a(2, new PathfinderGoalZombieAttack(this, 1.0D, false));  // Navigation 포함
        goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
        goalSelector.a(7, new PathfinderGoalRandomStrollLand(this, 1.0D));
        goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        goalSelector.a(8, new PathfinderGoalRandomLookaround(this));

        // do_ 부분
        goalSelector.a(6, new PathfinderGoalMoveThroughVillage(this, 1.0D, false));
        targetSelector.a(2, new PathfinderGoalNearestTarget(this, FOLLOW_RANGE, 10, entity -> !(entity instanceof HumanEntity)));
    }

    @Override
    public void r() {}
    @Override
    public void do_() {}
}
