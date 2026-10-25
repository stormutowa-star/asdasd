package com.keinloree.net;

import com.keinloree.client.net.ClientPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Servidor a cliente: una linea de dialogo. flags: 1 narrador, 2 susurro, 4 glitch, 8 limpiar. */
public record DialogueS2C(String speaker, String name, int color, String text, int ticks, int flags) {
    public static final int NARRADOR = 1;
    public static final int SUSURRO = 2;
    public static final int GLITCH = 4;
    public static final int LIMPIAR = 8;

    public static void encode(DialogueS2C m, FriendlyByteBuf b) {
        b.writeUtf(m.speaker, 64);
        b.writeUtf(m.name, 128);
        b.writeInt(m.color);
        b.writeUtf(m.text, 4096);
        b.writeVarInt(m.ticks);
        b.writeVarInt(m.flags);
    }

    public static DialogueS2C decode(FriendlyByteBuf b) {
        return new DialogueS2C(b.readUtf(64), b.readUtf(128), b.readInt(), b.readUtf(4096), b.readVarInt(), b.readVarInt());
    }

    public static void handle(DialogueS2C m, CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPackets.onDialogue(m);
        }
        ctx.setPacketHandled(true);
    }
}
