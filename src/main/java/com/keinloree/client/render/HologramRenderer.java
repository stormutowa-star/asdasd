package com.keinloree.client.render;

import com.keinloree.client.KeinLooreModel;
import com.keinloree.entity.KeinHologramEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Holograma de Kein: el modelo de Kein a escala gigante, translucido y verde, con parpadeo, franjas que suben y
 * desplazamientos de fallo de senal.
 */
public class HologramRenderer extends EntityRenderer<KeinHologramEntity> {
    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/kein_loree.png");
    private final KeinLooreModel<KeinHologramEntity> model;

    public HologramRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new KeinLooreModel<>(ctx.bakeLayer(KeinLooreModel.LAYER));
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(KeinHologramEntity e, float yaw, float pt, PoseStack ps, MultiBufferSource buffers, int light) {
        float age = e.tickCount + pt;
        float s = e.scale();
        float fade = e.fade() > 0 ? Mth.clamp(1.0F - (e.fade() + pt) / 40.0F, 0.0F, 1.0F) : Math.min(1.0F, age / 30.0F);
        // parpadeo y fallos de senal
        float flicker = 0.75F + 0.25F * Mth.sin(age * 1.7F) * Mth.sin(age * 0.31F);
        boolean glitch = ((int) (age / 3.0F) * 2654435761L & 0xFF) < 18;
        float alpha = Mth.clamp(0.55F * flicker * fade, 0.0F, 1.0F);
        ps.pushPose();
        if (glitch) {
            ps.translate((Math.sin(age * 13.0F) * 0.04F) * s, 0.0F, 0.0F);
        }
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(pt, e.yRotO, e.getYRot())));
        ps.scale(-s, -s, s);
        ps.translate(0.0F, -1.501F, 0.0F);
        model.setupAnim(e, 0.0F, 0.0F, age, 0.0F, 0.0F);
        int a = (int) (alpha * 255.0F);
        int color = (a << 24) | 0x46FF7C;
        model.renderToBuffer(ps, buffers.getBuffer(RenderType.entityTranslucentEmissive(TEX)), LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, color);
        // segunda pasada desplazada: lineas de escaneo
        float band = (age * 0.04F) % 1.0F;
        ps.translate(0.0F, -band * 0.08F, 0.0F);
        model.renderToBuffer(ps, buffers.getBuffer(RenderType.entityTranslucentEmissive(TEX)), LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, ((int) (alpha * 90.0F) << 24) | 0xB0FFC8);
        ps.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(KeinHologramEntity e) {
        return TEX;
    }
}
