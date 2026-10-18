package com.keinloree.entity;

/**
 * Lo que necesita el modelo de la facion Loree para animarse en el cliente.
 * Todo viene de datos sincronizados, asi que cliente y servidor ven la misma pose.
 */
public interface LooreVisual {
    int POSE_IDLE = 0;
    int POSE_CAST = 1;
    int POSE_SPREAD = 2;
    int POSE_FLY = 3;
    int POSE_CHARGE = 4;
    int POSE_FIRE = 5;
    int POSE_SIT = 6;
    int POSE_SUMMON = 7;

    /** Pose actual (una de las POSE_*). */
    int getLorePose();

    /** 0 = forma base (sin alas), 1 = forma final (Techno Winged Code). */
    int getLoreForm();

    /** 0 = alas plegadas, 1 = alas desplegadas. */
    float getWingOpen();

    /** Ticks restantes de emergencia desde el suelo (0 = completa). */
    int getSpawnTicks();

    /** Dormido en el trono de la nave: sin IA ni dano. */
    boolean isLoreDormant();

    /** 0 = Kein Loree, 1 = comandante, 2 = ejecutor (hombreras). */
    int getLoreSkin();
}
