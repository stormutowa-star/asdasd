package com.keinloree.client.ui;

import com.keinloree.net.TitleCardS2C;
import com.keinloree.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

/** Tarjeta de capitulo: titulo x3, subtitulo x1.5, linea que se expande y fundidos. */
public final class TitleCard {
    private static boolean active;
    private static String title = "";
    private static String subtitle = "";
    private static int color = 0xFFFFFF;
    private static int total;
    private static int age;

    private TitleCard() {}

    public static void show(TitleCardS2C msg) {
        title = KlText.resolve(msg.title());
        subtitle = KlText.resolve(msg.subtitle());
        color = msg.color() & 0xFFFFFF;
        total = Math.max(30, msg.ticks());
        age = 0;
        active = true;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.CHAPTER_STING.get(), 1.0F, 1.0F));
    }

    public static void clear() {
        active = false;
    }

    public static void tick() {
        if (active && ++age >= total) {
            active = false;
        }
    }

    public static void render(GuiGraphics gg, float partial, int w, int h) {
        if (!active) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        float t = age + partial;
        float fade = Math.min(1.0F, Math.min(t / 14.0F, (total - t) / 18.0F));
        if (fade <= 0.0F) {
            return;
        }
        int cy = (int) (h * 0.36F);
        float s1 = 3.0F;
        int tw = font.width(title);
        if (KlText.visible(fade)) {
            gg.pose().pushPose();
            gg.pose().scale(s1, s1, 1.0F);
            gg.drawString(font, title, (int) ((w / 2.0F - tw * s1 / 2.0F) / s1), (int) (cy / s1), KlText.argb(color, fade), true);
            gg.pose().popPose();
        }
        // Linea fina que se expande
        int lineW = (int) (Math.max(tw * s1, 120.0F) * Math.min(1.0F, t / 30.0F));
        int ly = cy + (int) (9 * s1) + 6;
        gg.fill(w / 2 - lineW / 2, ly, w / 2 + lineW / 2, ly + 1, KlText.argb(color, 0.8F * fade));
        if (!subtitle.isEmpty() && KlText.visible(fade)) {
            float s2 = 1.5F;
            int sw = font.width(subtitle);
            gg.pose().pushPose();
            gg.pose().scale(s2, s2, 1.0F);
            gg.drawString(font, subtitle, (int) ((w / 2.0F - sw * s2 / 2.0F) / s2), (int) ((ly + 8) / s2),
                    KlText.argb(0xE0E0E0, fade), true);
            gg.pose().popPose();
        }
    }
}
