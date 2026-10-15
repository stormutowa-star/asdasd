package com.ricardo.jugador2.ai;

/**
 * Cuántos ticks puede tardar el NPC en romper un bloque. El margen fijo cubre los avisos perdidos y las pausas; lo demás
 * es tres veces el tiempo que el bloque tarda con la herramienta elegida. Pasado el límite, la tarea falla en vez de
 * quedarse minando para siempre.
 */
public final class MiningBudget {
    private static final int SLACK_TICKS = 40;
    private static final double EXPECTED_FACTOR = 3.0;

    private final int limit;
    private int spent;

    /** progressPerTick es la fracción del bloque que se rompe en cada tick con la herramienta actual. */
    public MiningBudget(float progressPerTick) {
        this.limit = ticksFor(progressPerTick);
    }

    /** Ticks permitidos para un bloque que avanza progressPerTick por tick. Cero si no avanza nunca. */
    public static int ticksFor(float progressPerTick) {
        if (progressPerTick <= 0.0F) {
            return 0;
        }
        return SLACK_TICKS + (int) Math.ceil(EXPECTED_FACTOR / progressPerTick);
    }

    /** Gasta un tick. Devuelve true si ya se ha pasado del límite. */
    public boolean spend() {
        spent++;
        return spent > limit;
    }
}
