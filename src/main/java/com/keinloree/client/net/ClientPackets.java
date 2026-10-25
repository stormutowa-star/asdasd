package com.keinloree.client.net;

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
        // WP1b
    }

    /** WP1b: abrir ChoiceScreen (ignorar si ya esta abierta la misma eleccion). */
    public static void onChoice(ChoiceS2C msg) {
        // WP1b
    }

    /** WP1b: empezar una cinematica (CinematicCamera). */
    public static void onCinematic(CinematicS2C msg) {
        // WP1b
    }

    /** WP1b: terminar la cinematica y restaurar la camara. */
    public static void onCinematicEnd(CinematicEndS2C msg) {
        // WP1b
    }

    /** WP1b: efecto de pantalla (ScreenFxOverlay). */
    public static void onScreenFx(ScreenFxS2C msg) {
        // WP1b
    }

    /** WP1b: tarjeta de capitulo (TitleCard). */
    public static void onTitleCard(TitleCardS2C msg) {
        // WP1b
    }

    /** WP1b: objetivo actual (ObjectiveHud). */
    public static void onHud(HudS2C msg) {
        // WP1b
    }

    /** WP1b: susurro de omnisciencia. */
    public static void onWhisper(WhisperS2C msg) {
        // WP1b
    }

    /** WP1b: tinte y niebla de la atmosfera. */
    public static void onAtmosphere(AtmosphereS2C msg) {
        // WP1b
    }

    /** WP2: efecto del motor de VFX (VfxRenderer). */
    public static void onVfx(VfxS2C msg) {
        // WP2
    }
}
