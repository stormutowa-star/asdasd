package com.eternitywalker;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

/** Cliente -> servidor: botones de disparo del piloto (clic izquierdo = cañón central, clic derecho = cañones laterales). */
public record ControlPacket(boolean main, boolean side) {
    public static void encode(ControlPacket m, FriendlyByteBuf buf) {
        buf.writeBoolean(m.main);
        buf.writeBoolean(m.side);
    }

    public static ControlPacket decode(FriendlyByteBuf buf) {
        return new ControlPacket(buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(ControlPacket m, CustomPayloadEvent.Context ctx) {
        ServerPlayer sp = ctx.getSender();
        if (sp == null) return;
        if (sp.getVehicle() instanceof WalkerEntity w && w.getControllingPassenger() == sp) {
            w.fireMain = m.main;
            w.fireSide = m.side;
            w.lastControlTick = w.tickCount;
        }
    }
}
