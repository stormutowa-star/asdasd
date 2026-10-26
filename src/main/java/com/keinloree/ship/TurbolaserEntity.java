package com.keinloree.ship;

import com.keinloree.registry.ModEntities;
import com.keinloree.registry.ModSounds;
import com.keinloree.story.Factions;
import com.keinloree.vfx.Vfx;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Torreta turbolaser del destructor (o de una base). Pesada: dos canones largos, lenta y devastadora. Defensa: cuatro
 * canones cortos y rapidos, prioriza objetivos voladores. Apunta con velocidad limitada igual en servidor y cliente.
 */
public class TurbolaserEntity extends Entity {
    public static final int HEAVY = 0;
    public static final int DEFENSE = 1;

    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(TurbolaserEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> INVERTED = SynchedEntityData.defineId(TurbolaserEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(TurbolaserEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DESTROYED = SynchedEntityData.defineId(TurbolaserEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> HEALTH = SynchedEntityData.defineId(TurbolaserEntity.class, EntityDataSerializers.FLOAT);
    /** Punto forzado de disparo (bombardeo de la historia o mira del piloto): NaN = ninguno. */
    @Nullable
    private Vec3 forcedAim;
    private int forcedTicks;

    // Punteria actual (grados, mundo). Se calcula en ambos lados.
    public float aimYaw;
    public float aimPitch;
    public float aimYawO;
    public float aimPitchO;
    private int cooldown;
    private int barrel;
    /** Retroceso por canon (cliente), 1 = recien disparado. */
    public final float[] recoil = new float[4];

    public TurbolaserEntity(EntityType<? extends TurbolaserEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.cooldown = 20 + random.nextInt(40);
    }

    public static TurbolaserEntity create(ServerLevel level, Vec3 at, int type, boolean inverted, float yaw) {
        TurbolaserEntity t = new TurbolaserEntity(ModEntities.TURBOLASER.get(), level);
        t.entityData.set(TYPE, type);
        t.entityData.set(INVERTED, inverted);
        t.entityData.set(HEALTH, t.maxHealth());
        t.moveTo(at.x, at.y, at.z, yaw, 0.0F);
        t.aimYaw = t.aimYawO = yaw;
        level.addFreshEntity(t);
        return t;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(TYPE, HEAVY);
        b.define(INVERTED, false);
        b.define(TARGET, -1);
        b.define(DESTROYED, false);
        b.define(HEALTH, 80.0F);
    }

    public int turretType() {
        return entityData.get(TYPE);
    }

    public boolean isHeavy() {
        return turretType() == HEAVY;
    }

    public boolean isInverted() {
        return entityData.get(INVERTED);
    }

    public boolean isDestroyed() {
        return entityData.get(DESTROYED);
    }

    public float health() {
        return entityData.get(HEALTH);
    }

    public float maxHealth() {
        return isHeavy() ? 80.0F : 40.0F;
    }

    private double range() {
        return isHeavy() ? 110.0D : 80.0D;
    }

    /** Altura (bloques) del eje de los canones sobre la base. */
    private double pivotHeight() {
        return isHeavy() ? 1.45D : 0.8D;
    }

    public Vec3 pivot() {
        return position().add(0.0D, isInverted() ? -pivotHeight() : pivotHeight(), 0.0D);
    }

    /** Ordena disparar a un punto durante unos ticks (bombardeo / mira del piloto). */
    public void forceAim(Vec3 at, int ticks) {
        forcedAim = at;
        forcedTicks = ticks;
    }

    @Override
    public void tick() {
        super.tick();
        aimYawO = aimYaw;
        aimPitchO = aimPitch;
        for (int i = 0; i < recoil.length; i++) {
            recoil[i] = Math.max(0.0F, recoil[i] - 0.12F);
        }
        if (isDestroyed()) {
            if (level() instanceof ServerLevel sl && tickCount % 10 == 0) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE, getX(), getY() + (isInverted() ? -1 : 1.5), getZ(),
                        2, 0.4, 0.3, 0.4, 0.01);
            }
            return;
        }
        Vec3 aim = currentAimPoint();
        if (aim != null) {
            Vec3 d = aim.subtract(pivot());
            float wantYaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
            float wantPitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
            wantPitch = isInverted() ? Mth.clamp(wantPitch, 0.0F, 80.0F) : Mth.clamp(wantPitch, -80.0F, 8.0F);
            float yawStep = isHeavy() ? 4.0F : 7.0F;
            float pitchStep = isHeavy() ? 3.0F : 6.0F;
            aimYaw = aimYaw + Mth.clamp(Mth.wrapDegrees(wantYaw - aimYaw), -yawStep, yawStep);
            aimPitch = aimPitch + Mth.clamp(wantPitch - aimPitch, -pitchStep, pitchStep);
        }
        if (!level().isClientSide) {
            serverTick((ServerLevel) level(), aim);
        }
    }

    @Nullable
    private Vec3 currentAimPoint() {
        if (forcedAim != null && forcedTicks > 0) {
            return forcedAim;
        }
        int id = entityData.get(TARGET);
        if (id < 0) {
            return null;
        }
        Entity t = level().getEntity(id);
        return t == null ? null : t.position().add(0.0D, t.getBbHeight() * 0.5D, 0.0D);
    }

    private void serverTick(ServerLevel sl, @Nullable Vec3 aim) {
        if (forcedTicks > 0 && --forcedTicks == 0) {
            forcedAim = null;
        }
        if (getVehicle() instanceof DestroyerEntity ship && ship.getState() != DestroyerEntity.FLYING
                && ship.getState() != DestroyerEntity.ANCHORED && forcedAim == null) {
            return; // durante las transiciones no dispara
        }
        if (tickCount % 10 == 0 && forcedAim == null) {
            Entity t = findTarget(sl);
            entityData.set(TARGET, t == null ? -1 : t.getId());
        }
        if (aim == null || --cooldown > 0) {
            return;
        }
        Vec3 piv = pivot();
        Vec3 dir = Vec3.directionFromRotation(aimPitch, aimYaw);
        Vec3 want = aim.subtract(piv).normalize();
        if (dir.dot(want) < Math.cos(Math.toRadians(4.0D))) {
            return;
        }
        if (forcedAim == null && !clearShot(sl, piv.add(dir.scale(2.5D)), aim)) {
            cooldown = 10;
            return;
        }
        fire(sl, dir);
        cooldown = isHeavy() ? 50 + random.nextInt(15) : 12 + random.nextInt(6);
    }

    private boolean clearShot(ServerLevel sl, Vec3 from, Vec3 to) {
        HitResult h = sl.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return h.getType() == HitResult.Type.MISS || h.getLocation().distanceToSqr(to) < 4.0D;
    }

    private void fire(ServerLevel sl, Vec3 dir) {
        int barrels = isHeavy() ? 2 : 4;
        int b = barrel++ % barrels;
        Vec3 right = new Vec3(-dir.z, 0.0D, dir.x).normalize();
        double side = isHeavy() ? (b == 0 ? 0.55D : -0.55D) : ((b % 2 == 0) ? 0.3D : -0.3D);
        double up = isHeavy() ? 0.0D : (b < 2 ? 0.25D : -0.25D);
        Vec3 muzzle = pivot().add(dir.scale(isHeavy() ? 2.6D : 1.6D)).add(right.scale(side)).add(0.0D, up, 0.0D);
        float damage = isHeavy() ? 10.0F : 4.0F;
        float radius = isHeavy() ? 2.5F : 0.8F;
        BoltEntity.fire(sl, this, muzzle, dir, isHeavy() ? 3.5D : 4.0D, damage, radius, BoltEntity.LEGION_GREEN, isHeavy() ? 1.6F : 0.8F);
        Vfx.muzzle(sl, muzzle, dir, BoltEntity.LEGION_GREEN);
        sl.playSound(null, muzzle.x, muzzle.y, muzzle.z, isHeavy() ? ModSounds.TURBOLASER.get() : ModSounds.BLASTER.get(),
                SoundSource.HOSTILE, isHeavy() ? 3.0F : 1.2F, 0.9F + random.nextFloat() * 0.2F);
        sl.broadcastEntityEvent(this, (byte) (100 + b));
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id >= 100 && id < 104) {
            recoil[id - 100] = 1.0F;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Nullable
    private Entity findTarget(ServerLevel sl) {
        double r = range();
        AABB box = new AABB(position(), position()).inflate(r, r * 0.8D, r);
        Entity best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity e : sl.getEntities(this, box, this::isValidTarget)) {
            double d = e.distanceToSqr(this);
            if (d > r * r) {
                continue;
            }
            boolean flying = !e.onGround() && (e instanceof FighterEntity || e.getVehicle() instanceof FighterEntity
                    || e instanceof Player p && p.isFallFlying());
            double score = d * (!isHeavy() && flying ? 0.25D : 1.0D);
            Vec3 at = e.position().add(0.0D, e.getBbHeight() * 0.5D, 0.0D);
            if (score < bestScore && clearShot(sl, pivot().add(at.subtract(pivot()).normalize().scale(2.5D)), at)) {
                best = e;
                bestScore = score;
            }
        }
        return best;
    }

    private boolean isValidTarget(Entity e) {
        if (!e.isAlive() || e instanceof BoltEntity || e instanceof TurbolaserEntity) {
            return false;
        }
        // la de la panza solo cubre lo que hay debajo (el casco tapa el resto; clearShot lo confirma)
        if (isInverted() && e.getY() > getY()) {
            return false;
        }
        if (e instanceof FighterEntity f) {
            Entity pilot = f.getControllingPassenger();
            return pilot instanceof Player p && Factions.hostile(this, p);
        }
        if (e instanceof IronGolem) {
            return true;
        }
        return e instanceof LivingEntity && Factions.hostile(this, e);
    }

    // ------------------------------------------------------------------ dano

    @Override
    public boolean isPickable() {
        return !isDestroyed();
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source) || isDestroyed() || level().isClientSide) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker != null && Factions.isLegion(attacker) || attacker instanceof TurbolaserEntity) {
            return false;
        }
        float hp = health() - amount;
        entityData.set(HEALTH, Math.max(0.0F, hp));
        if (hp <= 0.0F) {
            entityData.set(DESTROYED, true);
            entityData.set(TARGET, -1);
            ServerLevel sl = (ServerLevel) level();
            Vfx.explosion(sl, pivot(), isHeavy() ? 3.0F : 2.0F);
            sl.playSound(null, getX(), getY(), getZ(), ModSounds.EXPLOSION_BIG.get(), SoundSource.HOSTILE, 4.0F, 1.0F);
            com.keinloree.story.Campaign c = com.keinloree.story.Campaign.get(sl.getServer());
            c.inc(isHeavy() ? "turrets_heavy_destroyed" : "turrets_defense_destroyed", 1);
        }
        return true;
    }

    /** Reparada (la historia la usa al reiniciar o tras ciertos capitulos). */
    public void repair() {
        entityData.set(DESTROYED, false);
        entityData.set(HEALTH, maxHealth());
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !isDestroyed();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 384.0D * 384.0D;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(3.0D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(TYPE, tag.getInt("Kind"));
        entityData.set(INVERTED, tag.getBoolean("Inverted"));
        entityData.set(DESTROYED, tag.getBoolean("Destroyed"));
        entityData.set(HEALTH, tag.contains("Health") ? tag.getFloat("Health") : maxHealth());
        aimYaw = aimYawO = tag.getFloat("AimYaw");
        aimPitch = aimPitchO = tag.getFloat("AimPitch");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Kind", turretType());
        tag.putBoolean("Inverted", isInverted());
        tag.putBoolean("Destroyed", isDestroyed());
        tag.putFloat("Health", health());
        tag.putFloat("AimYaw", aimYaw);
        tag.putFloat("AimPitch", aimPitch);
    }
}
