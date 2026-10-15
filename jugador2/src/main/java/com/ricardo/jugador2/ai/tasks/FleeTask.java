package com.ricardo.jugador2.ai.tasks;

import com.ricardo.jugador2.ai.NpcContext;
import com.ricardo.jugador2.ai.Task;
import com.ricardo.jugador2.ai.TaskResult;
import com.ricardo.jugador2.ai.WorldView;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

/** Se aleja del enemigo más cercano hasta quedar a 10 bloques o más. Prueba otra dirección si no hay ruta. */
public final class FleeTask implements Task {
    private static final double SAFE_DISTANCE = 10.0;
    private static final double FLEE_DISTANCE = 12.0;
    private static final int[] ANGLES_DEGREES = {0, 45, -45, 90, -90};

    private int attempt;

    @Override
    public TaskResult tick(NpcContext ctx) {
        NpcPlayer npc = ctx.npc();
        LivingEntity threat = WorldView.nearestHostile(npc.serverLevel(), npc, ctx.settings().hostileDetectionRadius());
        if (threat == null || npc.distanceTo(threat) >= SAFE_DISTANCE) {
            ctx.movement().stop();
            npc.setSprinting(false);
            return TaskResult.SUCCESS;
        }
        // Huye corriendo: a paso normal el enemigo le sigue el ritmo.
        npc.setSprinting(true);
        if (!ctx.movement().isDone() && !ctx.movement().isStuck()) {
            return TaskResult.RUNNING;
        }
        if (attempt >= ANGLES_DEGREES.length) {
            npc.setSprinting(false);
            return TaskResult.FAILURE;
        }
        // Una ruta por tick: cada tick prueba otra dirección, alejándose del enemigo.
        BlockPos goal = fleeGoal(npc, threat, Math.toRadians(ANGLES_DEGREES[attempt++]));
        if (goal != null && ctx.walkTo(goal)) {
            attempt = 0;
        }
        return TaskResult.RUNNING;
    }

    private static BlockPos fleeGoal(NpcPlayer npc, LivingEntity threat, double offset) {
        double away = Math.atan2(npc.getZ() - threat.getZ(), npc.getX() - threat.getX()) + offset;
        int x = (int) Math.floor(npc.getX() + Math.cos(away) * FLEE_DISTANCE);
        int z = (int) Math.floor(npc.getZ() + Math.sin(away) * FLEE_DISTANCE);
        return WorldView.standableNear(npc.serverLevel(), new BlockPos(x, npc.getBlockY(), z), 2);
    }
}
