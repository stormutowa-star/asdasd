package com.ricardo.transformacion.client;

import com.ricardo.transformacion.TransformacionMod;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Dibuja la transformacion encima del jugador: rostro, pelo, alas y guanteletes. */
public class TransformLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation HEAD_TEXTURE = texture("head");
    private static final ResourceLocation WINGS_TEXTURE = texture("wings");
    private static final ResourceLocation RIGHT_HAND_TEXTURE = texture("hand_right");
    private static final ResourceLocation LEFT_HAND_TEXTURE = texture("hand_left");

    private static final float WING_SPREAD = 0.35F;
    private static final float WING_FLAP = 0.12F;

    private final ModelPart headRoot;
    private final ModelPart wingsRoot;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart gauntletRoot;

    public TransformLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                          EntityModelSet models, boolean slim) {
        super(parent);
        this.headRoot = models.bakeLayer(TransformModels.HEAD);
        this.wingsRoot = models.bakeLayer(TransformModels.WINGS);
        this.wingLeft = wingsRoot.getChild("wing_left");
        this.wingRight = wingsRoot.getChild("wing_right");
        this.gauntletRoot = models.bakeLayer(slim ? TransformModels.GAUNTLET_SLIM : TransformModels.GAUNTLET_WIDE);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(TransformacionMod.MODID, "textures/entity/" + name + ".png");
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!ClientTransformState.isTransformed(player.getUUID()) || player.isInvisible()) {
            return;
        }
        PlayerModel<AbstractClientPlayer> parent = getParentModel();

        poseStack.pushPose();
        parent.head.translateAndRotate(poseStack);
        headRoot.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(HEAD_TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        float flap = Mth.sin(ageInTicks * 0.1F) * WING_FLAP;
        wingLeft.yRot = -WING_SPREAD + flap;
        wingRight.yRot = WING_SPREAD - flap;
        poseStack.pushPose();
        parent.body.translateAndRotate(poseStack);
        wingsRoot.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(WINGS_TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        renderGauntlet(poseStack, buffer, packedLight, parent.rightArm, RIGHT_HAND_TEXTURE);
        renderGauntlet(poseStack, buffer, packedLight, parent.leftArm, LEFT_HAND_TEXTURE);
    }

    private void renderGauntlet(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                ModelPart arm, ResourceLocation texture) {
        poseStack.pushPose();
        arm.translateAndRotate(poseStack);
        gauntletRoot.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(texture)),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
