package com.ricardo.jugador2.npc;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

/** Conexión sin red: el NPC no tiene cliente, así que no envía nada a ningún lado. */
public final class FakeConnection extends Connection {
    public FakeConnection() {
        super(PacketFlow.SERVERBOUND);
    }

    @Override
    public void send(Packet<?> packet) {
    }

    @Override
    public void send(Packet<?> packet, PacketSendListener listener) {
    }
}
