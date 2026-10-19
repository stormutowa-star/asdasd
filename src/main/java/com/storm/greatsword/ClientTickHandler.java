package com.storm.greatsword;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = GreatswordMod.MODID, value = Dist.CLIENT)
public final class ClientTickHandler {
    private ClientTickHandler() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        while (ClientSetup.DRAW_KEY.consumeClick()) {
            ItemStack main = minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
            if (main.getItem() instanceof GreatswordItem) {
                ModNetwork.CHANNEL.send(new WeaponActionPacket(WeaponActionPacket.Action.DRAW),
                        PacketDistributor.SERVER.noArg());
            }
        }

        while (ClientSetup.TACKLE_KEY.consumeClick()) {
            ItemStack main = minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
            if (main.getItem() instanceof GreatswordItem && GreatswordItem.isDrawn(main)) {
                ModNetwork.CHANNEL.send(new WeaponActionPacket(WeaponActionPacket.Action.TACKLE),
                        PacketDistributor.SERVER.noArg());
            }
        }
    }
}
