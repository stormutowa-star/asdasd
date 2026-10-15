package com.ricardo.jugador2.npc;

/** Valores configurables del NPC, con los de la sección 10 del spec por defecto. */
public record NpcSettings(
        boolean enabled,
        boolean spawnOnServerStart,
        int aiDecisionIntervalTicks,
        double hostileDetectionRadius,
        int eatHungerThreshold,
        float fleeHealthThreshold,
        int respawnDelayTicks,
        int pathSearchNodeLimit) {

    public static NpcSettings defaults() {
        return new NpcSettings(true, true, 10, 6.0, 14, 6.0f, 100, 2000);
    }
}
