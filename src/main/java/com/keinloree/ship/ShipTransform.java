package com.keinloree.ship;

import com.keinloree.world.Palette;
import com.keinloree.world.design.Grid;
import com.keinloree.world.design.Mat;
import com.keinloree.world.design.ShipDesign;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Matematica comun (servidor y cliente) entre celdas locales del diseno del destructor y el mundo.
 * Punto de anclaje = celda del timon (ShipDesign.HELM). La entidad esta en el centro inferior de esa celda.
 * Rotacion anclada r: 0=NONE, 1=CLOCKWISE_90, 2=CLOCKWISE_180, 3=COUNTERCLOCKWISE_90; yaw(r) = 180 + 90r.
 * Continuo: mundo = entityPos + rotY(180 - yaw) * L, con L relativo al centro inferior de la celda del timon.
 */
public final class ShipTransform {
    public static final int HX = ShipDesign.HELM_X;
    public static final int HY = ShipDesign.HELM_Y;
    public static final int HZ = ShipDesign.HELM_Z;

    /** Limites (inclusivos) de las celdas solidas del diseno: minX, minY, minZ, maxX, maxY, maxZ. */
    private static final int[] BOUNDS = computeBounds();

    /** Altura minima/maxima del timon para que la nave quede entre Y=200 y Y=318. */
    public static final int MIN_ANCHOR_Y = 200 - (BOUNDS[1] - HY);
    public static final int MAX_ANCHOR_Y = 318 - (BOUNDS[4] - HY);
    /** Orbita por defecto: base de la nave en Y=222. */
    public static final int DEFAULT_ANCHOR_Y = Mth.clamp(222 - (BOUNDS[1] - HY), MIN_ANCHOR_Y, MAX_ANCHOR_Y);

    private static final Rotation[] ROTATIONS = {Rotation.NONE, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180,
            Rotation.COUNTERCLOCKWISE_90};
    private static BlockState[][] states;

    private ShipTransform() {
    }

    private static int[] computeBounds() {
        Grid g = ShipDesign.grid();
        int[] b = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        for (int x = g.x0; x <= g.maxX(); x++) {
            for (int y = g.y0; y <= g.maxY(); y++) {
                for (int z = g.z0; z <= g.maxZ(); z++) {
                    byte m = g.get(x, y, z);
                    if (m != Mat.NONE && m != Mat.AIR) {
                        b[0] = Math.min(b[0], x);
                        b[1] = Math.min(b[1], y);
                        b[2] = Math.min(b[2], z);
                        b[3] = Math.max(b[3], x);
                        b[4] = Math.max(b[4], y);
                        b[5] = Math.max(b[5], z);
                    }
                }
            }
        }
        return b;
    }

    public static int[] solidBounds() {
        return BOUNDS.clone();
    }

    // ------------------------------------------------------------------ rotacion entera

    public static Rotation rotationFor(int r) {
        return ROTATIONS[Math.floorMod(r, 4)];
    }

    /** Yaw de la entidad para la rotacion r (convencion MC: 0 sur, 90 oeste, 180 norte, 270 este). */
    public static float yawFor(int r) {
        return Mth.wrapDegrees(180.0F + 90.0F * Math.floorMod(r, 4));
    }

    /** Rotacion entera mas cercana a un yaw. */
    public static int nearestR(float yaw) {
        return Math.floorMod(Math.round((yaw - 180.0F) / 90.0F), 4);
    }

    public static int rotX(int r, int dx, int dz) {
        return switch (Math.floorMod(r, 4)) {
            case 1 -> -dz;
            case 2 -> -dx;
            case 3 -> dz;
            default -> dx;
        };
    }

    public static int rotZ(int r, int dx, int dz) {
        return switch (Math.floorMod(r, 4)) {
            case 1 -> dx;
            case 2 -> -dz;
            case 3 -> -dx;
            default -> dz;
        };
    }

    /** Bloque del mundo de la celda local (x,y,z) con la nave anclada en anchor/r. */
    public static BlockPos blockFor(BlockPos anchor, int r, int x, int y, int z) {
        int dx = x - HX;
        int dz = z - HZ;
        return new BlockPos(anchor.getX() + rotX(r, dx, dz), anchor.getY() + y - HY, anchor.getZ() + rotZ(r, dx, dz));
    }

    /** Version sin reservar memoria para los bucles de bloques. */
    public static BlockPos.MutableBlockPos blockFor(BlockPos.MutableBlockPos out, BlockPos anchor, int r, int x, int y, int z) {
        int dx = x - HX;
        int dz = z - HZ;
        return out.set(anchor.getX() + rotX(r, dx, dz), anchor.getY() + y - HY, anchor.getZ() + rotZ(r, dx, dz));
    }

    /** Estado del material ya rotado (null para Mat.NONE). */
    public static BlockState stateFor(byte mat, int r) {
        BlockState[][] t = states;
        if (t == null) {
            t = new BlockState[4][Mat.COUNT];
            for (int rr = 0; rr < 4; rr++) {
                for (byte m = 0; m < Mat.COUNT; m++) {
                    BlockState s = Palette.of(m);
                    t[rr][m] = s == null ? null : s.rotate(rotationFor(rr));
                }
            }
            states = t;
        }
        return mat >= 0 && mat < Mat.COUNT ? t[Math.floorMod(r, 4)][mat] : null;
    }

    /** Posicion de la entidad cuando esta anclada en anchor. */
    public static Vec3 entityPos(BlockPos anchor) {
        return new Vec3(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D);
    }

    /** Ancla (celda del timon) mas cercana a una posicion continua de la entidad. */
    public static BlockPos anchorAt(Vec3 entityPos) {
        return BlockPos.containing(entityPos.x, Math.round(entityPos.y), entityPos.z);
    }

    // ------------------------------------------------------------------ continuo

    /** Punto local (relativo al centro inferior de la celda del timon) de la celda (x,y,z) + (u,v,w) dentro de ella. */
    public static Vec3 local(int x, int y, int z, double u, double v, double w) {
        return new Vec3(x - HX - 0.5D + u, y - HY + v, z - HZ - 0.5D + w);
    }

    /** Centro inferior de una celda en coordenadas locales (donde se pisa). */
    public static Vec3 cellFloor(int[] cell) {
        return local(cell[0], cell[1], cell[2], 0.5D, 0.0D, 0.5D);
    }

    public static Vec3 toWorld(Vec3 entityPos, float yaw, Vec3 local) {
        double a = Math.toRadians(180.0D - yaw);
        double c = Math.cos(a);
        double s = Math.sin(a);
        return new Vec3(entityPos.x + local.x * c + local.z * s, entityPos.y + local.y, entityPos.z - local.x * s + local.z * c);
    }

    public static Vec3 toLocal(Vec3 entityPos, float yaw, Vec3 world) {
        double a = Math.toRadians(180.0D - yaw);
        double c = Math.cos(a);
        double s = Math.sin(a);
        double x = world.x - entityPos.x;
        double z = world.z - entityPos.z;
        return new Vec3(x * c - z * s, world.y - entityPos.y, x * s + z * c);
    }

    // ------------------------------------------------------------------ cajas

    /** Caja (en bloques, maximo exclusivo) de todas las celdas solidas con la nave anclada. */
    public static AABB worldAABB(BlockPos anchor, int r) {
        int ax = rotX(r, BOUNDS[0] - HX, BOUNDS[2] - HZ);
        int az = rotZ(r, BOUNDS[0] - HX, BOUNDS[2] - HZ);
        int bx = rotX(r, BOUNDS[3] - HX, BOUNDS[5] - HZ);
        int bz = rotZ(r, BOUNDS[3] - HX, BOUNDS[5] - HZ);
        return new AABB(anchor.getX() + Math.min(ax, bx), anchor.getY() + BOUNDS[1] - HY, anchor.getZ() + Math.min(az, bz),
                anchor.getX() + Math.max(ax, bx) + 1, anchor.getY() + BOUNDS[4] - HY + 1, anchor.getZ() + Math.max(az, bz) + 1);
    }

    /** Caja que contiene el diseno girado un yaw arbitrario (vuelo, render). */
    public static AABB worldAABB(Vec3 entityPos, float yaw) {
        Vec3 lo = local(BOUNDS[0], BOUNDS[1], BOUNDS[2], 0.0D, 0.0D, 0.0D);
        Vec3 hi = local(BOUNDS[3], BOUNDS[4], BOUNDS[5], 1.0D, 1.0D, 1.0D);
        double minX = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            Vec3 c = toWorld(entityPos, yaw, new Vec3((i & 1) == 0 ? lo.x : hi.x, 0.0D, (i & 2) == 0 ? lo.z : hi.z));
            minX = Math.min(minX, c.x);
            maxX = Math.max(maxX, c.x);
            minZ = Math.min(minZ, c.z);
            maxZ = Math.max(maxZ, c.z);
        }
        return new AABB(minX, entityPos.y + lo.y, minZ, maxX, entityPos.y + hi.y, maxZ);
    }
}
