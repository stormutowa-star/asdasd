package com.ricardo.jugador2.ai;

import com.ricardo.jugador2.ai.tasks.CraftTask;
import com.ricardo.jugador2.ai.tasks.DefendTask;
import com.ricardo.jugador2.ai.tasks.EatTask;
import com.ricardo.jugador2.ai.tasks.FleeTask;
import com.ricardo.jugador2.ai.tasks.GatherTask;
import com.ricardo.jugador2.ai.tasks.OrderTask;
import com.ricardo.jugador2.ai.tasks.SleepTask;
import com.ricardo.jugador2.ai.tasks.StoreTask;
import com.ricardo.jugador2.npc.NpcSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Elige la tarea del NPC por prioridad y la ejecuta. {@link #choose} es función pura: el mismo estado da siempre la
 * misma tarea. {@link #tick} mantiene la tarea activa y la reevalúa cada aiDecisionIntervalTicks.
 */
public final class NpcBrain {
    private static final Logger LOGGER = LoggerFactory.getLogger(NpcBrain.class);
    private static final int WOOD_TARGET = 16;
    private static final int STONE_TARGET = 32;
    private static final int COAL_TARGET = 16;
    /** Troncos para el primer pico: mesa, palos y pico de madera caben en tres. */
    private static final int PICKAXE_LOGS = 3;
    private static final int BLOCK_TICKS = 40;
    private static final long LOG_INTERVAL_MILLIS = 30_000L;

    private final Map<TaskKind, Long> blockedUntil = new EnumMap<>(TaskKind.class);
    private Task task;
    private TaskKind kind;
    private long nextDecisionTick;
    private long lastFailureLogMillis;

    /** Elige la tarea sin restricciones. */
    public static TaskKind choose(NpcSnapshot s, NpcSettings cfg) {
        return choose(s, cfg, EnumSet.noneOf(TaskKind.class));
    }

    /** Elige la tarea de mayor prioridad que no esté bloqueada. Null si todas lo están. */
    public static TaskKind choose(NpcSnapshot s, NpcSettings cfg, Set<TaskKind> blocked) {
        if (s.health() < cfg.fleeHealthThreshold() && s.hostilesWithinRadius() > 0 && free(TaskKind.FLEE, blocked)) {
            return TaskKind.FLEE;
        }
        if (s.food() < cfg.eatHungerThreshold() && s.hasFood() && free(TaskKind.EAT, blocked)) {
            return TaskKind.EAT;
        }
        if (s.hostilesWithinRadius() > 0 && free(TaskKind.DEFEND, blocked)) {
            return TaskKind.DEFEND;
        }
        if (s.hasActiveOrder() && free(TaskKind.ORDER, blocked)) {
            return TaskKind.ORDER;
        }
        // Va antes de recolectar: con el inventario lleno el NPC deja de recoger (sección 9 del spec).
        if (s.inventoryNearlyFull() && free(TaskKind.STORE, blocked)) {
            return TaskKind.STORE;
        }
        // Sin ningún pico no sale piedra ni carbón: el de madera va antes que recolectar más madera.
        if (!s.hasPickaxe() && s.logs() >= PICKAXE_LOGS && free(TaskKind.CRAFT, blocked)) {
            return TaskKind.CRAFT;
        }
        if (s.logs() < WOOD_TARGET && free(TaskKind.GATHER_WOOD, blocked)) {
            return TaskKind.GATHER_WOOD;
        }
        if (s.hasPickaxe() && s.stone() < STONE_TARGET && free(TaskKind.GATHER_STONE, blocked)) {
            return TaskKind.GATHER_STONE;
        }
        if (s.hasPickaxe() && s.coal() < COAL_TARGET && free(TaskKind.GATHER_COAL, blocked)) {
            return TaskKind.GATHER_COAL;
        }
        if (s.toolsMissing() && free(TaskKind.CRAFT, blocked)) {
            return TaskKind.CRAFT;
        }
        if (s.night() && s.hasBed() && free(TaskKind.SLEEP, blocked)) {
            return TaskKind.SLEEP;
        }
        // Nada pendiente: sin tarea, el NPC se queda quieto hasta que cambie el estado.
        return null;
    }

    /** Descarta la tarea activa y los bloqueos. Se llama al crear, reaparecer o retirar el NPC. */
    public void reset() {
        task = null;
        kind = null;
        blockedUntil.clear();
        nextDecisionTick = 0;
    }

    /** Un tick del cerebro: reevalúa si toca y avanza la tarea activa. */
    public void tick(NpcContext ctx) {
        long now = ctx.server().getTickCount();
        if (now >= nextDecisionTick) {
            decide(ctx, now);
        }
        if (task == null) {
            return;
        }
        TaskResult result = task.tick(ctx);
        if (result == TaskResult.RUNNING) {
            return;
        }
        if (result == TaskResult.FAILURE) {
            blockedUntil.put(kind, now + BLOCK_TICKS);
            logFailure(kind, ctx);
        }
        ctx.movement().stop();
        ctx.npc().setSprinting(false);
        task = null;
        kind = null;
        nextDecisionTick = now;
    }

    private void decide(NpcContext ctx, long now) {
        nextDecisionTick = now + ctx.settings().aiDecisionIntervalTicks();
        Set<TaskKind> blocked = EnumSet.noneOf(TaskKind.class);
        blockedUntil.entrySet().removeIf(entry -> entry.getValue() <= now);
        for (Map.Entry<TaskKind, Long> entry : blockedUntil.entrySet()) {
            blocked.add(entry.getKey());
        }
        TaskKind chosen = choose(SnapshotReader.read(ctx), ctx.settings(), blocked);
        if (chosen == kind && task != null) {
            return;
        }
        ctx.movement().stop();
        ctx.npc().setSprinting(false);
        kind = chosen;
        task = chosen == null ? null : create(chosen);
    }

    private static Task create(TaskKind kind) {
        return switch (kind) {
            case FLEE -> new FleeTask();
            case EAT -> new EatTask();
            case DEFEND -> new DefendTask();
            case ORDER -> new OrderTask();
            case GATHER_WOOD -> new GatherTask(GatherTask.Resource.WOOD);
            case GATHER_STONE -> new GatherTask(GatherTask.Resource.STONE);
            case GATHER_COAL -> new GatherTask(GatherTask.Resource.COAL);
            case CRAFT -> new CraftTask();
            case STORE -> new StoreTask();
            case SLEEP -> new SleepTask();
        };
    }

    private void logFailure(TaskKind failed, NpcContext ctx) {
        long nowMillis = System.currentTimeMillis();
        if (nowMillis - lastFailureLogMillis >= LOG_INTERVAL_MILLIS) {
            lastFailureLogMillis = nowMillis;
            String detail = ctx.order() == null || ctx.order().failure().isEmpty()
                    ? "sin ruta o sin objetivo" : ctx.order().failure();
            LOGGER.info("El NPC no pudo completar {} ({}); elige otra tarea", failed, detail);
        }
    }

    private static boolean free(TaskKind kind, Set<TaskKind> blocked) {
        return !blocked.contains(kind);
    }
}
