package com.keinloree.net;

import com.keinloree.ability.Techniques;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

/** Cliente a servidor: usar una tecnica de RareIri (action 0 + id), dash (1) o doble salto (2). */
public record AbilityC2S(int action, int technique) {
    public static void encode(AbilityC2S m, FriendlyByteBuf b) {
        b.writeVarInt(m.action);
        b.writeVarInt(m.technique);
    }

    public static AbilityC2S decode(FriendlyByteBuf b) {
        return new AbilityC2S(b.readVarInt(), b.readVarInt());
    }

    public static void handle(AbilityC2S m, CustomPayloadEvent.Context ctx) {
        ServerPlayer p = ctx.getSender();
        if (p != null) {
            Techniques.onRequest(p, m.action, m.technique);
        }
        ctx.setPacketHandled(true);
    }
}
