package com.ricardo.jugador2.ai.tasks;

import com.ricardo.jugador2.ai.NpcContext;
import com.ricardo.jugador2.ai.Task;
import com.ricardo.jugador2.ai.TaskResult;
import com.ricardo.jugador2.ai.WorldView;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Craftea el pico que le falta con las recetas del juego: el de madera si no tiene ninguno, y el de piedra después.
 * Pone una mesa si no hay una cerca, hace palos a partir de tablones y tablones a partir de troncos cuando faltan.
 * Cada receta consume sus ingredientes solo si el inventario los tiene todos.
 */
public final class CraftTask implements Task {
    private static final int TABLE_RANGE = 4;
    private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private static final Predicate<ItemStack> PLANKS = stack -> stack.is(ItemTags.PLANKS);

    @Override
    public TaskResult tick(NpcContext ctx) {
        NpcPlayer npc = ctx.npc();
        Inventory inventory = npc.getInventory();
        if (WorldView.count(inventory, Items.STONE_PICKAXE) > 0 || WorldView.count(inventory, Items.IRON_PICKAXE) > 0
                || WorldView.count(inventory, Items.DIAMOND_PICKAXE) > 0) {
            return TaskResult.SUCCESS;
        }
        boolean hasPickaxe = WorldView.count(inventory, ItemTags.PICKAXES) > 0;
        ServerLevel level = npc.serverLevel();
        boolean tableNearby = WorldView.findNearestBlock(level, npc.blockPosition(),
                state -> state.is(Blocks.CRAFTING_TABLE), TABLE_RANGE) != null;
        if (!tableNearby) {
            if (WorldView.count(inventory, Items.CRAFTING_TABLE) > 0) {
                return placeTable(ctx) ? TaskResult.RUNNING : TaskResult.FAILURE;
            }
            return craftOne(ctx, is(Items.CRAFTING_TABLE), false) || craftOne(ctx, PLANKS, false)
                    ? TaskResult.RUNNING : TaskResult.FAILURE;
        }
        if (WorldView.count(inventory, Items.STICK) < 2) {
            return craftOne(ctx, is(Items.STICK), false) || craftOne(ctx, PLANKS, false)
                    ? TaskResult.RUNNING : TaskResult.FAILURE;
        }
        // Sin pico no hay piedra que sacar: el de madera basta para empezar. Si faltan tablones para él, se hacen de un
        // tronco. Al craftearlo la tarea termina; el cerebro decide otra vez y la siguiente hace el de piedra.
        if (!hasPickaxe) {
            if (craftOne(ctx, is(Items.WOODEN_PICKAXE), true)) {
                return TaskResult.SUCCESS;
            }
            return craftOne(ctx, PLANKS, false) ? TaskResult.RUNNING : TaskResult.FAILURE;
        }
        return craftOne(ctx, is(Items.STONE_PICKAXE), true) ? TaskResult.RUNNING : TaskResult.FAILURE;
    }

    private static Predicate<ItemStack> is(Item item) {
        return stack -> stack.is(item);
    }

    /** Prueba las recetas que dan lo pedido hasta que una funciona. Con tableNearby, también las de 3x3. */
    private static boolean craftOne(NpcContext ctx, Predicate<ItemStack> wanted, boolean tableNearby) {
        ServerLevel level = ctx.npc().serverLevel();
        for (RecipeHolder<CraftingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = holder.value();
            if (!wanted.test(recipe.getResultItem(level.registryAccess()))) {
                continue;
            }
            if (!tableNearby && !recipe.canCraftInDimensions(2, 2)) {
                continue;
            }
            if (tryCraft(ctx, recipe)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryCraft(NpcContext ctx, CraftingRecipe recipe) {
        NpcPlayer npc = ctx.npc();
        ServerLevel level = npc.serverLevel();
        Inventory inventory = npc.getInventory();
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : (ingredients.size() <= 4 ? 2 : 3);
        int height = recipe instanceof ShapedRecipe shaped ? shaped.getHeight() : width;

        // Primero se simula con copias: si falta un ingrediente, el inventario no se toca.
        List<ItemStack> simulated = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            simulated.add(inventory.getItem(slot).copy());
        }
        List<ItemStack> grid = new ArrayList<>();
        List<Integer> used = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                grid.add(ItemStack.EMPTY);
                continue;
            }
            int slot = takeMatching(simulated, ingredient);
            if (slot < 0) {
                return false;
            }
            used.add(slot);
            grid.add(inventory.getItem(slot).copyWithCount(1));
        }
        while (grid.size() < width * height) {
            grid.add(ItemStack.EMPTY);
        }
        CraftingInput input = CraftingInput.of(width, height, grid);
        if (!recipe.matches(input, level)) {
            return false;
        }
        ItemStack result = recipe.assemble(input, level.registryAccess());
        for (int slot : used) {
            inventory.getItem(slot).shrink(1);
        }
        for (ItemStack leftover : recipe.getRemainingItems(input)) {
            if (!leftover.isEmpty() && !inventory.add(leftover)) {
                npc.drop(leftover, false);
            }
        }
        if (!inventory.add(result)) {
            npc.drop(result, false);
        }
        return true;
    }

    private static int takeMatching(List<ItemStack> stacks, Ingredient ingredient) {
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (!stack.isEmpty() && ingredient.test(stack)) {
                stack.shrink(1);
                return slot;
            }
        }
        return -1;
    }

    /** Pone una mesa del inventario en un hueco libre junto al NPC. */
    private static boolean placeTable(NpcContext ctx) {
        NpcPlayer npc = ctx.npc();
        ServerLevel level = npc.serverLevel();
        int slot = WorldView.moveToHotbar(npc.getInventory(), stack -> stack.is(Items.CRAFTING_TABLE));
        if (slot < 0) {
            return false;
        }
        for (Direction side : SIDES) {
            BlockPos spot = npc.blockPosition().relative(side);
            BlockPos floor = spot.below();
            if (level.getBlockState(spot).isAir() && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
                return ctx.actions().placeBlock(spot, Direction.UP, slot);
            }
        }
        return false;
    }
}
