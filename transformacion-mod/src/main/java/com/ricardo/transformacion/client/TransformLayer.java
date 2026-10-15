package com.ricardo.transformacion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Pelo, rostro, alas, vestido y guanteletes sobre el modelo del jugador. */
public class TransformLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation HEAD = tex("head");
    private static final ResourceLocation HEAD_GLOW = tex("head_glow");
    private static final ResourceLocation BODY = tex("body");
    private static final ResourceLocation BODY_GLOW = tex("body_glow");
    private static final ResourceLocation WINGS = tex("wings");
    private static final ResourceLocation WINGS_GLOW = tex("wings_glow");
    private static final ResourceLocation GAUNTLET = tex("gauntlet");
    private static final ResourceLocation GAUNTLET_GLOW = tex("gauntlet_glow");

    private final ModelPart head;
    private final ModelPart hairBack;
    private final ModelPart hairBack2;
    private final ModelPart[] tufts = new ModelPart[5];
    private final ModelPart body;
    private final ModelPart skirtFront;
    private final ModelPart skirtBack;
    private final ModelPart skirtLeft;
    private final ModelPart skirtRight;
    private final ModelPart wings;
    private final ModelPart wingRightUp;
    private final ModelPart wingLeftUp;
    private final ModelPart wingRightLow;
    private final ModelPart wingLeftLow;
    private final ModelPart gauntletRight;
    private final ModelPart gauntletLeft;

    public TransformLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models, boolean slim) {
        super(parent);
        head = models.bakeLayer(TransformModels.HEAD);
        hairBack = head.getChild("hair_back");
        hairBack2 = hairBack.getChild("hair_back_2");
        for (int i = 0; i < tufts.length; i++) {
            tufts[i] = head.getChild("tuft_" + i);
        }
        body = models.bakeLayer(TransformModels.BODY);
        skirtFront = body.getChild("skirt_front");
        skirtBack = body.getChild("skirt_back");
        skirtLeft = body.getChild("skirt_left");
        skirtRight = body.getChild("skirt_right");
        wings = models.bakeLayer(TransformModels.WINGS);
        wingRightUp = wings.getChild("wing_right_up");
        wingLeftUp = wings.getChild("wing_left_up");
        wingRightLow = wings.getChild("wing_right_low");
        wingLeftLow = wings.getChild("wing_left_low");
        gauntletRight = models.bakeLayer(TransformModels.gauntlet(false, slim));
        gauntletLeft = models.bakeLayer(TransformModels.gauntlet(true, slim));
    }

    private static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath("transformacion", "textures/entity/" + name + ".png");
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!ClientTransformState.isTransformed(player.getUUID()) || player.isInvisible()) {
            return;
        }
        PlayerModel<AbstractClientPlayer> model = getParentModel();
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);

        animate(model, player, limbSwingAmount, ageInTicks);

        // ---- cabeza, rostro y pelo
        pose.pushPose();
        model.head.translateAndRotate(pose);
        draw(pose, buffers, head, HEAD, HEAD_GLOW, light, overlay);
        pose.popPose();

        // ---- cuerpo: alas, corpino y falda
        pose.pushPose();
        model.body.translateAndRotate(pose);
        draw(pose, buffers, wings, WINGS, WINGS_GLOW, light, overlay);
        draw(pose, buffers, body, BODY, BODY_GLOW, light, overlay);
        pose.popPose();

        // ---- guanteletes
        renderGauntlet(pose, buffers, model.rightArm, gauntletRight, light, overlay);
        renderGauntlet(pose, buffers, model.leftArm, gauntletLeft, light, overlay);
    }

    /** Dibuja el modelo y despues su capa emisiva (venas, gemas, ojos) a brillo maximo. */
    private static void draw(PoseStack pose, MultiBufferSource buffers, ModelPart part, ResourceLocation diffuse,
                             ResourceLocation glow, int light, int overlay) {
        part.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(diffuse)), light, overlay);
        part.render(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(glow)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    private void renderGauntlet(PoseStack pose, MultiBufferSource buffers, ModelPart arm, ModelPart gauntlet, int light, int overlay) {
        pose.pushPose();
        arm.translateAndRotate(pose);
        draw(pose, buffers, gauntlet, GAUNTLET, GAUNTLET_GLOW, light, overlay);
        pose.popPose();
    }

    /** Version para la mano en primera persona (el brazo llega sin rotar). */
    public void renderFirstPerson(PoseStack pose, MultiBufferSource buffers, int light, ModelPart arm, boolean left) {
        pose.pushPose();
        pose.translate(arm.x / 16.0F, arm.y / 16.0F, arm.z / 16.0F);
        draw(pose, buffers, left ? gauntletLeft : gauntletRight, GAUNTLET, GAUNTLET_GLOW, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    private void animate(PlayerModel<AbstractClientPlayer> model, AbstractClientPlayer player, float limbSwingAmount, float age) {
        boolean airborne = !player.onGround() && !player.isInWater();
        boolean falling = airborne && player.getDeltaMovement().y < -0.1;

        // ---- alas: aleteo suave, mas amplio en el aire
        float speed = airborne ? 0.32F : 0.11F;
        float amp = falling ? 0.07F : (airborne ? 0.34F : 0.075F);
        float flap = Mth.sin(age * speed) * amp;
        float lag = Mth.sin(age * speed - 0.9F) * amp * 0.8F;
        float spread = airborne ? 0.12F : 0.0F;
        float sneak = model.crouching ? 0.12F : 0.0F;
        wingRightUp.yRot = 0.38F - spread + flap;
        wingLeftUp.yRot = -wingRightUp.yRot;
        wingRightUp.zRot = 0.14F + sneak + flap * 0.45F;
        wingLeftUp.zRot = -wingRightUp.zRot;
        wingRightLow.yRot = 0.26F - spread * 0.7F + lag;
        wingLeftLow.yRot = -wingRightLow.yRot;
        wingRightLow.zRot = -0.10F + lag * 0.5F;
        wingLeftLow.zRot = -wingRightLow.zRot;

        // ---- falda: se abre con las piernas
        float r = model.rightLeg.xRot;
        float l = model.leftLeg.xRot;
        float fwd = Math.min(0.0F, Math.min(r, l));
        float bwd = Math.max(0.0F, Math.max(r, l));
        float sway = Mth.sin(age * 0.07F) * 0.015F;
        skirtFront.xRot = -0.13F + Mth.clamp(fwd * 0.85F, -1.25F, 0.0F) + sway;
        skirtBack.xRot = 0.13F + Mth.clamp(bwd * 0.85F, 0.0F, 1.1F) - sway;
        float side = 0.13F + Math.min(0.35F, (Math.abs(r) + Math.abs(l)) * 0.12F);
        skirtRight.zRot = side;
        skirtLeft.zRot = -side;

        // ---- pelo largo: se mece al andar y al respirar
        float hairSwing = limbSwingAmount * 0.35F + Mth.sin(age * 0.06F) * 0.025F;
        hairBack.xRot = 0.10F + hairSwing * 0.55F + (model.crouching ? 0.25F : 0.0F);
        hairBack2.xRot = -0.14F + hairSwing * 0.8F + Mth.sin(age * 0.09F + 1.2F) * 0.03F;
        for (int i = 0; i < tufts.length; i++) {
            tufts[i].xRot = tufts[i].xRot; // base fija; el aleteo lo da la cabeza
        }
    }
}
