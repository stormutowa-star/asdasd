package com.storm.greatsword;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GreatswordRenderer extends GeoItemRenderer<GreatswordItem> {
    public GreatswordRenderer() {
        super(new GreatswordModel());
        useAlternateGuiLighting();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        // The sheathed sword lives on the player's back, not in their hand. GUI/ground displays still
        // show it normally. FIXED is used by the back layer, so it must always render there.
        boolean handContext = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        if (handContext && !GreatswordItem.isDrawn(stack)) {
            return;
        }

        super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
