package com.keinloree.entity;

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
import net.minecraft.world.level.Level;

/** Ejecutor de Kein Loree: comandante pesado con hombreras. Golpea el suelo cuando lo tienes cerca. */
public class LooreWarlord extends LooreCommander {

    private int slamCooldown = 60;

    public LooreWarlord(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 160.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    @Override
    public int getLoreSkin() {
        return 2;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide || this.isLoreDormant()) {
            return;
        }
        if (this.slamCooldown > 0) {
            this.slamCooldown--;
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || this.distanceToSqr(target) > 16.0D) {
            return;
        }
        if (this.level() instanceof ServerLevel level) {
            this.slamCooldown = 70;
            this.holdPose(POSE_CHARGE, 12);
            Fx.ring(level, this.position().add(0.0D, 0.1D, 0.0D), 4.0D, 40, Fx.GREEN, 0.0D);
            Fx.burst(level, Fx.CODE, this.position(), 24, 2.0D, 0.2D);
            Fx.sound(level, this.position(), SoundEvents.GENERIC_EXPLODE.value(), 0.8F, 0.6F);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, Geo.box(this.position(), 4.0D, 2.0D, 4.0D),
                    x -> x != this && x.isAlive() && !(x instanceof LooreMob))) {
                this.strike(e, 10.0F, 0.9D);
            }
        }
    }
}
