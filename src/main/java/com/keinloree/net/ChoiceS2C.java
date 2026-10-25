package com.keinloree.net;

import com.keinloree.client.net.ClientPackets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Servidor a cliente: abre una eleccion. El servidor la reenvia cada pocos segundos mientras siga
 * pendiente; el cliente debe ignorarla si ya tiene abierta la misma eleccion (mismo id).
 */
public record ChoiceS2C(String id, String prompt, List<String> keys, List<String> texts) {

    public static void encode(ChoiceS2C m, FriendlyByteBuf b) {
        b.writeUtf(m.id, 64);
        b.writeUtf(m.prompt, 1024);
        b.writeVarInt(m.keys.size());
        for (int i = 0; i < m.keys.size(); i++) {
            b.writeUtf(m.keys.get(i), 64);
            b.writeUtf(m.texts.get(i), 1024);
        }
    }

    public static ChoiceS2C decode(FriendlyByteBuf b) {
        String id = b.readUtf(64);
        String prompt = b.readUtf(1024);
        int n = b.readVarInt();
        List<String> keys = new ArrayList<>(n);
        List<String> texts = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            keys.add(b.readUtf(64));
            texts.add(b.readUtf(1024));
        }
        return new ChoiceS2C(id, prompt, keys, texts);
    }

    public static void handle(ChoiceS2C m, CustomPayloadEvent.Context ctx) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPackets.onChoice(m);
        }
        ctx.setPacketHandled(true);
    }
}
