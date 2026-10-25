package com.keinloree.world;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.CargoShip;
import com.keinloree.entity.Droid;
import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.entity.LooreCommander;
import com.keinloree.entity.LooreWarlord;
import com.keinloree.util.Fx;
import com.keinloree.world.design.Grid;
import com.keinloree.world.design.OutpostDesign;
import com.keinloree.world.design.ShipDesign;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Historia de la mision, guardada en el mundo. Etapas: nave, bases, fortaleza y combate.
 * La nave nodriza se "imprime" en el cielo; de su hangar salen naves de carga que vuelan, a la vista de
 * todos, hasta las coordenadas donde se levantan las bases militares.
 * Finales: vencer a Kein Loree, dejar que el mundo sea consumido, o perecer.
 */
public class LooreStory extends SavedData {

    public static final int IDLE = 0;
    public static final int SHIP = 1;
    public static final int BASES = 2;
    public static final int FORTRESS = 3;
    public static final int FIGHT = 4;

    public static final int END_NONE = 0;
    public static final int END_RESIST = 1;
    public static final int END_CONSUMED = 2;
    public static final int END_PERISHED = 3;

    private static final int FIRST_BASE_DELAY = 300;
    private static final int BASE_INTERVAL = 2400;
    private static final int OUTPOSTS = 5;
    private static final int OUTPOST_R = 12;
    private static final int FORT_R = 24;
    /** Altura del origen de una nave de carga atracada (el vientre del casco queda 1.5 mas arriba). */
    private static final double DOCK_ORIGIN_Y = -5.5D;
    private static final String DATA_ID = "keinloree_story";
    private static final SavedData.Factory<LooreStory> FACTORY =
            new SavedData.Factory<>(LooreStory::new, LooreStory::load, null);

    private static Grid shipGrid;
    private static final Map<Integer, Grid> BASE_GRIDS = new HashMap<>();

    private record Scene(long at, String title, String sub, String chat) {
    }

    /** Lineas del guion programadas (no se guardan: si se reinicia el servidor, se pierden). */
    private final List<Scene> scenes = new ArrayList<>();
    /** Guarniciones pendientes por si la nave de carga no llega a aterrizar: posicion de la base -> tick limite. */
    private final Map<Long, Long> garrisonDue = new HashMap<>();
    private long timer;
    private boolean shipBuilding;
    private boolean fortDispatched;

    private int stage = IDLE;
    private int ending = END_NONE;
    private int basesBuilt;
    private int nextBaseIn = BASE_INTERVAL;
    private int shipX;
    private int shipY;
    private int shipZ;
    private UUID bossId;
    private final List<BlockPos> bases = new ArrayList<>();

    public LooreStory() {
    }

    public static LooreStory get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
    }

    public static LooreStory load(CompoundTag tag, HolderLookup.Provider registries) {
        LooreStory s = new LooreStory();
        s.stage = tag.getInt("stage");
        s.ending = tag.getInt("ending");
        s.basesBuilt = tag.getInt("bases");
        s.nextBaseIn = tag.getInt("nextBase");
        s.shipX = tag.getInt("shipX");
        s.shipY = tag.getInt("shipY");
        s.shipZ = tag.getInt("shipZ");
        if (tag.hasUUID("boss")) {
            s.bossId = tag.getUUID("boss");
        }
        for (long l : tag.getLongArray("basePos")) {
            s.bases.add(BlockPos.of(l));
        }
        return s;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("stage", this.stage);
        tag.putInt("ending", this.ending);
        tag.putInt("bases", this.basesBuilt);
        tag.putInt("nextBase", this.nextBaseIn);
        tag.putInt("shipX", this.shipX);
        tag.putInt("shipY", this.shipY);
        tag.putInt("shipZ", this.shipZ);
        if (this.bossId != null) {
            tag.putUUID("boss", this.bossId);
        }
        long[] packed = new long[this.bases.size()];
        for (int i = 0; i < packed.length; i++) {
            packed[i] = this.bases.get(i).asLong();
        }
        tag.putLongArray("basePos", packed);
        return tag;
    }

    // ------------------------------------------------------------------ geometria de la nave en el mundo

    /** Punto del mundo para unas coordenadas locales de la nave (x lateral, y vertical, z de proa a popa). */
    private Vec3 shipPoint(double lx, double ly, double lz) {
        return new Vec3(this.shipX + lx, this.shipY + ly, this.shipZ + (lz - ShipDesign.ZC));
    }

    private Vec3 throne() {
        return this.shipPoint(ShipDesign.THRONE_X + 0.5D, ShipDesign.THRONE_Y - 0.32D, ShipDesign.THRONE_Z + 0.5D);
    }

    private Vec3 dockPoint(int slot) {
        return this.shipPoint(0.5D, DOCK_ORIGIN_Y, ShipDesign.DOCK_Z[slot] + 0.5D);
    }

    private static Grid shipGrid() {
        if (shipGrid == null) {
            shipGrid = ShipDesign.build();
        }
        return shipGrid;
    }

    private static Grid baseGrid(boolean fort) {
        return BASE_GRIDS.computeIfAbsent(fort ? 1 : 0,
                k -> k == 1 ? OutpostDesign.build(FORT_R, true) : OutpostDesign.build(OUTPOST_R, false));
    }

    // ------------------------------------------------------------------ inicio y ciclo

    /** Lo llama el Nucleo de Decodificacion: empieza la mision si no hay una en curso. */
    public static void startMission(ServerLevel level, ServerPlayer player) {
        ServerLevel overworld = level.getServer().overworld();
        LooreStory s = get(overworld);
        if (s.stage != IDLE) {
            player.sendSystemMessage(Component.literal("La misión ya está en curso.").withStyle(ChatFormatting.RED));
            return;
        }
        s.begin(overworld, player);
    }

    private void begin(ServerLevel overworld, ServerPlayer player) {
        this.stage = SHIP;
        this.ending = END_NONE;
        this.basesBuilt = 0;
        this.nextBaseIn = BASE_INTERVAL;
        this.shipX = player.getBlockX();
        this.shipZ = player.getBlockZ();
        int ground = (int) Fx.groundY(overworld, this.shipX + 0.5D, this.shipZ + 0.5D);
        this.shipY = Mth.clamp(ground + 125, 170, 245);
        this.bossId = null;
        this.bases.clear();
        this.garrisonDue.clear();
        this.fortDispatched = false;
        this.scenes.clear();
        this.timer = 0L;
        this.schedule(0L, "SEÑAL DETECTADA", "Núcleo de Decodificación activo",
                "Señal recibida. Este planeta es el siguiente en la red.");
        this.schedule(100L, null, null, "Decodificar un mundo es fácil. Resistirlo, no tanto.");
        this.schedule(200L, null, null, "Mi nave ya desciende sobre tu cielo.");
        this.startShipBuild(overworld);
        this.setDirty();
    }

    private void startShipBuild(ServerLevel level) {
        this.shipBuilding = true;
        Builds.Options o = new Builds.Options();
        o.order = Builds.Order.Z_SWEEP;
        o.perTick = 520;
        o.progress = (lvl, front, f) -> this.shipScanFx(lvl, front);
        o.done = () -> this.onShipBuilt(level);
        Builds.start(level, shipGrid(), this.shipX, this.shipY, this.shipZ - ShipDesign.ZC, o);
    }

    /** Linea de escaneo verde que recorre el casco mientras se imprime. */
    private void shipScanFx(ServerLevel level, int z) {
        if (z < 0 || z > ShipDesign.L) {
            return;
        }
        int w = ShipDesign.hw(z);
        for (int i = 0; i < 12; i++) {
            int x = level.random.nextInt(2 * w + 1) - w;
            int top = ShipDesign.topAt(Math.abs(x), z);
            if (top == Integer.MIN_VALUE) {
                continue;
            }
            Vec3 p = this.shipPoint(x + 0.5D, top + 1.5D, z + 0.5D);
            Fx.burstFar(level, level.random.nextBoolean() ? Fx.CODE : Fx.BRIGHT, p, 1, 0.4D, 0.03D);
        }
        if (this.timer % 3L == 0L) {
            Vec3 a = this.shipPoint(-w, ShipDesign.topAt(0, z) + 2.0D, z + 0.5D);
            Vec3 b = this.shipPoint(w, ShipDesign.topAt(0, z) + 2.0D, z + 0.5D);
            Fx.lineFar(level, a, b, Fx.GREEN, 3.0D);
        }
        if (this.timer % 40L == 0L) {
            Fx.sound(level, this.shipPoint(0.0D, 0.0D, z), SoundEvents.BEACON_AMBIENT, 6.0F, 0.5F);
        }
    }

    /** Cada tick del servidor: guion, bases y fortaleza. */
    public void tick(ServerLevel overworld) {
        if (this.stage == IDLE && this.scenes.isEmpty() && this.garrisonDue.isEmpty()) {
            return;
        }
        this.timer++;
        Iterator<Scene> it = this.scenes.iterator();
        while (it.hasNext()) {
            Scene s = it.next();
            if (s.at() <= this.timer) {
                it.remove();
                if (s.title() != null) {
                    title(overworld, s.title(), s.sub());
                }
                if (s.chat() != null) {
                    say(overworld, s.chat());
                }
            }
        }
        if (this.stage == SHIP && !this.shipBuilding) {
            // El servidor se reinicio a mitad de la obra: se retoma desde el principio.
            this.startShipBuild(overworld);
        }
        if (this.stage == BASES) {
            this.nextBaseIn--;
            if (this.nextBaseIn <= 0) {
                if (this.basesBuilt < OUTPOSTS) {
                    this.placeOutpost(overworld);
                } else {
                    this.placeFortress(overworld);
                }
            }
        }
        if (this.stage == FORTRESS && !this.fortDispatched) {
            this.nextBaseIn--;
            if (this.nextBaseIn <= 0) {
                this.placeFortress(overworld);
            }
        }
        if (!this.garrisonDue.isEmpty()) {
            Iterator<Map.Entry<Long, Long>> g = this.garrisonDue.entrySet().iterator();
            while (g.hasNext()) {
                Map.Entry<Long, Long> e = g.next();
                if (e.getValue() <= this.timer) {
                    g.remove();
                    BlockPos pos = BlockPos.of(e.getKey());
                    if (overworld.isLoaded(pos)) {
                        spawnGarrison(overworld, new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 7.5D),
                                pos.getY(), false);
                    }
                }
            }
        }
    }

    private void onShipBuilt(ServerLevel level) {
        this.shipBuilding = false;
        KeinLooreEntity boss = KeinLooreMod.KEIN_LOREE.get().create(level);
        if (boss != null) {
            Vec3 throne = this.throne();
            boss.moveTo(throne.x, throne.y, throne.z, 180.0F, 0.0F);
            boss.setLoreDormant(true);
            boss.setNoGravity(true);
            level.addFreshEntity(boss);
            this.bossId = boss.getUUID();
        }
        for (int i = 0; i < ShipDesign.DOCK_Z.length; i++) {
            this.spawnDocked(level, i);
        }
        this.stage = BASES;
        this.nextBaseIn = FIRST_BASE_DELAY;
        this.schedule(0L, "NAVE LOREE", "8 kilómetros de código sólido",
                "Mi trono está listo. Mis lacayos empiezan a decodificar tu mundo.");
        this.schedule(80L, null, null, "Mis naves de carga ya salen del hangar. Cada una llevará una base entera.");
        this.setDirty();
    }

    private CargoShip spawnDocked(ServerLevel level, int slot) {
        CargoShip ship = KeinLooreMod.CARGO_SHIP.get().create(level);
        if (ship == null) {
            return null;
        }
        Vec3 p = this.dockPoint(slot);
        ship.moveTo(p.x, p.y, p.z, 180.0F, 0.0F);
        ship.setDock(p);
        level.addFreshEntity(ship);
        return ship;
    }

    // ------------------------------------------------------------------ bases: eleccion del sitio y despacho

    private void placeOutpost(ServerLevel level) {
        BlockPos g = this.pickBase(level, OUTPOST_R, false);
        if (g == null) {
            this.nextBaseIn = 200;
            return;
        }
        CargoShip ship = this.takeShip(level, g, 0);
        if (ship == null) {
            this.nextBaseIn = 200;
            return;
        }
        this.basesBuilt++;
        this.nextBaseIn = BASE_INTERVAL;
        this.bases.add(g);
        Vec3 hover = new Vec3(g.getX() + 0.5D, g.getY() + 30.0D, g.getZ() + 0.5D);
        Vec3 land = new Vec3(g.getX() + 0.5D, g.getY(), g.getZ() + 3.0D);
        ship.dispatch(this.exitPoint(ship), hover, land, g, false, true, true, 0, 180.0F);
        say(level, "Nave de carga despachada hacia X " + g.getX() + ", Z " + g.getZ() + ". Base militar "
                + this.basesBuilt + " de " + OUTPOSTS + ".");
        this.setDirty();
    }

    private void placeFortress(ServerLevel level) {
        BlockPos g = this.pickBase(level, FORT_R, true);
        if (g == null) {
            this.nextBaseIn = 200;
            return;
        }
        CargoShip lead = this.takeShip(level, g, 0);
        if (lead == null) {
            this.nextBaseIn = 200;
            return;
        }
        this.stage = FORTRESS;
        this.fortDispatched = true;
        this.bases.add(g);
        Vec3 leadHover = new Vec3(g.getX() + 0.5D, g.getY() + 40.0D, g.getZ() + 8.5D);
        Vec3 land = new Vec3(g.getX() + 0.5D, g.getY(), g.getZ() + 3.5D);
        lead.dispatch(this.exitPoint(lead), leadHover, land, g, true, true, true, 0, 180.0F);
        int[][] escorts = {{-20, -12}, {20, -12}};
        for (int i = 0; i < escorts.length; i++) {
            CargoShip e = this.takeShip(level, g, i + 1);
            if (e == null) {
                continue;
            }
            Vec3 h = new Vec3(g.getX() + 0.5D + escorts[i][0], g.getY() + 46.0D, g.getZ() + 0.5D + escorts[i][1]);
            e.dispatch(this.exitPoint(e), h, h, g, true, false, false, 520, 180.0F);
        }
        say(level, "Flota de carga despachada hacia X " + g.getX() + ", Z " + g.getZ()
                + ". La Fortaleza Loree se levantará bajo mi nave.");
        this.setDirty();
    }

    private AABB hangarBox() {
        return new AABB(this.shipPoint(-12.0D, -16.0D, 70.0D), this.shipPoint(12.0D, 4.0D, 130.0D));
    }

    /** Punto bajo el casco por donde sale una nave de carga (si llego desde el cielo, sale desde donde esta). */
    private Vec3 exitPoint(CargoShip ship) {
        Vec3 d = ship.getDock();
        if (this.hangarBox().contains(d)) {
            return new Vec3(d.x, d.y - 22.0D, d.z);
        }
        return ship.position();
    }

    /**
     * Busca una nave de carga atracada en el hangar. Si no hay (o el hangar no esta cargado), aparece una
     * nueva en un atraque o, si la nave nodriza esta lejos, en el cielo, en direccion a la nave.
     */
    private CargoShip takeShip(ServerLevel level, BlockPos target, int index) {
        List<CargoShip> docked = level.getEntitiesOfClass(CargoShip.class, this.hangarBox(), CargoShip::isDocked);
        if (!docked.isEmpty()) {
            return docked.get(level.random.nextInt(docked.size()));
        }
        Vec3 p0 = this.dockPoint(Math.min(index, ShipDesign.DOCK_Z.length - 1));
        if (level.isLoaded(BlockPos.containing(p0))) {
            return this.spawnDocked(level, Math.min(index, ShipDesign.DOCK_Z.length - 1));
        }
        // La nave nodriza no esta cargada: la nave de carga llega desde el cielo, en direccion a la nodriza.
        double dx = this.shipX - target.getX();
        double dz = this.shipZ - target.getZ();
        double len = Math.max(1.0D, Math.sqrt(dx * dx + dz * dz));
        for (double dist : new double[]{110.0D, 70.0D, 40.0D}) {
            double x = target.getX() + dx / len * dist + index * 8.0D;
            double z = target.getZ() + dz / len * dist;
            BlockPos probe = BlockPos.containing(x, 80.0D, z);
            if (level.isLoaded(probe)) {
                CargoShip ship = KeinLooreMod.CARGO_SHIP.get().create(level);
                if (ship == null) {
                    return null;
                }
                double y = target.getY() + 70.0D;
                ship.moveTo(x, y, z, 0.0F, 0.0F);
                ship.setDock(new Vec3(x, y, z));
                level.addFreshEntity(ship);
                return ship;
            }
        }
        return null;
    }

    /**
     * Sitio de una base: el terreno mas plano de unos cuantos candidatos, sin agua y lejos de otras bases.
     * Las bases normales se ponen a 70-120 bloques de un jugador; la fortaleza, bajo la nave nodriza.
     */
    private BlockPos pickBase(ServerLevel level, int r, boolean nearShip) {
        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int attempt = 0; attempt < 16; attempt++) {
            int x;
            int z;
            if (nearShip) {
                x = this.shipX + level.random.nextInt(49) - 24;
                z = this.shipZ + level.random.nextInt(49) - 24;
            } else {
                Player p = randomPlayer(level);
                if (p == null) {
                    return null;
                }
                double ang = level.random.nextDouble() * Math.PI * 2.0D;
                double dist = 70.0D + level.random.nextDouble() * 50.0D;
                x = (int) Math.floor(p.getX() + Math.cos(ang) * dist);
                z = (int) Math.floor(p.getZ() + Math.sin(ang) * dist);
            }
            BlockPos g = groundPos(level, x, z);
            if (g == null) {
                continue;
            }
            boolean tooClose = false;
            for (BlockPos b : this.bases) {
                double ddx = b.getX() - g.getX();
                double ddz = b.getZ() - g.getZ();
                if (ddx * ddx + ddz * ddz < 55.0D * 55.0D) {
                    tooClose = true;
                    break;
                }
            }
            if (tooClose) {
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

    /** Diferencia de altura del terreno en varios puntos del circulo de la base (y una penalizacion si hay agua). */
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

    // ------------------------------------------------------------------ obra de las bases (la inician las naves de carga)

    /** Una nave de carga llego sobre su base: la base empieza a imprimirse bajo su haz. Devuelve el trabajo. */
    public static long onCargoArrived(ServerLevel level, CargoShip ship) {
        ServerLevel overworld = level.getServer().overworld();
        return get(overworld).startBaseBuild(level, ship.getBase(), ship.isFort());
    }

    private long startBaseBuild(ServerLevel level, BlockPos g, boolean fort) {
        Builds.Options o = new Builds.Options();
        o.order = Builds.Order.Y_RISE;
        o.perTick = fort ? 260 : 120;
        o.support = true;
        o.progress = (lvl, front, f) -> this.baseFx(lvl, g, fort, front);
        o.done = () -> this.onBaseBuilt(level, g, fort);
        return Builds.start(level, baseGrid(fort), g.getX(), g.getY(), g.getZ(), o);
    }

    /** Anillo de codigo a la altura de la capa que se esta imprimiendo. */
    private void baseFx(ServerLevel level, BlockPos g, boolean fort, int y) {
        if (this.timer % 3L != 0L) {
            return;
        }
        double r = fort ? FORT_R : OUTPOST_R;
        Vec3 c = new Vec3(g.getX() + 0.5D, g.getY() + y + 0.5D, g.getZ() + 0.5D);
        Fx.ringFar(level, c, r + 1.0D, fort ? 36 : 24, Fx.BRIGHT, this.timer * 0.1D);
    }

    private void onBaseBuilt(ServerLevel level, BlockPos g, boolean fort) {
        Vec3 c = new Vec3(g.getX() + 0.5D, g.getY() + 1.0D, g.getZ() + 0.5D);
        Fx.burstFar(level, Fx.BRIGHT, c, 70, fort ? 8.0D : 4.0D, 0.4D);
        Fx.sound(level, c, SoundEvents.BEACON_ACTIVATE, 4.0F, 0.8F);
        if (fort) {
            this.onFortressBuilt(level, g);
            return;
        }
        say(level, "Base militar digital establecida en X " + g.getX() + ", Z " + g.getZ() + ".");
        this.garrisonDue.put(g.asLong(), this.timer + 900L);
    }

    /** La nave de carga aterrizo y abrio la rampa: baja la guarnicion de la base. */
    public static void onCargoLanded(ServerLevel level, CargoShip ship) {
        ServerLevel overworld = level.getServer().overworld();
        LooreStory s = get(overworld);
        s.garrisonDue.remove(ship.getBase().asLong());
        if (ship.isReleased()) {
            return;
        }
        ship.markReleased();
        Vec3 rear = ship.local(0.0D, 0.0D, -7.8D);
        spawnGarrison(level, rear, ship.getY(), ship.isFort());
    }

    private void onFortressBuilt(ServerLevel level, BlockPos g) {
        this.stage = FIGHT;
        this.fortDispatched = false;
        Vec3 landing = new Vec3(g.getX() + 0.5D, g.getY(), g.getZ() - 5.5D);
        KeinLooreEntity boss = this.findBoss(level);
        if (boss == null) {
            boss = KeinLooreMod.KEIN_LOREE.get().create(level);
            if (boss == null) {
                return;
            }
            boss.moveTo(landing.x, landing.y + 40.0D, landing.z, 0.0F, 0.0F);
            level.addFreshEntity(boss);
        }
        this.bossId = boss.getUUID();
        boss.beginDescent(landing);
        title(level, "KEIN LOREE", "Desciende a su base");
        say(level, "Bienvenido a mi fortaleza. Aquí termina tu historia... o empieza la mía.");
        this.setDirty();
    }

    // ------------------------------------------------------------------ eventos de combate

    /** El jugador entrego el Nucleo de Decodificacion a Kein Loree: el mundo es consumido. */
    public static void onOfferAccepted(ServerLevel level, KeinLooreEntity boss, Player player) {
        LooreStory s = get(level.getServer().overworld());
        boss.closeOffer();
        s.stage = IDLE;
        s.ending = END_CONSUMED;
        s.scenes.clear();
        boss.retreatToThrone(s.throne());
        title(level, "MUNDO DECODIFICADO", "El Imperio Loree se expande");
        say(level, player.getName().getString() + " aceptó. Tu mundo ya es parte de mi red. Descansa... y mira el cielo.");
        s.setDirty();
    }

    /** Kein Loree murio: resistencia vencedora y recompensa. */
    public static void onBossDeath(KeinLooreEntity boss) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        LooreStory s = get(level.getServer().overworld());
        if (s.bossId == null || !s.bossId.equals(boss.getUUID())) {
            return;
        }
        s.stage = IDLE;
        s.ending = END_RESIST;
        s.scenes.clear();
        s.bossId = null;
        title(level, "KEIN LOREE DERROTADO", "La resistencia ha vencido");
        say(level, "Imposible... mi código se fragmenta. Esto no ha terminado.");
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            p.getInventory().add(new ItemStack(KeinLooreMod.DECODE_CORE.get()));
        }
        s.setDirty();
    }

    /** Un jugador murio en supervivencia durante el combate: final de perecer. */
    public static void onPlayerDeath(ServerPlayer player) {
        ServerLevel overworld = player.getServer().overworld();
        LooreStory s = get(overworld);
        if (s.stage != FIGHT) {
            return;
        }
        s.stage = IDLE;
        s.ending = END_PERISHED;
        s.scenes.clear();
        KeinLooreEntity boss = s.findBoss(overworld);
        if (boss != null) {
            boss.retreatToThrone(s.throne());
        }
        title(overworld, "HAS PERECIDO", "Tu planeta será decodificado");
        say(overworld, player.getName().getString() + " ha perecido. La decodificación continúa sin resistencia.");
        s.setDirty();
    }

    // ------------------------------------------------------------------ utilidades

    private void schedule(long delay, String title, String sub, String chat) {
        this.scenes.add(new Scene(this.timer + delay, title, sub, chat));
    }

    private KeinLooreEntity findBoss(ServerLevel level) {
        if (this.bossId == null) {
            return null;
        }
        Entity e = level.getEntity(this.bossId);
        return e instanceof KeinLooreEntity k && !k.isCopy() ? k : null;
    }

    private static BlockPos groundPos(ServerLevel level, int x, int z) {
        if (!level.hasChunkAt(new BlockPos(x, 64, z))) {
            return null;
        }
        int y = (int) Fx.groundY(level, x + 0.5D, z + 0.5D);
        return new BlockPos(x, y, z);
    }

    private static Player randomPlayer(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        return players.isEmpty() ? null : players.get((int) (Math.random() * players.size()));
    }

    /** Droides y un comandante (y, en la fortaleza, un ejecutor) bajan por la rampa de la nave de carga. */
    private static void spawnGarrison(ServerLevel level, Vec3 rear, double groundY, boolean fortress) {
        int droids = fortress ? 6 : 3;
        for (int i = 0; i < droids; i++) {
            Droid droid = KeinLooreMod.DROID.get().create(level);
            if (droid == null) {
                continue;
            }
            double a = (i - (droids - 1) / 2.0D) * 1.6D;
            placeSafe(level, droid, rear.x + a, groundY, rear.z + (i % 2) * 1.5D, (float) (i * 40));
            level.addFreshEntity(droid);
        }
        LooreCommander c = KeinLooreMod.LOREE_COMMANDER.get().create(level);
        if (c != null) {
            placeSafe(level, c, rear.x, groundY, rear.z - 2.5D, 0.0F);
            level.addFreshEntity(c);
        }
        if (fortress) {
            LooreWarlord w = KeinLooreMod.LOREE_WARLORD.get().create(level);
            if (w != null) {
                placeSafe(level, w, rear.x + 3.0D, groundY, rear.z - 3.0D, 0.0F);
                level.addFreshEntity(w);
            }
        }
        Fx.burst(level, Fx.CODE, new Vec3(rear.x, groundY + 1.0D, rear.z), 30, 1.5D, 0.2D);
    }

    /** Coloca a la criatura en el hueco libre mas cercano al punto deseado (no dentro de muros ni cajas). */
    private static void placeSafe(ServerLevel level, Mob mob, double x, double y, double z, float yaw) {
        for (int ring = 0; ring <= 6; ring++) {
            int steps = Math.max(1, ring * 6);
            for (int k = 0; k < steps; k++) {
                double ang = k * (Math.PI * 2.0D / steps);
                double px = x + Math.cos(ang) * ring;
                double pz = z + Math.sin(ang) * ring;
                AABB box = mob.getType().getDimensions().makeBoundingBox(px, y, pz);
                if (level.noCollision(box) && !level.containsAnyLiquid(box)) {
                    mob.moveTo(px, y, pz, yaw, 0.0F);
                    return;
                }
            }
        }
        mob.moveTo(x, y + 3.0D, z, yaw, 0.0F);
    }

    /** Mensaje de Kein Loree a todos los jugadores del servidor. */
    public static void say(ServerLevel level, String text) {
        Component msg = Component.literal("<Kein Loree> " + text).withStyle(ChatFormatting.GREEN);
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(msg);
        }
    }

    /** Titulo grande en pantalla para todos los jugadores. */
    public static void title(ServerLevel level, String title, String sub) {
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(sub)));
            p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
        }
    }
}
