package com.keinloree.net;

import com.keinloree.story.Cinematics;
import com.keinloree.story.ScenePlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

/** Cliente a servidor: el heroe quiere adelantar la linea (o saltar la cinematica si es saltable). */
public record SkipC2S() {

    public static void encode(SkipC2S m, FriendlyByteBuf b) {
    }

    public static SkipC2S decode(FriendlyByteBuf b) {
        return new SkipC2S();
    }

    public static void handle(SkipC2S m, CustomPayloadEvent.Context ctx) {
        ServerPlayer sender = ctx.getSender();
        if (sender != null) {
            if (Cinematics.isActive(sender)) {
                Cinematics.skip(sender);
            } else {
                ScenePlayer.skip(sender);
            }
        }
        ctx.setPacketHandled(true);
    }
}
