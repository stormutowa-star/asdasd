package com.storm.greatsword;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Client -> server intent packet. The server validates the held item and cooldowns before acting.
 */
public class WeaponActionPacket {
    public enum Action {
        DRAW,
        ATTACK,
        TACKLE
    }

    private final Action action;

    public WeaponActionPacket(Action action) {
        this.action = action;
    }

    public static void encode(WeaponActionPacket message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.action.ordinal());
    }

    public static WeaponActionPacket decode(FriendlyByteBuf buffer) {
        int index = buffer.readUnsignedByte();
        Action[] values = Action.values();
        return new WeaponActionPacket(values[Math.min(index, values.length - 1)]);
    }

    public static void handle(WeaponActionPacket message, CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || player.isSpectator() || !player.isAlive()) {
            return;
        }

        switch (message.action) {
            case DRAW -> GreatswordActions.toggleDraw(player);
            case ATTACK -> GreatswordActions.attack(player);
            case TACKLE -> GreatswordActions.tackle(player);
        }
    }
}
