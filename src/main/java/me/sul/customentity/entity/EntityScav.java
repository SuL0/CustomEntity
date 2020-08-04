package me.sul.customentity.entity;

import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.sul.customentity.goal.target.PathfinderGoalShootEntity;
import me.sul.customentity.goal.PathfinderGoalStrollInSpecificArea;
import me.sul.customentity.spawnarea.Area;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

// NOTE: 이상하게 r() 호출 후 생성자 호출됨
public class EntityScav extends EntitySkeleton {
    private final Area area;

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
        setCustomName("§cScav");
        setCustomNameVisible(true);
        getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(15);
//        ((org.bukkit.entity.LivingEntity)getBukkitEntity()).getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.DIAMOND_PICKAXE, 1, (short)2));
        ((org.bukkit.entity.LivingEntity)getBukkitEntity()).getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.BOW, 1));
        // TODO: 재생효과 추가
        PlayerDisguise playerDisguise = new PlayerDisguise("§cScav");
        playerDisguise.setEntity(this.getBukkitEntity());
        playerDisguise.startDisguise();
        registerGoalSelector();
        registerTargetSelector();
    }
    private void registerGoalSelector() {
        goalSelector.a(1, new PathfinderGoalFloat(this));
        goalSelector.a(1, new PathfinderGoalOpenDoor(this, false));
        goalSelector.a(3, new PathfinderGoalStrollInSpecificArea(this, area, 1.0F, 60));
        goalSelector.a(4, new PathfinderGoalBowShoot<EntityScav>(this, 1.0D, 20, 15.0F));
        goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
    }
    private void registerTargetSelector() {
        // 유저는 원거리로만 공격
        targetSelector.a(1, new PathfinderGoalShootEntity<>(this, EntityPlayer.class, 5, true, false));
        targetSelector.a(2, new PathfinderGoalShootEntity<>(this, EntityMonster.class, 5, true, false));
//        targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, 5, true, false, null));
//        targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(this, EntityInsentient.class, 5, false, false, (var0) -> {
//            return var0 instanceof IMonster && !(var0 instanceof EntityScav);
//        }));
    }



    @Override
    public void r() {}
    @Override
    public void dm() {} // 몹 아이템에 따라 원거리/근거리 정하는 메소드
    // 몹마다 다른데 좀비는 do_()도 오버라이드 해줘야지 기본 설정 targetSelector가 사라짐
}
