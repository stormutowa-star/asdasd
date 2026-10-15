package com.ricardo.jugador2.chat;

/** "Nombre, ve a x y z": ir a un punto del mundo. */
public record GoTo(int x, int y, int z) implements ParsedOrder {}
