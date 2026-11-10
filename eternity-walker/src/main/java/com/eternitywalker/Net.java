package com.eternitywalker;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class Net {
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(EternityWalkerMod.MODID, "main"))
            .networkProtocolVersion(1)
            .simpleChannel();

    private Net() {}

    public static void init() {
        CHANNEL.messageBuilder(ControlPacket.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ControlPacket::encode)
                .decoder(ControlPacket::decode)
                .consumerMainThread(ControlPacket::handle)
                .add();
    }

    public static void sendControls(boolean main, boolean side) {
        CHANNEL.send(new ControlPacket(main, side), PacketDistributor.SERVER.noArg());
    }
}
