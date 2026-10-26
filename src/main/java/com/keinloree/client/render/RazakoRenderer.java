package com.keinloree.client.render;

import com.keinloree.client.model.gen.RazakoAnims;
import com.keinloree.client.model.gen.RazakoModel;
import com.keinloree.entity.Animated;
import com.keinloree.entity.RazakoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/** Razako (y su eco verde translucido). */
public class RazakoRenderer extends MobRenderer<RazakoEntity, AnimatedModel<RazakoEntity>> {
    public RazakoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, null, 0.9F);
        RazakoModel<Entity> m = new RazakoModel<>(ctx.bakeLayer(RazakoModel.LAYER));
        AnimSet set = new AnimSet(RazakoModel.TEXTURE, RazakoModel.GLOW_TEXTURE, RazakoAnims.IDLE, RazakoAnims.WALK, RazakoAnims.RUN)
                .on(Animated.A_ATTACK_1, RazakoAnims.ATTACK_1).on(Animated.A_ATTACK_2, RazakoAnims.ATTACK_2)
                .on(Animated.A_ATTACK_3, RazakoAnims.ATTACK_3).on(Animated.A_CHARGE, RazakoAnims.CHARGE)
                .on(Animated.A_LEAP, RazakoAnims.LEAP).on(Animated.A_ROAR, RazakoAnims.ROAR).on(Animated.A_BLOCK, RazakoAnims.BLOCK)
                .on(Animated.A_HORIZON, RazakoAnims.HORIZON).on(Animated.A_CHAIN, RazakoAnims.CHAIN)
                .on(Animated.A_KNEEL, RazakoAnims.KNEEL).on(Animated.A_HURT, RazakoAnims.HURT).on(Animated.A_POSE, RazakoAnims.POSE);
        this.model = new AnimatedModel<>(m, m.head, set);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void render(PoseStack ps, MultiBufferSource buffers, int light, RazakoEntity e, float ls, float la,
                               float pt, float age, float hy, float hp) {
                getParentModel().renderToBuffer(ps, buffers.getBuffer(RenderType.eyes(RazakoModel.GLOW_TEXTURE)),
                        LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
            }
        });
    }

    @Override
    protected RenderType getRenderType(RazakoEntity e, boolean visible, boolean translucent, boolean glowing) {
        if (e.isEcho()) {
            return RenderType.entityTranslucentEmissive(RazakoModel.TEXTURE);
        }
        return super.getRenderType(e, visible, translucent, glowing);
    }

    @Override
    public void render(RazakoEntity e, float yaw, float pt, PoseStack ps, MultiBufferSource buffers, int light) {
        super.render(e, yaw, pt, ps, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(RazakoEntity e) {
        return RazakoModel.TEXTURE;
    }
}
