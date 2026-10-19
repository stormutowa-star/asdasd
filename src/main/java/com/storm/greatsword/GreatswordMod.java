package com.storm.greatsword;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

/**
 * Hunter's Greatsword - a huge two-handed Monster Hunter style greatsword.
 */
@Mod(GreatswordMod.MODID)
public class GreatswordMod {
    public static final String MODID = "greatsword";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    /**
     * Whether this exact stack is currently drawn (held with both hands) or sheathed on the player's back.
     */
    public static final RegistryObject<DataComponentType<Boolean>> DRAWN =
            DATA_COMPONENTS.register("drawn", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    public static final RegistryObject<Item> GREATSWORD =
            ITEMS.register("greatsword", ModItems::greatsword);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.greatsword"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> GREATSWORD.get().getDefaultInstance())
                    .displayItems((params, output) -> output.accept(GREATSWORD.get()))
                    .build());

    public GreatswordMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreativeTabItems);

        ITEMS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        TABS.register(modEventBus);

        ModNetwork.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Hunter's Greatsword loaded. Z draws/sheathes, V tackles, right click charges a slam.");
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(GREATSWORD.get());
        }
    }
}
