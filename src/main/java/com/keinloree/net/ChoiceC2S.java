package com.keinloree.net;

import com.keinloree.story.ScenePlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

/** Cliente a servidor: el heroe eligio una opcion. */
public record ChoiceC2S(String id, String key) {

    public static void encode(ChoiceC2S m, FriendlyByteBuf b) {
        b.writeUtf(m.id, 64);
        b.writeUtf(m.key, 64);
    }

    public static ChoiceC2S decode(FriendlyByteBuf b) {
        return new ChoiceC2S(b.readUtf(64), b.readUtf(64));
    }

    public static void handle(ChoiceC2S m, CustomPayloadEvent.Context ctx) {
        ServerPlayer sender = ctx.getSender();
        if (sender != null) {
            ScenePlayer.onChoice(sender, m.id, m.key);
        }
        ctx.setPacketHandled(true);
    }
}
