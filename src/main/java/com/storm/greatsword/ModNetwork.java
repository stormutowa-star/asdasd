package com.storm.greatsword;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public final class ModNetwork {
    private ModNetwork() {}

    private static final int PROTOCOL = 2;

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(GreatswordMod.MODID, "main"))
            .networkProtocolVersion(PROTOCOL)
            .clientAcceptedVersions((status, version) -> true)
            .serverAcceptedVersions((status, version) -> true)
            .optional()
            .simpleChannel();

    public static void register(IEventBus modEventBus) {
        CHANNEL.messageBuilder(WeaponActionPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(WeaponActionPacket::encode)
                .decoder(WeaponActionPacket::decode)
                .consumerMainThread(WeaponActionPacket::handle)
                .add();
    }
}
