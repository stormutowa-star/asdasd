package com.ricardo.jugador2.npc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcSuspensionTest {
    @Test
    void suspendedForExactly200Ticks() {
        NpcSuspension suspension = new NpcSuspension();
        suspension.suspend(1000);
        assertTrue(suspension.isSuspended(1199));
        assertFalse(suspension.isSuspended(1200));
    }
}
