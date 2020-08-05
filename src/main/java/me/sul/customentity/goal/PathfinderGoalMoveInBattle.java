package me.sul.customentity.goal;

import me.sul.customentity.entity.CustomEntity;
import net.minecraft.server.v1_12_R1.*;

// PathfinderGoalBowShoot기반
// PathfinderGoalShootEntity가 설정하는 mob.getGoalTarget()을 기반으로 타겟이 있으면 실행되고, 없어지면 멈춘다.
public class PathfinderGoalMoveInBattle<T extends EntityCreature & CustomEntity> extends PathfinderGoal {
    private final T entity;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public PathfinderGoalMoveInBattle(T entity, double speedModifier, float attackRadiusSqr) {
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.attackRadiusSqr = attackRadiusSqr * attackRadiusSqr;
        this.a(3);
    }

    @Override
    public boolean a() { // canUse()
        return entity.getGoalTarget() != null;
    }

    @Override
    public boolean b() { // canContinueToUse()
        return a();
    }

    @Override
    public void c() { // start()
        super.c();
//        ((IRangedEntity)mob).p(true); // setSwingingArms(true). IRangedEntity중 SkeletonAbstract에만 있는 DataWatcher값. 대체 뭐하는건지 모르겠음.
    }

    @Override
    public void d() { // stop()
        super.d();
//        ((IRangedEntity)mob).p(false); // start()쪽의 설명과 같음
        this.seeTime = 0;
    }

    @Override
    public void e() { // tick()
        EntityLiving goalTarget = this.entity.getGoalTarget();
        if (goalTarget != null) {
            double distanceBetweenMobAndTarget = this.entity.d(goalTarget.locX, goalTarget.getBoundingBox().b, goalTarget.locZ); // distanceToSqr(x, minY, z)
            boolean canSee = this.entity.getEntitySenses().a(goalTarget);
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
                this.entity.getNavigation().p();
                ++this.strafingTime;
            } else {
                this.entity.getNavigation().a(goalTarget, this.speedModifier);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if ((double)this.entity.getRandom().nextFloat() < 0.3D) {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if ((double)this.entity.getRandom().nextFloat() < 0.3D) {
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

                this.entity.getControllerMove().a(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                this.entity.a(goalTarget, 30.0F, 30.0F);
            } else {
                this.entity.getControllerLook().a(goalTarget, 30.0F, 30.0F);
            }
        }
    }
}