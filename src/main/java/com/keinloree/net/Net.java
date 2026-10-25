package com.keinloree.net;

import com.keinloree.KeinLooreMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

/** Canal de red del mod y ayudantes de envio. */
public final class Net {
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "main"))
            .networkProtocolVersion(1)
            .simpleChannel();

    private static boolean ready;

    private Net() {}

    /** Registra todos los paquetes (una sola vez, en el arranque comun). */
    public static synchronized void init() {
        if (ready) {
            return;
        }
        ready = true;
        // Servidor a cliente
        CHANNEL.messageBuilder(DialogueS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DialogueS2C::encode).decoder(DialogueS2C::decode).consumerMainThread(DialogueS2C::handle).add();
        CHANNEL.messageBuilder(ChoiceS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ChoiceS2C::encode).decoder(ChoiceS2C::decode).consumerMainThread(ChoiceS2C::handle).add();
        CHANNEL.messageBuilder(CinematicS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CinematicS2C::encode).decoder(CinematicS2C::decode).consumerMainThread(CinematicS2C::handle).add();
        CHANNEL.messageBuilder(CinematicEndS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CinematicEndS2C::encode).decoder(CinematicEndS2C::decode).consumerMainThread(CinematicEndS2C::handle).add();
        CHANNEL.messageBuilder(ScreenFxS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ScreenFxS2C::encode).decoder(ScreenFxS2C::decode).consumerMainThread(ScreenFxS2C::handle).add();
        CHANNEL.messageBuilder(TitleCardS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TitleCardS2C::encode).decoder(TitleCardS2C::decode).consumerMainThread(TitleCardS2C::handle).add();
        CHANNEL.messageBuilder(HudS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(HudS2C::encode).decoder(HudS2C::decode).consumerMainThread(HudS2C::handle).add();
        CHANNEL.messageBuilder(WhisperS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(WhisperS2C::encode).decoder(WhisperS2C::decode).consumerMainThread(WhisperS2C::handle).add();
        CHANNEL.messageBuilder(AtmosphereS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AtmosphereS2C::encode).decoder(AtmosphereS2C::decode).consumerMainThread(AtmosphereS2C::handle).add();
        CHANNEL.messageBuilder(VfxS2C.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(VfxS2C::encode).decoder(VfxS2C::decode).consumerMainThread(VfxS2C::handle).add();
        // Cliente a servidor
        CHANNEL.messageBuilder(ChoiceC2S.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ChoiceC2S::encode).decoder(ChoiceC2S::decode).consumerMainThread(ChoiceC2S::handle).add();
        CHANNEL.messageBuilder(SkipC2S.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SkipC2S::encode).decoder(SkipC2S::decode).consumerMainThread(SkipC2S::handle).add();
        CHANNEL.build();
    }

    /** Envia a un jugador. */
    public static void toPlayer(ServerPlayer player, Object msg) {
        CHANNEL.send(msg, PacketDistributor.PLAYER.with(player));
    }

    /** Envia a todos los jugadores de una dimension. */
    public static void toAll(ServerLevel level, Object msg) {
        CHANNEL.send(msg, PacketDistributor.DIMENSION.with(level.dimension()));
    }

    /** Envia a los jugadores a menos de {@code radius} bloques de un punto. */
    public static void toNear(ServerLevel level, Vec3 pos, double radius, Object msg) {
        CHANNEL.send(msg, PacketDistributor.NEAR.with(
                new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius * radius, level.dimension())));
    }

    /** Cliente a servidor. */
    public static void toServer(Object msg) {
        CHANNEL.send(msg, PacketDistributor.SERVER.noArg());
    }
}
