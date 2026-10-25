package com.keinloree.story;

import com.keinloree.net.CinematicEndS2C;
import com.keinloree.net.CinematicS2C;
import com.keinloree.net.Net;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/**
 * Cinematicas (servidor): registro por nombre, envio de planos y proteccion del jugador mientras duran
 * (invulnerable y no objetivo de los mobs).
 */
public final class Cinematics {
    private static final Logger LOGGER = LogUtils.getLogger();

    @FunctionalInterface
    public interface Factory {
        List<Shot> build(StoryCtx ctx, String[] args);
    }

    private static final class Active {
        int remaining;
        final boolean skippable;

        Active(int remaining, boolean skippable) {
            this.remaining = remaining;
            this.skippable = skippable;
        }
    }

    private static final Map<String, Factory> REGISTRY = new HashMap<>();
    private static final Map<UUID, Active> ACTIVE = new HashMap<>();

    private Cinematics() {}

    public static void register(String name, Factory factory) {
        REGISTRY.put(name, factory);
    }

    public static Set<String> names() {
        return REGISTRY.keySet();
    }

    /** Reproduce una cinematica registrada para todos los jugadores del Overworld (no bloquea). */
    public static boolean playNamed(StoryCtx ctx, String name, String[] args) {
        Factory f = REGISTRY.get(name);
        if (f == null) {
            LOGGER.warn("[Cine] Cinematica '{}' no registrada (se ignora)", name);
            return false;
        }
        List<Shot> shots = f.build(ctx, args);
        if (shots == null || shots.isEmpty()) {
            return false;
        }
        for (ServerPlayer p : ctx.players()) {
            play(p, shots, true);
        }
        return true;
    }

    /** Envia los planos a un jugador y lo protege mientras duran. */
    public static void play(ServerPlayer player, List<Shot> shots, boolean skippable) {
        int total = 10;
        for (Shot s : shots) {
            total += s.ticks;
        }
        ACTIVE.put(player.getUUID(), new Active(total, skippable));
        Net.toPlayer(player, new CinematicS2C(new ArrayList<>(shots), skippable ? CinematicS2C.SALTABLE : 0));
    }

    public static boolean isActive(@Nullable Entity e) {
        return e instanceof ServerPlayer p && ACTIVE.containsKey(p.getUUID());
    }

    public static void stop(ServerPlayer player) {
        if (ACTIVE.remove(player.getUUID()) != null) {
            Net.toPlayer(player, new CinematicEndS2C());
        }
    }

    /** Salto pedido por el jugador (solo si la cinematica es saltable). */
    public static void skip(ServerPlayer player) {
        Active a = ACTIVE.get(player.getUUID());
        if (a != null && a.skippable) {
            stop(player);
        }
    }

    public static void stopAll(MinecraftServer server) {
        for (UUID id : new ArrayList<>(ACTIVE.keySet())) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p != null) {
                stop(p);
            }
        }
        ACTIVE.clear();
    }

    /** Olvida al jugador sin enviar nada (desconexion). */
    public static void forget(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
    }

    public static void reset() {
        ACTIVE.clear();
    }

    public static void tick(MinecraftServer server) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        boolean sweep = server.getTickCount() % 20 == 0;
        Iterator<Map.Entry<UUID, Active>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Active> e = it.next();
            ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
            if (p == null || !p.isAlive()) {
                it.remove();
                continue;
            }
            if (--e.getValue().remaining <= 0) {
                it.remove();
                Net.toPlayer(p, new CinematicEndS2C());
                continue;
            }
            if (sweep) {
                // Los mobs que ya lo perseguian lo sueltan
                for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(48.0D))) {
                    if (m.getTarget() == p) {
                        m.setTarget(null);
                    }
                }
            }
        }
    }

    /** Registra la cinematica de prueba prueba_orbita. */
    static void registerBuiltins() {
        register("prueba_orbita", (ctx, args) -> {
            ServerPlayer hero = ctx.hero();
            if (hero == null && !ctx.players().isEmpty()) {
                hero = ctx.players().get(0);
            }
            if (hero == null) {
                return List.of();
            }
            Vec3 c = hero.position().add(0.0D, 1.0D, 0.0D);
            return List.of(Shot.orbit(c, hero.getId(), 8.0F, 3.0F, 0.0F, 360.0F, 160).lookAtEntity(hero.getId()));
        });
    }
}
