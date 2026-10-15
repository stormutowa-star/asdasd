package com.ricardo.transformacion.client;

import com.ricardo.transformacion.TransformacionMod;

import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Solo se carga en el cliente: registra los modelos y la capa en los dos tipos de jugador (ancho y delgado). */
public final class TransformClient {

    private TransformClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(TransformClient::registerLayers);
        modBus.addListener(TransformClient::addLayers);
        MinecraftForge.EVENT_BUS.register(TransformClient.class);
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TransformModels.HEAD, TransformModels::createHead);
        event.registerLayerDefinition(TransformModels.WINGS, TransformModels::createWings);
        event.registerLayerDefinition(TransformModels.GAUNTLET_WIDE, () -> TransformModels.createGauntlet(false));
        event.registerLayerDefinition(TransformModels.GAUNTLET_SLIM, () -> TransformModels.createGauntlet(true));
    }

    private static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new TransformLayer(renderer, event.getEntityModels(), skin == PlayerSkin.Model.SLIM));
            }
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientTransformState.clear();
    }
}
