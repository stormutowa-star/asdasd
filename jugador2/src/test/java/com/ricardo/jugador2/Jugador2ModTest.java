package com.ricardo.jugador2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Jugador2ModTest {
    @Test
    void modIdIsJugador2() {
        assertEquals("jugador2", Jugador2Mod.MOD_ID);
    }
}
