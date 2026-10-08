package com.ricardo.makuosaga.world;

import com.dragonminez.common.init.MainEntities;
import com.dragonminez.common.init.entities.questnpc.QuestNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Builds the two landmarks of Vharos the first time someone arrives:
 * the ruined Guardians' camp at the landing site (0, 0) and Makuo's Citadel at (0, -320).
 */
public final class VharosBuilder {
	public static final int CITADEL_X = 0;
	public static final int CITADEL_Z = -320;
	/** Platform height; quest 12 targets (0, 101, -300), the Citadel's gate. */
	public static final int CITADEL_Y = 100;

	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState BRICKS = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
	private static final BlockState BLACKSTONE = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
	private static final BlockState CHISELED = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
	private static final BlockState GILDED = Blocks.GILDED_BLACKSTONE.defaultBlockState();
	private static final BlockState OBSIDIAN = Blocks.OBSIDIAN.defaultBlockState();
	private static final BlockState CRYING = Blocks.CRYING_OBSIDIAN.defaultBlockState();
	private static final BlockState AMETHYST = Blocks.AMETHYST_BLOCK.defaultBlockState();
	private static final BlockState CLUSTER = Blocks.AMETHYST_CLUSTER.defaultBlockState();
	private static final BlockState PURPUR_PILLAR = Blocks.PURPUR_PILLAR.defaultBlockState();
	private static final BlockState GLASS = Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
	private static final BlockState LANTERN_BLOCK = Blocks.SEA_LANTERN.defaultBlockState();
	private static final BlockState END_ROD = Blocks.END_ROD.defaultBlockState();
	private static final BlockState SOUL_LANTERN = Blocks.SOUL_LANTERN.defaultBlockState();
	private static final BlockState CARPET = Blocks.PURPLE_CARPET.defaultBlockState();
	private static final BlockState QUARTZ = Blocks.SMOOTH_QUARTZ.defaultBlockState();
	private static final BlockState CALCITE = Blocks.CALCITE.defaultBlockState();
	private static final BlockState STONE = Blocks.STONE.defaultBlockState();
	private static final BlockState LANTERN = Blocks.LANTERN.defaultBlockState();
	private static final BlockState FENCE = Blocks.DARK_OAK_FENCE.defaultBlockState();

	private VharosBuilder() {
	}

	// ---------------------------------------------------------------- camp

	public static void buildCamp(ServerLevel level) {
		int ground = groundY(level, 0, 0);
		// Plaza: clear the area and lay a calcite/quartz floor on a stone base (also bridges water or holes).
		for (int x = -11; x <= 11; x++) {
			for (int z = -11; z <= 11; z++) {
				double d = Math.sqrt(x * x + z * z);
				if (d > 11.5) continue;
				for (int y = ground + 1; y <= ground + 10; y++) set(level, x, y, z, AIR);
				set(level, x, ground, z, hash(x, 0, z) % 4 == 0 ? QUARTZ : CALCITE);
				for (int y = ground - 1; y >= ground - 4; y--) {
					if (level.getBlockState(new BlockPos(x, y, z)).canBeReplaced()) set(level, x, y, z, STONE);
				}
			}
		}
		// Three ruined Namekian domes.
		dome(level, 7, ground + 1, 5, 4);
		dome(level, -8, ground + 1, -2, 4);
		dome(level, 3, ground + 1, -8, 3);
		// Lanterns on posts and amethyst growing between the stones.
		for (int[] p : new int[][]{{-4, 4}, {4, -3}, {-3, -5}, {5, 8}}) {
			set(level, p[0], ground + 1, p[1], FENCE);
			set(level, p[0], ground + 2, p[1], LANTERN);
		}
		for (int[] p : new int[][]{{-6, 7}, {9, -4}, {-10, 3}, {1, 10}, {-2, -10}}) {
			set(level, p[0], ground + 1, p[1], CLUSTER);
		}
		// Saien, the last Guardian of the Chain.
		QuestNPCEntity saien = MainEntities.QUEST_NPC.get().create(level);
		if (saien != null) {
			saien.setNpcId("saien");
			saien.setNpcModel("saga_piccolo");
			saien.setNpcTexture("saga_saien");
			saien.moveTo(0.5D, ground + 1, -2.5D, 180.0F, 0.0F);
			saien.setYHeadRot(180.0F);
			saien.setYBodyRot(180.0F);
			saien.setHomePosition(0.5D, -2.5D);
			level.addFreshEntity(saien);
		}
	}

	private static void dome(ServerLevel level, int cx, int baseY, int cz, int r) {
		for (int x = -r; x <= r; x++) {
			for (int y = 0; y <= r; y++) {
				for (int z = -r; z <= r; z++) {
					double d = Math.sqrt(x * x + y * y + z * z);
					if (d > r + 0.5 || d < r - 0.5) continue;
					boolean doorway = z == r || z == r - 1;
					if (doorway && Math.abs(x) <= 1 && y <= 2) continue;
					// Ruined: one side has collapsed and some blocks are missing.
					if (x > 1 && y >= r - 1) continue;
					if (hash(cx + x, y, cz + z) % 6 == 0) continue;
					BlockState state = y == 2 && hash(x, y, z) % 3 == 0 ? GLASS : QUARTZ;
					set(level, cx + x, baseY + y, cz + z, state);
				}
			}
		}
	}

	// ---------------------------------------------------------------- citadel

	public static void buildCitadel(ServerLevel level) {
		int cx = CITADEL_X, cz = CITADEL_Z, top = CITADEL_Y;
		for (int chunkX = (cx - 26) >> 4; chunkX <= (cx + 26) >> 4; chunkX++) {
			for (int chunkZ = (cz - 26) >> 4; chunkZ <= (cz + 26) >> 4; chunkZ++) {
				level.getChunk(chunkX, chunkZ);
			}
		}

		// Clear the sky above the platform (carves through mountains if needed).
		for (int x = -24; x <= 24; x++) {
			for (int z = -24; z <= 24; z++) {
				if (x * x + z * z > 24 * 24) continue;
				for (int y = top + 1; y <= top + 50; y++) set(level, cx + x, y, cz + z, AIR);
			}
		}

		// Platform with a gilded rim.
		for (int x = -22; x <= 22; x++) {
			for (int z = -22; z <= 22; z++) {
				double d = Math.sqrt(x * x + z * z);
				if (d > 22.5) continue;
				set(level, cx + x, top, cz + z, d > 20.5 ? GILDED : (hash(x, top, z) % 7 == 0 ? CHISELED : BRICKS));
			}
		}

		// Inverted obsidian cone down to the ground (or the sea floor).
		for (int y = top - 1, depth = 1; y > level.getMinBuildHeight() + 4 && depth < 140; y--, depth++) {
			double radius = Math.max(7.0D, 22.0D - depth * 0.45D);
			boolean placedAny = false;
			for (int x = (int) -radius; x <= radius; x++) {
				for (int z = (int) -radius; z <= radius; z++) {
					if (x * x + z * z > radius * radius) continue;
					BlockPos pos = new BlockPos(cx + x, y, cz + z);
					if (level.getBlockState(pos).canBeReplaced()) {
						level.setBlock(pos, hash(x, y, z) % 9 == 0 ? CRYING : OBSIDIAN, Block.UPDATE_CLIENTS);
						placedAny = true;
					}
				}
			}
			if (!placedAny) break;
		}

		buildWalls(level, cx, top, cz);
		for (int[] t : new int[][]{{15, 15}, {-15, 15}, {15, -15}, {-15, -15}}) {
			buildTower(level, cx + t[0], top, cz + t[1]);
		}
		buildKeep(level, cx, top, cz);
		buildSpire(level, cx, top, cz - 7);

		for (int[] p : new int[][]{{8, 12}, {-9, 11}, {12, -2}, {-12, 3}, {6, 17}, {-5, 16}}) {
			set(level, cx + p[0], top + 1, cz + p[1], CLUSTER);
		}
	}

	private static void buildWalls(ServerLevel level, int cx, int top, int cz) {
		for (int x = -22; x <= 22; x++) {
			for (int z = -22; z <= 22; z++) {
				double d = Math.sqrt(x * x + z * z);
				if (d < 19.5 || d >= 21.5) continue;
				boolean gate = z > 0 && Math.abs(x) <= 2;
				for (int y = top + 1; y <= top + 8; y++) {
					if (gate && y <= top + 6) continue;
					set(level, cx + x, y, cz + z, BRICKS);
				}
				if ((x + z) % 2 == 0) set(level, cx + x, top + 9, cz + z, BRICKS);
				if (hash(x, 0, z) % 11 == 0) set(level, cx + x, top + 9, cz + z, SOUL_LANTERN);
			}
		}
		// Gate frame (the gate faces south, towards the camp).
		int gz = cz + 20;
		for (int y = top + 1; y <= top + 7; y++) {
			set(level, cx - 3, y, gz, PURPUR_PILLAR);
			set(level, cx + 3, y, gz, PURPUR_PILLAR);
			set(level, cx - 3, y, gz + 1, PURPUR_PILLAR);
			set(level, cx + 3, y, gz + 1, PURPUR_PILLAR);
		}
		for (int x = -3; x <= 3; x++) {
			set(level, cx + x, top + 7, gz, CHISELED);
			set(level, cx + x, top + 7, gz + 1, CHISELED);
		}
		set(level, cx, top + 8, gz + 1, AMETHYST);
	}

	private static void buildTower(ServerLevel level, int tx, int top, int tz) {
		for (int x = -4; x <= 4; x++) {
			for (int z = -4; z <= 4; z++) {
				double d = Math.sqrt(x * x + z * z);
				if (d > 3.5) continue;
				boolean wall = d >= 2.5;
				for (int y = top + 1; y <= top + 18; y++) {
					BlockState state = wall ? (y == top + 12 && (x == 0 || z == 0) ? GLASS : BLACKSTONE) : AIR;
					set(level, tx + x, y, tz + z, state);
				}
				set(level, tx + x, top + 19, tz + z, AMETHYST);
			}
		}
		set(level, tx, top + 20, tz, END_ROD);
		set(level, tx, top + 21, tz, END_ROD);
	}

	private static void buildKeep(ServerLevel level, int cx, int top, int cz) {
		int x0 = -7, x1 = 7, z0 = -14, z1 = 0;
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
				boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
				for (int y = top + 1; y <= top + 12; y++) {
					BlockState state;
					if (!edge) {
						state = AIR;
					} else if (corner) {
						state = CRYING;
					} else if (z == z1 && Math.abs(x) <= 1 && y <= top + 4) {
						state = AIR; // entrance
					} else if (y >= top + 4 && y <= top + 8 && (x == x0 || x == x1) && Math.floorMod(z, 3) == 1) {
						state = GLASS;
					} else {
						state = BRICKS;
					}
					set(level, cx + x, y, cz + z, state);
				}
				set(level, cx + x, top + 13, cz + z, BRICKS);
				if (!edge) {
					set(level, cx + x, top, cz + z, Math.abs(x) <= 1 ? BLACKSTONE : BRICKS);
					if (Math.abs(x) <= 1 && z >= z0 + 3) set(level, cx + x, top + 1, cz + z, CARPET);
					if ((x == x0 + 2 || x == x1 - 2) && Math.floorMod(z, 4) == 2) set(level, cx + x, top, cz + z, LANTERN_BLOCK);
				}
			}
		}
		// Makuo's throne.
		for (int x = -3; x <= 3; x++) {
			for (int z = z0 + 1; z <= z0 + 3; z++) set(level, cx + x, top + 1, cz + z, AMETHYST);
		}
		set(level, cx, top + 2, cz + z0 + 2,
				Blocks.PURPUR_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
		for (int y = top + 2; y <= top + 5; y++) set(level, cx, y, cz + z0 + 1, CRYING);
		set(level, cx - 1, top + 2, cz + z0 + 1, AMETHYST);
		set(level, cx + 1, top + 2, cz + z0 + 1, AMETHYST);
		set(level, cx, top + 6, cz + z0 + 1, END_ROD);
	}

	private static void buildSpire(ServerLevel level, int sx, int top, int sz) {
		int base = top + 14;
		for (int i = 0; i <= 22; i++) {
			double radius = 3.2D - i * 0.1D;
			for (int x = -4; x <= 4; x++) {
				for (int z = -4; z <= 4; z++) {
					if (x * x + z * z > radius * radius) continue;
					boolean vein = (x == 0 || z == 0) && i % 4 == 0;
					set(level, sx + x, base + i, sz + z, vein ? AMETHYST : OBSIDIAN);
				}
			}
		}
		// The Void Heart: a violet crystal of glass around a glowing amethyst core.
		int hy = base + 28;
		for (int x = -4; x <= 4; x++) {
			for (int y = -4; y <= 4; y++) {
				for (int z = -4; z <= 4; z++) {
					int m = Math.abs(x) + Math.abs(y) + Math.abs(z);
					if (m > 4) continue;
					BlockState state = m == 4 ? GLASS : (m <= 1 ? LANTERN_BLOCK : AMETHYST);
					set(level, sx + x, hy + y, sz + z, state);
				}
			}
		}
		for (int y = base + 23; y < hy - 4; y++) set(level, sx, y, sz, END_ROD);
		set(level, sx, hy + 5, sz, END_ROD);
	}

	// ---------------------------------------------------------------- helpers

	private static int groundY(ServerLevel level, int x, int z) {
		level.getChunk(x >> 4, z >> 4);
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
		return Math.max(y, level.getSeaLevel());
	}

	private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
		level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
	}

	private static int hash(int x, int y, int z) {
		int h = x * 73856093 ^ y * 19349663 ^ z * 83492791;
		return Math.abs(h % 1000);
	}
}
