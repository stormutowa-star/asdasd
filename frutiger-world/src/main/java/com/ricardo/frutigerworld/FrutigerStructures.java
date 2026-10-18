package com.ricardo.frutigerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Estructuras de Frutiger World (estética Frutiger Aero: cristal aqua, blanco brillante,
 * cian, césped y burbujas).
 *
 * Reglas para que nunca bloqueen el paso:
 *  - Cada estructura de suelo se levanta sobre una plataforma plana que queda exactamente
 *    un escalón por encima del terreno delante de la puerta (se sube de un salto).
 *  - La puerta siempre tiene 3 bloques de ancho y 3 de alto, despejada de hojas y hierba.
 *  - No se generan estructuras sobre agua, ni con más de 4 bloques de desnivel.
 *  - Nada flota: cada torre y cada burbuja están unidas a su base por bloques.
 */
public final class FrutigerStructures {

    private static final int SEA_LEVEL = 63;
    private static final int MAX_RELIEF = 4;
    private static final int NONE = Integer.MIN_VALUE;
    /** Bandera de actualización de bloques para el cliente (valor 2 en Minecraft). */
    private static final int UPDATE_FLAGS = 2;
    /** Índice de lado -> dirección hacia fuera: 0 norte (-Z), 1 este (+X), 2 sur (+Z), 3 oeste (-X). */
    private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState FOUNDATION = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
    private static final BlockState WHITE = Blocks.WHITE_CONCRETE.defaultBlockState();
    private static final BlockState QUARTZ = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState QUARTZ_BLOCK = Blocks.QUARTZ_BLOCK.defaultBlockState();
    private static final BlockState PILLAR = Blocks.QUARTZ_PILLAR.defaultBlockState();
    private static final BlockState LANTERN = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState AQUA_GLASS = Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
    private static final BlockState SHEEN_GLASS = Blocks.WHITE_STAINED_GLASS.defaultBlockState();
    private static final BlockState CYAN = Blocks.CYAN_CONCRETE.defaultBlockState();
    private static final BlockState TRUNK = Blocks.ACACIA_LOG.defaultBlockState();
    private static final BlockState FROND = Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

    private FrutigerStructures() {
    }

    /** Banco de cuarzo: el lado alto queda detrás y el asiento mira hacia el centro. */
    private static BlockState bench(Direction facing) {
        return Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing);
    }

    /** Vista de una estructura: coordenadas relativas al centro y a la altura de la plataforma. */
    private static final class Site {
        private final WorldGenLevel level;
        private final int cx;
        private final int cz;
        private final int floorY;

        private Site(WorldGenLevel level, int cx, int cz, int floorY) {
            this.level = level;
            this.cx = cx;
            this.cz = cz;
            this.floorY = floorY;
        }

        private void set(int dx, int dy, int dz, BlockState state) {
            level.setBlock(new BlockPos(cx + dx, floorY + dy, cz + dz), state, UPDATE_FLAGS);
        }

        private void air(int dx, int dy, int dz) {
            set(dx, dy, dz, AIR);
        }

        private void setIfAir(int dx, int dy, int dz, BlockState state) {
            BlockPos pos = new BlockPos(cx + dx, floorY + dy, cz + dz);
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, state, UPDATE_FLAGS);
            }
        }

        /** Caja de bloques con límites inclusivos. */
        private void box(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    for (int z = z1; z <= z2; z++) {
                        set(x, y, z, state);
                    }
                }
            }
        }

        /** Burbuja de cristal hueca de radio {@code rad} con una luz en el centro. */
        private void bubble(int ox, int oy, int oz, int rad, BlockState glass, BlockState core) {
            int inner = (rad - 1) * (rad - 1);
            int outer = rad * rad;
            for (int dx = -rad; dx <= rad; dx++) {
                for (int dy = -rad; dy <= rad; dy++) {
                    for (int dz = -rad; dz <= rad; dz++) {
                        int d2 = dx * dx + dy * dy + dz * dz;
                        if (d2 >= inner && d2 <= outer) {
                            set(ox + dx, oy + dy, oz + dz, glass);
                        }
                    }
                }
            }
            set(ox, oy, oz, core);
        }
    }

    /**
     * Prepara el terreno: plataforma plana a nivel del terreno de la puerta (o del centro si no hay puerta),
     * relleno bajo las columnas bajas, excavación de las altas y despeje de hojas, hierba y troncos.
     * Devuelve null si el sitio no es apto (agua, desnivel grande o puerta sin acceso), sin tocar nada.
     */
    private static Site prepare(WorldGenLevel level, BlockPos origin, int radius, int height, int doorSide) {
        int cx = origin.getX();
        int cz = origin.getZ();
        int n = 2 * radius + 1;
        int[] footprint = new int[n * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int g = ground(level, cx + i - radius, cz + j - radius);
                if (g == NONE) {
                    return null;
                }
                footprint[i * n + j] = g;
            }
        }

        int floorY = footprint[radius * n + radius];
        int[] approach = new int[3];
        if (doorSide >= 0) {
            int[] o = outward(doorSide, radius + 1, 0);
            floorY = ground(level, cx + o[0], cz + o[1]);
            if (floorY == NONE) {
                return null;
            }
            for (int k = -1; k <= 1; k++) {
                int[] p = outward(doorSide, radius + 1, k);
                int g = ground(level, cx + p[0], cz + p[1]);
                if (g == NONE || Math.abs(g - floorY) > MAX_RELIEF) {
                    return null;
                }
                approach[k + 1] = g;
            }
        }
        if (floorY <= SEA_LEVEL) {
            return null;
        }
        for (int g : footprint) {
            if (Math.abs(g - floorY) > MAX_RELIEF) {
                return null;
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                shapeColumn(level, cx + i - radius, cz + j - radius, footprint[i * n + j], floorY, height);
            }
        }
        if (doorSide >= 0) {
            for (int k = -1; k <= 1; k++) {
                int[] p = outward(doorSide, radius + 1, k);
                shapeColumn(level, cx + p[0], cz + p[1], approach[k + 1], floorY, height);
            }
        }
        return new Site(level, cx, cz, floorY);
    }

    private static void shapeColumn(WorldGenLevel level, int x, int z, int ground, int floorY, int height) {
        if (ground < floorY) {
            for (int y = ground; y < floorY; y++) {
                level.setBlock(new BlockPos(x, y, z), FOUNDATION, UPDATE_FLAGS);
            }
        } else {
            for (int y = floorY; y < ground; y++) {
                level.setBlock(new BlockPos(x, y, z), AIR, UPDATE_FLAGS);
            }
        }
        for (int y = floorY; y <= floorY + height; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir()) {
                level.setBlock(pos, AIR, UPDATE_FLAGS);
            }
        }
    }

    /** Altura del terreno sólido en una columna, o NONE si hay líquido encima. */
    private static int ground(WorldGenLevel level, int x, int z) {
        int ocean = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        return ocean == surface ? ocean : NONE;
    }

    /** Coordenadas {dx, dz} de un punto a {@code depth} bloques hacia el lado indicado. */
    private static int[] outward(int side, int depth, int lateral) {
        return switch (side) {
            case 0 -> new int[] {lateral, -depth};
            case 1 -> new int[] {depth, lateral};
            case 2 -> new int[] {lateral, depth};
            default -> new int[] {-depth, lateral};
        };
    }

    /**
     * Pabellón de burbuja: cúpula de cristal aqua con franjas blancas, estanque central,
     * faroles en las esquinas y una luz en lo alto.
     */
    public static boolean dome(WorldGenLevel level, BlockPos origin, RandomSource random) {
        final int r = 4;
        int side = random.nextInt(4);
        Site s = prepare(level, origin, r, 12, side);
        if (s == null) {
            return false;
        }

        // Suelo en damero con anillo cian en el borde, y muro blanco bajo la cúpula.
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 16) {
                    BlockState floor = d2 >= 13 ? CYAN : (((dx + dz) & 1) == 0 ? QUARTZ : WHITE);
                    s.set(dx, 0, dz, floor);
                }
                if (d2 >= 12 && d2 <= 17) {
                    s.set(dx, 1, dz, WHITE);
                    s.set(dx, 2, dz, QUARTZ);
                }
            }
        }

        // Cúpula de cristal con franjas blancas.
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = 0; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 >= 9 && d2 <= 16) {
                        s.set(dx, 3 + dy, dz, (dx == dz && dy >= 2) ? SHEEN_GLASS : AQUA_GLASS);
                    }
                }
            }
        }

        // Entrada de 3x3 despejada.
        for (int k = -1; k <= 1; k++) {
            for (int depth = 3; depth <= r; depth++) {
                int[] p = outward(side, depth, k);
                for (int dy = 1; dy <= 3; dy++) {
                    s.air(p[0], dy, p[1]);
                }
            }
        }

        // Estanque central de 3x3 con borde de cuarzo.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                s.set(dx, -1, dz, QUARTZ_BLOCK);
                s.set(dx, 0, dz, WATER);
            }
        }
        s.set(-2, 1, -2, LANTERN);
        s.set(2, 1, -2, LANTERN);
        s.set(-2, 1, 2, LANTERN);
        s.set(2, 1, 2, LANTERN);

        // Columnas exteriores en las esquinas, con farol encima.
        for (int sx : new int[] {-4, 4}) {
            for (int sz : new int[] {-4, 4}) {
                s.box(sx, 0, sz, sx, 2, sz, PILLAR);
                s.set(sx, 3, sz, LANTERN);
            }
        }

        // Luz en lo alto de la cúpula.
        s.set(0, 8, 0, LANTERN);
        return true;
    }

    /**
     * Plaza con fuente: damero con borde cian, estanque de cuarzo, surtidor con burbuja de cristal,
     * bancos y cuatro faroles.
     */
    public static boolean fountain(WorldGenLevel level, BlockPos origin, RandomSource random) {
        final int r = 4;
        Site s = prepare(level, origin, r, 8, -1);
        if (s == null) {
            return false;
        }

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                boolean border = Math.abs(dx) == r || Math.abs(dz) == r;
                s.set(dx, 0, dz, border ? CYAN : (((dx + dz) & 1) == 0 ? QUARTZ : WHITE));
            }
        }

        // Estanque de cuarzo 5x5 con agua en el centro 3x3.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean inner = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
                if (inner) {
                    s.set(dx, -1, dz, QUARTZ_BLOCK);
                    s.set(dx, 0, dz, WATER);
                } else {
                    s.set(dx, 0, dz, QUARTZ_BLOCK);
                }
            }
        }

        // Surtidor: columna central con una burbuja de cristal y luz dentro.
        s.box(0, 0, 0, 0, 1, 0, PILLAR);
        s.bubble(0, 3, 0, 2, AQUA_GLASS, LANTERN);

        // Bancos alrededor del estanque.
        s.set(3, 1, 0, bench(Direction.WEST));
        s.set(-3, 1, 0, bench(Direction.EAST));
        s.set(0, 1, 3, bench(Direction.NORTH));
        s.set(0, 1, -3, bench(Direction.SOUTH));

        // Cuatro faroles en las esquinas.
        for (int sx : new int[] {-r, r}) {
            for (int sz : new int[] {-r, r}) {
                s.box(sx, 1, sz, sx, 2, sz, PILLAR);
                s.set(sx, 3, sz, LANTERN);
            }
        }
        return true;
    }

    /**
     * Torre mirador de 7x7 y 14 bloques: muros blancos con ventanas aqua, escalera interior,
     * mirador con barandilla de cristal y un faro en lo alto.
     */
    public static boolean lookout(WorldGenLevel level, BlockPos origin, RandomSource random) {
        final int r = 3;
        final int h = 14;
        int side = random.nextInt(4);
        Site s = prepare(level, origin, r, h + 6, side);
        if (s == null) {
            return false;
        }

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                boolean ring = Math.abs(dx) == r || Math.abs(dz) == r;
                boolean corner = Math.abs(dx) == r && Math.abs(dz) == r;
                s.set(dx, 0, dz, QUARTZ);
                if (ring) {
                    for (int dy = 1; dy < h; dy++) {
                        boolean band = dy >= 3 && (dy - 3) % 4 < 2;
                        boolean window = band && !corner && ((dx + dz) & 1) == 0;
                        s.set(dx, dy, dz, window ? AQUA_GLASS : WHITE);
                    }
                }
                // Suelo del mirador (también sobre el interior).
                s.set(dx, h, dz, QUARTZ);
                if (ring) {
                    // Barandilla de cristal alrededor del mirador.
                    s.set(dx, h + 1, dz, AQUA_GLASS);
                }
            }
        }

        // Entrada de 1x2 en el muro del lado elegido.
        int[] door = outward(side, r, 0);
        s.air(door[0], 1, door[1]);
        s.air(door[0], 2, door[1]);

        // Escalera interior pegada al muro opuesto, con trampilla en el mirador.
        int back = (side + 2) % 4;
        int[] ladderPos = outward(back, r - 1, 0);
        BlockState ladder = Blocks.LADDER.defaultBlockState()
                .setValue(LadderBlock.FACING, SIDES[back].getOpposite());
        for (int dy = 1; dy < h; dy++) {
            s.set(ladderPos[0], dy, ladderPos[1], ladder);
        }
        s.air(ladderPos[0], h, ladderPos[1]);

        s.set(-2, 1, -2, LANTERN);
        s.set(2, 1, -2, LANTERN);
        s.set(-2, 1, 2, LANTERN);
        s.set(2, 1, 2, LANTERN);

        // Faro en el centro del mirador.
        s.box(0, h + 1, 0, 0, h + 3, 0, PILLAR);
        s.set(0, h + 4, 0, LANTERN);
        return true;
    }

    /**
     * Aerogenerador: torre blanca con bandas, góndola, cubo cian y tres aspas de 9 bloques,
     * sobre una plataforma de cuarzo con borde cian.
     */
    public static boolean turbine(WorldGenLevel level, BlockPos origin, RandomSource random) {
        boolean alongX = random.nextBoolean();
        final int r = 2;
        Site s = prepare(level, origin, r, 26, -1);
        if (s == null) {
            return false;
        }

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                boolean border = Math.abs(dx) == r || Math.abs(dz) == r;
                s.set(dx, 0, dz, border ? CYAN : QUARTZ);
            }
        }
        for (int dy = 1; dy <= 15; dy++) {
            s.set(0, dy, 0, dy % 4 == 0 ? WHITE : PILLAR);
        }

        // Góndola en la dirección del rotor y cubo cian al frente.
        int ax = alongX ? 1 : 0;
        int az = alongX ? 0 : 1;
        s.set(0, 16, 0, WHITE);
        s.set(ax, 16, az, WHITE);
        s.set(2 * ax, 16, 2 * az, WHITE);
        s.set(3 * ax, 16, 3 * az, CYAN);

        // Tres aspas de 9 bloques que parten del cubo. Cada paso avanza por caras (nunca en diagonal),
        // así ninguna aspa queda con bloques sueltos.
        double[][] arms = {{0.0, 1.0}, {-0.866, -0.5}, {0.866, -0.5}};
        for (double[] arm : arms) {
            int pu = 0;
            int pv = 0;
            for (int t = 1; t <= 9; t++) {
                int u = (int) Math.round(arm[0] * t);
                int v = (int) Math.round(arm[1] * t);
                BlockState blade = t <= 6 ? WHITE : QUARTZ;
                while (pu != u) {
                    pu += Integer.signum(u - pu);
                    bladeCell(s, alongX, pu, pv, blade);
                }
                while (pv != v) {
                    pv += Integer.signum(v - pv);
                    bladeCell(s, alongX, pu, pv, blade);
                }
            }
        }
        return true;
    }

    /** Un bloque de aspa: el plano del rotor es x=3 si la góndola va en X, o z=3 si va en Z. */
    private static void bladeCell(Site s, boolean alongX, int u, int v, BlockState state) {
        if (alongX) {
            s.setIfAir(3, 16 + v, u, state);
        } else {
            s.setIfAir(u, 16 + v, 3, state);
        }
    }

    /**
     * Torre de burbujas: pilar blanco anclado al suelo con una burbuja grande de cristal
     * con luz arriba y dos burbujas pequeñas unidas al pilar por brazos.
     */
    public static boolean bubbleCluster(WorldGenLevel level, BlockPos origin, RandomSource random) {
        Site s = prepare(level, origin, 2, 22, -1);
        if (s == null) {
            return false;
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean border = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                s.set(dx, 0, dz, border ? CYAN : QUARTZ_BLOCK);
            }
        }
        for (int dy = 1; dy <= 11; dy++) {
            s.set(0, dy, 0, dy % 4 == 0 ? WHITE : PILLAR);
        }

        // Brazos horizontales que sujetan las burbujas pequeñas.
        s.set(1, 11, 0, QUARTZ);
        s.set(-1, 11, 0, QUARTZ);

        // Burbuja grande sobre el pilar y dos burbujas laterales.
        s.bubble(0, 15, 0, 3, AQUA_GLASS, LANTERN);
        s.bubble(4, 11, 0, 2, AQUA_GLASS, LANTERN);
        s.bubble(-4, 11, 0, 2, AQUA_GLASS, LANTERN);
        return true;
    }

    /** Grupo de hasta tres palmeras tropicales con copa persistente. */
    public static boolean palmGrove(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int[][] spots = {{-2, -1}, {2, -2}, {0, 3}};
        boolean any = false;
        for (int[] spot : spots) {
            if (random.nextInt(3) == 0) {
                continue;
            }
            int x = origin.getX() + spot[0];
            int z = origin.getZ() + spot[1];
            int base = ground(level, x, z);
            if (base == NONE || base <= SEA_LEVEL) {
                continue;
            }
            palm(level, x, base, z, random);
            any = true;
        }
        return any;
    }

    private static void palm(WorldGenLevel level, int x, int base, int z, RandomSource random) {
        int height = 6 + random.nextInt(3);
        Direction lean = SIDES[random.nextInt(4)];
        int tx = x;
        int tz = z;
        for (int i = 0; i < height; i++) {
            if (i >= 3 && (i & 1) == 1) {
                tx += lean.getStepX();
                tz += lean.getStepZ();
            }
            level.setBlock(new BlockPos(tx, base + i, tz), TRUNK, UPDATE_FLAGS);
        }

        BlockPos crown = new BlockPos(tx, base + height, tz);
        level.setBlock(crown, FROND, UPDATE_FLAGS);
        for (Direction d : SIDES) {
            BlockPos a = crown.relative(d);
            BlockPos b = a.relative(d);
            BlockPos tip = b.relative(d).below();
            BlockPos left = b.relative(d.getClockWise()).below();
            BlockPos right = b.relative(d.getCounterClockWise()).below();
            level.setBlock(a, FROND, UPDATE_FLAGS);
            level.setBlock(b, FROND, UPDATE_FLAGS);
            level.setBlock(tip, FROND, UPDATE_FLAGS);
            level.setBlock(left, FROND, UPDATE_FLAGS);
            level.setBlock(right, FROND, UPDATE_FLAGS);
        }
    }
}
