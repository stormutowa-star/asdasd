package com.storm.greatsword;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

/**
 * Renders the sheathed greatsword diagonally across the player's back. The 3D model is drawn through the
 * same GeckoLib renderer as the held item, so it keeps its idle animation while it is on the back.
 */
@OnlyIn(Dist.CLIENT)
public class BackSheathLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public BackSheathLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        ItemStack stack = findSheathedGreatsword(player);
        if (stack.isEmpty()) {
            return;
        }

        var renderer = GeoRenderProvider.of(stack).getGeoItemRenderer();
        if (renderer == null) {
            return;
        }

        poseStack.pushPose();

        // Lower back. The model origin is the bottom of the grip, so the handle rests near the hip
        // while the blade rises diagonally over the opposite shoulder.
        poseStack.translate(0.0D, player.isCrouching() ? 0.56D : 0.72D, 0.19D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(42.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(7.0F));
        poseStack.scale(0.62F, 0.62F, 0.62F);

        renderer.renderByItem(stack, ItemDisplayContext.FIXED, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    private ItemStack findSheathedGreatsword(AbstractClientPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof GreatswordItem && !GreatswordItem.isDrawn(mainHand)) {
            return mainHand;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof GreatswordItem && !GreatswordItem.isDrawn(stack)) {
                return stack;
            }
        }

        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof GreatswordItem && !GreatswordItem.isDrawn(stack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}
