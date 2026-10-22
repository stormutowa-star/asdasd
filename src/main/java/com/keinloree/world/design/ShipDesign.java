package com.keinloree.world.design;

/**
 * Nave nodriza de Kein Loree: casco triangular tipo destructor estelar, torre de mando con domos,
 * hangar abierto por debajo, pasillos, sala del reactor, cubierta de tripulacion y puente con trono.
 * Coordenadas locales: x lateral, y vertical (0 = plano medio del casco), z de proa (0) a popa (L).
 */
public final class ShipDesign {

    public static final int L = 180;
    /** Valor de z local que queda justo encima del punto donde se invoca la nave. */
    public static final int ZC = 90;
    public static final double HW_MAX = 50.0D;

    // Puntos clave (celdas locales)
    public static final int THRONE_X = 0;
    public static final int THRONE_Y = 35;
    public static final int THRONE_Z = 153;
    public static final int HANGAR_Z0 = 72;
    public static final int HANGAR_Z1 = 126;
    public static final int[] DOCK_Z = {88, 100, 112};
    public static final int DOCK_Y = -4;
    /** Posiciones locales de las toberas (para el humo de los motores). */
    public static final int[][] ENGINES = {{0, 2, 4}, {-26, 1, 3}, {26, 1, 3}, {-40, 0, 2}, {40, 0, 2}};

    private ShipDesign() {
    }

    // ------------------------------------------------------------------ forma del casco

    public static int hw(int z) {
        return Math.max(2, (int) Math.round(HW_MAX * z / (double) L));
    }

    static double topC(int z) {
        return 4.0D + 10.0D * z / L;
    }

    static double botC(int z) {
        return 3.0D + 7.0D * z / L;
    }

    public static int topAt(int ax, int z) {
        int w = hw(z);
        if (ax > w) {
            return Integer.MIN_VALUE;
        }
        double f = (double) ax / w;
        double tc = topC(z);
        double v = f <= 0.30D ? tc : tc * (1.0D - (f - 0.30D) / 0.70D);
        return (int) Math.round(v);
    }

    public static int botAt(int ax, int z) {
        int w = hw(z);
        if (ax > w) {
            return Integer.MIN_VALUE;
        }
        double f = (double) ax / w;
        double bc = botC(z);
        double v = f <= 0.45D ? bc : bc * (1.0D - (f - 0.45D) / 0.55D);
        return (int) Math.round(v);
    }

    static int hash(int a, int b, int c) {
        int h = a * 73856093 ^ b * 19349663 ^ c * 83492791;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return h & 0x7fffffff;
    }

    // ------------------------------------------------------------------ construccion

    public static Grid build() {
        Grid g = new Grid(-58, 58, -16, 64, -2, 196);
        hull(g);
        tower(g);
        engines(g);
        plate(g);
        interiors(g);
        exterior(g);
        return g;
    }

    private static void hull(Grid g) {
        for (int z = 0; z <= L; z++) {
            int w = hw(z);
            for (int x = -w; x <= w; x++) {
                int ax = Math.abs(x);
                int t = topAt(ax, z);
                int b = botAt(ax, z);
                boolean trench = ax <= 3 && z >= 30 && z <= 128;
                int tt = trench ? t - 2 : t;
                for (int y = -b; y <= tt; y++) {
                    g.set(x, y, z, Mat.HULL_D);
                }
            }
        }
    }

    private static void tower(Grid g) {
        // Base: dos escalones
        for (int y = 8; y <= 19; y++) {
            int ix = y <= 14 ? 18 : y <= 17 ? 16 : 13;
            int z0 = y <= 14 ? 134 : y <= 17 ? 136 : 139;
            int z1 = y <= 14 ? 166 : y <= 17 ? 164 : 161;
            g.fill(-ix, y, z0, ix, y, z1, Mat.HULL_D);
        }
        // Cuello octagonal
        for (int y = 20; y <= 32; y++) {
            for (int x = -9; x <= 9; x++) {
                for (int z = 141; z <= 159; z++) {
                    if (Math.abs(x) + Math.abs(z - 150) <= 14) {
                        g.set(x, y, z, Mat.HULL_D);
                    }
                }
            }
        }
        // Puente de mando: zocalo escalonado y bloque principal
        g.fill(-16, 31, 146, 16, 31, 154, Mat.HULL_D);
        g.fill(-22, 32, 145, 22, 32, 155, Mat.HULL_D);
        g.fill(-28, 33, 143, 28, 43, 157, Mat.HULL_D);
        // Chaflan del techo del puente
        for (int x = -28; x <= 28; x++) {
            for (int z = 143; z <= 157; z++) {
                boolean edge = Math.abs(x) >= 27 || z <= 144 || z >= 156;
                if (edge) {
                    g.set(x, 43, z, Mat.NONE);
                }
            }
        }
    }

    private static void engines(Grid g) {
        engine(g, 0, 2, 8.4D);
        engine(g, -26, 1, 5.6D);
        engine(g, 26, 1, 5.6D);
        engine(g, -40, 0, 3.6D);
        engine(g, 40, 0, 3.6D);
        // Barras estructurales en la cara de popa
        for (int x = -46; x <= 46; x++) {
            for (int y : new int[]{-8, 11}) {
                int ax = Math.abs(x);
                if (ax <= hw(L) && y <= topAt(ax, L) && y >= -botAt(ax, L)) {
                    g.set(x, y, L, Mat.TRIM);
                }
            }
        }
    }

    private static void engine(Grid g, int cx, int cy, double r) {
        int ri = (int) Math.ceil(r) + 1;
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -ri; dy <= ri; dy++) {
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d > r + 1.5D) {
                    continue;
                }
                for (int z = L - 3; z <= L + 10; z++) {
                    int x = cx + dx;
                    int y = cy + dy;
                    if (d <= r - 1.5D && z > L + 2) {
                        g.set(x, y, z, Mat.AIR);
                    } else if (d <= r - 1.5D) {
                        if (z == L + 2) {
                            g.set(x, y, z, d <= r * 0.35D ? Mat.LAMP : Mat.GLOW);
                        } else if (z < L + 2) {
                            g.set(x, y, z, Mat.HULL_D);
                        }
                    } else if (d <= r + 0.2D) {
                        g.set(x, y, z, z >= L + 9 ? Mat.TRIM : Mat.HULL_A);
                    } else if (z <= L + 8) {
                        g.set(x, y, z, z == L + 7 ? Mat.HULL_E : Mat.HULL_B);
                    }
                }
            }
        }
    }

    /** Reviste con paneles las celdas expuestas del casco (el nucleo interior queda liso). */
    private static void plate(Grid g) {
        for (int x = g.x0; x <= g.maxX(); x++) {
            for (int y = g.y0; y <= g.maxY(); y++) {
                for (int z = g.z0; z <= g.maxZ(); z++) {
                    if (g.get(x, y, z) != Mat.HULL_D) {
                        continue;
                    }
                    if (g.get(x + 1, y, z) == Mat.NONE || g.get(x - 1, y, z) == Mat.NONE
                            || g.get(x, y + 1, z) == Mat.NONE || g.get(x, y - 1, z) == Mat.NONE
                            || g.get(x, y, z + 1) == Mat.NONE || g.get(x, y, z - 1) == Mat.NONE) {
                        g.set(x, y, z, panel(x, y, z));
                    }
                }
            }
        }
    }

    static byte panel(int x, int y, int z) {
        int h = hash(Math.floorDiv(x, 5), Math.floorDiv(y, 3), Math.floorDiv(z, 6));
        boolean seam = Math.floorMod(x, 5) == 0 || Math.floorMod(z, 6) == 0;
        if (seam) {
            return (h & 3) == 0 ? Mat.HULL_C : Mat.HULL_B;
        }
        switch ((h >> 3) & 7) {
            case 0, 1, 2:
                return Mat.HULL_A;
            case 3, 4:
                return Mat.HULL_B;
            case 5:
                return Mat.HULL_E;
            case 6:
                return Mat.HULL_F;
            default:
                return Mat.HULL_C;
        }
    }

    // ------------------------------------------------------------------ interiores

    private static void interiors(Grid g) {
        corridorFront(g);
        hangar(g);
        cargoHolds(g);
        rearCorridor(g);
        reactorHall(g);
        crewDeck(g);
        atrium(g);
        shaft(g);
        bridge(g);
    }

    private static void lightsCeil(Grid g, int x, int y, int z0, int z1, int step) {
        for (int z = z0; z <= z1; z += step) {
            g.set(x, y, z, Mat.GLOW);
        }
    }

    private static void corridorFront(Grid g) {
        g.room(-2, -2, 38, 2, 1, 71, Mat.FLOOR, Mat.WALL, Mat.WALL2);
        for (int z = 38; z <= 71; z++) {
            g.set(-2, -3, z, Mat.TRIM);
            g.set(2, -3, z, Mat.TRIM);
            if (z % 4 == 0) {
                for (int y = -2; y <= 1; y++) {
                    g.set(-3, y, z, Mat.WALL2);
                    g.set(3, y, z, Mat.WALL2);
                }
            }
            if (z % 8 == 2) {
                g.set(-3, 0, z, Mat.GLOW);
                g.set(3, 0, z, Mat.GLOW);
            }
        }
        lightsCeil(g, 0, 2, 40, 70, 6);
        // Sello del fondo con emblema
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 1; y++) {
                g.set(x, y, 37, Mat.WALL2);
            }
        }
        g.set(0, -1, 37, Mat.GLOW);
        g.set(0, 0, 37, Mat.GLOW);
        g.set(-1, 0, 37, Mat.TRIM);
        g.set(1, 0, 37, Mat.TRIM);
    }

    private static void hangar(Grid g) {
        final int z0 = HANGAR_Z0;
        final int z1 = HANGAR_Z1;
        g.room(-11, -2, z0 + 1, 11, 1, z1 - 1, Mat.FLOOR, Mat.WALL2, Mat.WALL2);
        // Bahia abierta por debajo
        g.carve(-7, -16, z0 + 5, 7, -3, z1 - 5);
        // Muros de la bahia (donde hay casco) y borde luminoso de las cornisas
        for (int z = z0 + 4; z <= z1 - 4; z++) {
            for (int y = -14; y <= -4; y++) {
                for (int sx : new int[]{-8, 8}) {
                    if (g.get(sx, y, z) != Mat.NONE && g.get(sx, y, z) != Mat.AIR) {
                        g.set(sx, y, z, z % 6 == 0 ? Mat.GLOW : Mat.WALL2);
                    }
                }
            }
        }
        for (int x = -8; x <= 8; x++) {
            for (int y = -14; y <= -4; y++) {
                for (int sz : new int[]{z0 + 4, z1 - 4}) {
                    if (g.get(x, y, sz) != Mat.NONE && g.get(x, y, sz) != Mat.AIR) {
                        g.set(x, y, sz, x % 4 == 0 ? Mat.GLOW : Mat.WALL2);
                    }
                }
            }
        }
        for (int x = -8; x <= 8; x++) {
            g.set(x, -3, z0 + 4, Mat.TRIM);
            g.set(x, -3, z1 - 4, Mat.TRIM);
        }
        for (int z = z0 + 4; z <= z1 - 4; z++) {
            g.set(-8, -3, z, Mat.TRIM);
            g.set(8, -3, z, Mat.TRIM);
        }
        // Techo: franjas de luz
        for (int z = z0 + 2; z <= z1 - 2; z++) {
            if (z % 3 != 0) {
                for (int x : new int[]{-8, 0, 8}) {
                    g.set(x, 2, z, Mat.GLOW);
                }
            }
        }
        // Columnas de las paredes
        for (int z = z0 + 4; z <= z1 - 4; z += 8) {
            for (int sx : new int[]{-11, 11}) {
                for (int y = -2; y <= 1; y++) {
                    g.set(sx, y, z, Mat.HULL_A);
                }
                g.set(sx, 1, z, Mat.GLOW);
            }
        }
        // Puertas frontal y trasera
        g.carve(-2, -2, z0, 2, 1, z0);
        g.carve(-2, -2, z1, 2, 1, z1);
        // Guias de aterrizaje en el techo (sobre cada hueco de atraque)
        for (int dz : DOCK_Z) {
            for (int x = -3; x <= 3; x++) {
                if (Math.abs(x) == 3) {
                    g.set(x, 2, dz, Mat.TRIM);
                }
            }
            g.set(0, 2, dz - 4, Mat.TRIM);
            g.set(0, 2, dz + 4, Mat.TRIM);
        }
    }

    private static void cargoHolds(Grid g) {
        for (int s : new int[]{-1, 1}) {
            int xa = s < 0 ? -19 : 14;
            int xb = s < 0 ? -14 : 19;
            g.room(xa, -2, 82, xb, 1, 118, Mat.FLOOR2, Mat.WALL, Mat.WALL2);
            // puerta al hangar
            int dx = s < 0 ? -13 : 12;
            g.carve(Math.min(dx, dx + 1), -2, 97, Math.max(dx, dx + 1), 0, 99);
            // cajas de carga
            for (int z = 84; z <= 116; z += 4) {
                int h = hash(z, s, 7) & 3;
                int cx0 = s < 0 ? xa : xb - 1;
                g.fill(cx0, -2, z, cx0 + 1, -2 + (h & 1), z + 1, Mat.CRATE);
                g.set(cx0, -1 + (h & 1), z, Mat.BLACK);
                if ((z / 4) % 3 == 0) {
                    int cx1 = s < 0 ? xb - 1 : xa;
                    g.fill(cx1, -2, z, cx1 + 1, -1, z + 1, Mat.BLACK);
                    g.set(cx1, 0, z, Mat.TRIM);
                }
            }
            for (int z = 84; z <= 116; z += 8) {
                g.set((xa + xb) / 2, 2, z, Mat.GLOW);
            }
        }
    }

    private static void rearCorridor(Grid g) {
        g.room(-2, -2, 127, 2, 1, 133, Mat.FLOOR, Mat.WALL, Mat.WALL2);
        for (int z = 127; z <= 133; z++) {
            g.set(-2, -3, z, Mat.TRIM);
            g.set(2, -3, z, Mat.TRIM);
        }
        g.set(0, 2, 129, Mat.GLOW);
        g.set(0, 2, 132, Mat.GLOW);
    }

    private static void reactorHall(Grid g) {
        g.room(-13, -7, 135, 13, 7, 170, Mat.FLOOR2, Mat.WALL, Mat.WALL2);
        g.carve(-2, -2, 134, 2, 1, 134);
        // Bancos de generadores a lo largo de los muros
        for (int z = 137; z <= 168; z += 6) {
            for (int sx : new int[]{-12, 12}) {
                int x0 = sx < 0 ? -12 : 10;
                g.fill(x0, -7, z, x0 + 2, -4, z + 2, Mat.BLACK);
                g.fill(x0, -3, z, x0 + 2, -3, z + 2, Mat.IRON);
                g.set(x0 + 1, -2, z + 1, Mat.GLOW);
            }
        }
        for (int z : new int[]{138, 146, 154, 162, 168}) {
            for (int sx : new int[]{-13, 13}) {
                for (int y = -6; y <= 6; y++) {
                    g.set(sx, y, z, Mat.GLOW);
                }
            }
        }
        for (int z = 138; z <= 168; z += 6) {
            for (int x : new int[]{-9, 0, 9}) {
                g.set(x, 8, z, Mat.GLOW);
            }
        }
        // Pasarela central y plataforma del nucleo
        final int cz = 160;
        g.fill(-1, -3, 135, 1, -3, cz - 6, Mat.FLOOR);
        for (int z = 135; z <= cz - 6; z++) {
            if (z < 145 || z > 149) {
                g.set(-2, -2, z, Mat.GLASS);
                g.set(2, -2, z, Mat.GLASS);
            }
            if (z % 4 == 0) {
                g.set(-1, -3, z, Mat.TRIM);
                g.set(1, -3, z, Mat.TRIM);
            }
        }
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 >= 16 && d2 <= 81) {
                    g.set(dx, -3, cz + dz, Mat.FLOOR);
                    if (d2 >= 72) {
                        g.set(dx, -3, cz + dz, Mat.TRIM);
                        g.set(dx, -2, cz + dz, Mat.GLASS);
                    }
                }
            }
        }
        // Rama de la pasarela hacia el ascensor
        g.fill(-4, -3, 146, -2, -3, 148, Mat.FLOOR);
        // Nucleo del reactor
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > 10) {
                    continue;
                }
                for (int y = -7; y <= 7; y++) {
                    g.set(dx, y, cz + dz, d2 >= 5 ? Mat.GLASS : Mat.GLOW);
                }
            }
        }
        for (int y : new int[]{-6, -1, 4, 7}) {
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 >= 12 && d2 <= 22) {
                        g.set(dx, y, cz + dz, Mat.EMERALD);
                    }
                }
            }
        }
        // Base del nucleo
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 64) {
                    g.set(dx, -7, cz + dz, d2 >= 52 ? Mat.TRIM : Mat.FLOOR);
                }
            }
        }
    }

    private static void crewDeck(Grid g) {
        g.room(-12, 13, 140, 12, 16, 160, Mat.FLOOR2, Mat.WALL, Mat.WALL2);
        for (int z = 142; z <= 158; z += 4) {
            g.set(-9, 17, z, Mat.GLOW);
            g.set(0, 17, z, Mat.GLOW);
            g.set(9, 17, z, Mat.GLOW);
        }
        // Literas
        for (int z = 141; z <= 159; z += 3) {
            for (int sx : new int[]{-1, 1}) {
                int x0 = sx < 0 ? -12 : 10;
                g.fill(x0, 13, z, x0 + 2, 13, z + 1, Mat.BLACK);
                g.fill(x0, 14, z, x0 + 2, 14, z + 1, Mat.TRIM);
                g.fill(x0, 15, z, x0 + 2, 15, z + 1, Mat.BLACK);
                g.fill(x0, 16, z, x0 + 2, 16, z + 1, Mat.TRIM);
            }
        }
        // Mesa central con holo
        g.fill(-1, 13, 149, 1, 13, 151, Mat.BLACK);
        g.fill(-1, 14, 149, 1, 14, 151, Mat.GLASS);
        g.set(0, 15, 150, Mat.ROD_UP);
    }

    private static void atrium(Grid g) {
        g.room(-7, 20, 145, 7, 31, 155, Mat.FLOOR2, Mat.WALL, Mat.WALL2);
        // Pilar luminoso
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int y = 20; y <= 31; y++) {
                    g.set(4 + dx, y, 150 + dz, (dx == 0 && dz == 0) ? Mat.GLOW : Mat.GLASS);
                }
            }
        }
        for (int y = 21; y <= 30; y += 3) {
            g.fill(-7, y, 145, 7, y, 145, Mat.TRIM);
            g.fill(-7, y, 155, 7, y, 155, Mat.TRIM);
        }
        for (int y : new int[]{22, 28}) {
            g.set(-7, y, 150, Mat.GLOW);
            g.set(7, y, 150, Mat.GLOW);
        }
    }

    /** Ascensor con escalera de mano: del reactor (y=-3) hasta el piso del puente (y=33). */
    private static void shaft(Grid g) {
        final int xa = -7;
        final int xb = -5;
        final int za = 146;
        final int zb = 148;
        for (int y = -3; y <= 32; y++) {
            for (int x = xa - 1; x <= xb + 1; x++) {
                for (int z = za - 1; z <= zb + 1; z++) {
                    boolean inside = x >= xa && x <= xb && z >= za && z <= zb;
                    g.set(x, y, z, inside ? Mat.AIR : Mat.WALL2);
                }
            }
        }
        for (int y = -2; y <= 33; y++) {
            g.set(xa, y, 147, Mat.LADDER_E);
        }
        // Abertura en el piso del puente
        g.carve(xa, 33, za, xb, 33, zb);
        // Puertas: al reactor (lado este) y a la cubierta de tripulacion
        g.carve(xb + 1, -2, 147, xb + 1, 0, 147);
        g.carve(xb + 1, 13, 147, xb + 1, 15, 147);
        // Aire sobre el pasamanos de la rama
        for (int y = 0; y <= 2; y += 2) {
            g.set(xb + 1, y, 147, y == 2 ? Mat.GLOW : Mat.AIR);
        }
        g.set(xb + 1, 0, 147, Mat.AIR);
    }

    private static void bridge(Grid g) {
        g.room(-27, 34, 144, 27, 40, 156, Mat.FLOOR, Mat.WALL, Mat.WALL2);
        // Ventanal frontal (z = 143) con parteluces
        for (int x = -27; x <= 27; x++) {
            for (int y = 35; y <= 40; y++) {
                g.set(x, y, 143, x % 6 == 0 ? Mat.HULL_A : Mat.GLASS);
            }
        }
        // Ventanas laterales
        for (int z = 146; z <= 154; z++) {
            for (int y = 36; y <= 39; y++) {
                if (z % 3 != 0) {
                    g.set(-28, y, z, Mat.GLASS);
                    g.set(28, y, z, Mat.GLASS);
                }
            }
        }
        // Luces del techo
        for (int x = -24; x <= 24; x += 6) {
            for (int z : new int[]{147, 153}) {
                g.set(x, 41, z, Mat.GLOW);
            }
        }
        for (int x = -27; x <= 27; x++) {
            g.set(x, 34, 144, x % 4 == 0 ? Mat.TRIM : Mat.FLOOR);
        }
        // Consolas frente al ventanal
        for (int cx : new int[]{-22, -16, -10, 10, 16, 22}) {
            g.fill(cx - 2, 34, 145, cx + 2, 35, 146, Mat.BLACK);
            g.fill(cx - 2, 36, 146, cx + 2, 36, 146, Mat.GLASS);
            g.set(cx, 37, 146, Mat.GLOW);
            g.fill(cx - 2, 35, 145, cx + 2, 35, 145, Mat.TRIM);
        }
        // Mesa holografica con planeta
        g.fill(-2, 34, 148, 2, 34, 150, Mat.BLACK);
        g.fill(-1, 35, 148, 1, 35, 150, Mat.BLACK);
        g.fill(-2, 35, 148, 2, 35, 150, Mat.GLASS);
        g.fill(-1, 35, 149, 1, 35, 149, Mat.GLOW);
        sphere(g, 0, 39, 149, 2.2D, Mat.GLASS_L, Mat.GLOW);
        // Estrado y trono
        g.fill(-5, 34, 151, 5, 34, 156, Mat.FLOOR2);
        for (int x = -5; x <= 5; x++) {
            g.set(x, 34, 151, Mat.TRIM);
        }
        for (int x = -3; x <= 3; x++) {
            g.set(x, 34, 150, Mat.STAIR_S);
        }
        g.set(THRONE_X, THRONE_Y, THRONE_Z, Mat.SLAB_B);
        g.set(-2, 35, 153, Mat.CHISEL);
        g.set(2, 35, 153, Mat.CHISEL);
        g.set(-2, 36, 153, Mat.SLAB_B);
        g.set(2, 36, 153, Mat.SLAB_B);
        for (int x = -3; x <= 3; x++) {
            for (int y = 35; y <= 41; y++) {
                boolean core = Math.abs(x) <= 1 && y >= 37;
                g.set(x, y, 155, core ? Mat.GLOW : Mat.CHISEL);
            }
        }
        for (int x = -4; x <= 4; x++) {
            g.set(x, 42, 155, Mat.CHISEL);
        }
        g.set(0, 41, 155, Mat.EMERALD);
        // Pilares con luz a los lados del trono
        for (int sx : new int[]{-5, 5}) {
            for (int y = 35; y <= 40; y++) {
                g.set(sx, y, 156, Mat.CHISEL);
            }
            g.set(sx, 40, 156, Mat.GLOW);
        }
        // Despeje sobre el trono
        g.carve(-1, 36, 152, 1, 40, 154);
    }

    private static void sphere(Grid g, int cx, int cy, int cz, double r, byte shell, byte core) {
        int ri = (int) Math.ceil(r);
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -ri; dy <= ri; dy++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (d <= r) {
                        g.set(cx + dx, cy + dy, cz + dz, d >= r - 1.0D ? shell : core);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ exterior

    private static void exterior(Grid g) {
        edgeLights(g);
        dorsal(g);
        underside(g);
        domesAndMasts(g);
        turrets(g);
        towerDetails(g);
    }

    private static void edgeLights(Grid g) {
        for (int z = 6; z <= L; z++) {
            int w = hw(z);
            for (int s : new int[]{-1, 1}) {
                // luces del borde
                if (z % 4 == 0) {
                    g.set(s * w, 0, z, Mat.GLOW);
                    g.set(s * (w - 1), 0, z, Mat.GLOW);
                }
                // linea verde del "hombro"
                int ax = (int) Math.round(w * 0.66D);
                if (ax >= 1) {
                    int y = topAt(ax, z);
                    if (g.get(s * ax, y, z) != Mat.NONE) {
                        g.set(s * ax, y, z, Mat.TRIM);
                    }
                }
                // ventanas del flanco superior
                int ax2 = (int) Math.round(w * 0.45D);
                if (z % 3 == 0 && ax2 >= 3 && z >= 20 && z <= 175) {
                    int y = topAt(ax2, z);
                    if (g.get(s * ax2, y, z) != Mat.NONE && !(z >= 132 && z <= 168 && ax2 <= 20)) {
                        g.set(s * ax2, y, z, Mat.GLASS_L);
                    }
                }
            }
        }
        // Linea verde del borde de proa
        for (int z = 2; z <= 8; z++) {
            for (int x = -2; x <= 2; x++) {
                int y = topAt(Math.abs(x), z);
                if (y != Integer.MIN_VALUE && g.get(x, y, z) != Mat.NONE) {
                    g.set(x, y, z, Mat.TRIM);
                }
            }
        }
    }

    private static void dorsal(Grid g) {
        // Zanja central: luces y bordes
        for (int z = 30; z <= 128; z++) {
            int tt = topAt(0, z) - 2;
            if (z % 4 == 0) {
                g.set(0, tt, z, Mat.GLOW);
            }
            g.set(-3, tt + 1, z, Mat.TRIM);
            g.set(3, tt + 1, z, Mat.TRIM);
            if (z % 12 == 0) {
                for (int x = -2; x <= 2; x++) {
                    g.set(x, tt + 1, z, Mat.HULL_E);
                }
            }
        }
        // Greebles y paneles elevados sobre el dorso
        for (int z = 20; z <= 176; z += 2) {
            for (int x = 4; x <= 46; x += 2) {
                int h = hash(x, z, 5);
                if ((h % 23) != 0) {
                    continue;
                }
                if (z >= 130 && z <= 168 && x <= 20) {
                    continue;
                }
                int w = hw(z);
                double f = (double) x / w;
                if (f > 0.27D) {
                    continue;
                }
                int y0 = topAt(x, z) + 1;
                byte m = (h & 1) == 0 ? Mat.HULL_E : Mat.HULL_A;
                int hgt = 1 + ((h >> 4) & 1);
                for (int s : new int[]{-1, 1}) {
                    g.fill(s * x, y0, z, s * (x + 1), y0 + hgt - 1, z + 1, m);
                }
            }
        }
    }

    private static void underside(Grid g) {
        for (int z = 14; z <= L; z += 2) {
            int y = -botAt(0, z);
            if (z < HANGAR_Z0 || z > HANGAR_Z1) {
                g.set(0, y, z, z % 6 == 0 ? Mat.GLOW : Mat.HULL_E);
            }
        }
        // Luces alrededor de la boca del hangar
        for (int x = -12; x <= 12; x += 2) {
            for (int z : new int[]{HANGAR_Z0 - 1, HANGAR_Z1 + 1}) {
                int y = -botAt(Math.abs(x), z);
                g.set(x, y, z, Mat.GLOW);
            }
        }
        for (int z = HANGAR_Z0; z <= HANGAR_Z1; z += 3) {
            for (int x : new int[]{-12, 12}) {
                int y = -botAt(12, z);
                g.set(x, y, z, Mat.GLOW);
            }
        }
        // Paneles inferiores elevados
        for (int z = 20; z <= 176; z += 2) {
            for (int x = 4; x <= 40; x += 2) {
                int h = hash(x, z, 11);
                if ((h % 29) != 0) {
                    continue;
                }
                if (z >= HANGAR_Z0 - 2 && z <= HANGAR_Z1 + 2 && x <= 14) {
                    continue;
                }
                int w = hw(z);
                if ((double) x / w > 0.44D) {
                    continue;
                }
                int y0 = -botAt(x, z) - 1;
                for (int s : new int[]{-1, 1}) {
                    g.fill(s * x, y0, z, s * (x + 1), y0, z + 1, Mat.HULL_E);
                }
            }
        }
    }

    private static void domesAndMasts(Grid g) {
        for (int s : new int[]{-1, 1}) {
            int cx = s * 22;
            // pedestal
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (dx * dx + dz * dz <= 10) {
                        g.set(cx + dx, 44, 150 + dz, Mat.HULL_A);
                        g.set(cx + dx, 45, 150 + dz, dx * dx + dz * dz >= 8 ? Mat.TRIM : Mat.HULL_B);
                    }
                }
            }
            // domo
            for (int dx = -5; dx <= 5; dx++) {
                for (int dy = 0; dy <= 5; dy++) {
                    for (int dz = -5; dz <= 5; dz++) {
                        double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (d <= 5.3D && d >= 3.6D) {
                            byte m = dy == 2 ? Mat.TRIM : (dy % 2 == 0 ? Mat.HULL_E : Mat.HULL_A);
                            if (dy == 5 && dx == 0 && dz == 0) {
                                m = Mat.GLOW;
                            }
                            g.set(cx + dx, 46 + dy, 150 + dz, m);
                        }
                    }
                }
            }
            g.set(cx, 52, 150, Mat.GLOW);
        }
        // Antenas
        for (int[] m : new int[][]{{0, 14}, {-10, 9}, {10, 9}, {-34, 6}, {34, 6}}) {
            int x = m[0];
            int len = m[1];
            int y0 = x == 0 || Math.abs(x) == 10 ? 44 : 44;
            for (int y = 0; y < len; y++) {
                g.set(x, y0 + y, 150, Mat.ROD_UP);
            }
            g.set(x, y0 + len, 150, Mat.GLOW);
        }
        // Parabola de comunicaciones
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 16) {
                    g.set(dx, 44 + (d2 >= 9 ? 1 : 0), 152 + dz / 2 + 4, Mat.NONE);
                }
            }
        }
    }

    private static void turrets(Grid g) {
        int[][] spots = {
                {14, 38}, {-14, 38}, {18, 60}, {-18, 60}, {22, 84}, {-22, 84},
                {20, 118}, {-20, 118}, {28, 144}, {-28, 144}, {32, 172}, {-32, 172}, {16, 172}, {-16, 172}
        };
        for (int[] s : spots) {
            int x = s[0];
            int z = s[1];
            int ax = Math.abs(x);
            int y = topAt(ax, z);
            if (y == Integer.MIN_VALUE) {
                continue;
            }
            // base 3x3 y torreta
            g.fill(x - 1, y + 1, z - 1, x + 1, y + 1, z + 1, Mat.HULL_B);
            g.fill(x - 1, y + 2, z - 1, x + 1, y + 2, z + 1, Mat.HULL_E);
            g.set(x, y + 3, z, Mat.HULL_A);
            g.set(x - 1, y + 3, z - 2, Mat.ROD_N);
            g.set(x + 1, y + 3, z - 2, Mat.ROD_N);
            g.set(x - 1, y + 3, z - 1, Mat.HULL_A);
            g.set(x + 1, y + 3, z - 1, Mat.HULL_A);
            g.set(x, y + 4, z, Mat.GLOW);
        }
    }

    private static void towerDetails(Grid g) {
        // Bandas verdes y luces en la base y el cuello
        for (int y : new int[]{10, 14, 18}) {
            int ix = y <= 14 ? 18 : y <= 17 ? 16 : 13;
            int z0 = y <= 14 ? 134 : y <= 17 ? 136 : 139;
            int z1 = y <= 14 ? 166 : y <= 17 ? 164 : 161;
            for (int x = -ix; x <= ix; x += 3) {
                g.set(x, y, z0, Mat.GLOW);
                g.set(x, y, z1, Mat.GLOW);
            }
            for (int z = z0; z <= z1; z += 3) {
                g.set(-ix, y, z, Mat.GLOW);
                g.set(ix, y, z, Mat.GLOW);
            }
        }
        for (int y = 22; y <= 30; y += 4) {
            for (int x = -9; x <= 9; x++) {
                for (int z = 141; z <= 159; z++) {
                    if (Math.abs(x) + Math.abs(z - 150) == 14 && (x + z) % 3 == 0) {
                        g.set(x, y, z, Mat.GLOW);
                    }
                }
            }
        }
        // Bandas del puente
        for (int x = -28; x <= 28; x++) {
            g.set(x, 42, 143, x % 3 == 0 ? Mat.GLOW : Mat.TRIM);
            g.set(x, 33, 143, Mat.TRIM);
        }
        for (int z = 143; z <= 157; z++) {
            g.set(-28, 42, z, z % 3 == 0 ? Mat.GLOW : Mat.TRIM);
            g.set(28, 42, z, z % 3 == 0 ? Mat.GLOW : Mat.TRIM);
        }
    }
}
