package com.keinloree.entity;

import com.keinloree.registry.ModEntities;
import com.keinloree.util.Fx;
import com.keinloree.util.Geo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Comandante de Kein Loree: de su misma raza. Dispara dos rayos a la vez y, en combate,
 * llama droides a su alrededor.
 */
public class LooreCommander extends LooreMob implements RangedAttackMob {

    private int summonCooldown = 200;

    public LooreCommander(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 90.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    public int getLoreSkin() {
        return 1;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0D, 40, 22.0F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide || this.isLoreDormant() || this.getTarget() == null) {
            return;
        }
        if (this.summonCooldown > 0) {
            this.summonCooldown--;
            return;
        }
        if (this.level() instanceof ServerLevel level) {
            this.summonCooldown = 400;
            this.holdPose(POSE_SUMMON, 30);
            for (int i = 0; i < 2; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0D;
                Droid droid = ModEntities.DROID.get().create(level);
                if (droid != null) {
                    droid.moveTo(this.getX() + Math.cos(a) * 3.0D, this.getY(), this.getZ() + Math.sin(a) * 3.0D,
                            this.getYRot(), 0.0F);
                    droid.setTarget(this.getTarget());
                    level.addFreshEntity(droid);
                }
            }
            Fx.sound(level, this.position(), SoundEvents.BEACON_POWER_SELECT, 1.0F, 1.4F);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        this.holdPose(POSE_FIRE, 10);
        Vec3 from = this.getEyePosition();
        Vec3 side = Geo.side(this.getLookAngle()).scale(0.7D);
        Vec3 aim = target.getEyePosition();
        this.laser(level, from.add(side), aim.add(side.scale(0.4D)), 6.0F, Fx.GREEN);
        this.laser(level, from.subtract(side), aim.subtract(side.scale(0.4D)), 6.0F, Fx.GREEN);
        this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 0.7F);
    }
}
