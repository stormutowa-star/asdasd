package com.ricardo.jugador2.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

/** Consultas al mundo y al inventario que usan las tareas. Solo lectura. */
public final class WorldView {
    private WorldView() {
    }

    /** Bloque más cercano a origin que cumple match, dentro de un cubo de radius bloques. Null si no hay ninguno. */
    public static BlockPos findNearestBlock(ServerLevel level, BlockPos origin, Predicate<BlockState> match, int radius) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius),
                origin.offset(radius, radius, radius))) {
            if (match.test(level.getBlockState(pos))) {
                double distance = pos.distSqr(origin);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = pos.immutable();
                }
            }
        }
        return best;
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
