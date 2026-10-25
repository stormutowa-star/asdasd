package com.keinloree.story;

import com.keinloree.net.DialogueS2C;
import java.util.Map;
import javax.annotation.Nullable;

/** Hablantes del guion: nombre visible, color y banderas de estilo. */
public record Speaker(String id, String name, int color, int flags) {
    public static final String PLAYER_ID = "JUGADOR";

    private static final Map<String, Speaker> TABLE = Map.ofEntries(
            entry("KEIN", "Kein Loree", 0x4CFF7A, 0),
            entry("RAZAKO", "Razako", 0xFF3B3B, 0),
            entry("ORREL", "Almirante Orrel", 0xC9B27A, 0),
            entry("OFICIAL", "Oficial", 0x9FD9A8, 0),
            entry("SOLDADO", "Soldado", 0x9FD9A8, 0),
            entry("ALBA", "Alba", 0xFFC861, 0),
            entry("CENIT", "Cénit", 0xFFF2A8, 0),
            entry("OCASO", "Ocaso", 0xC24CFF, 0),
            entry("MEDIANOCHE", "Medianoche", 0x6F7BFF, 0),
            entry("ECLIPSE", "Eclipse", 0xE8E8FF, 0),
            entry("VOZ", "???", 0xFFFFFF, DialogueS2C.GLITCH),
            entry("NARRADOR", "", 0xB8B8B8, DialogueS2C.NARRADOR),
            entry(PLAYER_ID, "", 0xFFFFFF, 0));

    private static Map.Entry<String, Speaker> entry(String id, String name, int color, int flags) {
        return Map.entry(id, new Speaker(id, name, color, flags));
    }

    @Nullable
    public static Speaker get(String id) {
        return TABLE.get(id);
    }
}
