package com.ricardo.jugador2.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Dueño del NPC de un servidor: lo crea, lo retira y lo mantiene. Un NPC por servidor. */
public final class NpcManager {
    private final MinecraftServer server;
    private NpcPlayer npc;

    public NpcManager(MinecraftServer server) {
        this.server = server;
    }

    /** Perfil de jugador sin cuenta, con el UUID que Minecraft usa en modo offline. */
    public static GameProfile offlineProfile(String name) {
        return new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)), name);
    }

    /** Crea el NPC si no existe; si ya existe, devuelve el mismo. */
    public NpcPlayer spawn(GameProfile profile, ServerLevel level, Vec3 pos) {
        if (npc == null) {
            npc = NpcPlayer.spawnNew(server, level, profile, pos);
        }
        return npc;
    }

    /** Retira el NPC de la lista de jugadores. Devuelve false si no había NPC. */
    public boolean remove() {
        if (npc == null) {
            return false;
        }
        server.getPlayerList().remove(npc);
        npc = null;
        return true;
    }

    public NpcPlayer npc() {
        return npc;
    }

    public String status() {
        if (npc == null) {
            return "NPC: no activo";
        }
        return "NPC: " + npc.getGameProfile().getName() + " en " + npc.blockPosition().toShortString();
    }

    public void tick() {
        // La IA se conecta en las tareas 6 a 12.
    }
}
