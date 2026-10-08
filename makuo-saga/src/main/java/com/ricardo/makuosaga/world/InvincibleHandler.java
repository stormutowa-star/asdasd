package com.ricardo.makuosaga.world;

import com.ricardo.makuosaga.MakuoSaga;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * "Story mode" for players tagged makuo_invencible (/tag @s add makuo_invencible): enemies still target and hit
 * them (knockback, hurt animation, stun), but the damage itself is cancelled. Creative players are never targeted,
 * so this is meant for survival. Cancelling here, before DragonMineZ's own LOWEST-priority damage override,
 * also stops DragonMineZ from re-applying the hit; vanilla Resistance does not, because DMZ recomputes the amount.
 */
@Mod.EventBusSubscriber(modid = MakuoSaga.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InvincibleHandler {
	public static final String TAG = "makuo_invencible";

	private InvincibleHandler() {
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onDamage(LivingDamageEvent event) {
		if (event.getEntity() instanceof Player player && player.getTags().contains(TAG)) {
			event.setCanceled(true);
		}
	}
}
