package com.ricardo.makuosaga;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * La Saga de Makuo: a DragonMineZ addon with a new story saga, its villains and the planet Vharos.
 */
@Mod(MakuoSaga.MOD_ID)
public class MakuoSaga {
	public static final String MOD_ID = "makuosaga";

	public MakuoSaga() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModEntities.ENTITY_TYPES.register(modBus);
		ModItems.ITEMS.register(modBus);
		modBus.addListener(ModEntities::registerAttributes);
		modBus.addListener(ModItems::addToCreativeTab);
	}
}
