package com.ricardo.akagane;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AkaganeItem extends Item {

    public AkaganeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            Techniques.use(sp, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** Cada corte (golpe al aire, a un bloque o a un mob) deja una marca automaticamente. */
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
            Techniques.onSwing(sp, stack);
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            Techniques.tick(sp, stack, selected);
        }
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    // El cambio de reiatsu (NBT) no debe hacer que la espada "rebote" en la mano.
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    // ---- Barra de reiatsu (energia espiritual) bajo el icono ----

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return Techniques.state(stack) > 0 || Techniques.rei(stack) < Techniques.MAX_REI;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * Techniques.rei(stack) / (float) Techniques.MAX_REI);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return switch (Techniques.state(stack)) {
            case Techniques.SHIKAI -> 0xC81E1E;
            case Techniques.BANKAI -> 0xFF2A2A;
            default -> 0x9A9AAE;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tips, TooltipFlag flag) {
        super.appendHoverText(stack, context, tips, flag);
        int st = Techniques.state(stack);
        tips.add(Component.literal("Zanpakuto: Akagane (Oro Rojo)").withStyle(ChatFormatting.DARK_RED));
        tips.add(Component.literal("Reiatsu: " + (Techniques.rei(stack) * 100 / Techniques.MAX_REI) + "%").withStyle(ChatFormatting.RED));
        if (st == Techniques.SEALED) {
            tips.add(Component.literal("Sellada. Clic derecho o di: Arde, Akagane.").withStyle(ChatFormatting.GRAY));
        } else if (st == Techniques.SHIKAI) {
            tips.add(Component.literal("SHIKAI - cada corte deja una marca (max. 5)").withStyle(ChatFormatting.GRAY));
            tips.add(Component.literal("Clic der.: repite el corte mas antiguo").withStyle(ChatFormatting.GRAY));
            tips.add(Component.literal("Agachado + clic der.: Gyakuzan (las 5 a la vez)").withStyle(ChatFormatting.GRAY));
            tips.add(Component.literal("Agachado + mirar abajo + clic der. (o chat: Bankai): Guren no Kiba").withStyle(ChatFormatting.GRAY));
        } else {
            tips.add(Component.literal("BANKAI - Guren no Kiba").withStyle(ChatFormatting.GRAY));
            tips.add(Component.literal("Clic der.: conecta las marcas en un solo corte").withStyle(ChatFormatting.GRAY));
            tips.add(Component.literal("Agachado + clic der.: libera los cortes conectados").withStyle(ChatFormatting.GRAY));
            tips.add(Component.literal("Agachado + mirar arriba + clic der.: Guren Retsudan").withStyle(ChatFormatting.GRAY));
            tips.add(Component.literal("Agachado + mirar abajo + clic der.: sellar").withStyle(ChatFormatting.GRAY));
        }
    }
}
