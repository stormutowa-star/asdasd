package com.keinloree.client.render;

import com.keinloree.client.model.gen.TurbolaserDefenseModel;
import com.keinloree.client.model.gen.TurbolaserHeavyModel;
import com.keinloree.ship.TurbolaserEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** Torretas turbolaser: la casa gira con la punteria, la cuna cabecea y cada canon retrocede al disparar. */
public class TurretRenderer extends EntityRenderer<TurbolaserEntity> {
    private final TurbolaserHeavyModel<Entity> heavy;
    private final TurbolaserDefenseModel<Entity> defense;

    public TurretRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.heavy = new TurbolaserHeavyModel<>(ctx.bakeLayer(TurbolaserHeavyModel.LAYER));
        this.defense = new TurbolaserDefenseModel<>(ctx.bakeLayer(TurbolaserDefenseModel.LAYER));
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(TurbolaserEntity e, float entityYaw, float pt, PoseStack ps, MultiBufferSource buffers, int light) {
        float yaw = Mth.rotLerp(pt, e.aimYawO, e.aimYaw);
        float pitch = Mth.lerp(pt, e.aimPitchO, e.aimPitch);
        ps.pushPose();
        if (e.isInverted()) {
            ps.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - (e.isInverted() ? -yaw : yaw)));
        ps.scale(-1.0F, -1.0F, 1.0F);
        ps.translate(0.0F, -1.501F, 0.0F);
        float p = (float) Math.toRadians(e.isInverted() ? -pitch : pitch);
        boolean dead = e.isDestroyed();
        ResourceLocation tex;
        ResourceLocation glow;
        if (e.isHeavy()) {
            heavy.resetPose();
            heavy.pitch.xRot = dead ? 0.45F : p;
            recoil(heavy.barrel0, e.recoil[0]);
            recoil(heavy.barrel1, e.recoil[1]);
            heavy.renderToBuffer(ps, buffers.getBuffer(RenderType.entityCutoutNoCull(TurbolaserHeavyModel.TEXTURE)), light,
                    OverlayTexture.NO_OVERLAY, dead ? 0xFF555555 : -1);
            if (!dead) {
                heavy.renderToBuffer(ps, buffers.getBuffer(RenderType.eyes(TurbolaserHeavyModel.GLOW_TEXTURE)), light,
                        OverlayTexture.NO_OVERLAY, -1);
            }
        } else {
            defense.resetPose();
            defense.pitch.xRot = dead ? 0.45F : p;
            recoil(defense.barrel0, e.recoil[0]);
            recoil(defense.barrel1, e.recoil[1]);
            recoil(defense.barrel2, e.recoil[2]);
            recoil(defense.barrel3, e.recoil[3]);
            defense.renderToBuffer(ps, buffers.getBuffer(RenderType.entityCutoutNoCull(TurbolaserDefenseModel.TEXTURE)), light,
                    OverlayTexture.NO_OVERLAY, dead ? 0xFF555555 : -1);
            if (!dead) {
                defense.renderToBuffer(ps, buffers.getBuffer(RenderType.eyes(TurbolaserDefenseModel.GLOW_TEXTURE)), light,
                        OverlayTexture.NO_OVERLAY, -1);
            }
        }
        ps.popPose();
        super.render(e, entityYaw, pt, ps, buffers, light);
    }

    private static void recoil(ModelPart barrel, float r) {
        barrel.z += r * r * 3.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(TurbolaserEntity e) {
        return e.isHeavy() ? TurbolaserHeavyModel.TEXTURE : TurbolaserDefenseModel.TEXTURE;
    }
}
