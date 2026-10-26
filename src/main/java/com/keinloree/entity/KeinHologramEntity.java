package com.keinloree.entity;

import com.keinloree.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Holograma gigante de Kein Loree (transmisiones al mundo). Sin colision; dura lo que diga la historia. */
public class KeinHologramEntity extends Entity implements LooreVisual {
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(KeinHologramEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> FADE = SynchedEntityData.defineId(KeinHologramEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> POSE = SynchedEntityData.defineId(KeinHologramEntity.class, EntityDataSerializers.INT);

    /** Ticks de vida que le quedan (-1 = hasta que la historia lo quite). */
    private int life = -1;

    public KeinHologramEntity(EntityType<? extends KeinHologramEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static KeinHologramEntity create(ServerLevel level, Vec3 at, float yaw, float scale) {
        KeinHologramEntity h = new KeinHologramEntity(ModEntities.KEIN_HOLOGRAM.get(), level);
        h.moveTo(at.x, at.y, at.z, yaw, 0.0F);
        h.entityData.set(SCALE, scale);
        level.addFreshEntity(h);
        return h;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(SCALE, 12.0F);
        b.define(FADE, 0);
        b.define(POSE, POSE_SPREAD);
    }

    public float scale() {
        return entityData.get(SCALE);
    }

    /** 0 = visible; >0 = se esta deshaciendo (ticks transcurridos). */
    public int fade() {
        return entityData.get(FADE);
    }

    public void dissolve() {
        if (fade() == 0) {
            entityData.set(FADE, 1);
            life = 40;
        }
    }

    public void setPoseId(int p) {
        entityData.set(POSE, p);
    }

    /** Gira suavemente hacia un punto. */
    public void faceTowards(Vec3 p) {
        Vec3 d = p.subtract(position());
        setYRot((float) Math.toDegrees(Math.atan2(-d.x, d.z)));
    }

    @Override
    public void tick() {
        super.tick();
        if (fade() > 0 && !level().isClientSide) {
            entityData.set(FADE, fade() + 1);
        }
        if (!level().isClientSide && life > 0 && --life == 0) {
            discard();
        }
    }

    @Override
    public int getLorePose() {
        return entityData.get(POSE);
    }

    @Override
    public int getLoreForm() {
        return 1;
    }

    @Override
    public float getWingOpen() {
        return 1.0F;
    }

    @Override
    public int getSpawnTicks() {
        return 0;
    }

    @Override
    public boolean isLoreDormant() {
        return false;
    }

    @Override
    public int getLoreSkin() {
        return 0;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 512.0D * 512.0D;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        float s = scale();
        return getBoundingBox().inflate(s * 1.5D, s * 3.0D, s * 1.5D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(SCALE, tag.getFloat("Scale"));
        life = tag.getInt("Life");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Scale", scale());
        tag.putInt("Life", life);
    }
}
