package com.lenadorbosque.world;

import com.lenadorbosque.ModBlocks;
import com.lenadorbosque.ModEntities;
import com.lenadorbosque.entity.DeadDeerEntity;
import com.lenadorbosque.entity.WoodcutterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Construye la escena completa en el punto dado. {@code o} es el nivel del suelo de la cabaña.
 * Coordenadas locales: la cabaña ocupa x 0..8 y z 0..6, la puerta está en (4, z=0),
 * el tocón del Roble Madre está en x 3..5 y z -5..-3, y el ciervo en (7, -9).
 */
public final class GroveBuilder {

    private static final int GROUND_RADIUS = 26;

    private GroveBuilder() {}

    public static void build(ServerLevel level, BlockPos o) {
        RandomSource rand = level.getRandom();

        for (int dx = -GROUND_RADIUS; dx <= GROUND_RADIUS; dx++) {
            for (int dz = -GROUND_RADIUS; dz <= GROUND_RADIUS; dz++) {
                level.setBlock(o.offset(dx, -1, dz), Blocks.PODZOL.defaultBlockState(), 2);
            }
        }

        for (int i = 0; i < 70; i++) {
            int dx = rand.nextInt(GROUND_RADIUS * 2 + 1) - GROUND_RADIUS;
            int dz = rand.nextInt(GROUND_RADIUS * 2 + 1) - GROUND_RADIUS;
            // Deja libre la zona de la cabaña, el tocón y el ciervo.
            if (dx >= -5 && dx <= 13 && dz >= -11 && dz <= 10) {
                continue;
            }
            growTree(level, o.offset(dx, 0, dz), rand);
        }

        buildCabin(level, o);
        buildStump(level, o);
        spawnDeer(level, o);
        spawnTomas(level, o);
    }

    private static void growTree(ServerLevel level, BlockPos base, RandomSource rand) {
        int height = 5 + rand.nextInt(3);
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        for (int y = 0; y < height; y++) {
            level.setBlock(base.above(y), log, 2);
        }

        BlockState leaves = ModBlocks.AUTUMN_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true);
        BlockPos top = base.above(height);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 1; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    int distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > 6 + rand.nextInt(2)) {
                        continue;
                    }
                    BlockPos p = top.offset(dx, dy, dz);
                    if (level.isEmptyBlock(p)) {
                        level.setBlock(p, leaves, 2);
                    }
                }
            }
        }
    }

    private static void buildCabin(ServerLevel level, BlockPos o) {
        BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState logs = Blocks.OAK_LOG.defaultBlockState();
        BlockState roof = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 6; z++) {
                level.setBlock(o.offset(x, 0, z), planks, 2);
                boolean border = x == 0 || x == 8 || z == 0 || z == 6;
                boolean corner = (x == 0 || x == 8) && (z == 0 || z == 6);
                for (int y = 1; y <= 3; y++) {
                    if (!border) {
                        level.setBlock(o.offset(x, y, z), air, 2);
                    } else {
                        level.setBlock(o.offset(x, y, z), corner ? logs : planks, 2);
                    }
                }
            }
        }

        // Puerta (hueco) y ventanas
        level.setBlock(o.offset(4, 1, 0), air, 2);
        level.setBlock(o.offset(4, 2, 0), air, 2);
        BlockState pane = Blocks.GLASS_PANE.defaultBlockState();
        level.setBlock(o.offset(0, 2, 3), pane, 2);
        level.setBlock(o.offset(8, 2, 3), pane, 2);
        level.setBlock(o.offset(4, 2, 6), pane, 2);

        // Tejado plano, chimenea
        for (int x = -1; x <= 9; x++) {
            for (int z = -1; z <= 7; z++) {
                level.setBlock(o.offset(x, 4, z), roof, 2);
            }
        }
        BlockState brick = Blocks.STONE_BRICKS.defaultBlockState();
        level.setBlock(o.offset(7, 5, 2), brick, 2);
        level.setBlock(o.offset(7, 6, 2), brick, 2);

        // Mobiliario
        level.setBlock(o.offset(2, 1, 5), Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        level.setBlock(o.offset(6, 1, 5), Blocks.CAMPFIRE.defaultBlockState(), 2);
        level.setBlock(o.offset(7, 1, 1), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(o.offset(2, 3, 3), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // Leña apilada junto a la pared
        for (int z = 3; z <= 6; z++) {
            level.setBlock(o.offset(-2, 0, z), logs, 2);
        }
    }

    private static void buildStump(ServerLevel level, BlockPos o) {
        BlockState stripped = Blocks.STRIPPED_OAK_LOG.defaultBlockState();
        for (int x = 3; x <= 5; x++) {
            for (int z = -5; z <= -3; z++) {
                level.setBlock(o.offset(x, 0, z), stripped, 2);
            }
        }
        // El ámbar del corazón del roble
        level.setBlock(o.offset(4, 0, -4), Blocks.HONEY_BLOCK.defaultBlockState(), 2);
    }

    private static void spawnDeer(ServerLevel level, BlockPos o) {
        DeadDeerEntity deer = ModEntities.DEAD_DEER.get().create(level);
        if (deer == null) {
            return;
        }
        double x = o.getX() + 7.5D;
        double z = o.getZ() - 8.5D;
        double cabinX = o.getX() + 4.5D;
        double cabinZ = o.getZ() + 3.5D;
        // El ciervo queda con la cabeza girada hacia la cabaña.
        float yaw = (float) Math.toDegrees(Math.atan2(-(cabinX - x), cabinZ - z));
        deer.moveTo(x, o.getY(), z, yaw, 0.0F);
        level.addFreshEntity(deer);
    }

    private static void spawnTomas(ServerLevel level, BlockPos o) {
        WoodcutterEntity tomas = ModEntities.WOODCUTTER.get().create(level);
        if (tomas == null) {
            return;
        }
        tomas.moveTo(o.getX() + 4.5D, o.getY(), o.getZ() - 0.5D, 0.0F, 0.0F);
        level.addFreshEntity(tomas);
    }
}
