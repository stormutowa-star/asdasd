package com.ricardo.frutigeraero;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FrutigerAeroMod.MODID)
public class FrutigerAeroMod {
    public static final String MODID = "frutigeraero";

    public FrutigerAeroMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        // Los DeferredRegister se registran en modEventBus en tareas posteriores.
    }
}
