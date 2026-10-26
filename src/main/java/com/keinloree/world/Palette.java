package com.keinloree.world;

import com.keinloree.registry.ModBlocks;
import com.keinloree.world.design.Mat;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Traduce los materiales de los disenos (Mat) a bloques de Minecraft. */
public final class Palette {

    private static BlockState[] table;

    private Palette() {
    }

    /** Bloque del material; null si la celda no se toca (Mat.NONE). */
    public static BlockState of(byte mat) {
        if (table == null) {
            table = build();
        }
        return mat >= 0 && mat < table.length ? table[mat] : null;
    }

    private static BlockState stair(Direction facing, Half half) {
        return Blocks.POLISHED_BLACKSTONE_STAIRS.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.HALF, half);
    }

    private static BlockState ladder(Direction facing) {
        return Blocks.LADDER.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
    }

    private static BlockState rod(Direction facing) {
        return Blocks.END_ROD.defaultBlockState().setValue(BlockStateProperties.FACING, facing);
    }

    private static BlockState door(DoubleBlockHalf half) {
        return Blocks.IRON_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, half);
    }

    private static BlockState[] build() {
        BlockState[] t = new BlockState[Mat.COUNT];
        t[Mat.NONE] = null;
        t[Mat.AIR] = Blocks.AIR.defaultBlockState();
        t[Mat.HULL_A] = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        t[Mat.HULL_B] = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        t[Mat.HULL_C] = Blocks.DEEPSLATE_TILES.defaultBlockState();
        t[Mat.HULL_D] = Blocks.BLACKSTONE.defaultBlockState();
        t[Mat.HULL_E] = Blocks.POLISHED_TUFF.defaultBlockState();
        t[Mat.HULL_F] = Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        t[Mat.TRIM] = Blocks.GREEN_CONCRETE.defaultBlockState();
        t[Mat.GLOW] = Blocks.VERDANT_FROGLIGHT.defaultBlockState();
        t[Mat.LAMP] = Blocks.SEA_LANTERN.defaultBlockState();
        t[Mat.GLASS] = Blocks.GREEN_STAINED_GLASS.defaultBlockState();
        t[Mat.GLASS_L] = Blocks.LIME_STAINED_GLASS.defaultBlockState();
        t[Mat.FLOOR] = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        t[Mat.FLOOR2] = Blocks.DEEPSLATE_TILES.defaultBlockState();
        t[Mat.WALL] = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        t[Mat.WALL2] = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        t[Mat.EMERALD] = Blocks.EMERALD_BLOCK.defaultBlockState();
        t[Mat.BLACK] = Blocks.BLACK_CONCRETE.defaultBlockState();
        t[Mat.CRATE] = Blocks.GREEN_TERRACOTTA.defaultBlockState();
        t[Mat.CHISEL] = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        t[Mat.IRON] = Blocks.IRON_BLOCK.defaultBlockState();
        t[Mat.ROD_UP] = rod(Direction.UP);
        t[Mat.ROD_N] = rod(Direction.NORTH);
        t[Mat.ROD_S] = rod(Direction.SOUTH);
        t[Mat.LADDER_E] = ladder(Direction.EAST);
        t[Mat.LADDER_W] = ladder(Direction.WEST);
        t[Mat.LADDER_N] = ladder(Direction.NORTH);
        t[Mat.LADDER_S] = ladder(Direction.SOUTH);
        t[Mat.SLAB_B] = Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM);
        t[Mat.SLAB_T] = Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);
        t[Mat.STAIR_N] = stair(Direction.NORTH, Half.BOTTOM);
        t[Mat.STAIR_E] = stair(Direction.EAST, Half.BOTTOM);
        t[Mat.STAIR_S] = stair(Direction.SOUTH, Half.BOTTOM);
        t[Mat.STAIR_W] = stair(Direction.WEST, Half.BOTTOM);
        t[Mat.STAIR_N_T] = stair(Direction.NORTH, Half.TOP);
        t[Mat.STAIR_E_T] = stair(Direction.EAST, Half.TOP);
        t[Mat.STAIR_S_T] = stair(Direction.SOUTH, Half.TOP);
        t[Mat.STAIR_W_T] = stair(Direction.WEST, Half.TOP);
        t[Mat.LIME] = Blocks.LIME_CONCRETE.defaultBlockState();
        t[Mat.SMOOTH] = Blocks.SMOOTH_STONE.defaultBlockState();
        t[Mat.NETHERITE] = Blocks.NETHERITE_BLOCK.defaultBlockState();
        t[Mat.HELM] = ModBlocks.HELM.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
        t[Mat.BARS_X] = Blocks.IRON_BARS.defaultBlockState()
                .setValue(BlockStateProperties.EAST, true).setValue(BlockStateProperties.WEST, true);
        t[Mat.BARS_Z] = Blocks.IRON_BARS.defaultBlockState()
                .setValue(BlockStateProperties.NORTH, true).setValue(BlockStateProperties.SOUTH, true);
        t[Mat.DOOR_LO] = door(DoubleBlockHalf.LOWER);
        t[Mat.DOOR_HI] = door(DoubleBlockHalf.UPPER);
        return t;
    }
}
