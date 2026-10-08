package com.ricardo.makuosaga.world;

import com.ricardo.makuosaga.MakuoSaga;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MakuoSaga.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VharosEvents {
	public static final ResourceKey<Level> VHAROS = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(MakuoSaga.MOD_ID, "vharos"));

	private VharosEvents() {
	}

	@SubscribeEvent
	public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getTo().equals(VHAROS) && event.getEntity() instanceof ServerPlayer player) {
			arrive(player, true);
		}
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && player.level().dimension().equals(VHAROS)) {
			arrive(player, false);
		}
	}

	private static void arrive(ServerPlayer player, boolean announce) {
		ServerLevel level = player.serverLevel();
		VharosData data = level.getDataStorage().computeIfAbsent(VharosData::load, VharosData::new, VharosData.ID);
		boolean firstVisit = !data.campBuilt || !data.citadelBuilt;
		if (!data.campBuilt) {
			VharosBuilder.buildCamp(level);
			data.campBuilt = true;
			data.setDirty();
		}
		if (!data.citadelBuilt) {
			VharosBuilder.buildCitadel(level);
			data.citadelBuilt = true;
			data.setDirty();
		}
		if (announce || firstVisit) {
			player.sendSystemMessage(Component.translatable("message.makuosaga.vharos.arrival"));
			player.sendSystemMessage(Component.translatable("message.makuosaga.vharos.citadel"));
		}
	}
}
