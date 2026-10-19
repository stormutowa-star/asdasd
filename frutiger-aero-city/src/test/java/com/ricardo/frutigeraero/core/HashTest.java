package com.ricardo.frutigeraero.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class HashTest {
    @Test void sameInputsSameOutput() {
        assertEquals(Hash.of(42L, 1, 2, 3), Hash.of(42L, 1, 2, 3));
    }
    @Test void differentInputsDiffer() {
        assertNotEquals(Hash.of(42L, 1, 2, 3), Hash.of(42L, 1, 2, 4));
        assertNotEquals(Hash.of(42L, 1, 2, 3), Hash.of(43L, 1, 2, 3));
        assertNotEquals(Hash.of(42L, 1, 2), Hash.of(42L, 2, 1));
    }
    @Test void unitInRangeAndUniform() {
        double sum = 0;
        for (int i = 0; i < 10000; i++) {
            double u = Hash.unit(Hash.of(7L, i));
            assertTrue(u >= 0.0 && u < 1.0);
            sum += u;
        }
        double mean = sum / 10000;
        assertTrue(mean > 0.45 && mean < 0.55, "mean=" + mean);
    }
    @Test void noiseInRangeAndSmooth() {
        for (int i = 0; i < 1000; i++) {
            double x = -50 + (i * 0.1037) % 100, z = -50 + (i * 0.0713) % 100;
            double n = Hash.noise01(99L, x, z);
            assertTrue(n >= 0.0 && n <= 1.0);
            assertTrue(Math.abs(n - Hash.noise01(99L, x + 0.01, z)) < 0.05);
        }
        assertEquals(Hash.noise01(5L, -3.5, -7.25), Hash.noise01(5L, -3.5, -7.25));
    }
}
