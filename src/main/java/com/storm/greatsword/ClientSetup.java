package com.storm.greatsword;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

// Configuracion exclusiva del cliente: tecla Z para desenfundar/enfundar.
public final class ClientSetup {
    private ClientSetup() {}

    public static final String CATEGORY = "key.greatsword.category";

    // Tecla Z por defecto.
    public static final KeyMapping DRAW_KEY = new KeyMapping(
            "key.greatsword.draw",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            CATEGORY);

    public static void onClientSetup(FMLClientSetupEvent event) {
        // El registro real de la KeyMapping ocurre en el evento del bus de mod (abajo).
        // Aqui solo encolamos la inicializacion segura en el hilo de cliente.
        event.enqueueWork(ClientSetup::registerClient);
    }

    private static void registerClient() {
        // Se registra el handler de tick de cliente para leer la tecla.
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(ClientTickHandler.class);
    }

    // Registro de la KeyMapping en el bus de mod (clase estatica anidada con SubscribeEvent).
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(
            modid = GreatswordMod.MODID,
            bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT)
    public static class ModBus {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(DRAW_KEY);
        }

        // Anade la capa de la espada en la espalda a cada renderer de jugador.
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void addLayers(net.minecraftforge.client.event.EntityRenderersEvent.AddLayers event) {
            net.minecraft.client.renderer.entity.ItemRenderer itemRenderer =
                    net.minecraft.client.Minecraft.getInstance().getItemRenderer();
            for (net.minecraft.client.resources.PlayerSkin.Model skin : event.getSkins()) {
                @SuppressWarnings("unchecked")
                net.minecraft.client.renderer.entity.player.PlayerRenderer renderer =
                        (net.minecraft.client.renderer.entity.player.PlayerRenderer) event.getPlayerSkin(skin);
                renderer.addLayer(new BackSheathLayer(renderer, itemRenderer));
            }
        }
    }
}
