package com.keinloree.story;

import com.keinloree.net.Net;
import com.keinloree.net.ScreenFxS2C;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Efectos de pantalla (servidor): envia el paquete al cliente. El color es RGB (0xRRGGBB). */
public final class ScreenFx {
    public static final int FLASH = 0;
    public static final int FADE_OUT = 1;
    public static final int FADE_IN = 2;
    public static final int BLACK = 3;
    public static final int SHAKE = 4;
    public static final int TINT = 5;
    public static final int GLITCH = 6;
    public static final int VIGNETTE = 7;
    public static final int TIME_FREEZE = 8;

    private static final Map<String, Integer> NAMES = Map.ofEntries(
            Map.entry("destello", FLASH), Map.entry("flash", FLASH),
            Map.entry("fundido_negro", FADE_OUT), Map.entry("fade_out", FADE_OUT),
            Map.entry("fundido_entrada", FADE_IN), Map.entry("fade_in", FADE_IN),
            Map.entry("negro", BLACK), Map.entry("black", BLACK),
            Map.entry("temblor", SHAKE), Map.entry("shake", SHAKE),
            Map.entry("tinte", TINT), Map.entry("tint", TINT),
            Map.entry("glitch", GLITCH),
            Map.entry("vineta", VIGNETTE), Map.entry("vignette", VIGNETTE),
            Map.entry("tiempo_detenido", TIME_FREEZE), Map.entry("time_freeze", TIME_FREEZE));

    private ScreenFx() {}

    /** Tipo por nombre (espanol del guion o ingles); -1 si no existe. */
    public static int parse(String name) {
        Integer t = NAMES.get(name.toLowerCase(java.util.Locale.ROOT));
        return t == null ? -1 : t;
    }

    public static java.util.Set<String> names() {
        return NAMES.keySet();
    }

    public static void send(ServerPlayer player, int type, int ticks, float intensity, int color) {
        Net.toPlayer(player, new ScreenFxS2C(type, ticks, intensity, color));
    }

    /** A todos los jugadores del nivel. */
    public static void sendAll(ServerLevel level, int type, int ticks, float intensity, int color) {
        Net.toAll(level, new ScreenFxS2C(type, ticks, intensity, color));
    }
}
