package com.keinloree.net;

import com.keinloree.client.net.ClientPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Servidor a cliente: tinte verde del cielo/niebla (0..1), niebla extra (0..1). flags: 1 nexo unido, 2 hora perdida. */
public record AtmosphereS2C(float tint, float fog, int flags) {
    public static final int NEXO_UNIDO = 1;
    public static final int HORA_PERDIDA = 2;

    public static void encode(AtmosphereS2C m, FriendlyByteBuf b) {
        b.writeFloat(m.tint);
        b.writeFloat(m.fog);
        b.writeVarInt(m.flags);
    }

    public static AtmosphereS2C decode(FriendlyByteBuf b) {
        return new AtmosphereS2C(b.readFloat(), b.readFloat(), b.readVarInt());
    }

    public static void handle(AtmosphereS2C m, CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPackets.onAtmosphere(m);
        }
        ctx.setPacketHandled(true);
    }
}
