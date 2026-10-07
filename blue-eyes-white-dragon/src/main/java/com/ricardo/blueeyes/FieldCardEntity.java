package com.ricardo.blueeyes;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

/**
 * La carta en el campo.
 *  - INVOCANDO: boca arriba, circulo magico; al terminar aparece el dragon.
 *  - ACTIVA: boca arriba, el dragon esta en el campo.
 *  - DESACTIVADA: boca abajo, sin dragon. Se puede volver a activar.
 */
public class FieldCardEntity extends Entity {

    public static final int SUMMONING = 0;
    public static final int ACTIVE = 1;
    public static final int INACTIVE = 2;

    public static final int SUMMON_TIME = 36;
    /** Espera tras desactivarla a voluntad / tras perder al dragon en combate. */
    private static final int COOLDOWN_RECALL = 60;
    private static final int COOLDOWN_DESTROYED = 600;

    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(FieldCardEntity.class, EntityDataSerializers.INT);

    @Nullable private UUID ownerUUID;
    @Nullable private UUID dragonUUID;
    private int summonTicks;
    private int missing;
    private long cooldownUntil;

    // solo cliente: animacion de volteo
    public int flipTick = -100;
    public boolean lastFaceUp = true;

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
        builder.define(DATA_STATE, SUMMONING);
    }

    public int getState() {
        return this.entityData.get(DATA_STATE);
    }

    private void setState(int state) {
        this.entityData.set(DATA_STATE, state);
    }

    public boolean isFaceUp() {
        return getState() != INACTIVE;
    }

    public boolean isOwnedBy(Player player) {
        return this.ownerUUID != null && this.ownerUUID.equals(player.getUUID());
    }

    @Nullable
    private Player getOwner() {
        return this.ownerUUID == null ? null : this.level().getPlayerByUUID(this.ownerUUID);
    }

    @Nullable
    private BlueEyesDragon getDragon(ServerLevel level) {
        Entity e = this.dragonUUID == null ? null : level.getEntity(this.dragonUUID);
        return e instanceof BlueEyesDragon d && d.isAlive() ? d : null;
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        switch (getState()) {
            case SUMMONING -> {
                summoningEffects(level);
                if (++this.summonTicks >= SUMMON_TIME) {
                    spawnDragon(level);
                }
            }
            case ACTIVE -> {
                if (this.tickCount % 20 == 0) {
                    BlueEyesDragon dragon = getDragon(level);
                    if (dragon == null) {
                        // el dragon fue destruido: la carta queda boca abajo
                        if (++this.missing >= 5) {
                            turnFaceDown(level, COOLDOWN_DESTROYED);
                            Player owner = getOwner();
                            if (owner != null) {
                                owner.displayClientMessage(Component.translatable("message.blueeyes.destroyed", COOLDOWN_DESTROYED / 20)
                                        .withStyle(ChatFormatting.RED), true);
                            }
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
            default -> {
            }
        }
    }

    private void summoningEffects(ServerLevel level) {
        float t = this.summonTicks / (float) SUMMON_TIME;
        double r = 1.6D - t * 0.8D;
        for (int i = 0; i < 3; i++) {
            double a = this.summonTicks * 0.45D + i * (Math.PI * 2.0D / 3.0D);
            level.sendParticles(ParticleTypes.END_ROD, getX() + Math.cos(a) * r, getY() + 0.1D + t * 2.5D,
                    getZ() + Math.sin(a) * r, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX() + Math.cos(-a) * 1.4D, getY() + 0.1D,
                    getZ() + Math.sin(-a) * 1.4D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (this.summonTicks == SUMMON_TIME / 2) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 2.0F, 0.6F);
        }
    }

    private void spawnDragon(ServerLevel level) {
        BlueEyesDragon dragon = BlueEyesMod.DRAGON.get().create(level);
        if (dragon == null) {
            turnFaceDown(level, COOLDOWN_RECALL);
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
        this.missing = 0;
        setState(ACTIVE);

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

    private void turnFaceDown(ServerLevel level, int cooldownTicks) {
        this.dragonUUID = null;
        this.missing = 0;
        this.cooldownUntil = level.getGameTime() + cooldownTicks;
        setState(INACTIVE);
        level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.2D, getZ(), 25, 0.4D, 0.2D, 0.4D, 0.04D);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.2F, 1.2F);
    }

    // ------------------------------------------------------------------ activar / desactivar / recoger

    /** DESACTIVAR: el dragon vuelve a la carta y esta queda boca abajo. */
    public boolean deactivate(@Nullable Player by) {
        if (!(this.level() instanceof ServerLevel level) || getState() == INACTIVE) {
            return false;
        }
        BlueEyesDragon dragon = getDragon(level);
        if (getState() == ACTIVE && dragon != null) {
            dragon.recall();   // al terminar de volver llama a onDragonReturned()
        } else {
            turnFaceDown(level, COOLDOWN_RECALL);
        }
        if (by != null) {
            by.displayClientMessage(Component.translatable("message.blueeyes.deactivated").withStyle(ChatFormatting.AQUA), true);
        }
        return true;
    }

    /** ACTIVAR de nuevo una carta boca abajo. */
    public boolean activate(@Nullable Player by) {
        if (!(this.level() instanceof ServerLevel level) || getState() != INACTIVE) {
            return false;
        }
        long wait = this.cooldownUntil - level.getGameTime();
        if (wait > 0) {
            if (by != null) {
                by.displayClientMessage(Component.translatable("message.blueeyes.cooldown", (wait + 19) / 20).withStyle(ChatFormatting.GRAY), true);
            }
            return false;
        }
        if (by != null) {
            this.setYRot(by.getYRot());
        }
        this.summonTicks = 0;
        setState(SUMMONING);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.4F);
        if (by != null) {
            by.displayClientMessage(Component.translatable("message.blueeyes.activated").withStyle(ChatFormatting.AQUA), true);
        }
        return true;
    }

    void onDragonReturned(BlueEyesDragon dragon) {
        if (this.level() instanceof ServerLevel level && dragon.getUUID().equals(this.dragonUUID)) {
            turnFaceDown(level, COOLDOWN_RECALL);
        }
    }

    /** Recoger la carta: el dragon se retira y la carta vuelve a la mano. */
    public void retrieve(Player player) {
        if (!(this.level() instanceof ServerLevel level) || this.isRemoved()) {
            return;
        }
        BlueEyesDragon dragon = getDragon(level);
        if (dragon != null) {
            dragon.dismiss();
        }
        ItemStack stack = new ItemStack(BlueEyesMod.CARD.get());
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        player.displayClientMessage(Component.translatable("message.blueeyes.card_returned"), true);
        level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.2D, getZ(), 30, 0.4D, 0.3D, 0.4D, 0.05D);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 0.8F);
        this.discard();
    }

    private boolean mayUse(Player player) {
        return this.ownerUUID == null || this.ownerUUID.equals(player.getUUID()) || player.getAbilities().instabuild;
    }

    /** Clic derecho: activa/desactiva. Agachado + clic derecho: recoger la carta. */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (!mayUse(player)) {
                player.displayClientMessage(Component.translatable("message.blueeyes.not_owner"), true);
            } else if (player.isShiftKeyDown()) {
                retrieve(player);
            } else if (getState() == INACTIVE) {
                activate(player);
            } else {
                deactivate(player);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /** Golpear la carta tambien la recoge. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && source.getEntity() instanceof Player player && mayUse(player)) {
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

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
        if (this.dragonUUID != null) {
            tag.putUUID("Dragon", this.dragonUUID);
        }
        tag.putInt("CardState", getState());
        tag.putInt("SummonTicks", this.summonTicks);
        tag.putLong("Cooldown", this.cooldownUntil);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        this.dragonUUID = tag.hasUUID("Dragon") ? tag.getUUID("Dragon") : null;
        setState(tag.getInt("CardState"));
        this.summonTicks = tag.getInt("SummonTicks");
        this.cooldownUntil = tag.getLong("Cooldown");
    }
}
