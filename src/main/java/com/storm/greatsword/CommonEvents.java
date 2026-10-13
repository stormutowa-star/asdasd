package com.storm.greatsword;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Mecanica de "dos manos": mientras la gran espada esta desenfundada en la mano
// principal, la mano secundaria (offhand) queda bloqueada: no se puede usar ni
// equipar ningun objeto ahi.
@Mod.EventBusSubscriber(modid = GreatswordMod.MODID)
public final class CommonEvents {

    // Comprueba si la mano principal del jugador es una gran espada desenfundada.
    private static boolean holdingDrawnGreatsword(Player player) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        return main.getItem() instanceof GreatswordItem && GreatswordItem.isDrawn(main);
    }

    // Cancela el clic derecho con la mano secundaria mientras se empuña a dos manos.
    @SubscribeEvent
    public static void onOffhandInteract(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() == InteractionHand.OFF_HAND
                && holdingDrawnGreatsword(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onOffhandUseOn(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.OFF_HAND
                && holdingDrawnGreatsword(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onOffhandEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() == InteractionHand.OFF_HAND
                && holdingDrawnGreatsword(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // Si algo intenta equiparse en el offhand con la espada desenfundada, lo devuelve al inventario.
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getSlot() == EquipmentSlot.OFFHAND
                && !event.getTo().isEmpty()
                && holdingDrawnGreatsword(player)) {
            // Devuelve el objeto al inventario principal para no perderlo.
            if (!player.getInventory().add(event.getTo())) {
                player.drop(event.getTo(), false);
            }
            event.setCanceled(true);
        }
    }
}
