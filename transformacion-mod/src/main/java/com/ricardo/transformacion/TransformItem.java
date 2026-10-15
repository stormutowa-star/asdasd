package com.ricardo.transformacion;

import com.ricardo.transformacion.TransformState;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TransformItem
extends Item {
    private static final int COOLDOWN_TICKS = 10;

    public TransformItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            if (sp.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.pass(stack);
            }
            sp.getCooldowns().addCooldown(this, 10);
            boolean activate = !TransformState.isTransformed(sp);
            TransformState.set(sp, activate);
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), activate ? SoundEvents.PORTAL_TRIGGER : SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, activate ? 0.8f : 1.4f);
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, sp.getX(), sp.getY() + 1.0, sp.getZ(), 40, 0.4, 0.8, 0.4, 0.1);
            }
            sp.displayClientMessage(Component.translatable((String)(activate ? "message.transformacion.on" : "message.transformacion.off")), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}

