package com.keinloree.test;

import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.ship.DestroyerEntity;
import com.keinloree.ship.ShipBlocks;
import com.keinloree.ship.ShipCrew;
import com.keinloree.ship.ShipData;
import com.keinloree.ship.ShipTransform;
import com.keinloree.world.design.Grid;
import com.keinloree.world.design.Mat;
import com.keinloree.world.design.ShipDesign;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

/** Escenarios de servidor del destructor: ship_transform (coherencia de ShipTransform) y ship_cycle (ciclo completo). */
public final class ShipTests {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ShipTests() {
    }

    public static void register() {
        AutoTest.register("ship_transform", s -> s.wait(60).run(server -> start(new TransformTest())).wait(1000000));
        AutoTest.register("ship_cycle", s -> s.wait(60).run(server -> start(new CycleTest())).wait(1000000));
    }

    private static void start(Object test) {
        MinecraftForge.EVENT_BUS.register(test);
    }

    private static void log(String fmt, Object... args) {
        LOGGER.info("[AutoTest] " + fmt, args);
    }

    private static void check(List<String> fails, boolean ok, String what) {
        log("{} {}", ok ? "OK" : "FALLO", what);
        if (!ok) {
            fails.add(what);
        }
    }

    /** Diferencias entre el mundo y el diseno colocado en anchor/r: [solidas mal, aire mal]. */
    static int[] verifyPlaced(ServerLevel lv, BlockPos anchor, int r) {
        Grid g = ShipDesign.grid();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int badSolid = 0;
        int badAir = 0;
        for (int x = g.x0; x <= g.maxX(); x++) {
            for (int y = g.y0; y <= g.maxY(); y++) {
                for (int z = g.z0; z <= g.maxZ(); z++) {
                    byte m = g.get(x, y, z);
                    if (m == Mat.NONE) {
                        continue;
                    }
                    ShipTransform.blockFor(p, anchor, r, x, y, z);
                    BlockState s = lv.getBlockState(p);
                    if (m == Mat.AIR) {
                        if (!s.isAir()) {
                            badAir++;
                        }
                    } else if (s != ShipTransform.stateFor(m, r)) {
                        if (badSolid < 3) {
                            log("  celda {} {} {} mat {} -> {} es {} (esperado {})", x, y, z, m, p, s, ShipTransform.stateFor(m, r));
                        }
                        badSolid++;
                    }
                }
            }
        }
        return new int[]{badSolid, badAir};
    }

    /** Celdas solidas del diseno en anchor/r que siguen en el mundo, sin contar las que pisa otra colocacion. */
    static int remaining(ServerLevel lv, BlockPos anchor, int r, BlockPos otherAnchor, int otherR) {
        Grid g = ShipDesign.grid();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        Vec3 other = otherAnchor == null ? null : ShipTransform.entityPos(otherAnchor);
        int n = 0;
        for (int x = g.x0; x <= g.maxX(); x++) {
            for (int y = g.y0; y <= g.maxY(); y++) {
                for (int z = g.z0; z <= g.maxZ(); z++) {
                    byte m = g.get(x, y, z);
                    if (m == Mat.NONE || m == Mat.AIR) {
                        continue;
                    }
                    ShipTransform.blockFor(p, anchor, r, x, y, z);
                    if (other != null) {
                        Vec3 l = ShipTransform.toLocal(other, ShipTransform.yawFor(otherR), Vec3.atLowerCornerOf(p).add(0.5D, 0.5D, 0.5D));
                        int lx = Mth.floor(l.x + 0.5D) + ShipTransform.HX;
                        int ly = Mth.floor(l.y) + ShipTransform.HY;
                        int lz = Mth.floor(l.z + 0.5D) + ShipTransform.HZ;
                        if (g.get(lx, ly, lz) != Mat.NONE) {
                            continue;
                        }
                    }
                    if (!lv.getBlockState(p).isAir()) {
                        n++;
                    }
                }
            }
        }
        return n;
    }

    // ------------------------------------------------------------------ ship_transform

    public static final class TransformTest {
        private int r;
        private final List<String> fails = new ArrayList<>();

        @SubscribeEvent
        public void onTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            MinecraftServer server = event.getServer();
            ServerLevel lv = server.overworld();
            try {
                if (r == 0) {
                    log("ship_transform: banda del timon Y={}..{} (orbita {}), limites solidos {}", ShipTransform.MIN_ANCHOR_Y,
                            ShipTransform.MAX_ANCHOR_Y, ShipTransform.DEFAULT_ANCHOR_Y, java.util.Arrays.toString(ShipTransform.solidBounds()));
                    int[] b = ShipTransform.solidBounds();
                    check(fails, ShipTransform.DEFAULT_ANCHOR_Y + b[1] - ShipTransform.HY >= 200
                            && ShipTransform.MAX_ANCHOR_Y + b[4] - ShipTransform.HY <= 318, "banda de altitud (base >= 200, techo <= 318)");
                }
                if (r < 4) {
                    runRotation(lv, r);
                    r++;
                    return;
                }
                log("ship_transform: {} ({} fallos)", fails.isEmpty() ? "TODO OK" : "HAY FALLOS", fails.size());
                MinecraftForge.EVENT_BUS.unregister(this);
                server.halt(false);
            } catch (Exception e) {
                LOGGER.error("[AutoTest] ship_transform: excepcion", e);
                MinecraftForge.EVENT_BUS.unregister(this);
                server.halt(false);
            }
        }

        private void runRotation(ServerLevel lv, int r) {
            BlockPos spawn = lv.getSharedSpawnPos();
            BlockPos anchor = new BlockPos(spawn.getX() + 7, ShipTransform.DEFAULT_ANCHOR_Y, spawn.getZ() - 3);
            float yaw = ShipTransform.yawFor(r);
            Vec3 ep = ShipTransform.entityPos(anchor);
            Grid g = ShipDesign.grid();
            int badRot = 0;
            int badCont = 0;
            int badInv = 0;
            int cells = 0;
            for (int x = g.x0; x <= g.maxX(); x++) {
                for (int y = g.y0; y <= g.maxY(); y++) {
                    for (int z = g.z0; z <= g.maxZ(); z++) {
                        if (g.get(x, y, z) == Mat.NONE) {
                            continue;
                        }
                        cells++;
                        BlockPos p = ShipTransform.blockFor(anchor, r, x, y, z);
                        // independiente: rotacion de vanilla
                        BlockPos v = new BlockPos(x - ShipTransform.HX, y - ShipTransform.HY, z - ShipTransform.HZ)
                                .rotate(ShipTransform.rotationFor(r)).offset(anchor);
                        if (!v.equals(p)) {
                            badRot++;
                        }
                        Vec3 w = ShipTransform.toWorld(ep, yaw, ShipTransform.local(x, y, z, 0.25D, 0.5D, 0.75D));
                        if (!BlockPos.containing(w).equals(p)) {
                            badCont++;
                        }
                        Vec3 back = ShipTransform.toLocal(ep, yaw, w);
                        if (back.distanceTo(ShipTransform.local(x, y, z, 0.25D, 0.5D, 0.75D)) > 1.0E-6D) {
                            badInv++;
                        }
                    }
                }
            }
            check(fails, badRot == 0, "r=" + r + " blockFor = rotacion vanilla (" + cells + " celdas, " + badRot + " distintas)");
            check(fails, badCont == 0, "r=" + r + " toWorld continuo cae en la celda de blockFor (" + badCont + " distintas)");
            check(fails, badInv == 0, "r=" + r + " toLocal(toWorld(p)) = p (" + badInv + " distintas)");
            Vec3 fwd = ShipTransform.toWorld(ep, yaw, new Vec3(0, 0, -1)).subtract(ep);
            double rad = Math.toRadians(yaw);
            check(fails, fwd.distanceTo(new Vec3(-Math.sin(rad), 0, Math.cos(rad))) < 1.0E-6D,
                    "r=" + r + " proa (-Z local) = vector de vista del yaw " + yaw);
            // colocar con ShipBlocks y comprobar
            long t0 = System.nanoTime();
            ShipBlocks.Job place = new ShipBlocks.Job(true, anchor, r, false, 0);
            place.finish(lv);
            long t1 = System.nanoTime();
            int[] bad = verifyPlaced(lv, anchor, r);
            check(fails, bad[0] == 0 && bad[1] == 0, "r=" + r + " bloques colocados = diseno rotado (solidas mal " + bad[0]
                    + ", aire mal " + bad[1] + ", " + place.written() + " escritos en " + (t1 - t0) / 1000000 + " ms)");
            BlockState helm = lv.getBlockState(ShipTransform.blockFor(anchor, r, ShipDesign.HELM_X, ShipDesign.HELM_Y, ShipDesign.HELM_Z));
            check(fails, helm == ShipTransform.stateFor(Mat.HELM, r), "r=" + r + " timon en la celda de anclaje: " + helm);
            check(fails, ShipTransform.worldAABB(anchor, r).contains(Vec3.atCenterOf(ShipTransform.blockFor(anchor, r,
                    ShipDesign.ENGINES[0][0], ShipDesign.ENGINES[0][1], ShipDesign.L))), "r=" + r + " worldAABB contiene la popa");
            ShipBlocks.Job erase = new ShipBlocks.Job(false, anchor, r, false, 0);
            erase.finish(lv);
            int left = remaining(lv, anchor, r, null, 0);
            check(fails, left == 0, "r=" + r + " borrado completo (" + left + " restantes, " + erase.written() + " borrados)");
        }
    }

    // ------------------------------------------------------------------ ship_cycle

    public static final class CycleTest {
        private int phase;
        private int ticks;
        private int total;
        private final List<String> fails = new ArrayList<>();
        private final List<String> times = new ArrayList<>();
        private BlockPos oldAnchor;
        private int oldR;
        private BlockPos goalAnchor;
        private int goalR;
        private boolean arrived;
        private final LongSet forced = new LongOpenHashSet();

        private void next(String what) {
            times.add(what + ": " + ticks + " ticks");
            log("ship_cycle: {} en {} ticks", what, ticks);
            phase++;
            ticks = 0;
        }

        /** Mantiene cargados los chunks bajo la nave (como si hubiera un jugador cerca). */
        private void keepLoaded(ServerLevel lv, DestroyerEntity s) {
            LongSet want = DestroyerEntity.chunksOf(ShipTransform.worldAABB(s.position(), s.getYRot()).inflate(2.0D));
            for (long l : forced.toLongArray()) {
                if (!want.contains(l)) {
                    lv.setChunkForced(ChunkPos.getX(l), ChunkPos.getZ(l), false);
                    forced.remove(l);
                }
            }
            for (long l : want) {
                if (forced.add(l) | !lv.getForcedChunks().contains(l)) {
                    lv.setChunkForced(ChunkPos.getX(l), ChunkPos.getZ(l), true);
                }
            }
        }

        @SubscribeEvent
        public void onTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            MinecraftServer server = event.getServer();
            ServerLevel lv = server.overworld();
            ticks++;
            total++;
            try {
                if (total > 24000) {
                    check(fails, false, "tiempo total agotado en la fase " + phase);
                    finish(server);
                    return;
                }
                tickPhase(server, lv);
            } catch (Exception e) {
                LOGGER.error("[AutoTest] ship_cycle: excepcion", e);
                fails.add("excepcion " + e);
                finish(server);
            }
        }

        private void tickPhase(MinecraftServer server, ServerLevel lv) {
            DestroyerEntity s = DestroyerEntity.find(lv);
            if (s != null && phase < 7) {
                keepLoaded(lv, s);
            }
            switch (phase) {
                case 0 -> {
                    if (ShipData.get(server).ship != null) {
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "keinloree ship remove");
                    }
                    BlockPos spawn = lv.getSharedSpawnPos();
                    DestroyerEntity n = DestroyerEntity.arriveNear(lv, Vec3.atBottomCenterOf(spawn));
                    check(fails, n != null, "arrive sobre el spawn " + spawn.toShortString());
                    if (n == null) {
                        finish(server);
                        return;
                    }
                    log("ship_cycle: ancla {} r={} (salto desde {})", n.getAnchor().toShortString(), n.getRot(), n.blockPosition().toShortString());
                    next("arrive");
                }
                case 1 -> {
                    if (s != null && s.getState() == DestroyerEntity.ANCHORED) {
                        oldAnchor = s.getAnchor();
                        oldR = s.getRot();
                        next("llegada + materializacion hasta ANCHORED");
                        int[] bad = verifyPlaced(lv, oldAnchor, oldR);
                        check(fails, bad[0] == 0 && bad[1] == 0, "bloques en sitio tras la llegada (solidas mal " + bad[0] + ", aire mal " + bad[1] + ")");
                        check(fails, s.position().equals(ShipTransform.entityPos(oldAnchor)) && s.getYRot() == ShipTransform.yawFor(oldR),
                                "entidad en el centro de la celda del timon con yaw(r)");
                        int base = oldAnchor.getY() + ShipTransform.solidBounds()[1] - ShipTransform.HY;
                        check(fails, base >= 200, "base de la nave en Y=" + base + " (>= 200, sobre las nubes)");
                    } else if (ticks > 3000) {
                        check(fails, false, "no llego a ANCHORED (estado " + (s == null ? "sin nave" : DestroyerEntity.stateName(s.getState())) + ")");
                        finish(server);
                    }
                }
                case 2 -> {
                    // espera unos ticks a que la tripulacion este en el mundo
                    if (ticks < 40 || s == null) {
                        return;
                    }
                    int crew = ShipCrew.alive(lv, s);
                    check(fails, crew == s.crew().size() && crew >= 40, "tripulacion generada: " + crew + "/" + s.crew().size());
                    Entity kein = lv.getEntity(s.crew().get("kein"));
                    Vec3 post = ShipCrew.postWorld(s, "kein");
                    check(fails, kein instanceof KeinLooreEntity && post != null && kein.position().distanceTo(post) < 1.0D,
                            "Kein Loree en KEIN_POST");
                    check(fails, s.liftoff(), "liftoff aceptado");
                    next("espera a la tripulacion");
                }
                case 3 -> {
                    if (s != null && s.getState() == DestroyerEntity.FLYING) {
                        next("LIFTOFF (captura + borrado) hasta FLYING");
                        check(fails, s.getPassengers().size() >= 40, "pasajeros a bordo: " + s.getPassengers().size());
                        int left = remaining(lv, oldAnchor, oldR, null, 0);
                        check(fails, left == 0, "no quedan bloques tras el despegue (" + left + ")");
                        Vec3 target = s.position().add(60.0D, 0.0D, 0.0D);
                        goalR = Math.floorMod(oldR + 1, 4);
                        goalAnchor = ShipTransform.anchorAt(target);
                        check(fails, s.autopilotTo(target, ShipTransform.yawFor(goalR), () -> arrived = true), "autopilotTo aceptado");
                    } else if (ticks > 1200) {
                        check(fails, false, "no llego a FLYING");
                        finish(server);
                    }
                }
                case 4 -> {
                    if (s != null && s.getState() == DestroyerEntity.ALIGN) {
                        next("vuelo con piloto automatico (60 bloques, giro 90) hasta ALIGN");
                    } else if (ticks > 4000) {
                        check(fails, false, "el piloto automatico no llego (" + (s == null ? "sin nave" : s.position()) + ")");
                        finish(server);
                    }
                }
                case 5 -> {
                    if (s != null && s.getState() == DestroyerEntity.ANCHORING) {
                        next("ALIGN hasta ANCHORING");
                    } else if (ticks > 600) {
                        check(fails, false, "ALIGN no termino");
                        finish(server);
                    }
                }
                case 6 -> {
                    if (s != null && s.getState() == DestroyerEntity.ANCHORED) {
                        next("ANCHORING hasta ANCHORED");
                        check(fails, arrived, "callback de llegada del piloto automatico");
                        check(fails, s.getAnchor().equals(goalAnchor) && s.getRot() == goalR, "nueva ancla " + s.getAnchor().toShortString()
                                + " r=" + s.getRot() + " (esperada " + goalAnchor.toShortString() + " r=" + goalR + ")");
                        int[] bad = verifyPlaced(lv, s.getAnchor(), s.getRot());
                        check(fails, bad[0] == 0 && bad[1] == 0, "bloques en la nueva posicion rotada (solidas mal " + bad[0] + ", aire mal " + bad[1] + ")");
                        int left = remaining(lv, oldAnchor, oldR, s.getAnchor(), s.getRot());
                        check(fails, left == 0, "ningun bloque del diseno en la posicion antigua (" + left + ")");
                        check(fails, s.getPassengers().isEmpty(), "pasajeros liberados");
                        Entity kein = lv.getEntity(s.crew().get("kein"));
                        Vec3 post = ShipCrew.postWorld(s, "kein");
                        check(fails, kein != null && post != null && !kein.isPassenger() && kein.position().distanceTo(post) < 0.6D,
                                "Kein en su puesto tras el viaje (" + (kein == null ? "?" : kein.position()) + " vs " + post + ")");
                        s.destroy();
                    } else if (ticks > 1200) {
                        check(fails, false, "ANCHORING no termino");
                        finish(server);
                    }
                }
                case 7 -> {
                    if (s == null) {
                        next("DESTROYED hasta discard");
                        int left = remaining(lv, goalAnchor, goalR, null, 0);
                        check(fails, left == 0, "no quedan bloques tras la destruccion (" + left + ")");
                        check(fails, ShipData.get(server).ship == null, "registro de la nave limpio");
                        finish(server);
                    } else if (ticks > 3000) {
                        check(fails, false, "la destruccion no termino");
                        finish(server);
                    }
                }
                default -> finish(server);
            }
        }

        private void finish(MinecraftServer server) {
            MinecraftForge.EVENT_BUS.unregister(this);
            for (long l : forced) {
                server.overworld().setChunkForced(ChunkPos.getX(l), ChunkPos.getZ(l), false);
            }
            log("ship_cycle: tiempos {}", times);
            log("ship_cycle: {} ({} fallos: {})", fails.isEmpty() ? "TODO OK" : "HAY FALLOS", fails.size(), fails);
            server.halt(false);
        }
    }
}
