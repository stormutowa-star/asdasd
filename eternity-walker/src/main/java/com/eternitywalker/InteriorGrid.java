package com.eternitywalker;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Rejilla de colisión del interior (muros, suelos, techos, escalones). Se genera desde el .bbmodel
 * y se valida por simulación: desde la escotilla se llega a ambas cubiertas, al puente y al reactor.
 * Coordenadas locales en bloques (1 unidad del modelo = 0.5 bloques).
 */
public final class InteriorGrid {
    public static final double S = 0.5;
    public static final double W = 0.5;   // ancho de colisión del jugador dentro del interior
    public static final double H = 1.6;   // alto de colisión del jugador dentro del interior

    private static int nx, nz, ny;
    private static double ox, oz, oy, vx, vy;
    private static byte[] bits;
    private static boolean loaded;

    private InteriorGrid() {}

    public static synchronized void load() {
        if (loaded) return;
        try (InputStream in = InteriorGrid.class.getResourceAsStream("/assets/eternitywalker/col.bin");
             GZIPInputStream gz = new GZIPInputStream(in);
             DataInputStream d = new DataInputStream(gz)) {
            nx = d.readInt();
            nz = d.readInt();
            ny = d.readInt();
            float x0 = d.readFloat(), z0 = d.readFloat(), y0 = d.readFloat(), cxz = d.readFloat(), cy = d.readFloat();
            ox = x0 * S;
            oz = z0 * S;
            oy = y0 * S;
            vx = cxz * S;
            vy = cy * S;
            bits = d.readAllBytes();
            loaded = true;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar la colision del Eternity Walker", e);
        }
    }

    private static boolean bit(int ix, int iz, int iy) {
        long i = ((long) ix * nz + iz) * ny + iy;
        return ((bits[(int) (i >> 3)] >> (int) (i & 7)) & 1) != 0;
    }

    /** true si el jugador (centro x,z; pies y; en bloques locales) choca con la estructura. */
    public static boolean collides(double x, double y, double z) {
        double hw = W / 2;
        int ix0 = (int) Math.floor((x - hw - ox) / vx + 1e-9);
        int ix1 = (int) Math.floor((x + hw - ox) / vx - 1e-9);
        int iz0 = (int) Math.floor((z - hw - oz) / vx + 1e-9);
        int iz1 = (int) Math.floor((z + hw - oz) / vx - 1e-9);
        int iy0 = (int) Math.floor((y - oy) / vy + 1e-6);
        int iy1 = (int) Math.floor((y + H - oy) / vy - 1e-6);
        if (ix0 < 0 || iz0 < 0 || ix1 >= nx || iz1 >= nz) return true;
        iy0 = Math.max(iy0, 0);
        iy1 = Math.min(iy1, ny - 1);
        if (iy1 < iy0) return false;
        for (int ix = ix0; ix <= ix1; ix++)
            for (int iz = iz0; iz <= iz1; iz++)
                for (int iy = iy0; iy <= iy1; iy++)
                    if (bit(ix, iz, iy)) return true;
        return false;
    }
}
