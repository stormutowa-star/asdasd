package com.keinloree.client.test;

import com.keinloree.client.model.gen.FighterModel;
import com.keinloree.client.model.gen.TurbolaserDefenseModel;
import com.keinloree.client.model.gen.TurbolaserHeavyModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Vista previa de las naves de Blockbench (WP4c) SIN entidades: solo con el escenario de cliente "preview_ships"
 * dibuja las torretas (varias rotaciones de yaw/pitch) y el caza (por fuera y desde dentro de la cabina).
 * Sirve tambien de referencia para los renderers reales: capa base sin culling (la cabina se ve por dentro),
 * capa emisiva con RenderType.eyes y el cristal de la ventana translucido en el plano del hueso window.
 */
@Mod.EventBusSubscriber(modid = "keinloree", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ShipPreview {
    private enum Kind { HEAVY, DEFENSE, FIGHTER }

    /** Objeto colocado relativo a la camara (bloques); yaw como el de una entidad; tYaw/tPitch en grados. */
    private record Item(Kind kind, double dx, double dy, double dz, float yaw, float tYaw, float tPitch, boolean pilot) {
    }

    // Ojo del piloto en Blockbench (0, 28.9, 3.4) px -> con yaw 0 queda en (0, 1.806, -0.2125) bloques del origen
    private static final double EYE_Y = 28.9 / 16.0;
    private static final double EYE_Z = -3.4 / 16.0;

    private static final List<Item> items = new ArrayList<>();
    private static boolean active;
    private static TurbolaserHeavyModel<Entity> heavy;
    private static TurbolaserDefenseModel<Entity> defense;
    private static FighterModel<Entity> fighter;

    private ShipPreview() {
    }

    /** Registra el escenario "preview_ships" de ClientAutoTest. */
    public static void registerScenario() {
        ClientAutoTest.register("preview_ships", s -> s
                .wait(200).cmd("gamemode spectator").cmd("time set noon").cmd("weather clear")
                .tp(0.5, 200, 0.5).wait(60).look(0, 0).hideHud(true).run(mc -> {
                    mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
                    active = true;
                }).wait(40)
                // torretas de frente y en reposo
                .run(mc -> scene(new Item(Kind.HEAVY, -2.2, -1.4, 6.5, 160, 0, 0, false),
                        new Item(Kind.DEFENSE, 2.2, -1.2, 5.5, 200, 0, 0, false))).wait(10).shot("ships_turrets")
                // varias rotaciones de yaw/pitch (la torreta mira a la camara con yaw 180)
                .run(mc -> scene(new Item(Kind.HEAVY, -6, -2.2, 10, 180, 0, 0, false),
                        new Item(Kind.HEAVY, -2, -2.2, 10, 180, 45, 20, false),
                        new Item(Kind.HEAVY, 2, -2.2, 10, 180, 90, 40, false),
                        new Item(Kind.HEAVY, 6, -2.2, 10, 180, -45, -10, false),
                        new Item(Kind.DEFENSE, -3, -0.2, 7, 180, 30, 30, false),
                        new Item(Kind.DEFENSE, 0, -0.2, 7, 180, -60, 60, false),
                        new Item(Kind.DEFENSE, 3, -0.2, 7, 180, 120, 15, false))).wait(10).shot("ships_turrets_aim")
                .run(mc -> scene(new Item(Kind.HEAVY, 0, -1.6, 5, 90, 0, 25, false))).wait(10).shot("ships_heavy_side")
                // caza por fuera
                .run(mc -> scene(new Item(Kind.FIGHTER, 0, -1.5, 7, 180, 0, 0, true))).wait(10).shot("ships_fighter_front")
                .run(mc -> scene(new Item(Kind.FIGHTER, 0, -1.6, 7, 140, 0, 0, true))).wait(10).shot("ships_fighter_q34")
                .run(mc -> scene(new Item(Kind.FIGHTER, 0, -1.6, 7, 30, 0, 0, false))).wait(10).shot("ships_fighter_back")
                .run(mc -> scene(new Item(Kind.FIGHTER, 0, -1.55, 2.6, 180, 0, 0, true))).wait(10).shot("ships_fighter_pilot")
                // desde dentro de la cabina: la camara en la cabeza del piloto mirando a la ventana
                .run(mc -> scene(new Item(Kind.FIGHTER, 0, -EYE_Y, -EYE_Z, 0, 0, 0, false))).wait(10).shot("ships_fighter_inside")
                .look(-35, 25).wait(10).shot("ships_fighter_inside_side")
                .wait(20).quit());
    }

    private static void scene(Item... list) {
        items.clear();
        items.addAll(List.of(list));
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (!active || event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (heavy == null) {
            heavy = new TurbolaserHeavyModel<>(TurbolaserHeavyModel.createBodyLayer().bakeRoot());
            defense = new TurbolaserDefenseModel<>(TurbolaserDefenseModel.createBodyLayer().bakeRoot());
            fighter = new FighterModel<>(FighterModel.createBodyLayer().bakeRoot());
        }
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        for (Item it : items) {
            PoseStack ps = new PoseStack();
            // posicion relativa a la camara (el escenario fija la camara en el sitio)
            ps.translate(it.dx(), it.dy(), it.dz());
            ps.mulPose(Axis.YP.rotationDegrees(180.0F - it.yaw()));
            ps.scale(-1.0F, -1.0F, 1.0F);
            ps.translate(0.0F, -1.501F, 0.0F);
            switch (it.kind()) {
                case HEAVY -> {
                    heavy.resetPose();
                    heavy.yaw.yRot = (float) Math.toRadians(it.tYaw());
                    heavy.pitch.xRot = (float) Math.toRadians(it.tPitch());
                    draw(heavy, ps, buffers, TurbolaserHeavyModel.TEXTURE, TurbolaserHeavyModel.GLOW_TEXTURE);
                }
                case DEFENSE -> {
                    defense.resetPose();
                    defense.yaw.yRot = (float) Math.toRadians(it.tYaw());
                    defense.pitch.xRot = (float) Math.toRadians(it.tPitch());
                    draw(defense, ps, buffers, TurbolaserDefenseModel.TEXTURE, TurbolaserDefenseModel.GLOW_TEXTURE);
                }
                case FIGHTER -> {
                    fighter.resetPose();
                    fighter.pilot.visible = it.pilot();
                    draw(fighter, ps, buffers, FighterModel.TEXTURE, FighterModel.GLOW_TEXTURE);
                    drawGlass(ps, buffers);
                }
            }
        }
        buffers.endBatch();
    }

    /** Capa base sin culling (las caras interiores de la cabina se ven desde dentro) + capa emisiva. */
    private static void draw(HierarchicalModel<Entity> model, PoseStack ps, MultiBufferSource.BufferSource buffers,
            ResourceLocation tex, ResourceLocation glow) {
        model.renderToBuffer(ps, buffers.getBuffer(RenderType.entityCutoutNoCull(tex)), LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, -1);
        buffers.endBatch();
        model.renderToBuffer(ps, buffers.getBuffer(RenderType.eyes(glow)), LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, -1);
        buffers.endBatch();
    }

    /** Cristal verde translucido: octogono de 14 px en el plano del hueso window (su pivote esta en el cristal). */
    private static void drawGlass(PoseStack ps, MultiBufferSource.BufferSource buffers) {
        ps.pushPose();
        fighter.root().translateAndRotate(ps);
        fighter.rootBone.translateAndRotate(ps);
        fighter.cockpit.translateAndRotate(ps);
        fighter.window.translateAndRotate(ps);
        float a = 7.2F / 16.0F;
        float s = a * (float) Math.tan(Math.toRadians(22.5));
        PoseStack.Pose pose = ps.last();
        VertexConsumer vc = buffers.getBuffer(RenderType.debugQuads());
        int r = 60, g = 220, b = 120, al = 70;
        float[][] quads = {
            {-s, a, s, a, a, s, -a, s},
            {-a, s, a, s, a, -s, -a, -s},
            {-a, -s, a, -s, s, -a, -s, -a},
        };
        for (float[] q : quads) {
            for (int i = 0; i < 8; i += 2) {
                vc.addVertex(pose, q[i], q[i + 1], 0.0F).setColor(r, g, b, al);
            }
        }
        buffers.endBatch();
        ps.popPose();
    }
}
