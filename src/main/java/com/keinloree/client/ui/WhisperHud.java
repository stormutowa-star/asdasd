package com.keinloree.client.ui;

import com.keinloree.net.WhisperS2C;
import com.keinloree.registry.ModSounds;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

/** Susurro de omnisciencia: texto verde tembloroso arriba en el centro durante 4 segundos. */
public final class WhisperHud {
    private static final int TOTAL = 80;
    private static String text = "";
    private static int age = TOTAL;

    private WhisperHud() {}

    public static void show(WhisperS2C msg) {
        text = KlText.resolve(msg.text());
        age = 0;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.KEIN_WHISPER.get(), 1.0F, 0.7F));
    }

    public static void clear() {
        age = TOTAL;
    }

    public static void tick() {
        if (age < TOTAL) {
            age++;
        }
    }

    public static void render(GuiGraphics gg, float partial, int w, int h) {
        if (age >= TOTAL) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        float t = age + partial;
        float fade = Math.min(1.0F, Math.min(t / 10.0F, (TOTAL - t) / 20.0F)) * 0.8F;
        if (!KlText.visible(fade)) {
            return;
        }
        Random r = new Random((age / 2) * 7919L);
        int y = (int) (h * 0.2F);
        for (var line : font.split(net.minecraft.network.chat.Component.literal(text), (int) (w * 0.7F))) {
            int lw = font.width(line);
            gg.drawString(font, line, (w - lw) / 2 + r.nextInt(3) - 1, y + r.nextInt(3) - 1, KlText.argb(0x6CFF9A, fade), true);
            y += 10;
        }
    }
}
