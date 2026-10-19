package com.storm.greatsword;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Replaces the vanilla melee hit with the greatsword's own combo packet. If the sword is sheathed, the first
 * click draws it instead, which makes the weapon feel like a Monster Hunter greatsword.
 */
@Mod.EventBusSubscriber(modid = GreatswordMod.MODID, value = Dist.CLIENT)
public final class ClientInputHandler {
    private static long nextAllowedRequest;

    private ClientInputHandler() {}

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        ItemStack stack = minecraft.player.getMainHandItem();
        if (!(stack.getItem() instanceof GreatswordItem)) {
            return;
        }

        long now = minecraft.level.getGameTime();

        if (!GreatswordItem.isDrawn(stack)) {
            event.setCanceled(true);
            if (now >= nextAllowedRequest) {
                nextAllowedRequest = now + 4;
                ModNetwork.CHANNEL.send(new WeaponActionPacket(WeaponActionPacket.Action.DRAW),
                        PacketDistributor.SERVER.noArg());
            }
            return;
        }

        if (minecraft.player.getCooldowns().isOnCooldown(stack.getItem())) {
            event.setCanceled(true);
            return;
        }

        // Cancel vanilla damage so only the custom cone/combos can hurt entities.
        event.setCanceled(true);

        if (now >= nextAllowedRequest) {
            nextAllowedRequest = now + 2;
            ModNetwork.CHANNEL.send(new WeaponActionPacket(WeaponActionPacket.Action.ATTACK),
                    PacketDistributor.SERVER.noArg());
        }
    }
}
