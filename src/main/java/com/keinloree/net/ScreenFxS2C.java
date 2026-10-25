package com.keinloree.net;

import com.keinloree.client.net.ClientPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Servidor a cliente: efecto de pantalla (tipos en {@code ScreenFx}). */
public record ScreenFxS2C(int type, int ticks, float intensity, int color) {

    public static void encode(ScreenFxS2C m, FriendlyByteBuf b) {
        b.writeVarInt(m.type);
        b.writeVarInt(m.ticks);
        b.writeFloat(m.intensity);
        b.writeInt(m.color);
    }

    public static ScreenFxS2C decode(FriendlyByteBuf b) {
        return new ScreenFxS2C(b.readVarInt(), b.readVarInt(), b.readFloat(), b.readInt());
    }

    public static void handle(ScreenFxS2C m, CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPackets.onScreenFx(m);
        }
        ctx.setPacketHandled(true);
    }
}
