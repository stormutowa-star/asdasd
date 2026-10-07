package com.ricardo.blueeyes.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import com.ricardo.blueeyes.BlueEyesDragon;
import com.ricardo.blueeyes.BlueEyesMod;

public class BlueEyesRenderer extends MobRenderer<BlueEyesDragon, BlueEyesModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(BlueEyesMod.MODID, "textures/entity/blue_eyes_white_dragon.png");
    private static final RenderType EYES =
            RenderType.eyes(ResourceLocation.fromNamespaceAndPath(BlueEyesMod.MODID, "textures/entity/blue_eyes_white_dragon_eyes.png"));

    public BlueEyesRenderer(EntityRendererProvider.Context context) {
        super(context, new BlueEyesModel(context.bakeLayer(BlueEyesModel.LAYER)), 1.8F);
        this.addLayer(new EyesLayer<BlueEyesDragon, BlueEyesModel>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(BlueEyesDragon dragon) {
        return TEXTURE;
    }

    @Override
    protected void scale(BlueEyesDragon dragon, PoseStack poseStack, float partialTick) {
        poseStack.scale(BlueEyesDragon.MODEL_SCALE, BlueEyesDragon.MODEL_SCALE, BlueEyesDragon.MODEL_SCALE);
    }

    @Override
    public boolean shouldRender(BlueEyesDragon dragon, Frustum frustum, double camX, double camY, double camZ) {
        return super.shouldRender(dragon, frustum, camX, camY, camZ) || dragon.getFireTicks() > 0;
    }

    @Override
    public void render(BlueEyesDragon dragon, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(dragon, entityYaw, partialTick, poseStack, buffer, packedLight);

        int fire = dragon.getFireTicks();
        int charge = dragon.getCharge();
        if (fire <= 0 && charge <= 0) {
            return;
        }
        Vec3 base = new Vec3(Mth.lerp(partialTick, dragon.xo, dragon.getX()), Mth.lerp(partialTick, dragon.yo, dragon.getY()),
                Mth.lerp(partialTick, dragon.zo, dragon.getZ()));
        Vec3 mouth = dragon.getMouthPosition(partialTick).subtract(base);
        float time = dragon.tickCount + partialTick;
        VertexConsumer vc = buffer.getBuffer(RenderType.lightning());
        Matrix4f m = poseStack.last().pose();

        if (charge > 0) {
            // esfera de energia creciendo en las fauces
            float k = Mth.clamp((charge + partialTick) / BlueEyesDragon.CHARGE_TIME, 0.0F, 1.0F);
            orb(vc, m, mouth, 0.15F + k * 0.55F, time);
        }
        if (fire > 0) {
            Entity target = dragon.getBeamTarget();
            if (target != null) {
                Vec3 end = new Vec3(Mth.lerp(partialTick, target.xo, target.getX()),
                        Mth.lerp(partialTick, target.yo, target.getY()) + target.getBbHeight() * 0.5D,
                        Mth.lerp(partialTick, target.zo, target.getZ())).subtract(base);
                float fade = Mth.clamp((fire - partialTick) / BlueEyesDragon.FIRE_TIME, 0.0F, 1.0F);
                long seed = dragon.getId() * 31L + (long) (time / 2.0F);
                // halo azul, cuerpo celeste y nucleo blanco: White Lightning
                bolt(vc, m, mouth, end, seed, 0.75F * fade + 0.2F, 0.45F, 0.70F, 1.00F, 0.22F);
                bolt(vc, m, mouth, end, seed, 0.38F * fade + 0.12F, 0.75F, 0.90F, 1.00F, 0.45F);
                bolt(vc, m, mouth, end, seed, 0.14F * fade + 0.06F, 1.00F, 1.00F, 1.00F, 0.95F);
                // ramas secundarias
                bolt(vc, m, mouth, end, seed + 17L, 0.10F * fade, 0.80F, 0.92F, 1.00F, 0.6F);
                orb(vc, m, mouth, 0.6F * fade + 0.2F, time);
                orb(vc, m, end, 1.2F * fade + 0.3F, time);
            }
        }
    }

    /** Rayo zigzagueante entre a y b, dibujado como dos cintas cruzadas. */
    private static void bolt(VertexConsumer vc, Matrix4f m, Vec3 a, Vec3 b, long seed, float width, float r, float g, float bl, float alpha) {
        Vec3 d = b.subtract(a);
        double len = d.length();
        if (len < 0.1D) {
            return;
        }
        Vec3 dir = d.scale(1.0D / len);
        Vec3 up = Math.abs(dir.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 p1 = dir.cross(up).normalize();
        Vec3 p2 = dir.cross(p1).normalize();
        int segs = Math.max(6, (int) (len / 1.1D));
        RandomSource rnd = RandomSource.create(seed);
        Vec3 prev = a;
        for (int i = 1; i <= segs; i++) {
            double t = i / (double) segs;
            Vec3 next = a.add(d.scale(t));
            if (i < segs) {
                double amp = 0.55D * Math.sin(Math.PI * t);
                next = next.add(p1.scale((rnd.nextDouble() - 0.5D) * 2.0D * amp)).add(p2.scale((rnd.nextDouble() - 0.5D) * 2.0D * amp));
            }
            ribbon(vc, m, prev, next, p1.scale(width), r, g, bl, alpha);
            ribbon(vc, m, prev, next, p2.scale(width), r, g, bl, alpha);
            prev = next;
        }
    }

    private static void ribbon(VertexConsumer vc, Matrix4f m, Vec3 a, Vec3 b, Vec3 w, float r, float g, float bl, float alpha) {
        Vec3 a1 = a.add(w), a2 = a.subtract(w), b1 = b.add(w), b2 = b.subtract(w);
        quad(vc, m, a1, b1, b2, a2, r, g, bl, alpha);
        quad(vc, m, a2, b2, b1, a1, r, g, bl, alpha);
    }

    /** Destello de energia (estrella de cintas que gira). */
    private static void orb(VertexConsumer vc, Matrix4f m, Vec3 c, float size, float time) {
        for (int i = 0; i < 3; i++) {
            float ang = time * 0.25F + i * (Mth.PI / 3.0F);
            Vec3 ax = new Vec3(Mth.cos(ang), Mth.sin(ang * 0.7F) * 0.5F, Mth.sin(ang)).normalize();
            Vec3 ay = ax.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
            if (ay.lengthSqr() < 1.0E-4D) {
                ay = new Vec3(1.0D, 0.0D, 0.0D);
            }
            Vec3 a = c.subtract(ax.scale(size));
            Vec3 b = c.add(ax.scale(size));
            ribbon(vc, m, a, b, ay.scale(size * 0.35F), 0.7F, 0.9F, 1.0F, 0.35F);
            ribbon(vc, m, a, b, ay.scale(size * 0.12F), 1.0F, 1.0F, 1.0F, 0.8F);
        }
    }

    private static void quad(VertexConsumer vc, Matrix4f m, Vec3 a, Vec3 b, Vec3 c, Vec3 d, float r, float g, float bl, float alpha) {
        vc.addVertex(m, (float) a.x, (float) a.y, (float) a.z).setColor(r, g, bl, alpha);
        vc.addVertex(m, (float) b.x, (float) b.y, (float) b.z).setColor(r, g, bl, alpha);
        vc.addVertex(m, (float) c.x, (float) c.y, (float) c.z).setColor(r, g, bl, alpha);
        vc.addVertex(m, (float) d.x, (float) d.y, (float) d.z).setColor(r, g, bl, alpha);
    }
}
