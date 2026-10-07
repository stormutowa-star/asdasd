package com.ricardo.ludokressh;

import org.joml.Vector3f;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/** Destellos y sonidos del Veneno Sith (se envían desde el servidor para que todos los vean). */
public final class PoisonFx {
    private static final DustParticleOptions GREEN_DUST = new DustParticleOptions(new Vector3f(0.25F, 0.85F, 0.30F), 1.3F);
    private static final ColorParticleOption POISON_SWIRL = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF3FCF5A);

    private PoisonFx() {
    }

    /** Estallido de veneno al recibir el golpe. */
    public static void hitBurst(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }
        double x = target.getX(), y = target.getY(0.55), z = target.getZ();
        double w = target.getBbWidth() * 0.45, h = target.getBbHeight() * 0.3;
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 14, w, h, w, 0.0);
        level.sendParticles(GREEN_DUST, x, y, z, 24, w, h, w, 0.0);
        level.sendParticles(POISON_SWIRL, x, y, z, 12, w, h, w, 1.0);
    }

    /** Destellos más pequeños cada vez que el veneno hace daño. */
    public static void tickBurst(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }
        double x = target.getX(), y = target.getY(0.6), z = target.getZ();
        double w = target.getBbWidth() * 0.4, h = target.getBbHeight() * 0.3;
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 5, w, h, w, 0.0);
        level.sendParticles(GREEN_DUST, x, y, z, 10, w, h, w, 0.0);
    }

    /** Tajo de la hoja (sonidos vanilla). */
    public static void swingSound(LivingEntity attacker) {
        play(attacker, SoundEvents.PLAYER_ATTACK_SWEEP, 0.7F, 1.15F);
    }

    /** Impacto de la hoja: corte metálico + golpe fuerte (sonidos vanilla). */
    public static void hitSound(LivingEntity target) {
        play(target, SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
        play(target, SoundEvents.PLAYER_ATTACK_STRONG, 0.9F, 0.95F);
    }

    /** Burbujeo y siseo del veneno al entrar (sonidos vanilla). */
    public static void poisonSound(LivingEntity target) {
        play(target, SoundEvents.BREWING_STAND_BREW, 0.8F, 1.4F);
        play(target, SoundEvents.FIRE_EXTINGUISH, 0.25F, 1.7F);
    }

    /** Siseo suave cada segundo mientras dura el veneno. */
    public static void poisonTickSound(LivingEntity target) {
        play(target, SoundEvents.FIRE_EXTINGUISH, 0.2F, 1.8F);
    }

    private static void play(LivingEntity at, SoundEvent sound, float volume, float pitch) {
        float jitter = 0.92F + at.getRandom().nextFloat() * 0.16F;
        at.level().playSound(null, at.getX(), at.getY(), at.getZ(), sound, SoundSource.PLAYERS, volume, pitch * jitter);
    }
}
