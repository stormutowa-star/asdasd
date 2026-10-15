package com.ricardo.jugador2.ai;

import com.ricardo.jugador2.npc.NpcSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NpcBrainTest {
    private static final NpcSettings CFG = NpcSettings.defaults();

    /** Estado base con todo satisfecho: vida y comida llenas, sin enemigos ni órdenes, de día, recursos completos. */
    private static Snap base() {
        return new Snap();
    }

    /** Estado mutable para cambiar un campo por prueba. Por defecto es el estado base. */
    private static final class Snap {
        float health = 20f;
        int food = 20;
        boolean hasFood = false;
        int hostiles = 0;
        boolean order = false;
        boolean night = false;
        boolean bed = false;
        int logs = 16;
        int stone = 32;
        int coal = 16;
        boolean pickaxe = true;
        boolean toolsMissing = false;
        boolean full = false;

        NpcSnapshot build() {
            return new NpcSnapshot(health, food, hasFood, hostiles, order, night, bed,
                    logs, stone, coal, pickaxe, toolsMissing, full);
        }
    }

    @Test
    void lowHealthWithNearbyEnemyFlees() {
        Snap s = base();
        s.health = 5f;
        s.hostiles = 1;
        assertEquals(TaskKind.FLEE, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void lowHealthWithoutEnemyDoesNotFlee() {
        Snap s = base();
        s.health = 5f;
        s.hostiles = 0;
        assertNotEquals(TaskKind.FLEE, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void hungryWithFoodEats() {
        Snap s = base();
        s.food = 10;
        s.hasFood = true;
        assertEquals(TaskKind.EAT, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void hungryWithoutFoodDoesNotEat() {
        Snap s = base();
        s.food = 10;
        s.hasFood = false;
        assertNotEquals(TaskKind.EAT, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void foodBeatsDefendWhenHealthy() {
        Snap s = base();
        s.food = 10;
        s.hasFood = true;
        s.hostiles = 1;
        s.health = 20f;
        assertEquals(TaskKind.EAT, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void enemyNearbyDefends() {
        Snap s = base();
        s.hostiles = 1;
        s.health = 20f;
        assertEquals(TaskKind.DEFEND, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void activeOrderBeatsAutonomy() {
        Snap s = base();
        s.order = true;
        assertEquals(TaskKind.ORDER, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void missingOrderTargetFallsBackToAutonomous() {
        Snap s = base();
        s.order = false;
        s.logs = 0;
        assertEquals(TaskKind.GATHER_WOOD, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void gathersInOrder() {
        Snap noWood = base();
        noWood.logs = 0;
        assertEquals(TaskKind.GATHER_WOOD, NpcBrain.choose(noWood.build(), CFG));

        Snap noStone = base();
        noStone.stone = 0;
        assertEquals(TaskKind.GATHER_STONE, NpcBrain.choose(noStone.build(), CFG));

        Snap noCoal = base();
        noCoal.coal = 0;
        assertEquals(TaskKind.GATHER_COAL, NpcBrain.choose(noCoal.build(), CFG));
    }

    @Test
    void craftsWhenToolsMissing() {
        Snap s = base();
        s.toolsMissing = true;
        assertEquals(TaskKind.CRAFT, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void storesWhenInventoryFull() {
        // Va antes de recolectar aunque falte madera: el inventario lleno manda.
        Snap s = base();
        s.full = true;
        s.logs = 0;
        assertEquals(TaskKind.STORE, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void sleepsAtNightWithBed() {
        Snap s = base();
        s.night = true;
        s.bed = true;
        assertEquals(TaskKind.SLEEP, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void nightWithoutBedDoesNotSleep() {
        Snap s = base();
        s.night = true;
        s.bed = false;
        assertNotEquals(TaskKind.SLEEP, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void withoutAnyPickaxeCraftsBeforeGatheringStoneOrCoal() {
        // Sin pico no sale piedra ni carbón: con madera de sobra, el pico de madera va primero.
        Snap s = base();
        s.pickaxe = false;
        s.toolsMissing = true;
        s.logs = 16;
        s.stone = 0;
        s.coal = 0;
        assertEquals(TaskKind.CRAFT, NpcBrain.choose(s.build(), CFG));
    }

    @Test
    void withNothingLeftToDoIdles() {
        // Con todo cubierto no hay tarea: el NPC se queda quieto en vez de talar sin fin.
        assertNull(NpcBrain.choose(base().build(), CFG));
    }
}
