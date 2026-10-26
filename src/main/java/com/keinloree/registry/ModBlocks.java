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

    /** Ancla de luz de un pilar: solo se rompe cuando la historia lo permite (ver Pillars). */
    public static final RegistryObject<Block> HOUR_ANCHOR = BLOCKS.register("hour_anchor", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.GOLD)
            .strength(4.0F, 1200.0F)
            .noLootTable()
            .sound(SoundType.AMETHYST)
            .lightLevel(s -> 15)
            .pushReaction(PushReaction.BLOCK)));

    /** Generador de cadenas del bloque de detencion del destructor (se puede romper aunque el casco este protegido). */
    public static final RegistryObject<Block> NEXO_GENERATOR = BLOCKS.register("nexo_generator", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GREEN)
            .strength(3.0F, 1200.0F)
            .noLootTable()
            .sound(SoundType.NETHERITE_BLOCK)
            .lightLevel(s -> 12)
            .pushReaction(PushReaction.BLOCK)));

    private ModBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
