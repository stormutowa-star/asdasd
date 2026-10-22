package com.keinloree.world;

import com.keinloree.world.design.Grid;
import com.keinloree.world.design.Mat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Construye los disenos por bloques (la nave, las bases, la fortaleza). Cada trabajo recorre su
 * rejilla poco a poco en cada tick del servidor, asi que la construccion se ve "imprimirse" y el juego no se congela.
 */
public final class Builds {

    /** 2 = avisar a los clientes, 16 = sin reacciones de vecinos (rapido y las escaleras/escalas no se caen). */
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    /** Chunks que un trabajo puede cargar (o generar) por tick. */
    private static final int CHUNK_LOADS_PER_TICK = 2;
    /** Celdas que se revisan por tick como maximo (las vacias salen casi gratis). */
    private static final int SCAN_PER_TICK = 600_000;

    public enum Order {
        /** De la proa a la popa, como una linea de escaneo. */
        Z_SWEEP,
        /** De abajo hacia arriba, capa por capa. */
        Y_RISE
    }

    /** Aviso de avance (efectos). front = coordenada local del frente (z o y); fraction = 0..1. */
    public interface Progress {
        void tick(ServerLevel level, int front, double fraction);
    }

    public static final class Options {
        public Order order = Order.Z_SWEEP;
        public int perTick = 500;
        /** Rellena hacia abajo bajo lo que esta en y local <= 0 (cimientos hasta el terreno). */
        public boolean support;
        public int supportDepth = 24;
        public Progress progress;
        public Runnable done;
    }

    private static final List<Job> JOBS = new ArrayList<>();
    private static long nextId = 1L;

    private static final class Job {
        final long id;
        final ServerLevel level;
        final Grid grid;
        final int ox;
        final int oy;
        final int oz;
        final Options opt;
        final long total;
        long cursor;
        int lastCx = Integer.MIN_VALUE;
        int lastCz = Integer.MIN_VALUE;

        Job(long id, ServerLevel level, Grid grid, int ox, int oy, int oz, Options opt) {
            this.id = id;
            this.level = level;
            this.grid = grid;
            this.ox = ox;
            this.oy = oy;
            this.oz = oz;
            this.opt = opt;
            this.total = (long) grid.nx * grid.ny * grid.nz;
        }
    }

    private Builds() {
    }

    /**
     * Pone en cola la construccion de una rejilla. (ox, oy, oz) es la posicion del mundo de la celda local (0, 0, 0).
     * Devuelve un identificador para preguntar despues si sigue activa.
     */
    public static long start(ServerLevel level, Grid grid, int ox, int oy, int oz, Options opt) {
        long id = nextId++;
        JOBS.add(new Job(id, level, grid, ox, oy, oz, opt));
        return id;
    }

    public static boolean isActive(long id) {
        for (Job j : JOBS) {
            if (j.id == id) {
                return true;
            }
        }
        return false;
    }

    /** Avance 0..1 de un trabajo (1 si ya termino). */
    public static double progress(long id) {
        for (Job j : JOBS) {
            if (j.id == id) {
                return j.total == 0L ? 1.0D : j.cursor / (double) j.total;
            }
        }
        return 1.0D;
    }

    public static void tick() {
        if (JOBS.isEmpty()) {
            return;
        }
        List<Runnable> finished = new ArrayList<>();
        Iterator<Job> it = JOBS.iterator();
        while (it.hasNext()) {
            Job job = it.next();
            run(job);
            if (job.cursor >= job.total) {
                it.remove();
                if (job.opt.done != null) {
                    finished.add(job.opt.done);
                }
            }
        }
        for (Runnable r : finished) {
            r.run();
        }
    }

    private static void run(Job j) {
        Grid g = j.grid;
        int placed = 0;
        int scanned = 0;
        int loads = CHUNK_LOADS_PER_TICK;
        boolean zOuter = j.opt.order == Order.Z_SWEEP;
        int front = zOuter ? g.z0 : g.y0;
        while (j.cursor < j.total && placed < j.opt.perTick && scanned < SCAN_PER_TICK) {
            long c = j.cursor;
            int x;
            int y;
            int z;
            if (zOuter) {
                int zi = (int) (c / ((long) g.nx * g.ny));
                int rem = (int) (c % ((long) g.nx * g.ny));
                x = g.x0 + rem / g.ny;
                y = g.y0 + rem % g.ny;
                z = g.z0 + zi;
                front = z;
            } else {
                int yi = (int) (c / ((long) g.nx * g.nz));
                int rem = (int) (c % ((long) g.nx * g.nz));
                x = g.x0 + rem / g.nz;
                z = g.z0 + rem % g.nz;
                y = g.y0 + yi;
                front = y;
            }
            scanned++;
            byte m = g.get(x, y, z);
            if (m == Mat.NONE) {
                j.cursor++;
                continue;
            }
            int wx = j.ox + x;
            int wy = j.oy + y;
            int wz = j.oz + z;
            int cx = wx >> 4;
            int cz = wz >> 4;
            if (cx != j.lastCx || cz != j.lastCz) {
                if (!j.level.hasChunk(cx, cz)) {
                    if (loads <= 0) {
                        break;
                    }
                    loads--;
                    j.level.getChunk(cx, cz);
                }
                j.lastCx = cx;
                j.lastCz = cz;
            }
            if (wy < j.level.getMinBuildHeight() || wy >= j.level.getMaxBuildHeight()) {
                j.cursor++;
                continue;
            }
            BlockPos pos = new BlockPos(wx, wy, wz);
            BlockState state = Palette.of(m);
            if (m == Mat.AIR) {
                // El aire solo despeja lo que haya (terreno, arboles); no cuenta si ya estaba vacio.
                if (!j.level.getBlockState(pos).isAir()) {
                    j.level.setBlock(pos, state, FLAGS);
                    placed++;
                }
            } else {
                j.level.setBlock(pos, state, FLAGS);
                placed++;
                if (j.opt.support && y <= 0 && g.get(x, y - 1, z) == Mat.NONE) {
                    placed += support(j, wx, wy, wz);
                }
            }
            j.cursor++;
        }
        if (j.opt.progress != null) {
            double f = j.total == 0L ? 1.0D : Math.min(1.0D, j.cursor / (double) j.total);
            j.opt.progress.tick(j.level, front, f);
        }
    }

    /** Cimientos: rellena hacia abajo bajo una celda hasta encontrar terreno firme. Devuelve cuantos bloques puso. */
    private static int support(Job j, int wx, int wy, int wz) {
        BlockState fill = Palette.of(Mat.WALL2);
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(wx, wy - 1, wz);
        int depth = 0;
        while (depth < j.opt.supportDepth && p.getY() > j.level.getMinBuildHeight()) {
            if (!fillable(j.level.getBlockState(p))) {
                break;
            }
            j.level.setBlock(p, fill, FLAGS);
            p.move(0, -1, 0);
            depth++;
        }
        return depth;
    }

    private static boolean fillable(BlockState s) {
        return s.isAir() || s.canBeReplaced() || !s.getFluidState().isEmpty()
                || s.is(BlockTags.LEAVES) || s.is(BlockTags.LOGS);
    }
}
