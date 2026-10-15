package com.ricardo.jugador2.chat;

/** "Nombre, mina bloque": minar el bloque más cercano de ese tipo. El texto se valida al ejecutar. */
public record Mine(String block) implements ParsedOrder {}
