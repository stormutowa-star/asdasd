package com.keinloree.client;

import com.keinloree.KeinLooreMod;
import com.keinloree.registry.ModEntities;
import com.keinloree.client.test.ClientAutoTest;
import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.entity.LooreCommander;
import com.keinloree.entity.LooreWarlord;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Registros que solo existen en el cliente (modelos y renderers). */
public final class KeinLooreClient {

    private KeinLooreClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(KeinLooreClient::onRegisterLayers);
        modEventBus.addListener(KeinLooreClient::onRegisterRenderers);
        ClientAutoTest.init();
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(KeinLooreModel.LAYER, KeinLooreModel::createBodyLayer);
        event.registerLayerDefinition(DroidModel.LAYER, DroidModel::createBodyLayer);
        event.registerLayerDefinition(CargoShipModel.LAYER, CargoShipModel::createBodyLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.KEIN_LOREE.get(), ctx -> new LooreRenderer<KeinLooreEntity>(ctx));
        event.registerEntityRenderer(ModEntities.LOREE_COMMANDER.get(), ctx -> new LooreRenderer<LooreCommander>(ctx));
        event.registerEntityRenderer(ModEntities.LOREE_WARLORD.get(), ctx -> new LooreRenderer<LooreWarlord>(ctx));
        event.registerEntityRenderer(ModEntities.DROID.get(), DroidRenderer::new);
        event.registerEntityRenderer(ModEntities.CARGO_SHIP.get(), CargoShipRenderer::new);
    }
}
