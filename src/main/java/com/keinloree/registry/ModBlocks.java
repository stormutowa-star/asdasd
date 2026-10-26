package com.keinloree.registry;

import com.keinloree.KeinLooreMod;
import com.keinloree.ship.HelmBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Bloques del mod (los paquetes de la nave y los pilares anaden los suyos aqui). */
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, KeinLooreMod.MODID);

    /** Timon del destructor (lo coloca el diseno de la nave; con objeto en creativo para pruebas). */
    public static final RegistryObject<Block> HELM = BLOCKS.register("helm", () -> new HelmBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(-1.0F, 3600000.0F)
            .noLootTable()
            .noOcclusion()
            .sound(SoundType.NETHERITE_BLOCK)
            .lightLevel(s -> 7)
            .pushReaction(PushReaction.BLOCK)));

    private ModBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
