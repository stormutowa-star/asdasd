package com.ricardo.jugador2.nav;

import com.ricardo.jugador2.npc.NpcPlayer;

import java.util.List;

/**
 * Sigue una ruta de celdas. Cada tick gira hacia el siguiente nodo, pone la entrada de avance y salta cuando el nodo
 * está un bloque más arriba. La física (gravedad, fricción, colisiones) la calcula el juego, como con un jugador real,
 * porque el NPC tickea su cuerpo completo (ver NpcPlayer.tick).
 */
public final class NpcMover {
    private static final double NODE_REACHED = 0.5;
    private static final double PROGRESS_EPSILON = 0.01;
    private static final int STUCK_TICKS = 40;

    private final List<Cell> path;
    private int next = 1;
    private double bestDistance = Double.MAX_VALUE;
    private int ticksWithoutProgress;

    /** La celda 0 de la ruta es la de partida, así que el movimiento empieza en la 1. */
    public NpcMover(List<Cell> path) {
        this.path = List.copyOf(path);
    }

    /** Sin ruta: el NPC se queda quieto y el juego le aplica la gravedad como a cualquier jugador. */
    public static NpcMover idle() {
        return new NpcMover(List.of());
    }

    public boolean isDone() {
        return next >= path.size();
    }

    public boolean isStuck() {
        return ticksWithoutProgress >= STUCK_TICKS;
    }

    public void tick(NpcPlayer npc) {
        float forward = 0.0F;
        boolean jump = false;
        if (!isDone() && !isStuck()) {
            advanceTarget(npc);
        }
        if (!isDone() && !isStuck()) {
            Cell target = path.get(next);
            double dx = target.x() + 0.5 - npc.getX();
            double dz = target.z() + 0.5 - npc.getZ();
            npc.setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
            forward = 1.0F;
            jump = target.y() > npc.getBlockY() && npc.onGround();
        }
        npc.zza = forward;
        npc.setJumping(jump);
    }

    /** Avanza al siguiente nodo cuando llega a él, y lleva la cuenta de si se acerca. */
    private void advanceTarget(NpcPlayer npc) {
        if (distance(npc, path.get(next)) < NODE_REACHED) {
            next++;
            bestDistance = Double.MAX_VALUE;
            ticksWithoutProgress = 0;
            if (isDone()) {
                return;
            }
        }
        double current = distance(npc, path.get(next));
        if (current < bestDistance - PROGRESS_EPSILON) {
            bestDistance = current;
            ticksWithoutProgress = 0;
        } else {
            ticksWithoutProgress++;
        }
    }

    private static double distance(NpcPlayer npc, Cell cell) {
        double dx = cell.x() + 0.5 - npc.getX();
        double dz = cell.z() + 0.5 - npc.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
