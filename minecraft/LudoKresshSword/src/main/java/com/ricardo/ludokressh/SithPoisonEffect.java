package com.ricardo.ludokressh;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** «Veneno de Ludo Kressh»: 12 puntos de daño por segundo (ignora armadura, como el veneno de Skyrim).
 *  Afecta a todos los mobs, también a los no muertos. */
public class SithPoisonEffect extends MobEffect {
    public static final float DAMAGE_PER_SECOND = 12.0F;

    public SithPoisonEffect() {
        super(MobEffectCategory.HARMFUL, 0x3FCF5A);
    }

    // Un golpe dura 100 ticks: se aplica a los 1, 2, 3, 4 y 5 segundos (5 veces).
    // Volver a golpear reinicia la cuenta sin dar un daño extra inmediato.
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 1;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            entity.hurt(entity.damageSources().magic(), DAMAGE_PER_SECOND);
            PoisonFx.tickBurst(entity);
            PoisonFx.play(entity, LudoKresshMod.POISON_SOUND.get(), 0.45F);
        }
        return true;
    }
}
