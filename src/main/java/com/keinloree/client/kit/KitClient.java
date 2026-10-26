package com.keinloree.client.kit;

import com.keinloree.KeinLooreMod;
import com.keinloree.ability.Techniques;
import com.keinloree.net.AbilityC2S;
import com.keinloree.net.AbilityStateS2C;
import com.keinloree.net.Net;
import com.keinloree.registry.ModItems;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Kit de la Sexta Hora en el cliente: teclas de tecnica, cambio, dash y doble salto, y el HUD de RareIri. */
@Mod.EventBusSubscriber(modid = KeinLooreMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class KitClient {
    public static final KeyMapping USE = new KeyMapping("key.keinloree.technique", InputConstants.KEY_Z, "key.categories.keinloree");
    public static final KeyMapping CYCLE = new KeyMapping("key.keinloree.technique_cycle", InputConstants.KEY_G, "key.categories.keinloree");
    public static final KeyMapping DASH = new KeyMapping("key.keinloree.dash", InputConstants.KEY_V, "key.categories.keinloree");

    private static int unlocked;
    private static final int[] cooldowns = new int[Techniques.COUNT + 1];
    private static int charge;
    private static boolean corrupted;
    private static int selected;
    private static boolean jumpWasDown;
    private static boolean leftGround;

    private KitClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(KitClient::onKeys);
        modBus.addListener(KitClient::onLayers);
    }

    private static void onKeys(RegisterKeyMappingsEvent e) {
        e.register(USE);
        e.register(CYCLE);
        e.register(DASH);
    }

    public static void onState(AbilityStateS2C m) {
        unlocked = m.unlocked();
        for (int i = 0; i < cooldowns.length; i++) {
            cooldowns[i] = i < m.cooldowns().length ? m.cooldowns()[i] : 0;
        }
        charge = m.charge();
        corrupted = m.corrupted();
        if ((unlocked & (1 << selected)) == 0) {
            cycle();
        }
    }

    private static void cycle() {
        for (int i = 1; i <= Techniques.COUNT; i++) {
            int t = (selected + i) % Techniques.COUNT;
            if ((unlocked & (1 << t)) != 0) {
                selected = t;
                return;
            }
        }
    }

    private static boolean holds(LocalPlayer p) {
        return p.getMainHandItem().is(ModItems.RAREIRI.get());
    }

    private static boolean fullSet(LocalPlayer p) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SEXTA_HORA_HELMET.get())
                && p.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.SEXTA_HORA_CHESTPLATE.get())
                && p.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SEXTA_HORA_LEGGINGS.get())
                && p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.SEXTA_HORA_BOOTS.get());
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        for (int i = 0; i < cooldowns.length; i++) {
            if (cooldowns[i] > 0) {
                cooldowns[i]--;
            }
        }
        if (p == null || mc.screen != null || p.isPassenger()) {
            while (USE.consumeClick()) { }
            while (CYCLE.consumeClick()) { }
            while (DASH.consumeClick()) { }
            jumpWasDown = mc.options.keyJump.isDown();
            return;
        }
        while (CYCLE.consumeClick()) {
            cycle();
        }
        while (USE.consumeClick()) {
            if (holds(p) && (unlocked & (1 << selected)) != 0) {
                Net.toServer(new AbilityC2S(0, selected));
            }
        }
        while (DASH.consumeClick()) {
            if (fullSet(p) && cooldowns[Techniques.COUNT] <= 0) {
                Net.toServer(new AbilityC2S(1, 0));
            }
        }
        boolean jump = mc.options.keyJump.isDown();
        if (p.onGround()) {
            leftGround = false;
        } else if (!jump) {
            leftGround = true;
        }
        // doble salto: segunda pulsacion de salto en el aire
        if (jump && !jumpWasDown && !p.onGround() && leftGround && !p.getAbilities().flying && !p.isInWater() && fullSet(p)) {
            Net.toServer(new AbilityC2S(2, 0));
            leftGround = false;
        }
        jumpWasDown = jump;
    }

    private static void onLayers(AddGuiOverlayLayersEvent e) {
        e.getLayeredDraw().addWithCondition(ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "rareiri_hud"),
                (gg, dt) -> render(gg), () -> !Minecraft.getInstance().options.hideGui);
    }

    private static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || !holds(p) || p.isPassenger()) {
            return;
        }
        int w = g.guiWidth();
        int h = g.guiHeight();
        int x = w / 2 + 100;
        int y = h - 34;
        int accent = corrupted ? 0xFF2EE86A : 0xFF9FFFE8;
        if (unlocked == 0) {
            g.drawString(mc.font, Component.translatable("hud.keinloree.rareiri_dormant"), x, y + 10, 0xFF8890A0, true);
        } else {
            boolean ready = cooldowns[selected] <= 0 && (selected != Techniques.HORA_CERO || charge >= 100);
            Component name = Component.translatable("technique.keinloree." + Techniques.IDS[selected]);
            g.drawString(mc.font, name, x, y, ready ? accent : 0xFF707880, true);
            g.drawString(mc.font, Component.translatable("hud.keinloree.rareiri_keys",
                    USE.getTranslatedKeyMessage(), CYCLE.getTranslatedKeyMessage()), x, y + 10, 0xFFA0A8B0, true);
            if (cooldowns[selected] > 0) {
                int bw = Math.min(60, cooldowns[selected] / 4);
                g.fill(x, y - 4, x + bw, y - 2, 0xFF505868);
            }
        }
        // carga de la Hora Cero
        int bx = x;
        int by = y + 22;
        g.fill(bx, by, bx + 62, by + 4, 0xA0000000);
        g.fill(bx + 1, by + 1, bx + 1 + Math.round(60 * charge / 100.0F), by + 3, charge >= 100 ? 0xFFFFFFFF : accent);
    }
}
