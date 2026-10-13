package com.storm.greatsword;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Escucha la pulsacion de Z en el cliente y pide al servidor alternar desenfundar/enfundar.
@Mod.EventBusSubscriber(modid = GreatswordMod.MODID, value = Dist.CLIENT)
public final class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // consumeClick() devuelve true solo en el frame en que se pulso la tecla.
        while (ClientSetup.DRAW_KEY.consumeClick()) {
            ItemStack main = mc.player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
            if (main.getItem() instanceof GreatswordItem) {
                // Envia la peticion al servidor (que es quien aplica y sincroniza el cambio).
                ModNetwork.CHANNEL.send(new ToggleDrawPacket(),
                        net.minecraftforge.network.PacketDistributor.SERVER.noArg());
            }
        }
    }
}
