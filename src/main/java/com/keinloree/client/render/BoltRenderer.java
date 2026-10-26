package com.keinloree.client.render;

import com.keinloree.client.vfx.ModRenderTypes;
import com.keinloree.client.vfx.VfxEffect;
import com.keinloree.ship.BoltEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/** Rayo de energia: dos cintas cruzadas aditivas (resplandor de color y nucleo blanco) orientadas a la velocidad. */
public class BoltRenderer extends EntityRenderer<BoltEntity> {
    public BoltRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(BoltEntity e, float entityYaw, float pt, PoseStack ps, MultiBufferSource buffers, int light) {
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(-e.getYRot()));
        ps.mulPose(Axis.XP.rotationDegrees(e.getXRot()));
        float s = e.getSize();
        float len = 2.2F * s + 0.8F;
        int c = e.getColor();
        float r = ((c >> 16) & 255) / 255.0F;
        float g = ((c >> 8) & 255) / 255.0F;
        float b = (c & 255) / 255.0F;
        Matrix4f m = ps.last().pose();
        VertexConsumer glow = buffers.getBuffer(ModRenderTypes.glowAdditive(VfxEffect.GLOW));
        ribbons(glow, m, 0.22F * s, len, r, g, b, 1.0F);
        ribbons(glow, m, 0.07F * s, len * 0.9F, 1.0F, 1.0F, 1.0F, 1.0F);
        ps.popPose();
    }

    /** Dos cintas (vertical y horizontal) a lo largo de +Z desde -len hasta 0. */
    private static void ribbons(VertexConsumer vc, Matrix4f m, float w, float len, float r, float g, float b, float a) {
        quad(vc, m, -w, 0, w, 0, len, r, g, b, a);
        quad(vc, m, 0, -w, 0, w, len, r, g, b, a);
    }

    private static void quad(VertexConsumer vc, Matrix4f m, float x0, float y0, float x1, float y1, float len,
                             float r, float g, float b, float a) {
        vc.addVertex(m, x0, y0, -len).setUv(0.0F, 0.5F).setColor(r, g, b, a);
        vc.addVertex(m, x1, y1, -len).setUv(1.0F, 0.5F).setColor(r, g, b, a);
        vc.addVertex(m, x1, y1, len * 0.15F).setUv(1.0F, 0.5F).setColor(r, g, b, a);
        vc.addVertex(m, x0, y0, len * 0.15F).setUv(0.0F, 0.5F).setColor(r, g, b, a);
    }

    @Override
    public ResourceLocation getTextureLocation(BoltEntity e) {
        return TextureAtlas.LOCATION_PARTICLES;
    }
}
