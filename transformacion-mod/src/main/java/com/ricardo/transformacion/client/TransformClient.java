package com.ricardo.transformacion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class TransformClient {
    private static TransformLayer wideLayer;
    private static TransformLayer slimLayer;

    private TransformClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(TransformClient::registerLayers);
        modBus.addListener(TransformClient::addLayers);
        MinecraftForge.EVENT_BUS.register(TransformClient.class);
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TransformModels.HEAD, TransformModels::createHead);
        event.registerLayerDefinition(TransformModels.BODY, TransformModels::createBody);
        event.registerLayerDefinition(TransformModels.WINGS, TransformModels::createWings);
        event.registerLayerDefinition(TransformModels.GAUNTLET_RIGHT_WIDE, () -> TransformModels.createGauntlet(false, false));
        event.registerLayerDefinition(TransformModels.GAUNTLET_RIGHT_SLIM, () -> TransformModels.createGauntlet(false, true));
        event.registerLayerDefinition(TransformModels.GAUNTLET_LEFT_WIDE, () -> TransformModels.createGauntlet(true, false));
        event.registerLayerDefinition(TransformModels.GAUNTLET_LEFT_SLIM, () -> TransformModels.createGauntlet(true, true));
    }

    private static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            if (event.getPlayerSkin(skin) instanceof PlayerRenderer renderer) {
                boolean slim = skin == PlayerSkin.Model.SLIM;
                RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent = renderer;
                TransformLayer layer = new TransformLayer(parent, event.getEntityModels(), slim);
                renderer.addLayer(layer);
                if (slim) {
                    slimLayer = layer;
                } else {
                    wideLayer = layer;
                }
            }
        }
    }

    /** Guantelete tambien en la mano de primera persona. */
    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        AbstractClientPlayer player = event.getPlayer();
        if (!ClientTransformState.isTransformed(player.getUUID())) {
            return;
        }
        boolean slim = player.getSkin().model() == PlayerSkin.Model.SLIM;
        TransformLayer layer = slim ? slimLayer : wideLayer;
        if (layer == null || !(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) {
            return;
        }
        boolean left = event.getArm() == HumanoidArm.LEFT;
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        layer.renderFirstPerson(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(),
                left ? model.leftArm : model.rightArm, left);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientTransformState.clear();
    }
}
