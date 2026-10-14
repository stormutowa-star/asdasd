package com.ssj8.client;

import com.ssj8.network.KiPacket;
import com.ssj8.network.ScanResultPacket;
import com.ssj8.network.StatePacket;
import net.minecraft.client.Minecraft;

public final class ClientPackets {
    private ClientPackets() {
    }

    public static void onState(StatePacket msg) {
        ClientState cs = ClientState.getOrCreate(msg.entityId);
        float now = ClientState.now(0.0f);
        if (cs.mode != msg.mode || cs.form != msg.form) {
            cs.modeStart = now;
        }
        if (cs.mode != 1 && msg.mode == 1) {
            cs.chargeStart = now;
        }
        if (cs.mode == 1 && msg.mode != 1) {
            cs.chargeEnd = now;
        }
        cs.form = msg.form;
        cs.mode = msg.mode;
        if (msg.form && msg.fresh) {
            cs.growStart = now;
        } else if (!msg.form) {
            cs.growStart = -100000.0f;
        }
    }

    public static void onKi(KiPacket msg) {
        ClientState.localKi = msg.ki;
    }

    public static void onScan(ScanResultPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        Ssj8Scan.setResult(msg.entries, mc.player != null ? mc.player.tickCount : 0);
    }
}
