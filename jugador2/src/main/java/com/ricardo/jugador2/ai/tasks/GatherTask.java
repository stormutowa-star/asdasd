package com.ricardo.jugador2.ai.tasks;

import com.ricardo.jugador2.ai.NpcContext;
import com.ricardo.jugador2.ai.Task;
import com.ricardo.jugador2.ai.TaskResult;
import com.ricardo.jugador2.ai.WorldView;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

/**
 * Recolecta un bloque del tipo pedido: mina el más cercano a 32 bloques, o si no hay ninguno camina a un punto
 * al azar entre 24 y 48 bloques para buscar. Termina en cuanto mina un bloque o llega al punto de búsqueda.
 */
public final class GatherTask implements Task {
    public enum Resource { WOOD, STONE, COAL }

    private static final int SEARCH_RADIUS = 32;
    private static final int EXPLORE_MIN = 24;
    private static final int EXPLORE_MAX = 48;
    private static final double COLLECT_RADIUS = 8.0;
    private static final double PICKUP_REACH = 0.8;
    private static final int COLLECT_TICKS = 60;

    private final Resource resource;
    private MineTask mining;
    private boolean exploring;
    private boolean collecting;
    private int collectTicks;

    public GatherTask(Resource resource) {
        this.resource = resource;
    }

    @Override
    public TaskResult tick(NpcContext ctx) {
        if (collecting) {
            return collect(ctx);
        }
        if (mining != null) {
            TaskResult result = mining.tick(ctx);
            if (result == TaskResult.SUCCESS) {
                mining = null;
                collecting = true;
                collectTicks = 0;
                return TaskResult.RUNNING;
            }
            if (result == TaskResult.FAILURE) {
                mining = null;
            }
            return result;
        }
        if (exploring) {
            if (ctx.movement().isDone() || ctx.movement().isStuck()) {
                exploring = false;
                ctx.movement().stop();
                return TaskResult.SUCCESS;
            }
            return TaskResult.RUNNING;
        }
        NpcPlayer npc = ctx.npc();
        BlockPos found = WorldView.findNearestBlock(npc.serverLevel(), npc.blockPosition(), matcher(resource),
                SEARCH_RADIUS);
        if (found != null) {
            mining = new MineTask(found);
            TaskResult result = mining.tick(ctx);
            if (result != TaskResult.RUNNING) {
                mining = null;
            }
            return result;
        }
        BlockPos goal = explorationGoal(ctx);
        if (goal == null || !ctx.walkTo(goal)) {
            return TaskResult.FAILURE;
        }
        exploring = true;
        return TaskResult.RUNNING;
    }

    /** Camina hasta los objetos que soltó el bloque; el juego los recoge al pasar sobre ellos, como a cualquier jugador. */
    private TaskResult collect(NpcContext ctx) {
        NpcPlayer npc = ctx.npc();
        ServerLevel level = npc.serverLevel();
        collectTicks++;
        ItemEntity item = nearestItem(level, npc);
        if (item == null || collectTicks > COLLECT_TICKS) {
            collecting = false;
            ctx.movement().stop();
            return TaskResult.SUCCESS;
        }
        if (npc.distanceTo(item) <= PICKUP_REACH) {
            ctx.movement().stop();
            return TaskResult.RUNNING;
        }
        if (ctx.movement().isDone() || ctx.movement().isStuck()) {
            BlockPos spot = WorldView.standableNear(level, item.blockPosition(), 1);
            if (spot == null || !ctx.walkTo(spot)) {
                collecting = false;
                return TaskResult.SUCCESS;
            }
        }
        return TaskResult.RUNNING;
    }

    private static ItemEntity nearestItem(ServerLevel level, NpcPlayer npc) {
        ItemEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class,
                npc.getBoundingBox().inflate(COLLECT_RADIUS))) {
            double distance = item.distanceToSqr(npc);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = item;
            }
        }
        return best;
    }

    private static Predicate<BlockState> matcher(Resource resource) {
        return switch (resource) {
            case WOOD -> state -> state.is(BlockTags.LOGS);
            case STONE -> state -> state.is(BlockTags.BASE_STONE_OVERWORLD);
            case COAL -> state -> state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE);
        };
    }

    private static BlockPos explorationGoal(NpcContext ctx) {
        NpcPlayer npc = ctx.npc();
        ServerLevel level = npc.serverLevel();
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = EXPLORE_MIN + random.nextDouble() * (EXPLORE_MAX - EXPLORE_MIN);
        BlockPos around = npc.blockPosition().offset((int) (Math.cos(angle) * distance), 0,
                (int) (Math.sin(angle) * distance));
        return WorldView.standableNear(level, around, 4);
    }
}
