package com.ricardo.jugador2.ai;

/**
 * Foto del estado del NPC que usa el cerebro para elegir tarea.
 * {@code hostilesWithinRadius} ya viene contado con el radio de detección de la configuración.
 */
public record NpcSnapshot(
        float health,
        int food,
        boolean hasFood,
        int hostilesWithinRadius,
        boolean hasActiveOrder,
        boolean night,
        boolean hasBed,
        int logs,
        int stone,
        int coal,
        boolean toolsMissing,
        boolean inventoryNearlyFull) {}
