package com.ricardo.jugador2.ai.tasks;

import com.ricardo.jugador2.ai.NpcContext;
import com.ricardo.jugador2.ai.Task;
import com.ricardo.jugador2.ai.TaskResult;
import com.ricardo.jugador2.ai.WorldView;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.phys.Vec3;

/** Camina a la cama más cercana y se acuesta. Termina cuando se levanta. */
public final class SleepTask implements Task {
    private static final int BED_RANGE = 16;
    private static final double REACH = 2.0;

    private boolean asleep;

    @Override
    public TaskResult tick(NpcContext ctx) {
        NpcPlayer npc = ctx.npc();
        if (npc.isSleeping()) {
            asleep = true;
            return TaskResult.RUNNING;
        }
        if (asleep) {
            return TaskResult.SUCCESS;
        }
        BlockPos bed = WorldView.findNearestBlock(npc.serverLevel(), npc.blockPosition(),
                state -> state.is(BlockTags.BEDS), BED_RANGE);
        if (bed == null) {
            return TaskResult.FAILURE;
        }
        if (npc.position().distanceTo(Vec3.atCenterOf(bed)) > REACH) {
            if (!ctx.movement().isDone() && !ctx.movement().isStuck()) {
                return TaskResult.RUNNING;
            }
            BlockPos spot = WorldView.standableBeside(npc.serverLevel(), bed, npc.blockPosition());
            return spot != null && ctx.walkTo(spot) ? TaskResult.RUNNING : TaskResult.FAILURE;
        }
        ctx.movement().stop();
        return npc.startSleepInBed(bed).left().isPresent() ? TaskResult.FAILURE : TaskResult.RUNNING;
    }
}
