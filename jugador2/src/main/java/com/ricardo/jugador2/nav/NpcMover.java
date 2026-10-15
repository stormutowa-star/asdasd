package com.ricardo.jugador2.nav;

import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Sigue una ruta de celdas. Cada tick gira hacia el siguiente nodo, fija la velocidad horizontal, aplica
 * gravedad, salta cuando el nodo está un bloque más arriba y mueve al NPC con colisiones. El servidor no
 * aplica por sí solo la entrada de movimiento a un jugador sin cliente, por eso el movimiento se hace aquí.
 */
public final class NpcMover {
    private static final double NODE_REACHED = 0.5;
    private static final double WALK_SPEED = 0.1;
    private static final double JUMP_VELOCITY = 0.42;
    private static final double GRAVITY = 0.08;
    private static final double VERTICAL_DRAG = 0.98;
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

    public boolean isDone() {
        return next >= path.size();
    }

    public boolean isStuck() {
        return ticksWithoutProgress >= STUCK_TICKS;
    }

    public void tick(NpcPlayer npc) {
        double vx = 0.0;
        double vz = 0.0;
        boolean jump = false;
        if (!isDone() && !isStuck()) {
            advanceTarget(npc);
        }
        if (!isDone() && !isStuck()) {
            Cell target = path.get(next);
            double dx = target.x() + 0.5 - npc.getX();
            double dz = target.z() + 0.5 - npc.getZ();
            double length = Math.sqrt(dx * dx + dz * dz);
            if (length > 1e-6) {
                vx = dx / length * WALK_SPEED;
                vz = dz / length * WALK_SPEED;
                npc.setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
            }
            jump = target.y() > npc.getBlockY() && npc.onGround();
        }

        Vec3 v = npc.getDeltaMovement();
        double vy = npc.onGround() ? Math.max(v.y, 0.0) : (v.y - GRAVITY) * VERTICAL_DRAG;
        if (jump) {
            vy = JUMP_VELOCITY;
        }
        npc.setDeltaMovement(vx, vy, vz);
        npc.move(MoverType.SELF, npc.getDeltaMovement());
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
