package com.keinloree.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;

/**
 * Camara cinematica. Entidad invisible que solo crea el cliente (id negativo fijo) y que se pone
 * como entidad de camara. Nunca se guarda ni existe en el servidor.
 */
public class CameraEntity extends Entity {

    public CameraEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    /** Altura de ojos fija igual a la del jugador de pie: la camara vanilla no da tirones al cambiar de entidad. */
    public static final float EYE_HEIGHT = 1.62F;

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(0.2F, 0.2F).withEyeHeight(EYE_HEIGHT);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void tick() {
        // En el servidor no debe vivir nunca: si aparece, se retira.
        if (!this.level().isClientSide) {
            this.discard();
            return;
        }
        super.tick();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}
