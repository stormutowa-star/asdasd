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
    public static final RegistryObject<Item> RAREIRI = ITEMS.register("rareiri", () -> new com.keinloree.item.RareIriItem(
            new Item.Properties().attributes(net.minecraft.world.item.SwordItem.createAttributes(com.keinloree.item.KitItems.RAREIRI_TIER, 3, -2.2F))
                    .fireResistant().rarity(net.minecraft.world.item.Rarity.EPIC)
                    .component(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))));
    public static final RegistryObject<Item> SEXTA_HORA_HELMET = armor("sexta_hora_helmet", net.minecraft.world.item.ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SEXTA_HORA_CHESTPLATE = armor("sexta_hora_chestplate", net.minecraft.world.item.ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SEXTA_HORA_LEGGINGS = armor("sexta_hora_leggings", net.minecraft.world.item.ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SEXTA_HORA_BOOTS = armor("sexta_hora_boots", net.minecraft.world.item.ArmorItem.Type.BOOTS);

    private static RegistryObject<Item> armor(String id, net.minecraft.world.item.ArmorItem.Type type) {
        return ITEMS.register(id, () -> new net.minecraft.world.item.ArmorItem(com.keinloree.item.KitItems.SEXTA_HORA, type,
                new Item.Properties().fireResistant().rarity(net.minecraft.world.item.Rarity.EPIC)
                        .component(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))));
    }

    public static final RegistryObject<Item> HELM = ITEMS.register("helm",
            () -> new BlockItem(ModBlocks.HELM.get(), new Item.Properties()));

    public static final RegistryObject<Item> HOUR_ANCHOR = ITEMS.register("hour_anchor",
            () -> new BlockItem(ModBlocks.HOUR_ANCHOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> NEXO_GENERATOR = ITEMS.register("nexo_generator",
            () -> new BlockItem(ModBlocks.NEXO_GENERATOR.get(), new Item.Properties()));

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
