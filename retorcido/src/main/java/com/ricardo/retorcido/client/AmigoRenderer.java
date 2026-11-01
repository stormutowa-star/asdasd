package com.ricardo.retorcido.client;

import com.ricardo.retorcido.amigo.AmigoEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** El amigo se ve como un jugador (skin vanilla "kai", modelo ancho). */
public class AmigoRenderer extends HumanoidMobRenderer<AmigoEntity, PlayerModel<AmigoEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/player/wide/kai.png");

    public AmigoRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(AmigoEntity entity) {
        return TEXTURE;
    }
}
