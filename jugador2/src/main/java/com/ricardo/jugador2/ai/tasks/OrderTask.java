package com.ricardo.jugador2.ai.tasks;

import com.ricardo.jugador2.ai.Movement;
import com.ricardo.jugador2.ai.NpcContext;
import com.ricardo.jugador2.ai.OrderState;
import com.ricardo.jugador2.ai.Task;
import com.ricardo.jugador2.ai.TaskResult;
import com.ricardo.jugador2.ai.WorldView;
import com.ricardo.jugador2.chat.Come;
import com.ricardo.jugador2.chat.Follow;
import com.ricardo.jugador2.chat.GoTo;
import com.ricardo.jugador2.chat.Help;
import com.ricardo.jugador2.chat.Mine;
import com.ricardo.jugador2.chat.Stop;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Ejecuta la orden activa: ir hacia alguien, seguirlo, ir a un punto, minar un bloque o parar. */
public final class OrderTask implements Task {
    private static final int REPLAN_TICKS = 10;
    private static final double COME_DISTANCE = 2.0;
    private static final double FOLLOW_DISTANCE = 3.0;
    private static final double GOTO_DISTANCE = 1.5;
    private static final int GOTO_REACH = 16;
    private static final int MINE_RADIUS = 32;

    private int ticksSincePlan = REPLAN_TICKS;
    private MineTask mining;

    @Override
    public TaskResult tick(NpcContext ctx) {
        OrderState order = ctx.order();
        if (order == null || order.status() != OrderState.Status.ACTIVE) {
            return TaskResult.SUCCESS;
        }
        return switch (order.order()) {
            case Come come -> followPlayer(ctx, order, order.issuer(), COME_DISTANCE, true);
            case Follow follow -> followPlayer(ctx, order, follow.player(), FOLLOW_DISTANCE, false);
            case GoTo goTo -> goTo(ctx, order, new BlockPos(goTo.x(), goTo.y(), goTo.z()));
            case Mine mine -> mine(ctx, order, mine.block());
            case Stop stop -> stop(ctx, order);
            case Help help -> stop(ctx, order);
        };
    }

    private TaskResult followPlayer(NpcContext ctx, OrderState order, String name, double stopDistance,
                                    boolean finishOnArrival) {
        ServerPlayer target = ctx.server().getPlayerList().getPlayerByName(name);
        if (target == null) {
            order.onTargetMissing(name);
            return TaskResult.SUCCESS;
        }
        if (ctx.npc().distanceTo(target) <= stopDistance) {
            ctx.movement().stop();
            if (finishOnArrival) {
                order.finish("Ya estoy aquí");
                return TaskResult.SUCCESS;
            }
            return TaskResult.RUNNING;
        }
        return replan(ctx, order, target.blockPosition()) ? TaskResult.RUNNING : TaskResult.FAILURE;
    }

    private TaskResult goTo(NpcContext ctx, OrderState order, BlockPos goal) {
        // Un punto en el aire se cambia por el suelo que hay debajo, donde el NPC puede estar de pie.
        BlockPos spot = WorldView.standableNear(ctx.npc().serverLevel(), goal, GOTO_REACH);
        if (spot == null) {
            order.fail("no encuentro suelo en " + goal.toShortString());
            return TaskResult.FAILURE;
        }
        if (ctx.npc().position().distanceTo(Vec3.atBottomCenterOf(spot)) <= GOTO_DISTANCE) {
            ctx.movement().stop();
            order.finish("Llegué");
            return TaskResult.SUCCESS;
        }
        return replan(ctx, order, spot) ? TaskResult.RUNNING : TaskResult.FAILURE;
    }

    private TaskResult mine(NpcContext ctx, OrderState order, String block) {
        if (mining == null) {
            BlockPos found = WorldView.findNearestBlock(ctx.npc().serverLevel(), ctx.npc().blockPosition(),
                    state -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().equals(block), MINE_RADIUS);
            if (found == null) {
                order.fail("no encuentro " + block);
                return TaskResult.FAILURE;
            }
            mining = new MineTask(found);
        }
        TaskResult result = mining.tick(ctx);
        if (result == TaskResult.SUCCESS) {
            order.finish("Listo");
        } else if (result == TaskResult.FAILURE) {
            order.fail("no puedo minar " + block);
        }
        return result;
    }

    private TaskResult stop(NpcContext ctx, OrderState order) {
        ctx.movement().stop();
        order.finish();
        return TaskResult.SUCCESS;
    }

    /** Calcula la ruta a goal como mucho cada REPLAN_TICKS, o antes si ya no camina. Falla la orden si no hay ruta. */
    private boolean replan(NpcContext ctx, OrderState order, BlockPos goal) {
        Movement movement = ctx.movement();
        ticksSincePlan++;
        if (ticksSincePlan < REPLAN_TICKS && !movement.isDone() && !movement.isStuck()) {
            return true;
        }
        ticksSincePlan = 0;
        if (ctx.walkTo(goal)) {
            return true;
        }
        order.fail("no encuentro ruta hasta " + goal.toShortString());
        return false;
    }
}
