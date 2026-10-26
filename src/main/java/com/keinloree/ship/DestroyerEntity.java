package com.keinloree.ship;

import com.keinloree.registry.ModEntities;
import com.keinloree.registry.ModSounds;
import com.keinloree.story.Campaign;
import com.keinloree.story.ScreenFx;
import com.keinloree.util.Fx;
import com.keinloree.vfx.Vfx;
import com.keinloree.world.design.Grid;
import com.keinloree.world.design.Mat;
import com.keinloree.world.design.ShipDesign;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/**
 * Destructor estelar "Sentencia": cerebro de la nave en todos sus estados. Anclada es un monton de bloques
 * reales; en vuelo es esta entidad (el cliente dibuja la malla) y lleva a todos los de dentro como pasajeros.
 * La posicion de la entidad es siempre el centro inferior de la celda del timon (ver {@link ShipTransform}).
 */
public class DestroyerEntity extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();

    // ---- estados ----
    public static final int HYPERSPACE_IN = 0;
    public static final int MATERIALIZE = 1;
    public static final int ANCHORED = 2;
    public static final int LIFTOFF = 3;
    public static final int FLYING = 4;
    public static final int ALIGN = 5;
    public static final int ANCHORING = 6;
    public static final int DESTROYED = 7;
    private static final String[] STATE_NAMES = {"HYPERSPACE_IN", "MATERIALIZE", "ANCHORED", "LIFTOFF", "FLYING", "ALIGN",
            "ANCHORING", "DESTROYED"};

    // ---- FLAGS ----
    public static final int FLAG_ENGINES = 1;
    public static final int FLAG_ALARM = 2;
    public static final int FLAG_DAMAGED = 4;
    public static final int FLAG_BOOST = 8;

    // ---- botones de ShipInputC2S ----
    public static final int BTN_FIRE = 1;
    public static final int BTN_FIGHTERS = 2;
    public static final int BTN_BOOST = 4;
    public static final int BTN_RELEASE = 8;

    // ---- fisica de vuelo (bloques/tick y grados/tick) ----
    public static final float VMAX = 0.55F;
    public static final float VBOOST = 0.9F;
    public static final int BOOST_TICKS = 60;
    public static final int BOOST_COOLDOWN = 400;
    public static final float ACCEL = 0.012F;
    public static final float BRAKE = 0.02F;
    public static final float TURN_MAX = 0.7F;
    public static final float TURN_ACCEL = 0.05F;
    public static final float VERT_MAX = 0.12F;
    /** Duracion de la llegada desde el hiperespacio (35 s). */
    public static int hyperTicks = 700;

    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(DestroyerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ROT = SynchedEntityData.defineId(DestroyerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> ANCHOR = SynchedEntityData.defineId(DestroyerEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> FLAGS = SynchedEntityData.defineId(DestroyerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PILOT = SynchedEntityData.defineId(DestroyerEntity.class, EntityDataSerializers.INT);
    /** Progreso 0..1 del trabajo de bloques actual (colocar/borrar). */
    private static final EntityDataAccessor<Float> PROGRESS = SynchedEntityData.defineId(DestroyerEntity.class, EntityDataSerializers.FLOAT);
    /** Alabeo en grados (destruccion). El cabeceo va en xRot. */
    private static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(DestroyerEntity.class, EntityDataSerializers.FLOAT);
    /** Pasajeros: ids de entidad y desplazamientos locales (para que el cliente los coloque igual). */
    private static final EntityDataAccessor<CompoundTag> RIDERS = SynchedEntityData.defineId(DestroyerEntity.class, EntityDataSerializers.COMPOUND_TAG);

    /** Ganchos para otros paquetes (torretas WP7, cazas WP8, historia). firstTime = primera materializacion. */
    public static final List<AnchorHook> ON_ANCHORED = new ArrayList<>();
    /** Disparo principal del piloto (WP7) y lanzamiento de cazas (WP8). */
    public static BiConsumer<DestroyerEntity, ServerPlayer> onFirePrimary = (s, p) -> { };
    public static BiConsumer<DestroyerEntity, ServerPlayer> onLaunchFighters = (s, p) -> { };

    @FunctionalInterface
    public interface AnchorHook {
        void anchored(ServerLevel level, DestroyerEntity ship, boolean firstTime);
    }

    /** Datos de un pasajero: desplazamiento y yaw locales, y si ya tenia noAi. */
    public static final class Rider {
        public float x;
        public float y;
        public float z;
        public float yaw;
        public boolean noAi;

        Rider(Vec3 local, float yaw, boolean noAi) {
            this.x = (float) local.x;
            this.y = (float) local.y;
            this.z = (float) local.z;
            this.yaw = yaw;
            this.noAi = noAi;
        }

        public Vec3 local() {
            return new Vec3(x, y, z);
        }
    }

    // ---- estado de servidor ----
    private int stateTicks;
    @Nullable
    private ShipBlocks.Job job;
    private int jobCursor;
    private final Map<UUID, Rider> riders = new LinkedHashMap<>();
    @Nullable
    private UUID pilotId;
    private boolean releasing;
    private final LongSet forced = new LongOpenHashSet();
    private final LongSet footprint = new LongOpenHashSet();
    private boolean captured;
    /** Creada por la llegada o la recuperacion (no por /summon). */
    private boolean valid;
    private float speed;
    private float yawVel;
    private float vertVel;
    private float inForward;
    private float inTurn;
    private float inLift;
    private int inputAge = 1000;
    private int boostTicks;
    private int boostCooldown;
    // llegada
    private Vec3 hyperFrom = Vec3.ZERO;
    private Vec3 hyperTo = Vec3.ZERO;
    private float hyperYaw;
    // piloto automatico (no se guarda: tras reiniciar queda parado)
    @Nullable
    private Vec3 apTarget;
    private float apYaw;
    @Nullable
    private Runnable apCallback;
    // alineado
    private BlockPos alignAnchor = BlockPos.ZERO;
    private int alignR;
    // tripulacion
    private boolean crewSpawned;
    private final Map<String, UUID> crew = new LinkedHashMap<>();
    // destruccion
    private int destroyWave;
    private int fallTicks = -1;
    // registro de tiempos de transicion (pruebas)
    private final List<String> timings = new ArrayList<>();
    private int dataTick;

    // ---- cliente ----
    private final Map<Integer, Vec3> clientRiders = new HashMap<>();
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    private float riderDelta;

    public DestroyerEntity(EntityType<? extends DestroyerEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    // ------------------------------------------------------------------ datos

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(STATE, ANCHORED);
        builder.define(ROT, 0);
        builder.define(ANCHOR, BlockPos.ZERO);
        builder.define(FLAGS, 0);
        builder.define(PILOT, -1);
        builder.define(PROGRESS, 1.0F);
        builder.define(ROLL, 0.0F);
        builder.define(RIDERS, new CompoundTag());
    }

    public int getState() {
        return entityData.get(STATE);
    }

    public static String stateName(int s) {
        return s >= 0 && s < STATE_NAMES.length ? STATE_NAMES[s] : "?";
    }

    /** Rotacion anclada r (0..3). */
    public int getRot() {
        return entityData.get(ROT);
    }

    public BlockPos getAnchor() {
        return entityData.get(ANCHOR);
    }

    public int getFlags() {
        return entityData.get(FLAGS);
    }

    public boolean hasFlag(int f) {
        return (getFlags() & f) != 0;
    }

    /** Id de entidad del piloto o -1. */
    public int getPilotId() {
        return entityData.get(PILOT);
    }

    public float getProgress() {
        return entityData.get(PROGRESS);
    }

    public float getRoll() {
        return entityData.get(ROLL);
    }

    public float getSpeed() {
        return speed;
    }

    public int stateTicks() {
        return stateTicks;
    }

    public List<String> timings() {
        return timings;
    }

    public Map<String, UUID> crew() {
        return crew;
    }

    /** Estados en los que hay bloques del diseno en el mundo. */
    public static boolean blocksPresent(int s) {
        return s == MATERIALIZE || s == ANCHORED || s == ANCHORING || s == LIFTOFF;
    }

    /** Estados en los que el cliente debe dibujar la malla. */
    public static boolean meshVisible(int s) {
        return s != ANCHORED;
    }

    private void setFlag(int f, boolean on) {
        int v = getFlags();
        entityData.set(FLAGS, on ? v | f : v & ~f);
    }

    private void setState(int s) {
        int old = getState();
        if (old != s) {
            String t = stateName(old) + "->" + stateName(s) + " tras " + stateTicks + " ticks";
            timings.add(t);
            LOGGER.info("[Destructor] {}", t);
        }
        entityData.set(STATE, s);
        stateTicks = 0;
        setFlag(FLAG_ENGINES, s != ANCHORED);
        saveToData();
    }

    private void setAnchor(BlockPos anchor, int r) {
        entityData.set(ANCHOR, anchor.immutable());
        entityData.set(ROT, Math.floorMod(r, 4));
    }

    /** Copia el estado al registro persistente del mundo. */
    private void saveToData() {
        if (valid && level() instanceof ServerLevel sl && !isRemoved()) {
            ShipData.get(sl.getServer()).update(getUUID(), getState(), getAnchor(), getRot(), position(), getYRot());
        }
    }

    // ------------------------------------------------------------------ propiedades de entidad

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean canChangeDimensions(Level from, Level to) {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return true;
    }

    /** Solo cliente: el renderer lo activa un instante mientras dibuja al piloto (pose sentada forzada). */
    public static boolean renderPilotSitting;

    /** Todos de pie (la pose sentada es por vehiculo, no por pasajero); el cliente fuerza la del piloto al dibujarlo. */
    @Override
    public boolean shouldRiderSit() {
        return renderPilotSitting;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return ShipTransform.worldAABB(position(), getYRot()).inflate(4.0D);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 1024.0D * 1024.0D;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 l = localOf(passenger);
        return l != null ? ShipTransform.toWorld(position(), getYRot(), l) : super.getDismountLocationForPassenger(passenger);
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            tickLerp();
            riderDelta = Mth.wrapDegrees(getYRot() - yRotO);
            return;
        }
        ServerLevel sl = (ServerLevel) level();
        if (!valid) {
            // invocada a mano (/summon): sin ancla valida no puede existir; usa /keinloree ship arrive
            LOGGER.warn("[Destructor] Entidad sin llegada valida eliminada (usa /keinloree ship arrive)");
            discard();
            return;
        }
        stateTicks++;
        switch (getState()) {
            case HYPERSPACE_IN -> tickHyperspace(sl);
            case MATERIALIZE -> tickPlace(sl, false);
            case ANCHORED -> tickAnchored(sl);
            case LIFTOFF -> tickLiftoff(sl);
            case FLYING -> tickFlying(sl);
            case ALIGN -> tickAlign(sl);
            case ANCHORING -> tickPlace(sl, true);
            case DESTROYED -> tickDestroyed(sl);
            default -> setState(ANCHORED);
        }
        if (isRemoved()) {
            return;
        }
        riderDelta = Mth.wrapDegrees(getYRot() - yRotO);
        tickRiders();
        updateForcedChunks(sl);
        if (++dataTick % 20 == 0) {
            saveToData();
        }
    }

    // ---- llegada ----

    private void tickHyperspace(ServerLevel sl) {
        if (stateTicks == 1) {
            fxHyperspace(sl);
        }
        float t = Math.min(1.0F, stateTicks / (float) Math.max(1, hyperTicks));
        double e = 1.0D - Math.pow(1.0D - t, 3.0D);
        Vec3 p = hyperFrom.lerp(hyperTo, e);
        float target = ShipTransform.yawFor(getRot());
        float s = t * t * (3.0F - 2.0F * t);
        setYRot(hyperYaw + Mth.wrapDegrees(target - hyperYaw) * s);
        setPos(p.x, p.y, p.z);
        if (stateTicks % 4 == 0) {
            Vec3 back = ShipTransform.toWorld(position(), getYRot(), new Vec3(0, 0, 30));
            particles(sl, ParticleTypes.END_ROD, back, 6, 2.0D, 0.05D);
        }
        if (t >= 1.0F) {
            setYRot(target);
            setPos(ShipTransform.entityPos(getAnchor()));
            startJob(true, MATERIALIZE);
        }
    }

    private void fxHyperspace(ServerLevel sl) {
        Vec3 p = position();
        double yr = Math.toRadians(getYRot());
        Vfx.hyperspace(sl, p, new Vec3(-Math.sin(yr), 0, Math.cos(yr)), 12.0F);
        particles(sl, ParticleTypes.FLASH, p, 4, 3.0D, 0.0D);
        particles(sl, ParticleTypes.END_ROD, p, 200, 20.0D, 0.4D);
        for (ServerPlayer pl : sl.players()) {
            double d = pl.position().distanceTo(p);
            if (d <= 400.0D) {
                float vol = (float) (0.4D + 1.6D * (1.0D - d / 400.0D));
                pl.playNotifySound(ModSounds.HYPERSPACE.get(), SoundSource.AMBIENT, vol, 1.0F);
                ScreenFx.send(pl, ScreenFx.SHAKE, 40, 0.4F * (float) (1.0D - d / 500.0D), 0xFFFFFF);
            }
        }
    }

    // ---- bloques ----

    private void startJob(boolean place, int newState) {
        setPos(ShipTransform.entityPos(getAnchor()));
        setYRot(ShipTransform.yawFor(getRot()));
        job = new ShipBlocks.Job(place, getAnchor(), getRot(), false, 0);
        jobCursor = 0;
        entityData.set(PROGRESS, 0.0F);
        setState(newState);
    }

    private ShipBlocks.Job job(boolean place, boolean reverse) {
        if (job == null || job.place != place || !job.anchor.equals(getAnchor()) || job.r != getRot()) {
            job = new ShipBlocks.Job(place, getAnchor(), getRot(), reverse, jobCursor);
        }
        return job;
    }

    /** MATERIALIZE / ANCHORING. */
    private void tickPlace(ServerLevel sl, boolean anchoring) {
        ShipBlocks.Job j = job(true, false);
        boolean done = j.step(sl);
        jobCursor = j.cursor();
        entityData.set(PROGRESS, j.progress());
        if (stateTicks % 10 == 0) {
            Vec3 at = ShipTransform.toWorld(position(), getYRot(),
                    new Vec3(0, 0, ShipDesign.L * (0.0D - ShipDesign.HELM_Z / (double) ShipDesign.L + j.progress())));
            Fx.burst(sl, Fx.CODE, at, 30, 12.0D, 0.05D);
        }
        if (!done) {
            return;
        }
        job = null;
        jobCursor = 0;
        if (anchoring) {
            releasePassengers(sl);
        }
        boolean first = !crewSpawned;
        setState(ANCHORED);
        if (first) {
            spawnCrew(sl);
        }
        for (AnchorHook h : ON_ANCHORED) {
            try {
                h.anchored(sl, this, first);
            } catch (Exception e) {
                LOGGER.error("[Destructor] Fallo en un gancho de anclaje", e);
            }
        }
        Runnable cb = apCallback;
        apCallback = null;
        apTarget = null;
        if (cb != null) {
            cb.run();
        }
    }

    private void tickAnchored(ServerLevel sl) {
        BlockPos a = getAnchor();
        if (position().distanceToSqr(ShipTransform.entityPos(a)) > 1.0E-4D) {
            setPos(ShipTransform.entityPos(a));
        }
        if (stateTicks % 20 == 0) {
            ShipCrew.keepPosts(sl, this);
        }
    }

    private void tickLiftoff(ServerLevel sl) {
        if (!captured) {
            // espera a que esten cargadas las entidades de toda la huella para no dejarse a nadie
            if (footprint.isEmpty()) {
                footprint.addAll(chunksOf(ShipTransform.worldAABB(getAnchor(), getRot()).inflate(2.0D)));
            }
            boolean ready = true;
            for (long l : footprint) {
                if (!sl.areEntitiesLoaded(l)) {
                    ready = false;
                    break;
                }
            }
            if (!ready && stateTicks < 300) {
                return;
            }
            capturePassengers(sl);
            captured = true;
            return;
        }
        ShipBlocks.Job j = job(false, false);
        boolean done = j.step(sl);
        jobCursor = j.cursor();
        entityData.set(PROGRESS, j.progress());
        if (done) {
            job = null;
            jobCursor = 0;
            speed = 0;
            yawVel = 0;
            vertVel = 0;
            footprint.clear();
            setState(FLYING);
        }
    }

    /** Chunks que toca una caja. */
    public static LongSet chunksOf(AABB box) {
        LongSet set = new LongOpenHashSet();
        for (int cx = Mth.floor(box.minX) >> 4; cx <= Mth.floor(box.maxX) >> 4; cx++) {
            for (int cz = Mth.floor(box.minZ) >> 4; cz <= Mth.floor(box.maxZ) >> 4; cz++) {
                set.add(ChunkPos.asLong(cx, cz));
            }
        }
        return set;
    }

    // ---- despegue y timon ----

    /** El jugador pide el timon (bloque del timon o comando). force = sin permiso de historia. */
    public boolean requestHelm(ServerPlayer player, boolean force) {
        int s = getState();
        if (s == FLYING && pilotId == null && player.getVehicle() == this) {
            setPilot(player);
            return true;
        }
        if (s != ANCHORED) {
            player.displayClientMessage(Component.translatable("message.keinloree.ship_busy"), true);
            return false;
        }
        if (!force && !player.isCreative() && !Campaign.get(player.getServer()).hasFlag("helm_unlocked")) {
            player.displayClientMessage(Component.translatable("message.keinloree.helm_denied"), true);
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 0.6F);
            return false;
        }
        ServerLevel sl = (ServerLevel) level();
        Vec3 seat = ShipTransform.toWorld(position(), getYRot(), ShipTransform.cellFloor(ShipDesign.HELM_SEAT));
        if (player.level() != sl || !ShipTransform.worldAABB(getAnchor(), getRot()).contains(player.position())) {
            player.teleportTo(sl, seat.x, seat.y, seat.z, getYRot(), 0.0F);
        }
        pilotId = player.getUUID();
        entityData.set(PILOT, player.getId());
        liftoff();
        player.displayClientMessage(Component.translatable("message.keinloree.helm_taken"), true);
        return true;
    }

    private void setPilot(ServerPlayer player) {
        pilotId = player.getUUID();
        entityData.set(PILOT, player.getId());
        Rider r = riders.get(player.getUUID());
        if (r != null) {
            Vec3 seat = ShipTransform.cellFloor(ShipDesign.HELM_SEAT);
            r.x = (float) seat.x;
            r.y = (float) seat.y;
            r.z = (float) seat.z;
            syncRiders();
        }
    }

    /** Despega (si esta anclada): captura pasajeros y retira los bloques. */
    public boolean liftoff() {
        if (getState() != ANCHORED || !(level() instanceof ServerLevel sl)) {
            return false;
        }
        startJob(false, LIFTOFF);
        captured = false;
        footprint.clear();
        return true;
    }

    /** Soltar el timon / anclar donde esta. */
    public boolean releaseHelm() {
        if (getState() != FLYING) {
            return false;
        }
        apTarget = null;
        beginAlign(ShipTransform.nearestR(getYRot()));
        return true;
    }

    /** Piloto automatico hasta un punto del timon (yaw final). Si esta anclada, despega antes. */
    public boolean autopilotTo(Vec3 helmTarget, float yaw, @Nullable Runnable onArrive) {
        int s = getState();
        if (s != ANCHORED && s != FLYING && s != LIFTOFF && s != ALIGN) {
            return false;
        }
        apTarget = new Vec3(helmTarget.x, Mth.clamp(helmTarget.y, ShipTransform.MIN_ANCHOR_Y, ShipTransform.MAX_ANCHOR_Y),
                helmTarget.z);
        apYaw = yaw;
        apCallback = onArrive;
        if (s == ANCHORED) {
            liftoff();
        } else if (s == ALIGN) {
            setState(FLYING);
        }
        return true;
    }

    /** Entrada del piloto (ShipInputC2S). */
    public void input(ServerPlayer player, float forward, float turn, float lift, int buttons) {
        if (pilotId == null || !pilotId.equals(player.getUUID()) || player.getVehicle() != this) {
            return;
        }
        if ((buttons & BTN_RELEASE) != 0) {
            releaseHelm();
            return;
        }
        if (getState() != FLYING) {
            return;
        }
        apTarget = null;
        inForward = Mth.clamp(forward, -1.0F, 1.0F);
        inTurn = Mth.clamp(turn, -1.0F, 1.0F);
        inLift = Mth.clamp(lift, -1.0F, 1.0F);
        inputAge = 0;
        if ((buttons & BTN_BOOST) != 0 && boostCooldown <= 0) {
            boostTicks = BOOST_TICKS;
            boostCooldown = BOOST_COOLDOWN;
        }
        if ((buttons & BTN_FIRE) != 0) {
            onFirePrimary.accept(this, player);
        }
        if ((buttons & BTN_FIGHTERS) != 0) {
            onLaunchFighters.accept(this, player);
        }
    }

    // ---- vuelo ----

    private void tickFlying(ServerLevel sl) {
        inputAge++;
        if (boostTicks > 0) {
            boostTicks--;
        }
        if (boostCooldown > 0) {
            boostCooldown--;
        }
        setFlag(FLAG_BOOST, boostTicks > 0);
        if (pilotId != null && getPilot() == null) {
            // el piloto ya no esta a bordo
            pilotId = null;
            entityData.set(PILOT, -1);
        }
        Vec3 pos = position();
        float yaw = getYRot();
        if (apTarget != null) {
            Vec3 to = apTarget.subtract(pos);
            double d = Math.sqrt(to.x * to.x + to.z * to.z);
            float heading = (float) Math.toDegrees(Math.atan2(-to.x, to.z));
            float desired = d > 30.0D ? heading : apYaw;
            float err = Mth.wrapDegrees(desired - yaw);
            yawVel += Mth.clamp(Mth.clamp(err / 25.0F, -1.0F, 1.0F) * TURN_MAX - yawVel, -TURN_ACCEL, TURN_ACCEL);
            if (Math.abs(err) < 0.3F && Math.abs(yawVel) < 0.06F) {
                yawVel = err;
            }
            double vt = Math.min(VMAX, Math.sqrt(2.0D * BRAKE * 0.8D * d));
            if (d > 30.0D) {
                vt *= Mth.clamp(1.0D - Math.abs(err) / 120.0D, 0.25D, 1.0D);
            }
            speed += Mth.clamp((float) vt - speed, -BRAKE, ACCEL);
            double step = Math.min(Math.max(speed, 0.0F), d);
            Vec3 move = d > 1.0E-4D ? new Vec3(to.x / d * step, 0, to.z / d * step) : Vec3.ZERO;
            vertVel += Mth.clamp(Mth.clamp((float) to.y * 0.05F, -VERT_MAX, VERT_MAX) - vertVel, -0.01F, 0.01F);
            double dy = Math.abs(to.y) < 0.05D ? to.y : vertVel;
            yaw += yawVel;
            pos = pos.add(move.x, dy, move.z);
            float finalErr = Mth.wrapDegrees(apYaw - yaw);
            if (d - step < 0.05D && Math.abs(apTarget.y - pos.y) < 0.1D && Math.abs(finalErr) < 1.0F) {
                setYRot(yaw);
                setPos(pos.x, pos.y, pos.z);
                beginAlign(ShipTransform.nearestR(apYaw));
                return;
            }
        } else {
            boolean piloted = inputAge < 10;
            float fwd = piloted ? inForward : 0.0F;
            float turn = piloted ? inTurn : 0.0F;
            float lift = piloted ? inLift : 0.0F;
            float vmax = boostTicks > 0 ? VBOOST : VMAX;
            float target = fwd >= 0 ? fwd * vmax : fwd * 0.25F;
            float dv = target - speed;
            boolean slowing = Math.abs(target) < Math.abs(speed) || Math.signum(target) != Math.signum(speed);
            speed += Mth.clamp(dv, -(slowing ? BRAKE : ACCEL), slowing ? BRAKE : ACCEL);
            yawVel += Mth.clamp(turn * TURN_MAX - yawVel, -TURN_ACCEL, TURN_ACCEL);
            vertVel += Mth.clamp(lift * VERT_MAX - vertVel, -0.01F, 0.01F);
            yaw += yawVel;
            double r = Math.toRadians(yaw);
            pos = pos.add(-Math.sin(r) * speed, vertVel, Math.cos(r) * speed);
        }
        // banda de altitud y terreno
        double y = Mth.clamp(pos.y, ShipTransform.MIN_ANCHOR_Y, ShipTransform.MAX_ANCHOR_Y);
        if (y != pos.y) {
            vertVel = 0;
        }
        pos = new Vec3(pos.x, y, pos.z);
        if (stateTicks % 10 == 0) {
            int h = terrainMax(sl, ShipTransform.worldAABB(pos, yaw));
            double base = pos.y + ShipTransform.solidBounds()[1] - ShipTransform.HY;
            if (h + 4 > base) {
                speed *= 0.6F;
                vertVel = Math.max(vertVel, 0.08F);
            }
        }
        setYRot(Mth.wrapDegrees(yaw));
        setPos(pos.x, pos.y, pos.z);
    }

    private void beginAlign(int r) {
        alignR = r;
        BlockPos a = ShipTransform.anchorAt(position());
        alignAnchor = new BlockPos(a.getX(), Mth.clamp(a.getY(), ShipTransform.MIN_ANCHOR_Y, ShipTransform.MAX_ANCHOR_Y), a.getZ());
        setState(ALIGN);
    }

    private void tickAlign(ServerLevel sl) {
        speed *= 0.8F;
        yawVel = 0;
        vertVel = 0;
        Vec3 target = ShipTransform.entityPos(alignAnchor);
        Vec3 d = target.subtract(position());
        double len = d.length();
        Vec3 pos = len <= 0.06D ? target : position().add(d.scale(Math.min(0.25D, len * 0.12D + 0.02D) / len));
        float want = ShipTransform.yawFor(alignR);
        float err = Mth.wrapDegrees(want - getYRot());
        float yaw = Math.abs(err) <= 0.3F ? want : getYRot() + Mth.clamp(err * 0.1F, -1.5F, 1.5F) + Math.signum(err) * 0.1F;
        setYRot(Mth.wrapDegrees(yaw));
        setPos(pos.x, pos.y, pos.z);
        if (pos.equals(target) && Math.abs(Mth.wrapDegrees(want - getYRot())) < 1.0E-3F) {
            BlockPos blocked = obstruction(sl, alignAnchor, alignR);
            if (blocked != null) {
                LOGGER.info("[Destructor] Anclaje obstruido en {}", blocked);
                Entity p = getPilot();
                if (p instanceof ServerPlayer sp) {
                    sp.displayClientMessage(Component.translatable("message.keinloree.ship_obstructed"), true);
                }
                apTarget = null;
                setState(FLYING);
                return;
            }
            setAnchor(alignAnchor, alignR);
            startJob(true, ANCHORING);
        }
    }

    /** Primer bloque solido del mundo que estorba al diseno en anchor/r (chunks cargados), o null. */
    @Nullable
    public static BlockPos obstruction(ServerLevel sl, BlockPos anchor, int r) {
        Grid g = ShipDesign.grid();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = g.x0; x <= g.maxX(); x++) {
            for (int y = g.y0; y <= g.maxY(); y++) {
                for (int z = g.z0; z <= g.maxZ(); z++) {
                    byte m = g.get(x, y, z);
                    if (m == Mat.NONE || m == Mat.AIR) {
                        continue;
                    }
                    ShipTransform.blockFor(p, anchor, r, x, y, z);
                    if (sl.getChunkSource().getChunkNow(p.getX() >> 4, p.getZ() >> 4) == null) {
                        continue;
                    }
                    BlockState s = sl.getBlockState(p);
                    if (!s.isAir() && !s.canBeReplaced()) {
                        return p.immutable();
                    }
                }
            }
        }
        return null;
    }

    /** Altura maxima del terreno (heightmap WORLD_SURFACE de chunks cargados) bajo una caja. */
    public static int terrainMax(ServerLevel sl, AABB box) {
        int max = sl.getMinBuildHeight();
        for (int x = (int) Math.floor(box.minX); x <= box.maxX; x += 8) {
            for (int z = (int) Math.floor(box.minZ); z <= box.maxZ; z += 8) {
                LevelChunk c = sl.getChunkSource().getChunkNow(x >> 4, z >> 4);
                if (c != null) {
                    max = Math.max(max, c.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15));
                }
            }
        }
        return max;
    }

    // ---- pasajeros ----

    @Nullable
    public Entity getPilot() {
        if (pilotId == null) {
            return null;
        }
        for (Entity e : getPassengers()) {
            if (e.getUUID().equals(pilotId)) {
                return e;
            }
        }
        return null;
    }

    public boolean isReleasing() {
        return releasing;
    }

    /** Captura todo lo que hay dentro de la caja de la nave anclada. */
    private void capturePassengers(ServerLevel sl) {
        AABB box = ShipTransform.worldAABB(getAnchor(), getRot()).inflate(1.0D);
        Set<Entity> roots = new LinkedHashSet<>();
        for (Entity e : sl.getEntities(this, box, e -> true)) {
            if (e instanceof DestroyerEntity || e.isSpectator() || e instanceof HangingEntity) {
                continue;
            }
            Entity root = e.getRootVehicle();
            if (root == this || root instanceof DestroyerEntity || root.isSpectator()) {
                continue;
            }
            roots.add(root);
        }
        Vec3 seat = ShipTransform.cellFloor(ShipDesign.HELM_SEAT);
        for (Entity e : roots) {
            boolean pilot = pilotId != null && pilotId.equals(e.getUUID());
            Vec3 local = pilot ? seat : ShipTransform.toLocal(position(), getYRot(), e.position());
            board(e, local, Mth.wrapDegrees(e.getYRot() - getYRot()));
        }
        syncRiders();
        LOGGER.info("[Destructor] {} pasajeros a bordo", riders.size());
    }

    /** Sube una entidad como pasajera en un punto local. */
    public boolean board(Entity e, Vec3 local, float localYaw) {
        boolean noAi = e instanceof Mob m && m.isNoAi();
        releasing = true;
        boolean ok = e.startRiding(this, true);
        releasing = false;
        if (!ok) {
            return false;
        }
        if (e instanceof Mob m) {
            m.setNoAi(true);
        }
        riders.put(e.getUUID(), new Rider(local, localYaw, noAi));
        return true;
    }

    /** Suelta a todos en su sitio (tras anclar). */
    private void releasePassengers(ServerLevel sl) {
        releasing = true;
        try {
            for (Entity e : new ArrayList<>(getPassengers())) {
                Rider r = riders.get(e.getUUID());
                Vec3 local = r != null ? r.local() : ShipTransform.toLocal(position(), getYRot(), e.position());
                Vec3 w = ShipTransform.toWorld(position(), getYRot(), local);
                float yaw = Mth.wrapDegrees((r != null ? r.yaw : 0.0F) + getYRot());
                e.stopRiding();
                if (e instanceof ServerPlayer sp) {
                    sp.connection.teleport(w.x, w.y, w.z, sp.getYRot(), sp.getXRot());
                } else {
                    e.moveTo(w.x, w.y, w.z, yaw, e.getXRot());
                    e.setYHeadRot(yaw);
                    if (e instanceof LivingEntity le) {
                        le.setYBodyRot(yaw);
                    }
                    e.setDeltaMovement(Vec3.ZERO);
                }
                if (e instanceof Mob m) {
                    m.setNoAi(r != null && r.noAi);
                }
                e.resetFallDistance();
            }
        } finally {
            releasing = false;
        }
        riders.clear();
        pilotId = null;
        entityData.set(PILOT, -1);
        syncRiders();
    }

    /** Baja a un jugador que se desconecta en vuelo y guarda su sitio (lo llama ShipEvents). */
    public void detachPlayer(ServerPlayer p) {
        Vec3 local = localOf(p);
        if (local == null) {
            local = ShipTransform.toLocal(position(), getYRot(), p.position());
        }
        ShipData d = ShipData.get(p.getServer());
        d.offline.put(p.getUUID(), new float[]{(float) local.x, (float) local.y, (float) local.z,
                Mth.wrapDegrees(p.getYRot() - getYRot())});
        d.setDirty();
        releasing = true;
        try {
            p.stopRiding();
        } finally {
            releasing = false;
        }
        riders.remove(p.getUUID());
        if (p.getUUID().equals(pilotId)) {
            pilotId = null;
            entityData.set(PILOT, -1);
        }
        syncRiders();
    }

    /** Vuelve a subir a un jugador que reconecta (en vuelo) o lo coloca dentro de la nave anclada. */
    public void reattachPlayer(ServerPlayer p, float[] off) {
        Vec3 local = new Vec3(off[0], off[1], off[2]);
        Vec3 w = ShipTransform.toWorld(position(), getYRot(), local);
        float yaw = Mth.wrapDegrees(off[3] + getYRot());
        if (getState() == ANCHORED || getState() == MATERIALIZE || getState() == DESTROYED || getState() == HYPERSPACE_IN) {
            p.teleportTo((ServerLevel) level(), w.x, w.y, w.z, yaw, p.getXRot());
            return;
        }
        p.teleportTo((ServerLevel) level(), w.x, w.y, w.z, yaw, p.getXRot());
        board(p, local, off[3]);
        syncRiders();
    }

    @Nullable
    private Vec3 localOf(Entity e) {
        if (level().isClientSide) {
            return clientRiders.get(e.getId());
        }
        Rider r = riders.get(e.getUUID());
        return r == null ? null : r.local();
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        Vec3 local = localOf(passenger);
        if (local == null) {
            local = ShipTransform.toLocal(position(), getYRot(), passenger.position());
            if (level().isClientSide) {
                clientRiders.put(passenger.getId(), local);
            } else {
                riders.put(passenger.getUUID(), new Rider(local, Mth.wrapDegrees(passenger.getYRot() - getYRot()),
                        passenger instanceof Mob m && m.isNoAi()));
            }
        }
        Vec3 w = ShipTransform.toWorld(position(), getYRot(), local);
        move.accept(passenger, w.x, w.y, w.z);
        // giro de la nave: el cliente gira al jugador local, el servidor al resto (como el bote)
        boolean turn = passenger instanceof Player ? level().isClientSide : !level().isClientSide;
        if (turn && riderDelta != 0.0F) {
            passenger.setYRot(passenger.getYRot() + riderDelta);
            passenger.setYHeadRot(passenger.getYHeadRot() + riderDelta);
            if (passenger instanceof LivingEntity le) {
                le.setYBodyRot(le.yBodyRot + riderDelta);
            }
        }
    }

    /** Los jugadores a bordo no se bajan con Mayus: el piloto suelta el timon. */
    private void tickRiders() {
        for (Entity e : getPassengers()) {
            if (e instanceof Player p && p.isShiftKeyDown()) {
                if (pilotId != null && pilotId.equals(p.getUUID()) && getState() == FLYING) {
                    releaseHelm();
                }
                p.setShiftKeyDown(false);
            }
        }
    }

    private void syncRiders() {
        List<Entity> list = getPassengers();
        int[] ids = new int[list.size()];
        int[] f = new int[list.size() * 3];
        int n = 0;
        for (Entity e : list) {
            Rider r = riders.get(e.getUUID());
            if (r == null) {
                continue;
            }
            ids[n] = e.getId();
            f[n * 3] = Float.floatToIntBits(r.x);
            f[n * 3 + 1] = Float.floatToIntBits(r.y);
            f[n * 3 + 2] = Float.floatToIntBits(r.z);
            n++;
        }
        CompoundTag t = new CompoundTag();
        t.putIntArray("ids", java.util.Arrays.copyOf(ids, n));
        t.putIntArray("f", java.util.Arrays.copyOf(f, n * 3));
        t.putInt("seq", tickCount);
        entityData.set(RIDERS, t);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (RIDERS.equals(key) && level().isClientSide) {
            CompoundTag t = entityData.get(RIDERS);
            int[] ids = t.getIntArray("ids");
            int[] f = t.getIntArray("f");
            clientRiders.clear();
            for (int i = 0; i < ids.length && i * 3 + 2 < f.length; i++) {
                clientRiders.put(ids[i], new Vec3(Float.intBitsToFloat(f[i * 3]), Float.intBitsToFloat(f[i * 3 + 1]),
                        Float.intBitsToFloat(f[i * 3 + 2])));
            }
        }
    }

    // ---- chunks forzados ----

    private void updateForcedChunks(ServerLevel sl) {
        LongSet want = new LongOpenHashSet();
        if (!blocksPresentStrict()) {
            ChunkPos c = new ChunkPos(blockPosition());
            want.add(c.toLong());
            double r = Math.toRadians(getYRot());
            Vec3 ahead = position().add(-Math.sin(r) * 16.0D * Math.signum(speed), 0, Math.cos(r) * 16.0D * Math.signum(speed));
            if (apTarget != null) {
                Vec3 to = apTarget.subtract(position());
                double d = Math.sqrt(to.x * to.x + to.z * to.z);
                if (d > 1.0D) {
                    ahead = position().add(to.x / d * 16.0D, 0, to.z / d * 16.0D);
                }
            }
            want.add(new ChunkPos(BlockPos.containing(ahead)).toLong());
            if (getState() == LIFTOFF) {
                want.addAll(footprint);
            }
        }
        setForced(sl, want);
    }

    /** Anclada del todo (no hay que forzar chunks). */
    private boolean blocksPresentStrict() {
        return getState() == ANCHORED;
    }

    /** Fuerza los chunks pedidos; solo libera los que forzo ella (no los de /forceload u otros). */
    private void setForced(ServerLevel sl, LongSet want) {
        for (long l : forced.toLongArray()) {
            if (!want.contains(l)) {
                sl.setChunkForced(ChunkPos.getX(l), ChunkPos.getZ(l), false);
                forced.remove(l);
            }
        }
        for (long l : want) {
            if (!forced.contains(l) && !sl.getForcedChunks().contains(l)) {
                sl.setChunkForced(ChunkPos.getX(l), ChunkPos.getZ(l), true);
                forced.add(l);
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide && level() instanceof ServerLevel sl && reason.shouldDestroy()) {
            setForced(sl, new LongOpenHashSet());
            if (riders.size() > 0) {
                releasing = true;
                ejectPassengers();
                releasing = false;
            }
            ShipData d = ShipData.get(sl.getServer());
            if (valid && getUUID().equals(d.ship) && (reason == RemovalReason.DISCARDED || !blocksPresent(getState()))) {
                d.clear();
            }
        }
        super.remove(reason);
    }

    // ---- tripulacion ----

    /** Genera la tripulacion la primera vez (gancho ampliable: {@link ShipCrew#spawner}). */
    public void spawnCrew(ServerLevel sl) {
        crewSpawned = true;
        try {
            ShipCrew.spawner.spawn(sl, this);
        } catch (Exception e) {
            LOGGER.error("[Destructor] Fallo generando la tripulacion", e);
        }
    }

    // ---- destruccion ----

    /** Secuencia de destruccion (final bueno). */
    public void destroy() {
        if (!(level() instanceof ServerLevel sl) || getState() == DESTROYED) {
            return;
        }
        boolean hadBlocks = blocksPresent(getState());
        if (!riders.isEmpty()) {
            releasePassengers(sl);
        }
        apTarget = null;
        apCallback = null;
        destroyWave = 0;
        fallTicks = hadBlocks ? -1 : 0;
        job = hadBlocks ? new ShipBlocks.Job(false, getAnchor(), getRot(), true, 0) : null;
        jobCursor = 0;
        setFlag(FLAG_DAMAGED | FLAG_ALARM, true);
        setState(DESTROYED);
    }

    private void tickDestroyed(ServerLevel sl) {
        if (fallTicks < 0) {
            if (job == null) {
                job = new ShipBlocks.Job(false, getAnchor(), getRot(), true, jobCursor);
            }
            if (stateTicks % 15 == 1 && destroyWave < 12) {
                destroyWave++;
                job.setLimit(destroyWave / 12.0F);
                for (int i = 0; i < 6; i++) {
                    explosionFx(sl, randomHullPoint(), 1.0F);
                }
            }
            job.step(sl);
            jobCursor = job.cursor();
            entityData.set(PROGRESS, job.progress());
            if (job.done()) {
                job = null;
                fallTicks = 0;
            }
            return;
        }
        fallTicks++;
        float f = fallTicks;
        setXRot(Math.min(14.0F, f * 0.05F));
        entityData.set(ROLL, Math.min(22.0F, f * 0.08F));
        double vy = 0.04D + f * 0.0015D;
        setPos(getX(), getY() - vy, getZ() + 0.0D);
        if (fallTicks % 6 == 0) {
            explosionFx(sl, randomHullPoint(), 0.6F);
        }
        if (fallTicks % 3 == 0) {
            Fx.burst(sl, Fx.CODE, position().add(0, 30, 0), 40, 40.0D, 0.0D);
        }
        if (fallTicks % 40 == 1) {
            Vfx.codeRain(sl, position().add(0, 40, 0), 60.0F, 60);
        }
        if (getY() < 150.0D || fallTicks > 900) {
            particles(sl, ParticleTypes.FLASH, position(), 8, 20.0D, 0.0D);
            particles(sl, ParticleTypes.EXPLOSION_EMITTER, position(), 10, 25.0D, 0.0D);
            for (ServerPlayer pl : sl.players()) {
                double d = pl.position().distanceTo(position());
                if (d < 600.0D) {
                    ScreenFx.send(pl, ScreenFx.FLASH, 30, 1.0F, 0xD8FFE0);
                    pl.playNotifySound(ModSounds.EXPLOSION_BIG.get(), SoundSource.AMBIENT, 2.0F, 0.6F);
                }
            }
            LOGGER.info("[Destructor] Destruido tras {} ticks de caida", fallTicks);
            ShipData.get(sl.getServer()).clear();
            discard();
        }
    }

    private Vec3 randomHullPoint() {
        int[] b = ShipTransform.solidBounds();
        int x = b[0] + random.nextInt(b[3] - b[0] + 1);
        int z = b[2] + random.nextInt(b[5] - b[2] + 1);
        return ShipTransform.toWorld(position(), getYRot(), ShipTransform.local(x, 0, z, 0.5D, random.nextInt(10), 0.5D));
    }

    /** Particulas vanilla a todos los jugadores cercanos. */
    private static void particles(ServerLevel sl, ParticleOptions type, Vec3 at, int count, double spread, double speed) {
        sl.sendParticles(type, at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    private void explosionFx(ServerLevel sl, Vec3 at, float vol) {
        Vfx.explosion(sl, at, 4.0F * vol);
        particles(sl, ParticleTypes.EXPLOSION_EMITTER, at, 1, 0.0D, 0.0D);
        particles(sl, ParticleTypes.LARGE_SMOKE, at, 20, 3.0D, 0.05D);
        Fx.burst(sl, Fx.GREEN, at, 30, 4.0D, 0.1D);
        sl.playSound(null, at.x, at.y, at.z, ModSounds.EXPLOSION_BIG.get(), SoundSource.HOSTILE, 8.0F * vol, 0.8F);
    }

    // ------------------------------------------------------------------ llegada (la llama la campana)

    /** El destructor sale del hiperespacio cerca del heroe. Devuelve null si ya hay uno. */
    @Nullable
    public static DestroyerEntity arrive(ServerLevel level, ServerPlayer hero) {
        return arriveNear(level, hero.position());
    }

    @Nullable
    public static DestroyerEntity arriveNear(ServerLevel level, Vec3 center) {
        if (find(level) != null) {
            return null;
        }
        int lowOff = ShipTransform.solidBounds()[1] - ShipTransform.HY;
        float base = level.random.nextFloat() * Mth.TWO_PI;
        BlockPos bestAnchor = null;
        int bestR = 0;
        Vec3 bestHeading = null;
        int bestRaise = Integer.MAX_VALUE;
        for (int i = 0; i < 9 && bestRaise > 0; i++) {
            double ang = base + i * Mth.TWO_PI / 9.0D;
            double dist = 48 + level.random.nextInt(33);
            double tx = center.x + Math.cos(ang) * dist;
            double tz = center.z + Math.sin(ang) * dist;
            Vec3 heading = new Vec3(center.x - tx, 0, center.z - tz).normalize();
            float yawArrive = (float) Math.toDegrees(Math.atan2(-heading.x, heading.z));
            int r = ShipTransform.nearestR(yawArrive);
            BlockPos anchor = BlockPos.containing(tx, ShipTransform.DEFAULT_ANCHOR_Y, tz);
            int h = terrainMax(level, ShipTransform.worldAABB(anchor, r));
            int need = h + 8 - lowOff;
            int raise = Math.max(0, need - anchor.getY());
            if (anchor.getY() + raise > ShipTransform.MAX_ANCHOR_Y) {
                continue;
            }
            if (raise < bestRaise) {
                bestRaise = raise;
                bestAnchor = anchor.above(raise);
                bestR = r;
                bestHeading = heading;
            }
        }
        if (bestAnchor == null) {
            bestAnchor = BlockPos.containing(center.x, ShipTransform.MAX_ANCHOR_Y, center.z);
            bestHeading = new Vec3(0, 0, -1);
            bestR = 0;
        }
        int view = level.getServer().getPlayerList().getViewDistance();
        double d = Mth.clamp(0.85D * view * 16.0D, 96.0D, 180.0D);
        Vec3 to = ShipTransform.entityPos(bestAnchor);
        Vec3 from = to.subtract(bestHeading.scale(d));
        return spawnArriving(level, bestAnchor, bestR, from, (float) Math.toDegrees(Math.atan2(-bestHeading.x, bestHeading.z)));
    }

    /** Crea la nave en el punto de salida del salto, en estado HYPERSPACE_IN. */
    public static DestroyerEntity spawnArriving(ServerLevel level, BlockPos anchor, int r, Vec3 from, float yaw) {
        DestroyerEntity s = ModEntities.DESTROYER.get().create(level);
        s.valid = true;
        s.setAnchor(anchor, r);
        s.hyperFrom = from;
        s.hyperTo = ShipTransform.entityPos(anchor);
        s.hyperYaw = yaw;
        s.moveTo(from.x, from.y, from.z, yaw, 0.0F);
        s.entityData.set(STATE, HYPERSPACE_IN);
        s.setFlag(FLAG_ENGINES, true);
        ChunkPos c = new ChunkPos(BlockPos.containing(from));
        level.setChunkForced(c.x, c.z, true);
        s.forced.add(c.toLong());
        level.getChunk(c.x, c.z);
        level.addFreshEntity(s);
        s.saveToData();
        LOGGER.info("[Destructor] Llegada: salto desde {} hacia el ancla {} (r={})", BlockPos.containing(from), anchor, r);
        return s;
    }

    /** Recrea la entidad de una nave anclada que desaparecio (sus bloques siguen en el mundo). */
    public void restoreAnchored(BlockPos anchor, int r) {
        setAnchor(anchor, r);
        Vec3 p = ShipTransform.entityPos(anchor);
        moveTo(p.x, p.y, p.z, ShipTransform.yawFor(r), 0.0F);
        crewSpawned = true;
        valid = true;
        entityData.set(STATE, ANCHORED);
        saveToData();
    }

    /** El destructor del mundo, si esta cargado. */
    @Nullable
    public static DestroyerEntity find(ServerLevel level) {
        ShipData d = ShipData.get(level.getServer());
        if (d.ship == null) {
            return null;
        }
        Entity e = level.getEntity(d.ship);
        return e instanceof DestroyerEntity s && !s.isRemoved() ? s : null;
    }

    // ------------------------------------------------------------------ cliente: interpolacion

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYRot = yRot;
        lerpXRot = xRot;
        lerpSteps = Math.max(1, steps);
    }

    @Override
    public double lerpTargetX() {
        return lerpSteps > 0 ? lerpX : getX();
    }

    @Override
    public double lerpTargetY() {
        return lerpSteps > 0 ? lerpY : getY();
    }

    @Override
    public double lerpTargetZ() {
        return lerpSteps > 0 ? lerpZ : getZ();
    }

    @Override
    public float lerpTargetXRot() {
        return lerpSteps > 0 ? (float) lerpXRot : getXRot();
    }

    @Override
    public float lerpTargetYRot() {
        return lerpSteps > 0 ? (float) lerpYRot : getYRot();
    }

    private void tickLerp() {
        if (lerpSteps > 0) {
            lerpPositionAndRotationStep(lerpSteps, lerpX, lerpY, lerpZ, lerpYRot, lerpXRot);
            lerpSteps--;
        }
    }

    // ------------------------------------------------------------------ NBT

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        int s = tag.getInt("state");
        setAnchor(BlockPos.of(tag.getLong("anchor")), tag.getInt("rot"));
        entityData.set(FLAGS, tag.getInt("flags"));
        stateTicks = tag.getInt("stateTicks");
        jobCursor = tag.getInt("jobCursor");
        pilotId = tag.hasUUID("pilot") ? tag.getUUID("pilot") : null;
        crewSpawned = tag.getBoolean("crewSpawned");
        valid = tag.getBoolean("valid");
        hyperFrom = new Vec3(tag.getDouble("hfx"), tag.getDouble("hfy"), tag.getDouble("hfz"));
        hyperTo = new Vec3(tag.getDouble("htx"), tag.getDouble("hty"), tag.getDouble("htz"));
        hyperYaw = tag.getFloat("hyaw");
        alignAnchor = BlockPos.of(tag.getLong("alignAnchor"));
        alignR = tag.getInt("alignR");
        destroyWave = tag.getInt("destroyWave");
        fallTicks = tag.contains("fallTicks") ? tag.getInt("fallTicks") : -1;
        riders.clear();
        for (Tag t : tag.getList("riders", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            if (c.hasUUID("id")) {
                riders.put(c.getUUID("id"), new Rider(new Vec3(c.getFloat("x"), c.getFloat("y"), c.getFloat("z")),
                        c.getFloat("yaw"), c.getBoolean("noAi")));
            }
        }
        crew.clear();
        CompoundTag cr = tag.getCompound("crew");
        for (String k : cr.getAllKeys()) {
            if (cr.hasUUID(k)) {
                crew.put(k, cr.getUUID(k));
            }
        }
        forced.clear();
        for (long l : tag.getLongArray("forced")) {
            forced.add(l);
        }
        // reanudacion: ALIGN pasa a FLYING con el piloto automatico parado; colocar se relanza entero
        if (s == ALIGN) {
            s = FLYING;
        }
        if (s == MATERIALIZE || s == ANCHORING) {
            jobCursor = 0;
        }
        captured = s == LIFTOFF && (jobCursor > 0 || !riders.isEmpty());
        entityData.set(STATE, s);
        job = null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("version", ShipDesign.VERSION);
        tag.putInt("state", getState());
        tag.putLong("anchor", getAnchor().asLong());
        tag.putInt("rot", getRot());
        tag.putInt("flags", getFlags());
        tag.putInt("stateTicks", stateTicks);
        tag.putInt("jobCursor", job != null ? job.cursor() : jobCursor);
        if (pilotId != null) {
            tag.putUUID("pilot", pilotId);
        }
        tag.putBoolean("crewSpawned", crewSpawned);
        tag.putBoolean("valid", valid);
        tag.putDouble("hfx", hyperFrom.x);
        tag.putDouble("hfy", hyperFrom.y);
        tag.putDouble("hfz", hyperFrom.z);
        tag.putDouble("htx", hyperTo.x);
        tag.putDouble("hty", hyperTo.y);
        tag.putDouble("htz", hyperTo.z);
        tag.putFloat("hyaw", hyperYaw);
        tag.putLong("alignAnchor", alignAnchor.asLong());
        tag.putInt("alignR", alignR);
        tag.putInt("destroyWave", destroyWave);
        tag.putInt("fallTicks", fallTicks);
        ListTag rl = new ListTag();
        riders.forEach((id, r) -> {
            CompoundTag c = new CompoundTag();
            c.putUUID("id", id);
            c.putFloat("x", r.x);
            c.putFloat("y", r.y);
            c.putFloat("z", r.z);
            c.putFloat("yaw", r.yaw);
            c.putBoolean("noAi", r.noAi);
            rl.add(c);
        });
        tag.put("riders", rl);
        CompoundTag cr = new CompoundTag();
        crew.forEach(cr::putUUID);
        tag.put("crew", cr);
        tag.putLongArray("forced", forced.toLongArray());
    }
}
