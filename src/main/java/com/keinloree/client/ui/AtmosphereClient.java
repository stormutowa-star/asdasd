package com.keinloree.client.ui;

import com.keinloree.net.AtmosphereS2C;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.ViewportEvent;

/** Atmosfera: niebla y velo verdosos con transiciones suaves (lerp por tick). */
public final class AtmosphereClient {
    private static float tintTarget;
    private static float fogTarget;
    private static int flags;
    private static float tint;
    private static float fog;
    private static float tintPrev;
    private static float fogPrev;

    private AtmosphereClient() {}

    public static void set(AtmosphereS2C msg) {
        tintTarget = Mth.clamp(msg.tint(), 0.0F, 1.0F);
        fogTarget = Mth.clamp(msg.fog(), 0.0F, 1.0F);
        flags = msg.flags();
    }

    public static void reset() {
        tintTarget = 0;
        fogTarget = 0;
        flags = 0;
        tint = tintPrev = 0;
        fog = fogPrev = 0;
    }

    public static void tick() {
        tintPrev = tint;
        fogPrev = fog;
        tint += (tintTarget - tint) * 0.04F;
        fog += (fogTarget - fog) * 0.04F;
    }

    public static float tint(float partial) {
        return Mth.lerp(partial, tintPrev, tint);
    }

    public static void onFogColor(ViewportEvent.ComputeFogColor e) {
        float k = tint((float) e.getPartialTick());
        if (k < 0.005F) {
            return;
        }
        float mix = k * 0.8F;
        float tr = 0.05F;
        float tg = (flags & AtmosphereS2C.NEXO_UNIDO) != 0 ? 0.30F : 0.20F;
        float tb = 0.09F;
        if ((flags & AtmosphereS2C.HORA_PERDIDA) != 0) {
            mix *= 0.9F;
            tg *= 0.7F;
        }
        e.setRed(Mth.lerp(mix, e.getRed(), tr));
        e.setGreen(Mth.lerp(mix, e.getGreen(), tg));
        e.setBlue(Mth.lerp(mix, e.getBlue(), tb));
    }

    public static void onFog(ViewportEvent.RenderFog e) {
        float f = Mth.lerp((float) e.getPartialTick(), fogPrev, fog);
        if (f < 0.005F || e.getType() != net.minecraft.world.level.material.FogType.NONE) {
            return;
        }
        float scale = Mth.lerp(f, 1.0F, 0.3F);
        if (e.getMode() == FogRenderer.FogMode.FOG_TERRAIN || e.getMode() == FogRenderer.FogMode.FOG_SKY) {
            e.scaleFarPlaneDistance(scale);
            e.scaleNearPlaneDistance(scale * 0.8F);
            e.setCanceled(true);
        }
    }
}
