package com.keinloree.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Daño de la faccion Loree. En creativo, un jugador recibe el golpe, el empuje y el sonido,
 * pero su vida no baja (el modo creativo es para vivir la historia sin morir). En supervivencia
 * el daño es normal.
 */
public final class LooreDamage {

    private LooreDamage() {
    }

    public static void apply(LivingEntity target, DamageSource source, float amount, Vec3 dir, double push) {
        if (!target.isAlive()) {
            return;
        }
        if (target instanceof Player player && player.getAbilities().instabuild) {
            shove(target, dir, push);
            if (target.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.6D), target.getZ(),
                        8, 0.3D, 0.3D, 0.3D, 0.1D);
            }
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_HURT, SoundSource.HOSTILE, 0.8F, 1.0F);
            return;
        }
        target.invulnerableTime = 0;
        target.hurt(source, amount);
        shove(target, dir, push);
    }

    private static void shove(LivingEntity target, Vec3 dir, double push) {
        if (push <= 0.0D) {
            return;
        }
        Vec3 m = target.getDeltaMovement();
        target.setDeltaMovement(m.x + dir.x * push, Math.max(m.y, 0.0D) + Math.min(0.5D, push * 0.4D),
                m.z + dir.z * push);
        target.hurtMarked = true;
    }
}
