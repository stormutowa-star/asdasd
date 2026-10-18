package com.keinloree.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Construcciones por bloques (la nave, la fortaleza y las bases). Se ponen en cola y se colocan
 * un poco en cada tick del servidor, para no congelar el juego.
 */
public final class Builds {

    public record Placement(BlockPos pos, BlockState state) {
    }

    /** Bloques colocados por tick entre todos los trabajos. */
    private static final int BUDGET = 1500;
    private static final List<Job> JOBS = new ArrayList<>();

    private static final class Job {
        final ServerLevel level;
        final ArrayDeque<Placement> todo;
        final Runnable done;

        Job(ServerLevel level, List<Placement> list, Runnable done) {
            this.level = level;
            this.todo = new ArrayDeque<>(list);
            this.done = done;
        }
    }

    private Builds() {
    }

    public static void enqueue(ServerLevel level, List<Placement> list, Runnable done) {
        JOBS.add(new Job(level, list, done));
    }

    public static void tick() {
        if (JOBS.isEmpty()) {
            return;
        }
        int budget = BUDGET;
        List<Runnable> finished = new ArrayList<>();
        Iterator<Job> it = JOBS.iterator();
        while (it.hasNext() && budget > 0) {
            Job job = it.next();
            while (budget > 0 && !job.todo.isEmpty()) {
                Placement p = job.todo.poll();
                job.level.setBlock(p.pos(), p.state(), 3);
                budget--;
            }
            if (job.todo.isEmpty()) {
                it.remove();
                finished.add(job.done);
            }
        }
        for (Runnable r : finished) {
            r.run();
        }
    }

    /** La nave: casco circular de 28 bloques de radio, torre central y sala del trono. c = base del casco. */
    public static List<Placement> ship(BlockPos c) {
        List<Placement> out = new ArrayList<>();
        BlockState hull = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        BlockState deck = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState glow = Blocks.GREEN_CONCRETE.defaultBlockState();
        BlockState lamp = Blocks.SEA_LANTERN.defaultBlockState();
        final int r = 28;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > r * r) {
                    continue;
                }
                boolean rim = d2 >= (r - 1) * (r - 1);
                BlockPos col = c.offset(dx, 0, dz);
                for (int y = 0; y <= 2; y++) {
                    out.add(new Placement(col.above(y), rim ? glow : (y == 1 ? deck : hull)));
                }
                if (rim && Math.floorMod(dx + dz, 6) == 0) {
                    out.add(new Placement(col.above(3), lamp));
                }
            }
        }
        final int t = 8;
        for (int dx = -t; dx <= t; dx++) {
            for (int dz = -t; dz <= t; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > t * t) {
                    continue;
                }
                BlockPos col = c.offset(dx, 0, dz);
                if (d2 >= 36) {
                    for (int y = 3; y <= 14; y++) {
                        boolean door = dz > 5 && Math.abs(dx) <= 1 && y <= 6;
                        if (!door) {
                            out.add(new Placement(col.above(y), hull));
                        }
                    }
                }
                out.add(new Placement(col.above(15), hull));
            }
        }
        out.add(new Placement(c.above(3), Blocks.CHISELED_DEEPSLATE.defaultBlockState()));
        out.add(new Placement(c.offset(2, 3, 0), glow));
        out.add(new Placement(c.offset(-2, 3, 0), glow));
        out.add(new Placement(c.offset(0, 3, 2), glow));
        out.add(new Placement(c.offset(0, 3, -2), glow));
        out.add(new Placement(c.offset(5, 15, 0), lamp));
        out.add(new Placement(c.offset(-5, 15, 0), lamp));
        return out;
    }

    /**
     * Fortaleza o base militar: despeja el terreno, pone piso, muros con almenas, puerta al sur
     * y un nucleo verde con luz. g = primer bloque libre del suelo; r = radio; h = altura de muros.
     */
    public static List<Placement> fortress(BlockPos g, int r, int h) {
        List<Placement> out = new ArrayList<>();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState floor = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        BlockState wall = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState glow = Blocks.GREEN_CONCRETE.defaultBlockState();
        BlockState lamp = Blocks.SEA_LANTERN.defaultBlockState();
        int r2 = r * r;
        int inner = (r - 1) * (r - 1);

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > r2) {
                    continue;
                }
                for (int y = 0; y <= h + 3; y++) {
                    out.add(new Placement(g.offset(dx, y, dz), air));
                }
                out.add(new Placement(g.offset(dx, -1, dz), floor));
            }
        }
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > r2 || d2 < inner) {
                    continue;
                }
                for (int y = 0; y < h; y++) {
                    boolean gate = dz > r - 3 && Math.abs(dx) <= 1 && y <= 2;
                    if (!gate) {
                        out.add(new Placement(g.offset(dx, y, dz), wall));
                    }
                }
                if (Math.floorMod(dx + dz, 2) == 0) {
                    out.add(new Placement(g.offset(dx, h, dz), glow));
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int y = 0; y < h + 3; y++) {
                    out.add(new Placement(g.offset(dx, y, dz), glow));
                }
            }
        }
        out.add(new Placement(g.offset(0, h + 3, 0), lamp));
        return out;
    }
}
