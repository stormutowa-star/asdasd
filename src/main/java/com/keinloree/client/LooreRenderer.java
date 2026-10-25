package com.keinloree.client;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.LooreVisual;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * Renderer de Kein Loree y de su facción (misma geometría; cambia la textura según el tipo).
 * Kein Loree tiene una segunda capa luminosa (ribetes, puños, colgante) que se enciende al despertar su forma final.
 */
public class LooreRenderer<T extends Mob & LooreVisual> extends MobRenderer<T, KeinLooreModel<T>> {

    private static final ResourceLocation KEIN_AWAKE =
            ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "textures/entity/kein_loree_glow2.png");

    public LooreRenderer(EntityRendererProvider.Context context) {
        super(context, new KeinLooreModel<T>(context.bakeLayer(KeinLooreModel.LAYER)), 0.55F);
        this.addLayer(new GlowLayer<T, KeinLooreModel<T>>(this, e -> texture(e.getLoreSkin(), true)));
        this.addLayer(new GlowLayer<T, KeinLooreModel<T>>(this, e -> KEIN_AWAKE, LooreRenderer::awakening));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture(entity.getLoreSkin(), false);
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTickTime) {
        int skin = entity.getLoreSkin();
        float s = skin == 0 ? 1.3F : skin == 1 ? 1.2F : 1.25F;
        poseStack.scale(s, s, s);
    }

    /** Intensidad de los ribetes luminosos de Kein Loree: apagados en la forma base, encendidos con las alas y en los ataques. */
    private static <E extends Mob & LooreVisual> double awakening(E entity, Float partialTick) {
        if (entity.getLoreSkin() != 0 || entity.isLoreDormant()) {
            return 0.0D;
        }
        double t = entity.tickCount + partialTick;
        double k = 0.0D;
        if (entity.getLoreForm() == 1) {
            k = 0.5D + 0.5D * Mth.clamp(entity.getWingOpen(), 0.0F, 1.0F);
        }
        int pose = entity.getLorePose();
        if (pose == LooreVisual.POSE_CHARGE || pose == LooreVisual.POSE_FIRE || pose == LooreVisual.POSE_SUMMON
                || pose == LooreVisual.POSE_CAST) {
            k = Math.max(k, 0.65D + 0.35D * Math.sin(t * 0.6D));
        }
        if (k > 0.0D) {
            k *= 0.92D + 0.08D * Math.sin(t * 0.21D);
        }
        return k;
    }

    private static ResourceLocation texture(int skin, boolean glow) {
        String base = skin == 2 ? "loree_warlord" : skin == 1 ? "loree_commander" : "kein_loree";
        return ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID,
                "textures/entity/" + base + (glow ? "_glow" : "") + ".png");
    }
}
