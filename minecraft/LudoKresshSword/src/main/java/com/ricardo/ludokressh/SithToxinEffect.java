package com.ricardo.ludokressh;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * «Toxina Sith»: drena 10 puntos de aguante por segundo.
 * En Skyrim el aguante base es 100, así que son un 10 % por segundo. El aguante de Minecraft es la
 * barra de hambre (20 puntos): un 10 % son 2 puntos por segundo (4 de agotamiento = 1 punto).
 * Los mobs no tienen hambre, así que el agotamiento los deja lentos mientras dura la toxina.
 */
public class SithToxinEffect extends MobEffect {
    public static final float EXHAUSTION_PER_SECOND = 8.0F;

    public SithToxinEffect() {
        super(MobEffectCategory.HARMFUL, 0x8BC34A);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 1;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide || SithPoisonEffect.isImmune(entity)) {
            return true;
        }
        if (entity instanceof Player player) {
            player.causeFoodExhaustion(EXHAUSTION_PER_SECOND);
        } else {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 21, 0, false, false));
        }
        return true;
    }
}
