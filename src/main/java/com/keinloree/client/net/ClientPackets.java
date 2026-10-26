package com.keinloree.client.net;

import com.keinloree.client.cam.CinematicCamera;
import com.keinloree.client.ui.AtmosphereClient;
import com.keinloree.client.ui.ChoiceScreen;
import com.keinloree.client.ui.DialogueOverlay;
import com.keinloree.client.ui.ObjectiveHud;
import com.keinloree.client.ui.ScreenFxOverlay;
import com.keinloree.client.ui.TitleCard;
import com.keinloree.client.ui.WhisperHud;
import com.keinloree.net.AtmosphereS2C;
import com.keinloree.net.ChoiceS2C;
import com.keinloree.net.CinematicEndS2C;
import com.keinloree.net.CinematicS2C;
import com.keinloree.net.DialogueS2C;
import com.keinloree.net.HudS2C;
import com.keinloree.net.ScreenFxS2C;
import com.keinloree.net.TitleCardS2C;
import com.keinloree.net.VfxS2C;
import com.keinloree.net.WhisperS2C;

/**
 * Puntos de entrada de los paquetes servidor a cliente. Solo los llaman los records de
 * {@code com.keinloree.net} dentro de {@code FMLEnvironment.dist == Dist.CLIENT}.
 * Los metodos estan vacios a proposito: WP1b (interfaz y camara) y WP2 (VFX) los implementan.
 */
public final class ClientPackets {
    private ClientPackets() {}

    /** WP1b: linea de dialogo (DialogueOverlay). */
    public static void onDialogue(DialogueS2C msg) {
        DialogueOverlay.onLine(msg);
    }

    /** WP1b: abrir ChoiceScreen (ignorar si ya esta abierta la misma eleccion). */
    public static void onChoice(ChoiceS2C msg) {
        ChoiceScreen.onChoice(msg);
    }

    /** WP1b: empezar una cinematica (CinematicCamera). */
    public static void onCinematic(CinematicS2C msg) {
        CinematicCamera.start(msg.shots(), (msg.flags() & CinematicS2C.SALTABLE) != 0);
    }

    /** WP1b: terminar la cinematica y restaurar la camara. */
    public static void onCinematicEnd(CinematicEndS2C msg) {
        CinematicCamera.end();
    }

    /** WP1b: efecto de pantalla (ScreenFxOverlay). */
    public static void onScreenFx(ScreenFxS2C msg) {
        ScreenFxOverlay.onFx(msg);
    }

    /** WP1b: tarjeta de capitulo (TitleCard). */
    public static void onTitleCard(TitleCardS2C msg) {
        TitleCard.show(msg);
    }

    /** WP1b: objetivo actual (ObjectiveHud). */
    public static void onHud(HudS2C msg) {
        ObjectiveHud.set(msg);
    }

    /** WP1b: susurro de omnisciencia. */
    public static void onWhisper(WhisperS2C msg) {
        WhisperHud.show(msg);
    }

    /** WP1b: tinte y niebla de la atmosfera. */
    public static void onAtmosphere(AtmosphereS2C msg) {
        AtmosphereClient.set(msg);
    }

    /** WP2: efecto del motor de VFX (VfxRenderer). */
    public static void onVfx(VfxS2C msg) {
        com.keinloree.client.vfx.VfxRenderer.add(msg);
    }

    public static void onAbilityState(com.keinloree.net.AbilityStateS2C m) {
        com.keinloree.client.kit.KitClient.onState(m);
    }
}
