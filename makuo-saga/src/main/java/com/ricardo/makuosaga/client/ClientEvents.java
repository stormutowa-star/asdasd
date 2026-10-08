package com.ricardo.makuosaga.client;

import com.dragonminez.client.init.entities.renderer.sagas.DBSagasRenderer;
import com.dragonminez.common.init.entities.sagas.DBSagasEntity;
import com.ricardo.makuosaga.MakuoSaga;
import com.ricardo.makuosaga.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MakuoSaga.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
	private ClientEvents() {
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		for (var type : ModEntities.all()) {
			register(event, type.get());
		}
	}

	private static <T extends DBSagasEntity> void register(EntityRenderersEvent.RegisterRenderers event, EntityType<T> type) {
		event.registerEntityRenderer(type, DBSagasRenderer<T>::new);
	}
}
