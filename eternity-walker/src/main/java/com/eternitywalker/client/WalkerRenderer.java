package com.eternitywalker.client;

import com.eternitywalker.EternityWalkerMod;
import com.eternitywalker.WalkerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class WalkerRenderer extends EntityRenderer<WalkerEntity> {
    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath(EternityWalkerMod.MODID, "textures/entity/eternity_walker.png");
    private static final float[] RECOIL = {0.9f, 1.0f, 0.8f, 0.55f, 0.35f, 0.2f, 0.1f, 0.04f, 0f};

    public WalkerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(WalkerEntity e) {
        return TEX;
    }

    private static float recoil(float age, float amp) {
        if (age < 0 || age >= RECOIL.length - 1) return 0f;
        int i = (int) age;
        float t = age - i;
        return amp * Mth.lerp(t, RECOIL[i], RECOIL[i + 1]);
    }

    @Override
    public void render(WalkerEntity e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
        WalkerModel.load();
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        int l = LightTexture.pack(Math.max(LightTexture.block(light), 8), Math.max(LightTexture.sky(light), 5));

        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(90.0f - yaw));
        ps.scale(0.5f, 0.5f, 0.5f);

        State s = new State();
        float phase = Mth.lerp(pt, e.walkPhaseO, e.walkPhase);
        s.phase = phase - (float) Math.floor(phase);
        s.amt = Mth.lerp(pt, e.moveAmtO, e.moveAmt);
        s.turretYaw = e.getEntityData().get(WalkerEntity.TURRET_YAW);
        s.mainPitch = e.getEntityData().get(WalkerEntity.TURRET_PITCH);
        s.sideYaw = e.getEntityData().get(WalkerEntity.SIDE_YAW);
        s.sidePitch = e.getEntityData().get(WalkerEntity.SIDE_PITCH);
        s.recoilMain = recoil(e.tickCount - e.getEntityData().get(WalkerEntity.SHOT_MAIN) + pt, 1.77f);
        s.recoilL = recoil(e.tickCount - e.getEntityData().get(WalkerEntity.SHOT_L) + pt, 0.9f);
        s.recoilR = recoil(e.tickCount - e.getEntityData().get(WalkerEntity.SHOT_R) + pt, 0.9f);
        s.sensor = 51.0f * Mth.sin((e.tickCount + pt) * 0.03927f);

        for (int r : WalkerModel.roots) drawBone(r, ps, vc, l, s);
        ps.popPose();
    }

    private static final class State {
        float phase, amt, turretYaw, mainPitch, sideYaw, sidePitch, recoilMain, recoilL, recoilR, sensor;
    }

    private void drawBone(int idx, PoseStack ps, VertexConsumer vc, int light, State s) {
        WalkerModel.Bone b = WalkerModel.bones[idx];
        ps.pushPose();

        float ry = 0, rz = 0, off = 0;
        switch (b.kind) {
            case WalkerModel.LEG -> rz = WalkerModel.sample(b.walk, s.phase) * s.amt;
            case WalkerModel.TURRET_YAW -> ry = s.turretYaw;
            case WalkerModel.MAIN_PITCH -> rz = s.mainPitch;
            case WalkerModel.SIDE_YAW -> ry = s.sideYaw;
            case WalkerModel.SIDE_PITCH -> rz = s.sidePitch;
            case WalkerModel.RECOIL_MAIN -> off = s.recoilMain;
            case WalkerModel.RECOIL_L -> off = s.recoilL;
            case WalkerModel.RECOIL_R -> off = s.recoilR;
            case WalkerModel.SENSOR -> ry = s.sensor;
            default -> { }
        }
        if (ry != 0 || rz != 0) {
            ps.translate(b.px, b.py, b.pz);
            if (ry != 0) ps.mulPose(Axis.YP.rotationDegrees(ry));
            if (rz != 0) ps.mulPose(Axis.ZP.rotationDegrees(rz));
            ps.translate(-b.px, -b.py, -b.pz);
        }
        if (off != 0) ps.translate(off, 0, 0);

        PoseStack.Pose pose = ps.last();
        float[] q = b.q;
        for (int i = 0; i + 22 < q.length; i += 23) {
            float nx = q[i + 20], ny = q[i + 21], nz = q[i + 22];
            for (int v = 0; v < 4; v++) {
                int o = i + v * 5;
                vc.addVertex(pose, q[o], q[o + 1], q[o + 2])
                        .setColor(255, 255, 255, 255)
                        .setUv(q[o + 3], q[o + 4])
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(light)
                        .setNormal(pose, nx, ny, nz);
            }
        }
        for (int c : b.children) drawBone(c, ps, vc, light, s);
        ps.popPose();
    }
}
