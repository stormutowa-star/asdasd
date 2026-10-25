package com.keinloree.client;

import com.keinloree.KeinLooreMod;
import com.keinloree.client.cam.CinematicCamera;
import com.keinloree.client.ui.AtmosphereClient;
import com.keinloree.client.ui.ChoiceScreen;
import com.keinloree.client.ui.DialogueOverlay;
import com.keinloree.client.ui.KlText;
import com.keinloree.client.ui.ObjectiveHud;
import com.keinloree.client.ui.ScreenFxOverlay;
import com.keinloree.client.ui.TitleCard;
import com.keinloree.client.ui.WhisperHud;
import com.keinloree.net.Net;
import com.keinloree.net.SkipC2S;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Enganche de los sistemas de historia en el cliente: capas de HUD, teclas y eventos de camara. */
public final class StoryClient {
    /** Tecla "Avanzar diálogo" (Enter). */
    public static final KeyMapping NEXT = new KeyMapping("key.keinloree.next", InputConstants.KEY_RETURN,
            "key.categories.keinloree");

    private static final int HOLD_TO_SKIP = 20;
    private static int hold;
    private static boolean skipSent;
    private static float time;

    private StoryClient() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(StoryClient::onKeys);
        modBus.addListener(StoryClient::onLayers);
        MinecraftForge.EVENT_BUS.register(new StoryClient());
    }

    private static void onKeys(RegisterKeyMappingsEvent e) {
        e.register(NEXT);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, path);
    }

    private static void onLayers(AddGuiOverlayLayersEvent e) {
        ForgeLayeredDraw d = e.getLayeredDraw();
        d.add(id("letterbox"), (gg, dt) -> ScreenFxOverlay.renderBars(gg, pt(dt), w(), h()));
        d.add(id("atmosphere"), (gg, dt) -> ScreenFxOverlay.renderAtmosphere(gg, w(), h(), AtmosphereClient.tint(pt(dt))));
        d.addWithCondition(id("objective"), (gg, dt) -> ObjectiveHud.render(gg, pt(dt), w(), h()),
                () -> !Minecraft.getInstance().options.hideGui);
        d.add(id("screen_fx"), (gg, dt) -> ScreenFxOverlay.render(gg, pt(dt), w(), h(), time + pt(dt)));
        d.addWithCondition(id("dialogue"), (gg, dt) -> DialogueOverlay.render(gg, pt(dt), w(), h()),
                () -> !Minecraft.getInstance().options.hideGui || CinematicCamera.isActive());
        d.addWithCondition(id("whisper"), (gg, dt) -> WhisperHud.render(gg, pt(dt), w(), h()),
                () -> !Minecraft.getInstance().options.hideGui || CinematicCamera.isActive());
        d.add(id("skip_hint"), (gg, dt) -> renderSkipHint(gg, w(), h()));
        d.add(id("title_card"), (gg, dt) -> TitleCard.render(gg, pt(dt), w(), h()));
    }

    private static float pt(net.minecraft.client.DeltaTracker dt) {
        return dt.getGameTimeDeltaPartialTick(false);
    }

    private static int w() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    private static int h() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    /** Indicador "Mantén ENTER para saltar" con barra de progreso. */
    private static void renderSkipHint(GuiGraphics gg, int w, int h) {
        if (!CinematicCamera.isSkippable()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        String s = I18n.get("keinloree.cine.skip", NEXT.getTranslatedKeyMessage().getString());
        int x = w - mc.font.width(s) - 8;
        int y = h - Math.round(h * CinematicCamera.barFraction(1.0F)) + 3;
        gg.drawString(mc.font, s, x, y, KlText.argb(0xFFFFFF, 0.6F), true);
        if (hold > 0) {
            int bw = mc.font.width(s);
            gg.fill(x, y + 10, x + (int) (bw * Math.min(1.0F, hold / (float) HOLD_TO_SKIP)), y + 11, 0xFFFFFFFF);
        }
    }

    // ---- eventos del bus de Forge ----

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        time++;
        if (mc.level == null || mc.player == null) {
            return;
        }
        CinematicCamera.tick(mc);
        DialogueOverlay.tick();
        TitleCard.tick();
        WhisperHud.tick();
        ObjectiveHud.tick();
        ScreenFxOverlay.tick();
        AtmosphereClient.tick();
        ChoiceScreen.tick(mc);
        tickSkipKey(mc);
        if (!mc.player.isAlive()) {
            ScreenFxOverlay.clear();
        }
    }

    private static void tickSkipKey(Minecraft mc) {
        if (mc.screen != null) {
            hold = 0;
            return;
        }
        if (CinematicCamera.isSkippable()) {
            while (NEXT.consumeClick()) {
                // un toque no hace nada: hay que mantener
            }
            if (NEXT.isDown()) {
                hold++;
                if (hold >= HOLD_TO_SKIP && !skipSent) {
                    skipSent = true;
                    Net.toServer(new SkipC2S());
                }
            } else {
                hold = 0;
                skipSent = false;
            }
        } else {
            hold = 0;
            skipSent = false;
            while (NEXT.consumeClick()) {
                Net.toServer(new SkipC2S());
            }
        }
    }

    @SubscribeEvent
    public void onFov(ViewportEvent.ComputeFov e) {
        if (CinematicCamera.isActive() && e.usedConfiguredFov()) {
            e.setFOV(CinematicCamera.modifyFov(e.getFOV(), (float) e.getPartialTick()));
        }
    }

    @SubscribeEvent
    public void onAngles(ViewportEvent.ComputeCameraAngles e) {
        float p = (float) e.getPartialTick();
        float roll = CinematicCamera.roll(p);
        float amp = ScreenFxOverlay.shakeAmount(p);
        if (amp > 0.0F) {
            float t = time + p;
            e.setYaw(e.getYaw() + amp * (Mth_sin(t * 1.7F) + Mth_sin(t * 3.1F)) * 0.5F);
            e.setPitch(e.getPitch() + amp * (Mth_sin(t * 2.3F + 1.0F) + Mth_sin(t * 4.3F)) * 0.5F);
            roll += amp * 0.6F * Mth_sin(t * 2.9F + 2.0F);
        }
        if (roll != 0.0F) {
            e.setRoll(e.getRoll() + roll);
        }
    }

    private static float Mth_sin(float v) {
        return net.minecraft.util.Mth.sin(v);
    }

    @SubscribeEvent
    public void onFogColor(ViewportEvent.ComputeFogColor e) {
        AtmosphereClient.onFogColor(e);
    }

    @SubscribeEvent
    public void onFog(ViewportEvent.RenderFog e) {
        AtmosphereClient.onFog(e);
    }

    /** Congela el movimiento del jugador durante las cinematicas. */
    @SubscribeEvent
    public void onMove(MovementInputUpdateEvent e) {
        if (CinematicCamera.isActive()) {
            var in = e.getInput();
            in.leftImpulse = 0.0F;
            in.forwardImpulse = 0.0F;
            in.up = false;
            in.down = false;
            in.left = false;
            in.right = false;
            in.jumping = false;
            in.shiftKeyDown = false;
        }
    }

    @SubscribeEvent
    public void onInteract(InputEvent.InteractionKeyMappingTriggered e) {
        if (CinematicCamera.isActive()) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        CinematicCamera.end();
        DialogueOverlay.clear();
        TitleCard.clear();
        WhisperHud.clear();
        ObjectiveHud.clear();
        ScreenFxOverlay.clear();
        AtmosphereClient.reset();
        ChoiceScreen.reset();
    }
}
