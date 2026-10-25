package com.keinloree.client.test;

import com.keinloree.client.net.ClientPackets;
import com.keinloree.client.ui.ChoiceScreen;
import com.keinloree.net.HudS2C;
import com.keinloree.net.ScreenFxS2C;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Escenarios de cliente del WP1b: dialogue, cine, hud y fx. */
final class StoryScenarios {
    private StoryScenarios() {}

    static void registerAll() {
        // Escena de prueba: titulo, linea, narrador, susurro, voz (glitch) y eleccion
        ClientAutoTest.register("dialogue", s -> s.wait(200).cmd("time set noon").wait(20)
                .cmd("keinloree scene prueba")
                .wait(35).shot("dialogue_title")
                .wait(40).shot("dialogue_line")
                .wait(75).shot("dialogue_narrator")
                .wait(70).shot("dialogue_whisper")
                .wait(45).shot("dialogue_glitch")
                .run(mc -> ChoiceShot.arm("dialogue_choice"))
                .wait(120).shot("dialogue_after").quit());
        ClientAutoTest.register("cine", s -> s.wait(200).cmd("time set noon").wait(20)
                .cmd("keinloree cine prueba_orbita")
                .wait(44).shot("cine_25")
                .wait(40).shot("cine_50")
                .wait(40).shot("cine_75")
                .wait(100).shot("cine_after").quit());
        ClientAutoTest.register("hud", s -> s.wait(200).cmd("time set noon").wait(20)
                .run(mc -> ClientPackets.onHud(new HudS2C("Llega al faro del norte", true,
                        mc.player.blockPosition().offset(40, 0, -30), "Faro", 2, 5, 0x4CFF7A)))
                .wait(20).shot("hud_marker").quit());
        ClientAutoTest.register("fx", s -> s.wait(200).cmd("time set noon").wait(20)
                .run(mc -> fx(0, 40, 1.0F, 0xFFFFFF)).wait(2).shot("fx_flash")
                .wait(60).run(mc -> fx(5, 60, 1.0F, 0x4CFF7A)).wait(25).shot("fx_tint")
                .wait(60).run(mc -> fx(6, 40, 1.0F, 0x4CFF7A)).wait(10).shot("fx_glitch")
                .wait(40).run(mc -> fx(8, 60, 1.0F, 0xFFFFFF)).wait(25).shot("fx_time")
                .wait(60).cmd("keinloree cine prueba_orbita").wait(35).shot("fx_letterbox").quit());
    }

    private static void fx(int type, int ticks, float intensity, int color) {
        ClientPackets.onScreenFx(new ScreenFxS2C(type, ticks, intensity, color));
    }

    /** Espera a que se abra la eleccion, la captura y elige la primera opcion (el script se detiene con pantallas abiertas). */
    static final class ChoiceShot {
        private final String name;
        private int seen;

        private ChoiceShot(String name) {
            this.name = name;
        }

        static void arm(String name) {
            MinecraftForge.EVENT_BUS.register(new ChoiceShot(name));
        }

        @SubscribeEvent
        public void onTick(TickEvent.ClientTickEvent.Post e) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof ChoiceScreen && ++seen >= 15) {
                File dir = mc.gameDirectory;
                Screenshot.grab(dir, name + ".png", mc.getMainRenderTarget(), msg -> { });
                if (seen >= 25) {
                    ChoiceScreen.choose(mc, 0);
                    MinecraftForge.EVENT_BUS.unregister(this);
                }
            }
        }
    }
}
