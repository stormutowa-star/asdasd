package com.keinloree.net;

import com.keinloree.client.net.ClientPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Servidor a cliente: tarjeta de capitulo. */
public record TitleCardS2C(String title, String subtitle, int color, int ticks) {

    public static void encode(TitleCardS2C m, FriendlyByteBuf b) {
        b.writeUtf(m.title, 256);
        b.writeUtf(m.subtitle, 256);
        b.writeInt(m.color);
        b.writeVarInt(m.ticks);
    }

    public static TitleCardS2C decode(FriendlyByteBuf b) {
        return new TitleCardS2C(b.readUtf(256), b.readUtf(256), b.readInt(), b.readVarInt());
    }

    public static void handle(TitleCardS2C m, CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPackets.onTitleCard(m);
        }
        ctx.setPacketHandled(true);
    }
}
