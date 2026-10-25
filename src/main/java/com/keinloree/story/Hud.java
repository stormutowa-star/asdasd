package com.keinloree.story;

import com.keinloree.net.HudS2C;
import com.keinloree.net.Net;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/** HUD de objetivo (servidor). Objetivo vacio oculta el panel. */
public final class Hud {
    private Hud() {}

    public static void send(ServerPlayer player, String objective, @Nullable BlockPos marker, String label,
                            int cur, int max, int color) {
        Net.toPlayer(player, new HudS2C(objective == null ? "" : objective, marker != null,
                marker == null ? BlockPos.ZERO : marker, label == null ? "" : label, cur, max, color));
    }

    public static void clear(ServerPlayer player) {
        send(player, "", null, "", 0, 0, 0xFFFFFF);
    }
}
