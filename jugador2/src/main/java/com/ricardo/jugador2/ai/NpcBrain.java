package com.ricardo.jugador2.ai;

import com.ricardo.jugador2.npc.NpcSettings;

/** Elige la tarea del NPC por prioridad. Función pura: el mismo estado da siempre la misma tarea. */
public final class NpcBrain {
    private static final int WOOD_TARGET = 16;
    private static final int STONE_TARGET = 32;
    private static final int COAL_TARGET = 16;

    private NpcBrain() {
    }

    public static TaskKind choose(NpcSnapshot s, NpcSettings cfg) {
        if (s.health() < cfg.fleeHealthThreshold() && s.hostilesWithinRadius() > 0) {
            return TaskKind.FLEE;
        }
        if (s.food() < cfg.eatHungerThreshold() && s.hasFood()) {
            return TaskKind.EAT;
        }
        if (s.hostilesWithinRadius() > 0) {
            return TaskKind.DEFEND;
        }
        if (s.hasActiveOrder()) {
            return TaskKind.ORDER;
        }
        // Va antes de recolectar: con el inventario lleno el NPC deja de recoger (sección 9 del spec).
        if (s.inventoryNearlyFull()) {
            return TaskKind.STORE;
        }
        if (s.logs() < WOOD_TARGET) {
            return TaskKind.GATHER_WOOD;
        }
        if (s.stone() < STONE_TARGET) {
            return TaskKind.GATHER_STONE;
        }
        if (s.coal() < COAL_TARGET) {
            return TaskKind.GATHER_COAL;
        }
        if (s.toolsMissing()) {
            return TaskKind.CRAFT;
        }
        if (s.night() && s.hasBed()) {
            return TaskKind.SLEEP;
        }
        return TaskKind.GATHER_WOOD;
    }
}
