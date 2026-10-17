package com.ricardo.mario.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ricardo.mario.MarioCoinEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

/** Dibuja la moneda de cara a la camara y la hace girar sobre si misma, como una moneda al aire. */
public class MarioCoinRenderer extends EntityRenderer<MarioCoinEntity> {

    private final ItemRenderer itemRenderer;

    public MarioCoinRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(MarioCoinEntity coin, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.6F, 0.6F, 0.6F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        float spin = (coin.tickCount + partialTick) * 18.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        this.itemRenderer.renderStatic(coin.getItem(), ItemDisplayContext.GROUND, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, coin.level(), coin.getId());
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(MarioCoinEntity coin) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
