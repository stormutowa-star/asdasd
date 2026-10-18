package com.keinloree.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import java.util.function.Function;

/** Capa de brillo: vuelve a dibujar el modelo con su textura luminosa (los ojos, el codigo y el visor). */
public class GlowLayer<T extends Mob, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private final Function<T, ResourceLocation> texture;

    public GlowLayer(RenderLayerParent<T, M> parent, Function<T, ResourceLocation> texture) {
        super(parent);
        this.texture = texture;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(this.texture.apply(entity)));
        this.getParentModel().renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, -1);
    }
}
