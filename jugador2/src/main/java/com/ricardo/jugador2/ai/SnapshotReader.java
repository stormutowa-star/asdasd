package com.ricardo.jugador2.ai;

import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

/** Foto del estado del NPC que el cerebro usa para elegir tarea. */
public final class SnapshotReader {
    private static final long DAY_LENGTH = 24000L;
    private static final long NIGHT_START = 13000L;
    private static final long NIGHT_END = 23000L;
    private static final int BED_SEARCH_RADIUS = 16;
    private static final int NEARLY_FULL_SLOTS = 2;

    private SnapshotReader() {
    }

    public static NpcSnapshot read(NpcContext ctx) {
        NpcPlayer npc = ctx.npc();
        ServerLevel level = npc.serverLevel();
        Inventory inventory = npc.getInventory();
        long time = level.getDayTime() % DAY_LENGTH;
        boolean night = time >= NIGHT_START && time < NIGHT_END;
        boolean hasPickaxe = WorldView.count(inventory, ItemTags.PICKAXES) > 0;
        boolean hasStonePickaxe = WorldView.count(inventory, Items.STONE_PICKAXE) > 0
                || WorldView.count(inventory, Items.IRON_PICKAXE) > 0
                || WorldView.count(inventory, Items.DIAMOND_PICKAXE) > 0;
        boolean hasBed = night && WorldView.findNearestBlock(level, npc.blockPosition(),
                state -> state.is(BlockTags.BEDS), BED_SEARCH_RADIUS) != null;
        return new NpcSnapshot(
                npc.getHealth(),
                npc.getFoodData().getFoodLevel(),
                WorldView.findSlot(inventory, stack -> stack.has(DataComponents.FOOD)) >= 0,
                WorldView.countHostiles(level, npc, ctx.settings().hostileDetectionRadius()),
                ctx.order() != null && ctx.order().status() == OrderState.Status.ACTIVE,
                night,
                hasBed,
                WorldView.count(inventory, ItemTags.LOGS),
                WorldView.count(inventory, Items.COBBLESTONE),
                WorldView.count(inventory, Items.COAL),
                hasPickaxe,
                !hasStonePickaxe,
                WorldView.emptySlots(inventory) <= NEARLY_FULL_SLOTS);
    }
}
