package com.keinloree.entity;

import com.keinloree.util.Fx;
import com.keinloree.world.Builds;
import com.keinloree.world.LegionOps;
import net.minecraft.core.BlockPos;
import com.keinloree.registry.ModParticles;
import com.keinloree.vfx.Vfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Nave de carga de Kein Loree. Sale del hangar de la nave nodriza, vuela hasta unas coordenadas,
 * descarga su contenedor (una base entera que se "imprime" bajo su haz), aterriza, suelta a la guarnicion
 * y regresa a su atraque. Todo el vuelo lo calcula el servidor; el cliente solo dibuja el estado sincronizado.
 * Origen de la entidad = pie de las patas de aterrizaje, centrado.
 */
public class CargoShip extends Mob {

    public static final int PH_DOCKED = 0;
    public static final int PH_EXIT = 1;
    public static final int PH_CRUISE = 2;
    public static final int PH_HOVER = 3;
    public static final int PH_LAND = 4;
    public static final int PH_UNLOAD = 5;
    public static final int PH_LIFT = 6;
    public static final int PH_RETURN = 7;
    public static final int PH_DOCKING = 8;
    /** Se aleja hacia el cielo y desaparece (cuando la nave nodriza no esta cargada). */
    public static final int PH_LEAVE = 9;
    /** Pilotada por un jugador desde la cabina. */
    public static final int PH_PILOT = 10;
    /** Aparcada (tras bajarse el piloto): se posa en el suelo y espera. */
    public static final int PH_PARKED = 11;

    private static final EntityDataAccessor<Float> GEAR =
            SynchedEntityData.defineId(CargoShip.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RAMP =
            SynchedEntityData.defineId(CargoShip.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> THRUST =
            SynchedEntityData.defineId(CargoShip.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LIFT =
            SynchedEntityData.defineId(CargoShip.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ROLL =
            SynchedEntityData.defineId(CargoShip.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> CARGO =
            SynchedEntityData.defineId(CargoShip.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BEAM =
            SynchedEntityData.defineId(CargoShip.class, EntityDataSerializers.BOOLEAN);


    /** Distancia al suelo del vientre del casco con las patas desplegadas (en bloques). */
    public static final double BELLY = 1.5D;

    // --- estado de la mision (solo servidor, se guarda)
    private int phase = PH_DOCKED;
    private int phaseTick;
    private Vec3 dockPos = Vec3.ZERO;
    private Vec3 exitPos = Vec3.ZERO;
    private Vec3 hoverPos = Vec3.ZERO;
    private Vec3 landPos = Vec3.ZERO;
    private BlockPos base = BlockPos.ZERO;
    private boolean fort;
    private boolean lander;
    private boolean builder;
    private boolean jobStarted;
    private boolean released;
    private long jobId;
    private int holdTicks;
    private float landYaw = 180.0F;
    private Vec3 vel = Vec3.ZERO;
    private float targetYaw;

    // --- animacion (cliente y servidor)
    private float gear;
    private float gearO;
    private float ramp;
    private float rampO;
    private float thrust;
    private float thrustO;
    private float lift;
    private float liftO;
    private float roll;
    private float rollO;

    public CargoShip(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setPersistenceRequired();
        // El control de mirada vanilla pone el cabeceo a 0 en cada tick; aqui el cabeceo lo decide el vuelo.
        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
            }
        };
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 8.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GEAR, 0.0F);
        builder.define(RAMP, 0.0F);
        builder.define(THRUST, 0.0F);
        builder.define(LIFT, 0.0F);
        builder.define(ROLL, 0.0F);
        builder.define(CARGO, true);
        builder.define(BEAM, false);
    }

    // ------------------------------------------------------------------ lo que dibuja el modelo

    public float getGear(float partial) {
        return Mth.lerp(partial, this.gearO, this.gear);
    }

    public float getRamp(float partial) {
        return Mth.lerp(partial, this.rampO, this.ramp);
    }

    public float getThrust(float partial) {
        return Mth.lerp(partial, this.thrustO, this.thrust);
    }

    public float getLift(float partial) {
        return Mth.lerp(partial, this.liftO, this.lift);
    }

    /** Alabeo en grados (positivo = el lado izquierdo baja). */
    public float getRoll(float partial) {
        return Mth.lerp(partial, this.rollO, this.roll);
    }

    public boolean hasCargo() {
        return this.entityData.get(CARGO);
    }

    public boolean isBeam() {
        return this.entityData.get(BEAM);
    }

    public int getPhase() {
        return this.phase;
    }

    public boolean isDocked() {
        return this.phase == PH_DOCKED;
    }

    // ------------------------------------------------------------------ comportamiento general

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this) {
            @Override
            public void clientTick() {
                CargoShip.this.yBodyRot = CargoShip.this.getYRot();
                CargoShip.this.yHeadRot = CargoShip.this.getYRot();
            }
        };
    }

    // ------------------------------------------------------------------ pilotaje

    @Override
    protected net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if (this.isVehicle() || player.isSecondaryUseActive()) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        boolean idle = this.phase == PH_DOCKED || this.phase == PH_UNLOAD || this.phase == PH_PARKED;
        if (!this.level().isClientSide) {
            boolean allowed = player.isCreative() || com.keinloree.story.Factions.legionAlly(player)
                    || com.keinloree.story.Campaign.get(player.getServer()).hasFlag("cargo_unlocked");
            if (!allowed || !idle) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.keinloree.cargo_locked"), true);
                return net.minecraft.world.InteractionResult.CONSUME;
            }
            if (player.startRiding(this)) {
                this.mission = "player";
                this.setPhase(PH_PILOT);
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.keinloree.cargo_keys"), true);
            }
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /** El jugador sentado en la cabina (la nave la mueve el servidor con su entrada). */
    @javax.annotation.Nullable
    public net.minecraft.world.entity.player.Player pilot() {
        return this.getFirstPassenger() instanceof net.minecraft.world.entity.player.Player p ? p : null;
    }

    @Override
    protected void positionRider(net.minecraft.world.entity.Entity passenger, net.minecraft.world.entity.Entity.MoveFunction move) {
        Vec3 seat = this.local(0.0D, 0.9D, 3.2D);
        move.accept(passenger, seat.x, seat.y, seat.z);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(net.minecraft.world.entity.LivingEntity passenger) {
        return this.local(2.6D, 0.2D, 0.0D);
    }

    private void tickPilot() {
        net.minecraft.world.entity.player.Player pilot = this.pilot();
        if (pilot == null) {
            this.setPhase(PH_PARKED);
            return;
        }
        float forward = pilot.zza;
        float strafe = pilot.xxa;
        double speed = 0.9D;
        Vec3 look = Vec3.directionFromRotation(pilot.getXRot(), pilot.getYRot());
        Vec3 side = new Vec3(Math.cos(Math.toRadians(pilot.getYRot())), 0.0D, Math.sin(Math.toRadians(pilot.getYRot())));
        Vec3 want = look.scale(forward * speed).add(side.scale(strafe * speed * 0.5D));
        Vec3 dv = want.subtract(this.vel);
        double dl = dv.length();
        if (dl > 0.05D) {
            dv = dv.scale(0.05D / dl);
        }
        this.vel = this.vel.add(dv);
        // choques sencillos: no atraviesa el terreno
        AABB box = this.getBoundingBox();
        if (!this.level().noCollision(this, box.move(this.vel.x, 0.0D, 0.0D))) {
            this.vel = new Vec3(0.0D, this.vel.y, this.vel.z);
        }
        if (!this.level().noCollision(this, box.move(0.0D, this.vel.y, 0.0D))) {
            this.vel = new Vec3(this.vel.x, 0.0D, this.vel.z);
        }
        if (!this.level().noCollision(this, box.move(0.0D, 0.0D, this.vel.z))) {
            this.vel = new Vec3(this.vel.x, this.vel.y, 0.0D);
        }
        this.setDeltaMovement(this.vel);
        float oldYaw = this.getYRot();
        float turn = Mth.clamp(Mth.wrapDegrees(pilot.getYRot() - oldYaw), -3.5F, 3.5F);
        this.setYRot(oldYaw + turn);
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
        this.roll = approach(this.roll, Mth.clamp(-turn * 5.0F, -20.0F, 20.0F), 1.5F);
        this.thrust = approach(this.thrust, Math.abs(forward), 0.04F);
        this.lift = approach(this.lift, 0.5F, 0.03F);
        boolean nearGround = !this.level().noCollision(this, box.move(0.0D, -2.0D, 0.0D));
        this.gear = approach(this.gear, nearGround ? 1.0F : 0.0F, 0.05F);
        this.ramp = approach(this.ramp, 0.0F, 0.05F);
        this.setCargo(false);
    }

    private void tickParked() {
        this.thrust = approach(this.thrust, 0.0F, 0.04F);
        this.gear = approach(this.gear, 1.0F, 0.05F);
        this.roll = approach(this.roll, 0.0F, 1.5F);
        boolean onGround = !this.level().noCollision(this, this.getBoundingBox().move(0.0D, -0.2D, 0.0D));
        this.vel = onGround ? Vec3.ZERO : new Vec3(0.0D, -0.15D, 0.0D);
        this.setDeltaMovement(this.vel);
        if (this.pilot() != null) {
            this.setPhase(PH_PILOT);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Solo el vacio y /kill pueden con ella.
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(3.5D, 2.5D, 3.5D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("phase", this.phase);
        tag.putInt("phaseTick", this.phaseTick);
        putVec(tag, "dock", this.dockPos);
        putVec(tag, "exit", this.exitPos);
        putVec(tag, "hover", this.hoverPos);
        putVec(tag, "land", this.landPos);
        tag.putInt("baseX", this.base.getX());
        tag.putInt("baseY", this.base.getY());
        tag.putInt("baseZ", this.base.getZ());
        tag.putBoolean("fort", this.fort);
        tag.putBoolean("lander", this.lander);
        tag.putBoolean("builder", this.builder);
        tag.putBoolean("jobStarted", this.jobStarted);
        tag.putBoolean("released", this.released);
        tag.putLong("job", this.jobId);
        tag.putInt("hold", this.holdTicks);
        tag.putFloat("landYaw", this.landYaw);
        tag.putBoolean("cargo", this.hasCargo());
        tag.putString("mission", this.mission);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.phase = tag.getInt("phase");
        this.phaseTick = tag.getInt("phaseTick");
        this.dockPos = getVec(tag, "dock");
        this.exitPos = getVec(tag, "exit");
        this.hoverPos = getVec(tag, "hover");
        this.landPos = getVec(tag, "land");
        this.base = new BlockPos(tag.getInt("baseX"), tag.getInt("baseY"), tag.getInt("baseZ"));
        this.fort = tag.getBoolean("fort");
        this.lander = tag.getBoolean("lander");
        this.builder = tag.getBoolean("builder");
        this.jobStarted = tag.getBoolean("jobStarted");
        this.released = tag.getBoolean("released");
        this.jobId = tag.getLong("job");
        this.holdTicks = tag.getInt("hold");
        this.landYaw = tag.contains("landYaw") ? tag.getFloat("landYaw") : 180.0F;
        this.entityData.set(CARGO, !tag.contains("cargo") || tag.getBoolean("cargo"));
        this.targetYaw = this.getYRot();
        this.mission = tag.getString("mission");
    }

    private static void putVec(CompoundTag tag, String key, Vec3 v) {
        tag.putDouble(key + "X", v.x);
        tag.putDouble(key + "Y", v.y);
        tag.putDouble(key + "Z", v.z);
    }

    private static Vec3 getVec(CompoundTag tag, String key) {
        return new Vec3(tag.getDouble(key + "X"), tag.getDouble(key + "Y"), tag.getDouble(key + "Z"));
    }

    // ------------------------------------------------------------------ ordenes de la historia

    /** Mision de la historia ("" = base normal; "patrol:...", "razako", "pickup", "shuttle"): ver LegionOps. */
    public String mission = "";

    /** Las naves de mision no vuelven a un atraque: se marchan al cielo y desaparecen. */
    public boolean isTransient() {
        return !this.mission.isEmpty();
    }

    /** Pruebas e historia: fuerza el despegue y la marcha (sale de cualquier fase). */
    public void leaveNow() {
        this.mission = this.mission.isEmpty() ? "pickup" : this.mission;
        this.landPos = this.position();
        this.setPhase(PH_LIFT);
    }

    /** Fija el atraque de origen (donde duerme entre misiones). */
    public void setDock(Vec3 dock) {
        this.dockPos = dock;
    }

    public Vec3 getDock() {
        return this.dockPos;
    }

    public void setCargo(boolean cargo) {
        this.entityData.set(CARGO, cargo);
    }

    /**
     * Inicia una mision: sale del atraque, vuela a `hover` y, si es constructora, ordena la obra de la base.
     * Si aterriza, baja a `land` con la rampa hacia atras. Un escolta solo vuela, espera y regresa.
     */
    public void dispatch(Vec3 exit, Vec3 hover, Vec3 land, BlockPos baseGround, boolean fortress,
                         boolean willLand, boolean willBuild, int escortHold, float landingYaw) {
        this.exitPos = exit;
        this.hoverPos = hover;
        this.landPos = land;
        this.base = baseGround;
        this.fort = fortress;
        this.lander = willLand;
        this.builder = willBuild;
        this.holdTicks = escortHold;
        this.landYaw = landingYaw;
        this.jobStarted = false;
        this.released = false;
        this.jobId = 0L;
        this.setCargo(true);
        this.phase = PH_EXIT;
        this.phaseTick = 0;
        this.targetYaw = (float) (Mth.atan2(-(hover.x - this.getX()), hover.z - this.getZ()) * (180.0D / Math.PI));
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void aiStep() {
        this.gearO = this.gear;
        this.rampO = this.ramp;
        this.thrustO = this.thrust;
        this.liftO = this.lift;
        this.rollO = this.roll;
        if (!this.level().isClientSide && this.level() instanceof ServerLevel level) {
            this.serverTick(level);
        } else {
            this.gear = this.entityData.get(GEAR);
            this.ramp = this.entityData.get(RAMP);
            this.thrust = this.entityData.get(THRUST);
            this.lift = this.entityData.get(LIFT);
            this.roll = this.entityData.get(ROLL);
            this.clientFx();
        }
        super.aiStep();
    }

    private void serverTick(ServerLevel level) {
        this.phaseTick++;
        switch (this.phase) {
            case PH_DOCKED -> this.tickDocked();
            case PH_EXIT -> this.tickExit(level);
            case PH_CRUISE -> this.tickCruise(level);
            case PH_HOVER -> this.tickHover(level);
            case PH_LAND -> this.tickLand();
            case PH_UNLOAD -> this.tickUnload(level);
            case PH_LIFT -> this.tickLift();
            case PH_RETURN -> this.tickReturn(level);
            case PH_DOCKING -> this.tickDocking();
            case PH_LEAVE -> this.tickLeave();
            case PH_PILOT -> this.tickPilot();
            case PH_PARKED -> this.tickParked();
            default -> this.phase = PH_DOCKED;
        }
        this.entityData.set(GEAR, this.gear);
        this.entityData.set(RAMP, this.ramp);
        this.entityData.set(THRUST, this.thrust);
        this.entityData.set(LIFT, this.lift);
        this.entityData.set(ROLL, this.roll);
    }

    private void tickDocked() {
        if (this.dockPos.equals(Vec3.ZERO)) {
            this.dockPos = this.position();
        }
        this.entityData.set(BEAM, false);
        this.gear = approach(this.gear, 0.0F, 0.05F);
        this.ramp = approach(this.ramp, 0.0F, 0.05F);
        this.thrust = approach(this.thrust, 0.0F, 0.05F);
        this.lift = approach(this.lift, 0.0F, 0.05F);
        double bob = Math.sin(this.tickCount * 0.05D) * 0.05D;
        this.steer(this.dockPos.add(0.0D, bob, 0.0D), 0.06D, 0.02D, 2.0D, 180.0F);
    }

    private void tickExit(ServerLevel level) {
        if (this.phaseTick == 1) {
            Fx.sound(level, this.position(), SoundEvents.BEACON_ACTIVATE, 3.0F, 1.6F);
        }
        this.gear = approach(this.gear, 0.0F, 0.05F);
        this.thrust = approach(this.thrust, 0.6F, 0.04F);
        this.lift = approach(this.lift, 0.7F, 0.04F);
        double speed = 0.12D + Math.min(0.5D, this.phaseTick * 0.012D);
        this.steer(this.exitPos, speed, 0.03D, 3.0D, this.targetYaw);
        if (this.position().distanceToSqr(this.exitPos) < 4.0D || this.phaseTick > 200) {
            this.setPhase(PH_CRUISE);
        }
    }

    private void tickCruise(ServerLevel level) {
        this.thrust = approach(this.thrust, 1.0F, 0.04F);
        this.lift = approach(this.lift, 0.0F, 0.04F);
        double dx = this.hoverPos.x - this.getX();
        double dz = this.hoverPos.z - this.getZ();
        float heading = (float) (Mth.atan2(-dx, dz) * (180.0D / Math.PI));
        this.steer(this.hoverPos, 1.15D, 0.05D, 34.0D, heading);
        if (this.position().distanceToSqr(this.hoverPos) < 2.5D || this.phaseTick > 900) {
            this.setPhase(PH_HOVER);
        }
    }

    private void tickHover(ServerLevel level) {
        this.thrust = approach(this.thrust, 0.25F, 0.03F);
        this.lift = approach(this.lift, 0.55F, 0.03F);
        double bob = Math.sin(this.tickCount * 0.07D) * 0.12D;
        this.steer(this.hoverPos.add(0.0D, bob, 0.0D), 0.25D, 0.03D, 4.0D, this.landYaw);
        if (this.phaseTick < 40) {
            return;
        }
        if (this.builder && !this.jobStarted) {
            this.jobStarted = true;
            this.jobId = LegionOps.onCargoArrived(level, this);
            this.entityData.set(BEAM, true);
            Fx.sound(level, this.position(), SoundEvents.BEACON_POWER_SELECT, 3.0F, 0.7F);
        }
        boolean working;
        if (this.builder) {
            working = Builds.isActive(this.jobId);
        } else {
            working = this.phaseTick < this.holdTicks;
        }
        if (this.entityData.get(BEAM)) {
            this.beamFx(level);
        }
        if (!working) {
            this.entityData.set(BEAM, false);
            if (this.hasCargo()) {
                this.setCargo(false);
                Fx.burstFar(level, Fx.BRIGHT, this.position().add(0.0D, 1.0D, 0.0D), 40, 2.0D, 0.3D);
                Fx.sound(level, this.position(), SoundEvents.ENDERMAN_TELEPORT, 2.0F, 0.6F);
            }
            this.setPhase(this.lander ? PH_LAND : PH_RETURN);
        }
    }

    private void tickLand() {
        this.thrust = approach(this.thrust, 0.15F, 0.03F);
        this.lift = approach(this.lift, 0.8F, 0.03F);
        this.gear = approach(this.gear, 1.0F, 0.04F);
        Vec3 above = new Vec3(this.landPos.x, this.landPos.y + 6.0D, this.landPos.z);
        // Primero se coloca sobre el punto; despues baja despacio.
        Vec3 goal = Math.abs(this.getX() - this.landPos.x) + Math.abs(this.getZ() - this.landPos.z) > 1.2D
                && this.getY() > this.landPos.y + 5.0D ? above : this.landPos;
        this.steer(goal, 0.32D, 0.02D, 7.0D, this.landYaw);
        if (this.position().distanceToSqr(this.landPos) < 0.04D || this.phaseTick > 400) {
            this.setPos(this.landPos.x, this.landPos.y, this.landPos.z);
            this.vel = Vec3.ZERO;
            this.setPhase(PH_UNLOAD);
        }
    }

    private void tickUnload(ServerLevel level) {
        this.thrust = approach(this.thrust, 0.0F, 0.04F);
        this.lift = approach(this.lift, 0.0F, 0.04F);
        this.gear = 1.0F;
        this.vel = Vec3.ZERO;
        if (this.phaseTick <= 30) {
            this.ramp = approach(this.ramp, 1.0F, 0.04F);
        }
        if (this.phaseTick == 34) {
            Fx.sound(level, this.position(), SoundEvents.IRON_DOOR_OPEN, 2.0F, 0.5F);
            LegionOps.onCargoLanded(level, this);
        }
        if (this.phaseTick >= 120) {
            this.ramp = approach(this.ramp, 0.0F, 0.04F);
        }
        if (this.phaseTick >= 160) {
            this.setPhase(PH_LIFT);
        }
    }

    private void tickLift() {
        this.thrust = approach(this.thrust, 0.3F, 0.03F);
        this.lift = approach(this.lift, 0.9F, 0.03F);
        Vec3 goal = new Vec3(this.landPos.x, this.landPos.y + 16.0D, this.landPos.z);
        this.steer(goal, 0.4D, 0.02D, 6.0D, this.landYaw);
        if (this.getY() > this.landPos.y + 6.0D) {
            this.gear = approach(this.gear, 0.0F, 0.05F);
        }
        if (this.position().distanceToSqr(goal) < 2.0D || this.phaseTick > 200) {
            this.setPhase(PH_RETURN);
        }
    }

    private void tickReturn(ServerLevel level) {
        this.thrust = approach(this.thrust, 1.0F, 0.04F);
        this.lift = approach(this.lift, 0.0F, 0.04F);
        this.gear = approach(this.gear, 0.0F, 0.05F);
        this.ramp = approach(this.ramp, 0.0F, 0.05F);
        Vec3 approachPoint = this.dockPos.add(0.0D, -22.0D, 0.0D);
        if (this.phaseTick == 1 && (!level.isLoaded(BlockPos.containing(this.dockPos)) || this.isTransient())) {
            this.setPhase(PH_LEAVE);
            return;
        }
        double dx = approachPoint.x - this.getX();
        double dz = approachPoint.z - this.getZ();
        float heading = (float) (Mth.atan2(-dx, dz) * (180.0D / Math.PI));
        this.steer(approachPoint, 1.1D, 0.05D, 30.0D, heading);
        if (this.position().distanceToSqr(approachPoint) < 6.0D || this.phaseTick > 900) {
            this.setPhase(PH_DOCKING);
        }
    }

    private void tickDocking() {
        this.thrust = approach(this.thrust, 0.2F, 0.04F);
        this.lift = approach(this.lift, 0.7F, 0.04F);
        this.steer(this.dockPos, 0.3D, 0.025D, 8.0D, 180.0F);
        if (this.position().distanceToSqr(this.dockPos) < 0.09D || this.phaseTick > 400) {
            this.setPos(this.dockPos.x, this.dockPos.y, this.dockPos.z);
            this.vel = Vec3.ZERO;
            this.setCargo(true);
            this.setPhase(PH_DOCKED);
        }
    }

    private void tickLeave() {
        this.thrust = approach(this.thrust, 1.0F, 0.04F);
        Vec3 goal = this.position().add(0.0D, 60.0D, 0.0D);
        this.steer(goal, 0.9D, 0.05D, 1.0D, this.getYRot());
        if (this.phaseTick > 140) {
            this.discard();
        }
    }

    private void setPhase(int p) {
        this.phase = p;
        this.phaseTick = 0;
    }

    // ------------------------------------------------------------------ movimiento

    /**
     * Acelera hacia el punto con un perfil suave (frena al acercarse), gira hacia `yaw` y calcula
     * cabeceo y alabeo a partir del movimiento.
     */
    private void steer(Vec3 goal, double maxSpeed, double accel, double slowRadius, float yaw) {
        Vec3 d = goal.subtract(this.position());
        double dist = d.length();
        double want = dist < slowRadius ? maxSpeed * Math.max(0.12D, dist / slowRadius) : maxSpeed;
        Vec3 desired = dist < 1.0E-6D ? Vec3.ZERO : d.scale(want / dist);
        Vec3 dv = desired.subtract(this.vel);
        double dl = dv.length();
        if (dl > accel) {
            dv = dv.scale(accel / dl);
        }
        this.vel = this.vel.add(dv);
        this.setDeltaMovement(this.vel);

        float oldYaw = this.getYRot();
        float diff = Mth.wrapDegrees(yaw - oldYaw);
        float turn = Mth.clamp(diff, -4.5F, 4.5F);
        float newYaw = oldYaw + turn;
        this.setYRot(newYaw);
        this.yBodyRot = newYaw;
        this.yHeadRot = newYaw;

        double horizontal = Math.sqrt(this.vel.x * this.vel.x + this.vel.z * this.vel.z);
        float pitch = (float) Mth.clamp(-this.vel.y * 30.0D + horizontal * 6.0D, -22.0D, 22.0D);
        this.setXRot(approach(this.getXRot(), pitch, 1.2F));
        float bank = Mth.clamp(-turn * 5.0F, -24.0F, 24.0F);
        this.roll = approach(this.roll, bank, 1.5F);
    }

    private static float approach(float value, float goal, float step) {
        if (value < goal) {
            return Math.min(goal, value + step);
        }
        return Math.max(goal, value - step);
    }

    // ------------------------------------------------------------------ efectos

    /** Punto del mundo a (izquierda, arriba, adelante) del origen de la nave (sin cabeceo ni alabeo). */
    public Vec3 local(double left, double up, double forward) {
        double yaw = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double lx = Math.cos(yaw);
        double lz = Math.sin(yaw);
        return new Vec3(this.getX() + lx * left + fx * forward, this.getY() + up, this.getZ() + lz * left + fz * forward);
    }

    private void clientFx() {
        if (this.thrust > 0.08F) {
            double yaw = Math.toRadians(this.getYRot());
            double bx = Math.sin(yaw) * 0.25D * this.thrust;
            double bz = -Math.cos(yaw) * 0.25D * this.thrust;
            for (int s = -1; s <= 1; s += 2) {
                Vec3 n = this.local(s * 3.75D, 2.5D, -6.1D);
                this.level().addParticle(ModParticles.EMBER.get(), n.x, n.y, n.z, bx, 0.0D, bz);
                if (this.thrust > 0.5F && this.random.nextInt(2) == 0) {
                    this.level().addParticle(ModParticles.SPARK.get(), n.x, n.y, n.z, bx * 1.4D, 0.0D, bz * 1.4D);
                }
            }
        }
        if (this.lift > 0.08F) {
            double[][] spots = {{3.75D, 1.2D, -2.75D}, {-3.75D, 1.2D, -2.75D}, {0.0D, 1.2D, 4.25D}};
            for (double[] sp : spots) {
                Vec3 n = this.local(sp[0], sp[1], sp[2]);
                this.level().addParticle(ModParticles.EMBER.get(), n.x, n.y, n.z, 0.0D, -0.2D * this.lift, 0.0D);
                if (this.tickCount % 3 == 0) {
                    this.level().addParticle(ModParticles.SMOKE.get(), n.x, n.y - 0.2D, n.z, 0.0D, -0.08D, 0.0D);
                }
            }
        }
    }

    /** Haz de escaneo verde desde el vientre de la nave hasta el centro de la base que se esta imprimiendo. */
    private void beamFx(ServerLevel level) {
        Vec3 from = this.local(0.0D, 1.2D, 4.25D);
        Vec3 to = new Vec3(this.base.getX() + 0.5D, this.base.getY() + 0.5D, this.base.getZ() + 0.5D);
        if (this.tickCount % 5 == 0) {
            Vfx.beam(level, from, to, Fx.GREEN.color(), this.fort ? 0.6F : 0.35F, 8);
        }
        for (int i = 0; i < 14; i++) {
            double t = this.random.nextDouble();
            Vec3 p = from.add(to.subtract(from).scale(t));
            double spread = 0.25D + 1.6D * t;
            Fx.burstFar(level, this.random.nextBoolean() ? Fx.CODE : Fx.GREEN,
                    p.add((this.random.nextDouble() - 0.5D) * spread, 0.0D, (this.random.nextDouble() - 0.5D) * spread),
                    1, 0.0D, 0.0D);
        }
        if (this.tickCount % 4 == 0) {
            Fx.ringFar(level, new Vec3(to.x, to.y + 0.3D, to.z), this.fort ? 24.0D : 12.0D, this.fort ? 56 : 40,
                    Fx.CODE, this.tickCount * 0.12D);
        }
    }

    // ------------------------------------------------------------------ datos para la historia

    public BlockPos getBase() {
        return this.base;
    }

    public boolean isFort() {
        return this.fort;
    }

    public float getLandYaw() {
        return this.landYaw;
    }

    public boolean isReleased() {
        return this.released;
    }

    public void markReleased() {
        this.released = true;
    }
}
