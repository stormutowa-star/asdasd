package com.ricardo.jugador2.npc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcSettingsTest {
    @Test
    void defaultsMatchSpecSection10() {
        NpcSettings s = NpcSettings.defaults();
        assertTrue(s.enabled());
        assertTrue(s.spawnOnServerStart());
        assertEquals(10, s.aiDecisionIntervalTicks());
        assertEquals(6.0, s.hostileDetectionRadius());
        assertEquals(14, s.eatHungerThreshold());
        assertEquals(6.0f, s.fleeHealthThreshold());
        assertEquals(100, s.respawnDelayTicks());
        assertEquals(2000, s.pathSearchNodeLimit());
    }
}
