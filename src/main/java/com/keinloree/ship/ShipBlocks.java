package com.keinloree.ship;

import com.keinloree.world.design.Grid;
import com.keinloree.world.design.Mat;
import com.keinloree.world.design.ShipDesign;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Trabajos rapidos e idempotentes para poner o quitar los bloques del destructor (con rotacion).
 * Colocar = bloque del diseno (las celdas AIR solo despejan si hay algo). Borrar = aire en las celdas solidas.
 */
public final class ShipBlocks {
    /** Bloques escritos por tick (configurable). */
    public static int perTick = 4000;
    /** Chunks que se pueden cargar por tick. */
    public static int chunkLoadsPerTick = 8;
    private static final int VISITS_PER_TICK = 40000;
    public static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;

    // Celdas no vacias del diseno, ordenadas de proa a popa y de abajo arriba
    private static int[] cellX;
    private static int[] cellY;
    private static int[] cellZ;
    private static byte[] cellMat;
    private static int solidCount;

    private ShipBlocks() {
    }

    private static synchronized void ensureCells() {
        if (cellX != null) {
            return;
        }
        Grid g = ShipDesign.grid();
        int n = g.count();
        int[] xs = new int[n];
        int[] ys = new int[n];
        int[] zs = new int[n];
        byte[] ms = new byte[n];
        int i = 0;
        int solid = 0;
        for (int z = g.z0; z <= g.maxZ(); z++) {
            for (int y = g.y0; y <= g.maxY(); y++) {
                for (int x = g.x0; x <= g.maxX(); x++) {
                    byte m = g.get(x, y, z);
                    if (m != Mat.NONE) {
                        xs[i] = x;
                        ys[i] = y;
                        zs[i] = z;
                        ms[i] = m;
                        i++;
                        if (m != Mat.AIR) {
                            solid++;
                        }
                    }
                }
            }
        }
        cellX = xs;
        cellY = ys;
        cellZ = zs;
        cellMat = ms;
        solidCount = solid;
    }

    /** Numero de celdas no vacias (solidas + aire) del diseno. */
    public static int cellCount() {
        ensureCells();
        return cellMat.length;
    }

    public static int solidCount() {
        ensureCells();
        return solidCount;
    }

    /** Un trabajo de colocar o borrar. El cursor se guarda en la entidad para reanudar. */
    public static final class Job {
        public final boolean place;
        public final BlockPos anchor;
        public final int r;
        /** Recorre de popa a proa (destruccion). */
        public final boolean reverse;
        private int cursor;
        /** Fraccion maxima que se permite procesar (oleadas). */
        private float limit = 1.0F;
        private int written;

        public Job(boolean place, BlockPos anchor, int r, boolean reverse, int cursor) {
            ensureCells();
            this.place = place;
            this.anchor = anchor.immutable();
            this.r = r;
            this.reverse = reverse;
            this.cursor = Math.max(0, cursor);
        }

        public int cursor() {
            return cursor;
        }

        public boolean done() {
            return cursor >= cellMat.length;
        }

        public float progress() {
            return cellMat.length == 0 ? 1.0F : Math.min(1.0F, cursor / (float) cellMat.length);
        }

        public void setLimit(float limit) {
            this.limit = limit;
        }

        /** Bloques cambiados en total (para estadisticas). */
        public int written() {
            return written;
        }

        /** Avanza con el presupuesto por defecto. Devuelve true al terminar. */
        public boolean step(ServerLevel level) {
            return step(level, perTick);
        }

        public boolean step(ServerLevel level, int budget) {
            int total = cellMat.length;
            int end = Math.min(total, (int) Math.ceil(limit * total));
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            int writes = 0;
            int visits = 0;
            int loads = 0;
            int min = level.getMinBuildHeight();
            int max = level.getMaxBuildHeight();
            BlockState air = Blocks.AIR.defaultBlockState();
            while (cursor < end && writes < budget && visits < VISITS_PER_TICK) {
                int i = reverse ? total - 1 - cursor : cursor;
                byte m = cellMat[i];
                visits++;
                if (!place && m == Mat.AIR) {
                    cursor++;
                    continue;
                }
                ShipTransform.blockFor(pos, anchor, r, cellX[i], cellY[i], cellZ[i]);
                if (pos.getY() < min || pos.getY() >= max) {
                    cursor++;
                    continue;
                }
                int cx = SectionPos.blockToSectionCoord(pos.getX());
                int cz = SectionPos.blockToSectionCoord(pos.getZ());
                if (!level.hasChunk(cx, cz)) {
                    if (loads >= chunkLoadsPerTick) {
                        break;
                    }
                    level.getChunk(cx, cz);
                    loads++;
                }
                BlockState cur = level.getBlockState(pos);
                BlockState want = place ? ShipTransform.stateFor(m, r) : air;
                if (want != null && cur != want && !(want.isAir() && cur.isAir())) {
                    level.setBlock(pos, want, FLAGS);
                    writes++;
                    written++;
                }
                cursor++;
            }
            return done();
        }

        /** Termina el trabajo en un solo paso (pruebas, comandos de limpieza). */
        public void finish(ServerLevel level) {
            this.limit = 1.0F;
            int guard = 0;
            while (!done() && guard++ < 100000) {
                int before = cursor;
                step(level, Integer.MAX_VALUE);
                if (cursor == before) {
                    // solo esperaba a cargar chunks: sigue
                    continue;
                }
            }
        }
    }
}
