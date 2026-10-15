package com.ricardo.jugador2.npc;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcNameGeneratorTest {
    @Test
    void generatesValidUsernames() {
        for (long seed = 0; seed < 500; seed++)
            assertTrue(NpcNameGenerator.generate(seed, Set.of()).matches("[A-Za-z0-9_]{3,16}"));
    }

    @Test
    void sameSeedSameName() {
        assertEquals(NpcNameGenerator.generate(42L, Set.of()), NpcNameGenerator.generate(42L, Set.of()));
    }

    @Test
    void avoidsTakenNamesIgnoringCase() {
        String first = NpcNameGenerator.generate(42L, Set.of());
        String second = NpcNameGenerator.generate(42L, Set.of(first.toLowerCase(Locale.ROOT)));
        assertNotEquals(first.toLowerCase(Locale.ROOT), second.toLowerCase(Locale.ROOT));
        assertTrue(second.matches("[A-Za-z0-9_]{3,16}"));
    }

    @Test
    void survivesLargeTakenSet() {
        Set<String> taken = new HashSet<>();
        for (long seed = 0; seed < 2000; seed++)
            taken.add(NpcNameGenerator.generate(seed, Set.of()).toLowerCase(Locale.ROOT));
        String name = NpcNameGenerator.generate(7L, taken);
        assertFalse(taken.contains(name.toLowerCase(Locale.ROOT)));
        assertTrue(name.matches("[A-Za-z0-9_]{3,16}"));
    }
}
