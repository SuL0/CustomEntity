package me.sul.customentity.entity;

import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.sul.customentity.goal.PathfinderGoalRandomLookaround;
import me.sul.customentity.goal.PathfinderGoalStrollInSpecificArea;
import me.sul.customentity.goal.scav.*;
import me.sul.customentity.goal.scav.phasegoal.PathfinderGoalScavChaseTargetLastSeen;
import me.sul.customentity.goal.scav.phasegoal.PathfinderGoalScavHandleGun;
import me.sul.customentity.goal.scav.phasegoal.PathfinderGoalScavLookForwardInCombat;
import me.sul.customentity.goal.scav.phasegoal.PathfinderGoalScavMovementWhileShootingTarget;
import me.sul.customentity.spawnarea.Area;
import me.sul.customentity.spawnarea.AreaUtil;
import net.minecraft.server.v1_12_R1.EntitySkeleton;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_12_R1.PathfinderGoalOpenDoor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

// NOTE: 이상하게 r() 호출 후 생성자 호출됨
// TODO: 50칸 밖의 EntityScav는 안보이도록 설정
public class EntityScav extends EntitySkeleton {
    private static final String ENTITY_NAME = "§c§lAI BOT";
    private static final int FOLLOW_RANGE  = 50;

    private @Getter @Setter ScavCombatPhase scavCombatPhase;
    private @Getter ScavCombatPhaseManager scavCombatPhaseManager;
    private @Getter @Setter int unseenTick = 0;

    private final Area area;

    public EntityScav(Area area) {
        this(area, AreaUtil.getRandomLocation(area));
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
        ((org.bukkit.entity.LivingEntity)getBukkitEntity()).getEquipment().setItemInOffHand(new org.bukkit.inventory.ItemStack(Material.SHIELD, 1));
        // TODO: 재생효과 추가
        PlayerDisguise playerDisguise = new PlayerDisguise(ENTITY_NAME, "DeathSimo46");
        playerDisguise.setEntity(this.getBukkitEntity());
        playerDisguise.startDisguise();
        registerGoalSelector();
    }
    // priority 1이 start()됐을 때 -> 2 또한 canUse() 중단
    // priority 2가 start()됐을 때 -> 1은 canUse() 계속 실행중
    // 2가 start() 후 1이 start() -> 2는 중단됨

    // targetSelector은 타게팅만 하는 곳.

    private void registerGoalSelector() {
        // priority에 대체 무슨 의미가 있지
        registerTargetSelector();
        goalSelector.a(1, new PathfinderGoalFloat(this));
        goalSelector.a(2, new PathfinderGoalOpenDoor(this, false));

        scavCombatPhaseManager = new ScavCombatPhaseManager(this);
        goalSelector.a(4, new PathfinderGoalScavLookForwardInCombat(this));
        goalSelector.a(4, new PathfinderGoalScavMovementWhileShootingTarget(this));
        goalSelector.a(4, new PathfinderGoalScavHandleGun(this));
        goalSelector.a(4, new PathfinderGoalScavChaseTargetLastSeen(this));

        goalSelector.a(5, new PathfinderGoalStrollInSpecificArea<>(this, area, 1.0F, 55));
        goalSelector.a(6, new PathfinderGoalRandomLookaround(this, 0.5F));
    }
    private void registerTargetSelector() {
        targetSelector.a(1, new PathfinderGoalScavTargetSelector(this));
    }

    @Override
    public void r() {}
    @Override
    public void dm() {} // 몹 아이템에 따라 원거리/근거리 정하는 메소드
    // 몹마다 다른데 좀비는 do_()도 오버라이드 해줘야지 기본 설정 targetSelector가 사라짐
}
