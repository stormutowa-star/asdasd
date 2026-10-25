package com.keinloree.story;

import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;

/** Acciones del guion (@accion nombre args...), registradas por nombre. */
public final class ScriptActions {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, ScriptAction> ACTIONS = new HashMap<>();
    private static final Set<String> WARNED = new HashSet<>();

    @FunctionalInterface
    public interface ScriptAction {
        void run(StoryCtx ctx, String[] args);
    }

    private ScriptActions() {}

    public static void register(String name, ScriptAction action) {
        ACTIONS.put(name, action);
    }

    public static boolean has(String name) {
        return ACTIONS.containsKey(name);
    }

    /** Ejecuta la accion; si no existe avisa una sola vez y sigue. */
    public static void run(StoryCtx ctx, String name, String[] args) {
        ScriptAction a = ACTIONS.get(name);
        if (a == null) {
            if (WARNED.add(name)) {
                LOGGER.warn("[Guion] @accion '{}' no esta registrada (se ignora)", name);
            }
            return;
        }
        try {
            a.run(ctx, args);
        } catch (RuntimeException e) {
            LOGGER.error("[Guion] Fallo en @accion {}", name, e);
        }
    }
}
