package com.keinloree.net;

import com.keinloree.client.net.ClientPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Servidor a cliente: termina la cinematica y devuelve la camara al jugador. */
public record CinematicEndS2C() {

    public static void encode(CinematicEndS2C m, FriendlyByteBuf b) {
    }

    public static CinematicEndS2C decode(FriendlyByteBuf b) {
        return new CinematicEndS2C();
    }

    public static void handle(CinematicEndS2C m, CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPackets.onCinematicEnd(m);
        }
        ctx.setPacketHandled(true);
    }
}
