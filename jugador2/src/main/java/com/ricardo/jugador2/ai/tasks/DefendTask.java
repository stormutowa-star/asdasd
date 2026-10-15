package com.ricardo.jugador2.ai.tasks;

import com.ricardo.jugador2.ai.NpcContext;
import com.ricardo.jugador2.ai.Task;
import com.ricardo.jugador2.ai.TaskResult;
import com.ricardo.jugador2.ai.WorldView;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Ataca al enemigo más cercano: se acerca con una ruta y golpea cuando el cooldown de ataque está listo. */
public final class DefendTask implements Task {
    private static final double ATTACK_RANGE = 3.0;
    private static final int REPLAN_TICKS = 10;

    private int ticksSincePlan = REPLAN_TICKS;

    @Override
    public TaskResult tick(NpcContext ctx) {
        NpcPlayer npc = ctx.npc();
        LivingEntity enemy = WorldView.nearestHostile(npc.serverLevel(), npc, ctx.settings().hostileDetectionRadius());
        if (enemy == null) {
            ctx.movement().stop();
            return TaskResult.SUCCESS;
        }
        ctx.actions().lookAt(enemy.getEyePosition());
        if (npc.distanceTo(enemy) <= ATTACK_RANGE) {
            ctx.movement().stop();
            ctx.actions().attack(enemy);
            return TaskResult.RUNNING;
        }
        ticksSincePlan++;
        if (ticksSincePlan >= REPLAN_TICKS || ctx.movement().isDone() || ctx.movement().isStuck()) {
            ticksSincePlan = 0;
            return ctx.walkTo(enemy.blockPosition()) ? TaskResult.RUNNING : TaskResult.FAILURE;
        }
        return TaskResult.RUNNING;
    }
}
