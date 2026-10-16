package com.ricardo.mario.client;

import com.ricardo.mario.MarioEntity;
import com.ricardo.mario.MarioMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MarioRenderer extends MobRenderer<MarioEntity, MarioModel<MarioEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MarioMod.MODID, "textures/entity/mario.png");

    public MarioRenderer(EntityRendererProvider.Context context) {
        super(context, new MarioModel<MarioEntity>(context.bakeLayer(MarioModel.LAYER)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(MarioEntity entity) {
        return TEXTURE;
    }
}
