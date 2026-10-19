package com.lenadorbosque;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import com.lenadorbosque.client.ClientSetup;
import com.lenadorbosque.story.StoryCommands;

@Mod(LenadorBosque.MODID)
public class LenadorBosque {
    public static final String MODID = "lenadorbosque";

    public LenadorBosque() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITIES.register(bus);

        bus.addListener(ModEntities::registerAttributes);
        bus.addListener(this::addToCreativeTab);

        StoryCommands.register();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.init(bus);
        }
    }

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.CARTA_DE_TOMAS.get());
            event.accept(ModItems.BROTE_DE_AMBAR.get());
            event.accept(ModItems.ASTA_DE_CIERVO.get());
            event.accept(ModItems.HACHA_DE_TOMAS.get());
            event.accept(ModItems.HOJAS_DE_AMBAR.get());
        }
    }
}
