package com.ssj8.client;

import com.ssj8.ModEntities;
import com.ssj8.Ssj8Mod;
import com.ssj8.client.render.FlashRenderer;
import com.ssj8.client.render.GenkidamaRenderer;
import com.ssj8.client.render.KiBlastRenderer;
import com.ssj8.client.render.Ssj8PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public final class Ssj8Client {
    private Ssj8Client() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(Ssj8Client::registerRenderers);
        modBus.addListener(Ssj8Client::addLayers);
        modBus.addListener(Ssj8Keys::register);
        MinecraftForge.EVENT_BUS.register(Ssj8Keys.class);
        MinecraftForge.EVENT_BUS.register(GuiEvents.class);
        MinecraftForge.EVENT_BUS.register(Ssj8ClientEvents.class);
        MinecraftForge.EVENT_BUS.register(KiChargeSoundHandler.class);
        if (System.getProperty("ssj8.autotest") != null) {
            try {
                Class.forName("com.ssj8.dev.AutoTest").getMethod("init", new Class[0]).invoke(null, new Object[0]);
            } catch (LinkageError | ReflectiveOperationException e) {
                Ssj8Mod.LOGGER.warn("Autotest no disponible", e);
            }
        }
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer((EntityType) ModEntities.KI_BLAST.get(), KiBlastRenderer::new);
        event.registerEntityRenderer((EntityType) ModEntities.FINAL_SILVER_FLASH.get(), FlashRenderer::new);
        event.registerEntityRenderer((EntityType) ModEntities.GENKIDAMA.get(), GenkidamaRenderer::new);
    }

    private static void addLayers(EntityRenderersEvent.AddLayers event) {
        Ssj8PlayerRenderer.instance = new Ssj8PlayerRenderer(event.getContext(), false);
        Ssj8PlayerRenderer.instanceSlim = new Ssj8PlayerRenderer(event.getContext(), true);
    }
}
