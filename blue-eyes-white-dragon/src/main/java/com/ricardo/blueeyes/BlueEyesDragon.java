package com.ricardo.blueeyes;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Dragon Blanco de Ojos Azules. Vuela sobre su carta (o junto a su duenio) y destruye a sus enemigos con White Lightning.
 */
public class BlueEyesDragon extends FlyingMob {

    private static final EntityDataAccessor<Integer> DATA_CHARGE = SynchedEntityData.defineId(BlueEyesDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FIRE = SynchedEntityData.defineId(BlueEyesDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BEAM_TARGET = SynchedEntityData.defineId(BlueEyesDragon.class, EntityDataSerializers.INT);

    public static final int CHARGE_TIME = 24;
    public static final int FIRE_TIME = 14;
    public static final float MODEL_SCALE = 0.8F;
    /** Boca respecto a los pies (bloques), medido sobre el modelo generado * MODEL_SCALE. */
    private static final double MOUTH_HEIGHT = 4.85D;
    private static final double MOUTH_FORWARD = 1.75D;

    private static final float BEAM_DAMAGE = 40.0F;
    private static final float SPLASH_DAMAGE = 20.0F;
    private static final float PIERCE_DAMAGE = 15.0F;

    @Nullable private UUID ownerUUID;
    @Nullable private UUID cardUUID;
    private int ownerMissing;
    private int cardMissing;

    public BlueEyesDragon(EntityType<? extends BlueEyesDragon> type, Level level) {
        super(type, level);
        this.moveControl = new DragonMoveControl(this);
        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                // la orientacion la controla el propio dragon (ver updateOrientation)
            }
        };
        this.xpReward = 0;
        this.noCulling = true;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.ATTACK_DAMAGE, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHARGE, 0);
        builder.define(DATA_FIRE, 0);
        builder.define(DATA_BEAM_TARGET, -1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new WhiteLightningGoal(this));
        this.goalSelector.addGoal(2, new GuardGoal(this));
        this.targetSelector.addGoal(1, new DefendOwnerTargetGoal(this));
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this) {
            @Override
            public void clientTick() {
                BlueEyesDragon.this.yBodyRot = BlueEyesDragon.this.getYRot();
                BlueEyesDragon.this.yHeadRot = BlueEyesDragon.this.getYRot();
            }
        };
    }

    // ------------------------------------------------------------------ duenio / carta

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.ownerUUID = uuid;
    }

    public void setCardUUID(@Nullable UUID uuid) {
        this.cardUUID = uuid;
    }

    @Nullable
    public Player getOwner() {
        if (this.ownerUUID == null) {
            return null;
        }
        Player p = this.level().getPlayerByUUID(this.ownerUUID);
        return p != null && p.isAlive() && !p.isSpectator() ? p : null;
    }

    public boolean isOwner(@Nullable Entity e) {
        return e != null && this.ownerUUID != null && this.ownerUUID.equals(e.getUUID());
    }

    @Nullable
    public FieldCardEntity getCard() {
        if (this.cardUUID == null || !(this.level() instanceof ServerLevel sl)) {
            return null;
        }
        Entity e = sl.getEntity(this.cardUUID);
        return e instanceof FieldCardEntity c && c.isAlive() ? c : null;
    }

    /** Retirada (al recoger la carta, o si el duenio desaparece). */
    public void dismiss() {
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.5D, getZ(), 120, 1.4D, 2.2D, 1.4D, 0.08D);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 2.5D, getZ(), 60, 1.4D, 2.2D, 1.4D, 0.2D);
            sl.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_AMBIENT, SoundSource.NEUTRAL, 2.0F, 1.2F);
        }
        this.discard();
    }

    // ------------------------------------------------------------------ estado sincronizado (animaciones / rayo)

    public int getCharge() {
        return this.entityData.get(DATA_CHARGE);
    }

    public void setCharge(int ticks) {
        this.entityData.set(DATA_CHARGE, ticks);
    }

    public int getFireTicks() {
        return this.entityData.get(DATA_FIRE);
    }

    @Nullable
    public Entity getBeamTarget() {
        int id = this.entityData.get(DATA_BEAM_TARGET);
        return id < 0 ? null : this.level().getEntity(id);
    }

    public Vec3 getMouthPosition(float partialTick) {
        double x = Mth.lerp(partialTick, this.xo, this.getX());
        double y = Mth.lerp(partialTick, this.yo, this.getY());
        double z = Mth.lerp(partialTick, this.zo, this.getZ());
        float yaw = Mth.rotLerp(partialTick, this.yRotO, this.getYRot());
        float pitch = Mth.lerp(partialTick, this.xRotO, this.getXRot());
        Vec3 flat = Vec3.directionFromRotation(0.0F, yaw);
        Vec3 look = Vec3.directionFromRotation(pitch, yaw);
        return new Vec3(x, y + MOUTH_HEIGHT, z).add(flat.scale(MOUTH_FORWARD)).add(look.scale(0.4D));
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (getCharge() > 0) {
                Vec3 m = getMouthPosition(1.0F);
                for (int i = 0; i < 2; i++) {
                    this.level().addParticle(ParticleTypes.END_ROD, m.x + (this.random.nextDouble() - 0.5D) * 0.6D,
                            m.y + (this.random.nextDouble() - 0.5D) * 0.6D, m.z + (this.random.nextDouble() - 0.5D) * 0.6D, 0.0D, 0.0D, 0.0D);
                }
            }
            return;
        }
        int fire = getFireTicks();
        if (fire > 0) {
            this.entityData.set(DATA_FIRE, fire - 1);
            if (fire - 1 == 0) {
                this.entityData.set(DATA_BEAM_TARGET, -1);
            }
        }
        updateOrientation();

        if (this.tickCount % 20 == 0) {
            // sin duenio (desconectado, muerto o en otra dimension) -> se retira
            if (this.ownerUUID != null && getOwner() == null) {
                if (++this.ownerMissing >= 10) {
                    dismiss();
                    return;
                }
            } else {
                this.ownerMissing = 0;
            }
            // si su carta ya no esta en el campo, el dragon tampoco
            if (this.cardUUID != null && getCard() == null) {
                if (++this.cardMissing >= 10) {
                    dismiss();
                    return;
                }
            } else {
                this.cardMissing = 0;
            }
            if (this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(2.0F);
            }
        }
        if (this.tickCount % 26 == 0) {
            this.level().playSound(null, getX(), getY() + 3.0D, getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.NEUTRAL, 0.9F,
                    0.9F + this.random.nextFloat() * 0.2F);
        }
    }

    /** Gira el cuerpo/cabeza hacia el objetivo, el duenio o la direccion de vuelo. */
    private void updateOrientation() {
        LivingEntity target = this.getTarget();
        Vec3 look = null;
        if (target != null && target.isAlive()) {
            look = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D).subtract(getMouthPosition(1.0F));
        } else {
            Vec3 v = this.getDeltaMovement();
            if (v.horizontalDistanceSqr() > 0.0064D) {
                look = new Vec3(v.x, 0.0D, v.z);
            } else {
                Player owner = getOwner();
                if (owner != null && owner.distanceToSqr(this) < 30.0D * 30.0D) {
                    look = owner.getEyePosition().subtract(getMouthPosition(1.0F));
                }
            }
        }
        float yaw = this.getYRot();
        float pitch = 0.0F;
        if (look != null && look.lengthSqr() > 1.0E-4D) {
            yaw = (float) (Mth.atan2(look.z, look.x) * (180.0D / Math.PI)) - 90.0F;
            pitch = (float) (-(Mth.atan2(look.y, look.horizontalDistance()) * (180.0D / Math.PI)));
        }
        float newYaw = Mth.approachDegrees(this.getYRot(), yaw, 9.0F);
        this.setYRot(newYaw);
        this.setXRot(Mth.approach(this.getXRot(), Mth.clamp(pitch, -40.0F, 45.0F), 6.0F));
        this.yBodyRot = newYaw;
        this.yHeadRot = newYaw;
    }

    // ------------------------------------------------------------------ White Lightning

    boolean canTarget(@Nullable LivingEntity e) {
        if (e == null || !e.isAlive() || e == this || e.isSpectator() || e.isInvulnerable()) {
            return false;
        }
        if (e instanceof Player || e instanceof BlueEyesDragon || e instanceof ArmorStand || e instanceof AbstractVillager) {
            return false;
        }
        if (isOwner(e)) {
            return false;
        }
        Player owner = getOwner();
        return !(owner != null && e instanceof TamableAnimal pet && pet.isOwnedBy(owner));
    }

    private DamageSource lightningDamage() {
        Holder<DamageType> type = this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.LIGHTNING_BOLT);
        Player owner = getOwner();
        return new DamageSource(type, this, owner != null ? owner : this);
    }

    void fireWhiteLightning(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) {
            return;
        }
        Vec3 from = getMouthPosition(1.0F);
        Vec3 to = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        this.entityData.set(DATA_BEAM_TARGET, target.getId());
        this.entityData.set(DATA_FIRE, FIRE_TIME);

        // rastro del rayo (particulas que completan el haz que dibuja el cliente)
        Vec3 d = to.subtract(from);
        double len = d.length();
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(0.78F, 0.93F, 1.0F), 2.5F);
        for (double s = 0.0D; s < len; s += 0.9D) {
            Vec3 p = from.add(d.scale(s / len));
            sl.sendParticles(dust, p.x, p.y, p.z, 1, 0.12D, 0.12D, 0.12D, 0.0D);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.3D, 0.3D, 0.3D, 0.05D);
        }

        // rayo visual sobre el objetivo + impacto
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
        if (bolt != null) {
            bolt.moveTo(target.getX(), target.getY(), target.getZ());
            bolt.setVisualOnly(true);
            sl.addFreshEntity(bolt);
        }
        sl.sendParticles(ParticleTypes.FLASH, to.x, to.y, to.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        sl.sendParticles(ParticleTypes.END_ROD, to.x, to.y, to.z, 70, 0.8D, 0.8D, 0.8D, 0.25D);
        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, to.x, to.y, to.z, 60, 1.0D, 1.0D, 1.0D, 0.5D);
        sl.playSound(null, from.x, from.y, from.z, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL, 2.5F, 1.3F);
        sl.playSound(null, to.x, to.y, to.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL, 4.0F, 1.4F);
        sl.playSound(null, to.x, to.y, to.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.NEUTRAL, 2.0F, 1.0F);

        DamageSource src = lightningDamage();
        target.invulnerableTime = 0;
        target.hurt(src, BEAM_DAMAGE);

        // onda expansiva en el impacto + todo lo que atraviese el haz
        AABB area = new AABB(from, to).inflate(3.5D);
        List<LivingEntity> hit = sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != target && canTarget(e));
        for (LivingEntity e : hit) {
            float dmg = 0.0F;
            if (e.distanceToSqr(to) <= 3.5D * 3.5D) {
                dmg = SPLASH_DAMAGE;
            } else if (distanceToSegment(e.position().add(0.0D, e.getBbHeight() * 0.5D, 0.0D), from, to) < 1.6D) {
                dmg = PIERCE_DAMAGE;
            }
            if (dmg > 0.0F) {
                e.invulnerableTime = 0;
                e.hurt(src, dmg);
                e.setDeltaMovement(e.getDeltaMovement().add(0.0D, 0.35D, 0.0D));
            }
        }
    }

    private static double distanceToSegment(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double l2 = ab.lengthSqr();
        double t = l2 < 1.0E-6D ? 0.0D : Mth.clamp(p.subtract(a).dot(ab) / l2, 0.0D, 1.0D);
        return p.distanceTo(a.add(ab.scale(t)));
    }

    // ------------------------------------------------------------------ interaccion / danio

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isOwner(player) && player.isShiftKeyDown()) {
            if (!this.level().isClientSide) {
                FieldCardEntity card = getCard();
                if (card != null) {
                    card.retrieve(player);
                } else {
                    dismiss();
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isOwner(source.getEntity())) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return super.isInvulnerableTo(source)
                || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.FALL)
                || source.is(DamageTypes.LIGHTNING_BOLT)
                || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.CRAMMING);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.5D, getZ(), 150, 1.5D, 2.5D, 1.5D, 0.12D);
        }
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
    protected float getSoundVolume() {
        return 1.6F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    // ------------------------------------------------------------------ guardado

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
        if (this.cardUUID != null) {
            tag.putUUID("Card", this.cardUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        this.cardUUID = tag.hasUUID("Card") ? tag.getUUID("Card") : null;
    }

    // ================================================================== IA

    /** Vuelo directo hacia el punto deseado (como un ghast, pero mas rapido y suave). */
    static class DragonMoveControl extends MoveControl {
        DragonMoveControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.85D));
                return;
            }
            Vec3 delta = new Vec3(this.wantedX - this.mob.getX(), this.wantedY - this.mob.getY(), this.wantedZ - this.mob.getZ());
            double dist = delta.length();
            if (dist < 0.6D) {
                this.operation = MoveControl.Operation.WAIT;
                return;
            }
            double speed = Math.min(0.55D * this.speedModifier, dist * 0.12D);
            Vec3 desired = delta.scale(speed / dist);
            Vec3 v = this.mob.getDeltaMovement().scale(0.82D).add(desired.scale(0.18D / 0.91D * 1.2D));
            if (this.mob.horizontalCollision) {
                v = v.add(0.0D, 0.12D, 0.0D);
            }
            this.mob.setDeltaMovement(v);
        }
    }

    /** Sin combate: flota sobre su carta si el duenio esta cerca; si no, acompania al duenio. */
    static class GuardGoal extends Goal {
        private final BlueEyesDragon dragon;
        private Vec3 lastPos = Vec3.ZERO;
        private int stuck;

        GuardGoal(BlueEyesDragon dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.dragon.getTarget() == null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Player owner = this.dragon.getOwner();
            FieldCardEntity card = this.dragon.tickCount % 10 == 0 || this.cached == null ? (this.cached = this.dragon.getCard()) : this.cached;
            float t = this.dragon.tickCount;
            Vec3 anchor;
            double speed = 1.0D;
            if (card != null && (owner == null || owner.distanceToSqr(card) < 26.0D * 26.0D)) {
                // sobre la carta, balanceandose suavemente
                anchor = card.position().add(Math.sin(t * 0.02D) * 0.6D, 1.6D + Math.sin(t * 0.05D) * 0.35D, Math.cos(t * 0.02D) * 0.6D);
            } else if (owner != null) {
                Vec3 back = Vec3.directionFromRotation(0.0F, owner.getYRot()).scale(-4.0D);
                anchor = owner.position().add(back.x, 3.5D + Math.sin(t * 0.05D) * 0.4D, back.z);
                double d = this.dragon.distanceTo(owner);
                speed = d > 16.0D ? 2.0D : 1.2D;
                if (d > 48.0D) {
                    teleport(anchor);
                    return;
                }
                if (this.dragon.tickCount % 20 == 0) {
                    if (d > 14.0D && this.dragon.position().distanceTo(this.lastPos) < 1.0D) {
                        if (++this.stuck >= 5) {
                            teleport(anchor);
                        }
                    } else {
                        this.stuck = 0;
                    }
                    this.lastPos = this.dragon.position();
                }
            } else {
                return;
            }
            this.dragon.getMoveControl().setWantedPosition(anchor.x, anchor.y, anchor.z, speed);
        }

        @Nullable private FieldCardEntity cached;

        private void teleport(Vec3 to) {
            if (this.dragon.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, this.dragon.getX(), this.dragon.getY() + 2.0D, this.dragon.getZ(), 40, 1.0D, 2.0D, 1.0D, 0.05D);
            }
            this.dragon.moveTo(to.x, to.y, to.z, this.dragon.getYRot(), 0.0F);
            this.dragon.setDeltaMovement(Vec3.ZERO);
            this.stuck = 0;
        }
    }

    /** Combate: se coloca a distancia, carga la energia en las fauces y dispara White Lightning. */
    static class WhiteLightningGoal extends Goal {
        private final BlueEyesDragon dragon;
        private int cooldown = 10;
        private int charge;
        private int noSight;

        WhiteLightningGoal(BlueEyesDragon dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = this.dragon.getTarget();
            return t != null && t.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            this.charge = 0;
            this.dragon.setCharge(0);
        }

        @Override
        public void tick() {
            LivingEntity target = this.dragon.getTarget();
            if (target == null) {
                return;
            }
            ServerLevel sl = (ServerLevel) this.dragon.level();
            double dist = this.dragon.distanceTo(target);
            boolean sight = this.dragon.hasLineOfSight(target);
            this.noSight = sight ? 0 : this.noSight + 1;

            // posicion de tiro: a ~13 bloques, por encima del objetivo, rodeandolo despacio
            Vec3 flat = new Vec3(this.dragon.getX() - target.getX(), 0.0D, this.dragon.getZ() - target.getZ());
            if (flat.lengthSqr() < 1.0E-4D) {
                flat = new Vec3(1.0D, 0.0D, 0.0D);
            }
            flat = flat.normalize().yRot((float) Math.sin(this.dragon.tickCount * 0.015D) * 0.02F);
            double height = this.noSight > 30 ? 10.0D : 5.0D;
            Vec3 wanted = target.position().add(flat.scale(this.noSight > 30 ? 6.0D : 13.0D)).add(0.0D, height, 0.0D);

            if (this.charge > 0) {
                // cargando: se queda quieto
                this.charge++;
                this.dragon.setCharge(this.charge);
                this.dragon.getMoveControl().setWantedPosition(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), 0.5D);
                Vec3 m = this.dragon.getMouthPosition(1.0F);
                sl.sendParticles(ParticleTypes.END_ROD, m.x, m.y, m.z, 3, 0.25D, 0.25D, 0.25D, 0.01D);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, m.x, m.y, m.z, 4, 0.9D, 0.9D, 0.9D, 0.0D);
                if (this.charge == CHARGE_TIME / 2) {
                    sl.playSound(null, m.x, m.y, m.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL, 0.6F, 2.0F);
                }
                if (this.charge >= CHARGE_TIME) {
                    this.dragon.fireWhiteLightning(target);
                    this.charge = 0;
                    this.dragon.setCharge(0);
                    this.cooldown = 45 + this.dragon.getRandom().nextInt(20);
                }
                return;
            }

            if (this.dragon.position().distanceTo(wanted) > 2.5D) {
                this.dragon.getMoveControl().setWantedPosition(wanted.x, wanted.y, wanted.z, dist > 24.0D ? 2.0D : 1.4D);
            }
            if (this.cooldown > 0) {
                this.cooldown--;
            } else if (sight && dist < 34.0D) {
                this.charge = 1;
                this.dragon.setCharge(1);
                sl.playSound(null, this.dragon.getX(), this.dragon.getY() + 4.0D, this.dragon.getZ(), SoundEvents.BEACON_ACTIVATE,
                        SoundSource.NEUTRAL, 2.0F, 1.8F);
            }
        }
    }

    /** Elige a quien atacar: quien ataca al duenio, a quien ataca el duenio, quien lo persigue, o el hostil mas cercano. */
    static class DefendOwnerTargetGoal extends Goal {
        private final BlueEyesDragon dragon;
        @Nullable private LivingEntity pending;
        private int recheck;

        DefendOwnerTargetGoal(BlueEyesDragon dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            this.pending = find();
            return this.pending != null;
        }

        @Override
        public void start() {
            this.dragon.setTarget(this.pending);
            this.recheck = 20;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity t = this.dragon.getTarget();
            if (t == null || !this.dragon.canTarget(t) || this.dragon.distanceToSqr(t) > 56.0D * 56.0D) {
                return false;
            }
            if (--this.recheck <= 0) {
                this.recheck = 20;
                LivingEntity better = priority();
                if (better != null && better != t) {
                    this.dragon.setTarget(better);
                }
            }
            return true;
        }

        @Override
        public void stop() {
            this.dragon.setTarget(null);
        }

        /** Enemigos relacionados con el duenio. */
        @Nullable
        private LivingEntity priority() {
            Player owner = this.dragon.getOwner();
            if (owner == null) {
                return null;
            }
            LivingEntity a = owner.getLastHurtByMob();
            if (a != null && owner.tickCount - owner.getLastHurtByMobTimestamp() < 200 && this.dragon.canTarget(a)
                    && a.distanceToSqr(owner) < 48.0D * 48.0D) {
                return a;
            }
            LivingEntity b = owner.getLastHurtMob();
            if (b != null && owner.tickCount - owner.getLastHurtMobTimestamp() < 200 && this.dragon.canTarget(b)
                    && b.distanceToSqr(owner) < 48.0D * 48.0D) {
                return b;
            }
            List<Mob> hunters = this.dragon.level().getEntitiesOfClass(Mob.class, owner.getBoundingBox().inflate(24.0D),
                    m -> m.getTarget() == owner && this.dragon.canTarget(m));
            return closest(hunters);
        }

        @Nullable
        private LivingEntity find() {
            LivingEntity p = priority();
            if (p != null) {
                return p;
            }
            Player owner = this.dragon.getOwner();
            Entity center = owner != null ? owner : this.dragon;
            List<LivingEntity> hostiles = this.dragon.level().getEntitiesOfClass(LivingEntity.class, center.getBoundingBox().inflate(26.0D),
                    e -> e instanceof Enemy && !(e instanceof NeutralMob) && this.dragon.canTarget(e));
            return closest(hostiles);
        }

        @Nullable
        private LivingEntity closest(List<? extends LivingEntity> list) {
            LivingEntity best = null;
            double bd = Double.MAX_VALUE;
            for (LivingEntity e : list) {
                double d = e.distanceToSqr(this.dragon);
                if (d < bd) {
                    bd = d;
                    best = e;
                }
            }
            return best;
        }
    }
}
