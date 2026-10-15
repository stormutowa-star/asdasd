package com.ricardo.transformacion;

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

public class TransformItem extends Item {
    private static final int COOLDOWN_TICKS = 10;

    public TransformItem(Properties properties) {
        super(properties);
    }

    /** Clic derecho: alterna la transformacion. El cambio ocurre en el servidor y se envia a quien pueda ver al jugador. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (sp.getCooldowns().isOnCooldown(stack)) {
                return InteractionResultHolder.pass(stack);
            }
            sp.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            boolean activate = !TransformState.isTransformed(sp);
            TransformState.set(sp, activate);

            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    activate ? SoundEvents.PORTAL_TRIGGER : SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS, 0.8F, activate ? 0.8F : 1.4F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 0.4D, 0.8D, 0.4D, 0.1D);
            }
            sp.displayClientMessage(Component.translatable(
                    activate ? "message.transformacion.on" : "message.transformacion.off"), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
