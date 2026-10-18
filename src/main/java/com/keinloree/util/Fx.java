package com.keinloree.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Efectos visuales y de sonido del lado del servidor (las particulas llegan a todos los jugadores cercanos). */
public final class Fx {

    public static final DustParticleOptions GREEN = new DustParticleOptions(new Vector3f(0.2F, 1.0F, 0.45F), 1.2F);
    public static final DustParticleOptions DEEP_GREEN = new DustParticleOptions(new Vector3f(0.05F, 0.55F, 0.2F), 1.4F);
    public static final DustParticleOptions BRIGHT = new DustParticleOptions(new Vector3f(0.85F, 1.0F, 0.9F), 0.9F);
    /** Particulas que imitan caracteres de codigo cayendo. */
    public static final DustParticleOptions CODE = new DustParticleOptions(new Vector3f(0.4F, 1.0F, 0.6F), 0.7F);

    private Fx() {
    }

    public static void burst(ServerLevel level, ParticleOptions particle, Vec3 at, int count, double spread, double speed) {
        level.sendParticles(particle, at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    /** Puntos a lo largo de un segmento (una particula por punto). */
    public static void line(ServerLevel level, Vec3 a, Vec3 b, ParticleOptions particle, double step) {
        Vec3 d = b.subtract(a);
        double len = d.length();
        int n = Math.max(1, (int) Math.ceil(len / step));
        for (int i = 0; i <= n; i++) {
            Vec3 q = a.add(d.scale(i / (double) n));
            level.sendParticles(particle, q.x, q.y, q.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    /** Curva cuadratica de Bezier (los rayos que se doblan). */
    public static void curve(ServerLevel level, Vec3 a, Vec3 control, Vec3 b, ParticleOptions particle, int samples) {
        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;
            double u = 1.0D - t;
            Vec3 q = a.scale(u * u).add(control.scale(2.0D * u * t)).add(b.scale(t * t));
            level.sendParticles(particle, q.x, q.y, q.z, 2, 0.04D, 0.04D, 0.04D, 0.0D);
        }
    }

    public static void ring(ServerLevel level, Vec3 center, double radius, int points, ParticleOptions particle, double phase) {
        for (int i = 0; i < points; i++) {
            double a = phase + Math.PI * 2.0D * i / points;
            level.sendParticles(particle, center.x + Math.cos(a) * radius, center.y, center.z + Math.sin(a) * radius,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
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
