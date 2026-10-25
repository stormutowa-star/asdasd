package com.keinloree.story;

import com.keinloree.net.Net;
import com.keinloree.net.WhisperS2C;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Susurros de omnisciencia (servidor). El cliente pone el texto y el sonido kein_whisper. */
public final class Whisper {
    private Whisper() {}

    public static void send(ServerPlayer player, String text) {
        Net.toPlayer(player, new WhisperS2C(text));
    }

    public static void sendAll(ServerLevel level, String text) {
        Net.toAll(level, new WhisperS2C(text));
    }
}
