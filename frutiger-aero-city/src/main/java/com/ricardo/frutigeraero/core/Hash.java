package com.ricardo.frutigeraero.core;

/** Hash determinista (SplitMix64) y value noise. Java puro, sin estado: no depende del orden de las consultas. */
public final class Hash {

    private static final long GOLDEN = 0x9E3779B97F4A7C15L;

    private Hash() {
    }

    /** Finalizador SplitMix64. */
    public static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    // Cuatro sobrecargas explicitas (sin varargs) para no reservar memoria en cada llamada.

    public static long of(long seed, int a) {
        return step(mix(seed), a);
    }

    public static long of(long seed, int a, int b) {
        return step(step(mix(seed), a), b);
    }

    public static long of(long seed, int a, int b, int c) {
        return step(step(step(mix(seed), a), b), c);
    }

    public static long of(long seed, int a, int b, int c, int d) {
        return step(step(step(step(mix(seed), a), b), c), d);
    }

    /** Convierte un hash en un double uniforme en [0, 1). */
    public static double unit(long h) {
        return (h >>> 11) * 0x1.0p-53;
    }

    /** Value noise bilineal con suavizado t*t*(3-2t). Devuelve un valor en [0, 1]. */
    public static double noise01(long seed, double x, double z) {
        double fx = Math.floor(x);
        double fz = Math.floor(z);
        int x0 = (int) fx;
        int z0 = (int) fz;
        double tx = smooth(x - fx);
        double tz = smooth(z - fz);
        double v00 = unit(of(seed, x0, z0));
        double v10 = unit(of(seed, x0 + 1, z0));
        double v01 = unit(of(seed, x0, z0 + 1));
        double v11 = unit(of(seed, x0 + 1, z0 + 1));
        double near = v00 + (v10 - v00) * tx;
        double far = v01 + (v11 - v01) * tx;
        return near + (far - near) * tz;
    }

    private static long step(long h, int p) {
        return mix(h + p * GOLDEN);
    }

    private static double smooth(double t) {
        return t * t * (3.0 - 2.0 * t);
    }
}
