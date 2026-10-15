package com.ricardo.jugador2.nav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A* sobre celdas donde el NPC puede estar de pie. Las reglas son las de un jugador: camina,
 * sube 1 bloque, baja hasta 3 y nunca entra en lava ni agua.
 */
public final class NpcNavigator {
    private static final int[][] LATERAL = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int MAX_DROP = 3;

    private NpcNavigator() {
    }

    /**
     * Ruta de start a goal, ambos incluidos. Devuelve null si no hay ruta o si se expanden
     * más de maxNodes nodos.
     */
    public static List<Cell> findPath(BlockAccess world, Cell start, Cell goal, int maxNodes) {
        if (start.equals(goal)) {
            return List.of(start);
        }
        if (!canStand(world, goal.x(), goal.y(), goal.z())) {
            return null;
        }

        PriorityQueue<Node> open = new PriorityQueue<>((a, b) -> Integer.compare(a.f(), b.f()));
        Map<Cell, Integer> g = new HashMap<>();
        Map<Cell, Cell> cameFrom = new HashMap<>();
        Set<Cell> closed = new HashSet<>();
        g.put(start, 0);
        open.add(new Node(start, heuristic(start, goal)));

        int expanded = 0;
        while (!open.isEmpty()) {
            Cell current = open.poll().cell();
            if (current.equals(goal)) {
                return reconstruct(cameFrom, goal);
            }
            if (!closed.add(current)) {
                continue;
            }
            expanded++;
            if (expanded > maxNodes) {
                return null;
            }
            int gCurrent = g.get(current);
            for (Cell next : neighbors(world, current)) {
                if (closed.contains(next)) {
                    continue;
                }
                int tentative = gCurrent + 1;
                if (tentative < g.getOrDefault(next, Integer.MAX_VALUE)) {
                    g.put(next, tentative);
                    cameFrom.put(next, current);
                    open.add(new Node(next, tentative + heuristic(next, goal)));
                }
            }
        }
        return null;
    }

    private static List<Cell> neighbors(BlockAccess world, Cell c) {
        List<Cell> out = new ArrayList<>();
        // Subir un bloque exige espacio libre para la cabeza sobre la celda de partida.
        boolean headroom = isOpen(world, c.x(), c.y() + 2, c.z());
        for (int[] d : LATERAL) {
            int nx = c.x() + d[0];
            int nz = c.z() + d[1];
            if (canStand(world, nx, c.y(), nz)) {
                out.add(new Cell(nx, c.y(), nz));
            }
            if (headroom && canStand(world, nx, c.y() + 1, nz)) {
                out.add(new Cell(nx, c.y() + 1, nz));
            }
            addDrop(world, c, nx, nz, out);
        }
        return out;
    }

    /** Bajada de 1 a 3 bloques: la columna hasta el suelo de llegada debe estar libre. */
    private static void addDrop(BlockAccess world, Cell c, int nx, int nz, List<Cell> out) {
        for (int k = 1; k <= MAX_DROP; k++) {
            if (!isOpen(world, nx, c.y() - k + 1, nz)) {
                return;
            }
            if (canStand(world, nx, c.y() - k, nz)) {
                out.add(new Cell(nx, c.y() - k, nz));
                return;
            }
        }
    }

    /** Celda que el NPC puede ocupar: sin colisión y sin peligro. */
    private static boolean isOpen(BlockAccess world, int x, int y, int z) {
        return world.isPassable(x, y, z) && !world.isHazard(x, y, z);
    }

    /** De pie en la celda: cuerpo libre, sólido debajo y ningún peligro. */
    private static boolean canStand(BlockAccess world, int x, int y, int z) {
        return isOpen(world, x, y, z) && isOpen(world, x, y + 1, z)
                && world.isSolid(x, y - 1, z) && !world.isHazard(x, y - 1, z);
    }

    private static int heuristic(Cell a, Cell b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y()) + Math.abs(a.z() - b.z());
    }

    private static List<Cell> reconstruct(Map<Cell, Cell> cameFrom, Cell goal) {
        List<Cell> path = new ArrayList<>();
        Cell c = goal;
        path.add(c);
        while (cameFrom.containsKey(c)) {
            c = cameFrom.get(c);
            path.add(c);
        }
        Collections.reverse(path);
        return path;
    }

    private record Node(Cell cell, int f) {}
}
