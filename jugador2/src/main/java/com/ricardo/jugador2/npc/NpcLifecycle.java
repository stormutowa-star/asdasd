package com.ricardo.jugador2.npc;

/** Estado del NPC entre partidas: activo o no, vivo o en espera de reaparecer. Función pura, sin Minecraft. */
public final class NpcLifecycle {
    public enum Phase { INACTIVE, ALIVE, DEAD }

    public enum Action { NONE, RESPAWN }

    private final int respawnDelayTicks;
    private Phase phase;
    private int ticksUntilRespawn;

    public NpcLifecycle(Phase phase, int ticksUntilRespawn, int respawnDelayTicks) {
        this.phase = phase;
        this.ticksUntilRespawn = ticksUntilRespawn;
        this.respawnDelayTicks = respawnDelayTicks;
    }

    public Phase phase() {
        return phase;
    }

    public int ticksUntilRespawn() {
        return ticksUntilRespawn;
    }

    /** Pasa de inactivo a vivo. En los demás estados no cambia nada. */
    public void activate() {
        if (phase == Phase.INACTIVE) {
            phase = Phase.ALIVE;
        }
    }

    public void deactivate() {
        phase = Phase.INACTIVE;
    }

    public void onDeath() {
        if (phase == Phase.ALIVE) {
            phase = Phase.DEAD;
            ticksUntilRespawn = respawnDelayTicks;
        }
    }

    /** Avanza un tick. Devuelve RESPAWN en el tick en que el NPC debe reaparecer. */
    public Action tick() {
        if (phase != Phase.DEAD) {
            return Action.NONE;
        }
        ticksUntilRespawn = Math.max(0, ticksUntilRespawn - 1);
        if (ticksUntilRespawn > 0) {
            return Action.NONE;
        }
        phase = Phase.ALIVE;
        return Action.RESPAWN;
    }
}
