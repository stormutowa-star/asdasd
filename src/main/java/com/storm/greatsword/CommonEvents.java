package com.storm.greatsword;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GreatswordMod.MODID)
public final class CommonEvents {
    private CommonEvents() {}

    private static boolean holdingDrawnGreatsword(Player player) {
        ItemStack main = player.getMainHandItem();
        return main.getItem() instanceof GreatswordItem && GreatswordItem.isDrawn(main);
    }

    // ------------------------------------------------------------------
    // The offhand is occupied by the second hand while the sword is drawn.
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onOffhandInteract(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() == InteractionHand.OFF_HAND && holdingDrawnGreatsword(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onOffhandUseOn(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.OFF_HAND && holdingDrawnGreatsword(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onOffhandEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() == InteractionHand.OFF_HAND && holdingDrawnGreatsword(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onOffhandSpecificEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getHand() == InteractionHand.OFF_HAND && holdingDrawnGreatsword(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMainHandEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Switching away from the greatsword automatically sheathes it so that it never invisibly
        // remains "drawn" in another inventory slot. Reference equality keeps a component-only
        // refresh (the same stack being re-detected) from sheathing the sword that is still in hand.
        if (event.getSlot() == EquipmentSlot.MAINHAND
                && event.getFrom().getItem() instanceof GreatswordItem
                && GreatswordItem.isDrawn(event.getFrom())
                && event.getFrom() != event.getTo()) {
            GreatswordItem.setDrawn(event.getFrom(), false);
            player.inventoryMenu.sendAllDataToRemote();
        }
    }

    @SubscribeEvent
    public static void onOffhandEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player
                && event.getSlot() == EquipmentSlot.OFFHAND
                && event.getTo().getItem() instanceof GreatswordItem
                && GreatswordItem.isDrawn(event.getTo())) {
            // A drawn sword is only valid in the main hand.
            GreatswordItem.setDrawn(event.getTo(), false);
            player.inventoryMenu.sendAllDataToRemote();
        }
    }

    // ------------------------------------------------------------------
    // Safety net: vanilla attacks with a greatsword are always handled by the mod packet.
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity().getMainHandItem().getItem() instanceof GreatswordItem) {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------
    // Housekeeping: drawn swords left in other slots are sheathed automatically.
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }

        Player player = event.player;
        if (player.tickCount % 20 != 0 || !player.isAlive()) {
            return;
        }

        boolean changed = false;
        ItemStack mainHand = player.getMainHandItem();

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof GreatswordItem
                    && GreatswordItem.isDrawn(stack)
                    && stack != mainHand) {
                GreatswordItem.setDrawn(stack, false);
                changed = true;
            }
        }

        // A drawn sword is only valid in the main hand. This also catches a main/offhand swap, which
        // would otherwise leave a second hand free while the sword stayed in its drawn state.
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof GreatswordItem && GreatswordItem.isDrawn(stack)) {
                GreatswordItem.setDrawn(stack, false);
                changed = true;
            }
        }

        if (changed) {
            player.inventoryMenu.sendAllDataToRemote();
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            GreatswordActions.clear(serverPlayer);
        }
    }
}
