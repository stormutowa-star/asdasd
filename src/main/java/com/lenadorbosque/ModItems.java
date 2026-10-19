package com.lenadorbosque;

import com.lenadorbosque.item.LetterItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, LenadorBosque.MODID);

    /** Carta de Tomás. Al usarla empieza (o retoma) la historia. */
    public static final RegistryObject<Item> CARTA_DE_TOMAS =
            ITEMS.register("carta_de_tomas", () -> new LetterItem(new Item.Properties().stacksTo(1)));

    /** Se obtiene si respetas al ciervo. Es necesario para el final bueno. */
    public static final RegistryObject<Item> BROTE_DE_AMBAR =
            ITEMS.register("brote_de_ambar", () -> new Item(new Item.Properties().stacksTo(1)));

    /** Trofeo que se obtiene si profanas al ciervo. Altera lo que dice Tomás. */
    public static final RegistryObject<Item> ASTA_DE_CIERVO =
            ITEMS.register("asta_de_ciervo", () -> new Item(new Item.Properties().stacksTo(1)));

    /** El hacha de Tomás. Tomarla es uno de los finales. */
    public static final RegistryObject<Item> HACHA_DE_TOMAS =
            ITEMS.register("hacha_de_tomas", () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> HOJAS_DE_AMBAR =
            ITEMS.register("hojas_de_ambar", () -> new BlockItem(ModBlocks.AUTUMN_LEAVES.get(), new Item.Properties()));

    private ModItems() {}
}
