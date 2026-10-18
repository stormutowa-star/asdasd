package com.ricardo.frutigerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Estructuras de Frutiger World (estética Frutiger Aero: cristal azul, blanco brillante,
 * aqua, cielo y césped).
 *
 * Reglas para que nunca bloqueen el paso:
 *  - Cada estructura de suelo se levanta sobre una plataforma plana que queda exactamente
 *    un escalón por encima del terreno delante de la puerta (se sube de un salto).
 *  - La puerta siempre tiene 3 bloques de ancho y 3 de alto, despejada de hojas y hierba.
 *  - No se generan estructuras sobre agua, ni con más de 4 bloques de desnivel.
 *  - Las estructuras de altura (burbujas, turbinas) quedan por encima del camino.
 */
public final class FrutigerStructures {

    private static final int SEA_LEVEL = 63;
    private static final int MAX_RELIEF = 4;
    private static final int NONE = Integer.MIN_VALUE;
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
            level.setBlock(new BlockPos(cx + dx, floorY + dy, cz + dz), state, Block.UPDATE_CLIENT);
        }

        private void air(int dx, int dy, int dz) {
            set(dx, dy, dz, AIR);
        }

        private void setIfAir(int dx, int dy, int dz, BlockState state) {
            BlockPos pos = new BlockPos(cx + dx, floorY + dy, cz + dz);
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, state, Block.UPDATE_CLIENT);
            }
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
                level.setBlock(new BlockPos(x, y, z), FOUNDATION, Block.UPDATE_CLIENT);
            }
        } else {
            for (int y = floorY; y < ground; y++) {
                level.setBlock(new BlockPos(x, y, z), AIR, Block.UPDATE_CLIENT);
            }
        }
        for (int y = floorY; y <= floorY + height; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir()) {
                level.setBlock(pos, AIR, Block.UPDATE_CLIENT);
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

    private static void setIfAir(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, state, Block.UPDATE_CLIENT);
        }
    }

    /** Pabellón de burbuja: cúpula de cristal aqua con franjas de brillo blancas, suelo de cuarzo y farolas marinas. */
    public static boolean dome(WorldGenLevel level, BlockPos origin, RandomSource random) {
        final int r = 5;
        int side = random.nextInt(4);
        Site s = prepare(level, origin, r, 10, side);
        if (s == null) {
            return false;
        }

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 25) {
                    s.set(dx, 0, dz, ((dx + dz) & 1) == 0 ? QUARTZ : WHITE);
                }
                if (d2 >= 21 && d2 <= 26) {
                    s.set(dx, 1, dz, WHITE);
                    s.set(dx, 2, dz, QUARTZ);
                }
            }
        }

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = 0; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 >= 16 && d2 <= 25) {
                        s.set(dx, 3 + dy, dz, (dx == dz && dy >= 2) ? SHEEN_GLASS : AQUA_GLASS);
                    }
                }
            }
        }

        for (int k = -1; k <= 1; k++) {
            for (int depth = 3; depth <= r; depth++) {
                int[] p = outward(side, depth, k);
                for (int dy = 1; dy <= 3; dy++) {
                    s.air(p[0], dy, p[1]);
                }
            }
        }

        s.set(-2, 1, -2, LANTERN);
        s.set(2, 1, -2, LANTERN);
        s.set(-2, 1, 2, LANTERN);
        s.set(2, 1, 2, LANTERN);
        s.set(0, 1, 0, PILLAR);
        s.set(0, 2, 0, LANTERN);
        return true;
    }

    /** Plaza con fuente: estanque de agua enmarcado en cuarzo, columna central con luz y cuatro farolas. */
    public static boolean fountain(WorldGenLevel level, BlockPos origin, RandomSource random) {
        final int r = 4;
        Site s = prepare(level, origin, r, 6, -1);
        if (s == null) {
            return false;
        }

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                s.set(dx, 0, dz, ((dx + dz) & 1) == 0 ? QUARTZ : WHITE);
            }
        }

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

        s.set(0, 0, 0, PILLAR);
        s.set(0, 1, 0, PILLAR);
        s.set(0, 2, 0, LANTERN);

        for (int sx = -r; sx <= r; sx += 2 * r) {
            for (int sz = -r; sz <= r; sz += 2 * r) {
                s.set(sx, 1, sz, PILLAR);
                s.set(sx, 2, sz, PILLAR);
                s.set(sx, 3, sz, LANTERN);
            }
        }
        return true;
    }

    /** Torre mirador de 7x7: muros blancos con ventanas aqua, escalera de mano interior y pasarela en lo alto. */
    public static boolean lookout(WorldGenLevel level, BlockPos origin, RandomSource random) {
        final int r = 3;
        final int h = 12;
        int side = random.nextInt(4);
        Site s = prepare(level, origin, r, h + 3, side);
        if (s == null) {
            return false;
        }

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                s.set(dx, 0, dz, QUARTZ);
                if (Math.abs(dx) != r && Math.abs(dz) != r) {
                    continue;
                }
                for (int dy = 1; dy <= h; dy++) {
                    boolean window = dx != 0 && dz != 0
                            && (dy == 4 || dy == 5 || dy == 8 || dy == 9)
                            && ((dx + dz) & 1) == 0;
                    s.set(dx, dy, dz, window ? AQUA_GLASS : WHITE);
                }
                s.set(dx, h, dz, QUARTZ);
            }
        }

        int[] door = outward(side, r, 0);
        s.air(door[0], 1, door[1]);
        s.air(door[0], 2, door[1]);

        int back = (side + 2) % 4;
        int[] ladderPos = outward(back, r - 1, 0);
        BlockState ladder = Blocks.LADDER.defaultBlockState()
                .setValue(LadderBlock.FACING, SIDES[back].getOpposite());
        for (int dy = 1; dy <= h; dy++) {
            s.set(ladderPos[0], dy, ladderPos[1], ladder);
        }

        s.set(-2, 1, -2, LANTERN);
        s.set(2, 1, -2, LANTERN);
        s.set(-2, 1, 2, LANTERN);
        s.set(2, 1, 2, LANTERN);
        return true;
    }

    /** Aerogenerador blanco de 17 bloques con rotor de tres aspas (plano en X o en Z). */
    public static boolean turbine(WorldGenLevel level, BlockPos origin, RandomSource random) {
        boolean alongX = random.nextBoolean();
        Site s = prepare(level, origin, 1, 26, -1);
        if (s == null) {
            return false;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                s.set(dx, 0, dz, QUARTZ);
            }
        }
        for (int dy = 1; dy <= 16; dy++) {
            s.set(0, dy, 0, PILLAR);
        }

        int ax = alongX ? 1 : 0;
        int az = alongX ? 0 : 1;
        s.set(0, 17, 0, WHITE);
        s.set(ax, 17, az, WHITE);
        s.set(2 * ax, 17, 2 * az, CYAN);

        double[][] arms = {{0.0, 1.0}, {-0.866, -0.5}, {0.866, -0.5}};
        for (double[] arm : arms) {
            for (int t = 1; t <= 7; t++) {
                int u = (int) Math.round(arm[0] * t);
                int v = (int) Math.round(arm[1] * t);
                int bx = alongX ? 2 : u;
                int bz = alongX ? u : 2;
                s.setIfAir(bx, 17 + v, bz, QUARTZ);
            }
        }
        return true;
    }

    /** Racimo de burbujas de cristal flotando a 12-19 bloques del suelo, con brillo blanco y luz en el centro. */
    public static boolean bubbleCluster(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int count = 2 + random.nextInt(2);
        for (int b = 0; b < count; b++) {
            int cx = origin.getX() + random.nextInt(9) - 4;
            int cz = origin.getZ() + random.nextInt(9) - 4;
            int cy = origin.getY() + 12 + random.nextInt(8);
            int rad = 2 + random.nextInt(2);
            int lo = rad * rad - rad;
            int hi = rad * rad + rad / 2;
            for (int dx = -rad; dx <= rad; dx++) {
                for (int dy = -rad; dy <= rad; dy++) {
                    for (int dz = -rad; dz <= rad; dz++) {
                        int d2 = dx * dx + dy * dy + dz * dz;
                        if (d2 < lo || d2 > hi) {
                            continue;
                        }
                        boolean sheen = dx < 0 && dy > 0 && dz < 0;
                        setIfAir(level, new BlockPos(cx + dx, cy + dy, cz + dz), sheen ? SHEEN_GLASS : AQUA_GLASS);
                    }
                }
            }
            setIfAir(level, new BlockPos(cx, cy, cz), LANTERN);
        }
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
            level.setBlock(new BlockPos(tx, base + i, tz), TRUNK, Block.UPDATE_CLIENT);
        }

        BlockPos crown = new BlockPos(tx, base + height, tz);
        level.setBlock(crown, FROND, Block.UPDATE_CLIENT);
        for (Direction d : SIDES) {
            BlockPos a = crown.relative(d);
            BlockPos b = a.relative(d);
            BlockPos tip = b.relative(d).below();
            BlockPos left = b.relative(d.getClockWise()).below();
            BlockPos right = b.relative(d.getCounterClockWise()).below();
            level.setBlock(a, FROND, Block.UPDATE_CLIENT);
            level.setBlock(b, FROND, Block.UPDATE_CLIENT);
            level.setBlock(tip, FROND, Block.UPDATE_CLIENT);
            level.setBlock(left, FROND, Block.UPDATE_CLIENT);
            level.setBlock(right, FROND, Block.UPDATE_CLIENT);
        }
    }
}
