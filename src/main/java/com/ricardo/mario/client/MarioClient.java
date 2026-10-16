package com.ricardo.mario.client;

import com.ricardo.mario.MarioMod;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Registros que solo existen en el cliente (modelo y renderer). */
public final class MarioClient {

    private MarioClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(MarioClient::onRegisterLayers);
        modEventBus.addListener(MarioClient::onRegisterRenderers);
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MarioModel.LAYER, MarioModel::createBodyLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MarioMod.MARIO.get(), MarioRenderer::new);
    }
}
