package com.ricardo.jugador2.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiningBudgetTest {
    @Test
    void slowerBlocksGetMoreTicks() {
        // 40 de margen más tres veces el tiempo que tarda el bloque: medio bloque por tick tarda 2 ticks, un cuarto, 4.
        assertEquals(46, MiningBudget.ticksFor(0.5F));
        assertEquals(52, MiningBudget.ticksFor(0.25F));
    }

    @Test
    void blockThatNeverProgressesGetsNoTicks() {
        assertEquals(0, MiningBudget.ticksFor(0.0F));
    }

    @Test
    void spendingPastTheLimitIsReported() {
        MiningBudget budget = new MiningBudget(0.5F);
        for (int tick = 0; tick < 46; tick++) {
            assertFalse(budget.spend());
        }
        assertTrue(budget.spend());
    }
}
