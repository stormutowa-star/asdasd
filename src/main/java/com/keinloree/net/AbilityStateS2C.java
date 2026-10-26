package com.keinloree.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Servidor a cliente: tecnicas desbloqueadas, recargas restantes (ticks), carga de la Hora Cero y si esta corrompida. */
public record AbilityStateS2C(int unlocked, int[] cooldowns, int charge, boolean corrupted) {
    public static void encode(AbilityStateS2C m, FriendlyByteBuf b) {
        b.writeVarInt(m.unlocked);
        b.writeVarInt(m.cooldowns.length);
        for (int c : m.cooldowns) {
            b.writeVarInt(c);
        }
        b.writeVarInt(m.charge);
        b.writeBoolean(m.corrupted);
    }

    public static AbilityStateS2C decode(FriendlyByteBuf b) {
        int u = b.readVarInt();
        int n = Math.min(16, b.readVarInt());
        int[] cd = new int[n];
        for (int i = 0; i < n; i++) {
            cd[i] = b.readVarInt();
        }
        return new AbilityStateS2C(u, cd, b.readVarInt(), b.readBoolean());
    }

    public static void handle(AbilityStateS2C m, CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.keinloree.client.net.ClientPackets.onAbilityState(m);
        }
        ctx.setPacketHandled(true);
    }
}
