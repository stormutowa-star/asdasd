package com.ricardo.jugador2.nav;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcNavigatorTest {

    /** Mundo de prueba: todo es aire salvo lo que se marca. */
    static final class GridWorld implements BlockAccess {
        private final Set<Cell> solids = new HashSet<>();
        private final Set<Cell> hazards = new HashSet<>();

        GridWorld solid(int x, int y, int z) {
            solids.add(new Cell(x, y, z));
            return this;
        }

        /** Suelo sólido en y=0 para x en [x0,x1] y z en [z0,z1]. */
        GridWorld floor(int x0, int x1, int z0, int z1) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    solid(x, 0, z);
                }
            }
            return this;
        }

        /** Lava: ocupable pero no pisable. Sustituye a un bloque sólido si lo había. */
        GridWorld lava(int x, int y, int z) {
            Cell cell = new Cell(x, y, z);
            solids.remove(cell);
            hazards.add(cell);
            return this;
        }

        @Override
        public boolean isPassable(int x, int y, int z) {
            return !solids.contains(new Cell(x, y, z));
        }

        @Override
        public boolean isSolid(int x, int y, int z) {
            return solids.contains(new Cell(x, y, z));
        }

        @Override
        public boolean isHazard(int x, int y, int z) {
            return hazards.contains(new Cell(x, y, z));
        }
    }

    @Test
    void flatGroundPathEndsAtGoal() {
        GridWorld world = new GridWorld().floor(0, 4, 0, 0);
        List<Cell> path = NpcNavigator.findPath(world, new Cell(0, 1, 0), new Cell(4, 1, 0), 1000);
        assertNotNull(path);
        assertEquals(5, path.size());
        assertEquals(new Cell(4, 1, 0), path.get(path.size() - 1));
        assertTrue(path.stream().allMatch(c -> c.y() == 1));
    }

    @Test
    void stepsUpOneBlock() {
        // Columna de 2 bloques en x=2 (suelo + un bloque): hay que subir encima.
        GridWorld world = new GridWorld().floor(0, 4, 0, 0).solid(2, 1, 0);
        List<Cell> path = NpcNavigator.findPath(world, new Cell(0, 1, 0), new Cell(4, 1, 0), 1000);
        assertNotNull(path);
        assertTrue(path.contains(new Cell(2, 2, 0)));
    }

    @Test
    void dropsUpToThreeBlocks() {
        // Plataforma en (0,3,0) para estar de pie en (0,4,0); suelo en (1,0,0) para el destino (1,1,0).
        GridWorld world = new GridWorld().solid(0, 3, 0).solid(1, 0, 0);
        List<Cell> path = NpcNavigator.findPath(world, new Cell(0, 4, 0), new Cell(1, 1, 0), 1000);
        assertNotNull(path);
        assertEquals(new Cell(1, 1, 0), path.get(path.size() - 1));
    }

    @Test
    void dropFourBlocksIsRejected() {
        // Mismo punto de salida, pero el destino (1,0,0) está 4 bloques más abajo.
        GridWorld world = new GridWorld().solid(0, 3, 0).solid(1, -1, 0);
        assertNull(NpcNavigator.findPath(world, new Cell(0, 4, 0), new Cell(1, 0, 0), 1000));
    }

    @Test
    void goesAroundWall() {
        // Suelo ancho en z∈[-1,1]; muro de 2 bloques en x=2 para z∈{0,1}: el camino rodea por z=-1.
        GridWorld world = new GridWorld().floor(0, 4, -1, 1)
                .solid(2, 1, 0).solid(2, 2, 0).solid(2, 1, 1).solid(2, 2, 1);
        List<Cell> path = NpcNavigator.findPath(world, new Cell(0, 1, 0), new Cell(4, 1, 0), 1000);
        assertNotNull(path);
        assertEquals(new Cell(4, 1, 0), path.get(path.size() - 1));
        assertFalse(path.contains(new Cell(2, 1, 0)));
        assertFalse(path.contains(new Cell(2, 1, 1)));
    }

    @Test
    void closedRoomReturnsNull() {
        // Celda (1,1,0) con suelo y muros de 2 bloques en los cuatro lados; la meta está fuera.
        GridWorld world = new GridWorld().solid(1, 0, 0)
                .solid(0, 1, 0).solid(0, 2, 0).solid(2, 1, 0).solid(2, 2, 0)
                .solid(1, 1, -1).solid(1, 2, -1).solid(1, 1, 1).solid(1, 2, 1)
                .solid(5, 0, 0);
        assertNull(NpcNavigator.findPath(world, new Cell(1, 1, 0), new Cell(5, 1, 0), 1000));
    }

    @Test
    void nodeLimitReturnsNull() {
        GridWorld world = new GridWorld().floor(0, 99, 0, 0);
        assertNull(NpcNavigator.findPath(world, new Cell(0, 1, 0), new Cell(99, 1, 0), 5));
    }

    @Test
    void avoidsLava() {
        // Lava en el suelo de (2,0,0): el único camino recto queda cortado.
        GridWorld corridor = new GridWorld().floor(0, 4, 0, 0).lava(2, 0, 0);
        assertNull(NpcNavigator.findPath(corridor, new Cell(0, 1, 0), new Cell(4, 1, 0), 1000));

        // Con un desvío por z=-1 la ruta existe y no pisa lava.
        GridWorld withDetour = new GridWorld().floor(0, 4, -1, 0).lava(2, 0, 0);
        List<Cell> path = NpcNavigator.findPath(withDetour, new Cell(0, 1, 0), new Cell(4, 1, 0), 1000);
        assertNotNull(path);
        assertTrue(path.stream().noneMatch(c -> withDetour.isHazard(c.x(), c.y() - 1, c.z())));
    }

    @Test
    void floatingGoalReturnsNull() {
        GridWorld world = new GridWorld().floor(0, 4, 0, 0);
        assertNull(NpcNavigator.findPath(world, new Cell(0, 1, 0), new Cell(4, 5, 0), 1000));
    }

    @Test
    void startEqualsGoalReturnsStart() {
        GridWorld world = new GridWorld().floor(0, 4, 0, 0);
        assertEquals(List.of(new Cell(2, 1, 0)), NpcNavigator.findPath(world, new Cell(2, 1, 0), new Cell(2, 1, 0), 1000));
    }
}
