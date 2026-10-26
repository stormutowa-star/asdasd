package com.keinloree.entity;

/**
 * Entidad con animaciones de accion sincronizadas (modelos de Blockbench). El servidor fija la accion con un id y un
 * contador; el cliente arranca la animacion cuando cambia el contador.
 */
public interface Animated {
    int A_NONE = 0;
    int A_ATTACK_1 = 1;
    int A_ATTACK_2 = 2;
    int A_ATTACK_3 = 3;
    int A_SPECIAL_1 = 4;
    int A_SPECIAL_2 = 5;
    int A_CAST = 6;
    int A_POSE = 7;
    int A_KNEEL = 8;
    int A_HURT = 9;
    int A_CHARGE = 10;
    int A_LEAP = 11;
    int A_ROAR = 12;
    int A_BLOCK = 13;
    int A_HORIZON = 14;
    int A_CHAIN = 15;

    int animId();

    int animSeq();

    /** Tick de cliente en que empezo la animacion actual (lo mantiene el renderer). */
    int[] clientAnimState();
}
