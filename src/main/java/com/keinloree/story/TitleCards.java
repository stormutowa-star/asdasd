package com.keinloree.story;

import com.keinloree.net.Net;
import com.keinloree.net.TitleCardS2C;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Tarjetas de capitulo (servidor). */
public final class TitleCards {
    public static final int DEFAULT_TICKS = 100;

    private TitleCards() {}

    public static void send(ServerPlayer player, String title, String subtitle, int color, int ticks) {
        Net.toPlayer(player, new TitleCardS2C(title, subtitle, color, ticks));
    }

    public static void sendAll(ServerLevel level, String title, String subtitle, int color, int ticks) {
        Net.toAll(level, new TitleCardS2C(title, subtitle, color, ticks));
    }
}
