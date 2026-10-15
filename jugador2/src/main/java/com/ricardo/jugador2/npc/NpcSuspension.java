package com.ricardo.jugador2.npc;

/** Pausa del cerebro del NPC tras una excepción: el servidor sigue, el NPC no actúa durante TICKS ticks. */
public final class NpcSuspension {
    public static final int TICKS = 200;

    private long suspendedUntil;

    public void suspend(long currentTick) {
        suspendedUntil = currentTick + TICKS;
    }

    public boolean isSuspended(long currentTick) {
        return currentTick < suspendedUntil;
    }
}
