package com.ricardo.retorcido.terror;

/**
 * Todas las constantes ajustables del director de terror. Los tiempos van en TICKS
 * (20 ticks = 1 segundo, 1200 = 1 minuto, 24000 = un dia de Minecraft); las distancias en bloques.
 * Cambia un valor y recompila el mod.
 */
public final class TerrorConfig {
    private TerrorConfig() {}

    // ------------------------------------------------------------ progresion de la historia
    /** playTicks (tiempo jugado en el overworld) a partir del cual empieza la Fase 2 (10 min). */
    public static final int PHASE2_TICKS = 12000;
    /** ... la Fase 3 (25 min). */
    public static final int PHASE3_TICKS = 30000;
    /** ... la Fase 4 = momento del secuestro de Dani (40 min, y solo de noche). */
    public static final int PHASE4_TICKS = 48000;

    // ------------------------------------------------------------ frecuencia de sucesos
    /** Espera minima entre sucesos segun la fase (indice 0 = Fase 1 ... 4 = Fase 5). */
    public static final int[] EVENT_COOLDOWN_MIN = {2400, 1800, 1200, 900, 900};
    /** Espera maxima entre sucesos segun la fase (indice 0 = Fase 1 ... 4 = Fase 5). */
    public static final int[] EVENT_COOLDOWN_MAX = {4800, 3600, 2400, 1800, 1800};
    /** Si un suceso no encuentra sitio valido, se reintenta otro pasado este tiempo. */
    public static final int EVENT_RETRY_TICKS = 200;

    // ------------------------------------------------------------ PASOS
    public static final int STEPS_MIN_COUNT = 4;
    public static final int STEPS_MAX_COUNT = 6;
    /** Ticks entre un paso y el siguiente. */
    public static final int STEPS_INTERVAL = 10;
    /** Distancia detras del jugador del primer paso y del ultimo (se van acercando). */
    public static final double STEPS_DIST_FAR = 5.0D;
    public static final double STEPS_DIST_NEAR = 3.0D;
    public static final float STEPS_VOLUME = 0.6F;
    public static final float STEPS_PITCH = 0.6F;

    // ------------------------------------------------------------ SUSURRO
    public static final double WHISPER_MIN_DIST = 12.0D;
    public static final double WHISPER_MAX_DIST = 20.0D;
    public static final float WHISPER_VOLUME = 1.0F;
    public static final float WHISPER_PITCH_MIN = 0.5F;
    public static final float WHISPER_PITCH_MAX = 0.8F;

    // ------------------------------------------------------------ PUERTA / ANTORCHA
    public static final int DOOR_RADIUS = 10;
    public static final int TORCH_RADIUS = 12;

    // ------------------------------------------------------------ OSCURIDAD
    public static final int DARKNESS_TICKS = 100;
    public static final int HEARTBEAT_MIN_COUNT = 6;
    public static final int HEARTBEAT_MAX_COUNT = 8;
    public static final int HEARTBEAT_INTERVAL = 20;
    public static final float HEARTBEAT_VOLUME_START = 0.3F;
    public static final float HEARTBEAT_VOLUME_END = 1.0F;

    // ------------------------------------------------------------ DANI MIRA
    /** Duracion de la mirada fija de Dani (160 = 8 s). */
    public static final int DANI_STARE_TICKS = 160;
    /** Distancia a la que mira (un punto lejano en la oscuridad). */
    public static final double DANI_STARE_DISTANCE = 40.0D;
    /** Distancia maxima jugador-Dani para que Dani reaccione a sucesos. */
    public static final double DANI_MAX_DIST = 40.0D;
    /** Dani solo habla tras ver al observador si esta a menos de esto. */
    public static final double DANI_SPEAK_RANGE = 15.0D;
    /** Probabilidad (0-1) de que Dani comente algo tras ver al observador. */
    public static final double DANI_SPEAK_AFTER_WATCHER_CHANCE = 0.7D;

    // ------------------------------------------------------------ OBSERVADOR
    /** Distancia de aparicion en Fase 2 (min, max). */
    public static final double WATCHER_F2_MIN = 45.0D;
    public static final double WATCHER_F2_MAX = 60.0D;
    /** Distancia de aparicion en Fase 3+ (min, max); ademas aparece DETRAS del jugador. */
    public static final double WATCHER_F3_MIN = 30.0D;
    public static final double WATCHER_F3_MAX = 40.0D;
    /** Intentos de buscar un sitio valido. */
    public static final int SPAWN_ATTEMPTS = 12;
    /** Desaparece si el jugador se acerca a menos de esto. */
    public static final double WATCHER_VANISH_DIST = 22.0D;
    /** Desaparece si el jugador lo ha mirado mas de estos ticks (acumulados). */
    public static final int WATCHER_STARE_LIMIT = 70;
    /** Desaparece al superar esta edad (2000 = ~1.7 min). */
    public static final int WATCHER_MAX_AGE = 2000;
    /** Producto punto minimo (vista del jugador . direccion al mob) para contar como "me mira". */
    public static final double WATCHER_LOOK_DOT = 0.985D;
    /** Cada cuantos ticks se comprueba si lo miran (suma/resta este mismo valor). */
    public static final int WATCHER_CHECK_INTERVAL = 5;

    // ------------------------------------------------------------ SECUESTRO
    /** Ventana nocturna (dayTime % 24000) en la que puede ocurrir el secuestro. */
    public static final int NIGHT_START = 13000;
    public static final int NIGHT_END = 22000;
    /** Dani debe estar a menos de esto del jugador para iniciar el secuestro. */
    public static final double KIDNAP_MAX_DANI_DIST = 20.0D;
    /** Distancia del punto al que Dani "va a ver qué pasa". */
    public static final double KIDNAP_POINT_MIN = 28.0D;
    public static final double KIDNAP_POINT_MAX = 34.0D;
    /** Se considera que Dani llego al punto a esta distancia. */
    public static final double KIDNAP_ARRIVE_DIST = 3.0D;
    /** Maximo de espera a que llegue (500 = 25 s). */
    public static final int KIDNAP_WALK_TIMEOUT = 500;
    /** Mirada previa al ataque (60 = 3 s) y lo lejos que mira. */
    public static final int KIDNAP_STARE_TICKS = 60;
    public static final double KIDNAP_STARE_EXTRA = 15.0D;
    /** Tiempo entre la desaparicion de Dani y el primer cazador (160 = 8 s). */
    public static final int KIDNAP_AFTER_TICKS = 160;
    public static final int KIDNAP_DARKNESS_TICKS = 120;

    // ------------------------------------------------------------ CAZADOR
    /** Distancia detras del jugador a la que aparece. */
    public static final double HUNTER_MIN = 35.0D;
    public static final double HUNTER_MAX = 40.0D;
    /** Rango de seguimiento del cazador (el normal es 32). */
    public static final double HUNTER_FOLLOW_RANGE = 64.0D;
    /** Ventana (dayTime % 24000) para el cazador nocturno de la Fase 5. */
    public static final int HUNT_START = 13000;
    public static final int HUNT_END = 13600;
    /** Dias sin cacerias tras matar a un cazador. */
    public static final int HUNT_COOLDOWN_DAYS = 2;
    /** Tag de entidad que marca a los cazadores del director. */
    public static final String HUNTER_TAG = "retorcido_hunter";

    // ------------------------------------------------------------ REGRESO DE DANI
    /** Ventana del amanecer (dayTime % 24000) para el regreso. */
    public static final int RETURN_START = 0;
    public static final int RETURN_END = 2000;
    public static final double RETURN_MIN = 6.0D;
    public static final double RETURN_MAX = 8.0D;
    /** Ticks entre la reaparicion y su primera frase (60 = 3 s). */
    public static final int RETURN_SAY_DELAY = 60;
    /** Si Dani muere antes del secuestro, reaparece tras esto (1200 = 60 s). */
    public static final int DANI_RESPAWN_DELAY = 1200;
}
