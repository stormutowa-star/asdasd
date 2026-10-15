package com.ricardo.jugador2.chat;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderParserTest {
    @Test
    void comeOrder() {
        assertEquals(Optional.of(new Come()), OrderParser.parse("Ryvex", "Ryvex, ven"));
    }

    @Test
    void followIgnoresCase() {
        assertEquals(Optional.of(new Follow("Steve")), OrderParser.parse("Ryvex", "ryvex, sigue a Steve"));
    }

    @Test
    void gotoAcceptsNegativeCoordinates() {
        assertEquals(Optional.of(new GoTo(100, 64, -20)), OrderParser.parse("Ryvex", "Ryvex, ve a 100 64 -20"));
    }

    @Test
    void mineKeepsBlockText() {
        assertEquals(Optional.of(new Mine("piedra")), OrderParser.parse("Ryvex", "Ryvex, mina piedra"));
    }

    @Test
    void stopOrder() {
        assertEquals(Optional.of(new Stop()), OrderParser.parse("Ryvex", "Ryvex, para"));
    }

    @Test
    void unknownCommandReturnsHelp() {
        assertEquals(Optional.of(new Help()), OrderParser.parse("Ryvex", "Ryvex, baila"));
    }

    @Test
    void gotoWithNonNumbersReturnsHelp() {
        assertEquals(Optional.of(new Help()), OrderParser.parse("Ryvex", "Ryvex, ve a x y z"));
    }

    @Test
    void unknownBlockNameIsKeptAsText() {
        assertEquals(Optional.of(new Mine("xyz")), OrderParser.parse("Ryvex", "Ryvex, mina xyz"));
    }

    @Test
    void otherNameIsIgnored() {
        assertTrue(OrderParser.parse("Ryvex", "Otro, ven").isEmpty());
    }

    @Test
    void similarNameDoesNotTrigger() {
        assertTrue(OrderParser.parse("Ryvex", "Ryvexito, ven").isEmpty());
    }

    @Test
    void missingCommaIsIgnored() {
        assertTrue(OrderParser.parse("Ryvex", "Ryvex ven").isEmpty());
    }
}
