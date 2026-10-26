package com.keinloree.util;

import net.minecraft.core.BlockPos;
import com.keinloree.registry.ModParticles;
import com.keinloree.vfx.Vfx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/** Efectos visuales y de sonido del lado del servidor (las particulas llegan a todos los jugadores cercanos). */
public final class Fx {

    /** Estilo visual: color de los VFX y tipo de particula propia (0 ascua, 1 glifo, 2 chispa). */
    public record Tint(int color, int kind) {
        ParticleOptions particle() {
            return switch (kind) {
                case 1 -> ModParticles.GLYPH.get();
                case 2 -> ModParticles.SPARK.get();
                default -> ModParticles.EMBER.get();
            };
        }
    }

    public static final Tint GREEN = new Tint(0x3CFF7A, 0);
    public static final Tint DEEP_GREEN = new Tint(0x14B84C, 0);
    public static final Tint BRIGHT = new Tint(0xE4FFEC, 2);
    /** Glifos de codigo. */
    public static final Tint CODE = new Tint(0x66FF99, 1);

    private Fx() {
    }

    /** Chispas del estilo dado (particulas propias, con luz completa). */
    public static void burst(ServerLevel level, Tint tint, Vec3 at, int count, double spread, double speed) {
        level.sendParticles(tint.particle(), at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    /** Haz entre dos puntos (VFX BEAM de corta vida). */
    public static void line(ServerLevel level, Vec3 a, Vec3 b, Tint tint, double step) {
        Vfx.beam(level, a, b, tint.color(), 0.1F, 6);
    }

    /** Curva cuadratica de Bezier (los rayos que se doblan): cadena de haces con nucleo blanco. */
    public static void curve(ServerLevel level, Vec3 a, Vec3 control, Vec3 b, Tint tint, int samples) {
        int seg = Math.max(4, samples / 3);
        Vec3 prev = a;
        for (int i = 1; i <= seg; i++) {
            double t = i / (double) seg;
            double u = 1.0D - t;
            Vec3 q = a.scale(u * u).add(control.scale(2.0D * u * t)).add(b.scale(t * t));
            Vfx.beam(level, prev, q, tint.color(), 0.14F, 10);
            prev = q;
        }
    }

    /** Anillo horizontal (VFX RING); la fase ya no se usa. */
    public static void ring(ServerLevel level, Vec3 center, double radius, int points, Tint tint, double phase) {
        Vfx.ring(level, center, new Vec3(0.0D, 1.0D, 0.0D), tint.color(), (float) radius, 4);
    }

    /** Particulas visibles desde lejos (hasta 512 bloques): para la nave nodriza y las naves de carga. */
    public static void burstFar(ServerLevel level, Tint tint, Vec3 at, int count, double spread, double speed) {
        for (ServerPlayer p : level.players()) {
            level.sendParticles(p, tint.particle(), true, at.x, at.y, at.z, count, spread, spread, spread, speed);
        }
    }

    /** Haz visible desde lejos. */
    public static void lineFar(ServerLevel level, Vec3 a, Vec3 b, Tint tint, double step) {
        Vfx.beam(level, a, b, tint.color(), 0.5F, 8);
    }

    /** Anillo horizontal visible desde lejos. */
    public static void ringFar(ServerLevel level, Vec3 center, double radius, int points, Tint tint, double phase) {
        Vfx.ring(level, center, new Vec3(0.0D, 1.0D, 0.0D), tint.color(), (float) radius, 5);
    }

    public static void sound(ServerLevel level, Vec3 at, SoundEvent event, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, event, SoundSource.HOSTILE, volume, pitch);
    }

    /** Altura de pie sobre el terreno en (x, z). */
    public static double groundY(ServerLevel level, double x, double z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
    }

    /**
     * Destruye bloques en un cilindro alrededor de un punto (terreno, arboles, construcciones).
     * No toca bloques indestructibles. Devuelve cuantos bloques se quitaron.
     */
    public static int carve(ServerLevel level, Vec3 center, double radius, int below, int above) {
        int changed = 0;
        int r = (int) Math.ceil(radius);
        BlockPos origin = BlockPos.containing(center.x, center.y, center.z);
        double r2 = radius * radius;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > r2) {
                    continue;
                }
                for (int dy = -below; dy <= above; dy++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.is(BlockTags.DRAGON_IMMUNE) || state.getDestroySpeed(level, pos) < 0.0F) {
                        continue;
                    }
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    changed++;
                }
            }
        }
        return changed;
    }
}
