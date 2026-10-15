package com.ricardo.jugador2.ai.tasks;

import com.ricardo.jugador2.ai.NpcContext;
import com.ricardo.jugador2.ai.Task;
import com.ricardo.jugador2.ai.TaskResult;
import com.ricardo.jugador2.ai.WorldView;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Guarda en un cofre cercano los objetos que no son herramientas, armadura ni comida. Si no hay cofre, tira la tierra
 * y la grava, que sobran. Termina cuando el inventario ya no está casi lleno.
 */
public final class StoreTask implements Task {
    private static final int CHEST_RANGE = 16;
    private static final double REACH = 3.0;
    private static final int NEARLY_FULL_SLOTS = 2;

    @Override
    public TaskResult tick(NpcContext ctx) {
        NpcPlayer npc = ctx.npc();
        ServerLevel level = npc.serverLevel();
        Inventory inventory = npc.getInventory();
        BlockPos chest = WorldView.findNearestBlock(level, npc.blockPosition(), state -> state.is(Blocks.CHEST),
                CHEST_RANGE);
        if (chest == null) {
            return discardBulk(inventory);
        }
        if (npc.position().distanceTo(Vec3.atCenterOf(chest)) > REACH) {
            if (!ctx.movement().isDone() && !ctx.movement().isStuck()) {
                return TaskResult.RUNNING;
            }
            BlockPos spot = WorldView.standableBeside(level, chest, npc.blockPosition());
            return spot != null && ctx.walkTo(spot) ? TaskResult.RUNNING : TaskResult.FAILURE;
        }
        if (!(level.getBlockEntity(chest) instanceof Container container)) {
            return TaskResult.FAILURE;
        }
        ctx.movement().stop();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (isStorable(stack)) {
                inventory.setItem(slot, insert(container, stack));
            }
        }
        container.setChanged();
        return WorldView.emptySlots(inventory) > NEARLY_FULL_SLOTS ? TaskResult.SUCCESS : discardBulk(inventory);
    }

    /** Sin cofre, o con el cofre lleno: tira tierra y grava. Éxito si el inventario queda libre. */
    private static TaskResult discardBulk(Inventory inventory) {
        discard(inventory, Items.DIRT);
        discard(inventory, Items.GRAVEL);
        return WorldView.emptySlots(inventory) > NEARLY_FULL_SLOTS ? TaskResult.SUCCESS : TaskResult.FAILURE;
    }

    private static boolean isStorable(ItemStack stack) {
        return !stack.isEmpty() && !stack.isDamageableItem() && !stack.has(DataComponents.FOOD);
    }

    /** Mete en el contenedor lo que quepa, primero en pilas del mismo objeto y luego en huecos. Devuelve lo que sobra. */
    private static ItemStack insert(Container container, ItemStack stack) {
        ItemStack rest = stack.copy();
        for (int slot = 0; slot < container.getContainerSize() && !rest.isEmpty(); slot++) {
            ItemStack target = container.getItem(slot);
            if (ItemStack.isSameItemSameComponents(target, rest) && target.getCount() < target.getMaxStackSize()) {
                int moved = Math.min(rest.getCount(), target.getMaxStackSize() - target.getCount());
                target.grow(moved);
                rest.shrink(moved);
            }
        }
        for (int slot = 0; slot < container.getContainerSize() && !rest.isEmpty(); slot++) {
            if (container.getItem(slot).isEmpty()) {
                int moved = Math.min(rest.getCount(), rest.getMaxStackSize());
                container.setItem(slot, rest.copyWithCount(moved));
                rest.shrink(moved);
            }
        }
        return rest;
    }

    private static void discard(Inventory inventory, Item item) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(item)) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }
}
