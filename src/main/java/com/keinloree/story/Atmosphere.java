package com.keinloree.story;

import com.keinloree.net.AtmosphereS2C;
import com.keinloree.net.Net;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Atmosfera inquietante (tinte verde del cielo y niebla). Se guarda en Campaign para reenviarla al reconectar. */
public final class Atmosphere {
    private Atmosphere() {}

    /** Fija la atmosfera para todos los jugadores del nivel. */
    public static void set(ServerLevel overworld, float tint, float fog, int flags) {
        Campaign c = Campaign.get(overworld);
        c.atmoTint = clamp01(tint);
        c.atmoFog = clamp01(fog);
        c.atmoFlags = flags;
        c.setDirty();
        Net.toAll(overworld, new AtmosphereS2C(c.atmoTint, c.atmoFog, c.atmoFlags));
    }

    /** Reenvia la atmosfera guardada a un jugador (al entrar). */
    public static void sendTo(ServerPlayer player, Campaign c) {
        if (c.atmoTint != 0 || c.atmoFog != 0 || c.atmoFlags != 0) {
            Net.toPlayer(player, new AtmosphereS2C(c.atmoTint, c.atmoFog, c.atmoFlags));
        }
    }

    private static float clamp01(float v) {
        return Math.max(0.0F, Math.min(1.0F, v));
    }
}
