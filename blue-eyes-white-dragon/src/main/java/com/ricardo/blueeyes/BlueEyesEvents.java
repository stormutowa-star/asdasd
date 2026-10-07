package com.ricardo.blueeyes;

import java.text.Normalizer;
import java.util.Locale;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;

/**
 * Tecnicas por chat (se pueden usar lejos de la carta):
 *  - "desactivar" / "regresa" / "vuelve a la carta": el dragon vuelve a su carta, que queda boca abajo.
 *  - "ojos azules" / "invoco" / "activar": las cartas boca abajo se vuelven a activar.
 */
public final class BlueEyesEvents {
    private BlueEyesEvents() {}

    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String text = normalize(event.getRawText());
        boolean off = text.contains("desactiv") || text.contains("regresa") || text.contains("vuelve a la carta");
        boolean on = !off && (text.contains("ojos azules") || text.contains("invoco") || text.contains("activar"));
        if ((!off && !on) || player.getServer() == null) {
            return;
        }
        player.getServer().execute(() -> {
            for (ServerLevel level : player.getServer().getAllLevels()) {
                for (FieldCardEntity card : level.getEntities(BlueEyesMod.FIELD_CARD.get(), c -> c.isOwnedBy(player))) {
                    if (off) {
                        card.deactivate(player);
                    } else {
                        card.activate(player);
                    }
                }
                if (off) {
                    // dragones cuya carta no esta cargada: vuelven igualmente
                    for (BlueEyesDragon dragon : level.getEntities(BlueEyesMod.DRAGON.get(), d -> d.isOwner(player) && !d.isRecalling())) {
                        dragon.recall();
                    }
                }
            }
        });
    }

    private static String normalize(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase(Locale.ROOT);
    }
}
