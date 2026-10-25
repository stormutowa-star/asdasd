package com.keinloree.net;

import com.keinloree.client.net.ClientPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Servidor a cliente: efecto visual del motor de VFX (WP2). entityId -1 si no sigue a ninguna entidad. */
public record VfxS2C(int type, double x, double y, double z, double dx, double dy, double dz,
                     int color, float scale, int ticks, int entityId, float extra) {

    public static void encode(VfxS2C m, FriendlyByteBuf b) {
        b.writeVarInt(m.type);
        b.writeDouble(m.x);
        b.writeDouble(m.y);
        b.writeDouble(m.z);
        b.writeDouble(m.dx);
        b.writeDouble(m.dy);
        b.writeDouble(m.dz);
        b.writeInt(m.color);
        b.writeFloat(m.scale);
        b.writeVarInt(m.ticks);
        b.writeVarInt(m.entityId + 1);
        b.writeFloat(m.extra);
    }

    public static VfxS2C decode(FriendlyByteBuf b) {
        return new VfxS2C(b.readVarInt(), b.readDouble(), b.readDouble(), b.readDouble(),
                b.readDouble(), b.readDouble(), b.readDouble(),
                b.readInt(), b.readFloat(), b.readVarInt(), b.readVarInt() - 1, b.readFloat());
    }

    public static void handle(VfxS2C m, CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPackets.onVfx(m);
        }
        ctx.setPacketHandled(true);
    }
}
