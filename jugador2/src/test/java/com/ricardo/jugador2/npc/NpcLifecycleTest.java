package com.ricardo.jugador2.npc;

import com.ricardo.jugador2.npc.NpcLifecycle.Action;
import com.ricardo.jugador2.npc.NpcLifecycle.Phase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NpcLifecycleTest {
    @Test
    void deathThenRespawnAfterDelay() {
        NpcLifecycle lc = new NpcLifecycle(Phase.ALIVE, 0, 100);
        lc.onDeath();
        for (int i = 0; i < 99; i++) {
            assertEquals(Action.NONE, lc.tick());
        }
        assertEquals(Action.RESPAWN, lc.tick());
        assertEquals(Phase.ALIVE, lc.phase());
    }

    @Test
    void restoreWhileDeadKeepsRemainingTicks() {
        NpcLifecycle lc = new NpcLifecycle(Phase.DEAD, 30, 100);
        for (int i = 0; i < 29; i++) {
            assertEquals(Action.NONE, lc.tick());
        }
        assertEquals(Action.RESPAWN, lc.tick());
    }

    @Test
    void activateTwiceKeepsState() {
        NpcLifecycle alive = new NpcLifecycle(Phase.ALIVE, 0, 100);
        alive.activate();
        assertEquals(Phase.ALIVE, alive.phase());

        NpcLifecycle dead = new NpcLifecycle(Phase.DEAD, 40, 100);
        dead.activate();
        assertEquals(40, dead.ticksUntilRespawn());
    }

    @Test
    void deactivateStopsRespawn() {
        NpcLifecycle lc = new NpcLifecycle(Phase.DEAD, 5, 100);
        lc.deactivate();
        assertEquals(Action.NONE, lc.tick());
        assertEquals(Phase.INACTIVE, lc.phase());
    }
}
