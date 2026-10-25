package com.keinloree.client;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.Droid;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renderer del droide: armadura verde oscura con visor, núcleo y cañón luminosos (más intensos al apuntar). */
public class DroidRenderer extends MobRenderer<Droid, DroidModel<Droid>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "textures/entity/droid.png");
    private static final ResourceLocation GLOW =
            ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "textures/entity/droid_glow.png");

    public DroidRenderer(EntityRendererProvider.Context context) {
        super(context, new DroidModel<Droid>(context.bakeLayer(DroidModel.LAYER)), 0.4F);
        this.addLayer(new GlowLayer<Droid, DroidModel<Droid>>(this, e -> GLOW, DroidRenderer::pulse));
    }

    @Override
    public ResourceLocation getTextureLocation(Droid entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(Droid entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.88F, 0.88F, 0.88F);
    }

    /** Latido suave de los sistemas; a plena intensidad cuando tiene un objetivo. */
    private static double pulse(Droid entity, Float partialTick) {
        if (entity.getTarget() != null) {
            return 1.0D;
        }
        return 0.74D + 0.18D * Math.sin((entity.tickCount + partialTick) * 0.12D);
    }
}
