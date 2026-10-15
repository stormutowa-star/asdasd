package com.ricardo.jugador2.ai;

/**
 * Foto del estado del NPC que usa el cerebro para elegir tarea.
 * {@code hostilesWithinRadius} ya viene contado con el radio de detección de la configuración.
 * {@code hasPickaxe} es cualquier pico, de madera o mejor; {@code toolsMissing} es no tener pico de piedra o mejor.
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
        boolean hasPickaxe,
        boolean toolsMissing,
        boolean inventoryNearlyFull) {}
