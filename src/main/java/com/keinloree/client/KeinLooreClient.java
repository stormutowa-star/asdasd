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
        StoryClient.register(modEventBus);
        modEventBus.addListener(com.keinloree.client.vfx.VfxParticles::onRegister);
        modEventBus.addListener(com.keinloree.client.ship.OverworldFx::register);
        modEventBus.addListener((net.minecraftforge.client.event.RegisterClientReloadListenersEvent e) ->
                e.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) rm ->
                        com.keinloree.client.ship.ShipMesh.release()));
        com.keinloree.client.ship.ClientShipInput.register(modEventBus);
        modEventBus.addListener(com.keinloree.client.ship.FighterClient::onLayers);
        ClientAutoTest.init();
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(KeinLooreModel.LAYER, KeinLooreModel::createBodyLayer);
        event.registerLayerDefinition(DroidModel.LAYER, DroidModel::createBodyLayer);
        event.registerLayerDefinition(CargoShipModel.LAYER, CargoShipModel::createBodyLayer);
        event.registerLayerDefinition(com.keinloree.client.model.gen.TurbolaserHeavyModel.LAYER, com.keinloree.client.model.gen.TurbolaserHeavyModel::createBodyLayer);
        event.registerLayerDefinition(com.keinloree.client.model.gen.TurbolaserDefenseModel.LAYER, com.keinloree.client.model.gen.TurbolaserDefenseModel::createBodyLayer);
        event.registerLayerDefinition(com.keinloree.client.model.gen.FighterModel.LAYER, com.keinloree.client.model.gen.FighterModel::createBodyLayer);
        event.registerLayerDefinition(com.keinloree.client.model.gen.RazakoModel.LAYER, com.keinloree.client.model.gen.RazakoModel::createBodyLayer);
        event.registerLayerDefinition(com.keinloree.client.model.gen.AlbaModel.LAYER, com.keinloree.client.model.gen.AlbaModel::createBodyLayer);
        event.registerLayerDefinition(com.keinloree.client.model.gen.CenitModel.LAYER, com.keinloree.client.model.gen.CenitModel::createBodyLayer);
        event.registerLayerDefinition(com.keinloree.client.model.gen.OcasoModel.LAYER, com.keinloree.client.model.gen.OcasoModel::createBodyLayer);
        event.registerLayerDefinition(com.keinloree.client.model.gen.MedianocheModel.LAYER, com.keinloree.client.model.gen.MedianocheModel::createBodyLayer);
        event.registerLayerDefinition(com.keinloree.client.model.gen.EclipseModel.LAYER, com.keinloree.client.model.gen.EclipseModel::createBodyLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.KEIN_LOREE.get(), ctx -> new LooreRenderer<KeinLooreEntity>(ctx));
        event.registerEntityRenderer(ModEntities.LOREE_COMMANDER.get(), ctx -> new LooreRenderer<LooreCommander>(ctx));
        event.registerEntityRenderer(ModEntities.LOREE_WARLORD.get(), ctx -> new LooreRenderer<LooreWarlord>(ctx));
        event.registerEntityRenderer(ModEntities.DROID.get(), DroidRenderer::new);
        event.registerEntityRenderer(ModEntities.CARGO_SHIP.get(), CargoShipRenderer::new);
        event.registerEntityRenderer(ModEntities.DESTROYER.get(), com.keinloree.client.ship.DestroyerRenderer::new);
        event.registerEntityRenderer(ModEntities.TURBOLASER.get(), com.keinloree.client.render.TurretRenderer::new);
        event.registerEntityRenderer(ModEntities.BOLT.get(), com.keinloree.client.render.BoltRenderer::new);
        event.registerEntityRenderer(ModEntities.FIGHTER.get(), com.keinloree.client.render.FighterRenderer::new);
        event.registerEntityRenderer(ModEntities.RAZAKO.get(), com.keinloree.client.render.RazakoRenderer::new);
        event.registerEntityRenderer(ModEntities.CALENDARIO.get(), com.keinloree.client.render.HeroRenderer::new);
        event.registerEntityRenderer(ModEntities.KEIN_HOLOGRAM.get(), com.keinloree.client.render.HologramRenderer::new);
    }
}
