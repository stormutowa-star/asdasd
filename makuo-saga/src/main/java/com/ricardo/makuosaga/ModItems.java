package com.ricardo.makuosaga;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MakuoSaga.MOD_ID);

	/** Dropped by Zhar Elites; quest 10 asks for four of them. */
	public static final RegistryObject<Item> VOID_SHARD = ITEMS.register("void_shard",
			() -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

	private ModItems() {
	}

	static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
			event.accept(VOID_SHARD);
		}
	}
}
