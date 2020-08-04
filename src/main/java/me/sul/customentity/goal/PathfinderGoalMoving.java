package me.sul.customentity.goal;

import net.minecraft.server.v1_12_R1.*;

public class PathfinderGoalMoving<T extends EntityMonster & IRangedEntity> extends PathfinderGoal {
    private final T mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private int e = -1;
    private int f;
    private boolean g;
    private boolean h;
    private int i = -1;

    public PathfinderGoalMoving(T mob, double speedModifier, int attackIntervalMin, float attackRadiusSqr) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadiusSqr * attackRadiusSqr;
        this.a(3);
    }

    public void b(int var1) {
        this.attackIntervalMin = var1;
    }

    public boolean a() {
        return this.mob.getGoalTarget() != null && this.f();
    }

    protected boolean f() {
        return !this.mob.getItemInMainHand().isEmpty() && this.mob.getItemInMainHand().getItem() == Items.BOW;
    }

    public boolean b() {
        return (this.a() || !this.mob.getNavigation().o()) && this.f();
    }

    public void c() {
        super.c();
        ((IRangedEntity)this.mob).p(true);
    }

    public void d() {
        super.d();
        ((IRangedEntity)this.mob).p(false);
        this.f = 0;
        this.e = -1;
        this.mob.cN();
    }

    public void e() {
        EntityLiving var1 = this.mob.getGoalTarget();
        if (var1 != null) {
            double var2 = this.mob.d(var1.locX, var1.getBoundingBox().b, var1.locZ);
            boolean var4 = this.mob.getEntitySenses().a(var1);
            boolean var5 = this.f > 0;
            if (var4 != var5) {
                this.f = 0;
            }

            if (var4) {
                ++this.f;
            } else {
                --this.f;
            }

            if (var2 <= (double)this.attackRadiusSqr && this.f >= 20) {
                this.mob.getNavigation().p();
                ++this.i;
            } else {
                this.mob.getNavigation().a(var1, this.speedModifier);
                this.i = -1;
            }

            if (this.i >= 20) {
                if ((double)this.mob.getRandom().nextFloat() < 0.3D) {
                    this.g = !this.g;
                }

                if ((double)this.mob.getRandom().nextFloat() < 0.3D) {
                    this.h = !this.h;
                }

                this.i = 0;
            }

            if (this.i > -1) {
                if (var2 > (double)(this.attackRadiusSqr * 0.75F)) {
                    this.h = false;
                } else if (var2 < (double)(this.attackRadiusSqr * 0.25F)) {
                    this.h = true;
                }

                this.mob.getControllerMove().a(this.h ? -0.5F : 0.5F, this.g ? 0.5F : -0.5F);
                this.mob.a(var1, 30.0F, 30.0F);
            } else {
                this.mob.getControllerLook().a(var1, 30.0F, 30.0F);
            }

            if (this.mob.isHandRaised()) {
                if (!var4 && this.f < -60) {
                    this.mob.cN();
                } else if (var4) {
                    int var6 = this.mob.cL();
                    if (var6 >= 20) {
                        this.mob.cN();
                        ((IRangedEntity)this.mob).a(var1, ItemBow.b(var6));
                        this.e = this.attackIntervalMin;
                    }
                }
            } else if (--this.e <= 0 && this.f >= -60) {
                this.mob.c(EnumHand.MAIN_HAND);
            }

        }
    }
}