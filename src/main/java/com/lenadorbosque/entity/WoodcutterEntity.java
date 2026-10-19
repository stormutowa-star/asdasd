package com.lenadorbosque.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Tomás Ibarra, el leñador. Es invulnerable: su destino lo decide la historia, no el jugador.
 */
public class WoodcutterEntity extends PathfinderMob implements GeoEntity {

    public static final int ANIM_IDLE = 0;
    public static final int ANIM_CHOP = 1;
    public static final int ANIM_KNEEL = 2;

    private static final int CHOP_TICKS = 40;

    private static final EntityDataAccessor<Integer> DATA_ANIM =
            SynchedEntityData.defineId(WoodcutterEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation CHOP = RawAnimation.begin().thenPlay("chop");
    private static final RawAnimation KNEEL = RawAnimation.begin().thenPlayAndHold("kneel");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int animEnd;
    private int fleeTicks;
    private int fadeCountdown;
    private Vec3 fleeDir = Vec3.ZERO;

    public WoodcutterEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANIM, ANIM_IDLE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }

        if (fadeCountdown > 0) {
            fadeCountdown--;
            if (fadeCountdown == 0) {
                fadeAway();
                return;
            }
        }

        if (fleeTicks > 0) {
            fleeTicks--;
            if (fleeTicks % 20 == 0) {
                getNavigation().moveTo(getX() + fleeDir.x * 12.0D, getY(), getZ() + fleeDir.z * 12.0D, 1.3D);
            }
            if (fleeTicks == 0) {
                fadeAway();
            }
            return;
        }

        int anim = entityData.get(DATA_ANIM);
        if (anim == ANIM_CHOP && tickCount >= animEnd) {
            setAnim(ANIM_IDLE);
        } else if (anim == ANIM_IDLE && getRandom().nextInt(400) == 0) {
            setAnim(ANIM_CHOP);
        }
    }

    /** Cambia la animación activa. Se sincroniza con los clientes. */
    public void setAnim(int anim) {
        entityData.set(DATA_ANIM, anim);
        if (anim == ANIM_CHOP) {
            animEnd = tickCount + CHOP_TICKS;
        }
    }

    /** Desaparece en humo dorado dentro de N ticks. */
    public void fadeAfter(int ticks) {
        this.fadeCountdown = ticks;
    }

    /** Se arrodilla frente al tocón (final bueno). */
    public void kneel() {
        setAnim(ANIM_KNEEL);
    }

    /** Se aleja caminando de la cabaña en llamas (final de ceniza). */
    public void flee() {
        fleeDir = Vec3.directionFromRotation(0.0F, getYRot() + 180.0F);
        fleeTicks = 160;
    }

    /** Desaparece en humo dorado. */
    public void fadeAway() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.0D, getZ(), 40, 0.4D, 0.8D, 0.4D, 0.05D);
        }
        discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<WoodcutterEntity>(this, "base", 4, this::predicate));
    }

    private PlayState predicate(AnimationState<WoodcutterEntity> state) {
        int anim = entityData.get(DATA_ANIM);
        if (anim == ANIM_CHOP) {
            return state.setAndContinue(CHOP);
        }
        if (anim == ANIM_KNEEL) {
            return state.setAndContinue(KNEEL);
        }
        return state.isMoving() ? state.setAndContinue(WALK) : state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
