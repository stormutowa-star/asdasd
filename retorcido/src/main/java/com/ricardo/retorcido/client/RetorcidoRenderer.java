package com.ricardo.retorcido.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ricardo.retorcido.RetorcidoEntity;
import com.ricardo.retorcido.RetorcidoMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RetorcidoRenderer extends MobRenderer<RetorcidoEntity, RetorcidoModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RetorcidoMod.MODID, "textures/entity/retorcido.png");

    public RetorcidoRenderer(EntityRendererProvider.Context context) {
        super(context, new RetorcidoModel(context.bakeLayer(RetorcidoModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new RetorcidoEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(RetorcidoEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(RetorcidoEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.3F, 1.3F, 1.3F);
    }
}
