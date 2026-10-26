package com.keinloree.ship;

import com.keinloree.registry.ModEntities;
import com.keinloree.registry.ModSounds;
import com.keinloree.story.Factions;
import com.keinloree.util.LooreDamage;
import com.keinloree.vfx.Vfx;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Rayo de energia (turbolaser, caza, blaster pesado). Avanza por raycast cada tick; al chocar hace dano en area y una
 * explosion visual. Nunca rompe bloques.
 */
public class BoltEntity extends Entity {
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(BoltEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(BoltEntity.class, EntityDataSerializers.FLOAT);

    public static final int LEGION_GREEN = 0x4CFF7A;
    public static final int PLAYER_RED = 0xFF5040;

    private float damage = 6.0F;
    private float radius = 2.0F;
    private int life = 60;
    @Nullable
    private UUID ownerId;
    @Nullable
    private Entity ownerCache;

    public BoltEntity(EntityType<? extends BoltEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** Dispara un rayo desde {@code from} en la direccion {@code dir} (unitaria). */
    public static BoltEntity fire(ServerLevel level, @Nullable Entity owner, Vec3 from, Vec3 dir, double speed, float damage,
                                  float radius, int color, float size) {
        BoltEntity b = new BoltEntity(ModEntities.BOLT.get(), level);
        b.setPos(from.x, from.y, from.z);
        b.setDeltaMovement(dir.normalize().scale(speed));
        b.damage = damage;
        b.radius = radius;
        b.entityData.set(COLOR, color);
        b.entityData.set(SIZE, size);
        if (owner != null) {
            b.ownerId = owner.getUUID();
            b.ownerCache = owner;
        }
        b.alignToVelocity();
        level.addFreshEntity(b);
        return b;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(COLOR, LEGION_GREEN);
        builder.define(SIZE, 1.0F);
    }

    public int getColor() {
        return entityData.get(COLOR);
    }

    public float getSize() {
        return entityData.get(SIZE);
    }

    private void alignToVelocity() {
        Vec3 v = getDeltaMovement();
        double h = Math.sqrt(v.x * v.x + v.z * v.z);
        setYRot((float) Math.toDegrees(Math.atan2(-v.x, v.z)));
        setXRot((float) -Math.toDegrees(Math.atan2(v.y, h)));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Nullable
    private Entity owner() {
        if (ownerCache == null && ownerId != null && level() instanceof ServerLevel sl) {
            ownerCache = sl.getEntity(ownerId);
        }
        return ownerCache;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 from = position();
        Vec3 vel = getDeltaMovement();
        Vec3 to = from.add(vel);
        if (!level().isClientSide) {
            if (--life <= 0) {
                discard();
                return;
            }
            HitResult hit = level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            Vec3 end = hit.getType() != HitResult.Type.MISS ? hit.getLocation() : to;
            EntityHitResult eh = ProjectileUtil.getEntityHitResult(level(), this, from, end,
                    getBoundingBox().expandTowards(vel).inflate(1.0D), this::canHit);
            if (eh != null) {
                impact(eh.getLocation(), eh.getEntity());
                return;
            }
            if (hit instanceof BlockHitResult bh && bh.getType() == HitResult.Type.BLOCK) {
                impact(bh.getLocation(), null);
                return;
            }
        }
        setPos(to.x, to.y, to.z);
    }

    private boolean canHit(Entity e) {
        if (!e.isAlive() || !e.isPickable() || e == owner() || e instanceof BoltEntity) {
            return false;
        }
        Entity o = owner();
        if (o == null) {
            return true;
        }
        if (e.isPassengerOfSameVehicle(o) || o.getVehicle() == e) {
            return false;
        }
        // un jugador puede darle a cualquiera; los demas solo a sus enemigos
        if (o instanceof Player) {
            return true;
        }
        Entity shooter = o instanceof FighterEntity f && f.getControllingPassenger() != null ? f.getControllingPassenger() : o;
        return Factions.hostile(shooter, e);
    }

    private void impact(Vec3 at, @Nullable Entity direct) {
        ServerLevel sl = (ServerLevel) level();
        Entity o = owner();
        DamageSource src = sl.damageSources().indirectMagic(this, o != null ? o : this);
        if (direct != null) {
            hurtTarget(direct, src, damage, at);
        }
        if (radius > 0.0F) {
            AABB box = new AABB(at, at).inflate(radius);
            for (Entity e : sl.getEntities(this, box, ex -> ex != direct && canHit(ex))) {
                double d = e.position().distanceTo(at);
                if (d <= radius + 0.5D) {
                    hurtTarget(e, src, damage * (float) (1.0D - d / (radius + 1.0D)) * 0.7F, at);
                }
            }
        }
        float scale = Math.max(0.5F, radius * 0.6F);
        Vfx.explosion(sl, at, scale);
        sl.playSound(null, at.x, at.y, at.z, ModSounds.IMPACT.get(), SoundSource.HOSTILE, 0.8F + radius * 0.3F,
                0.8F + random.nextFloat() * 0.3F);
        discard();
    }

    private void hurtTarget(Entity e, DamageSource src, float amount, Vec3 at) {
        if (amount <= 0.0F) {
            return;
        }
        Vec3 dir = e.position().subtract(at).normalize();
        if (e instanceof LivingEntity le) {
            LooreDamage.apply(le, src, amount, dir, 0.3D);
        } else {
            e.hurt(src, amount);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 256.0D * 256.0D;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        damage = tag.getFloat("Damage");
        radius = tag.getFloat("Radius");
        life = tag.getInt("Life");
        entityData.set(COLOR, tag.getInt("Color"));
        entityData.set(SIZE, tag.contains("Size") ? tag.getFloat("Size") : 1.0F);
        if (tag.hasUUID("Owner")) {
            ownerId = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", damage);
        tag.putFloat("Radius", radius);
        tag.putInt("Life", life);
        tag.putInt("Color", getColor());
        tag.putFloat("Size", getSize());
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
    }
}
