package com.keinloree.client.ui;

import com.keinloree.net.HudS2C;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Panel de objetivo arriba a la izquierda y brujula horizontal con rombo y distancia. */
public final class ObjectiveHud {
    private static String objective = "";
    private static boolean hasMarker;
    private static BlockPos marker = BlockPos.ZERO;
    private static String label = "";
    private static int cur;
    private static int max;
    private static int color = 0x4CFF7A;
    private static int age;

    private ObjectiveHud() {}

    public static void set(HudS2C msg) {
        String text = KlText.resolve(msg.objective());
        if (!text.equals(objective)) {
            age = 0;
        }
        objective = text;
        hasMarker = msg.hasMarker();
        marker = msg.marker();
        label = KlText.resolve(msg.label());
        cur = msg.cur();
        max = msg.max();
        color = (msg.color() & 0xFFFFFF) == 0 ? 0x4CFF7A : msg.color() & 0xFFFFFF;
    }

    public static void clear() {
        objective = "";
        hasMarker = false;
    }

    public static void tick() {
        age++;
    }

    public static void render(GuiGraphics gg, float partial, int w, int h) {
        if (objective.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        float fade = Math.min(1.0F, (age + partial) / 10.0F);
        // Panel
        int pw = 150;
        List<FormattedCharSequence> lines = font.split(Component.literal(objective), pw - 12);
        int ph = 6 + 8 + lines.size() * 10 + (max > 0 ? 10 : 0) + 4;
        int x = 6;
        int y = 6;
        gg.fill(x, y, x + pw, y + ph, KlText.argb(0, 0.55F * fade));
        gg.fill(x, y, x + 2, y + ph, KlText.argb(color, 0.9F * fade));
        int ty = y + 5;
        if (KlText.visible(fade)) {
            gg.pose().pushPose();
            gg.pose().scale(0.75F, 0.75F, 1.0F);
            gg.drawString(font, I18n.get("keinloree.hud.objective"), (int) ((x + 8) / 0.75F), (int) (ty / 0.75F),
                    KlText.argb(color, fade), false);
            gg.pose().popPose();
            ty += 9;
            for (FormattedCharSequence l : lines) {
                gg.drawString(font, l, x + 8, ty, KlText.argb(0xFFFFFF, fade), true);
                ty += 10;
            }
        }
        if (max > 0) {
            int bx = x + 8;
            int bw = pw - 16;
            gg.fill(bx, ty + 1, bx + bw, ty + 5, KlText.argb(0x303030, 0.8F * fade));
            gg.fill(bx, ty + 1, bx + (int) (bw * Mth.clamp(cur / (float) max, 0.0F, 1.0F)), ty + 5, KlText.argb(color, 0.95F * fade));
        }
        if (hasMarker && mc.player != null) {
            compass(gg, font, mc, partial, w, fade);
        }
    }

    private static void compass(GuiGraphics gg, Font font, Minecraft mc, float partial, int w, float fade) {
        int sw = Math.min(200, w / 2);
        int cx = w / 2;
        int y = 6;
        int hgt = 12;
        gg.fill(cx - sw / 2, y, cx + sw / 2, y + hgt, KlText.argb(0, 0.45F * fade));
        float yaw = mc.player.getViewYRot(partial);
        // Marcas cada 15 grados dentro de la franja de 180
        for (int a = -90; a <= 90; a += 15) {
            float world = Mth.wrapDegrees(Math.round((yaw + a) / 15.0F) * 15.0F);
            float rel = Mth.wrapDegrees(world - yaw);
            if (Math.abs(rel) > 90.0F) {
                continue;
            }
            int px = cx + (int) (rel / 90.0F * (sw / 2));
            int mod = Math.floorMod(Math.round(world), 360);
            String card = mod == 0 ? "keinloree.compass.s" : mod == 90 ? "keinloree.compass.w"
                    : mod == 180 ? "keinloree.compass.n" : mod == 270 ? "keinloree.compass.e" : null;
            if (card != null) {
                String s = I18n.get(card);
                gg.drawString(font, s, px - font.width(s) / 2, y + 2, KlText.argb(0xFFFFFF, fade), true);
            } else {
                gg.fill(px, y + 4, px + 1, y + 8, KlText.argb(0xFFFFFF, 0.4F * fade));
            }
        }
        // Rombo en el rumbo del objetivo
        Vec3 target = Vec3.atCenterOf(marker);
        Vec3 eye = mc.player.getEyePosition(partial);
        double dx = target.x - eye.x;
        double dz = target.z - eye.z;
        float bearing = (float) (Mth.atan2(-dx, dz) * (180.0D / Math.PI));
        float rel = Mth.clamp(Mth.wrapDegrees(bearing - yaw), -90.0F, 90.0F);
        int mx = cx + (int) (rel / 90.0F * (sw / 2));
        int my = y + hgt / 2;
        int mc2 = KlText.argb(color, fade);
        for (int i = -3; i <= 3; i++) {
            int half = 3 - Math.abs(i);
            gg.fill(mx - half, my + i, mx + half + 1, my + i + 1, mc2);
        }
        int dist = (int) Math.round(eye.distanceTo(target));
        String txt = (label.isEmpty() ? "" : label + "  ") + I18n.get("keinloree.hud.meters", dist);
        gg.drawString(font, txt, cx - font.width(txt) / 2, y + hgt + 3, KlText.argb(color, fade), true);
    }
}
