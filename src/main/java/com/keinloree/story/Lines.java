package com.keinloree.story;

import net.minecraft.server.level.ServerLevel;

/** Lineas sueltas de dialogo desde fuera de una escena (combates, eventos). */
public final class Lines {
    private Lines() {
    }

    /** Envia una linea del hablante (id del guion: KEIN, RAZAKO, ALBA...) a todos los jugadores. */
    public static void say(ServerLevel level, String speaker, String text) {
        ServerLevel ow = level.getServer().overworld();
        ScenePlayer.say(ow, Campaign.get(ow), speaker, text, -1);
    }
}
