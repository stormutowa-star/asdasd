package com.keinloree.client.test;

import com.keinloree.client.model.gen.RazakoAnims;
import com.keinloree.client.model.gen.RazakoModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
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
import org.joml.Vector3f;

/**
 * Vista previa generica de modelos generados por Blockbench (client/model/gen) SIN entidades.
 * Cada modelo se registra con una linea en {@link #MODELS} (nombre -> modelo, textura, _glow y animaciones) y
 * obtiene el escenario de cliente "preview_&lt;nombre&gt;": capturas de frente, perfil, espalda, 3/4 y cada animacion
 * en el 0/25/50/75 % de su duracion (de frente) y al 50 % de perfil. Capturas: run/screenshots/pv_&lt;nombre&gt;_*.png.
 * Para registrar el escenario hay que llamar a {@link #registerScenario(String)} desde el bloque static de ClientAutoTest.
 */
@Mod.EventBusSubscriber(modid = "keinloree", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModelPreview {
    /** Un modelo previsualizable: fabrica del modelo horneado, texturas y animaciones por nombre. */
    public record Entry(Supplier<HierarchicalModel<Entity>> factory, ResourceLocation texture, ResourceLocation glow,
            Map<String, AnimationDefinition> anims, float distance) {
    }

    /** Registro de modelos: anade aqui una linea por modelo nuevo. */
    public static final Map<String, Entry> MODELS = new LinkedHashMap<>();

    static {
        MODELS.put("razako", new Entry(() -> new RazakoModel<>(RazakoModel.createBodyLayer().bakeRoot()),
                RazakoModel.TEXTURE, RazakoModel.GLOW_TEXTURE, anims(
                        "idle", RazakoAnims.IDLE, "walk", RazakoAnims.WALK, "run", RazakoAnims.RUN,
                        "attack_1", RazakoAnims.ATTACK_1, "attack_2", RazakoAnims.ATTACK_2, "attack_3", RazakoAnims.ATTACK_3,
                        "charge", RazakoAnims.CHARGE, "leap", RazakoAnims.LEAP, "roar", RazakoAnims.ROAR,
                        "block", RazakoAnims.BLOCK, "horizon", RazakoAnims.HORIZON, "chain", RazakoAnims.CHAIN,
                        "kneel", RazakoAnims.KNEEL, "hurt", RazakoAnims.HURT, "pose", RazakoAnims.POSE), 6.5F));
    }

    private static final Vector3f CACHE = new Vector3f();
    private static final Map<String, HierarchicalModel<Entity>> BAKED = new LinkedHashMap<>();
    private static String current;
    private static float yaw = 180.0F; // 180 = de frente a la camara (la camara mira al sur)
    private static String anim; // null = pose de reposo
    private static long animMs;

    private ModelPreview() {
    }

    /** Ayuda para construir el mapa de animaciones: pares nombre, definicion. */
    public static Map<String, AnimationDefinition> anims(Object... pairs) {
        Map<String, AnimationDefinition> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            map.put((String) pairs[i], (AnimationDefinition) pairs[i + 1]);
        }
        return map;
    }

    /** Registra el escenario "preview_&lt;nombre&gt;" de ClientAutoTest para un modelo de {@link #MODELS}. */
    public static void registerScenario(String name) {
        Entry entry = MODELS.get(name);
        if (entry == null) {
            return;
        }
        ClientAutoTest.register("preview_" + name, s -> {
            s.wait(200).cmd("gamemode spectator").cmd("time set noon").cmd("weather clear")
                    .tp(0.5, 200, 0.5).wait(60).look(0, 0).hideHud(true).run(mc -> {
                        mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
                        current = name;
                    }).wait(40);
            String p = "pv_" + name + "_";
            s.run(mc -> set(180, null, 0)).wait(5).shot(p + "front");
            s.run(mc -> set(90, null, 0)).wait(5).shot(p + "side");
            s.run(mc -> set(0, null, 0)).wait(5).shot(p + "back");
            s.run(mc -> set(140, null, 0)).wait(5).shot(p + "q34");
            List<String> names = new ArrayList<>(entry.anims().keySet());
            for (String a : names) {
                long len = (long) (entry.anims().get(a).lengthInSeconds() * 1000.0F);
                for (int pct : new int[] {0, 25, 50, 75}) {
                    long ms = len * pct / 100;
                    s.run(mc -> set(180, a, ms)).wait(3).shot(p + a + "_" + pct);
                }
                s.run(mc -> set(90, a, len / 2)).wait(3).shot(p + a + "_50_side");
            }
            s.run(mc -> current = null).wait(20).quit();
        });
    }

    private static void set(float newYaw, String newAnim, long ms) {
        yaw = newYaw;
        anim = newAnim;
        animMs = ms;
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (current == null || event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Entry entry = MODELS.get(current);
        HierarchicalModel<Entity> model = BAKED.computeIfAbsent(current, k -> entry.factory().get());
        model.root().getAllParts().forEach(ModelPart::resetPose);
        if (anim != null) {
            KeyframeAnimations.animate(model, entry.anims().get(anim), animMs, 1.0F, CACHE);
        }
        Minecraft mc = Minecraft.getInstance();
        PoseStack ps = new PoseStack();
        // delante de la camara (que mira al sur), pies algo por debajo de los ojos
        ps.translate(0.0, -2.0, entry.distance());
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - yaw)); // igual que LivingEntityRenderer.render
        ps.scale(-1.0F, -1.0F, 1.0F);
        ps.translate(0.0F, -1.501F, 0.0F);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        model.renderToBuffer(ps, buffers.getBuffer(RenderType.entityCutoutNoCull(entry.texture())),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
        buffers.endBatch();
        model.renderToBuffer(ps, buffers.getBuffer(RenderType.eyes(entry.glow())), LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, -1);
        buffers.endBatch();
    }
}
