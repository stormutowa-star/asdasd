package com.storm.greatsword;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

// Canal de red del mod. Solo lleva el packet de desenfundar/enfundar (cliente -> servidor).
public final class ModNetwork {
    private ModNetwork() {}

    private static final int PROTOCOL = 1;

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(GreatswordMod.MODID, "main"))
            .networkProtocolVersion(PROTOCOL)
            .clientAcceptedVersions((status, version) -> true)
            .serverAcceptedVersions((status, version) -> true)
            .optional()
            .simpleChannel();

    private static int packetId = 0;

    public static void register(IEventBus modEventBus) {
        CHANNEL.messageBuilder(ToggleDrawPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleDrawPacket::encode)
                .decoder(ToggleDrawPacket::decode)
                .consumerMainThread(ToggleDrawPacket::handle)
                .add();
    }
}
