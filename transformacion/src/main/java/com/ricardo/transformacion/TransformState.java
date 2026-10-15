package com.ricardo.transformacion;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Estado de la transformacion de cada jugador. Se guarda en los datos persistentes del jugador. */
public final class TransformState {
    private static final String KEY = "transformacion:active";

    private TransformState() {
    }

    public static boolean isTransformed(Player player) {
        return player.getPersistentData().getBoolean(KEY);
    }

    public static void set(ServerPlayer player, boolean active) {
        if (active) {
            player.getPersistentData().putBoolean(KEY, true);
        } else {
            player.getPersistentData().remove(KEY);
        }
        TransformNetwork.sendToTrackers(player, active);
    }

    /** Conserva la transformacion al morir o al cambiar de dimension (el jugador se clona). */
    public static void copy(Player from, Player to) {
        if (isTransformed(from)) {
            to.getPersistentData().putBoolean(KEY, true);
        }
    }
}
