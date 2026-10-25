package com.keinloree;

import com.keinloree.client.KeinLooreClient;
import com.keinloree.events.LooreEvents;
import com.keinloree.net.Net;
import com.keinloree.registry.ModArmorMaterials;
import com.keinloree.registry.ModBlocks;
import com.keinloree.registry.ModEntities;
import com.keinloree.registry.ModItems;
import com.keinloree.registry.ModParticles;
import com.keinloree.registry.ModSounds;
import com.keinloree.registry.ModTabs;
import com.keinloree.story.StoryBootstrap;
import com.keinloree.story.StoryEvents;
import com.keinloree.test.AutoTest;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Kein Loree: arranque del mod. Los registros viven en {@code com.keinloree.registry}. */
@Mod(KeinLooreMod.MODID)
public class KeinLooreMod {
    public static final String MODID = "keinloree";

    public KeinLooreMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ModEntities.register(modBus);
        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModSounds.register(modBus);
        ModParticles.register(modBus);
        ModArmorMaterials.register(modBus);
        ModTabs.register(modBus);
        modBus.addListener(this::onCommonSetup);

        MinecraftForge.EVENT_BUS.register(new LooreEvents());
        MinecraftForge.EVENT_BUS.register(new StoryEvents());
        AutoTest.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            KeinLooreClient.register(modBus);
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Net.init();
            StoryBootstrap.init();
        });
    }
}
