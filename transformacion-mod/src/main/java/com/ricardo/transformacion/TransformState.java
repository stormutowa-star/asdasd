package com.ricardo.transformacion;

import com.ricardo.transformacion.TransformNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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

    public static void copy(Player from, Player to) {
        if (TransformState.isTransformed(from)) {
            to.getPersistentData().putBoolean(KEY, true);
        }
    }
}

