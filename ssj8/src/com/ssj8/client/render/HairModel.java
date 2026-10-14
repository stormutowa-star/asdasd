package com.ssj8.client.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;

public final class HairModel {
    private static final float[] GOLD;
    private static final float[] GOLD_D;
    private static final float[] SILVER;
    private static final float[] SILVER_D;
    private static final float[] TAIL;
    private static final List<Lock> LOCKS;

    private HairModel() {
    }

    private static void add(boolean mane, float[] color, float rx, float ry, float rz, float dx, float dy, float dz,
                            float len, float size, int cubes, float bx, float by, float bz) {
        Lock lock = new Lock();
        lock.mane = mane;
        lock.color = color;
        lock.rx = rx;
        lock.ry = ry;
        lock.rz = rz;
        float n = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        lock.dx = dx / n;
        lock.dy = dy / n;
        lock.dz = dz / n;
        lock.len = len;
        lock.size = size;
        lock.cubes = cubes;
        lock.bx = bx;
        lock.by = by;
        lock.bz = bz;
        lock.phase = LOCKS.size() * 1.37f;
        LOCKS.add(lock);
    }

    /**
     * Dibuja los mechones. {@code growth} va de 0 a 1 durante la transformacion: el pelo nace
     * casi sin largo y se alarga hasta su tamano final. Plateado y dorado usan el mismo
     * transform de cabeza (ver Ssj8HairLayer), asi que no se separan al mirar hacia abajo.
     */
    public static void drawHair(Prism.Sink sink, float age, float windR, float windU, float windB, float growth, boolean mane) {
        float grow = Shapes.smooth(0.0f, 1.0f, growth);
        for (Lock lock : LOCKS) {
            if (lock.mane != mane) continue;
            float len = lock.len * (0.02f + 0.98f * grow);
            float flutter = Mth.sin(age * 0.1f + lock.phase) * 0.03f;
            float sway = mane ? 0.35f : 0.08f;
            strand(sink, lock, len, windR * sway + flutter, windU * sway, windB * sway, grow, mane ? 0.42f : 0.62f, mane);
        }
    }

    public static void drawTail(Prism.Sink sink, float age, float windR, float windU, float windB, float growth) {
        float[][] points = new float[][]{
                {0.0f, -0.62f, 0.12f}, {0.02f, -0.64f, 0.34f}, {0.1f, -0.7f, 0.6f}, {0.28f, -0.82f, 0.78f},
                {0.46f, -1.02f, 0.74f}, {0.52f, -1.24f, 0.56f}, {0.42f, -1.34f, 0.36f}, {0.28f, -1.28f, 0.28f}
        };
        int segments = 16;
        float wave = Mth.sin(age * 0.09f) * 0.05f;
        float[] previous = null;
        for (int i = 0; i <= segments; ++i) {
            float along = (float) i / (float) segments * (float) (points.length - 1);
            int base = Math.min(points.length - 2, (int) along);
            float frac = along - (float) base;
            float[] current = new float[3];
            for (int j = 0; j < 3; ++j) {
                current[j] = Mth.lerp(frac, points[base][j], points[base + 1][j]);
            }
            float t = (float) i / (float) segments;
            current[0] = current[0] + (wave + windR * 0.2f) * t * t;
            current[2] = current[2] + windB * 0.2f * t * t;
            if (previous != null) {
                cube(sink,
                        (current[0] + previous[0]) * 0.5f, (current[1] + previous[1]) * 0.5f, (current[2] + previous[2]) * 0.5f,
                        current[0] - previous[0], current[1] - previous[1], current[2] - previous[2],
                        0.07f * (1.0f - 0.4f * t), 0.08f, TAIL);
            }
            previous = current;
        }
    }

    private static void strand(Prism.Sink sink, Lock lock, float len, float bendX, float bendY, float bendZ,
                               float grow, float taper, boolean mane) {
        int segments = Math.min(24, Math.max(8, lock.cubes * 2));
        float[][] previousRing = null;
        float[] side = null;
        for (int i = 0; i <= segments; ++i) {
            float t = (float) i / (float) segments;
            float cx = lock.rx + len * (lock.dx * t + lock.bx * t * t) + bendX * t * t;
            float cy = lock.ry + len * (lock.dy * t + lock.by * t * t) + bendY * t * t;
            float cz = lock.rz + len * (lock.dz * t + lock.bz * t * t) + bendZ * t * t;
            float tx = len * (lock.dx + 2.0f * lock.bx * t) + 2.0f * bendX * t;
            float ty = len * (lock.dy + 2.0f * lock.by * t) + 2.0f * bendY * t;
            float tz = len * (lock.dz + 2.0f * lock.bz * t) + 2.0f * bendZ * t;
            float tl = Mth.sqrt(tx * tx + ty * ty + tz * tz);
            if (tl < 1.0E-5f) {
                tx = 0.0f;
                ty = 1.0f;
                tz = 0.0f;
                tl = 1.0f;
            }
            tx /= tl;
            ty /= tl;
            tz /= tl;
            if (side == null) {
                side = referenceSide(tx, ty, tz);
            } else {
                float dot = side[0] * tx + side[1] * ty + side[2] * tz;
                float[] projected = new float[]{side[0] - dot * tx, side[1] - dot * ty, side[2] - dot * tz};
                side = projected;
                if (Mth.sqrt(side[0] * side[0] + side[1] * side[1] + side[2] * side[2]) < 1.0E-4f) {
                    side = referenceSide(tx, ty, tz);
                }
            }
            normalize(side);
            float[] binormal = cross(tx, ty, tz, side[0], side[1], side[2]);
            float radius = lock.size * 0.5f * (1.0f - taper * t) * (0.5f + 0.5f * grow);
            float[][] ring = ring(cx, cy, cz, side, binormal, radius);
            if (previousRing != null) {
                for (int j = 0; j < 4; ++j) {
                    int k = (j + 1) & 3;
                    float nx = 0.5f * (ring[j][0] + ring[k][0]) - cx;
                    float ny = 0.5f * (ring[j][1] + ring[k][1]) - cy;
                    float nz = 0.5f * (ring[j][2] + ring[k][2]) - cz;
                    if (mane) {
                        quadSolid(sink, previousRing[j], previousRing[k], ring[k], ring[j], nx, ny, nz, lock.color);
                    } else {
                        quad(sink, previousRing[j], previousRing[k], ring[k], ring[j], nx, ny, nz, lock.color);
                    }
                }
            }
            previousRing = ring;
            if (i == segments) {
                if (mane) {
                    quadSolid(sink, ring[0], ring[1], ring[2], ring[3], tx, ty, tz, lock.color);
                } else {
                    quad(sink, ring[0], ring[1], ring[2], ring[3], tx, ty, tz, lock.color);
                }
            }
        }
    }

    private static float[] referenceSide(float x, float y, float z) {
        float[] axis = Math.abs(x) < 0.9f ? new float[]{1.0f, 0.0f, 0.0f} : new float[]{0.0f, 0.0f, 1.0f};
        float[] side = cross(x, y, z, axis[0], axis[1], axis[2]);
        normalize(side);
        return side;
    }

    private static float[][] ring(float cx, float cy, float cz, float[] side, float[] binormal, float radius) {
        float[][] ring = new float[4][3];
        int[] signSide = new int[]{1, -1, -1, 1};
        int[] signBinormal = new int[]{1, 1, -1, -1};
        for (int i = 0; i < 4; ++i) {
            ring[i][0] = cx + ((float) signSide[i] * side[0] + (float) signBinormal[i] * binormal[0]) * radius;
            ring[i][1] = cy + ((float) signSide[i] * side[1] + (float) signBinormal[i] * binormal[1]) * radius;
            ring[i][2] = cz + ((float) signSide[i] * side[2] + (float) signBinormal[i] * binormal[2]) * radius;
        }
        return ring;
    }

    /** Cara con sombreado por orientacion (dorado). */
    private static void quad(Prism.Sink sink, float[] a, float[] b, float[] c, float[] d, float nx, float ny, float nz, float[] color) {
        float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 1.0E-6f) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        float shade = ny > 0.5f ? 1.0f : (ny < -0.5f ? 0.6f : (Math.abs(nx) > Math.abs(nz) ? 0.82f : 0.72f));
        float r = color[0] * shade;
        float g = color[1] * shade;
        float bl = color[2] * shade;
        sink.vertex(a[0], a[1], a[2], r, g, bl, 1.0f, nx, ny, nz);
        sink.vertex(b[0], b[1], b[2], r, g, bl, 1.0f, nx, ny, nz);
        sink.vertex(c[0], c[1], c[2], r, g, bl, 1.0f, nx, ny, nz);
        sink.vertex(d[0], d[1], d[2], r, g, bl, 1.0f, nx, ny, nz);
    }

    /**
     * Cara plateada de un solo color: sin sombreado por orientacion ni degradado de raiz a punta,
     * para que el plateado quede enterizo y no aparezca con zonas mas oscuras.
     */
    private static void quadSolid(Prism.Sink sink, float[] a, float[] b, float[] c, float[] d, float nx, float ny, float nz, float[] color) {
        float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 1.0E-6f) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        float r = color[0];
        float g = color[1];
        float bl = color[2];
        sink.vertex(a[0], a[1], a[2], r, g, bl, 1.0f, nx, ny, nz);
        sink.vertex(b[0], b[1], b[2], r, g, bl, 1.0f, nx, ny, nz);
        sink.vertex(c[0], c[1], c[2], r, g, bl, 1.0f, nx, ny, nz);
        sink.vertex(d[0], d[1], d[2], r, g, bl, 1.0f, nx, ny, nz);
    }

    private static float[] cross(float ax, float ay, float az, float bx, float by, float bz) {
        return new float[]{ay * bz - az * by, az * bx - ax * bz, ax * by - ay * bx};
    }

    private static void normalize(float[] v) {
        float len = Mth.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (len > 1.0E-6f) {
            v[0] = v[0] / len;
            v[1] = v[1] / len;
            v[2] = v[2] / len;
        }
    }

    /** Caja orientada a lo largo de (dx,dy,dz); halfW es el grosor y halfL la mitad de longitud. */
    private static void cube(Prism.Sink sink, float cx, float cy, float cz, float dx, float dy, float dz,
                             float halfW, float halfL, float[] color) {
        float dl = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (dl < 1.0E-5f) {
            dx = 0.0f;
            dy = 1.0f;
            dz = 0.0f;
            dl = 1.0f;
        }
        dx /= dl;
        dy /= dl;
        dz /= dl;
        float ux = 1.0f - dx * dx;
        float uy = -dy * dx;
        float uz = -dz * dx;
        float ul = Mth.sqrt(ux * ux + uy * uy + uz * uz);
        if (ul < 0.2f) {
            ux = -dx * dz;
            uy = -dy * dz;
            uz = 1.0f - dz * dz;
            ul = Mth.sqrt(ux * ux + uy * uy + uz * uz);
        }
        ux /= ul;
        uy /= ul;
        uz /= ul;
        float vx = dy * uz - dz * uy;
        float vy = dz * ux - dx * uz;
        float vz = dx * uy - dy * ux;
        float[][] axes = new float[][]{
                {dx * halfL, dy * halfL, dz * halfL},
                {ux * halfW, uy * halfW, uz * halfW},
                {vx * halfW, vy * halfW, vz * halfW}
        };
        for (int i = 0; i < 3; ++i) {
            float[] axis = axes[i];
            float[] s1 = axes[(i + 1) % 3];
            float[] s2 = axes[(i + 2) % 3];
            for (int sign = -1; sign <= 1; sign += 2) {
                float fx = cx + (float) sign * axis[0];
                float fy = cy + (float) sign * axis[1];
                float fz = cz + (float) sign * axis[2];
                float al = Mth.sqrt(axis[0] * axis[0] + axis[1] * axis[1] + axis[2] * axis[2]);
                float nx = (float) sign * axis[0] / al;
                float ny = (float) sign * axis[1] / al;
                float nz = (float) sign * axis[2] / al;
                float shade = ny > 0.5f ? 1.0f : (ny < -0.5f ? 0.6f : (Math.abs(nx) > Math.abs(nz) ? 0.82f : 0.72f));
                float r = color[0] * shade;
                float g = color[1] * shade;
                float b = color[2] * shade;
                sink.vertex(fx - s1[0] - s2[0], fy - s1[1] - s2[1], fz - s1[2] - s2[2], r, g, b, 1.0f, nx, ny, nz);
                sink.vertex(fx + s1[0] - s2[0], fy + s1[1] - s2[1], fz + s1[2] - s2[2], r, g, b, 1.0f, nx, ny, nz);
                sink.vertex(fx + s1[0] + s2[0], fy + s1[1] + s2[1], fz + s1[2] + s2[2], r, g, b, 1.0f, nx, ny, nz);
                sink.vertex(fx - s1[0] + s2[0], fy - s1[1] + s2[1], fz - s1[2] + s2[2], r, g, b, 1.0f, nx, ny, nz);
            }
        }
    }

    static {
        GOLD = new float[]{1.0f, 0.88f, 0.3f};
        GOLD_D = new float[]{0.93f, 0.7f, 0.16f};
        SILVER = new float[]{0.98f, 0.98f, 1.0f};
        SILVER_D = new float[]{0.98f, 0.98f, 1.0f};
        TAIL = new float[]{0.96f, 0.84f, 0.4f};
        LOCKS = new ArrayList<>();
        add(false, GOLD_D, 0.0f, 0.47f, 0.01f, 0.0f, 1.0f, 0.0f, 0.12f, 0.56f, 1, 0.0f, 0.0f, 0.0f);
        add(false, GOLD, 0.0f, 0.5f, -0.1f, 0.0f, 1.0f, -0.35f, 0.7f, 0.26f, 4, 0.0f, 0.0f, 0.25f);
        add(false, GOLD, -0.16f, 0.48f, -0.08f, -0.45f, 1.0f, -0.25f, 0.55f, 0.22f, 4, 0.0f, 0.0f, 0.2f);
        add(false, GOLD, 0.16f, 0.48f, -0.08f, 0.45f, 1.0f, -0.25f, 0.55f, 0.22f, 4, 0.0f, 0.0f, 0.2f);
        add(false, GOLD_D, -0.1f, 0.5f, 0.08f, -0.25f, 1.0f, 0.25f, 0.62f, 0.22f, 4, 0.0f, 0.0f, 0.2f);
        add(false, GOLD_D, 0.1f, 0.5f, 0.08f, 0.25f, 1.0f, 0.25f, 0.62f, 0.22f, 4, 0.0f, 0.0f, 0.2f);
        add(false, GOLD, -0.24f, 0.42f, 0.0f, -1.0f, 0.6f, 0.0f, 0.38f, 0.18f, 3, 0.0f, 0.0f, 0.0f);
        add(false, GOLD, 0.24f, 0.42f, 0.0f, 1.0f, 0.6f, 0.0f, 0.38f, 0.18f, 3, 0.0f, 0.0f, 0.0f);
        add(false, GOLD, 0.0f, 0.44f, 0.18f, 0.0f, 0.6f, 1.0f, 0.6f, 0.24f, 4, 0.0f, -0.2f, 0.0f);
        add(false, GOLD, -0.12f, 0.47f, -0.26f, -0.1f, -1.0f, -0.4f, 0.1f, 0.1f, 2, 0.0f, 0.0f, 0.0f);
        add(false, GOLD, 0.05f, 0.47f, -0.26f, 0.1f, -1.0f, -0.4f, 0.12f, 0.1f, 2, 0.0f, 0.0f, 0.0f);
        add(false, GOLD, 0.06f, 0.5f, -0.24f, 0.3f, 0.8f, -0.3f, 0.75f, 0.06f, 22, 0.6f, -1.6f, -0.1f);
        for (int n = -3; n <= 3; ++n) {
            float f = (float) n / 3.0f;
            add(true, SILVER, 0.08f * (float) n, 0.4f, 0.2f, f * 0.35f, -0.2f, 0.55f, 1.3f - 0.12f * (float) Math.abs(n), 0.26f, 10, f * 0.1f, -1.0f, -0.25f);
        }
        for (int n = -2; n <= 2; ++n) {
            float f = (float) n / 2.0f;
            add(true, SILVER_D, 0.1f * (float) n, 0.22f, 0.22f, f * 0.3f, -0.7f, 0.5f, 1.45f - 0.12f * (float) Math.abs(n), 0.26f, 10, f * 0.1f, -0.7f, -0.25f);
        }
        add(true, SILVER, -0.26f, 0.3f, 0.05f, -0.3f, -1.0f, 0.3f, 0.55f, 0.14f, 4, 0.0f, 0.0f, 0.1f);
        add(true, SILVER, 0.26f, 0.3f, 0.05f, 0.3f, -1.0f, 0.3f, 0.55f, 0.14f, 4, 0.0f, 0.0f, 0.1f);
        for (int n = -3; n <= 2; ++n) {
            float f = (float) n + 0.5f;
            float f2 = f / 3.0f;
            add(true, SILVER, 0.08f * f, 0.36f, 0.2f, f2 * 0.35f, -0.2f, 0.55f, 1.2f - 0.12f * Math.abs(f), 0.22f, 10, f2 * 0.1f, -1.0f, -0.25f);
        }
    }

    static final class Lock {
        float rx;
        float ry;
        float rz;
        float dx;
        float dy;
        float dz;
        float len;
        float size;
        float bx;
        float by;
        float bz;
        float phase;
        int cubes;
        float[] color;
        boolean mane;
    }
}
