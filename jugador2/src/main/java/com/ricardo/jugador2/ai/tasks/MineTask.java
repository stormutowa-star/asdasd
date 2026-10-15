package com.ricardo.jugador2.ai.tasks;

import com.ricardo.jugador2.ai.NpcContext;
import com.ricardo.jugador2.ai.Task;
import com.ricardo.jugador2.ai.TaskResult;
import com.ricardo.jugador2.ai.WorldView;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Camina hasta un sitio desde el que alcanza un bloque, lo mina y termina cuando desaparece. Lo usan la recolección
 * y la orden "mina".
 */
public final class MineTask implements Task {
    private static final double REACH = 3.5;
    private static final int MAX_PLANS = 5;
    private static final int HOTBAR = 9;
    private static final int[][] SIDES = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final BlockPos target;
    private boolean started;
    private int plans;

    public MineTask(BlockPos target) {
        this.target = target.immutable();
    }

    @Override
    public TaskResult tick(NpcContext ctx) {
        NpcPlayer npc = ctx.npc();
        if (npc.serverLevel().getBlockState(target).isAir()) {
            ctx.movement().stop();
            return TaskResult.SUCCESS;
        }
        if (npc.getEyePosition().distanceTo(Vec3.atCenterOf(target)) > REACH) {
            if (started) {
                ctx.actions().stopMining(target);
                started = false;
            }
            if (!ctx.movement().isDone() && !ctx.movement().isStuck()) {
                return TaskResult.RUNNING;
            }
            plans++;
            return plans <= MAX_PLANS && planToReach(ctx) ? TaskResult.RUNNING : TaskResult.FAILURE;
        }
        ctx.movement().stop();
        ctx.actions().lookAt(Vec3.atCenterOf(target));
        if (!started) {
            selectBestTool(ctx);
            if (!ctx.actions().startMining(target)) {
                return TaskResult.FAILURE;
            }
            started = true;
            return TaskResult.RUNNING;
        }
        return ctx.actions().continueMining() ? TaskResult.SUCCESS : TaskResult.RUNNING;
    }

    /** Elige en la barra la herramienta que más rápido rompe el bloque; con las manos si ninguna sirve. */
    private void selectBestTool(NpcContext ctx) {
        Inventory inventory = ctx.npc().getInventory();
        BlockState state = ctx.npc().serverLevel().getBlockState(target);
        int best = inventory.selected;
        float bestSpeed = inventory.getItem(best).getDestroySpeed(state);
        for (int slot = 0; slot < HOTBAR; slot++) {
            float speed = inventory.getItem(slot).getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                best = slot;
            }
        }
        ctx.actions().selectHotbarSlot(best);
    }

    /** Ruta hasta el sitio libre más cercano junto al bloque, a su altura o una celda más arriba o abajo. */
    private boolean planToReach(NpcContext ctx) {
        ServerLevel level = ctx.npc().serverLevel();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int[] side : SIDES) {
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos candidate = target.offset(side[0], dy, side[1]);
                double distance = candidate.distSqr(ctx.npc().blockPosition());
                if (distance < bestDistance && WorldView.isStandable(level, candidate)) {
                    bestDistance = distance;
                    best = candidate;
                }
            }
        }
        return best != null && ctx.walkTo(best);
    }
}
