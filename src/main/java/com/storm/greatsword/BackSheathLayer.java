package com.storm.greatsword;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Capa que dibuja la gran espada cruzada en la ESPALDA del jugador mientras esta enfundada.
// Cuando se desenfundana (drawn), esta capa no pinta nada y el arma pasa a la mano.
@OnlyIn(Dist.CLIENT)
public class BackSheathLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private final ItemRenderer itemRenderer;

    public BackSheathLayer(net.minecraft.client.renderer.entity.RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                           ItemRenderer itemRenderer) {
        super(parent);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GreatswordItem)) {
            return;
        }
        // Solo se muestra en la espalda si esta ENFUNDADA.
        if (GreatswordItem.isDrawn(stack)) {
            return;
        }

        poseStack.pushPose();

        // Lleva el origen al centro de la espala del torso.
        // El cuerpo del modelo esta cerca de y=+12..24 (parte alta del pecho/espalda).
        boolean leftHanded = player.getMainArm() == HumanoidArm.LEFT;
        float side = leftHanded ? 1.0F : -1.0F;

        // Traslada a la espalda (z negativa = detras del cuerpo).
        poseStack.translate(0.0D, 14.0D / 16.0D, -0.35D);

        // Rota para que quede en diagonal cruzada (como una espada a la espalda).
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(45.0F * side));

        // Escala para que se vea en proporcion al cuerpo.
        float scale = 1.15F;
        poseStack.scale(scale, scale, scale);

        BakedModel model = this.itemRenderer.getModel(stack, player.level(), player, 0);
        this.itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack,
                buffer, packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, model);

        poseStack.popPose();
    }
}
