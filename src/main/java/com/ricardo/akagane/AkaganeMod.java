package com.ricardo.akagane;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(AkaganeMod.MODID)
public class AkaganeMod {
    public static final String MODID = "akagane";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Item> AKAGANE = ITEMS.register("akagane", () -> new AkaganeItem(
            new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()
                    .rarity(Rarity.EPIC)
                    .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4, -2.2F))));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("akagane_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.akagane"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> AKAGANE.get().getDefaultInstance())
            .displayItems((parameters, output) -> output.accept(AKAGANE.get()))
            .build());

    public AkaganeMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(new AkaganeEvents());
    }
}
