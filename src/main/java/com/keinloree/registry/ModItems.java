package com.keinloree.registry;

import com.keinloree.KeinLooreMod;
import com.keinloree.item.DecodeCoreItem;
import com.keinloree.item.LooreEggItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Objetos del mod. */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, KeinLooreMod.MODID);

    /** Nucleo de Decodificacion: ya no inicia la mision; solo susurra (la nave llega sola). */
    public static final RegistryObject<Item> DECODE_CORE = ITEMS.register("decode_core",
            () -> new DecodeCoreItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> KEIN_LOREE_EGG = ITEMS.register("kein_loree_egg",
            () -> new LooreEggItem(() -> ModEntities.KEIN_LOREE.get(), new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> DROID_EGG = ITEMS.register("droid_egg",
            () -> new LooreEggItem(() -> ModEntities.DROID.get(), new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> COMMANDER_EGG = ITEMS.register("commander_egg",
            () -> new LooreEggItem(() -> ModEntities.LOREE_COMMANDER.get(), new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> WARLORD_EGG = ITEMS.register("warlord_egg",
            () -> new LooreEggItem(() -> ModEntities.LOREE_WARLORD.get(), new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> CARGO_SHIP_EGG = ITEMS.register("cargo_ship_egg",
            () -> new LooreEggItem(() -> ModEntities.CARGO_SHIP.get(), new Item.Properties().stacksTo(16)));

    /** Timon del destructor (solo para pruebas en creativo). */
    public static final RegistryObject<Item> HELM = ITEMS.register("helm",
            () -> new BlockItem(ModBlocks.HELM.get(), new Item.Properties()));

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
