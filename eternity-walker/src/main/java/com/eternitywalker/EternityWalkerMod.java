package com.eternitywalker;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(EternityWalkerMod.MODID)
public class EternityWalkerMod {
    public static final String MODID = "eternitywalker";

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<EntityType<WalkerEntity>> WALKER = ENTITIES.register("eternity_walker",
            () -> EntityType.Builder.<WalkerEntity>of(WalkerEntity::new, MobCategory.MISC)
                    .sized(6.0f, 14.0f)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .fireImmune()
                    .build("eternity_walker"));

    public static final RegistryObject<Item> CORE = ITEMS.register("eternity_core",
            () -> new WalkerItem(new Item.Properties().stacksTo(1)));

    public EternityWalkerMod(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        ENTITIES.register(bus);
        ITEMS.register(bus);
        bus.addListener(this::addCreative);
        Net.init();
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS || event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(CORE);
        }
    }
}
