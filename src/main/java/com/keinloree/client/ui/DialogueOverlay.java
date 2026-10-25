package com.keinloree.client.ui;

import com.keinloree.client.cam.CinematicCamera;
import com.keinloree.net.DialogueS2C;
import com.keinloree.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

/**
 * Caja de dialogo: nombre en color, maquina de escribir con blip, narrador (cursiva centrada),
 * susurro (verde tembloroso en el centro) y glitch de la Voz. Dibujada por la capa de HUD del mod.
 */
public final class DialogueOverlay {
    private static final String GLITCH_CHARS = "#%&@$01?/<>*+=x";
    private static final int FADE_OUT = 8;

    private static boolean active;
    private static String name = "";
    private static String text = "";
    private static int color = 0xFFFFFF;
    private static int flags;
    private static int total;
    private static int age;
    private static int lastBlipGroup;
    private static final Random RNG = new Random();

    // Cache del ajuste de linea
    private static List<String> cacheLines = new ArrayList<>();
    private static int cacheWidth = -1;
    private static String cacheText = "";

    private DialogueOverlay() {}

    /** Recibe una linea del servidor (LIMPIAR borra la caja). */
    public static void onLine(DialogueS2C msg) {
        if ((msg.flags() & DialogueS2C.LIMPIAR) != 0 || msg.text().isEmpty()) {
            active = false;
            return;
        }
        active = true;
        name = KlText.resolve(msg.name());
        text = KlText.resolve(msg.text());
        color = msg.color() & 0xFFFFFF;
        flags = msg.flags();
        total = Math.max(1, msg.ticks());
        age = 0;
        lastBlipGroup = 0;
    }

    public static void clear() {
        active = false;
    }

    public static boolean isActive() {
        return active;
    }

    public static void tick() {
        if (!active) {
            return;
        }
        age++;
        if (age > total + FADE_OUT) {
            active = false;
            return;
        }
        if ((flags & (DialogueS2C.NARRADOR | DialogueS2C.SUSURRO)) == 0) {
            // Blip suave cada 3 letras
            int group = shown(1.0F) / 3;
            if (group > lastBlipGroup) {
                lastBlipGroup = group;
                int idx = Math.min(text.length(), shown(1.0F)) - 1;
                if (idx >= 0 && !Character.isWhitespace(text.charAt(idx))) {
                    Minecraft.getInstance().getSoundManager().play(
                            SimpleSoundInstance.forUI(ModSounds.DIALOGUE_BLIP.get(), 0.9F + RNG.nextFloat() * 0.3F, 0.25F));
                }
            }
        }
    }

    /** Letras visibles ahora (el ritmo se acelera si la linea dura poco). */
    private static int shown(float partial) {
        float cps = Math.max(2.25F, text.length() / Math.max(1.0F, total * 0.6F));
        return Math.min(text.length(), (int) ((age + partial - 1.0F) * cps) + 1);
    }

    private static List<String> wrap(Font font, int width) {
        if (width != cacheWidth || !text.equals(cacheText)) {
            cacheLines = new ArrayList<>();
            for (FormattedText t : font.getSplitter().splitLines(text, width, Style.EMPTY)) {
                cacheLines.add(t.getString());
            }
            cacheWidth = width;
            cacheText = text;
        }
        return cacheLines;
    }

    // ---- dibujo ----

    public static void render(GuiGraphics gg, float partial, int w, int h) {
        if (!active) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        float fade = age > total ? Math.max(0.0F, 1.0F - (age - total + partial) / FADE_OUT) : 1.0F;
        fade = Math.min(fade, Math.min(1.0F, (age + partial) / 4.0F));
        int visible = shown(partial);
        boolean glitch = (flags & DialogueS2C.GLITCH) != 0;
        long seed = (age / 2) * 7919L + 13L;

        if ((flags & DialogueS2C.SUSURRO) != 0) {
            drawWhisper(gg, font, w, h, fade, visible, seed);
            return;
        }
        int bottom = bottomY(w, h);
        if ((flags & DialogueS2C.NARRADOR) != 0) {
            drawNarrator(gg, font, w, bottom, fade, visible);
            return;
        }
        int bw = Math.min(420, (int) (w * 0.6F));
        bw = Math.max(bw, Math.min(w - 8, 200));
        int textW = bw - 16;
        List<String> lines = wrap(font, textW);
        boolean hasName = !name.isEmpty();
        int bh = 8 + (hasName ? 12 : 0) + lines.size() * 10 + 6;
        int x = (w - bw) / 2;
        int y = bottom - bh;
        int shake = glitch && (age % 23) < 3 ? RNG.nextInt(7) - 3 : 0;
        x += shake;

        int border = KlText.argb(glitch ? 0xE8E8E8 : color, 0.9F * fade);
        gg.fill(x, y, x + bw, y + bh, KlText.argb(0x000000, 0.7F * fade));
        gg.fill(x, y, x + bw, y + 1, border);
        gg.fill(x, y + bh - 1, x + bw, y + bh, border);
        gg.fill(x, y, x + 1, y + bh, border);
        gg.fill(x + bw - 1, y, x + bw, y + bh, border);

        int ty = y + 6;
        if (hasName) {
            drawText(gg, font, name, x + 8, ty, color, fade, glitch, seed + 1);
            ty += 12;
        }
        int remaining = visible;
        for (int i = 0; i < lines.size() && remaining > 0; i++) {
            String line = lines.get(i);
            String part = line.substring(0, Math.min(line.length(), remaining));
            remaining -= line.length();
            drawText(gg, font, part, x + 8, ty, 0xFFFFFF, fade, glitch, seed + i * 31L);
            ty += 10;
        }
        // Flecha de continuar cuando el texto termino de escribirse
        if (visible >= text.length() && ((age / 8) & 1) == 0) {
            int ax = x + bw - 14;
            int ay = y + bh - 9;
            int ac = KlText.argb(color, fade);
            gg.fill(ax, ay, ax + 7, ay + 1, ac);
            gg.fill(ax + 1, ay + 1, ax + 6, ay + 2, ac);
            gg.fill(ax + 2, ay + 2, ax + 5, ay + 3, ac);
            gg.fill(ax + 3, ay + 3, ax + 4, ay + 4, ac);
        }
    }

    /** Linea base inferior de la caja: sobre la barra rapida y las barras de vida, o sobre el letterbox. */
    static int bottomY(int w, int h) {
        if (CinematicCamera.isActive()) {
            return h - Math.round(h * CinematicCamera.barFraction(1.0F)) - 8;
        }
        return h - 66;
    }

    private static void drawNarrator(GuiGraphics gg, Font font, int w, int bottom, float fade, int visible) {
        List<String> lines = wrap(font, Math.min(380, (int) (w * 0.7F)));
        int y = bottom - lines.size() * 10;
        int remaining = visible;
        for (String line : lines) {
            if (remaining <= 0) {
                break;
            }
            String part = line.substring(0, Math.min(line.length(), remaining));
            remaining -= line.length();
            if (KlText.visible(fade)) {
                // El ancho se calcula con la linea completa para que no se mueva al escribir
                int lw = font.width(Component.literal(line).withStyle(ChatFormatting.ITALIC));
                gg.drawString(font, Component.literal(part).withStyle(ChatFormatting.ITALIC),
                        (w - lw) / 2, y, KlText.argb(0xB8B8B8, fade), true);
            }
            y += 10;
        }
    }

    private static void drawWhisper(GuiGraphics gg, Font font, int w, int h, float fade, int visible, long seed) {
        List<String> lines = wrap(font, Math.min(320, (int) (w * 0.7F)));
        Random r = new Random(seed);
        int y = h / 2 - lines.size() * 5 - 10;
        int remaining = visible;
        for (String line : lines) {
            if (remaining <= 0) {
                break;
            }
            String part = line.substring(0, Math.min(line.length(), remaining));
            remaining -= line.length();
            int lw = font.width(line);
            int jx = r.nextInt(3) - 1;
            int jy = r.nextInt(3) - 1;
            if (KlText.visible(fade * 0.75F)) {
                gg.drawString(font, part, (w - lw) / 2 + jx, y + jy, KlText.argb(0x6CFF9A, 0.75F * fade), true);
            }
            y += 10;
        }
    }

    /** Texto normal o con glitch: letras que cambian y desplazamientos con separacion de color. */
    private static void drawText(GuiGraphics gg, Font font, String s, int x, int y, int rgb, float fade,
                                 boolean glitch, long seed) {
        if (!KlText.visible(fade)) {
            return;
        }
        if (!glitch) {
            gg.drawString(font, s, x, y, KlText.argb(rgb, fade), true);
            return;
        }
        Random r = new Random(seed);
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) != ' ' && r.nextFloat() < 0.09F) {
                sb.setCharAt(i, GLITCH_CHARS.charAt(r.nextInt(GLITCH_CHARS.length())));
            }
        }
        int dx = r.nextFloat() < 0.25F ? r.nextInt(5) - 2 : 0;
        gg.drawString(font, sb.toString(), x + dx + 1, y, KlText.argb(0xFF3050, 0.45F * fade), false);
        gg.drawString(font, sb.toString(), x + dx - 1, y, KlText.argb(0x30E0FF, 0.45F * fade), false);
        gg.drawString(font, sb.toString(), x + dx, y, KlText.argb(rgb, fade), false);
    }
}
