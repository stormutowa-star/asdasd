package com.ricardo.blueeyes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.ricardo.blueeyes.BlueEyesMod;
import com.ricardo.blueeyes.FieldCardEntity;

/** Dibuja la carta boca arriba sobre el terreno. */
public class FieldCardRenderer extends EntityRenderer<FieldCardEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(BlueEyesMod.MODID, "textures/entity/blue_eyes_card.png");
    private static final ResourceLocation BACK =
            ResourceLocation.fromNamespaceAndPath(BlueEyesMod.MODID, "textures/entity/blue_eyes_card_back.png");
    private static final float LENGTH = 1.3F;
    private static final float WIDTH = LENGTH * 448.0F / 656.0F;

    public FieldCardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(FieldCardEntity card) {
        return TEXTURE;
    }

    @Override
    public void render(FieldCardEntity card, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // volteo animado al activar / desactivar
        boolean faceUp = card.isFaceUp();
        if (faceUp != card.lastFaceUp) {
            card.lastFaceUp = faceUp;
            card.flipTick = card.tickCount;
        }
        float p = Mth.clamp((card.tickCount + partialTick - card.flipTick) / 10.0F, 0.0F, 1.0F);
        float from = faceUp ? 180.0F : 0.0F;
        float to = faceUp ? 0.0F : 180.0F;
        float flip = Mth.lerp(p, from, to);
        float lift = Mth.sin(p * Mth.PI) * 0.6F;

        poseStack.pushPose();
        // aparece girando y cayendo sobre el campo
        float appear = Mth.clamp((card.tickCount + partialTick) / 12.0F, 0.0F, 1.0F);
        poseStack.translate(0.0D, 0.03D + (1.0F - appear) * 0.6D + lift, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw + (1.0F - appear) * 360.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(flip));
        poseStack.scale(appear, 1.0F, appear);

        PoseStack.Pose pose = poseStack.last();
        float hw = WIDTH / 2.0F;
        float hl = LENGTH / 2.0F;
        int light = LightTexture.FULL_BRIGHT;
        // anverso (arriba): la parte de arriba de la carta hacia -Z local
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        vertex(vc, pose, -hw, 0.004F, -hl, 0.0F, 0.0F, light, 1.0F);
        vertex(vc, pose, -hw, 0.004F, hl, 0.0F, 1.0F, light, 1.0F);
        vertex(vc, pose, hw, 0.004F, hl, 1.0F, 1.0F, light, 1.0F);
        vertex(vc, pose, hw, 0.004F, -hl, 1.0F, 0.0F, light, 1.0F);
        // reverso (abajo)
        VertexConsumer back = buffer.getBuffer(RenderType.entityCutoutNoCull(BACK));
        vertex(back, pose, hw, -0.004F, -hl, 0.0F, 0.0F, light, -1.0F);
        vertex(back, pose, hw, -0.004F, hl, 0.0F, 1.0F, light, -1.0F);
        vertex(back, pose, -hw, -0.004F, hl, 1.0F, 1.0F, light, -1.0F);
        vertex(back, pose, -hw, -0.004F, -hl, 1.0F, 0.0F, light, -1.0F);
        poseStack.popPose();
        super.render(card, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light, float ny) {
        vc.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, ny, 0.0F);
    }
}
