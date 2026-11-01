package com.ricardo.retorcido;

import net.minecraft.core.BlockPos;
import com.ricardo.retorcido.terror.TerrorConfig;
import com.ricardo.retorcido.terror.TerrorEvents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class RetorcidoEntity extends Monster {
    public RetorcidoEntity(EntityType<? extends RetorcidoEntity> type, Level level) {
        super(type, level);
        this.xpReward = 30; // experiencia (la loot table solo da objetos)
    }

    /** Jugador al que observa (null = Retorcido normal). Solo servidor, no se guarda. */
    private UUID watchTarget;
    private int stareTicks;
    private boolean discardOnLoad;

    /** Un Retorcido "watcher" solo observa (sin IA, invulnerable, silencioso) y el amigo no huye de el. */
    public boolean isWatcher() {
        return this.watchTarget != null;
    }

    /** Lo convierte en observador del jugador dado. */
    public void makeWatcher(UUID targetPlayer) {
        this.watchTarget = targetPlayer;
        this.stareTicks = 0;
        this.setNoAi(true);
        this.setInvulnerable(true);
        this.setSilent(true);
    }

    private void vanish(boolean seen) {
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(),
                    25, 0.4D, 0.8D, 0.4D, 0.02D);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.HOSTILE, 1.0F, 0.5F);
        }
        this.discard();
    }

    private void watcherTick() {
        if (this.discardOnLoad) { // cargado del disco como observador: fuera
            this.discard();
            return;
        }
        ServerPlayer p = this.level().getServer().getPlayerList().getPlayer(this.watchTarget);
        if (p == null || p.level() != this.level() || !p.isAlive() || this.tickCount > TerrorConfig.WATCHER_MAX_AGE) {
            this.vanish(false);
            return;
        }
        // Se orienta hacia el jugador.
        double dx = p.getX() - this.getX();
        double dz = p.getZ() - this.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        this.setYRot(yaw);
        this.setXRot(0.0F);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;

        if (this.distanceTo(p) < TerrorConfig.WATCHER_VANISH_DIST) {
            this.vanish(false);
            return;
        }
        if (this.tickCount % TerrorConfig.WATCHER_CHECK_INTERVAL == 0) {
            Vec3 toMob = this.getEyePosition().subtract(p.getEyePosition()).normalize();
            boolean looking = p.getViewVector(1.0F).dot(toMob) > TerrorConfig.WATCHER_LOOK_DOT && p.hasLineOfSight(this);
            if (looking) {
                this.stareTicks += TerrorConfig.WATCHER_CHECK_INTERVAL;
            } else {
                this.stareTicks = Math.max(0, this.stareTicks - TerrorConfig.WATCHER_CHECK_INTERVAL);
            }
            if (this.stareTicks > TerrorConfig.WATCHER_STARE_LIMIT) {
                TerrorEvents.onWatcherSeen(p);
                this.vanish(true);
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * Gotas de sangre desde la boca (solo cliente). Posicion de la boca calculada con el port del modelo
     * (tools de render): en reposo ~1.65 delante y 0.92 de altura; atacando la cabeza baja: ~1.28 y 0.27.
     */
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.isAlive() && (this.watchTarget != null || this.discardOnLoad)) {
            try {
                this.watcherTick();
            } catch (Exception e) {
                this.discard();
            }
        }
        if (this.level().isClientSide && this.isAlive()) {
            boolean angry = this.isAggressive();
            int period = angry ? 2 : 4;
            if (this.tickCount % period == 0) {
                float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
                double fwd = angry ? 1.28D : 1.65D;
                double h = angry ? 0.27D : 0.92D;
                int n = angry ? 2 : 1 + this.random.nextInt(2);
                for (int i = 0; i < n; i++) {
                    double jitter = (this.random.nextDouble() - 0.5D) * 0.25D;
                    double x = this.getX() - Mth.sin(yaw) * fwd + Mth.cos(yaw) * jitter;
                    double y = this.getY() + h;
                    double z = this.getZ() + Mth.cos(yaw) * fwd + Mth.sin(yaw) * jitter;
                    this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK,
                            Blocks.NETHER_WART_BLOCK.defaultBlockState()), x, y, z, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.watchTarget != null) tag.putBoolean("RetorcidoWatcher", true);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("RetorcidoWatcher")) this.discardOnLoad = true;
    }

    // Sonidos vanilla (Warden), ya graves de por si; ademas bajamos el tono.
    // Sin @Override a proposito: en 1.21.1 la visibilidad de este metodo en LivingEntity
    // no importa, "public" compila tanto si la base es protected como public.
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.65F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WARDEN_STEP, 0.2F, 0.8F);
    }
}
