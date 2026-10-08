package com.ricardo.orangesaiyan.client;

import com.dragonminez.common.config.ConfigManager;
import com.dragonminez.common.config.FormConfig;
import com.ricardo.orangesaiyan.OrangeSaiyan;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Makes the Super Saiyan Orange cracks pulse like lava. DragonMineZ tints a form's extra layer with
 * FormData#getRgbExtraFormColor() every frame, so changing that cached color animates the cracks
 * without touching DragonMineZ's renderer.
 */
@Mod.EventBusSubscriber(modid = OrangeSaiyan.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FormEffects {
	private static final String CRACK_LAYER = "orange_cracks";
	private static final float[] DARK = {0.54F, 0.14F, 0.0F};   // cooling lava #8A2400
	private static final float[] BRIGHT = {1.0F, 0.75F, 0.30F}; // molten orange #FFC04D
	private static final float PERIOD_TICKS = 28.0F;

	private FormEffects() {
	}

	@SubscribeEvent
	public static void onRenderTick(TickEvent.RenderTickEvent event) {
		if (event.phase != TickEvent.Phase.START) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		FormConfig group = ConfigManager.getFormGroup("saiyan", "orangesaiyan");
		if (group == null) return;

		float time = mc.level.getGameTime() + event.renderTickTime;
		float wave = (Mth.sin(time * (float) (Math.PI * 2.0D) / PERIOD_TICKS) + 1.0F) * 0.5F;
		float pulse = wave * wave; // stays dark longer, flares briefly: like a heartbeat of lava
		for (FormConfig.FormData form : group.getForms().values()) {
			if (form == null || form.getExtraFormLayer() == null || !form.getExtraFormLayer().contains(CRACK_LAYER)) continue;
			float[] color = form.getRgbExtraFormColor();
			if (color == null || color.length < 3) continue;
			for (int i = 0; i < 3; i++) color[i] = Mth.lerp(pulse, DARK[i], BRIGHT[i]);
		}
	}
}
