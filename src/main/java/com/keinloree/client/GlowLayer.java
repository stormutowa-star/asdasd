package com.keinloree.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;

/**
 * Capa de brillo: vuelve a dibujar el modelo con su textura luminosa (los ojos, el código y el visor).
 * La intensidad (0..1) puede variar con el estado de la entidad, por ejemplo para encender los ribetes al despertar.
 */
public class GlowLayer<T extends Mob, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private final Function<T, ResourceLocation> texture;
    private final ToDoubleBiFunction<T, Float> intensity;

    public GlowLayer(RenderLayerParent<T, M> parent, Function<T, ResourceLocation> texture) {
        this(parent, texture, (entity, partialTick) -> 1.0D);
    }

    public GlowLayer(RenderLayerParent<T, M> parent, Function<T, ResourceLocation> texture,
                     ToDoubleBiFunction<T, Float> intensity) {
        super(parent);
        this.texture = texture;
        this.intensity = intensity;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        float k = (float) Mth.clamp(this.intensity.applyAsDouble(entity, partialTick), 0.0D, 1.0D);
        if (k <= 0.01F) {
            return;
        }
        ResourceLocation tex = this.texture.apply(entity);
        if (tex == null) {
            return;
        }
        int c = Mth.clamp(Math.round(k * 255.0F), 0, 255);
        int color = 0xFF000000 | (c << 16) | (c << 8) | c;
        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(tex));
        this.getParentModel().renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, color);
    }
}
