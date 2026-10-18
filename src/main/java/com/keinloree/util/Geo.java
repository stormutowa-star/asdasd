package com.keinloree.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Geometria basica para rayos, ondas, campos y colisiones de los ataques. */
public final class Geo {

    private Geo() {
    }

    /** Distancia minima de un punto al segmento a-b. */
    public static double distToSegment(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double len2 = ab.lengthSqr();
        double t = len2 < 1.0E-9D ? 0.0D : Mth.clamp(p.subtract(a).dot(ab) / len2, 0.0D, 1.0D);
        return p.distanceTo(a.add(ab.scale(t)));
    }

    /** Distancia ignorando la altura. */
    public static double horizontalDist(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Vector horizontal perpendicular a v (para los lados de los ojos y de los rayos curvos). */
    public static Vec3 side(Vec3 v) {
        Vec3 s = new Vec3(-v.z, 0.0D, v.x);
        return s.lengthSqr() < 1.0E-9D ? new Vec3(1.0D, 0.0D, 0.0D) : s.normalize();
    }

    /** Caja centrada en c con semiejes rx, ry, rz. */
    public static AABB box(Vec3 c, double rx, double ry, double rz) {
        return new AABB(c.x - rx, c.y - ry, c.z - rz, c.x + rx, c.y + ry, c.z + rz);
    }

    /** Caja que envuelve el segmento a-b con un margen. */
    public static AABB span(Vec3 a, Vec3 b, double pad) {
        return new AABB(Math.min(a.x, b.x) - pad, Math.min(a.y, b.y) - pad, Math.min(a.z, b.z) - pad,
                Math.max(a.x, b.x) + pad, Math.max(a.y, b.y) + pad, Math.max(a.z, b.z) + pad);
    }
}
