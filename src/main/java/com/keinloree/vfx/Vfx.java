package com.keinloree.vfx;

import com.keinloree.net.Net;
import com.keinloree.net.VfxS2C;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * API de servidor del motor de VFX: cada llamada envia un {@link VfxS2C} a los jugadores cercanos (hasta 256
 * bloques) y el cliente lo dibuja. Colores en formato RGB 0xRRGGBB. Los efectos nunca rompen bloques.
 */
public final class Vfx {
    /** Alcance normal de los efectos. */
    public static final double RANGE = 256.0D;
    public static final int GREEN = 0x3CFF7A;
    public static final int WHITE = 0xFFFFFF;
    private static final Vec3 UP = new Vec3(0.0D, 1.0D, 0.0D);

    private Vfx() {
    }

    /** Envio generico a los jugadores cercanos al punto. */
    public static void send(ServerLevel level, VfxType type, Vec3 pos, Vec3 dir, int color, float scale, int ticks,
                            int entityId, float extra) {
        VfxS2C msg = new VfxS2C(type.id, pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, color, scale, ticks, entityId, extra);
        Net.toNear(level, pos, RANGE, msg);
    }

    /** Haz de energia (cilindro con nucleo blanco) de {@code from} a {@code to}. */
    public static void beam(ServerLevel level, Vec3 from, Vec3 to, int color, float width, int ticks) {
        send(level, VfxType.BEAM, from, to, color, width, ticks, -1, 0.0F);
    }

    /** Anillo con normal dada; {@code expand}=0 mantiene el radio, 1 crece desde el centro. */
    public static void ring(ServerLevel level, Vec3 center, Vec3 normal, int color, float radius, int ticks) {
        send(level, VfxType.RING, center, normal, color, radius, ticks, -1, 0.0F);
    }

    /** Anillo que crece (con normal dada). */
    public static void ringExpanding(ServerLevel level, Vec3 center, Vec3 normal, int color, float radius, int ticks) {
        send(level, VfxType.RING, center, normal, color, radius, ticks, -1, 1.0F);
    }

    /** Onda de choque horizontal que se expande y se adelgaza. */
    public static void shockwave(ServerLevel level, Vec3 center, int color, float radius, int ticks) {
        send(level, VfxType.SHOCKWAVE, center, UP, color, radius, ticks, -1, 0.0F);
    }

    /** Tajo en arco (angulos en grados: yaw/pitch de Minecraft y roll sobre el eje de avance). */
    public static void slash(ServerLevel level, Vec3 center, float yaw, float pitch, float roll, float radius,
                             int color, int ticks) {
        send(level, VfxType.SLASH, center, new Vec3(yaw, pitch, roll), color, radius, ticks, -1, 0.0F);
    }

    /** Esfera de energia pulsante. */
    public static void orb(ServerLevel level, Vec3 center, int color, float radius, int ticks) {
        send(level, VfxType.ORB, center, UP, color, radius, ticks, -1, 0.0F);
    }

    /** Esfera de energia que sigue a una entidad (a la altura del pecho). */
    public static void orbOn(ServerLevel level, Entity e, int color, float radius, int ticks) {
        send(level, VfxType.ORB, e.position().add(0.0D, e.getBbHeight() * 0.6D, 0.0D), UP, color, radius, ticks,
                e.getId(), 0.0F);
    }

    /** Columna de luz vertical desde {@code base}; height&lt;=0 sube hasta y=320. */
    public static void pillar(ServerLevel level, Vec3 base, float height, int color, int ticks) {
        send(level, VfxType.PILLAR, base, UP, color, 1.2F, ticks, -1, height);
    }

    /** Columna con radio propio. */
    public static void pillar(ServerLevel level, Vec3 base, float height, float radius, int color, int ticks) {
        send(level, VfxType.PILLAR, base, UP, color, radius, ticks, -1, height);
    }

    /** Circulo de runas girando, horizontal. */
    public static void runeCircle(ServerLevel level, Vec3 center, int color, float radius, int ticks) {
        send(level, VfxType.RUNE_CIRCLE, center, UP, color, radius, ticks, -1, 0.0F);
    }

    /** Rayo electrico quebrado (se regenera cada 2 ticks). */
    public static void lightning(ServerLevel level, Vec3 from, Vec3 to, int color, int ticks) {
        send(level, VfxType.LIGHTNING, from, to, color, 0.12F, ticks, -1, 0.0F);
    }

    /** Lluvia de glifos de codigo en un disco. */
    public static void codeRain(ServerLevel level, Vec3 center, float radius, int ticks) {
        send(level, VfxType.CODE_RAIN, center, UP, GREEN, radius, ticks, -1, 12.0F);
    }

    /** Chispas alargadas que salen disparadas y caen. */
    public static void burst(ServerLevel level, Vec3 pos, int color, int count, float speed) {
        send(level, VfxType.BURST, pos, UP, color, speed, 22, -1, count);
    }

    /** Esfera oscura distorsionante con borde luminoso. */
    public static void distortion(ServerLevel level, Vec3 center, int color, float radius, int ticks) {
        send(level, VfxType.DISTORTION, center, UP, color, radius, ticks, -1, 0.0F);
    }

    /** Burbuja de tiempo detenido (esfera gris azulada con reloj). */
    public static void timeFreeze(ServerLevel level, Vec3 center, float radius, int ticks) {
        send(level, VfxType.TIME_FREEZE, center, UP, 0x8FB4FF, radius, ticks, -1, 0.0F);
    }

    /** Salto al hiperespacio: estallido de estrellas, aro y estelas en la direccion {@code dir}. */
    public static void hyperspace(ServerLevel level, Vec3 pos, Vec3 dir, float scale) {
        send(level, VfxType.HYPERSPACE, pos, dir, 0xBFD8FF, scale, 50, -1, 0.0F);
    }

    /** Estela que sigue a una entidad durante {@code ticks} ticks. */
    public static void trail(ServerLevel level, Entity e, int color, int ticks) {
        send(level, VfxType.TRAIL, e.position(), UP, color, 0.8F, ticks, e.getId(), 0.0F);
    }

    /** Explosion visual (destello, aro, chispas y humo); NO rompe bloques. */
    public static void explosion(ServerLevel level, Vec3 pos, float scale) {
        send(level, VfxType.EXPLOSION, pos, UP, 0x7CFFAA, scale, 40, -1, 0.0F);
    }

    /** Destello cónico de disparo. */
    public static void muzzle(ServerLevel level, Vec3 pos, Vec3 dir, int color) {
        send(level, VfxType.MUZZLE, pos, dir, color, 1.0F, 5, -1, 0.0F);
    }
}
