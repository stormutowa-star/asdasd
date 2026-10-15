package com.ricardo.jugador2.nav;

/** Vista del mundo que necesita el navegador. En el juego la implementa WorldBlockAccess. */
public interface BlockAccess {
    /** Se puede ocupar: sin colisión. */
    boolean isPassable(int x, int y, int z);

    /** Se puede pisar: sólido. */
    boolean isSolid(int x, int y, int z);

    /** Lava, fuego o agua: prohibido. */
    boolean isHazard(int x, int y, int z);
}
