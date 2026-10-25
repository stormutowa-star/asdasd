package com.keinloree.client.ui;

import com.keinloree.net.ChoiceC2S;
import com.keinloree.net.ChoiceS2C;
import com.keinloree.net.Net;
import com.keinloree.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Pantalla de eleccion sin pausa: pregunta arriba y 2 a 4 botones grandes (tambien teclas 1-4).
 * Si se cierra con Esc y la eleccion sigue pendiente, se reabre a los 3 segundos.
 */
public final class ChoiceScreen extends Screen {
    private static final int REOPEN_TICKS = 60;
    /** Tras responder se ignoran reenvios retrasados del servidor durante este tiempo. */
    private static final int IGNORE_TICKS = 40;

    @Nullable
    private static ChoiceS2C pending;
    private static int reopenIn = -1;
    @Nullable
    private static String answeredId;
    private static int answeredAgo = IGNORE_TICKS;

    private final ChoiceS2C choice;
    private final List<String> keys = new ArrayList<>();
    private final List<String> texts = new ArrayList<>();

    private ChoiceScreen(ChoiceS2C choice) {
        super(Component.literal(KlText.resolve(choice.prompt())));
        this.choice = choice;
        int n = Math.min(4, Math.min(choice.keys().size(), choice.texts().size()));
        for (int i = 0; i < n; i++) {
            keys.add(choice.keys().get(i));
            texts.add(KlText.resolve(choice.texts().get(i)));
        }
    }

    // ---- estado estatico (lo usa ClientPackets y el tick) ----

    /** Paquete del servidor: lista vacia cierra; la misma eleccion ya abierta se ignora. */
    public static void onChoice(ChoiceS2C msg) {
        Minecraft mc = Minecraft.getInstance();
        if (msg.keys().isEmpty()) {
            pending = null;
            reopenIn = -1;
            if (mc.screen instanceof ChoiceScreen) {
                mc.setScreen(null);
            }
            return;
        }
        if (mc.screen instanceof ChoiceScreen open && open.choice.id().equals(msg.id())) {
            return;
        }
        if (msg.id().equals(answeredId) && answeredAgo < IGNORE_TICKS) {
            return;
        }
        pending = msg;
        open(mc);
    }

    private static void open(Minecraft mc) {
        if (pending == null || mc.player == null) {
            return;
        }
        reopenIn = -1;
        mc.setScreen(new ChoiceScreen(pending));
    }

    /** Cada tick de cliente: reabre la eleccion si se cerro y sigue pendiente. */
    public static void tick(Minecraft mc) {
        if (answeredAgo < IGNORE_TICKS) {
            answeredAgo++;
        }
        if (pending == null) {
            return;
        }
        if (mc.screen instanceof ChoiceScreen) {
            return;
        }
        if (reopenIn < 0) {
            reopenIn = REOPEN_TICKS;
        }
        if (mc.screen == null && --reopenIn <= 0) {
            open(mc);
        }
    }

    /** Olvida la eleccion (desconexion, cambio de mundo). */
    public static void reset() {
        pending = null;
        reopenIn = -1;
        answeredId = null;
        answeredAgo = IGNORE_TICKS;
    }

    /** Para el banco de pruebas: elige la opcion i de la pantalla abierta. */
    public static boolean choose(Minecraft mc, int i) {
        if (mc.screen instanceof ChoiceScreen s && i >= 0 && i < s.keys.size()) {
            s.pick(i);
            return true;
        }
        return false;
    }

    // ---- pantalla ----

    @Override
    protected void init() {
        int n = keys.size();
        int bw = 200;
        for (String t : texts) {
            bw = Math.max(bw, font.width(t) + 40);
        }
        bw = Math.min(bw, width - 40);
        int bh = 24;
        int gap = 6;
        int total = n * bh + (n - 1) * gap;
        int top = Math.max(height / 2 - total / 2 + 14, 70);
        for (int i = 0; i < n; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal((i + 1) + ".  " + texts.get(i)), b -> pick(idx))
                    .bounds((width - bw) / 2, top + i * (bh + gap), bw, bh).build());
        }
    }

    private void pick(int i) {
        if (i < 0 || i >= keys.size()) {
            return;
        }
        Net.toServer(new ChoiceC2S(choice.id(), keys.get(i)));
        answeredId = choice.id();
        answeredAgo = 0;
        pending = null;
        reopenIn = -1;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.CHOICE.get(), 1.0F, 0.6F));
        onClose();
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        // Teclas 1-4 (fila superior) y teclado numerico
        int idx = key >= 49 && key <= 52 ? key - 49 : (key >= 321 && key <= 324 ? key - 321 : -1);
        if (idx >= 0 && idx < keys.size()) {
            pick(idx);
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void render(GuiGraphics gg, int mx, int my, float partial) {
        super.render(gg, mx, my, partial);
        // Pregunta arriba, escalada x1.5
        String prompt = getTitle().getString();
        float scale = 1.5F;
        int wrapW = (int) (Math.min(width * 0.8F, 420.0F) / scale);
        List<FormattedCharSequence> lines = font.split(Component.literal(prompt), wrapW);
        int y = Math.max(16, height / 2 - (keys.size() * 30) / 2 - lines.size() * 14 - 14);
        gg.pose().pushPose();
        gg.pose().scale(scale, scale, 1.0F);
        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence l = lines.get(i);
            int lw = font.width(l);
            gg.drawString(font, l, (int) ((width / 2.0F) / scale - lw / 2.0F), (int) (y / scale) + i * 11, 0xFFFFFFFF, true);
        }
        gg.pose().popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
