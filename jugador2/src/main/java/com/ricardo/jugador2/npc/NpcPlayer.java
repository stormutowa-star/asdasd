package com.ricardo.jugador2.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.phys.Vec3;

/** Jugador real del servidor sin cliente: aparece en la lista de jugadores y usa las mismas APIs que cualquier jugador. */
public final class NpcPlayer extends ServerPlayer {
    private NpcPlayer(MinecraftServer server, ServerLevel level, GameProfile profile) {
        super(server, level, profile, ClientInformation.createDefault());
    }

    /** Crea el jugador, lo registra en la lista de jugadores y lo coloca en {@code pos} con vida completa. */
    public static NpcPlayer spawnNew(MinecraftServer server, ServerLevel level, GameProfile profile, Vec3 pos) {
        NpcPlayer npc = new NpcPlayer(server, level, profile);
        server.getPlayerList().placeNewPlayer(
                new FakeConnection(),
                npc,
                new CommonListenerCookie(profile, 0, npc.clientInformation(), false));
        npc.teleportTo(level, pos.x, pos.y, pos.z, 0f, 0f);
        npc.setHealth(npc.getMaxHealth());
        return npc;
    }
}
