package com.ricardo.jugador2.nav;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Vista del mundo de Minecraft para el navegador. */
public final class WorldBlockAccess implements BlockAccess {
    private final ServerLevel level;

    public WorldBlockAccess(ServerLevel level) {
        this.level = level;
    }

    @Override
    public boolean isPassable(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty();
    }

    @Override
    public boolean isSolid(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    public boolean isHazard(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);
        return !state.getFluidState().isEmpty() || state.is(BlockTags.FIRE);
    }
}
