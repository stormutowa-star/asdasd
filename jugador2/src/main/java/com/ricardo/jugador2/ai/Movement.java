package com.ricardo.jugador2.ai;

import com.ricardo.jugador2.nav.Cell;
import com.ricardo.jugador2.nav.NpcMover;
import com.ricardo.jugador2.npc.NpcPlayer;

import java.util.List;

/** Ruta que sigue el NPC. Su entrada de movimiento la pone el gestor en cada tick, tenga o no cerebro. */
public final class Movement {
    private NpcMover mover = NpcMover.idle();

    public void follow(List<Cell> path) {
        mover = new NpcMover(path);
    }

    /** Deja de andar: el NPC se queda quieto y el juego le aplica la gravedad. */
    public void stop() {
        mover = NpcMover.idle();
    }

    public boolean isDone() {
        return mover.isDone();
    }

    public boolean isStuck() {
        return mover.isStuck();
    }

    public void tick(NpcPlayer npc) {
        mover.tick(npc);
    }
}
