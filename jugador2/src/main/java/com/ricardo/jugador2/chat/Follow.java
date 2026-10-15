package com.ricardo.jugador2.chat;

/** "Nombre, sigue a jugador": seguir a un jugador conectado. */
public record Follow(String player) implements ParsedOrder {}
