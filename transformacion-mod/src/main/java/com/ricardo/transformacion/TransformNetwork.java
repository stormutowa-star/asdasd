package com.ricardo.transformacion;

import com.ricardo.transformacion.TransformState;
import com.ricardo.transformacion.client.ClientTransformState;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class TransformNetwork {
    private static final SimpleChannel CHANNEL = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath("transformacion", "main")).networkProtocolVersion(1).simpleChannel();

    private TransformNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(StatePacket.class, 0).encoder(StatePacket::encode).decoder(StatePacket::decode).consumerMainThread(StatePacket::handle).add();
    }

    public static void sendTo(ServerPlayer receiver, Player subject) {
        CHANNEL.send(new StatePacket(subject.getUUID(), TransformState.isTransformed(subject)), PacketDistributor.PLAYER.with(receiver));
    }

    public static void sendToTrackers(ServerPlayer subject, boolean active) {
        CHANNEL.send(new StatePacket(subject.getUUID(), active), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(subject));
    }

    public record StatePacket(UUID player, boolean active) {
        static void encode(StatePacket msg, FriendlyByteBuf buf) {
            buf.writeUUID(msg.player());
            buf.writeBoolean(msg.active());
        }

        static StatePacket decode(FriendlyByteBuf buf) {
            return new StatePacket(buf.readUUID(), buf.readBoolean());
        }

        static void handle(StatePacket msg, CustomPayloadEvent.Context ctx) {
            ctx.enqueueWork(() -> ClientTransformState.set(msg.player(), msg.active()));
            ctx.setPacketHandled(true);
        }
    }
}

