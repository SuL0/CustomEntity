package me.sul.customentity.entity;

import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.sul.customentity.goal.PathfinderGoalMoveInBattle;
import me.sul.customentity.goal.PathfinderGoalStrollInSpecificArea;
import me.sul.customentity.goal.target.PathfinderGoalFindEntityAndShootIt;
import me.sul.customentity.spawnarea.Area;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

// NOTE: 이상하게 r() 호출 후 생성자 호출됨
public class EntityScav extends EntitySkeleton implements CustomEntity {
    private static final String ENTITY_NAME = "§c§lAI BOT";
    private static final int FOLLOW_RANGE  = 50;

    private final Area area;
    private boolean isSeeingTarget = false;

    public EntityScav(Area area) {
        this(area, area.getRandomLocation());
    }
    public EntityScav(Area area, Location loc) {
        super (((CraftWorld)loc.getWorld()).getHandle());
        this.area = area;
        initializeEntity(loc);
        getWorld().addEntity(this);
    }

    private void initializeEntity(Location loc) {
        setPosition(loc.getX(), loc.getY(), loc.getZ());
        setCustomName(ENTITY_NAME);
        setCustomNameVisible(true);
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(FOLLOW_RANGE);
        ((org.bukkit.entity.LivingEntity)getBukkitEntity()).getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.DIAMOND_PICKAXE, 1, (short)2));
//        ((org.bukkit.entity.LivingEntity)getBukkitEntity()).getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.BOW, 1));
        // TODO: 재생효과 추가
        PlayerDisguise playerDisguise = new PlayerDisguise(ENTITY_NAME, "DeathSimo46");
        playerDisguise.setEntity(this.getBukkitEntity());
        playerDisguise.startDisguise();
        registerGoalSelector();
        registerTargetSelector();
    }
    private void registerGoalSelector() {
        goalSelector.a(1, new PathfinderGoalFloat(this));
        goalSelector.a(2, new PathfinderGoalOpenDoor(this, false));
        goalSelector.a(4, new PathfinderGoalMoveInBattle<EntityScav>(this, 1.0D, 15.0F));
        goalSelector.a(5, new PathfinderGoalStrollInSpecificArea<EntityScav>(this, area, 1.0F, 45));
        goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
    }
    private void registerTargetSelector() {
        // priority 1이 start()됐을 때 -> 2 또한 canUse() 중단
        // priority 2가 start()됐을 때 -> 1은 canUse() 계속 실행중
        // 2가 start() 후 1이 start() -> 2는 중단됨

        targetSelector.a(1, new PathfinderGoalFindEntityAndShootIt<>(this, 3, 4F, 5.0F, 5));
    }

    @Override
    public void setSeeingTarget(boolean b) {
        isSeeingTarget = b;
    }
    @Override
    public boolean isSeeingTarget() {
        return isSeeingTarget && getGoalTarget() != null;
    }



    @Override
    public void r() {}
    @Override
    public void dm() {} // 몹 아이템에 따라 원거리/근거리 정하는 메소드
    // 몹마다 다른데 좀비는 do_()도 오버라이드 해줘야지 기본 설정 targetSelector가 사라짐
}
