package com.keinloree.net;

import com.keinloree.client.net.ClientPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Servidor a cliente: susurro de omnisciencia (texto verde que tiembla 4 s). */
public record WhisperS2C(String text) {

    public static void encode(WhisperS2C m, FriendlyByteBuf b) {
        b.writeUtf(m.text, 1024);
    }

    public static WhisperS2C decode(FriendlyByteBuf b) {
        return new WhisperS2C(b.readUtf(1024));
    }

    public static void handle(WhisperS2C m, CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPackets.onWhisper(m);
        }
        ctx.setPacketHandled(true);
    }
}
