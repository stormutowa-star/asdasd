package com.keinloree.client.render;

import com.keinloree.client.model.gen.FighterModel;
import com.keinloree.ship.FighterEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** Caza del Nexo: cabina visible por dentro, cristal verde, alabeo segun el giro y piloto IA visible. */
public class FighterRenderer extends EntityRenderer<FighterEntity> {
    /** Altura del centro de la cabina (bloques): el cabeceo gira alrededor de ella para no mover el ojo del piloto. */
    private static final float CENTER = 1.806F;
    private final FighterModel<Entity> model;

    public FighterRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new FighterModel<>(ctx.bakeLayer(FighterModel.LAYER));
        this.shadowRadius = 1.2F;
    }

    @Override
    public void render(FighterEntity e, float entityYaw, float pt, PoseStack ps, MultiBufferSource buffers, int light) {
        float yaw = Mth.rotLerp(pt, e.yRotO, e.getYRot());
        float pitch = Mth.lerp(pt, e.xRotO, e.getXRot());
        float roll = Mth.clamp(Mth.wrapDegrees(e.getYRot() - e.yRotO) * -6.0F, -35.0F, 35.0F);
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        ps.translate(0.0F, CENTER, 0.0F);
        ps.mulPose(Axis.XP.rotationDegrees(-pitch));
        ps.mulPose(Axis.ZP.rotationDegrees(roll));
        ps.translate(0.0F, -CENTER, 0.0F);
        ps.scale(-1.0F, -1.0F, 1.0F);
        ps.translate(0.0F, -1.501F, 0.0F);
        model.resetPose();
        model.pilot.visible = e.hasNpcPilot();
        float hurt = e.health() / FighterEntity.MAX_HEALTH;
        int tint = hurt < 0.35F ? 0xFF9A8A8A : -1;
        model.renderToBuffer(ps, buffers.getBuffer(RenderType.entityCutoutNoCull(FighterModel.TEXTURE)), light,
                OverlayTexture.NO_OVERLAY, tint);
        model.renderToBuffer(ps, buffers.getBuffer(RenderType.eyes(FighterModel.GLOW_TEXTURE)), light,
                OverlayTexture.NO_OVERLAY, -1);
        glass(ps, buffers);
        ps.popPose();
        super.render(e, entityYaw, pt, ps, buffers, light);
    }

    /** Cristal translucido octogonal en el plano del hueso window. */
    private void glass(PoseStack ps, MultiBufferSource buffers) {
        ps.pushPose();
        model.root().translateAndRotate(ps);
        model.rootBone.translateAndRotate(ps);
        model.cockpit.translateAndRotate(ps);
        model.window.translateAndRotate(ps);
        float a = 7.2F / 16.0F;
        float s = a * (float) Math.tan(Math.toRadians(22.5));
        PoseStack.Pose pose = ps.last();
        VertexConsumer vc = buffers.getBuffer(RenderType.debugQuads());
        float[][] quads = {
            {-s, a, s, a, a, s, -a, s},
            {-a, s, a, s, a, -s, -a, -s},
            {-a, -s, a, -s, s, -a, -s, -a},
        };
        for (float[] q : quads) {
            for (int i = 0; i < 8; i += 2) {
                vc.addVertex(pose, q[i], q[i + 1], 0.0F).setColor(60, 220, 120, 60);
            }
        }
        ps.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(FighterEntity e) {
        return FighterModel.TEXTURE;
    }
}
