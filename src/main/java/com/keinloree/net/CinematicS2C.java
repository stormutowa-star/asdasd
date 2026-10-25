package com.keinloree.net;

import com.keinloree.client.net.ClientPackets;
import com.keinloree.story.Shot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Servidor a cliente: lista de planos de una cinematica. flags: 1 saltable. */
public record CinematicS2C(List<Shot> shots, int flags) {
    public static final int SALTABLE = 1;

    public static void encode(CinematicS2C m, FriendlyByteBuf b) {
        b.writeVarInt(m.shots.size());
        for (Shot s : m.shots) {
            s.write(b);
        }
        b.writeVarInt(m.flags);
    }

    public static CinematicS2C decode(FriendlyByteBuf b) {
        int n = b.readVarInt();
        List<Shot> shots = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            shots.add(Shot.read(b));
        }
        return new CinematicS2C(shots, b.readVarInt());
    }

    public static void handle(CinematicS2C m, CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPackets.onCinematic(m);
        }
        ctx.setPacketHandled(true);
    }
}
