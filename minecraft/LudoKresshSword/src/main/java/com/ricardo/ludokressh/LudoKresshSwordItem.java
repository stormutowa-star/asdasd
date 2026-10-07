package com.ricardo.ludokressh;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;

/** Encantamiento «Veneno Sith»: cada golpe aplica el veneno y la toxina durante 5 segundos. */
public class LudoKresshSwordItem extends SwordItem {
    public static final int EFFECT_TICKS = 5 * 20;

    public LudoKresshSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide && !SithPoisonEffect.isImmune(target)) {
            target.addEffect(new MobEffectInstance(LudoKresshMod.SITH_POISON.getHolder().orElseThrow(), EFFECT_TICKS, 0), attacker);
            target.addEffect(new MobEffectInstance(LudoKresshMod.SITH_TOXIN.getHolder().orElseThrow(), EFFECT_TICKS, 0), attacker);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ludokressh.ludo_kressh_sword.enchantment").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.ludokressh.ludo_kressh_sword.poison").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ludokressh.ludo_kressh_sword.toxin").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ludokressh.ludo_kressh_sword.weight_value").withStyle(ChatFormatting.DARK_GRAY));
    }
}
