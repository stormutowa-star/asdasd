package com.keinloree.item;

import com.keinloree.world.LooreStory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Nucleo de Decodificacion: al usarlo (clic derecho) activa la señal de Kein Loree.
 * Clic derecho sobre Kein Loree mientras ofrece su trato: acepta la oferta.
 */
public class DecodeCoreItem extends Item {

    public DecodeCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level instanceof ServerLevel sl && player instanceof ServerPlayer sp) {
            LooreStory.startMission(sl, sp);
        }
        return InteractionResultHolder.success(stack);
    }
}
