package com.storm.greatsword;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = GreatswordMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    public static final String CATEGORY = "key.greatsword.category";

    public static final KeyMapping DRAW_KEY = new KeyMapping(
            "key.greatsword.draw",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            CATEGORY);

    public static final KeyMapping TACKLE_KEY = new KeyMapping(
            "key.greatsword.tackle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY);

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        GreatswordMod.LOGGER.info("Hunter's Greatsword client setup complete.");
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(DRAW_KEY);
        event.register(TACKLE_KEY);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer renderer = event.getPlayerSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new BackSheathLayer(renderer));
            }
        }
    }
}
