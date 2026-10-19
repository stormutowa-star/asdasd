package com.lenadorbosque.client;

import com.lenadorbosque.LenadorBosque;
import com.lenadorbosque.ModEntities;
import com.lenadorbosque.entity.DeadDeerEntity;
import com.lenadorbosque.entity.WoodcutterEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Solo se carga en el cliente. Asocia cada entidad con su modelo, textura y animaciones de GeckoLib. */
public final class ClientSetup {

    private ClientSetup() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientSetup::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WOODCUTTER.get(), ctx ->
                new GeoEntityRenderer<WoodcutterEntity>(ctx,
                        new DefaultedEntityGeoModel<WoodcutterEntity>(new ResourceLocation(LenadorBosque.MODID, "woodcutter"))));

        event.registerEntityRenderer(ModEntities.DEAD_DEER.get(), ctx ->
                new GeoEntityRenderer<DeadDeerEntity>(ctx,
                        new DefaultedEntityGeoModel<DeadDeerEntity>(new ResourceLocation(LenadorBosque.MODID, "dead_deer"))));
    }
}
