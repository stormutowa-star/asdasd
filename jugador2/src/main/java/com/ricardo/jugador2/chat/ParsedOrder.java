package com.ricardo.jugador2.chat;

/** Orden que un jugador da al NPC por el chat. */
public sealed interface ParsedOrder permits Come, Follow, GoTo, Mine, Stop, Help {
}
