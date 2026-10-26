package com.keinloree.client.ship;

import com.keinloree.world.Palette;
import com.keinloree.world.design.Grid;
import com.keinloree.world.design.Mat;
import com.keinloree.world.design.ShipDesign;
import com.keinloree.ship.ShipTransform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.slf4j.Logger;

/**
 * Malla estatica del destructor: el diseno (ShipDesign.grid() + Palette) tesselado como un chunk, una
 * VertexBuffer por RenderType de bloque. Se construye en un hilo de fondo (con luz de cielo y de bloque
 * horneada en la rejilla) y se sube en el hilo de render. Coordenadas: relativas a la entidad
 * (celda del timon: x-hx-0.5, y-hy, z-hz-0.5).
 */
public final class ShipMesh {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Resultado del hilo de fondo: una malla por tipo de render (aun sin subir). */
    private record Built(Map<RenderType, MeshData> meshes, List<ByteBufferBuilder> builders, int quads, long ms) {
    }

    /** Capa ya subida a la GPU. */
    private record Layer(RenderType type, VertexBuffer buffer) {
    }

    private static CompletableFuture<Built> future;
    private static final List<Layer> LAYERS = new ArrayList<>();
    private static boolean uploaded;
    private static int generation;

    private ShipMesh() {
    }

    /** true cuando la malla esta subida y lista para dibujar (la lanza si hace falta). */
    public static boolean ready() {
        if (uploaded) {
            return true;
        }
        if (future == null) {
            start();
            return false;
        }
        if (future.isDone()) {
            try {
                upload(future.join());
            } catch (RuntimeException e) {
                LOGGER.error("[ShipMesh] Fallo al construir la malla del destructor", e);
                future = null;
                uploaded = true; // no reintentar en bucle: queda vacia
            }
        }
        return uploaded;
    }

    /** Descarta la malla (recarga de recursos, salir del mundo): se reconstruye al volver a hacer falta. */
    public static void release() {
        generation++;
        CompletableFuture<Built> f = future;
        future = null;
        if (f != null) {
            // libera la memoria nativa aunque el hilo de fondo aun no haya terminado
            f.thenAccept(ShipMesh::close);
        }
        for (Layer l : LAYERS) {
            l.buffer.close();
        }
        LAYERS.clear();
        uploaded = false;
    }

    private static void start() {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        // la paleta se inicializa aqui (hilo de render): no es segura entre hilos
        BlockState[] states = new BlockState[Mat.COUNT];
        for (byte m = 0; m < Mat.COUNT; m++) {
            BlockState s = m == Mat.NONE || m == Mat.AIR ? null : Palette.of(m);
            states[m] = s == null || s.isAir() ? null : s;
        }
        Grid grid = ShipDesign.grid();
        int gen = generation;
        future = CompletableFuture.supplyAsync(() -> build(dispatcher, grid, states), Util.backgroundExecutor());
        LOGGER.info("[ShipMesh] Construyendo la malla del destructor en segundo plano (gen {})", gen);
    }

    // ------------------------------------------------------------------ construccion (hilo de fondo)

    private static Built build(BlockRenderDispatcher dispatcher, Grid g, BlockState[] states) {
        long t0 = System.currentTimeMillis();
        GridGetter getter = new GridGetter(g, states);
        Map<RenderType, BufferBuilder> builders = new LinkedHashMap<>();
        List<ByteBufferBuilder> bytes = new ArrayList<>();
        PoseStack ps = new PoseStack();
        RandomSource random = RandomSource.create();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = g.x0; x <= g.maxX(); x++) {
            for (int y = g.y0; y <= g.maxY(); y++) {
                for (int z = g.z0; z <= g.maxZ(); z++) {
                    byte m = g.get(x, y, z);
                    BlockState st = states[m];
                    if (st == null || st.getRenderShape() != RenderShape.MODEL || getter.enclosed(x, y, z)) {
                        continue;
                    }
                    pos.set(x, y, z);
                    random.setSeed(st.getSeed(pos));
                    ChunkRenderTypeSet types = dispatcher.getBlockModel(st).getRenderTypes(st, random, ModelData.EMPTY);
                    for (RenderType type : types) {
                        BufferBuilder bb = builders.get(type);
                        if (bb == null) {
                            ByteBufferBuilder bytesBuilder = new ByteBufferBuilder(1 << 20);
                            bytes.add(bytesBuilder);
                            bb = new BufferBuilder(bytesBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
                            builders.put(type, bb);
                        }
                        ps.pushPose();
                        ps.translate(x - ShipTransform.HX - 0.5D, y - ShipTransform.HY, z - ShipTransform.HZ - 0.5D);
                        dispatcher.renderBatched(st, pos, getter, ps, bb, true, random, ModelData.EMPTY, type);
                        ps.popPose();
                    }
                }
            }
        }
        // orden de dibujo: solido, recortados, translucido
        Map<RenderType, MeshData> meshes = new LinkedHashMap<>();
        int quads = 0;
        for (RenderType type : new RenderType[]{RenderType.solid(), RenderType.cutoutMipped(), RenderType.cutout(),
                RenderType.translucent(), RenderType.tripwire()}) {
            BufferBuilder bb = builders.remove(type);
            if (bb != null) {
                MeshData md = bb.build();
                if (md != null) {
                    quads += md.drawState().vertexCount() / 4;
                    meshes.put(type, md);
                }
            }
        }
        for (BufferBuilder rest : builders.values()) {
            MeshData md = rest.build();
            if (md != null) {
                md.close();
            }
        }
        return new Built(meshes, bytes, quads, System.currentTimeMillis() - t0);
    }

    // ------------------------------------------------------------------ subida (hilo de render)

    private static void upload(Built b) {
        for (Map.Entry<RenderType, MeshData> e : b.meshes.entrySet()) {
            VertexBuffer vb = new VertexBuffer(VertexBuffer.Usage.STATIC);
            vb.bind();
            vb.upload(e.getValue()); // cierra la MeshData
            VertexBuffer.unbind();
            LAYERS.add(new Layer(e.getKey(), vb));
        }
        for (ByteBufferBuilder bb : b.builders) {
            bb.close();
        }
        uploaded = true;
        future = null;
        LOGGER.info("[ShipMesh] Malla lista: {} caras en {} capas ({} ms de construccion)", b.quads, LAYERS.size(), b.ms);
    }

    private static void close(Built b) {
        for (MeshData md : b.meshes.values()) {
            md.close();
        }
        for (ByteBufferBuilder bb : b.builders) {
            bb.close();
        }
    }

    // ------------------------------------------------------------------ dibujo

    /**
     * Dibuja todas las capas con la matriz de modelo-vista ya compuesta (camara + transformacion de la nave).
     * Debe llamarse en el hilo de render.
     */
    public static void draw(Matrix4f modelView, Matrix4f projection, boolean polygonOffset) {
        if (LAYERS.isEmpty()) {
            return;
        }
        if (polygonOffset) {
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(2.0F, 4.0F); // la malla pierde contra los bloques reales
        }
        for (Layer l : LAYERS) {
            l.type.setupRenderState();
            ShaderInstance shader = RenderSystem.getShader();
            shader.setDefaultUniforms(VertexFormat.Mode.QUADS, modelView, projection, Minecraft.getInstance().getWindow());
            shader.apply();
            if (shader.CHUNK_OFFSET != null) {
                shader.CHUNK_OFFSET.set(0.0F, 0.0F, 0.0F);
                shader.CHUNK_OFFSET.upload();
            }
            l.buffer.bind();
            l.buffer.draw();
            VertexBuffer.unbind();
            shader.clear();
            l.type.clearRenderState();
        }
        if (polygonOffset) {
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
        }
    }

    // ------------------------------------------------------------------ rejilla con luz

    /** BlockAndTintGetter falso sobre la rejilla del diseno (coordenadas de celda = coordenadas de bloque). */
    private static final class GridGetter implements BlockAndTintGetter {
        private final Grid g;
        private final BlockState[] states;
        private final boolean[] opaqueFull = new boolean[Mat.COUNT];
        private final int[] lightBlock = new int[Mat.COUNT];
        private final int[] emission = new int[Mat.COUNT];
        private final byte[] sky;
        private final byte[] blk;

        GridGetter(Grid g, BlockState[] states) {
            this.g = g;
            this.states = states;
            for (int m = 0; m < Mat.COUNT; m++) {
                BlockState s = states[m];
                if (s != null) {
                    opaqueFull[m] = s.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                    lightBlock[m] = s.getLightBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                    emission[m] = s.getLightEmission();
                }
            }
            int n = g.nx * g.ny * g.nz;
            sky = new byte[n];
            blk = new byte[n];
            bakeLight();
        }

        private int idx(int x, int y, int z) {
            return ((x - g.x0) * g.ny + (y - g.y0)) * g.nz + (z - g.z0);
        }

        /** Celda totalmente rodeada de bloques opacos: no aporta ninguna cara. */
        boolean enclosed(int x, int y, int z) {
            return opaqueFull[g.get(x + 1, y, z)] && opaqueFull[g.get(x - 1, y, z)] && opaqueFull[g.get(x, y + 1, z)]
                    && opaqueFull[g.get(x, y - 1, z)] && opaqueFull[g.get(x, y, z + 1)] && opaqueFull[g.get(x, y, z - 1)];
        }

        /** Luz de cielo (columnas abiertas = 15 y propagacion) y de bloque (BFS desde emisores). */
        private void bakeLight() {
            IntArrayFIFOQueue q = new IntArrayFIFOQueue();
            // cielo: columnas abiertas por arriba
            for (int x = g.x0; x <= g.maxX(); x++) {
                for (int z = g.z0; z <= g.maxZ(); z++) {
                    int level = 15;
                    for (int y = g.maxY(); y >= g.y0; y--) {
                        int lb = lightBlock[g.get(x, y, z)];
                        level = Math.max(0, level - lb);
                        int i = idx(x, y, z);
                        sky[i] = (byte) level;
                        if (level > 1) {
                            q.enqueue(i);
                        }
                    }
                }
            }
            // el exterior de la rejilla es cielo abierto (15): celdas del borde reciben 15 - atenuacion
            for (int x = g.x0; x <= g.maxX(); x++) {
                for (int y = g.y0; y <= g.maxY(); y++) {
                    for (int z = g.z0; z <= g.maxZ(); z++) {
                        boolean edge = x == g.x0 || x == g.maxX() || y == g.y0 || z == g.z0 || z == g.maxZ();
                        if (edge) {
                            int i = idx(x, y, z);
                            int v = 15 - Math.max(1, lightBlock[g.get(x, y, z)]);
                            if (v > sky[i]) {
                                sky[i] = (byte) v;
                                q.enqueue(i);
                            }
                        }
                    }
                }
            }
            propagate(sky, q);
            // bloque: emisores
            for (int x = g.x0; x <= g.maxX(); x++) {
                for (int y = g.y0; y <= g.maxY(); y++) {
                    for (int z = g.z0; z <= g.maxZ(); z++) {
                        int e = emission[g.get(x, y, z)];
                        if (e > 0) {
                            int i = idx(x, y, z);
                            blk[i] = (byte) e;
                            q.enqueue(i);
                        }
                    }
                }
            }
            propagate(blk, q);
        }

        private void propagate(byte[] light, IntArrayFIFOQueue q) {
            int ny = g.ny;
            int nz = g.nz;
            while (!q.isEmpty()) {
                int i = q.dequeueInt();
                int l = light[i];
                if (l <= 1) {
                    continue;
                }
                int z = i % nz;
                int y = (i / nz) % ny;
                int x = i / (nz * ny);
                for (int d = 0; d < 6; d++) {
                    int nx = x + (d == 0 ? 1 : d == 1 ? -1 : 0);
                    int nyy = y + (d == 2 ? 1 : d == 3 ? -1 : 0);
                    int nzz = z + (d == 4 ? 1 : d == 5 ? -1 : 0);
                    if (nx < 0 || nyy < 0 || nzz < 0 || nx >= g.nx || nyy >= ny || nzz >= nz) {
                        continue;
                    }
                    int j = (nx * ny + nyy) * nz + nzz;
                    int nl = l - Math.max(1, lightBlock[g.get(nx + g.x0, nyy + g.y0, nzz + g.z0)]);
                    if (nl > light[j]) {
                        light[j] = (byte) nl;
                        q.enqueue(j);
                    }
                }
            }
        }

        @Override
        public float getShade(Direction dir, boolean shade) {
            if (!shade) {
                return 1.0F;
            }
            return switch (dir) {
                case DOWN -> 0.5F;
                case UP -> 1.0F;
                case NORTH, SOUTH -> 0.8F;
                case WEST, EAST -> 0.6F;
            };
        }

        @Override
        public LevelLightEngine getLightEngine() {
            // no se usa: getBrightness y getRawBrightness estan sobrescritos
            throw new UnsupportedOperationException("ShipMesh no tiene motor de luz");
        }

        @Override
        public int getBrightness(LightLayer layer, BlockPos p) {
            if (!g.in(p.getX(), p.getY(), p.getZ())) {
                return layer == LightLayer.SKY ? 15 : 0;
            }
            int i = idx(p.getX(), p.getY(), p.getZ());
            return layer == LightLayer.SKY ? sky[i] : blk[i];
        }

        @Override
        public int getRawBrightness(BlockPos p, int darken) {
            return Math.max(getBrightness(LightLayer.SKY, p) - darken, getBrightness(LightLayer.BLOCK, p));
        }

        @Override
        public int getBlockTint(BlockPos p, ColorResolver resolver) {
            return 0xFFFFFF;
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos p) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos p) {
            BlockState s = states[g.get(p.getX(), p.getY(), p.getZ())];
            return s == null ? Blocks.AIR.defaultBlockState() : s;
        }

        @Override
        public FluidState getFluidState(BlockPos p) {
            return Fluids.EMPTY.defaultFluidState();
        }

        @Override
        public int getHeight() {
            return g.ny;
        }

        @Override
        public int getMinBuildHeight() {
            return g.y0;
        }
    }
}
