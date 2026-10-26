package com.keinloree.world;

import com.keinloree.entity.Animated;
import com.keinloree.entity.CalendarioEntity;
import com.keinloree.net.Net;
import com.keinloree.net.VfxS2C;
import com.keinloree.registry.ModBlocks;
import com.keinloree.registry.ModSounds;
import com.keinloree.story.Campaign;
import com.keinloree.vfx.Vfx;
import com.keinloree.vfx.VfxType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.keinloree.ship.FighterEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Los cinco Pilares de las Horas: posicion (en la campana), construccion cuando su zona esta cargada, version en
 * ruinas, haz de luz permanente, anclas de luz y zona segura. Indice de hora igual que {@link CalendarioEntity#IDS}.
 */
public final class Pillars {
    public static final int COUNT = 5;
    public static final int RADIUS = 11;
    public static final int HEIGHT = 28;
    public static final double SAFE_RADIUS = 24.0D;
    private static final int[] DISTANCES = {350, 500, 600, 750, 900};
    private static final int BEAM_PERIOD = 100;
    private static boolean registered;

    /** Materiales por Hora: suelo, acento, cristal, luz. */
    private static final Block[][] MATS = {
            {Blocks.CUT_SANDSTONE, Blocks.GOLD_BLOCK, Blocks.ORANGE_STAINED_GLASS, Blocks.SHROOMLIGHT},
            {Blocks.QUARTZ_BLOCK, Blocks.GOLD_BLOCK, Blocks.WHITE_STAINED_GLASS, Blocks.GLOWSTONE},
            {Blocks.RED_NETHER_BRICKS, Blocks.PURPUR_BLOCK, Blocks.AMETHYST_BLOCK, Blocks.SHROOMLIGHT},
            {Blocks.POLISHED_DEEPSLATE, Blocks.LAPIS_BLOCK, Blocks.AMETHYST_BLOCK, Blocks.SEA_LANTERN},
            {Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.WHITE_CONCRETE, Blocks.SEA_LANTERN}};

    private Pillars() {
    }

    public static synchronized void init() {
        if (!registered) {
            registered = true;
            MinecraftForge.EVENT_BUS.register(new Pillars.Events());
        }
    }

    public static String id(int h) {
        return CalendarioEntity.IDS[h];
    }

    public static int hourOf(String id) {
        for (int i = 0; i < COUNT; i++) {
            if (CalendarioEntity.IDS[i].equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public static int color(int h) {
        return CalendarioEntity.COLORS[h];
    }

    // ------------------------------------------------------------------ estado

    public static boolean planned(Campaign c) {
        return c.positions.containsKey("pilar." + id(0));
    }

    @Nullable
    public static BlockPos pos(Campaign c, int h) {
        return c.positions.get("pilar." + id(h));
    }

    public static boolean built(Campaign c, int h) {
        return c.hasFlag("pilar_hecho." + id(h));
    }

    public static boolean fallen(Campaign c, int h) {
        return c.hasFlag("pilar_caido." + id(h));
    }

    public static boolean standing(Campaign c, int h) {
        return planned(c) && !fallen(c, h);
    }

    /** Pilar (de la mascara {@code allowed}) mas cercano a un punto; -1 si no hay. */
    public static int nearest(Campaign c, Vec3 at, int allowedMask) {
        int best = -1;
        double bd = Double.MAX_VALUE;
        for (int h = 0; h < COUNT; h++) {
            BlockPos p = pos(c, h);
            if (p == null || (allowedMask & (1 << h)) == 0) {
                continue;
            }
            double d = horizontalDist(at, p);
            if (d < bd) {
                bd = d;
                best = h;
            }
        }
        return best;
    }

    public static double horizontalDist(Vec3 at, BlockPos p) {
        double dx = at.x - (p.getX() + 0.5D);
        double dz = at.z - (p.getZ() + 0.5D);
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** True si el punto esta en la zona segura de algun pilar en pie. */
    public static boolean inSafeZone(ServerLevel level, Vec3 at) {
        Campaign c = Campaign.get(level);
        for (int h = 0; h < COUNT; h++) {
            BlockPos p = pos(c, h);
            if (p != null && !fallen(c, h) && horizontalDist(at, p) < SAFE_RADIUS) {
                return true;
            }
        }
        return false;
    }

    /** Centro del pilar a la altura del suelo de la plataforma (si aun no esta construido, la superficie estimada). */
    public static Vec3 center(ServerLevel level, Campaign c, int h) {
        BlockPos p = pos(c, h);
        if (p == null) {
            return Vec3.ZERO;
        }
        int y = built(c, h) ? p.getY() : level.isLoaded(p) ? level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ()) : 70;
        return new Vec3(p.getX() + 0.5D, y + 1.0D, p.getZ() + 0.5D);
    }

    // ------------------------------------------------------------------ plan y construccion

    /** Elige las cinco posiciones alrededor de {@code around} (en tierra firme). */
    public static void plan(ServerLevel level, Vec3 around) {
        Campaign c = Campaign.get(level);
        if (planned(c)) {
            return;
        }
        RandomSource rnd = level.random;
        List<Integer> dists = new ArrayList<>();
        for (int d : DISTANCES) {
            dists.add(d);
        }
        Collections.shuffle(dists, new java.util.Random(rnd.nextLong()));
        double base = rnd.nextDouble() * Math.PI * 2.0D;
        for (int h = 0; h < COUNT; h++) {
            double ang = base + h * Math.PI * 2.0D / COUNT + (rnd.nextDouble() - 0.5D) * 0.3D;
            BlockPos found = null;
            int[] tries = {0, 60, -60, 120, -120};
            outer:
            for (int dd : new int[]{0, -100, 100, -200}) {
                for (int off : tries) {
                    double dist = Math.max(200, dists.get(h) + dd);
                    double a = ang + off / dist;
                    int x = Mth.floor(around.x + Math.cos(a) * dist);
                    int z = Mth.floor(around.z + Math.sin(a) * dist);
                    BlockPos probe = new BlockPos(x, 64, z);
                    var biome = level.getBiome(probe);
                    if (!biome.is(BiomeTags.IS_OCEAN) && !biome.is(BiomeTags.IS_DEEP_OCEAN) && !biome.is(BiomeTags.IS_RIVER)) {
                        found = probe;
                        break outer;
                    }
                }
            }
            if (found == null) {
                found = BlockPos.containing(around.x + Math.cos(ang) * dists.get(h), 64, around.z + Math.sin(ang) * dists.get(h));
            }
            c.positions.put("pilar." + id(h), found);
        }
        c.setDirty();
    }

    /** Construye los pilares planificados cuya zona ya esta cargada. */
    private static void buildPending(ServerLevel level, Campaign c) {
        for (int h = 0; h < COUNT; h++) {
            BlockPos p = pos(c, h);
            if (p == null || built(c, h)) {
                continue;
            }
            if (!level.isLoaded(p.offset(-14, 0, -14)) || !level.isLoaded(p.offset(14, 0, 14))
                    || !level.isLoaded(p.offset(-14, 0, 14)) || !level.isLoaded(p.offset(14, 0, -14))) {
                continue;
            }
            int y = groundY(level, p);
            BlockPos g = new BlockPos(p.getX(), y, p.getZ());
            build(level, h, g, fallen(c, h));
            c.positions.put("pilar." + id(h), g);
            c.setFlag("pilar_hecho." + id(h));
            c.setDirty();
        }
    }

    /** Fuerza la construccion (carga los chunks). Para pruebas y para la historia cuando hace falta ya. */
    public static void forceBuild(ServerLevel level, int h) {
        Campaign c = Campaign.get(level);
        BlockPos p = pos(c, h);
        if (p == null || built(c, h)) {
            return;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.getChunk((p.getX() >> 4) + dx, (p.getZ() >> 4) + dz);
            }
        }
        buildPending(level, c);
    }

    private static int groundY(ServerLevel level, BlockPos p) {
        int[] ys = new int[9];
        int k = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ys[k++] = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX() + dx * 8, p.getZ() + dz * 8);
            }
        }
        java.util.Arrays.sort(ys);
        return Mth.clamp(ys[4], level.getMinBuildHeight() + 8, level.getMaxBuildHeight() - HEIGHT - 10);
    }

    private static void set(ServerLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    /** Plataforma, obelisco, braseros y anclas. g = centro de la plataforma (bloque de suelo). */
    private static void build(ServerLevel level, int h, BlockPos g, boolean ruined) {
        Block floor = MATS[h][0];
        Block accent = MATS[h][1];
        Block crystal = MATS[h][2];
        Block light = MATS[h][3];
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx = -RADIUS - 1; dx <= RADIUS + 1; dx++) {
            for (int dz = -RADIUS - 1; dz <= RADIUS + 1; dz++) {
                double r = Math.sqrt(dx * dx + dz * dz);
                if (r > RADIUS + 1.2D) {
                    continue;
                }
                boolean step = r > RADIUS + 0.3D;
                int top = step ? g.getY() - 1 : g.getY();
                // despeja encima
                for (int y = top + 1; y <= g.getY() + HEIGHT + 2; y++) {
                    m.set(g.getX() + dx, y, g.getZ() + dz);
                    if (!level.getBlockState(m).isAir()) {
                        set(level, m, air);
                    }
                }
                // cimientos hasta el terreno
                for (int y = top; y >= g.getY() - 10; y--) {
                    m.set(g.getX() + dx, y, g.getZ() + dz);
                    BlockState cur = level.getBlockState(m);
                    if (y < top && !cur.isAir() && cur.getFluidState().isEmpty() && !cur.canBeReplaced()) {
                        break;
                    }
                    Block b = y == top ? (r > RADIUS - 0.8D || (int) r == 6 ? accent : floor) : floor;
                    set(level, m, b.defaultBlockState());
                }
            }
        }
        // obelisco
        int cut = ruined ? 11 : HEIGHT;
        for (int y = 1; y <= cut; y++) {
            if (y <= 8) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        boolean corner = dx != 0 && dz != 0;
                        Block b = (dx == 0 && dz == 0) ? (y == 4 && !ruined ? light : accent) : corner ? accent : (y % 3 == 0 ? crystal : floor);
                        set(level, g.offset(dx, y, dz), b.defaultBlockState());
                    }
                }
            } else {
                Block b = y == HEIGHT ? light : (y % 5 == 0 ? crystal : accent);
                set(level, g.above(y), b.defaultBlockState());
            }
        }
        // braseros
        for (int i = 0; i < 4; i++) {
            double a = Math.PI / 4.0D + i * Math.PI / 2.0D;
            BlockPos b = g.offset((int) Math.round(Math.cos(a) * 7.0D), 0, (int) Math.round(Math.sin(a) * 7.0D));
            set(level, b.above(1), accent.defaultBlockState());
            set(level, b.above(2), (ruined ? floor : light).defaultBlockState());
        }
        // anclas
        for (BlockPos a : anchors(g)) {
            set(level, a, (ruined ? floor : ModBlocks.HOUR_ANCHOR.get()).defaultBlockState());
        }
        if (ruined) {
            scatter(level, g, floor, accent);
        }
    }

    public static List<BlockPos> anchors(BlockPos g) {
        List<BlockPos> out = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            double a = i * Math.PI * 2.0D / 3.0D;
            out.add(g.offset((int) Math.round(Math.cos(a) * 4.0D), 1, (int) Math.round(Math.sin(a) * 4.0D)));
        }
        return out;
    }

    private static void scatter(ServerLevel level, BlockPos g, Block floor, Block accent) {
        RandomSource r = level.random;
        for (int i = 0; i < 26; i++) {
            double a = r.nextDouble() * Math.PI * 2.0D;
            double d = 2.5D + r.nextDouble() * 8.0D;
            BlockPos p = g.offset((int) Math.round(Math.cos(a) * d), 1, (int) Math.round(Math.sin(a) * d));
            if (level.getBlockState(p).isAir()) {
                set(level, p, (r.nextBoolean() ? floor : accent).defaultBlockState());
            }
        }
    }

    /** El pilar cae: obelisco partido, escombros, sin haz. Con efectos. */
    public static void topple(ServerLevel level, int h) {
        Campaign c = Campaign.get(level);
        if (!c.setFlag("pilar_caido." + id(h))) {
            return;
        }
        BlockPos g = pos(c, h);
        if (g == null) {
            return;
        }
        if (built(c, h) && level.isLoaded(g)) {
            Vec3 ctr = Vec3.atBottomCenterOf(g.above());
            for (int y = 12; y <= HEIGHT; y++) {
                set(level, g.above(y), Blocks.AIR.defaultBlockState());
                if (y % 4 == 0) {
                    Vfx.explosion(level, ctr.add(0, y, 0), 2.0F);
                }
            }
            for (int y = 1; y <= 8; y++) {
                set(level, g.above(y), MATS[h][1].defaultBlockState());
            }
            for (BlockPos a : anchors(g)) {
                set(level, a, MATS[h][0].defaultBlockState());
            }
            for (int i = 0; i < 4; i++) {
                double a = Math.PI / 4.0D + i * Math.PI / 2.0D;
                BlockPos b = g.offset((int) Math.round(Math.cos(a) * 7.0D), 2, (int) Math.round(Math.sin(a) * 7.0D));
                set(level, b, MATS[h][0].defaultBlockState());
            }
            scatter(level, g, MATS[h][0], MATS[h][1]);
            Vfx.shockwave(level, ctr, color(h), 14.0F, 30);
            level.playSound(null, g, ModSounds.EXPLOSION_BIG.get(), SoundSource.HOSTILE, 6.0F, 0.7F);
        }
        syncBeams(level);
    }

    // ------------------------------------------------------------------ Calendarios en sus pilares

    @Nullable
    public static CalendarioEntity calendario(ServerLevel level, int h) {
        Campaign c = Campaign.get(level);
        java.util.UUID id = c.entities.get("cal." + id(h));
        Entity e = id == null ? null : level.getEntity(id);
        return e instanceof CalendarioEntity ce && ce.isAlive() ? ce : null;
    }

    /** Busca o crea al Calendario {@code h} en {@code at}; lo registra en la campana. */
    public static CalendarioEntity summon(ServerLevel level, int h, Vec3 at, float yaw) {
        CalendarioEntity ce = calendario(level, h);
        if (ce == null) {
            ce = CalendarioEntity.create(level, h, at, yaw);
            Campaign c = Campaign.get(level);
            c.entities.put("cal." + id(h), ce.getUUID());
            c.setDirty();
        } else {
            ce.teleportTo(at.x, at.y, at.z);
            ce.setYRot(yaw);
        }
        return ce;
    }

    /** Lleva al Calendario a su pilar (si esta cargado) con su destello. */
    public static void sendHome(ServerLevel level, int h) {
        Campaign c = Campaign.get(level);
        BlockPos g = pos(c, h);
        CalendarioEntity ce = calendario(level, h);
        if (g == null) {
            return;
        }
        if (ce != null) {
            Vfx.pillar(level, ce.position(), 30.0F, color(h), 30);
        }
        Vec3 spot = center(level, c, h).add(3.5D, 0.0D, 0.5D);
        if (!level.isLoaded(BlockPos.containing(spot))) {
            if (ce != null) {
                ce.discard();
                c.entities.remove("cal." + id(h));
                c.setDirty();
            }
            return;
        }
        ce = summon(level, h, spot, 0.0F);
        ce.follow(null);
        ce.setMode(CalendarioEntity.MODE_GUARD);
        ce.setHome(BlockPos.containing(spot));
        Vfx.pillar(level, spot, 30.0F, color(h), 30);
    }

    // ------------------------------------------------------------------ haces

    public static void syncBeams(ServerLevel level) {
        Campaign c = Campaign.get(level);
        if (!planned(c) || c.hasFlag("pilares_apagados")) {
            return;
        }
        for (int h = 0; h < COUNT; h++) {
            if (fallen(c, h) || !c.hasFlag("pilares_encendidos")) {
                continue;
            }
            Vec3 base = center(level, c, h).add(0.0D, built(c, h) ? HEIGHT : 0.0D, 0.0D);
            VfxS2C msg = new VfxS2C(VfxType.PILLAR.id, base.x, base.y, base.z, 0, 1, 0, color(h), 2.4F,
                    BEAM_PERIOD + 30, -1, 380.0F - (float) base.y);
            for (ServerPlayer p : level.players()) {
                Net.toPlayer(p, msg);
            }
        }
    }

    public static void tick(ServerLevel level) {
        Campaign c = Campaign.get(level);
        if (!planned(c)) {
            return;
        }
        long t = level.getGameTime();
        if (t % 40 == 0) {
            buildPending(level, c);
        }
        if (t % BEAM_PERIOD == 0) {
            syncBeams(level);
        }
        if (t % 10 == 0) {
            defend(level, c);
        }
        if (t % 80 == 0 && c.hasFlag("pilares_encendidos")) {
            for (ServerPlayer p : level.players()) {
                for (int h = 0; h < COUNT; h++) {
                    BlockPos g = pos(c, h);
                    if (g != null && !fallen(c, h) && built(c, h) && horizontalDist(p.position(), g) < 32.0D) {
                        p.playNotifySound(ModSounds.PILLAR_HUM.get(), SoundSource.AMBIENT, 0.6F, 0.8F + h * 0.08F);
                    }
                }
            }
        }
    }

    /**
     * Los pilares en pie atacan los cazas de la Legion que sobrevuelan su zona: su Calendario lanza un ataque con
     * animacion y un rayo hacia el caza. Cada pilar ataca cada dos segundos.
     */
    private static void defend(ServerLevel level, Campaign c) {
        for (int h = 0; h < COUNT; h++) {
            if (!built(c, h) || fallen(c, h)) {
                continue;
            }
            Vec3 base = center(level, c, h);
            AABB zone = new AABB(base.x - 22.0D, base.y - 4.0D, base.z - 22.0D, base.x + 22.0D, base.y + 90.0D, base.z + 22.0D);
            FighterEntity target = null;
            double best = Double.MAX_VALUE;
            for (FighterEntity f : level.getEntitiesOfClass(FighterEntity.class, zone,
                    x -> x.isAlive() && x.isLegionCraft() && !(x.getControllingPassenger() instanceof Player))) {
                double d = f.distanceToSqr(base);
                if (d < best) {
                    best = d;
                    target = f;
                }
            }
            if (target == null || level.getGameTime() < c.counter("def_t." + id(h))) {
                continue;
            }
            c.setCounter("def_t." + id(h), (int) (level.getGameTime() + 40));
            CalendarioEntity guard = calendario(level, h);
            if (guard == null) {
                guard = summon(level, h, base.add(3.5D, 0.0D, 0.5D), 0.0F);
                guard.setMode(CalendarioEntity.MODE_STILL);
            }
            guard.play(Animated.A_CAST);
            guard.getLookControl().setLookAt(target, 30.0F, 30.0F);
            Vec3 from = guard.position().add(0.0D, 1.4D, 0.0D);
            Vec3 to = target.position().add(0.0D, 1.0D, 0.0D);
            Vfx.beam(level, from, to, color(h), 0.7F, 14);
            Vfx.burst(level, to, color(h), 10, 0.4F);
            level.playSound(null, from.x, from.y, from.z, ModSounds.KEIN_BEAM.get(), SoundSource.HOSTILE, 2.0F, 1.1F);
            target.hurt(level.damageSources().mobAttack(guard), 18.0F);
        }
    }

    /** Vuelve a dejar los pilares sin plan (reset de la historia): no toca bloques ya puestos. */
    public static void forget(Campaign c) {
        for (int h = 0; h < COUNT; h++) {
            c.positions.remove("pilar." + id(h));
        }
    }

    public static final class Events {
        @SubscribeEvent
        public void onBreak(BlockEvent.BreakEvent e) {
            if (!e.getState().is(ModBlocks.HOUR_ANCHOR.get()) || !(e.getLevel() instanceof ServerLevel sl)) {
                return;
            }
            Campaign c = Campaign.get(sl.getServer());
            int h = nearest(c, Vec3.atCenterOf(e.getPos()), 0x1F);
            boolean allowed = e.getPlayer().isCreative() || (h >= 0 && c.hasFlag("anclas_rompibles." + id(h)));
            if (!allowed) {
                e.setCanceled(true);
                e.getPlayer().displayClientMessage(Component.translatable("message.keinloree.anchor_protected"), true);
                return;
            }
            if (h >= 0) {
                c.inc("anclas_rotas." + id(h), 1);
                Vfx.burst(sl, Vec3.atCenterOf(e.getPos()), color(h), 30, 0.6F);
                sl.playSound(null, e.getPos(), ModSounds.GLITCH.get(), SoundSource.BLOCKS, 2.0F, 0.6F);
            }
        }
    }
}
