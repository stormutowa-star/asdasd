package com.keinloree.net;

import com.keinloree.client.net.ClientPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Servidor a cliente: objetivo actual. Objetivo vacio = ocultar el panel. max &gt; 0 muestra barra. */
public record HudS2C(String objective, boolean hasMarker, BlockPos marker, String label, int cur, int max, int color) {

    public static void encode(HudS2C m, FriendlyByteBuf b) {
        b.writeUtf(m.objective, 512);
        b.writeBoolean(m.hasMarker);
        if (m.hasMarker) {
            b.writeBlockPos(m.marker);
        }
        b.writeUtf(m.label, 128);
        b.writeVarInt(m.cur);
        b.writeVarInt(m.max);
        b.writeInt(m.color);
    }

    public static HudS2C decode(FriendlyByteBuf b) {
        String objective = b.readUtf(512);
        boolean has = b.readBoolean();
        BlockPos marker = has ? b.readBlockPos() : BlockPos.ZERO;
        return new HudS2C(objective, has, marker, b.readUtf(128), b.readVarInt(), b.readVarInt(), b.readInt());
    }

    public static void handle(HudS2C m, CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPackets.onHud(m);
        }
        ctx.setPacketHandled(true);
    }
}
