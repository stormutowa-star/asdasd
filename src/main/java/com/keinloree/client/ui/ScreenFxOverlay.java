package com.keinloree.client.ui;

import com.keinloree.client.cam.CinematicCamera;
import com.keinloree.net.ScreenFxS2C;
import com.keinloree.story.ScreenFx;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Efectos de pantalla: FLASH, FADE_OUT, FADE_IN, BLACK, SHAKE, TINT, GLITCH, VIGNETTE y TIME_FREEZE.
 * Tambien dibuja el letterbox de las cinematicas y el velo verde de la atmosfera.
 */
public final class ScreenFxOverlay {
    /** Si un fundido a negro se queda sin FADE_IN, se libera solo pasado este tiempo. */
    private static final int BLACK_FAILSAFE = 1200;

    private static final class Fx {
        final int type;
        final int total;
        final float intensity;
        final int color;
        int age;

        Fx(int type, int total, float intensity, int color) {
            this.type = type;
            this.total = Math.max(1, total);
            this.intensity = intensity;
            this.color = color & 0xFFFFFF;
        }

        float u(float partial) {
            return Mth.clamp((age + partial) / total, 0.0F, 1.0F);
        }
    }

    private static final List<Fx> ACTIVE = new ArrayList<>();

    // Capa de negro (FADE_OUT / FADE_IN / BLACK): interpolacion de alfa con retencion
    private static float blackFrom;
    private static float blackTo;
    private static int blackAge;
    private static int blackDur = 1;
    private static int blackHold;
    private static int blackColor;
    private static float blackNow;
    private static float blackPrev;
    private static int blackHeld;

    private ScreenFxOverlay() {}

    public static void onFx(ScreenFxS2C msg) {
        float in = msg.intensity() <= 0.0F ? 1.0F : msg.intensity();
        switch (msg.type()) {
            case ScreenFx.FADE_OUT -> startBlack(blackNow, 1.0F, msg.ticks(), 0, msg.color());
            case ScreenFx.FADE_IN -> startBlack(Math.max(blackNow, 1.0F), 0.0F, msg.ticks(), 0, msg.color());
            case ScreenFx.BLACK -> startBlack(1.0F, 1.0F, 1, Math.max(1, msg.ticks()), msg.color());
            default -> ACTIVE.add(new Fx(msg.type(), msg.ticks(), in, msg.color()));
        }
    }

    private static void startBlack(float from, float to, int ticks, int hold, int color) {
        blackFrom = from;
        blackTo = to;
        blackAge = 0;
        blackDur = Math.max(1, ticks);
        blackHold = hold;
        blackColor = 0; // los fundidos son siempre a negro (el guion pone blanco por defecto)
        blackHeld = 0;
    }

    /** Quita todo (desconexion, muerte). */
    public static void clear() {
        ACTIVE.clear();
        blackFrom = 0.0F;
        blackTo = 0.0F;
        blackNow = 0.0F;
        blackPrev = 0.0F;
        blackAge = blackDur;
        blackHold = 0;
        blackHeld = 0;
    }

    public static void tick() {
        blackPrev = blackNow;
        if (blackAge < blackDur) {
            blackAge++;
            blackNow = Mth.lerp(blackAge / (float) blackDur, blackFrom, blackTo);
        } else if (blackHold > 0) {
            blackHold--;
            if (blackHold == 0) {
                blackFrom = blackNow;
                blackTo = 0.0F;
                blackAge = 0;
                blackDur = 6;
            }
        } else if (blackNow > 0.99F && ++blackHeld > BLACK_FAILSAFE) {
            // Seguro: nunca dejar la pantalla negra para siempre
            startBlack(1.0F, 0.0F, 30, 0, 0);
        }
        Iterator<Fx> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Fx fx = it.next();
            if (++fx.age >= fx.total) {
                it.remove();
            }
        }
    }

    /** Intensidad de temblor en grados para la camara (suma de todos los SHAKE activos). */
    public static float shakeAmount(float partial) {
        float amp = 0.0F;
        for (Fx fx : ACTIVE) {
            if (fx.type == ScreenFx.SHAKE) {
                amp += fx.intensity * (1.0F - fx.u(partial));
            }
        }
        return amp;
    }

    // ---- dibujo ----

    public static void renderBars(GuiGraphics gg, float partial, int w, int h) {
        float frac = CinematicCamera.barFraction(partial);
        if (frac <= 0.001F) {
            return;
        }
        int bar = Math.round(h * frac);
        gg.fill(0, 0, w, bar, 0xFF000000);
        gg.fill(0, h - bar, w, h, 0xFF000000);
    }

    public static void render(GuiGraphics gg, float partial, int w, int h, float time) {
        // Velo negro
        float black = Mth.lerp(partial, blackPrev, blackNow);
        for (Fx fx : ACTIVE) {
            float u = fx.u(partial);
            switch (fx.type) {
                case ScreenFx.FLASH -> {
                    float a = fx.intensity * (1.0F - u) * (1.0F - u);
                    gg.fill(0, 0, w, h, KlText.argb(fx.color, Math.min(1.0F, a)));
                }
                case ScreenFx.TINT -> {
                    float a = 0.45F * Math.min(1.0F, fx.intensity) * envelope(u, 0.15F, 0.25F);
                    gg.fill(0, 0, w, h, KlText.argb(fx.color, a));
                }
                case ScreenFx.GLITCH -> glitch(gg, w, h, fx, time);
                case ScreenFx.VIGNETTE -> vignette(gg, w, h, fx, u, time);
                case ScreenFx.TIME_FREEZE -> timeFreeze(gg, w, h, fx, u, time);
                default -> {
                }
            }
        }
        if (black > 0.003F) {
            gg.fill(0, 0, w, h, KlText.argb(blackColor, Math.min(1.0F, black)));
        }
    }

    /** Sube al principio y baja al final (fracciones del total). */
    private static float envelope(float u, float in, float out) {
        return Math.min(1.0F, Math.min(u / in, (1.0F - u) / out));
    }

    private static void glitch(GuiGraphics gg, int w, int h, Fx fx, float time) {
        Random r = new Random((long) (time / 2.0F) * 104729L + fx.color);
        float k = Math.min(1.5F, fx.intensity);
        int bars = (int) (8 * k) + 2;
        float env = envelope(fx.u(0.0F), 0.1F, 0.2F);
        for (int i = 0; i < bars; i++) {
            int y = r.nextInt(h);
            int bh = 2 + r.nextInt(Math.max(3, h / 14));
            int x0 = r.nextInt(w / 3) - w / 8;
            int x1 = x0 + w / 2 + r.nextInt(w / 2);
            int rgb = r.nextInt(3) == 0 ? 0xFFFFFF : (r.nextBoolean() ? 0x2CFF6A : 0x0B8F3A);
            gg.fill(Math.max(0, x0), y, Math.min(w, x1), Math.min(h, y + bh), KlText.argb(rgb, (0.12F + r.nextFloat() * 0.25F) * env));
        }
        // Ruido verde
        int dots = (int) (60 * k);
        for (int i = 0; i < dots; i++) {
            int x = r.nextInt(w);
            int y = r.nextInt(h);
            gg.fill(x, y, x + 2, y + 1, KlText.argb(0x4CFF7A, (0.2F + r.nextFloat() * 0.4F) * env));
        }
    }

    private static void vignette(GuiGraphics gg, int w, int h, Fx fx, float u, float time) {
        float pulse = 0.75F + 0.25F * Mth.sin(time * 0.12F);
        float a = Math.min(1.0F, fx.intensity) * pulse * envelope(u, 0.15F, 0.2F);
        int c0 = KlText.argb(0, a * 0.85F);
        int c1 = KlText.argb(0, 0.0F);
        int tv = h / 3;
        int th = w / 4;
        gg.fillGradient(0, 0, w, tv, c0, c1);
        gg.fillGradient(0, h - tv, w, h, c1, c0);
        // Lados: degradado horizontal con tiras de 2 px
        for (int x = 0; x < th; x += 2) {
            float f = 1.0F - x / (float) th;
            int c = KlText.argb(0, a * 0.7F * f * f);
            gg.fill(x, 0, x + 2, h, c);
            gg.fill(w - x - 2, 0, w - x, h, c);
        }
    }

    private static void timeFreeze(GuiGraphics gg, int w, int h, Fx fx, float u, float time) {
        float env = envelope(u, 0.12F, 0.2F);
        gg.fill(0, 0, w, h, KlText.argb(0x1B2A3C, 0.38F * env));
        int cx = w / 2;
        int cy = h / 2;
        int radius = Math.max(24, Math.min(w, h) / 6);
        int ring = KlText.argb(0xBFD8F0, 0.75F * env);
        for (int i = 0; i < 120; i++) {
            double ang = i * (Math.PI * 2.0D / 120.0D);
            int x = cx + (int) Math.round(Math.cos(ang) * radius);
            int y = cy + (int) Math.round(Math.sin(ang) * radius);
            gg.fill(x, y, x + 1, y + 1, ring);
        }
        // Marcas de hora
        for (int i = 0; i < 12; i++) {
            double ang = i * (Math.PI / 6.0D);
            for (int d = 0; d < 3; d++) {
                int x = cx + (int) Math.round(Math.cos(ang) * (radius - 2 - d));
                int y = cy + (int) Math.round(Math.sin(ang) * (radius - 2 - d));
                gg.fill(x, y, x + 1, y + 1, ring);
            }
        }
        // Manecilla que gira despacio
        double hand = time * 0.02D;
        for (int d = 2; d < radius - 4; d++) {
            int x = cx + (int) Math.round(Math.cos(hand) * d);
            int y = cy + (int) Math.round(Math.sin(hand) * d);
            gg.fill(x, y, x + 1, y + 1, ring);
        }
    }

    /** Velo verde de la atmosfera (el tinte del cielo y la niebla se hace en eventos de render). */
    public static void renderAtmosphere(GuiGraphics gg, int w, int h, float tint) {
        if (tint > 0.01F) {
            gg.fill(0, 0, w, h, KlText.argb(0x1E7A44, tint * 0.14F));
        }
    }
}
