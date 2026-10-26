package com.keinloree.ship;

import com.keinloree.registry.ModEntities;
import com.keinloree.registry.ModSounds;
import com.keinloree.story.Campaign;
import com.keinloree.story.Factions;
import com.keinloree.vfx.Vfx;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Caza del Nexo (estilo TIE). Con jugador: el caza gira hacia donde mira el piloto y vuela en su cliente (como los
 * botes). Sin jugador: piloto de la Legion (IA de patrulla y pasadas de ataque) o aparcado.
 */
public class FighterEntity extends Entity {
    private static final EntityDataAccessor<Float> HEALTH = SynchedEntityData.defineId(FighterEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> NPC = SynchedEntityData.defineId(FighterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> THROTTLE = SynchedEntityData.defineId(FighterEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> LEGION = SynchedEntityData.defineId(FighterEntity.class, EntityDataSerializers.BOOLEAN);

    public static final float MAX_HEALTH = 40.0F;
    /** Ojo del piloto respecto a la entidad (bloques, sin girar). */
    public static final Vec3 EYE = new Vec3(0.0D, 1.806D, -0.21D);
    /** Mandos del piloto local que solo conoce el cliente (los escribe la clase de entrada del cliente). */
    public static volatile int clientButtons;

    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;

    private int fireCooldown;
    private int boostTicks;
    private int boostCooldown;
    private int gun;
    private int idleTicks;
    private int lastServerTick;
    private Vec3 lastServerPos = Vec3.ZERO;
    private double serverSpeed;
    @Nullable
    private BlockPos patrolCenter;
    private int attackRun;

    public FighterEntity(EntityType<? extends FighterEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.blocksBuilding = true;
    }

    public static FighterEntity spawn(ServerLevel level, Vec3 at, float yaw, boolean npcPilot) {
        FighterEntity f = new FighterEntity(ModEntities.FIGHTER.get(), level);
        f.moveTo(at.x, at.y, at.z, yaw, 0.0F);
        f.entityData.set(NPC, npcPilot);
        f.entityData.set(LEGION, true);
        f.entityData.set(THROTTLE, npcPilot ? 0.6F : 0.0F);
        f.patrolCenter = BlockPos.containing(at);
        level.addFreshEntity(f);
        return f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(HEALTH, MAX_HEALTH);
        b.define(NPC, false);
        b.define(THROTTLE, 0.0F);
        b.define(LEGION, true);
    }

    public boolean hasNpcPilot() {
        return entityData.get(NPC);
    }

    /** Caza de la Legion (con piloto IA o aparcado sin dueño). Robado por un jugador deja de serlo. */
    public boolean isLegionCraft() {
        return entityData.get(LEGION);
    }

    public float health() {
        return entityData.get(HEALTH);
    }

    public float throttle() {
        return entityData.get(THROTTLE);
    }

    public void setPatrolCenter(BlockPos p) {
        patrolCenter = p;
    }

    // ------------------------------------------------------------------ montar

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive() || isVehicle() || hasNpcPilot()) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide) {
            boolean allowed = player.isCreative() || Factions.legionAlly(player)
                    || Campaign.get(player.getServer()).hasFlag("fighters_unlocked");
            if (!allowed) {
                player.displayClientMessage(Component.translatable("message.keinloree.fighter_locked"), true);
                return InteractionResult.CONSUME;
            }
            if (player.startRiding(this)) {
                entityData.set(LEGION, Factions.legionAlly(player));
                entityData.set(THROTTLE, 0.0F);
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player p ? p : null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty() && passenger instanceof Player;
    }

    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction move) {
        if (!hasPassenger(passenger)) {
            return;
        }
        Vec3 off = EYE.yRot((float) Math.toRadians(-getYRot()));
        double eye = passenger.getEyeHeight();
        move.accept(passenger, getX() + off.x, getY() + off.y - eye, getZ() + off.z);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 side = Vec3.directionFromRotation(0.0F, getYRot() + 90.0F).scale(2.6D);
        return position().add(side);
    }

    /** Solo se puede saltar si va despacio, cerca del suelo o muy dañado. */
    public boolean canEject() {
        double ground = level().getHeight(Heightmap.Types.MOTION_BLOCKING, getBlockX(), getBlockZ());
        return speed() < 0.25D || getY() - ground < 3.0D || health() < MAX_HEALTH * 0.25F || throttle() < 0.05F;
    }

    private double speed() {
        if (level().isClientSide) {
            return getDeltaMovement().length();
        }
        return serverSpeed;
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        if (fireCooldown > 0) {
            fireCooldown--;
        }
        if (boostCooldown > 0) {
            boostCooldown--;
        }
        if (isControlledByLocalInstance()) {
            lerpSteps = 0;
            syncPacketPositionCodec(getX(), getY(), getZ());
            if (getControllingPassenger() instanceof Player pilot) {
                pilotControl(pilot);
            } else if (!level().isClientSide) {
                if (hasNpcPilot()) {
                    aiControl((ServerLevel) level());
                } else {
                    setDeltaMovement(getDeltaMovement().scale(0.8D));
                }
            }
            move(MoverType.SELF, getDeltaMovement());
        } else {
            if (lerpSteps > 0) {
                lerpPositionAndRotationStep(lerpSteps, lerpX, lerpY, lerpZ, lerpYRot, lerpXRot);
                lerpSteps--;
            }
        }
        if (!level().isClientSide) {
            serverTick((ServerLevel) level());
        }
    }

    /** Vuelo del piloto jugador (corre en su cliente). */
    private void pilotControl(Player pilot) {
        float t = throttle();
        if (pilot.zza > 0.1F) {
            t = Math.min(1.0F, t + 0.02F);
        } else if (pilot.zza < -0.1F) {
            t = Math.max(0.0F, t - 0.035F);
        }
        entityData.set(THROTTLE, t);
        int buttons = clientButtons;
        if ((buttons & DestroyerEntity.BTN_BOOST) != 0 && boostCooldown <= 0 && boostTicks <= 0) {
            boostTicks = 40;
            boostCooldown = 160;
        }
        float wantYaw = pilot.getYRot();
        float wantPitch = Mth.clamp(pilot.getXRot(), -80.0F, 80.0F);
        float dy = Mth.clamp(Mth.wrapDegrees(wantYaw - getYRot()), -3.5F, 3.5F);
        float dp = Mth.clamp(wantPitch - getXRot(), -3.5F, 3.5F);
        setYRot(getYRot() + dy);
        setXRot(getXRot() + dp);
        double sp = 0.3D * Math.min(1.0F, t * 4.0F) + 1.3D * t + (boostTicks-- > 0 ? 0.6D : 0.0D);
        if (t <= 0.01F) {
            sp = 0.0D;
        }
        Vec3 dir = Vec3.directionFromRotation(getXRot(), getYRot());
        Vec3 v = getDeltaMovement().scale(0.75D).add(dir.scale(sp * 0.25D));
        if (pilot.xxa != 0.0F) {
            // A/D: deslizamiento lateral suave
            Vec3 side = new Vec3(dir.z, 0.0D, -dir.x).normalize().scale(pilot.xxa * 0.08D);
            v = v.add(side);
        }
        setDeltaMovement(v);
    }

    /** Piloto de la Legion: patrulla en ovalos y hace pasadas de ataque. */
    private void aiControl(ServerLevel sl) {
        Entity target = null;
        if (tickCount % 20 == 0 || attackRun > 0) {
            target = findTarget(sl);
        }
        Vec3 goal;
        double sp;
        if (target != null) {
            idleTicks = 0;
            Vec3 tp = target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D);
            double dist = tp.distanceTo(position());
            if (attackRun <= 0 && dist > 60.0D) {
                attackRun = 120;
            }
            if (attackRun > 0) {
                attackRun--;
                goal = tp;
                sp = dist < 14.0D ? 0.9D : 1.3D;
                if (dist < 12.0D) {
                    attackRun = 0; // rompe la pasada y se aleja
                }
            } else {
                Vec3 away = position().subtract(tp).normalize();
                goal = position().add(away.scale(40.0D)).add(0.0D, 10.0D, 0.0D);
                sp = 1.4D;
            }
            Vec3 dir = Vec3.directionFromRotation(getXRot(), getYRot());
            if (attackRun > 0 && dist < 70.0D && dir.dot(tp.subtract(position()).normalize()) > 0.97D && fireCooldown <= 0) {
                fireGuns(sl, null, dir);
            }
        } else {
            idleTicks++;
            BlockPos c = patrolCenter != null ? patrolCenter : blockPosition();
            double a = (tickCount + getId() * 37) * 0.012D;
            goal = new Vec3(c.getX() + Math.cos(a) * 70.0D, c.getY(), c.getZ() + Math.sin(a) * 45.0D);
            sp = 0.8D;
        }
        // nunca por debajo de 12 bloques sobre el terreno (salvo pasadas)
        double ground = sl.getHeight(Heightmap.Types.MOTION_BLOCKING, getBlockX(), getBlockZ());
        if (attackRun <= 0 && goal.y < ground + 12.0D) {
            goal = new Vec3(goal.x, ground + 12.0D, goal.z);
        }
        Vec3 d = goal.subtract(position());
        float wantYaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
        float wantPitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
        setYRot(getYRot() + Mth.clamp(Mth.wrapDegrees(wantYaw - getYRot()), -3.0F, 3.0F));
        setXRot(getXRot() + Mth.clamp(Mth.clamp(wantPitch, -60.0F, 60.0F) - getXRot(), -2.5F, 2.5F));
        Vec3 dir = Vec3.directionFromRotation(getXRot(), getYRot());
        setDeltaMovement(getDeltaMovement().scale(0.8D).add(dir.scale(sp * 0.2D)));
        entityData.set(THROTTLE, (float) Math.min(1.0D, sp / 1.4D));
        if (idleTicks > 6000 || sl.getNearestPlayer(this, 300.0D) == null && tickCount > 200) {
            discard();
        }
    }

    @Nullable
    private Entity findTarget(ServerLevel sl) {
        AABB box = getBoundingBox().inflate(90.0D, 60.0D, 90.0D);
        Entity best = null;
        double bd = Double.MAX_VALUE;
        for (Entity e : sl.getEntities(this, box, e -> e.isAlive() && (e instanceof LivingEntity || e instanceof FighterEntity))) {
            boolean hostile;
            if (e instanceof FighterEntity f) {
                hostile = f.getControllingPassenger() instanceof Player p && Factions.hostile(this, p);
            } else {
                hostile = Factions.hostile(this, e) && (e.getVehicle() == null || !(e.getVehicle() instanceof DestroyerEntity));
            }
            // a cielo abierto: no persiguen a quien esta bajo techo
            if (hostile && e.level().canSeeSky(e.blockPosition().above())) {
                double d = e.distanceToSqr(this);
                if (d < bd) {
                    bd = d;
                    best = e;
                }
            }
        }
        return best;
    }

    private void serverTick(ServerLevel sl) {
        // velocidad real (el piloto jugador mueve el caza desde su cliente)
        serverSpeed = position().distanceTo(lastServerPos);
        lastServerPos = position();
        if (getControllingPassenger() instanceof Player && (horizontalCollision || verticalCollision) && serverSpeed > 0.8D) {
            hurt(sl.damageSources().flyIntoWall(), (float) (serverSpeed * 12.0D));
        }
        if (tickCount % 4 == 0 && throttle() > 0.05F) {
            Vec3 back = Vec3.directionFromRotation(getXRot(), getYRot()).scale(-1.6D);
            Vfx.burst(sl, position().add(0.0D, 1.8D, 0.0D).add(back), BoltEntity.LEGION_GREEN, 1, 0.05F);
        }
        if (tickCount % 40 == 0 && throttle() > 0.3F) {
            sl.playSound(null, getX(), getY(), getZ(), ModSounds.FIGHTER_PASS.get(), SoundSource.NEUTRAL, 2.5F, 0.9F + random.nextFloat() * 0.2F);
        }
    }

    /** Disparo principal (lo pide el piloto con ShipInputC2S o la IA). */
    public void fireGuns(ServerLevel sl, @Nullable Player shooter, Vec3 dir) {
        if (fireCooldown > 0) {
            return;
        }
        fireCooldown = 6;
        Vec3 right = new Vec3(-dir.z, 0.0D, dir.x).normalize();
        Vec3 muzzle = position().add(0.0D, 1.5D, 0.0D).add(dir.scale(1.6D)).add(right.scale((gun++ % 2 == 0) ? 0.6D : -0.6D));
        Entity owner = shooter != null ? shooter : this;
        BoltEntity.fire(sl, owner, muzzle, dir, 4.5D, 5.0F, 0.6F, BoltEntity.LEGION_GREEN, 0.7F);
        Vfx.muzzle(sl, muzzle, dir, BoltEntity.LEGION_GREEN);
        sl.playSound(null, muzzle.x, muzzle.y, muzzle.z, ModSounds.BLASTER.get(), SoundSource.NEUTRAL, 1.4F, 1.2F);
    }

    /** Mandos que llegan del piloto (ShipInputC2S). */
    public void input(ServerPlayer player, int buttons) {
        if (getControllingPassenger() != player) {
            return;
        }
        if ((buttons & DestroyerEntity.BTN_FIRE) != 0) {
            fireGuns((ServerLevel) level(), player, Vec3.directionFromRotation(getXRot(), getYRot()));
        }
    }

    // ------------------------------------------------------------------ dano

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source) || level().isClientSide || isRemoved()) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker != null && hasPassenger(attacker)) {
            return false;
        }
        if (attacker instanceof Player p && p.isCreative() && !isVehicle() && !hasNpcPilot()) {
            discard();
            return true;
        }
        float hp = health() - amount;
        entityData.set(HEALTH, Math.max(0.0F, hp));
        if (hp <= 0.0F) {
            explode((ServerLevel) level());
        }
        return true;
    }

    private void explode(ServerLevel sl) {
        Vfx.explosion(sl, position().add(0.0D, 1.6D, 0.0D), 2.2F);
        sl.playSound(null, getX(), getY(), getZ(), ModSounds.EXPLOSION_BIG.get(), SoundSource.NEUTRAL, 3.0F, 1.2F);
        spawnAtLocation(new ItemStack(Items.IRON_NUGGET, 3));
        Campaign.get(sl.getServer()).inc("fighters_destroyed", 1);
        ejectPassengers();
        discard();
    }

    // ------------------------------------------------------------------ sincronizacion

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYRot = yRot;
        lerpXRot = xRot;
        lerpSteps = 6;
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

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 256.0D * 256.0D;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(2.0D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(HEALTH, tag.contains("Health") ? tag.getFloat("Health") : MAX_HEALTH);
        entityData.set(NPC, tag.getBoolean("Npc"));
        entityData.set(LEGION, !tag.contains("Legion") || tag.getBoolean("Legion"));
        if (tag.contains("PatrolX")) {
            patrolCenter = new BlockPos(tag.getInt("PatrolX"), tag.getInt("PatrolY"), tag.getInt("PatrolZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Health", health());
        tag.putBoolean("Npc", hasNpcPilot());
        tag.putBoolean("Legion", isLegionCraft());
        if (patrolCenter != null) {
            tag.putInt("PatrolX", patrolCenter.getX());
            tag.putInt("PatrolY", patrolCenter.getY());
            tag.putInt("PatrolZ", patrolCenter.getZ());
        }
    }
}
