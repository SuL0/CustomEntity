package me.sul.customentity.goal;

import me.sul.customentity.entity.CustomEntity;
import net.minecraft.server.v1_12_R1.*;

// PathfinderGoalBowShoot기반

// MEMO: Pathfind의 기본적인 흐름에 관한 메모
// 우선 a()가 계속 실행되면서 체크를 한다.
// a()가 true일 시 c()가 실행되고, c()가 실행되면 1틱마다 d()가 실행된다.
// 그리고 b()는 c() -> d()를 계속 실행해도 되는지 계속 체크한다.

// PathfinderGoalShootEntity가 설정하는 mob.getGoalTarget()을 기반으로 타겟이 있으면 실행되고, 없어지면 멈춘다.
public class PathfinderGoalMoveInBattle<T extends EntityMonster & CustomEntity> extends PathfinderGoal {
    private final T mob;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public PathfinderGoalMoveInBattle(T mob, double speedModifier, float attackRadiusSqr) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackRadiusSqr = attackRadiusSqr * attackRadiusSqr;
        this.a(3);
    }

    @Override
    public boolean a() { // canUse()
        return mob.getGoalTarget() != null;
    }

    @Override
    public void c() { // start()
        super.c();
//        ((IRangedEntity)mob).p(true); // setSwingingArms(true). IRangedEntity중 SkeletonAbstract에만 있는 DataWatcher값. 대체 뭐하는건지 모르겠음.
        if (isHoldingBow()) {
            mob.c(EnumHand.MAIN_HAND); // startUsingItem()
        }
    }

    @Override
    public boolean b() { // canContinueToUse() - start()를 계속 지속해도 되는가?
        return a();
    }

    @Override
    public void d() { // stop()
        super.d();
//        ((IRangedEntity)mob).p(false); // start()쪽의 설명과 같음
        this.seeTime = 0;
        if (isHoldingBow()) {
            this.mob.cN(); // stopUsingItem()
        }
    }

    @Override
    public void e() { // tick()
        EntityLiving goalTarget = this.mob.getGoalTarget();
        if (goalTarget != null) {
            double distanceBetweenMobAndTarget = this.mob.d(goalTarget.locX, goalTarget.getBoundingBox().b, goalTarget.locZ); // distanceToSqr(x, minY, z)
            boolean canSee = this.mob.getEntitySenses().a(goalTarget);
            boolean isSeeing = this.seeTime > 0;
            if (canSee != isSeeing) {
                this.seeTime = 0;
            }
            if (canSee) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            // 이부분 수정해야할 듯
            // PathfinderGoalShootEntity가 지금 현재 몹을 보고 총을 쏘고 있는지 여부를 알 수 있는 Method가 필요함
            if (distanceBetweenMobAndTarget <= (double)this.attackRadiusSqr && this.seeTime >= 20) {
                this.mob.getNavigation().p();
                ++this.strafingTime;
            } else {
                this.mob.getNavigation().a(goalTarget, this.speedModifier);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if ((double)this.mob.getRandom().nextFloat() < 0.3D) {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if ((double)this.mob.getRandom().nextFloat() < 0.3D) {
                    this.strafingBackwards = !this.strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (distanceBetweenMobAndTarget > (double)(this.attackRadiusSqr * 0.75F)) {
                    this.strafingBackwards = false;
                } else if (distanceBetweenMobAndTarget < (double)(this.attackRadiusSqr * 0.25F)) {
                    this.strafingBackwards = true;
                }

                this.mob.getControllerMove().a(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                this.mob.a(goalTarget, 30.0F, 30.0F);
            } else {
                this.mob.getControllerLook().a(goalTarget, 30.0F, 30.0F);
            }
        }
    }
    private boolean isHoldingBow() { return !this.mob.getItemInMainHand().isEmpty() && this.mob.getItemInMainHand().getItem() == Items.BOW; }
}