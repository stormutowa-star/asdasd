package com.ricardo.jugador2.npc;

import com.mojang.authlib.GameProfile;
import com.ricardo.jugador2.actions.NpcActions;
import com.ricardo.jugador2.ai.Movement;
import com.ricardo.jugador2.ai.NpcBrain;
import com.ricardo.jugador2.ai.NpcContext;
import com.ricardo.jugador2.ai.OrderState;
import com.ricardo.jugador2.ai.WorldView;
import com.ricardo.jugador2.chat.Come;
import com.ricardo.jugador2.chat.Follow;
import com.ricardo.jugador2.chat.GoTo;
import com.ricardo.jugador2.chat.Help;
import com.ricardo.jugador2.chat.Mine;
import com.ricardo.jugador2.chat.ParsedOrder;
import com.ricardo.jugador2.chat.Stop;
import com.ricardo.jugador2.nav.Cell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Dueño del NPC de un servidor: su identidad (nombre y UUID, fijados una vez por mundo), su estado entre reinicios,
 * el jugador en marcha y su cerebro. Un NPC por servidor.
 */
public final class NpcManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NpcManager.class);
    private final MinecraftServer server;
    private final NpcSettings settings;
    private final NpcSavedData data;
    private final NpcLifecycle lifecycle;
    private final Movement movement = new Movement();
    private final NpcBrain brain = new NpcBrain();
    private final NpcSuspension suspension = new NpcSuspension();
    private NpcPlayer npc;
    private NpcActions actions;
    private OrderState order;
    private boolean brainEnabled = true;

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
        attach(NpcPlayer.spawnNew(server, level, profile, pos));
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
        order = null;
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
            attach(NpcPlayer.spawnNew(server, level, profile(), Vec3.atBottomCenterOf(level.getSharedSpawnPos())));
        }
    }

    /** Llamado al morir el NPC: guarda su punto de reaparición, olvida la orden y empieza la espera. */
    public void onNpcDeath(NpcPlayer dead) {
        if (dead != npc) {
            return;
        }
        BlockPos bed = dead.getRespawnPosition();
        if (bed != null) {
            data.setRespawnPoint(dead.getRespawnDimension().location().toString(), bed);
        }
        order = null;
        lifecycle.onDeath();
        saveState();
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

    /** Activa o pausa el cerebro. Con el cerebro pausado el NPC solo sigue la ruta que se le haya dado. */
    public void setBrainEnabled(boolean enabled) {
        brainEnabled = enabled;
    }

    /** Orden activa del NPC, o null si no tiene ninguna. */
    public void setOrder(OrderState order) {
        this.order = order;
    }

    /** Contexto con el que la IA ejecuta una tarea sobre el NPC actual. Lo usa el escenario de prueba. */
    public NpcContext context() {
        return new NpcContext(server, npc, actions, settings, order, movement);
    }

    /**
     * Orden de un jugador, por el chat o por un comando: la guarda como orden activa y responde en el chat, como
     * lo haría un jugador. Seguir a alguien que no está conectado no crea la orden.
     */
    public void handleOrder(ParsedOrder parsed, String issuer) {
        switch (parsed) {
            case Help help -> say("Órdenes: ven, sigue a <jugador>, ve a <x> <y> <z>, mina <bloque>, para");
            case Stop stop -> {
                stopOrder();
                say("Vale, paro");
            }
            case Follow follow -> {
                if (server.getPlayerList().getPlayerByName(follow.player()) == null) {
                    say(follow.player() + " no está conectado");
                } else {
                    setOrder(new OrderState(follow, issuer));
                    say("Sigo a " + follow.player());
                }
            }
            case Come come -> {
                setOrder(new OrderState(come, issuer));
                say("Voy");
            }
            case GoTo goTo -> {
                setOrder(new OrderState(goTo, issuer));
                say("Voy a " + goTo.x() + " " + goTo.y() + " " + goTo.z());
            }
            case Mine mine -> {
                setOrder(new OrderState(mine, issuer));
                say("Mino " + mine.block());
            }
        }
    }

    /** Cancela la orden activa: el NPC deja de andar y vuelve a sus tareas autónomas. */
    public void stopOrder() {
        if (order != null) {
            order.finish();
        }
        order = null;
        movement.stop();
    }

    /** Pone al NPC a seguir la ruta. Al terminarla, o si no puede avanzar, queda quieto. */
    public void followPath(List<Cell> path) {
        movement.follow(path);
    }

    /** Un tick del NPC: retira el cuerpo muerto, reaparece si toca, y ejecuta la IA y el movimiento. */
    public void tick() {
        if (npc != null && npc.isDeadOrDying()) {
            // Se retira en el tick siguiente a la muerte, no dentro del evento, para no interferir con los objetos que suelta.
            server.getPlayerList().remove(npc);
            npc = null;
            order = null;
            saveState();
        }
        if (lifecycle.tick() == NpcLifecycle.Action.RESPAWN) {
            respawn();
        }
        if (npc == null) {
            return;
        }
        long now = server.getTickCount();
        try {
            if (brainEnabled && !suspension.isSuspended(now)) {
                brain.tick(new NpcContext(server, npc, actions, settings, order, movement));
            }
            movement.tick(npc);
        } catch (RuntimeException e) {
            // Una excepción en la IA deja inactivo a este NPC un rato; el servidor sigue funcionando.
            suspension.suspend(now);
            brain.reset();
            movement.stop();
            LOGGER.error("La IA del NPC falló; queda inactiva {} ticks", NpcSuspension.TICKS, e);
        }
    }

    private void respawn() {
        ServerLevel level = respawnLevel();
        attach(NpcPlayer.spawnNew(server, level, profile(), respawnPosition(level)));
        npc.getFoodData().setFoodLevel(20);
        saveState();
    }

    /** Un cuerpo nuevo: sus acciones, su movimiento y su cerebro empiezan de cero. */
    private void attach(NpcPlayer player) {
        npc = player;
        actions = new NpcActions(player);
        movement.stop();
        brain.reset();
    }

    /** Dimensión del punto de reaparición guardado; si no hay, o ya no existe, el overworld. */
    private ServerLevel respawnLevel() {
        String dimension = data.respawnDimension();
        if (dimension != null) {
            ResourceKey<net.minecraft.world.level.Level> key =
                    ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension));
            ServerLevel level = server.getLevel(key);
            if (level != null) {
                return level;
            }
        }
        return server.overworld();
    }

    /** Punto guardado si el cuerpo cabe allí; si no, el spawn compartido del mundo. */
    private Vec3 respawnPosition(ServerLevel level) {
        BlockPos saved = data.respawnPos();
        if (saved != null && WorldView.isStandable(level, saved)) {
            return Vec3.atBottomCenterOf(saved);
        }
        return Vec3.atBottomCenterOf(level.getSharedSpawnPos());
    }

    /** Mensaje del NPC en el chat: como un jugador normal, con su nombre entre corchetes angulares. */
    private void say(String text) {
        server.getPlayerList().broadcastSystemMessage(Component.literal("<" + name() + "> " + text), false);
    }

    private Set<String> onlineNames() {
        Set<String> names = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            names.add(player.getGameProfile().getName());
        }
        return names;
    }
}
