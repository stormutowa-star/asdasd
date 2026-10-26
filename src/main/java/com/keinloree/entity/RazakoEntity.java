package com.keinloree.entity;

import com.keinloree.registry.ModSounds;
import com.keinloree.story.Factions;
import com.keinloree.story.Lines;
import com.keinloree.util.Geo;
import com.keinloree.util.LooreDamage;
import com.keinloree.vfx.Vfx;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Razako, el Ejecutor. Movimientos pesados con anticipacion legible: combo de tres tajos, embestida, salto del verdugo,
 * rugido, Partir el Horizonte, Cadena del Nucleo y guardia. Con "kneel" activo no muere: cae de rodillas.
 */
public class RazakoEntity extends LooreMob implements Animated {
    private static final EntityDataAccessor<Integer> ANIM = SynchedEntityData.defineId(RazakoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIM_SEQ = SynchedEntityData.defineId(RazakoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> KNEELING = SynchedEntityData.defineId(RazakoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ECHO = SynchedEntityData.defineId(RazakoEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int CRIMSON = 0xFF3B3B;

    /** Lo avisa al caer de rodillas (la historia decide). */
    public static Consumer<RazakoEntity> ON_KNEEL = r -> { };

    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.keinloree.razako"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private final int[] clientAnim = {-1, 0};

    private int move;
    private int moveTicks;
    private int cooldown = 40;
    private int comboStep;
    private int guardTicks;
    private boolean kneelEnabled = true;
    /** Pelea guionizada: no baja del 60% y nunca deja al jugador por debajo de 2 PV. */
    private boolean scripted;
    private int heavyHits;
    private Vec3 chargeDir = Vec3.ZERO;

    private static final int M_NONE = 0;
    private static final int M_COMBO = 1;
    private static final int M_CHARGE = 2;
    private static final int M_LEAP = 3;
    private static final int M_ROAR = 4;
    private static final int M_HORIZON = 5;
    private static final int M_CHAIN = 6;
    private static final int M_GUARD = 7;

    public RazakoEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 200;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LooreMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 900.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.STEP_HEIGHT, 1.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(ANIM, A_NONE);
        b.define(ANIM_SEQ, 0);
        b.define(KNEELING, false);
        b.define(ECHO, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    public int animId() {
        return entityData.get(ANIM);
    }

    @Override
    public int animSeq() {
        return entityData.get(ANIM_SEQ);
    }

    @Override
    public int[] clientAnimState() {
        return clientAnim;
    }

    private void play(int anim) {
        entityData.set(ANIM, anim);
        entityData.set(ANIM_SEQ, animSeq() + 1);
    }

    public boolean isKneeling() {
        return entityData.get(KNEELING);
    }

    /** El "eco de Razako" (version translucida verde de la ruta del Amanecer). */
    public boolean isEcho() {
        return entityData.get(ECHO);
    }

    public void setEcho(boolean echo) {
        entityData.set(ECHO, echo);
        if (echo) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(400.0D);
            setHealth(400.0F);
            kneelEnabled = false;
        }
    }

    public void setScripted(boolean value) {
        scripted = value;
    }

    public boolean isScripted() {
        return scripted;
    }

    public void setKneelEnabled(boolean value) {
        kneelEnabled = value;
    }

    /** Se levanta (tras la escena) y vuelve a pelear o se queda quieto segun la historia. */
    public void standUp() {
        entityData.set(KNEELING, false);
        setLoreDormant(false);
        play(A_NONE);
    }

    // ------------------------------------------------------------------ jefe

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isKneeling() || level().isClientSide) {
            return false;
        }
        if (guardTicks > 0 && source.getEntity() != null) {
            Vec3 to = source.getEntity().position().subtract(position());
            Vec3 look = Vec3.directionFromRotation(0.0F, getYRot());
            if (to.normalize().dot(look) > 0.3D) {
                amount *= 0.2F; // guardia frontal
                Vfx.burst((ServerLevel) level(), position().add(look.scale(1.2D)).add(0.0D, 1.6D, 0.0D), CRIMSON, 10, 0.4F);
            }
        }
        if (scripted && getHealth() - amount < getMaxHealth() * 0.6F) {
            amount = Math.max(0.0F, getHealth() - getMaxHealth() * 0.6F);
        }
        if (getHealth() - amount <= 1.0F && kneelEnabled) {
            setHealth(1.0F);
            entityData.set(KNEELING, true);
            setLoreDormant(true);
            setTarget(null);
            getNavigation().stop();
            move = M_NONE;
            play(A_KNEEL);
            level().playSound(null, getX(), getY(), getZ(), ModSounds.IMPACT.get(), SoundSource.HOSTILE, 2.0F, 0.5F);
            ON_KNEEL.accept(this);
            return true;
        }
        if (amount >= 8.0F && ++heavyHits % 4 == 0 && move == M_NONE) {
            play(A_HURT);
        }
        return super.hurt(source, amount);
    }

    // ------------------------------------------------------------------ IA

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            return;
        }
        ServerLevel sl = (ServerLevel) level();
        bossBar.setProgress(getHealth() / getMaxHealth());
        bossBar.setVisible(getTarget() != null && !isKneeling() && !isLoreDormant());
        if (guardTicks > 0) {
            guardTicks--;
        }
        if (isKneeling() || isLoreDormant()) {
            setDeltaMovement(getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
            return;
        }
        LivingEntity t = getTarget();
        if (move != M_NONE) {
            runMove(sl, t);
            return;
        }
        if (t == null) {
            return;
        }
        getLookControl().setLookAt(t, 30.0F, 30.0F);
        double d = distanceTo(t);
        if (cooldown > 0) {
            cooldown--;
        }
        if (d > 3.2D) {
            if (tickCount % 8 == 0) {
                getNavigation().moveTo(t, d > 10.0D ? 1.35D : 1.0D);
            }
        } else {
            getNavigation().stop();
        }
        if (cooldown > 0) {
            if (d <= 3.6D && cooldown < 20) {
                start(M_COMBO);
            }
            return;
        }
        int r = random.nextInt(100);
        if (d <= 3.6D) {
            start(r < 60 ? M_COMBO : r < 80 ? M_ROAR : M_GUARD);
        } else if (d < 9.0D) {
            start(r < 40 ? M_CHARGE : r < 70 ? M_HORIZON : M_LEAP);
        } else if (d < 18.0D) {
            start(r < 45 ? M_CHAIN : r < 75 ? M_LEAP : M_CHARGE);
        }
    }

    private void start(int m) {
        move = m;
        moveTicks = 0;
        comboStep = 0;
        getNavigation().stop();
        switch (m) {
            case M_COMBO -> play(A_ATTACK_1);
            case M_CHARGE -> play(A_CHARGE);
            case M_LEAP -> play(A_LEAP);
            case M_ROAR -> play(A_ROAR);
            case M_HORIZON -> play(A_HORIZON);
            case M_CHAIN -> play(A_CHAIN);
            case M_GUARD -> {
                play(A_BLOCK);
                guardTicks = 40;
            }
            default -> { }
        }
    }

    private void end(int cd) {
        move = M_NONE;
        cooldown = cd;
    }

    private void runMove(ServerLevel sl, LivingEntity t) {
        moveTicks++;
        if (t != null && move != M_CHARGE) {
            getLookControl().setLookAt(t, 20.0F, 20.0F);
        }
        Vec3 look = Vec3.directionFromRotation(0.0F, getYRot());
        switch (move) {
            case M_COMBO -> {
                // golpes a los 9 (tajo), 22 (barrido) y 40 (al suelo) ticks
                if (moveTicks == 9) {
                    cleave(sl, 3.8D, 14.0F, 0.8D);
                } else if (moveTicks == 17) {
                    play(A_ATTACK_2);
                } else if (moveTicks == 26) {
                    cleave(sl, 4.2D, 14.0F, 1.0D);
                } else if (moveTicks == 32) {
                    play(A_ATTACK_3);
                } else if (moveTicks == 46) {
                    cleave(sl, 4.0D, 20.0F, 0.4D);
                    Vec3 at = position().add(look.scale(3.0D));
                    Vfx.shockwave(sl, at, CRIMSON, 5.0F, 18);
                    Vfx.burst(sl, at.add(0.0D, 0.3D, 0.0D), 0x4CFF7A, 16, 0.6F);
                    areaHit(sl, at, 3.5D, 10.0F);
                } else if (moveTicks >= 56) {
                    end(30 + random.nextInt(20));
                }
            }
            case M_CHARGE -> {
                if (moveTicks == 1 && t != null) {
                    Vec3 d = t.position().subtract(position());
                    chargeDir = new Vec3(d.x, 0.0D, d.z).normalize();
                    setYRot((float) Math.toDegrees(Math.atan2(-chargeDir.x, chargeDir.z)));
                }
                if (moveTicks > 8 && moveTicks <= 20) {
                    setDeltaMovement(chargeDir.scale(0.85D).add(0.0D, getDeltaMovement().y, 0.0D));
                    hurtMarked = true;
                    for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.8D),
                            e -> e != this && Factions.hostile(this, e))) {
                        hit(e, 12.0F, chargeDir, 1.6D);
                    }
                    if (moveTicks % 3 == 0) {
                        Vfx.burst(sl, position().add(0.0D, 0.2D, 0.0D), CRIMSON, 6, 0.3F);
                    }
                }
                if (moveTicks >= 28) {
                    end(50);
                }
            }
            case M_LEAP -> {
                if (moveTicks == 8 && t != null) {
                    Vec3 d = t.position().subtract(position());
                    setDeltaMovement(d.x * 0.14D, 0.9D, d.z * 0.14D);
                    hurtMarked = true;
                }
                if (moveTicks > 12 && onGround()) {
                    Vfx.shockwave(sl, position(), CRIMSON, 6.0F, 20);
                    Vfx.burst(sl, position(), 0x4CFF7A, 24, 0.7F);
                    sl.playSound(null, getX(), getY(), getZ(), ModSounds.SLASH_HEAVY.get(), SoundSource.HOSTILE, 2.0F, 0.6F);
                    areaHit(sl, position(), 5.0D, 14.0F);
                    end(60);
                } else if (moveTicks > 60) {
                    end(40);
                }
            }
            case M_ROAR -> {
                if (moveTicks == 12) {
                    sl.playSound(null, getX(), getY(), getZ(), ModSounds.RAZAKO_ROAR.get(), SoundSource.HOSTILE, 3.0F, 0.8F);
                    Vfx.ringExpanding(sl, position().add(0.0D, 1.0D, 0.0D), new Vec3(0, 1, 0), CRIMSON, 8.0F, 20);
                    for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, Geo.box(position(), 8.0D, 4.0D, 8.0D),
                            e -> e != this && Factions.hostile(this, e))) {
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                    }
                }
                if (moveTicks >= 32) {
                    end(40);
                }
            }
            case M_HORIZON -> {
                if (moveTicks % 6 == 0 && moveTicks < 28) {
                    Vfx.burst(sl, position().add(0.0D, 1.5D, 0.0D), CRIMSON, 4, 0.2F);
                }
                if (moveTicks == 29) {
                    sl.playSound(null, getX(), getY(), getZ(), ModSounds.SLASH_HEAVY.get(), SoundSource.HOSTILE, 3.0F, 0.5F);
                    Vfx.slash(sl, position().add(0.0D, 1.3D, 0.0D), getYRot(), 0.0F, 0.0F, 13.0F, CRIMSON, 12);
                    Vfx.slash(sl, position().add(0.0D, 1.4D, 0.0D), getYRot(), 0.0F, 0.0F, 9.0F, 0xFFFFFF, 10);
                    for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, Geo.box(position(), 14.0D, 3.0D, 14.0D),
                            e -> e != this && Factions.hostile(this, e))) {
                        Vec3 to = e.position().subtract(position());
                        boolean front = new Vec3(to.x, 0, to.z).normalize().dot(look) > -0.1D;
                        boolean dodged = !e.onGround() && e.getY() > getY() + 0.6D; // saltar lo esquiva
                        if (front && !dodged && to.length() <= 14.0D) {
                            hit(e, 22.0F, look, 1.2D);
                        }
                    }
                }
                if (moveTicks >= 42) {
                    end(80);
                }
            }
            case M_CHAIN -> {
                if (moveTicks == 10 && t != null && distanceTo(t) < 18.0D) {
                    sl.playSound(null, getX(), getY(), getZ(), ModSounds.CHAIN.get(), SoundSource.HOSTILE, 2.0F, 0.8F);
                    Vfx.beam(sl, position().add(0.0D, 1.8D, 0.0D), t.position().add(0.0D, 1.0D, 0.0D), 0x4CFF7A, 0.25F, 14);
                    Vec3 pull = position().subtract(t.position()).normalize().scale(1.4D);
                    t.setDeltaMovement(pull.x, 0.45D, pull.z);
                    t.hurtMarked = true;
                }
                if (moveTicks >= 22) {
                    end(30);
                    if (t != null && distanceTo(t) < 4.5D) {
                        start(M_COMBO);
                    }
                }
            }
            case M_GUARD -> {
                if (moveTicks >= 40) {
                    end(20);
                }
            }
            default -> end(20);
        }
    }

    /** Tajo frontal: golpea lo que haya en un cono delante. */
    private void cleave(ServerLevel sl, double reach, float dmg, double push) {
        Vec3 look = Vec3.directionFromRotation(0.0F, getYRot());
        sl.playSound(null, getX(), getY(), getZ(), ModSounds.SLASH_HEAVY.get(), SoundSource.HOSTILE, 1.6F, 0.8F + random.nextFloat() * 0.2F);
        Vfx.slash(sl, position().add(0.0D, 1.6D, 0.0D).add(look.scale(1.0D)), getYRot(), random.nextFloat() * 40.0F - 20.0F,
                random.nextFloat() * 60.0F - 30.0F, (float) reach, CRIMSON, 8);
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, Geo.box(position(), reach + 1.0D, 3.0D, reach + 1.0D),
                e -> e != this && Factions.hostile(this, e))) {
            Vec3 to = e.position().subtract(position());
            if (to.length() <= reach + e.getBbWidth() && new Vec3(to.x, 0, to.z).normalize().dot(look) > 0.25D) {
                hit(e, dmg, look, push);
            }
        }
    }

    private void areaHit(ServerLevel sl, Vec3 at, double r, float dmg) {
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, Geo.box(at, r, 3.0D, r),
                e -> e != this && Factions.hostile(this, e) && e.position().distanceTo(at) <= r)) {
            hit(e, dmg, e.position().subtract(at).normalize(), 0.8D);
            e.setDeltaMovement(e.getDeltaMovement().add(0.0D, 0.5D, 0.0D));
        }
    }

    private void hit(LivingEntity e, float dmg, Vec3 dir, double push) {
        if (isEcho()) {
            dmg *= 0.7F;
        }
        if (scripted && e instanceof Player) {
            dmg = Math.min(dmg, Math.max(0.0F, e.getHealth() - 2.0F));
        }
        LooreDamage.apply(e, damageSources().mobAttack(this), dmg, dir, push);
    }

    /** Dice una linea de dialogo (para las escenas en combate). */
    public void say(String text) {
        if (level() instanceof ServerLevel sl) {
            Lines.say(sl, "RAZAKO", text);
        }
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Kneeling", isKneeling());
        tag.putBoolean("KneelEnabled", kneelEnabled);
        tag.putBoolean("Scripted", scripted);
        tag.putBoolean("Echo", isEcho());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(KNEELING, tag.getBoolean("Kneeling"));
        kneelEnabled = !tag.contains("KneelEnabled") || tag.getBoolean("KneelEnabled");
        scripted = tag.getBoolean("Scripted");
        entityData.set(ECHO, tag.getBoolean("Echo"));
        if (isKneeling()) {
            play(A_KNEEL);
        }
    }
}
