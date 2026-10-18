package com.ricardo.frutigerworld;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

@Mod(FrutigerWorldMod.MODID)
public class FrutigerWorldMod {
    public static final String MODID = "frutigerworld";

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MODID);

    public FrutigerWorldMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        FEATURES.register("frutiger_structure", () -> new FrutigerFeature(FrutigerFeature.Config.CODEC));
        FEATURES.register(modEventBus);
    }
}
