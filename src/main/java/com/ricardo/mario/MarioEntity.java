package com.ricardo.mario;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;

/** Mario: amistoso, lanza bolas de fuego a distancia y, de cerca, hace el Super Jump Punch con monedas. */
public class MarioEntity extends PathfinderMob implements RangedAttackMob {

    /** Tick del Super Jump Punch en curso (-1 = no esta saltando). Se sincroniza para animarlo en el cliente. */
    private static final EntityDataAccessor<Integer> PUNCH_TICK =
            SynchedEntityData.defineId(MarioEntity.class, EntityDataSerializers.INT);
    /** Tick en que aterrizo el Super Jump Punch (-1 = todavia en el aire). */
    private static final EntityDataAccessor<Integer> LANDED_TICK =
            SynchedEntityData.defineId(MarioEntity.class, EntityDataSerializers.INT);

    public MarioEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PUNCH_TICK, -1);
        builder.define(LANDED_TICK, -1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new SuperJumpPunchGoal(this));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0D, 30, 14.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    public boolean isSuperJumpPunching() {
        return this.getPunchTick() >= 0;
    }

    public int getPunchTick() {
        return this.entityData.get(PUNCH_TICK);
    }

    public void setPunchTick(int tick) {
        this.entityData.set(PUNCH_TICK, tick);
    }

    public int getLandedTick() {
        return this.entityData.get(LANDED_TICK);
    }

    public void setLandedTick(int tick) {
        this.entityData.set(LANDED_TICK, tick);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Caer despues de un Super Jump Punch no le hace dano
        if (this.isSuperJumpPunching()) {
            return false;
        }
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.onGround() && this.getTarget() == null && this.random.nextInt(60) == 0) {
            this.jumpFromGround();
            this.playSound(SoundEvents.SLIME_JUMP, 0.5F, 1.4F);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5D) - this.getEyeY();
        double dz = target.getZ() - this.getZ();
        SmallFireball fireball = new SmallFireball(EntityType.SMALL_FIREBALL, this.level());
        fireball.setOwner(this);
        fireball.setPos(this.getX(), this.getEyeY() - 0.1D, this.getZ());
        fireball.shoot(dx, dy, dz, 1.1F, 0.0F);
        this.level().addFreshEntity(fireball);
        this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }
}
