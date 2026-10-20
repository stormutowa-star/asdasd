package com.keinloree.entity;

import com.keinloree.util.Fx;
import com.keinloree.util.Geo;
import com.keinloree.util.LooreDamage;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Base de la faccion Loree (Kein Loree, comandantes y droides). Los datos que dibuja el modelo
 * viajan sincronizados al cliente; el daño y el empuje se calculan siempre en el servidor.
 */
public abstract class LooreMob extends PathfinderMob implements LooreVisual {

    private static final EntityDataAccessor<Integer> POSE =
            SynchedEntityData.defineId(LooreMob.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FORM =
            SynchedEntityData.defineId(LooreMob.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> WING =
            SynchedEntityData.defineId(LooreMob.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SPAWN =
            SynchedEntityData.defineId(LooreMob.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DORMANT =
            SynchedEntityData.defineId(LooreMob.class, EntityDataSerializers.BOOLEAN);

    /** Ticks que dura una pose especial antes de volver a la de reposo. */
    private int poseHold;

    protected LooreMob(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(POSE, POSE_IDLE);
        builder.define(FORM, 0);
        builder.define(WING, 0.0F);
        builder.define(SPAWN, 0);
        builder.define(DORMANT, false);
    }

    @Override
    public int getLorePose() {
        return this.entityData.get(POSE);
    }

    public void setLorePose(int pose) {
        this.entityData.set(POSE, pose);
    }

    @Override
    public int getLoreForm() {
        return this.entityData.get(FORM);
    }

    public void setLoreForm(int form) {
        this.entityData.set(FORM, form);
    }

    @Override
    public float getWingOpen() {
        return this.entityData.get(WING);
    }

    public void setWingOpen(float wing) {
        this.entityData.set(WING, wing);
    }

    @Override
    public int getSpawnTicks() {
        return this.entityData.get(SPAWN);
    }

    public void setSpawnTicks(int ticks) {
        this.entityData.set(SPAWN, ticks);
    }

    @Override
    public boolean isLoreDormant() {
        return this.entityData.get(DORMANT);
    }

    public void setLoreDormant(boolean dormant) {
        this.entityData.set(DORMANT, dormant);
    }

    @Override
    public int getLoreSkin() {
        return 0;
    }

    /** Pone una pose durante un numero de ticks y despues vuelve a la de reposo. */
    protected void holdPose(int pose, int ticks) {
        this.setLorePose(pose);
        this.poseHold = ticks;
    }

    /** Los mobs vanilla ignoran a los jugadores en creativo; la faccion Loree no. */
    private void acquireTarget() {
        LivingEntity cur = this.getTarget();
        if (cur != null && cur.isAlive() && !(cur instanceof net.minecraft.world.entity.player.Player pl && pl.isSpectator())) {
            return;
        }
        double range = this.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE);
        net.minecraft.world.entity.player.Player p = this.level().getNearestPlayer(this, range);
        this.setTarget(p != null && p.isAlive() && !p.isSpectator() ? p : null);
    }

    /** Golpe cuerpo a cuerpo que empuja tambien a jugadores en creativo. */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (target instanceof LivingEntity living) {
            this.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            this.strike(living, (float) this.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), 0.6D);
            return true;
        }
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.tickCount % 10 == 0) {
            this.acquireTarget();
        }
        if (!this.level().isClientSide && this.poseHold > 0) {
            this.poseHold--;
            if (this.poseHold == 0) {
                this.setLorePose(POSE_IDLE);
            }
        }
    }

    /** Direccion horizontal de a hacia b (para el empuje). */
    protected static Vec3 flat(Vec3 from, Vec3 to) {
        Vec3 d = new Vec3(to.x - from.x, 0.0D, to.z - from.z);
        return d.lengthSqr() < 1.0E-9D ? new Vec3(0.0D, 0.0D, 1.0D) : d.normalize();
    }

    /** Golpe cuerpo a cuerpo de la faccion. */
    protected void strike(LivingEntity target, float damage, double push) {
        LooreDamage.apply(target, this.damageSources().mobAttack(this), damage, flat(this.position(), target.position()), push);
    }

    /**
     * Rayo verde instantaneo: dibuja la linea y dana al primer ser vivo que toque.
     * Nunca dana a la faccion Loree.
     */
    protected void laser(ServerLevel level, Vec3 from, Vec3 to, float damage, ParticleOptions color) {
        BlockHitResult block = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        Vec3 end = block.getType() != HitResult.Type.MISS ? block.getLocation() : to;
        Fx.line(level, from, end, color, 0.45D);

        List<LivingEntity> near = level.getEntitiesOfClass(LivingEntity.class, Geo.span(from, end, 1.0D),
                e -> e != this && e.isAlive() && !(e instanceof LooreMob));
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : near) {
            Vec3 center = e.position().add(0.0D, e.getBbHeight() * 0.5D, 0.0D);
            if (Geo.distToSegment(center, from, end) > 0.9D + e.getBbWidth() * 0.5D) {
                continue;
            }
            double d = from.distanceToSqr(center);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        if (best != null) {
            Fx.burst(level, ParticleTypes.ELECTRIC_SPARK, best.position().add(0.0D, best.getBbHeight() * 0.5D, 0.0D),
                    6, 0.3D, 0.05D);
            LooreDamage.apply(best, this.damageSources().mobAttack(this), damage, flat(from, end), 0.35D);
        }
    }
}
