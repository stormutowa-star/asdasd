package com.ricardo.akagane;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AkaganeEvents {

    /** Los nombres de las tecnicas se pueden decir en el chat: "Arde, Akagane.", "Gyakuzan", "Bankai", "Guren Retsudan". */
    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String raw = event.getRawText();
        if (player.getServer() != null) {
            player.getServer().execute(() -> Techniques.onChat(player, raw));
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Techniques.forget(event.getEntity().getUUID());
    }
}
