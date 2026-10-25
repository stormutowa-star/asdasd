package com.keinloree.client.test;

import com.keinloree.client.model.gen.BbtestAnims;
import com.keinloree.client.model.gen.BbtestModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Verificacion del pipeline de Blockbench SIN registrar entidades: solo cuando el escenario de cliente
 * "bbtest" esta activo dibuja el modelo de prueba delante de la camara (sin entidad ni renderer).
 */
@Mod.EventBusSubscriber(modid = "keinloree", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BbTestRender {
    private static boolean active;
    private static float yaw = 180.0F; // 180 = mira hacia la camara (la camara mira al sur, yaw 0)
    private static long animMs = -1; // <0 = sin animacion
    private static BbtestModel<Entity> model;

    private BbTestRender() {
    }

    /** Registra el escenario "bbtest" de ClientAutoTest. */
    public static void registerScenario() {
        ClientAutoTest.register("bbtest", s -> s
                .wait(200).cmd("gamemode spectator").cmd("time set noon").cmd("weather clear")
                .tp(0.5, 200, 0.5).wait(60).look(0, 0).hideHud(true).run(mc -> {
                    mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
                    active = true;
                }).wait(60)
                .run(mc -> set(180, -1)).wait(10).shot("bbtest_front")
                .run(mc -> set(90, -1)).wait(10).shot("bbtest_side")
                .run(mc -> set(270, -1)).wait(10).shot("bbtest_side_left")
                .run(mc -> set(0, -1)).wait(10).shot("bbtest_back")
                .run(mc -> set(180, 0)).wait(10).shot("bbtest_wave0")
                .run(mc -> set(180, 500)).wait(10).shot("bbtest_wave25")
                .run(mc -> set(180, 1000)).wait(10).shot("bbtest_wave50")
                .run(mc -> set(90, 1000)).wait(10).shot("bbtest_wave50_side")
                .run(mc -> set(90, 0)).wait(10).shot("bbtest_wave0_side")
                .wait(20).quit());
    }

    private static void set(float newYaw, long newAnimMs) {
        yaw = newYaw;
        animMs = newAnimMs;
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (!active || event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (model == null) {
            model = new BbtestModel<>(BbtestModel.createBodyLayer().bakeRoot());
        }
        model.resetPose();
        if (animMs >= 0) {
            model.applyAnim(BbtestAnims.WAVE, animMs); // mismo helper que HierarchicalModel.animate
        }
        Vec3 cam = event.getCamera().getPosition();
        PoseStack ps = new PoseStack();
        // 3.5 bloques al sur de la camara (mira al sur), centro del modelo a la altura de los ojos
        ps.translate(mc.player.getX() - cam.x, -1.0, mc.player.getZ() + 3.5 - cam.z);
        // Igual que LivingEntityRenderer.render
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        ps.scale(-1.0F, -1.0F, 1.0F);
        ps.translate(0.0F, -1.501F, 0.0F);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        model.renderToBuffer(ps, buffers.getBuffer(RenderType.entityCutoutNoCull(BbtestModel.TEXTURE)),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
        model.renderToBuffer(ps, buffers.getBuffer(RenderType.eyes(BbtestModel.GLOW_TEXTURE)),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
        buffers.endBatch();
    }
}
