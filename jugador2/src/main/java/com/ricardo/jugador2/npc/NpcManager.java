package com.ricardo.jugador2.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Dueño del NPC de un servidor: su identidad (nombre y UUID, fijados una vez por mundo), su estado entre
 * reinicios y el jugador en marcha. Un NPC por servidor.
 */
public final class NpcManager {
    private final MinecraftServer server;
    private final NpcSettings settings;
    private final NpcSavedData data;
    private final NpcLifecycle lifecycle;
    private NpcPlayer npc;

    public NpcManager(MinecraftServer server, NpcSettings settings) {
        this.server = server;
        this.settings = settings;
        this.data = server.overworld().getDataStorage().computeIfAbsent(NpcSavedData.factory(), NpcSavedData.ID);
        if (data.uuid() == null) {
            // Primer arranque en este mundo: el nombre sale de la semilla y el UUID es nuevo; no cambian después.
            data.initialize(NpcNameGenerator.generate(server.overworld().getSeed(), onlineNames()), UUID.randomUUID());
        }
        this.lifecycle = new NpcLifecycle(data.phase(), data.ticksUntilRespawn(), settings.respawnDelayTicks());
    }

    /** Perfil del NPC: nombre y UUID guardados en el mundo. */
    public GameProfile profile() {
        return new GameProfile(data.uuid(), data.name());
    }

    public String name() {
        return data.name();
    }

    /**
     * Activa el NPC en pos. Si ya existe, devuelve el mismo. Devuelve null mientras espera para reaparecer,
     * porque esa reaparición la controla el ciclo de vida.
     */
    public NpcPlayer spawn(GameProfile profile, ServerLevel level, Vec3 pos) {
        if (npc != null) {
            return npc;
        }
        if (lifecycle.phase() == NpcLifecycle.Phase.DEAD) {
            return null;
        }
        npc = NpcPlayer.spawnNew(server, level, profile, pos);
        lifecycle.activate();
        data.setActive(true);
        saveState();
        return npc;
    }

    /** Retira el NPC de la lista de jugadores y lo desactiva: no reaparecerá al arrancar. */
    public boolean remove() {
        boolean hadNpc = npc != null;
        if (hadNpc) {
            server.getPlayerList().remove(npc);
            npc = null;
        }
        lifecycle.deactivate();
        data.setActive(false);
        saveState();
        return hadNpc;
    }

    /** Al arrancar el servidor: si estaba activo y la configuración lo pide, reaparece en el spawn del mundo. */
    public void restoreOnStart(ServerLevel level) {
        if (!data.active()) {
            return;
        }
        if (!settings.spawnOnServerStart()) {
            lifecycle.deactivate();
            data.setActive(false);
            saveState();
            return;
        }
        if (lifecycle.phase() == NpcLifecycle.Phase.ALIVE) {
            npc = NpcPlayer.spawnNew(server, level, profile(), Vec3.atBottomCenterOf(level.getSharedSpawnPos()));
        }
    }

    /** Guarda el estado del NPC en los datos del mundo. Se llama al cerrar el servidor. */
    public void saveState() {
        data.saveLifecycle(lifecycle);
    }

    public NpcPlayer npc() {
        return npc;
    }

    public String status() {
        if (npc == null) {
            if (lifecycle.phase() == NpcLifecycle.Phase.DEAD) {
                return "NPC: muerto, reaparece en " + lifecycle.ticksUntilRespawn() + " ticks";
            }
            return "NPC: no activo";
        }
        return "NPC: " + npc.getGameProfile().getName() + " en " + npc.blockPosition().toShortString();
    }

    public void tick() {
        // Reaparición (tarea 9) y cerebro (tareas 6 a 12).
    }

    private Set<String> onlineNames() {
        Set<String> names = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            names.add(player.getGameProfile().getName());
        }
        return names;
    }
}
