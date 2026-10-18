package com.keinloree.client;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.LooreVisual;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/** Renderer de Kein Loree y de sus comandantes (misma geometria; cambia la textura segun el tipo). */
public class LooreRenderer<T extends Mob & LooreVisual> extends MobRenderer<T, KeinLooreModel<T>> {

    public LooreRenderer(EntityRendererProvider.Context context) {
        super(context, new KeinLooreModel<T>(context.bakeLayer(KeinLooreModel.LAYER)), 0.5F);
        this.addLayer(new GlowLayer<T, KeinLooreModel<T>>(this, e -> texture(e.getLoreSkin(), true)));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture(entity.getLoreSkin(), false);
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTickTime) {
        float s = entity.getLoreSkin() == 0 ? 1.5F : 1.25F;
        poseStack.scale(s, s, s);
    }

    private static ResourceLocation texture(int skin, boolean glow) {
        String base = skin == 2 ? "loree_warlord" : skin == 1 ? "loree_commander" : "kein_loree";
        return ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID,
                "textures/entity/" + base + (glow ? "_glow" : "") + ".png");
    }
}
