package com.keinloree.world.design;

/**
 * Base militar de Loree: plataforma circular con muros almenados, torres, barracones, torre de
 * mando con antena y una plataforma de aterrizaje para el carguero. La version grande
 * (fortaleza) agrega mas torres y un torreon escalonado. Origen = centro; y = 0 es el suelo
 * (el piso queda en y = -1).
 */
public final class OutpostDesign {

    private OutpostDesign() {
    }

    public static Grid build(int r, boolean fort) {
        final int wallH = fort ? 7 : 5;
        final int pad = fort ? 9 : 5;
        final int top = fort ? 46 : 34;
        Grid g = new Grid(-r - 8, r + 8, -3, top, -r - 8, r + 8);

        // 1. Despeje del volumen interior
        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                if (dx * dx + dz * dz <= (r + 1) * (r + 1)) {
                    for (int y = 0; y <= wallH + 16; y++) {
                        g.set(dx, y, dz, Mat.AIR);
                    }
                }
            }
        }
        floor(g, r, pad);
        walls(g, r, wallH);
        gate(g, r, wallH);

        // 2. Torres de las esquinas
        int tHalf = fort ? 3 : 2;
        int tH = fort ? 17 : 11;
        int tc = (int) Math.round(r * 0.70D);
        tower(g, tc, tc, tHalf, tH, -1, -1);
        tower(g, -tc, tc, tHalf, tH, 1, -1);
        tower(g, tc, -tc, tHalf, tH, -1, 1);
        tower(g, -tc, -tc, tHalf, tH, 1, 1);
        if (fort) {
            tower(g, 0, -r + 2, 3, 14, 0, 1);
            tower(g, r - 2, 0, 3, 14, -1, 0);
            tower(g, -r + 2, 0, 3, 14, 1, 0);
        }

        // 3. Torre de mando
        if (fort) {
            keep(g, 0, -(r - 10));
        } else {
            spire(g, 0, -(r - 5));
        }

        // 4. Barracones y cajas
        int bx = (int) Math.round(r * 0.58D);
        barracks(g, bx, -(int) Math.round(r * 0.05D), fort ? 3 : 2, fort ? 6 : 4, -1);
        barracks(g, -bx, -(int) Math.round(r * 0.05D), fort ? 3 : 2, fort ? 6 : 4, 1);
        crates(g, (int) Math.round(r * 0.35D), (int) Math.round(r * 0.55D));
        crates(g, -(int) Math.round(r * 0.35D), (int) Math.round(r * 0.55D));
        crates(g, (int) Math.round(r * 0.50D), -(int) Math.round(r * 0.45D));

        // 5. Torretas sobre el muro y pilones exteriores
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4.0D + Math.PI / 8.0D;
            int x = (int) Math.round(Math.cos(a) * (r - 0.5D));
            int z = (int) Math.round(Math.sin(a) * (r - 0.5D));
            if (z > r - 4 && Math.abs(x) <= 4) {
                continue;
            }
            turret(g, x, wallH + 1, z);
        }
        for (int k = 0; k < 4; k++) {
            double a = k * Math.PI / 2.0D + Math.PI / 4.0D;
            int x = (int) Math.round(Math.cos(a) * (r + 4));
            int z = (int) Math.round(Math.sin(a) * (r + 4));
            pylon(g, x, z, wallH + 4);
        }
        // Conductos de energia en el suelo
        for (int z = pad + 1; z <= r - 1; z++) {
            g.set(0, -1, z, Mat.TRIM);
            if (z % 3 == 0) {
                g.set(0, -1, z, Mat.GLOW);
            }
        }
        // Pilares de luz alrededor de la plataforma
        int pr = pad + 2;
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4.0D;
            int x = (int) Math.round(Math.cos(a) * pr);
            int z = (int) Math.round(Math.sin(a) * pr);
            int h = fort ? 4 : 2;
            for (int y = 0; y < h; y++) {
                g.set(x, y, z, Mat.HULL_A);
            }
            g.set(x, h, z, Mat.GLOW);
        }
        return g;
    }

    private static void floor(Grid g, int r, int pad) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r) {
                    continue;
                }
                byte m = (((dx >> 1) + (dz >> 1)) & 1) == 0 ? Mat.FLOOR : Mat.FLOOR2;
                if (d >= r - 1.0D) {
                    m = Mat.TRIM;
                } else if (d <= pad) {
                    m = Mat.BLACK;
                    if (d >= pad - 1.0D) {
                        m = Mat.TRIM;
                    }
                } else if (Math.abs(d - r * 0.62D) < 0.6D) {
                    m = Mat.WALL2;
                }
                g.set(dx, -1, dz, m);
                g.set(dx, -2, dz, Mat.FLOOR2);
            }
        }
        // Marca "H" de aterrizaje
        int p = pad / 2;
        for (int k = -p; k <= p; k++) {
            g.set(-p, -1, k, Mat.GLOW);
            g.set(p, -1, k, Mat.GLOW);
            g.set(k, -1, 0, Mat.GLOW);
        }
        // Luces empotradas en el anillo exterior
        for (int k = 0; k < 16; k++) {
            double a = k * Math.PI / 8.0D;
            int x = (int) Math.round(Math.cos(a) * (r - 2));
            int z = (int) Math.round(Math.sin(a) * (r - 2));
            g.set(x, -1, z, Mat.GLOW);
        }
    }

    private static void walls(Grid g, int r, int h) {
        double inner = r - 1.3D;
        double outer = r + 0.4D;
        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d < inner || d > outer) {
                    continue;
                }
                double ang = Math.atan2(dz, dx);
                int seg = (int) Math.floor((ang + Math.PI) / (2.0D * Math.PI) * (r * 2.5D));
                for (int y = 0; y < h; y++) {
                    byte m = ((seg + y / 2) & 1) == 0 ? Mat.HULL_A : Mat.HULL_B;
                    if (y == h - 2) {
                        m = Mat.TRIM;
                    }
                    if (y == 0) {
                        m = Mat.HULL_C;
                    }
                    g.set(dx, y, dz, m);
                }
                g.set(dx, h, dz, (seg & 1) == 0 ? Mat.HULL_E : Mat.NONE);
                if (seg % 5 == 0) {
                    g.set(dx, h, dz, Mat.GLOW);
                }
            }
        }
    }

    /** Puerta en el sur (z positivo): hueco de 5 de ancho con marco verde. */
    private static void gate(Grid g, int r, int wallH) {
        for (int dz = r - 3; dz <= r + 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                if (dx * dx + dz * dz >= (r - 1.5D) * (r - 1.5D)) {
                    for (int y = 0; y <= 3; y++) {
                        g.set(dx, y, dz, Mat.AIR);
                    }
                    g.set(dx, 4, dz, Mat.HULL_B);
                }
            }
        }
        for (int dz = r - 2; dz <= r + 1; dz++) {
            for (int y = 0; y <= 4; y++) {
                if ((double) dz * dz + 9 >= (r - 1.5D) * (r - 1.5D)) {
                    g.set(-3, y, dz, y == 4 ? Mat.GLOW : Mat.TRIM);
                    g.set(3, y, dz, y == 4 ? Mat.GLOW : Mat.TRIM);
                }
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = r - 2; dz <= r + 1; dz++) {
                if (dz * dz + dx * dx >= (r - 1.5D) * (r - 1.5D) && dz * dz + dx * dx <= (r + 0.8D) * (r + 0.8D)) {
                    g.set(dx, 5, dz, Mat.TRIM);
                    if (dx == 0) {
                        g.set(dx, 6, dz, Mat.GLOW);
                    }
                }
            }
        }
    }

    /** Torre hueca con ventanas, techo con torreta y baliza. dirx/dirz = hacia el centro. */
    private static void tower(Grid g, int cx, int cz, int half, int h, int dirx, int dirz) {
        for (int x = cx - half; x <= cx + half; x++) {
            for (int z = cz - half; z <= cz + half; z++) {
                boolean edge = x == cx - half || x == cx + half || z == cz - half || z == cz + half;
                for (int y = 0; y < h; y++) {
                    if (edge) {
                        byte m = ((x + z + y / 3) & 1) == 0 ? Mat.HULL_A : Mat.HULL_B;
                        if (y == 0) {
                            m = Mat.HULL_C;
                        } else if (y == 2 || y == h - 3) {
                            m = Mat.TRIM;
                        }
                        g.set(x, y, z, m);
                    } else {
                        g.set(x, y, z, Mat.AIR);
                    }
                }
                // techo con vuelo
                g.set(x, h, z, Mat.HULL_E);
            }
        }
        // vuelo del techo
        for (int x = cx - half - 1; x <= cx + half + 1; x++) {
            for (int z = cz - half - 1; z <= cz + half + 1; z++) {
                boolean ring = x == cx - half - 1 || x == cx + half + 1 || z == cz - half - 1 || z == cz + half + 1;
                if (ring) {
                    g.set(x, h, z, Mat.HULL_B);
                    if ((x + z) % 2 == 0) {
                        g.set(x, h + 1, z, Mat.GLOW);
                    }
                }
            }
        }
        // entrepiso y ventanas
        for (int x = cx - half + 1; x <= cx + half - 1; x++) {
            for (int z = cz - half + 1; z <= cz + half - 1; z++) {
                g.set(x, h / 2, z, Mat.FLOOR2);
            }
        }
        for (int y = 4; y <= h - 5; y += 4) {
            g.set(cx, y, cz - half, Mat.GLASS);
            g.set(cx, y + 1, cz - half, Mat.GLASS);
            g.set(cx, y, cz + half, Mat.GLASS);
            g.set(cx, y + 1, cz + half, Mat.GLASS);
            g.set(cx - half, y, cz, Mat.GLASS);
            g.set(cx - half, y + 1, cz, Mat.GLASS);
            g.set(cx + half, y, cz, Mat.GLASS);
            g.set(cx + half, y + 1, cz, Mat.GLASS);
        }
        // puerta hacia el centro
        if (dirx != 0 && Math.abs(dirx) >= Math.abs(dirz)) {
            int x = cx + dirx * half;
            g.carve(x, 0, cz, x, 2, cz);
        } else {
            int z = cz + dirz * half;
            g.carve(cx, 0, z, cx, 2, z);
        }
        // luz interior
        g.set(cx, h - 1, cz, Mat.GLOW);
        g.set(cx, h / 2 - 1, cz, Mat.GLOW);
        // torreta y baliza
        g.set(cx, h + 1, cz, Mat.HULL_A);
        g.set(cx, h + 2, cz, Mat.HULL_E);
        g.set(cx - 1, h + 2, cz - 2, Mat.ROD_N);
        g.set(cx + 1, h + 2, cz - 2, Mat.ROD_N);
        g.set(cx, h + 3, cz, Mat.GLOW);
    }

    /** Torre de mando del puesto avanzado: edificio con tejado de cristal y antena. */
    private static void spire(Grid g, int cx, int cz) {
        int half = 3;
        int h = 10;
        for (int x = cx - half; x <= cx + half; x++) {
            for (int z = cz - half; z <= cz + half; z++) {
                boolean edge = x == cx - half || x == cx + half || z == cz - half || z == cz + half;
                for (int y = 0; y < h; y++) {
                    if (edge) {
                        byte m = (y == 0) ? Mat.HULL_C : (y % 4 == 3 ? Mat.TRIM : ((x + z) % 2 == 0 ? Mat.HULL_A : Mat.HULL_B));
                        g.set(x, y, z, m);
                    } else {
                        g.set(x, y, z, Mat.AIR);
                    }
                }
                g.set(x, h, z, Mat.HULL_E);
            }
        }
        for (int y = 3; y <= 7; y++) {
            for (int s = -2; s <= 2; s += 2) {
                g.set(cx + s, y, cz + half, Mat.GLASS);
                g.set(cx + s, y, cz - half, Mat.GLASS);
            }
        }
        g.carve(cx, 0, cz + half, cx, 2, cz + half);
        // consola interior
        g.fill(cx - 2, 0, cz - 2, cx + 2, 1, cz - 2, Mat.BLACK);
        g.fill(cx - 2, 2, cz - 2, cx + 2, 2, cz - 2, Mat.GLASS);
        g.set(cx, 3, cz - 2, Mat.GLOW);
        g.set(cx, h - 1, cz, Mat.GLOW);
        // cupula y antena
        for (int x = cx - 2; x <= cx + 2; x++) {
            for (int z = cz - 2; z <= cz + 2; z++) {
                g.set(x, h + 1, z, Mat.GLASS_L);
            }
        }
        g.fill(cx - 1, h + 2, cz - 1, cx + 1, h + 2, cz + 1, Mat.GLASS_L);
        g.set(cx, h + 2, cz, Mat.GLOW);
        for (int y = h + 3; y <= h + 11; y++) {
            g.set(cx, y, cz, Mat.ROD_UP);
        }
        g.set(cx, h + 12, cz, Mat.GLOW);
    }

    /** Torreon de la fortaleza: tres pisos escalonados con vidrieras y antena. */
    private static void keep(Grid g, int cx, int cz) {
        int[] half = {6, 5, 3};
        int[] base = {0, 9, 17};
        int[] hgt = {9, 8, 7};
        for (int t = 0; t < 3; t++) {
            for (int x = cx - half[t]; x <= cx + half[t]; x++) {
                for (int z = cz - half[t]; z <= cz + half[t]; z++) {
                    boolean edge = x == cx - half[t] || x == cx + half[t] || z == cz - half[t] || z == cz + half[t];
                    for (int y = base[t]; y < base[t] + hgt[t]; y++) {
                        if (edge) {
                            int ly = y - base[t];
                            byte m = ly == 0 && t == 0 ? Mat.HULL_C : (ly % 4 == 3 ? Mat.TRIM : (((x + z) & 1) == 0 ? Mat.HULL_A : Mat.HULL_B));
                            g.set(x, y, z, m);
                        } else {
                            g.set(x, y, z, Mat.AIR);
                        }
                    }
                    int ytop = base[t] + hgt[t];
                    g.set(x, ytop, z, Mat.HULL_E);
                }
            }
            // ventanales
            for (int y = base[t] + 2; y < base[t] + hgt[t] - 2; y++) {
                for (int s = -half[t] + 2; s <= half[t] - 2; s += 2) {
                    g.set(cx + s, y, cz + half[t], Mat.GLASS);
                    g.set(cx + s, y, cz - half[t], Mat.GLASS);
                    g.set(cx + half[t], y, cz + s, Mat.GLASS);
                    g.set(cx - half[t], y, cz + s, Mat.GLASS);
                }
            }
            g.set(cx, base[t] + hgt[t] - 1, cz, Mat.GLOW);
            for (int x = cx - half[t]; x <= cx + half[t]; x += 2) {
                g.set(x, base[t] + hgt[t] + 1, cz - half[t], Mat.GLOW);
                g.set(x, base[t] + hgt[t] + 1, cz + half[t], Mat.GLOW);
            }
        }
        g.carve(cx - 1, 0, cz + 6, cx + 1, 3, cz + 6);
        for (int y = 25; y <= 38; y++) {
            g.set(cx, y, cz, Mat.ROD_UP);
        }
        g.set(cx, 39, cz, Mat.GLOW);
        for (int y = 0; y <= 6; y++) {
            g.set(cx, y, cz, Mat.GLOW);
        }
    }

    private static void barracks(Grid g, int cx, int cz, int hx, int hz, int doorDir) {
        int h = 4;
        for (int x = cx - hx; x <= cx + hx; x++) {
            for (int z = cz - hz; z <= cz + hz; z++) {
                boolean edge = x == cx - hx || x == cx + hx || z == cz - hz || z == cz + hz;
                for (int y = 0; y < h; y++) {
                    if (edge) {
                        g.set(x, y, z, y == 0 ? Mat.HULL_C : (y == 2 ? Mat.TRIM : (((x + z) & 1) == 0 ? Mat.HULL_A : Mat.HULL_B)));
                    } else {
                        g.set(x, y, z, Mat.AIR);
                    }
                }
                g.set(x, h, z, Mat.HULL_E);
                if (!edge && (x + z) % 3 == 0) {
                    g.set(x, h, z, Mat.GLOW);
                }
            }
        }
        for (int z = cz - hz + 2; z <= cz + hz - 2; z += 3) {
            g.set(cx - hx, 2, z, Mat.GLASS);
            g.set(cx + hx, 2, z, Mat.GLASS);
        }
        int dx = cx + doorDir * hx;
        g.carve(dx, 0, cz, dx, 2, cz);
        g.set(cx, h - 1, cz, Mat.GLOW);
        // literas
        for (int z = cz - hz + 1; z <= cz + hz - 1; z += 2) {
            int bxp = cx - doorDir * (hx - 1);
            g.set(bxp, 0, z, Mat.BLACK);
            g.set(bxp, 1, z, Mat.TRIM);
        }
    }

    private static void crates(Grid g, int cx, int cz) {
        g.fill(cx, 0, cz, cx + 1, 1, cz + 1, Mat.CRATE);
        g.fill(cx + 2, 0, cz, cx + 2, 0, cz + 1, Mat.BLACK);
        g.fill(cx, 2, cz, cx, 2, cz, Mat.BLACK);
        g.set(cx + 1, 2, cz + 1, Mat.TRIM);
        g.set(cx, 1, cz + 2, Mat.CRATE);
        g.set(cx + 1, 0, cz + 2, Mat.BLACK);
    }

    private static void turret(Grid g, int x, int y, int z) {
        g.set(x, y, z, Mat.HULL_B);
        g.set(x, y + 1, z, Mat.HULL_E);
        g.set(x, y + 2, z, Mat.HULL_A);
        g.set(x - 1, y + 2, z - 1, Mat.ROD_N);
        g.set(x + 1, y + 2, z - 1, Mat.ROD_N);
        g.set(x, y + 3, z, Mat.GLOW);
    }

    private static void pylon(Grid g, int x, int z, int h) {
        for (int y = 0; y < h; y++) {
            g.set(x, y, z, y % 3 == 2 ? Mat.TRIM : Mat.HULL_A);
        }
        g.set(x, h, z, Mat.GLOW);
        g.set(x, h + 1, z, Mat.ROD_UP);
    }
}
