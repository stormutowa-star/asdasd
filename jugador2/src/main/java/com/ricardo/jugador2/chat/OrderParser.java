package com.ricardo.jugador2.chat;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** Interpreta un mensaje de chat como orden para el NPC. Vacío si el mensaje no va dirigido a él. */
public final class OrderParser {
    private OrderParser() {
    }

    public static Optional<ParsedOrder> parse(String npcName, String message) {
        int comma = message.indexOf(',');
        if (comma < 0 || !message.substring(0, comma).trim().equalsIgnoreCase(npcName)) {
            return Optional.empty();
        }
        String[] words = message.substring(comma + 1).trim().split("\\s+");
        String verb = words[0].toLowerCase(Locale.ROOT);

        if (verb.equals("ven") && words.length == 1) {
            return Optional.of(new Come());
        }
        if (verb.equals("para") && words.length == 1) {
            return Optional.of(new Stop());
        }
        if (verb.equals("sigue") && words.length == 3 && words[1].equalsIgnoreCase("a")) {
            return Optional.of(new Follow(words[2]));
        }
        if (verb.equals("ve") && words.length == 5 && words[1].equalsIgnoreCase("a")) {
            return Optional.of(parseGoTo(words[2], words[3], words[4]));
        }
        if (verb.equals("mina") && words.length >= 2) {
            String block = String.join(" ", Arrays.copyOfRange(words, 1, words.length));
            return Optional.of(new Mine(block.toLowerCase(Locale.ROOT)));
        }
        return Optional.of(new Help());
    }

    private static ParsedOrder parseGoTo(String x, String y, String z) {
        try {
            return new GoTo(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
        } catch (NumberFormatException e) {
            return new Help();
        }
    }
}
