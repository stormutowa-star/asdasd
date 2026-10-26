package com.keinloree.client.ship;

import com.keinloree.KeinLooreMod;
import com.keinloree.net.Net;
import com.keinloree.net.ShipInputC2S;
import com.keinloree.ship.DestroyerEntity;
import com.keinloree.ship.ShipTransform;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Control del destructor desde el puesto del timon: lee las teclas, manda ShipInputC2S cada tick, anula el ataque y
 * el uso normales mientras se pilota y dibuja el HUD del piloto.
 */
@Mod.EventBusSubscriber(modid = KeinLooreMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientShipInput {
    /** Lanzar cazas (R: no choca con ninguna tecla de vanilla). */
    public static final KeyMapping SPECIAL = new KeyMapping("key.keinloree.ship_special", InputConstants.KEY_R,
            "key.categories.keinloree");
    /** Impulso (B). */
    public static final KeyMapping BOOST = new KeyMapping("key.keinloree.ship_boost", InputConstants.KEY_B,
            "key.categories.keinloree");

    /** Entrada simulada para las pruebas automaticas: {adelante, giro, subir, botones}; null = teclado real. */
    private static float[] simulated;
    private static float lastSpeed;

    private ClientShipInput() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ClientShipInput::onKeys);
        modBus.addListener(ClientShipInput::onLayers);
    }

    private static void onKeys(RegisterKeyMappingsEvent e) {
        e.register(SPECIAL);
        e.register(BOOST);
    }

    private static void onLayers(AddGuiOverlayLayersEvent e) {
        e.getLayeredDraw().addWithCondition(ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "ship_hud"),
                (gg, dt) -> renderHud(gg), () -> !Minecraft.getInstance().options.hideGui);
    }

    /** Pruebas: fuerza la entrada del piloto (null la devuelve al teclado). */
    public static void simulate(float forward, float turn, float lift, int buttons) {
        simulated = new float[]{forward, turn, lift, buttons};
    }

    public static void stopSimulation() {
        simulated = null;
    }

    // ------------------------------------------------------------------ entrada

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        DestroyerEntity ship = ShipClient.localPilotedShip();
        if (ship == null || mc.player == null) {
            lastSpeed = 0.0F;
            return;
        }
        double dx = ship.getX() - ship.xo;
        double dz = ship.getZ() - ship.zo;
        lastSpeed = (float) Math.sqrt(dx * dx + dz * dz);
        float forward = 0.0F;
        float turn = 0.0F;
        float lift = 0.0F;
        int buttons = 0;
        if (simulated != null) {
            forward = simulated[0];
            turn = simulated[1];
            lift = simulated[2];
            buttons = (int) simulated[3];
        } else if (mc.screen == null && mc.isWindowActive()) {
            var o = mc.options;
            forward = (o.keyUp.isDown() ? 1.0F : 0.0F) - (o.keyDown.isDown() ? 1.0F : 0.0F);
            turn = (o.keyRight.isDown() ? 1.0F : 0.0F) - (o.keyLeft.isDown() ? 1.0F : 0.0F);
            lift = (o.keyJump.isDown() ? 1.0F : 0.0F) - (o.keySprint.isDown() ? 1.0F : 0.0F);
            if (o.keyAttack.isDown()) {
                buttons |= DestroyerEntity.BTN_FIRE;
            }
            if (SPECIAL.isDown()) {
                buttons |= DestroyerEntity.BTN_FIGHTERS;
            }
            if (BOOST.isDown()) {
                buttons |= DestroyerEntity.BTN_BOOST;
            }
            if (o.keyShift.isDown()) {
                buttons |= DestroyerEntity.BTN_RELEASE;
            }
        }
        Net.toServer(new ShipInputC2S(forward, turn, lift, buttons));
    }

    /** El piloto no ataca ni usa objetos: el clic izquierdo es el disparo de la nave. */
    @SubscribeEvent
    public static void onInteract(InputEvent.InteractionKeyMappingTriggered e) {
        if (ShipClient.localPilotedShip() != null) {
            e.setCanceled(true);
            e.setSwingHand(false);
        }
    }

    /** El piloto no camina, salta ni se agacha por su cuenta. */
    @SubscribeEvent
    public static void onMovement(MovementInputUpdateEvent e) {
        if (ShipClient.localPilotedShip() != null) {
            var in = e.getInput();
            in.forwardImpulse = 0.0F;
            in.leftImpulse = 0.0F;
            in.up = false;
            in.down = false;
            in.left = false;
            in.right = false;
            in.jumping = false;
            in.shiftKeyDown = false;
        }
    }

    // ------------------------------------------------------------------ HUD

    private static void renderHud(GuiGraphics gg) {
        Minecraft mc = Minecraft.getInstance();
        DestroyerEntity ship = ShipClient.localPilotedShip();
        if (ship == null || mc.screen != null) {
            return;
        }
        int w = gg.guiWidth();
        int h = gg.guiHeight();
        int cx = w / 2;
        int cy = h / 2;
        int green = 0xFF7DFFA8;
        // punto de mira
        gg.fill(cx - 9, cy, cx - 3, cy + 1, green);
        gg.fill(cx + 3, cy, cx + 9, cy + 1, green);
        gg.fill(cx, cy - 9, cx + 1, cy - 3, green);
        gg.fill(cx, cy + 3, cx + 1, cy + 9, green);
        // datos
        float yaw = ship.getYRot();
        int bearing = Math.floorMod(Math.round(yaw + 180.0F), 360);
        int speed = Math.round(lastSpeed * 20.0F);
        int alt = Mth.floor(ship.getY()) + (ShipTransform.solidBounds()[1] - ShipTransform.HY);
        int y = h - 62;
        int x = 10;
        gg.fill(x - 4, y - 4, x + 128, y + 46, 0x66000000);
        gg.drawString(mc.font, I18n.get("keinloree.hud.ship.speed", speed), x, y, green, false);
        gg.drawString(mc.font, I18n.get("keinloree.hud.ship.altitude", alt), x, y + 11, green, false);
        gg.drawString(mc.font, I18n.get("keinloree.hud.ship.heading", bearing, compass(bearing)), x, y + 22, green, false);
        gg.drawString(mc.font, I18n.get("keinloree.hud.ship.release", mc.options.keyShift.getTranslatedKeyMessage().getString()),
                x, y + 33, 0xFFFFD27D, false);
        // ayuda de teclas (arriba a la derecha)
        String[] help = {
                I18n.get("keinloree.hud.ship.help_move", mc.options.keyUp.getTranslatedKeyMessage().getString(),
                        mc.options.keyDown.getTranslatedKeyMessage().getString()),
                I18n.get("keinloree.hud.ship.help_turn", mc.options.keyLeft.getTranslatedKeyMessage().getString(),
                        mc.options.keyRight.getTranslatedKeyMessage().getString()),
                I18n.get("keinloree.hud.ship.help_lift", mc.options.keyJump.getTranslatedKeyMessage().getString(),
                        mc.options.keySprint.getTranslatedKeyMessage().getString()),
                I18n.get("keinloree.hud.ship.help_fire"),
                I18n.get("keinloree.hud.ship.help_fighters", SPECIAL.getTranslatedKeyMessage().getString()),
                I18n.get("keinloree.hud.ship.help_boost", BOOST.getTranslatedKeyMessage().getString())};
        int widest = 0;
        for (String s : help) {
            widest = Math.max(widest, mc.font.width(s));
        }
        gg.fill(w - widest - 14, 6, w - 6, 12 + help.length * 11, 0x66000000);
        for (int i = 0; i < help.length; i++) {
            gg.drawString(mc.font, help[i], w - widest - 10, 10 + i * 11, 0xFFB8E8C8, false);
        }
    }

    private static String compass(int deg) {
        String[] n = {"N", "NE", "E", "SE", "S", "SO", "O", "NO"};
        return n[Math.floorMod(Math.round(deg / 45.0F), 8)];
    }
}
