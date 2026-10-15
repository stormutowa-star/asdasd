package com.ricardo.jugador2.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/** Consultas al mundo y al inventario que usan las tareas. Solo lectura, salvo {@link #moveToHotbar}. */
public final class WorldView {
    /** Hojas a este alcance de un tronco para contarlo como árbol natural. */
    private static final int TREE_LEAF_REACH = 3;
    private static final int HOTBAR = 9;

    private WorldView() {
    }

    /** Bloque más cercano a origin que cumple match, dentro de un cubo de radius bloques. Null si no hay ninguno. */
    public static BlockPos findNearestBlock(ServerLevel level, BlockPos origin, Predicate<BlockState> match, int radius) {
        return findNearest(level, origin, radius, Set.of(), (pos, state) -> match.test(state));
    }

    /**
     * Bloque más cercano a origin que cumple match, dentro de un cubo de radius bloques y fuera de skip. Null si no hay.
     * match recibe la posición además del estado, para mirar alrededor del bloque.
     */
    public static BlockPos findNearest(ServerLevel level, BlockPos origin, int radius, Set<BlockPos> skip,
                                       BiPredicate<BlockPos, BlockState> match) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius),
                origin.offset(radius, radius, radius))) {
            double distance = pos.distSqr(origin);
            if (distance < bestDistance && !skip.contains(pos) && match.test(pos, level.getBlockState(pos))) {
                bestDistance = distance;
                best = pos.immutable();
            }
        }
        return best;
    }

    /**
     * Tronco de un árbol natural: madera sin tallar con hojas a menos de TREE_LEAF_REACH bloques. Un tronco que
     * un jugador puso para construir, o uno tallado, no tiene hojas pegadas, así que el NPC no lo corta.
     */
    public static boolean isTreeLog(ServerLevel level, BlockPos pos, BlockState state) {
        return state.is(BlockTags.LOGS) && !isStripped(state) && hasLeavesNear(level, pos);
    }

    private static boolean isStripped(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().startsWith("stripped_");
    }

    private static boolean hasLeavesNear(ServerLevel level, BlockPos pos) {
        for (BlockPos near : BlockPos.betweenClosed(pos.offset(-TREE_LEAF_REACH, -TREE_LEAF_REACH, -TREE_LEAF_REACH),
                pos.offset(TREE_LEAF_REACH, TREE_LEAF_REACH, TREE_LEAF_REACH))) {
            if (level.getBlockState(near).is(BlockTags.LEAVES)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lleva a la barra el primer objeto que cumple match y devuelve su ranura de barra, o -1 si no hay ninguno. Si estaba
     * fuera de la barra, cambia de sitio con el objeto de la última ranura de la barra, como al pasar objetos a mano.
     */
    public static int moveToHotbar(Inventory inventory, Predicate<ItemStack> match) {
        int from = findSlot(inventory, match);
        if (from < 0) {
            return -1;
        }
        if (from < HOTBAR) {
            return from;
        }
        int target = HOTBAR - 1;
        ItemStack displaced = inventory.getItem(target);
        inventory.setItem(target, inventory.getItem(from));
        inventory.setItem(from, displaced);
        return target;
    }

    /** Sitio donde cabe el cuerpo: suelo sólido bajo los pies y dos celdas libres. */
    public static boolean isStandable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && isFree(level, feet)
                && isFree(level, feet.above());
    }

    /** Primer sitio donde cabe el cuerpo cerca de around, subiendo y bajando hasta reach bloques. Null si no hay. */
    public static BlockPos standableNear(ServerLevel level, BlockPos around, int reach) {
        for (int step = 0; step <= 2 * reach; step++) {
            int dy = step % 2 == 0 ? step / 2 : -(step + 1) / 2;
            BlockPos candidate = around.offset(0, dy, 0);
            if (isStandable(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Sitio donde cabe el cuerpo junto a target: a un lado del bloque, a su altura o una celda más arriba o abajo.
     * Devuelve el más cercano a from, o null si no hay ninguno.
     */
    public static BlockPos standableBeside(ServerLevel level, BlockPos target, BlockPos from) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos candidate = target.relative(side).offset(0, dy, 0);
                double distance = candidate.distSqr(from);
                if (distance < bestDistance && isStandable(level, candidate)) {
                    bestDistance = distance;
                    best = candidate;
                }
            }
        }
        return best;
    }

    /** Enemigo vivo más cercano a from dentro de radius bloques. Null si no hay ninguno. */
    public static LivingEntity nearestHostile(ServerLevel level, LivingEntity from, double radius) {
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity entity : hostilesWithin(level, from, radius)) {
            double distance = entity.distanceToSqr(from);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    public static int countHostiles(ServerLevel level, LivingEntity from, double radius) {
        return hostilesWithin(level, from, radius).size();
    }

    public static int count(Inventory inventory, Item item) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static int count(Inventory inventory, TagKey<Item> tag) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(tag)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static int emptySlots(Inventory inventory) {
        int empty = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                empty++;
            }
        }
        return empty;
    }

    /** Ranura cuyo objeto cumple match. -1 si no hay ninguna. */
    public static int findSlot(Inventory inventory, Predicate<ItemStack> match) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (match.test(inventory.getItem(slot))) {
                return slot;
            }
        }
        return -1;
    }

    private static List<LivingEntity> hostilesWithin(ServerLevel level, LivingEntity from, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, from.getBoundingBox().inflate(radius),
                entity -> entity instanceof Enemy && entity.isAlive() && entity.distanceTo(from) <= radius);
    }

    private static boolean isFree(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }
}
