package com.keinloree.client.render;

import com.keinloree.client.model.gen.*;
import com.keinloree.entity.CalendarioEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/** Los Cinco Calendarios: un modelo de Blockbench por Hora, con sus animaciones y su capa emisiva. */
public class HeroRenderer extends MobRenderer<CalendarioEntity, AnimatedModel<CalendarioEntity>> {
    private final AnimatedModel<CalendarioEntity>[] models;

    @SuppressWarnings("unchecked")
    public HeroRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, null, 0.5F);
        AlbaModel<Entity> alba = new AlbaModel<>(ctx.bakeLayer(AlbaModel.LAYER));
        CenitModel<Entity> cenit = new CenitModel<>(ctx.bakeLayer(CenitModel.LAYER));
        OcasoModel<Entity> ocaso = new OcasoModel<>(ctx.bakeLayer(OcasoModel.LAYER));
        MedianocheModel<Entity> medianoche = new MedianocheModel<>(ctx.bakeLayer(MedianocheModel.LAYER));
        EclipseModel<Entity> eclipse = new EclipseModel<>(ctx.bakeLayer(EclipseModel.LAYER));
        models = new AnimatedModel[]{
            new AnimatedModel<>(alba, alba.head, new AnimSet(AlbaModel.TEXTURE, AlbaModel.GLOW_TEXTURE, AlbaAnims.IDLE, AlbaAnims.WALK, AlbaAnims.RUN)
                    .humanoid(AlbaAnims.ATTACK_1, AlbaAnims.ATTACK_2, AlbaAnims.ATTACK_3, AlbaAnims.SPECIAL_1, AlbaAnims.SPECIAL_2,
                            AlbaAnims.CAST, AlbaAnims.POSE, AlbaAnims.KNEEL, AlbaAnims.HURT)),
            new AnimatedModel<>(cenit, cenit.head, new AnimSet(CenitModel.TEXTURE, CenitModel.GLOW_TEXTURE, CenitAnims.IDLE, CenitAnims.WALK, CenitAnims.RUN)
                    .humanoid(CenitAnims.ATTACK_1, CenitAnims.ATTACK_2, CenitAnims.ATTACK_3, CenitAnims.SPECIAL_1, CenitAnims.SPECIAL_2,
                            CenitAnims.CAST, CenitAnims.POSE, CenitAnims.KNEEL, CenitAnims.HURT)),
            new AnimatedModel<>(ocaso, ocaso.head, new AnimSet(OcasoModel.TEXTURE, OcasoModel.GLOW_TEXTURE, OcasoAnims.IDLE, OcasoAnims.WALK, OcasoAnims.RUN)
                    .humanoid(OcasoAnims.ATTACK_1, OcasoAnims.ATTACK_2, OcasoAnims.ATTACK_3, OcasoAnims.SPECIAL_1, OcasoAnims.SPECIAL_2,
                            OcasoAnims.CAST, OcasoAnims.POSE, OcasoAnims.KNEEL, OcasoAnims.HURT)),
            new AnimatedModel<>(medianoche, medianoche.head, new AnimSet(MedianocheModel.TEXTURE, MedianocheModel.GLOW_TEXTURE, MedianocheAnims.FLOAT,
                    MedianocheAnims.FLOAT, MedianocheAnims.FLOAT)
                    .humanoid(MedianocheAnims.ATTACK_1, MedianocheAnims.ATTACK_2, MedianocheAnims.ATTACK_3, MedianocheAnims.SPECIAL_1,
                            MedianocheAnims.SPECIAL_2, MedianocheAnims.CAST, MedianocheAnims.POSE, MedianocheAnims.KNEEL, MedianocheAnims.HURT)),
            new AnimatedModel<>(eclipse, eclipse.head, new AnimSet(EclipseModel.TEXTURE, EclipseModel.GLOW_TEXTURE, EclipseAnims.IDLE, EclipseAnims.WALK, EclipseAnims.RUN)
                    .humanoid(EclipseAnims.ATTACK_1, EclipseAnims.ATTACK_2, EclipseAnims.ATTACK_3, EclipseAnims.SPECIAL_1, EclipseAnims.SPECIAL_2,
                            EclipseAnims.CAST, EclipseAnims.POSE, EclipseAnims.KNEEL, EclipseAnims.HURT)),
        };
        this.model = models[0];
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void render(PoseStack ps, MultiBufferSource buffers, int light, CalendarioEntity e, float ls, float la,
                               float pt, float age, float hy, float hp) {
                if (e.getMode() == CalendarioEntity.MODE_FROZEN) {
                    return;
                }
                getParentModel().renderToBuffer(ps, buffers.getBuffer(RenderType.eyes(getParentModel().set().glow)),
                        LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
            }
        });
    }

    @Override
    public void render(CalendarioEntity e, float yaw, float pt, PoseStack ps, MultiBufferSource buffers, int light) {
        this.model = models[Math.floorMod(e.hour(), 5)];
        super.render(e, yaw, pt, ps, buffers, light);
    }

    @Override
    protected int getBlockLightLevel(CalendarioEntity e, net.minecraft.core.BlockPos pos) {
        return Math.max(9, super.getBlockLightLevel(e, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(CalendarioEntity e) {
        return models[Math.floorMod(e.hour(), 5)].set().texture;
    }
}
