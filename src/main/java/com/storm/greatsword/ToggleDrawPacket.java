package com.storm.greatsword;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;

// Packet cliente -> servidor: pide alternar desenfundar/enfundar la gran espada.
// El servidor valida y aplica el cambio, que luego se sincroniza via componente DRAWN.
public class ToggleDrawPacket {

    public ToggleDrawPacket() {}

    // Sin payload: solo la intencion de alternar.
    public static void encode(ToggleDrawPacket msg, FriendlyByteBuf buf) {}

    public static ToggleDrawPacket decode(FriendlyByteBuf buf) {
        return new ToggleDrawPacket();
    }

    public static void handle(ToggleDrawPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) {
            return;
        }

        // Se ejecuta en el hilo principal del servidor (consumerMainThread).
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

        // Solo actua si la mano principal es una gran espada.
        if (!(stack.getItem() instanceof GreatswordItem sword)) {
            player.displayClientMessage(Component.translatable("msg.greatsword.not_holding"), true);
            return;
        }

        boolean drawn = GreatswordItem.isDrawn(stack);

        if (!drawn) {
            // Para desenfundar, la mano secundaria debe estar libre (es un arma a dos manos).
            if (!player.getOffhandItem().isEmpty()) {
                player.displayClientMessage(
                        Component.translatable("msg.greatsword.need_free_offhand"), true);
                return;
            }
        }

        GreatswordItem.setDrawn(stack, !drawn);

        // Fuerza la reevaluacion de atributos del item en el inventario del jugador.
        player.inventoryMenu.sendAllDataToRemote();

        // Sonido + mensaje de estado.
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.6F, 1.4F);
        player.displayClientMessage(Component.translatable(
                drawn ? "msg.greatsword.sheathed" : "msg.greatsword.drawn"), true);
    }
}
