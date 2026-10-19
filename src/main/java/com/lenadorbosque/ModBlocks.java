package com.lenadorbosque;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, LenadorBosque.MODID);

    /** Hojas doradas de la arboleda. Usan una textura propia en vez de la tinta del bioma. */
    public static final RegistryObject<Block> AUTUMN_LEAVES = BLOCKS.register("hojas_de_ambar",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)));

    private ModBlocks() {}
}
