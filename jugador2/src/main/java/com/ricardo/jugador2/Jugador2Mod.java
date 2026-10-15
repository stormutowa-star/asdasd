package com.ricardo.jugador2;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Jugador2Mod.MOD_ID)
public class Jugador2Mod {
    public static final String MOD_ID = "jugador2";

    public Jugador2Mod(FMLJavaModLoadingContext context) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Jugador2Config.SPEC);
    }
}
