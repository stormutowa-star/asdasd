package com.ricardo.blueeyes;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** La carta boca arriba en el campo. Invoca al dragon encima y lo mantiene ligado a ella. */
public class FieldCardEntity extends Entity {

    public static final int SUMMON_TIME = 36;

    @Nullable private UUID ownerUUID;
    @Nullable private UUID dragonUUID;
    private boolean summoned;
    private int missing;

    public FieldCardEntity(EntityType<? extends FieldCardEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static FieldCardEntity summon(ServerLevel level, Player owner, Vec3 pos) {
        FieldCardEntity card = BlueEyesMod.FIELD_CARD.get().create(level);
        if (card == null) {
            return null;
        }
        card.setPos(pos);
        card.setYRot(owner.getYRot());
        card.yRotO = owner.getYRot();
        card.ownerUUID = owner.getUUID();
        level.addFreshEntity(card);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.4F);
        return card;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (!this.summoned) {
            summoningEffects(level);
            if (this.tickCount >= SUMMON_TIME) {
                spawnDragon(level);
            }
            return;
        }
        if (this.tickCount % 20 == 0) {
            Entity dragon = this.dragonUUID == null ? null : level.getEntity(this.dragonUUID);
            if (dragon == null || !dragon.isAlive()) {
                // el dragon fue destruido o se retiro: la carta vuelve a ser un objeto
                if (++this.missing >= 5) {
                    level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.2D, getZ(), 25, 0.4D, 0.2D, 0.4D, 0.05D);
                    this.spawnAtLocation(new ItemStack(BlueEyesMod.CARD.get()), 0.2F);
                    this.discard();
                }
            } else {
                this.missing = 0;
            }
        }
        if (this.tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, getX() + (this.random.nextDouble() - 0.5D) * 0.9D, getY() + 0.05D,
                    getZ() + (this.random.nextDouble() - 0.5D) * 0.9D, 1, 0.0D, 0.02D, 0.0D, 0.01D);
        }
    }

    private void summoningEffects(ServerLevel level) {
        // circulo magico que gira alrededor de la carta
        float t = this.tickCount / (float) SUMMON_TIME;
        double r = 1.6D - t * 0.8D;
        for (int i = 0; i < 3; i++) {
            double a = this.tickCount * 0.45D + i * (Math.PI * 2.0D / 3.0D);
            level.sendParticles(ParticleTypes.END_ROD, getX() + Math.cos(a) * r, getY() + 0.1D + t * 2.5D,
                    getZ() + Math.sin(a) * r, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX() + Math.cos(-a) * 1.4D, getY() + 0.1D,
                    getZ() + Math.sin(-a) * 1.4D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (this.tickCount == SUMMON_TIME / 2) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 2.0F, 0.6F);
        }
    }

    private void spawnDragon(ServerLevel level) {
        this.summoned = true;
        BlueEyesDragon dragon = BlueEyesMod.DRAGON.get().create(level);
        if (dragon == null) {
            return;
        }
        float yaw = this.getYRot() + 180.0F;   // mira hacia quien lo invoca
        dragon.moveTo(getX(), getY() + 0.6D, getZ(), yaw, 0.0F);
        dragon.setYBodyRot(yaw);
        dragon.setYHeadRot(yaw);
        dragon.setOwnerUUID(this.ownerUUID);
        dragon.setCardUUID(this.getUUID());
        level.addFreshEntity(dragon);
        this.dragonUUID = dragon.getUUID();

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(getX(), getY(), getZ());
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        level.sendParticles(ParticleTypes.FLASH, getX(), getY() + 2.0D, getZ(), 2, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.5D, getZ(), 120, 1.2D, 2.0D, 1.2D, 0.15D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 2.5D, getZ(), 80, 1.5D, 2.0D, 1.5D, 0.3D);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL, 3.0F, 1.1F);
    }

    /** Recoger la carta: el dragon se retira y la carta vuelve a la mano. */
    public void retrieve(Player player) {
        if (!(this.level() instanceof ServerLevel level) || this.isRemoved()) {
            return;
        }
        Entity dragon = this.dragonUUID == null ? null : level.getEntity(this.dragonUUID);
        if (dragon instanceof BlueEyesDragon d) {
            d.dismiss();
        }
        ItemStack stack = new ItemStack(BlueEyesMod.CARD.get());
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        player.displayClientMessage(Component.translatable("message.blueeyes.card_returned"), true);
        level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.2D, getZ(), 30, 0.4D, 0.3D, 0.4D, 0.05D);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
        this.discard();
    }

    private boolean mayRetrieve(Player player) {
        return this.ownerUUID == null || this.ownerUUID.equals(player.getUUID()) || player.getAbilities().instabuild;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            if (mayRetrieve(player)) {
                retrieve(player);
            } else {
                player.displayClientMessage(Component.translatable("message.blueeyes.not_owner"), true);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && source.getEntity() instanceof Player player && mayRetrieve(player)) {
            retrieve(player);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(BlueEyesMod.CARD.get());
    }

    public float summonProgress(float partialTick) {
        return this.summoned ? 1.0F : Mth.clamp((this.tickCount + partialTick) / SUMMON_TIME, 0.0F, 1.0F);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
        if (this.dragonUUID != null) {
            tag.putUUID("Dragon", this.dragonUUID);
        }
        tag.putBoolean("Summoned", this.summoned);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        this.dragonUUID = tag.hasUUID("Dragon") ? tag.getUUID("Dragon") : null;
        this.summoned = tag.getBoolean("Summoned");
    }
}
