package com.keinloree.client.vfx;

import com.keinloree.net.VfxS2C;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Motor de VFX del cliente: guarda los efectos activos (maximo 512, se descartan los mas viejos), los avanza cada
 * tick y los dibuja tras las particulas en un unico lote por tipo de render.
 */
@Mod.EventBusSubscriber(modid = "keinloree", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VfxRenderer {
    public static final int MAX_EFFECTS = 512;
    private static final List<VfxEffect> ACTIVE = new ArrayList<>();
    private static ClientLevel lastLevel;
    private static MultiBufferSource.BufferSource source;

    private VfxRenderer() {
    }

    /** Anade un efecto recibido del servidor. */
    public static void add(VfxS2C msg) {
        VfxEffect e = VfxEffects.create(msg);
        if (e == null) {
            return;
        }
        synchronized (ACTIVE) {
            while (ACTIVE.size() >= MAX_EFFECTS) {
                ACTIVE.remove(0);
            }
            ACTIVE.add(e);
        }
    }

    /** Borra todos los efectos (pruebas y cambio de mundo). */
    public static void clear() {
        synchronized (ACTIVE) {
            ACTIVE.clear();
        }
    }

    /** Numero de efectos activos (para pruebas). */
    public static int count() {
        synchronized (ACTIVE) {
            return ACTIVE.size();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        synchronized (ACTIVE) {
            if (level == null || level != lastLevel) {
                ACTIVE.clear();
                lastLevel = level;
                return;
            }
            if (mc.isPaused()) {
                return;
            }
            for (int i = ACTIVE.size() - 1; i >= 0; i--) {
                VfxEffect e = ACTIVE.get(i);
                e.tick(level);
                if (e.dead()) {
                    ACTIVE.remove(i);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        synchronized (ACTIVE) {
            if (ACTIVE.isEmpty()) {
                return;
            }
            MultiBufferSource.BufferSource buffers = buffers();
            VfxDraw draw = new VfxDraw(buffers, event.getCamera());
            float pt = event.getPartialTick();
            for (VfxEffect e : ACTIVE) {
                try {
                    e.render(draw, e.age + pt);
                } catch (RuntimeException ex) {
                    // un efecto roto no debe tumbar el renderizado
                    e.age = Integer.MAX_VALUE / 2;
                }
            }
            buffers.endBatch();
        }
    }

    /** Fuente de buffers propia: un buffer reservado por cada RenderType del motor, asi no se cortan lotes. */
    private static MultiBufferSource.BufferSource buffers() {
        if (source == null) {
            for (var tex : VfxEffect.ADDITIVE_TEXTURES) {
                ModRenderTypes.glowAdditive(tex);
            }
            ModRenderTypes.glowAlpha(VfxEffect.SMOKE);
            ModRenderTypes.beamAdditive();
            ModRenderTypes.alphaFlat();
            Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map = new Object2ObjectLinkedOpenHashMap<>();
            // orden de dibujo: primero lo que mezcla normal (humo, esferas oscuras), luego lo aditivo
            map.put(ModRenderTypes.alphaFlat(), new ByteBufferBuilder(1 << 18));
            map.put(ModRenderTypes.glowAlpha(VfxEffect.SMOKE), new ByteBufferBuilder(1 << 18));
            for (RenderType t : ModRenderTypes.all()) {
                if (!map.containsKey(t)) {
                    map.put(t, new ByteBufferBuilder(1 << 18));
                }
            }
            source = MultiBufferSource.immediateWithBuffers(map, new ByteBufferBuilder(1 << 16));
        }
        return source;
    }
}
