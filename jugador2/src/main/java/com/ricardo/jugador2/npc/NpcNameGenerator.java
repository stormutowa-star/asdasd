package com.ricardo.jugador2.npc;

import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/** Genera nombres de usuario únicos y originales para el NPC, a partir de una semilla del mundo. */
public final class NpcNameGenerator {
    // Sílabas inventadas: no son nombres de personajes conocidos ni de cuentas reales.
    private static final String[] SYLLABLES = {
            "ky", "ra", "vel", "zor", "ni", "thal", "mor", "quen", "ash", "elu",
            "dra", "vyn", "osk", "tev", "ilo", "brim", "sar", "yun", "fen", "gorm"
    };
    private static final int MAX_LENGTH = 16;

    private NpcNameGenerator() {
    }

    /** Devuelve un nombre válido para un usuario de Minecraft que no esté en {@code taken}, sin distinguir mayúsculas. */
    public static String generate(long seed, Set<String> taken) {
        Random random = new Random(seed);
        int syllables = 2 + random.nextInt(2);
        StringBuilder base = new StringBuilder();
        for (int i = 0; i < syllables; i++) {
            base.append(SYLLABLES[random.nextInt(SYLLABLES.length)]);
        }
        String name = capitalize(truncate(base.toString(), MAX_LENGTH));

        Set<String> lowerTaken = toLowerCase(taken);
        if (!lowerTaken.contains(name.toLowerCase(Locale.ROOT))) {
            return name;
        }
        for (int n = 2; ; n++) {
            String suffix = "_" + n;
            String candidate = truncate(name, MAX_LENGTH - suffix.length()) + suffix;
            if (!lowerTaken.contains(candidate.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
    }

    private static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static Set<String> toLowerCase(Set<String> names) {
        Set<String> lower = new HashSet<>();
        for (String name : names) {
            lower.add(name.toLowerCase(Locale.ROOT));
        }
        return lower;
    }
}
