package com.keinloree.net;

import com.keinloree.ship.DestroyerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Cliente a servidor: mandos del piloto del destructor, cada tick (WP6 lo envia).
 * forward -1..1 (W/S), turn -1..1 (positivo = a la derecha, el yaw aumenta), lift -1..1 (subir/bajar),
 * buttons: 1 disparo principal, 2 lanzar cazas, 4 impulso, 8 soltar el timon.
 */
public record ShipInputC2S(float forward, float turn, float lift, int buttons) {

    public static void encode(ShipInputC2S m, FriendlyByteBuf b) {
        b.writeFloat(m.forward);
        b.writeFloat(m.turn);
        b.writeFloat(m.lift);
        b.writeVarInt(m.buttons);
    }

    public static ShipInputC2S decode(FriendlyByteBuf b) {
        return new ShipInputC2S(b.readFloat(), b.readFloat(), b.readFloat(), b.readVarInt());
    }

    public static void handle(ShipInputC2S m, CustomPayloadEvent.Context ctx) {
        ServerPlayer sender = ctx.getSender();
        if (sender != null && sender.getVehicle() instanceof DestroyerEntity ship
                && Float.isFinite(m.forward) && Float.isFinite(m.turn) && Float.isFinite(m.lift)) {
            ship.input(sender, m.forward, m.turn, m.lift, m.buttons);
        }
        ctx.setPacketHandled(true);
    }
}
