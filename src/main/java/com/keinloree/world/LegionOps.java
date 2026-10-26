package com.keinloree.world;

import com.keinloree.entity.CargoShip;
import com.keinloree.entity.Droid;
import com.keinloree.entity.LooreCommander;
import com.keinloree.entity.LooreMob;
import com.keinloree.entity.LooreWarlord;
import com.keinloree.entity.Rank;
import com.keinloree.entity.RazakoEntity;
import com.keinloree.registry.ModEntities;
import com.keinloree.registry.ModSounds;
import com.keinloree.ship.DestroyerEntity;
import com.keinloree.ship.FighterEntity;
import com.keinloree.ship.ShipArmament;
import com.keinloree.ship.ShipTransform;
import com.keinloree.ship.TurbolaserEntity;
import com.keinloree.story.Campaign;
import com.keinloree.util.Fx;
import com.keinloree.vfx.Vfx;
import com.keinloree.world.design.Grid;
import com.keinloree.world.design.Mat;
import com.keinloree.world.design.OutpostDesign;
import com.keinloree.world.design.ShipDesign;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Operaciones de la Legion en tierra: bases que bajan del hangar del destructor, patrullas que aterrizan junto al
 * heroe, vuelos de cazas y destruccion de bases. Las bases se guardan en la campana ("base.N").
 */
public final class LegionOps {
    public static final int OUTPOST_R = 12;
    public static final int FORT_R = 24;
    private static final int GARRISON_FALLBACK = 900;
    private static final Map<Integer, Grid> GRIDS = new HashMap<>();
    /** Base -> tick limite para bajar la guarnicion si la nave no llega a aterrizar. */
    private static final Map<Long, Long> GARRISON_DUE = new HashMap<>();

    private record Demolition(BlockPos g, boolean fort, int[] layer) {}

    private static final List<Demolition> DEMOLITIONS = new ArrayList<>();

    private LegionOps() {
    }

    private static Grid grid(boolean fort) {
        return GRIDS.computeIfAbsent(fort ? 1 : 0, k -> k == 1 ? OutpostDesign.build(FORT_R, true) : OutpostDesign.build(OUTPOST_R, false));
    }

    // ------------------------------------------------------------------ bases

    public static List<BlockPos> bases(Campaign c) {
        List<BlockPos> out = new ArrayList<>();
        for (Map.Entry<String, BlockPos> e : c.positions.entrySet()) {
            if (e.getKey().startsWith("base.") && !c.hasFlag("base_destruida." + e.getValue().asLong())) {
                out.add(e.getValue());
            }
        }
        return out;
    }

    @Nullable
    public static BlockPos nearestBase(Campaign c, Vec3 at) {
        BlockPos best = null;
        double bd = Double.MAX_VALUE;
        for (BlockPos b : bases(c)) {
            double d = b.distToCenterSqr(at);
            if (d < bd) {
                bd = d;
                best = b;
            }
        }
        return best;
    }

    public static boolean isFort(Campaign c, BlockPos b) {
        return c.hasFlag("base_fort." + b.asLong());
    }

    /**
     * Despacha una nave de carga que levantara una base cerca de {@code near} (a 0-40 bloques, en el sitio mas llano).
     * Devuelve el suelo de la base o null si no se encontro sitio.
     */
    @Nullable
    public static BlockPos launchBase(ServerLevel level, BlockPos near, boolean fortress) {
        Campaign c = Campaign.get(level);
        int r = fortress ? FORT_R : OUTPOST_R;
        BlockPos g = pickSite(level, c, near, r);
        if (g == null) {
            return null;
        }
        int n = c.inc("bases", 1);
        c.positions.put("base." + n, g);
        if (fortress) {
            c.setFlag("base_fort." + g.asLong());
        }
        c.setDirty();
        CargoShip ship = takeShip(level, g);
        if (ship == null) {
            // sin nave: la base se imprime sola con su haz
            startBuild(level, g, fortress);
            GARRISON_DUE.put(g.asLong(), level.getGameTime() + 400L);
            return g;
        }
        Vec3 hover = new Vec3(g.getX() + 0.5D, g.getY() + (fortress ? 40.0D : 30.0D), g.getZ() + 0.5D);
        Vec3 land = new Vec3(g.getX() + 0.5D, g.getY(), g.getZ() + 3.0D);
        ship.dispatch(exitPoint(level, ship), hover, land, g, fortress, true, true, 0, 180.0F);
        GARRISON_DUE.put(g.asLong(), level.getGameTime() + 6000L);
        return g;
    }

    @Nullable
    private static BlockPos pickSite(ServerLevel level, Campaign c, BlockPos near, int r) {
        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int attempt = 0; attempt < 16; attempt++) {
            double ang = level.random.nextDouble() * Math.PI * 2.0D;
            double dist = attempt == 0 ? 0.0D : level.random.nextDouble() * 40.0D;
            int x = Mth.floor(near.getX() + Math.cos(ang) * dist);
            int z = Mth.floor(near.getZ() + Math.sin(ang) * dist);
            if (!level.hasChunkAt(new BlockPos(x, 64, z))) {
                continue;
            }
            BlockPos g = new BlockPos(x, (int) Fx.groundY(level, x + 0.5D, z + 0.5D), z);
            boolean bad = Pillars.inSafeZone(level, Vec3.atCenterOf(g));
            for (BlockPos b : bases(c)) {
                if (b.distSqr(g) < 55.0D * 55.0D) {
                    bad = true;
                    break;
                }
            }
            if (bad) {
                continue;
            }
            int score = roughness(level, g, r);
            if (score < bestScore) {
                bestScore = score;
                best = g;
            }
            if (score <= 3) {
                break;
            }
        }
        return best;
    }

    private static int roughness(ServerLevel level, BlockPos g, int r) {
        int min = g.getY();
        int max = g.getY();
        int penalty = 0;
        if (!level.getBlockState(g.below()).getFluidState().isEmpty()) {
            penalty += 100;
        }
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0D;
            for (double k : new double[]{0.5D, 0.95D}) {
                int sx = g.getX() + (int) Math.round(Math.cos(a) * r * k);
                int sz = g.getZ() + (int) Math.round(Math.sin(a) * r * k);
                if (!level.hasChunkAt(new BlockPos(sx, 64, sz))) {
                    penalty += 20;
                    continue;
                }
                int y = (int) Fx.groundY(level, sx + 0.5D, sz + 0.5D);
                min = Math.min(min, y);
                max = Math.max(max, y);
                if (!level.getBlockState(new BlockPos(sx, y - 1, sz)).getFluidState().isEmpty()) {
                    penalty += 10;
                }
            }
        }
        return max - min + penalty;
    }

    /** Punto del hangar del destructor (local de diseno) en el mundo. */
    private static Vec3 shipLocal(DestroyerEntity ship, int x, int y, int z) {
        return ShipTransform.toWorld(ship.position(), ship.getYRot(), ShipTransform.cellFloor(new int[]{x, y, z}));
    }

    private static Vec3 exitPoint(ServerLevel level, CargoShip ship) {
        DestroyerEntity d = DestroyerEntity.find(level);
        if (d != null && ship.position().distanceTo(d.position()) < 200.0D) {
            return shipLocal(d, 0, ShipDesign.DOCK_Y - 22, ShipDesign.DOCK_Z[1]);
        }
        return ship.position();
    }

    /** Una nave de carga atracada en el hangar o, si no hay, una nueva que llega del cielo. */
    @Nullable
    private static CargoShip takeShip(ServerLevel level, BlockPos target) {
        DestroyerEntity d = DestroyerEntity.find(level);
        if (d != null && d.getState() == DestroyerEntity.ANCHORED) {
            AABB hangar = new AABB(shipLocal(d, -14, ShipDesign.DOCK_Y - 4, ShipDesign.HANGAR_Z0),
                    shipLocal(d, 14, ShipDesign.DOCK_Y + 10, ShipDesign.HANGAR_Z1));
            List<CargoShip> docked = level.getEntitiesOfClass(CargoShip.class, hangar, CargoShip::isDocked);
            if (!docked.isEmpty()) {
                return docked.get(level.random.nextInt(docked.size()));
            }
        }
        return fromSky(level, Vec3.atCenterOf(target), d);
    }

    /** Nave de carga nueva en el cielo, a 70-110 bloques del objetivo (hacia la nave nodriza si existe). */
    @Nullable
    private static CargoShip fromSky(ServerLevel level, Vec3 target, @Nullable DestroyerEntity d) {
        double ang = level.random.nextDouble() * Math.PI * 2.0D;
        if (d != null) {
            ang = Math.atan2(d.getZ() - target.z, d.getX() - target.x);
        }
        for (double dist : new double[]{110.0D, 70.0D, 40.0D}) {
            double x = target.x + Math.cos(ang) * dist;
            double z = target.z + Math.sin(ang) * dist;
            if (level.isLoaded(BlockPos.containing(x, 80.0D, z))) {
                CargoShip ship = ModEntities.CARGO_SHIP.get().create(level);
                if (ship == null) {
                    return null;
                }
                double y = Math.min(level.getMaxBuildHeight() - 20, target.y + 70.0D);
                ship.moveTo(x, y, z, 0.0F, 0.0F);
                ship.setDock(new Vec3(x, y, z));
                ship.mission = "sky";
                level.addFreshEntity(ship);
                return ship;
            }
        }
        return null;
    }

    public static long onCargoArrived(ServerLevel level, CargoShip ship) {
        return startBuild(level, ship.getBase(), ship.isFort());
    }

    private static long startBuild(ServerLevel level, BlockPos g, boolean fort) {
        Builds.Options o = new Builds.Options();
        o.order = Builds.Order.Y_RISE;
        o.perTick = fort ? 260 : 120;
        o.support = true;
        o.progress = (lvl, front, f) -> {
            if (lvl.getGameTime() % 3 == 0) {
                Fx.ringFar(lvl, new Vec3(g.getX() + 0.5D, g.getY() + front + 0.5D, g.getZ() + 0.5D),
                        (fort ? FORT_R : OUTPOST_R) + 1.0D, fort ? 36 : 24, Fx.BRIGHT, lvl.getGameTime() * 0.1D);
            }
        };
        o.done = () -> onBaseBuilt(level, g, fort);
        return Builds.start(level, grid(fort), g.getX(), g.getY(), g.getZ(), o);
    }

    private static void onBaseBuilt(ServerLevel level, BlockPos g, boolean fort) {
        Vec3 c = new Vec3(g.getX() + 0.5D, g.getY() + 1.0D, g.getZ() + 0.5D);
        Fx.burstFar(level, Fx.BRIGHT, c, 70, fort ? 8.0D : 4.0D, 0.4D);
        Fx.sound(level, c, SoundEvents.BEACON_ACTIVATE, 4.0F, 0.8F);
        int r = fort ? FORT_R : OUTPOST_R;
        // torreta de defensa en el techo y un caza aparcado fuera del muro
        int roof = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, g.getX(), g.getZ());
        TurbolaserEntity.create(level, new Vec3(g.getX() + 0.5D, roof, g.getZ() + 0.5D), TurbolaserEntity.DEFENSE, false, 0.0F);
        int fx = g.getX() + r + 5;
        int fz = g.getZ();
        if (level.isLoaded(new BlockPos(fx, 64, fz))) {
            int fy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, fx, fz);
            FighterEntity.spawn(level, new Vec3(fx + 0.5D, fy + 0.1D, fz + 0.5D), 90.0F, false);
        }
        // decodificador: haz verde al suelo
        Vfx.pillar(level, c, 40.0F, Vfx.GREEN, 200);
        GARRISON_DUE.putIfAbsent(g.asLong(), level.getGameTime() + GARRISON_FALLBACK);
    }

    public static void onCargoLanded(ServerLevel level, CargoShip ship) {
        if (ship.isReleased()) {
            return;
        }
        ship.markReleased();
        Vec3 rear = ship.local(0.0D, 0.0D, -7.8D);
        String m = ship.mission;
        if (m.startsWith("patrol:")) {
            String[] p = m.split(":");
            Rank leader = Rank.valueOf(p[1]);
            int n = Integer.parseInt(p[2]);
            LivingEntity target = null;
            try {
                target = level.getServer().getPlayerList().getPlayer(UUID.fromString(p[3]));
            } catch (RuntimeException ignored) {
            }
            List<LooreMob> sq = squad(level, rear, ship.getY(), leader, n, target);
            if (p.length > 4) {
                for (LooreMob mob : sq) {
                    mob.getPersistentData().putBoolean(p[4], true);
                }
            }
        } else if (m.equals("razako")) {
            RazakoEntity rz = ModEntities.RAZAKO.get().create(level);
            if (rz != null) {
                placeSafe(level, rz, rear.x, ship.getY(), rear.z, ship.getYRot() + 180.0F);
                level.addFreshEntity(rz);
                Campaign.get(level).entities.put("razako", rz.getUUID());
                Campaign.get(level).setDirty();
            }
        } else if (m.isEmpty() || m.equals("sky")) {
            GARRISON_DUE.remove(ship.getBase().asLong());
            squad(level, rear, ship.getY(), ship.isFort() ? Rank.COMANDANTE : Rank.SARGENTO, ship.isFort() ? 6 : 3, null);
            if (ship.isFort()) {
                squad(level, rear.add(3, 0, -3), ship.getY(), Rank.SENOR_GUERRA, 0, null);
            }
        }
    }

    // ------------------------------------------------------------------ tropas

    /** Lider + soldados en un punto (hueco libre mas cercano). Si hay objetivo, van a por el. */
    public static List<LooreMob> squad(ServerLevel level, Vec3 at, double groundY, Rank leader, int soldiers, @Nullable LivingEntity target) {
        List<LooreMob> out = new ArrayList<>();
        EntityType<? extends LooreMob> lt = leader == Rank.SENOR_GUERRA || leader == Rank.GUARDIA_NEXO
                ? ModEntities.LOREE_WARLORD.get() : ModEntities.LOREE_COMMANDER.get();
        LooreMob l = lt.create(level);
        if (l != null) {
            placeSafe(level, l, at.x, groundY, at.z - 2.5D, 0.0F);
            l.setRank(leader);
            level.addFreshEntity(l);
            out.add(l);
        }
        for (int i = 0; i < soldiers; i++) {
            Droid d = ModEntities.DROID.get().create(level);
            if (d == null) {
                continue;
            }
            double a = (i - (soldiers - 1) / 2.0D) * 1.6D;
            placeSafe(level, d, at.x + a, groundY, at.z + (i % 2) * 1.5D, i * 40.0F);
            d.setRank(Rank.SOLDADO);
            level.addFreshEntity(d);
            out.add(d);
        }
        for (LooreMob m : out) {
            m.setPersistenceRequired();
            if (target != null) {
                m.setTarget(target);
            }
        }
        Fx.burst(level, Fx.CODE, new Vec3(at.x, groundY + 1.0D, at.z), 30, 1.5D, 0.2D);
        return out;
    }

    public static void placeSafe(ServerLevel level, Mob mob, double x, double y, double z, float yaw) {
        for (int ring = 0; ring <= 6; ring++) {
            int steps = Math.max(1, ring * 6);
            for (int k = 0; k < steps; k++) {
                double ang = k * (Math.PI * 2.0D / steps);
                double px = x + Math.cos(ang) * ring;
                double pz = z + Math.sin(ang) * ring;
                for (int dy = 0; dy <= 3; dy++) {
                    AABB box = mob.getType().getDimensions().makeBoundingBox(px, y + dy, pz);
                    if (level.noCollision(box) && !level.containsAnyLiquid(box)) {
                        mob.moveTo(px, y + dy, pz, yaw, 0.0F);
                        return;
                    }
                }
            }
        }
        mob.moveTo(x, y + 3.0D, z, yaw, 0.0F);
    }

    /** Suelo seguro a {@code min}-{@code max} bloques del punto, en una direccion al azar. */
    public static Vec3 groundAround(ServerLevel level, Vec3 at, double min, double max) {
        Vec3 best = null;
        for (int i = 0; i < 12; i++) {
            double ang = level.random.nextDouble() * Math.PI * 2.0D;
            double d = min + level.random.nextDouble() * (max - min);
            int x = Mth.floor(at.x + Math.cos(ang) * d);
            int z = Mth.floor(at.z + Math.sin(ang) * d);
            if (!level.hasChunkAt(new BlockPos(x, 64, z))) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos below = new BlockPos(x, y - 1, z);
            Vec3 p = new Vec3(x + 0.5D, y, z + 0.5D);
            if (level.getBlockState(below).getFluidState().isEmpty()) {
                return p;
            }
            best = p;
        }
        return best != null ? best : at;
    }

    /** Una nave de carga baja a 40-80 bloques del objetivo con lider y soldados, que van a por el. */
    @Nullable
    public static CargoShip dropPatrol(ServerLevel level, ServerPlayer target, Rank leader, int soldiers) {
        Vec3 land = groundAround(level, target.position(), 40.0D, 80.0D);
        return landShip(level, land, "patrol:" + leader.name() + ":" + soldiers + ":" + target.getUUID());
    }

    /** Nave de carga que llega del cielo y aterriza en {@code land} con la mision indicada. */
    @Nullable
    public static CargoShip landShip(ServerLevel level, Vec3 land, String mission) {
        CargoShip ship = fromSky(level, land, DestroyerEntity.find(level));
        if (ship == null) {
            return null;
        }
        ship.mission = mission;
        Vec3 hover = land.add(0.0D, 24.0D, 0.0D);
        float yaw = (float) Math.toDegrees(Math.atan2(-(land.x - ship.getX()), land.z - ship.getZ()));
        ship.dispatch(ship.position(), hover, land, BlockPos.containing(land), false, true, false, 0, yaw);
        return ship;
    }

    /** Cazas con piloto IA pasan sobre el objetivo (salen del hangar si la nave esta cerca). */
    public static void flyover(ServerLevel level, ServerPlayer target, int count) {
        DestroyerEntity d = DestroyerEntity.find(level);
        if (d != null && d.distanceTo(target) < 400.0F) {
            ShipArmament.launchFighters(level, d, count, target);
            return;
        }
        double ang = level.random.nextDouble() * Math.PI * 2.0D;
        for (int i = 0; i < count; i++) {
            Vec3 at = target.position().add(Math.cos(ang) * 140.0D + i * 6.0D, 50.0D + i * 3.0D, Math.sin(ang) * 140.0D);
            if (!level.isLoaded(BlockPos.containing(at))) {
                continue;
            }
            FighterEntity f = FighterEntity.spawn(level, at, (float) Math.toDegrees(Math.atan2(Math.cos(ang), -Math.sin(ang))), true);
            f.setPatrolCenter(target.blockPosition().above(20));
        }
        level.playSound(null, target.blockPosition(), ModSounds.FIGHTER_PASS.get(), SoundSource.HOSTILE, 3.0F, 1.0F);
    }

    // ------------------------------------------------------------------ destruccion

    /** Explosiones por toda la huella, mata a la guarnicion y deja escombros (en ~60 ticks). */
    public static void destroyBase(ServerLevel level, BlockPos g) {
        Campaign c = Campaign.get(level);
        boolean fort = isFort(c, g);
        c.setFlag("base_destruida." + g.asLong());
        int r = fort ? FORT_R : OUTPOST_R;
        for (Mob m : level.getEntitiesOfClass(Mob.class, new AABB(g).inflate(r + 6, 30, r + 6),
                e -> e instanceof LooreMob && !(e instanceof RazakoEntity))) {
            m.kill();
        }
        for (TurbolaserEntity t : level.getEntitiesOfClass(TurbolaserEntity.class, new AABB(g).inflate(r + 6, 30, r + 6))) {
            t.discard();
            Vfx.explosion(level, t.position(), 2.5F);
        }
        DEMOLITIONS.add(new Demolition(g, fort, new int[]{grid(fort).maxY()}));
        level.playSound(null, g, ModSounds.EXPLOSION_BIG.get(), SoundSource.HOSTILE, 8.0F, 0.6F);
    }

    private static BlockState debris(ServerLevel level) {
        int k = level.random.nextInt(3);
        return (k == 0 ? Blocks.GRAVEL : k == 1 ? Blocks.BLACKSTONE : Blocks.COARSE_DIRT).defaultBlockState();
    }

    public static void tick(ServerLevel level) {
        long now = level.getGameTime();
        if (!GARRISON_DUE.isEmpty()) {
            Iterator<Map.Entry<Long, Long>> it = GARRISON_DUE.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, Long> e = it.next();
                if (e.getValue() <= now) {
                    it.remove();
                    BlockPos g = BlockPos.of(e.getKey());
                    if (level.isLoaded(g) && !Campaign.get(level).hasFlag("base_destruida." + g.asLong())) {
                        squad(level, new Vec3(g.getX() + 0.5D, g.getY(), g.getZ() + 7.5D), g.getY(), Rank.SARGENTO, 3, null);
                    }
                }
            }
        }
        Iterator<Demolition> it = DEMOLITIONS.iterator();
        while (it.hasNext()) {
            Demolition d = it.next();
            Grid grid = grid(d.fort());
            int y = d.layer()[0];
            if (y < grid.y0) {
                it.remove();
                continue;
            }
            d.layer()[0] = y - 1;
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
            for (int x = grid.x0; x <= grid.maxX(); x++) {
                for (int z = grid.z0; z <= grid.maxZ(); z++) {
                    byte mat = grid.get(x, y, z);
                    if (mat == Mat.NONE || mat == Mat.AIR) {
                        continue;
                    }
                    m.set(d.g().getX() + x, d.g().getY() + y, d.g().getZ() + z);
                    BlockState s = y <= 0 || level.random.nextInt(4) == 0 ? debris(level) : Blocks.AIR.defaultBlockState();
                    level.setBlock(m, s, Block.UPDATE_CLIENTS);
                }
            }
            if (y % 2 == 0) {
                for (int i = 0; i < 3; i++) {
                    Vec3 p = new Vec3(d.g().getX() + grid.x0 + level.random.nextInt(grid.nx), d.g().getY() + y,
                            d.g().getZ() + grid.z0 + level.random.nextInt(grid.nz));
                    Vfx.explosion(level, p, 2.0F + level.random.nextFloat() * 2.0F);
                }
                level.playSound(null, d.g().above(y), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 4.0F, 0.7F);
            }
        }
    }

    public static void reset() {
        GARRISON_DUE.clear();
        DEMOLITIONS.clear();
    }
}
