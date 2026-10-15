package com.ricardo.jugador2.ai.tasks;

import com.ricardo.jugador2.ai.NpcContext;
import com.ricardo.jugador2.ai.Task;
import com.ricardo.jugador2.ai.TaskResult;
import com.ricardo.jugador2.ai.WorldView;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.core.component.DataComponents;

/** Come hasta llegar a 20 de hambre o hasta acabarse la comida del inventario. */
public final class EatTask implements Task {
    private static final int FULL = 20;

    private boolean ate;

    @Override
    public TaskResult tick(NpcContext ctx) {
        NpcPlayer npc = ctx.npc();
        if (npc.getFoodData().getFoodLevel() >= FULL) {
            return TaskResult.SUCCESS;
        }
        if (npc.isUsingItem()) {
            return TaskResult.RUNNING;
        }
        int slot = WorldView.findSlot(npc.getInventory(), stack -> stack.has(DataComponents.FOOD));
        if (slot < 0) {
            return ate ? TaskResult.SUCCESS : TaskResult.FAILURE;
        }
        ate = true;
        return ctx.actions().eat(slot) ? TaskResult.RUNNING : TaskResult.FAILURE;
    }
}
