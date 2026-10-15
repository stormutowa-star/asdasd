package com.ricardo.jugador2.ai;

/** Tarea del NPC: en cada tick decide su entrada y devuelve si sigue, terminó o falló. */
@FunctionalInterface
public interface Task {
    TaskResult tick(NpcContext ctx);
}
