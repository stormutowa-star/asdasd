package com.ricardo.retorcido.client;

import com.ricardo.retorcido.RetorcidoMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RetorcidoMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RetorcidoClientEvents {
    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RetorcidoModel.LAYER_LOCATION, RetorcidoModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RetorcidoMod.RETORCIDO.get(), RetorcidoRenderer::new);
        event.registerEntityRenderer(RetorcidoMod.AMIGO.get(), AmigoRenderer::new);
    }
}
