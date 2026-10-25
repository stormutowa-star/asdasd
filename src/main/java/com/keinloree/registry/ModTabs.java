package com.keinloree.registry;

import com.keinloree.KeinLooreMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Pestana creativa del mod. */
public final class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KeinLooreMod.MODID);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("keinloree_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.keinloree"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItems.DECODE_CORE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.DECODE_CORE.get());
                output.accept(ModItems.KEIN_LOREE_EGG.get());
                output.accept(ModItems.DROID_EGG.get());
                output.accept(ModItems.COMMANDER_EGG.get());
                output.accept(ModItems.WARLORD_EGG.get());
                output.accept(ModItems.CARGO_SHIP_EGG.get());
            })
            .build());

    private ModTabs() {}

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
