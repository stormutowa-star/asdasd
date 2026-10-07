package com.ricardo.blueeyes.client;

import net.minecraftforge.client.event.EntityRenderersEvent;

import com.ricardo.blueeyes.BlueEyesMod;

/** Registro de renderers y modelos (solo cliente). */
public final class ClientSetup {
    private ClientSetup() {}

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BlueEyesMod.DRAGON.get(), BlueEyesRenderer::new);
        event.registerEntityRenderer(BlueEyesMod.FIELD_CARD.get(), FieldCardRenderer::new);
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BlueEyesModel.LAYER, BlueEyesLayer::createBodyLayer);
    }
}
