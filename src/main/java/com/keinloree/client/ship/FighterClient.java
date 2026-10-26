package com.keinloree.client.ship;

import com.keinloree.KeinLooreMod;
import com.keinloree.net.Net;
import com.keinloree.net.ShipInputC2S;
import com.keinloree.ship.DestroyerEntity;
import com.keinloree.ship.FighterEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Mandos del caza en el cliente: disparo (clic izquierdo), impulso (Espacio), cabina sin nubes y HUD sencillo. */
@Mod.EventBusSubscriber(modid = KeinLooreMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FighterClient {
    private FighterClient() {
    }

    private static FighterEntity piloted(Minecraft mc) {
        return mc.player != null && mc.player.getVehicle() instanceof FighterEntity f && f.getControllingPassenger() == mc.player ? f : null;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        boolean inFighter = mc.player != null && mc.player.getVehicle() instanceof FighterEntity;
        OverworldFx.setInCockpit(inFighter);
        FighterEntity f = piloted(mc);
        if (f == null || mc.screen != null) {
            FighterEntity.clientButtons = 0;
            return;
        }
        int buttons = 0;
        if (mc.options.keyAttack.isDown()) {
            buttons |= DestroyerEntity.BTN_FIRE;
        }
        if (mc.options.keyJump.isDown()) {
            buttons |= DestroyerEntity.BTN_BOOST;
        }
        FighterEntity.clientButtons = buttons;
        if ((buttons & DestroyerEntity.BTN_FIRE) != 0) {
            Net.toServer(new ShipInputC2S(0.0F, 0.0F, 0.0F, buttons));
        }
    }

    /** En la cabina el clic izquierdo dispara: no golpea ni rompe bloques. */
    @SubscribeEvent
    public static void onInteract(InputEvent.InteractionKeyMappingTriggered event) {
        if (piloted(Minecraft.getInstance()) != null && event.isAttack()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    /** Se registra en el bus del mod desde KeinLooreClient. */
    public static void onLayers(AddGuiOverlayLayersEvent e) {
        e.getLayeredDraw().addWithCondition(ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "fighter_hud"),
                (gg, dt) -> renderHud(gg), () -> !Minecraft.getInstance().options.hideGui);
    }

    private static void renderHud(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        FighterEntity f = piloted(mc);
        if (f == null) {
            return;
        }
        int w = g.guiWidth();
        int h = g.guiHeight();
        int pct = Math.round(f.throttle() * 100.0F);
        int hp = Math.round(f.health() / FighterEntity.MAX_HEALTH * 100.0F);
        g.drawString(mc.font, Component.translatable("hud.keinloree.fighter", pct, hp), 8, h - 40, 0x7CFFB0, true);
        g.drawString(mc.font, Component.translatable("hud.keinloree.fighter_keys"), 8, h - 28, 0xA0C8A8, true);
        // mira
        int cx = w / 2;
        int cy = h / 2;
        g.fill(cx - 9, cy - 1, cx - 4, cy + 1, 0xC04CFF7A);
        g.fill(cx + 4, cy - 1, cx + 9, cy + 1, 0xC04CFF7A);
        g.fill(cx - 1, cy - 9, cx + 1, cy - 4, 0xC04CFF7A);
        g.fill(cx - 1, cy + 4, cx + 1, cy + 9, 0xC04CFF7A);
    }
}
