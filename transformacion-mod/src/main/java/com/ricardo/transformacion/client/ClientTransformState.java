package com.ricardo.transformacion.client;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientTransformState {
    private static final Set<UUID> ACTIVE = ConcurrentHashMap.newKeySet();

    private ClientTransformState() {
    }

    public static void set(UUID player, boolean active) {
        if (active) {
            ACTIVE.add(player);
        } else {
            ACTIVE.remove(player);
        }
    }

    public static boolean isTransformed(UUID player) {
        return ACTIVE.contains(player);
    }

    public static void clear() {
        ACTIVE.clear();
    }
}

