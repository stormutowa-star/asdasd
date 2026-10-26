package com.keinloree.entity;

import com.keinloree.registry.ModEntities;
import com.keinloree.registry.ModSounds;
import com.keinloree.ship.BoltEntity;
import com.keinloree.story.Campaign;
import com.keinloree.story.Factions;
import com.keinloree.util.Geo;
import com.keinloree.util.LooreDamage;
import com.keinloree.vfx.Vfx;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Uno de los Cinco Calendarios (variante HOUR 0..4: Alba, Cenit, Ocaso, Medianoche, Eclipse). Pelean contra la Legion
 * con su estilo propio; no mueren por la Legion (se retiran a su pilar) y no reciben dano de jugadores salvo traicion.
 */
public class CalendarioEntity extends PathfinderMob implements Animated {
    public static final int ALBA = 0;
    public static final int CENIT = 1;
    public static final int OCASO = 2;
    public static final int MEDIANOCHE = 3;
    public static final int ECLIPSE = 4;
    public static final String[] IDS = {"alba", "cenit", "ocaso", "medianoche", "eclipse"};
    public static final String[] SPEAKERS = {"ALBA", "CENIT", "OCASO", "MEDIANOCHE", "ECLIPSE"};
    public static final int[] COLORS = {0xFFC861, 0xFFF2A8, 0xC24CFF, 0x6F7BFF, 0xE8E8FF};

    private static final EntityDataAccessor<Integer> HOUR = SynchedEntityData.defineId(CalendarioEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIM = SynchedEntityData.defineId(CalendarioEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIM_SEQ = SynchedEntityData.defineId(CalendarioEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(CalendarioEntity.class, EntityDataSerializers.INT);

    /** Modos: libre (vigila su pilar), seguir al heroe, duelo de entrenamiento, hostil (traicion), congelado (final malo). */
    public static final int MODE_GUARD = 0;
    public static final int MODE_FOLLOW = 1;
    public static final int MODE_DUEL = 2;
    public static final int MODE_HOSTILE = 3;
    public static final int MODE_FROZEN = 4;
    public static final int MODE_STILL = 5;

    private final int[] clientAnim = {-1, 0};
    @Nullable
    private BlockPos home;
    @Nullable
    private UUID followId;
    private int cooldown = 30;
    private int move;
    private int moveTicks;
    private int retreatHeal;
    /** Golpes recibidos del heroe en un duelo de entrenamiento. */
    public int duelHits;
    private Vec3 dashDir = Vec3.ZERO;

    public CalendarioEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static CalendarioEntity create(ServerLevel level, int hour, Vec3 at, float yaw) {
        CalendarioEntity c = new CalendarioEntity(ModEntities.CALENDARIO.get(), level);
        c.entityData.set(HOUR, hour);
        c.moveTo(at.x, at.y, at.z, yaw, 0.0F);
        c.setYHeadRot(yaw);
        c.setYBodyRot(yaw);
        c.setCustomName(Component.translatable("entity.keinloree.calendario." + IDS[hour]));
        level.addFreshEntity(c);
        return c;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.STEP_HEIGHT, 1.1D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(HOUR, ALBA);
        b.define(ANIM, A_NONE);
        b.define(ANIM_SEQ, 0);
        b.define(MODE, MODE_GUARD);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7D) {
            @Override
            public boolean canUse() {
                return getMode() == MODE_GUARD && getTarget() == null && super.canUse();
            }
        });
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    public int hour() {
        return entityData.get(HOUR);
    }

    public int getMode() {
        return entityData.get(MODE);
    }

    public void setMode(int mode) {
        entityData.set(MODE, mode);
        if (mode == MODE_FROZEN || mode == MODE_STILL) {
            getNavigation().stop();
            setTarget(null);
        }
    }

    public void setHome(BlockPos p) {
        home = p;
        restrictTo(p, 24);
    }

    @Nullable
    public BlockPos home() {
        return home;
    }

    public void follow(@Nullable Entity e) {
        followId = e == null ? null : e.getUUID();
        setMode(e == null ? MODE_GUARD : MODE_FOLLOW);
        if (e != null) {
            clearRestriction();
        }
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

    public void play(int anim) {
        entityData.set(ANIM, anim);
        entityData.set(ANIM_SEQ, animSeq() + 1);
    }

    public int color() {
        return COLORS[Math.floorMod(hour(), 5)];
    }

    // ------------------------------------------------------------------ dano

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || getMode() == MODE_FROZEN) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (getMode() == MODE_DUEL && attacker instanceof Player) {
            duelHits++;
            Vfx.burst((ServerLevel) level(), position().add(0.0D, 1.2D, 0.0D), color(), 8, 0.4F);
            level().playSound(null, getX(), getY(), getZ(), ModSounds.TIME_TICK.get(), SoundSource.NEUTRAL, 1.0F, 1.4F);
            return false;
        }
        if (attacker instanceof Player p && !Factions.hostile(this, p) && getMode() != MODE_HOSTILE) {
            return false;
        }
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
            return false;
        }
        boolean r = super.hurt(source, amount);
        if (getHealth() < getMaxHealth() * 0.15F && getMode() != MODE_HOSTILE && getMode() != MODE_DUEL) {
            retreat();
        }
        return r;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource src) {
        return false;
    }

    /** Se retira a su pilar (o a su casa) con un destello y se cura poco a poco. */
    public void retreat() {
        if (!(level() instanceof ServerLevel sl)) {
            return;
        }
        Vfx.pillar(sl, position(), 30.0F, color(), 30);
        if (home != null) {
            teleportTo(home.getX() + 0.5D, home.getY() + 1.0D, home.getZ() + 0.5D);
            Vfx.pillar(sl, position(), 30.0F, color(), 30);
        }
        setTarget(null);
        retreatHeal = 400;
        if (getMode() == MODE_FOLLOW) {
            setMode(MODE_GUARD);
        }
    }

    @Override
    public void die(DamageSource source) {
        // Las Horas no mueren en combate normal: si llega aqui (p. ej. /kill) se deja morir
        super.die(source);
    }

    // ------------------------------------------------------------------ IA

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            return;
        }
        ServerLevel sl = (ServerLevel) level();
        int mode = getMode();
        if (mode == MODE_FROZEN || mode == MODE_STILL) {
            getNavigation().stop();
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            return;
        }
        if (retreatHeal > 0) {
            retreatHeal--;
            if (tickCount % 20 == 0) {
                heal(8.0F);
            }
        }
        if (tickCount % 10 == 0) {
            pickTarget(sl);
        }
        LivingEntity t = getTarget();
        if (move != 0) {
            runMove(sl, t);
            return;
        }
        if (cooldown > 0) {
            cooldown--;
        }
        if (t == null) {
            if (mode == MODE_FOLLOW) {
                Entity f = followId == null ? null : sl.getEntity(followId);
                if (f != null && distanceToSqr(f) > 36.0D && tickCount % 10 == 0) {
                    if (distanceToSqr(f) > 40.0D * 40.0D) {
                        teleportTo(f.getX() + 2.0D, f.getY(), f.getZ() + 2.0D);
                    } else {
                        getNavigation().moveTo(f, 1.2D);
                    }
                }
            } else if (home != null && distanceToSqr(home.getCenter()) > 30.0D * 30.0D && tickCount % 20 == 0) {
                getNavigation().moveTo(home.getX() + 0.5D, home.getY() + 1.0D, home.getZ() + 0.5D, 1.0D);
            }
            return;
        }
        getLookControl().setLookAt(t, 30.0F, 30.0F);
        double d = distanceTo(t);
        double reach = hour() == ECLIPSE ? 4.2D : hour() == CENIT ? 3.6D : 3.0D;
        boolean ranged = hour() == MEDIANOCHE;
        if ((ranged ? d > 12.0D : d > reach - 0.4D) && tickCount % 6 == 0) {
            getNavigation().moveTo(t, d > 10.0D ? 1.3D : 1.05D);
        } else if (!ranged && d <= reach - 0.4D) {
            getNavigation().stop();
        }
        if (cooldown > 0) {
            return;
        }
        int r = random.nextInt(100);
        switch (hour()) {
            case ALBA -> start(d > 6.0D ? 4 : r < 70 ? 1 : 5);
            case CENIT -> start(d < 4.0D ? (r < 60 ? 1 : 4) : (r < 50 ? 5 : 4));
            case OCASO -> start(d > 5.0D ? 4 : r < 65 ? 1 : 5);
            case MEDIANOCHE -> start(r < 40 ? 4 : r < 70 ? 5 : 6);
            default -> start(d > 7.0D ? 2 : r < 55 ? 1 : r < 80 ? 4 : 5);
        }
    }

    private void pickTarget(ServerLevel sl) {
        LivingEntity cur = getTarget();
        if (cur != null && cur.isAlive() && isEnemy(cur) && cur.distanceToSqr(this) < 40.0D * 40.0D) {
            return;
        }
        LivingEntity best = null;
        double bd = Double.MAX_VALUE;
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(32.0D), this::isEnemy)) {
            double dd = e.distanceToSqr(this);
            if (dd < bd && hasLineOfSight(e)) {
                bd = dd;
                best = e;
            }
        }
        setTarget(best);
    }

    private boolean isEnemy(LivingEntity e) {
        if (e == this || !e.isAlive()) {
            return false;
        }
        if (getMode() == MODE_DUEL || getMode() == MODE_HOSTILE) {
            ServerPlayer hero = heroPlayer();
            if (e == hero) {
                return true;
            }
        }
        return Factions.hostile(this, e);
    }

    @Nullable
    private ServerPlayer heroPlayer() {
        if (!(level() instanceof ServerLevel sl)) {
            return null;
        }
        UUID h = Campaign.get(sl.getServer()).hero;
        return h == null ? null : sl.getServer().getPlayerList().getPlayer(h);
    }

    // Movimientos: 1 combo, 2 estocada/embestida, 4 especial 1, 5 especial 2, 6 lanzamiento (cast)
    private void start(int m) {
        move = m;
        moveTicks = 0;
        switch (m) {
            case 1 -> play(A_ATTACK_1);
            case 2 -> play(A_ATTACK_3);
            case 4 -> play(A_SPECIAL_1);
            case 5 -> play(A_SPECIAL_2);
            case 6 -> play(A_CAST);
            default -> { }
        }
    }

    private void end(int cd) {
        move = 0;
        cooldown = cd;
    }

    private float dmg(float base) {
        return getMode() == MODE_DUEL ? Math.min(3.0F, base) : base;
    }

    private void runMove(ServerLevel sl, @Nullable LivingEntity t) {
        moveTicks++;
        int h = hour();
        int c = color();
        Vec3 look = Vec3.directionFromRotation(0.0F, getYRot());
        Vec3 chest = position().add(0.0D, 1.3D, 0.0D);
        switch (move) {
            case 1 -> { // combo de 3 golpes
                int speed = h == CENIT ? 12 : h == ALBA ? 7 : 9;
                if (moveTicks == speed / 2 + 2 || moveTicks == speed + speed / 2 + 2 || moveTicks == 2 * speed + speed / 2 + 2) {
                    float base = h == CENIT ? 13.0F : h == ALBA ? 7.0F : 9.0F;
                    double reach = h == ECLIPSE ? 4.5D : h == CENIT ? 3.8D : 3.2D;
                    strikeArc(sl, reach, dmg(base), c, h == OCASO ? 140 : 80);
                    sound(h == CENIT ? ModSounds.SLASH_HEAVY.get() : ModSounds.SLASH_LIGHT.get(), 1.2F);
                }
                if (moveTicks == speed) {
                    play(A_ATTACK_2);
                } else if (moveTicks == 2 * speed) {
                    play(A_ATTACK_3);
                } else if (moveTicks >= 3 * speed + 6) {
                    end(h == ALBA ? 8 : 18);
                }
            }
            case 2 -> { // estocada larga (Eclipse)
                if (moveTicks == 6) {
                    setDeltaMovement(look.scale(1.1D).add(0.0D, 0.1D, 0.0D));
                    hurtMarked = true;
                }
                if (moveTicks == 10) {
                    strikeArc(sl, 5.0D, dmg(12.0F), c, 40);
                    Vfx.beam(sl, chest, chest.add(look.scale(6.0D)), c, 0.18F, 6);
                    sound(ModSounds.SLASH_LIGHT.get(), 1.4F);
                }
                if (moveTicks >= 18) {
                    end(20);
                }
            }
            case 4 -> special1(sl, t, look, chest, c);
            case 5 -> special2(sl, t, look, chest, c);
            case 6 -> { // Medianoche: orbes estelares a distancia
                if (moveTicks == 10 && t != null) {
                    Vec3 to = t.position().add(0.0D, t.getBbHeight() * 0.5D, 0.0D).subtract(chest).normalize();
                    BoltEntity.fire(sl, this, chest.add(to), to, 2.2D, dmg(8.0F), 1.5F, c, 1.2F);
                    sound(ModSounds.STAR_FALL.get(), 1.0F);
                }
                if (moveTicks >= 20) {
                    end(25);
                }
            }
            default -> end(10);
        }
    }

    private void special1(ServerLevel sl, @Nullable LivingEntity t, Vec3 look, Vec3 chest, int c) {
        switch (hour()) {
            case ALBA -> { // Primer Rayo: embestida de 10 bloques
                if (moveTicks == 3) {
                    dashDir = t != null ? flat(t.position().subtract(position())) : look;
                    sound(ModSounds.SLASH_LIGHT.get(), 1.5F);
                }
                if (moveTicks > 3 && moveTicks <= 11) {
                    setDeltaMovement(dashDir.scale(1.25D).add(0.0D, getDeltaMovement().y, 0.0D));
                    hurtMarked = true;
                    Vfx.trail(sl, this, c, 16);
                    hitNear(sl, 1.6D, dmg(10.0F), c);
                }
                if (moveTicks >= 16) {
                    end(70);
                }
            }
            case CENIT -> { // Llamarada Solar: salto y golpe al suelo
                if (moveTicks == 7) {
                    setDeltaMovement(look.scale(0.3D).add(0.0D, 0.85D, 0.0D));
                    hurtMarked = true;
                }
                if (moveTicks > 12 && onGround()) {
                    sound(ModSounds.SOLAR_FLARE.get(), 2.0F);
                    Vfx.shockwave(sl, position(), c, 7.0F, 20);
                    Vfx.burst(sl, position().add(0.0D, 0.4D, 0.0D), 0xFFD27A, 30, 0.8F);
                    for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, Geo.box(position(), 7.0D, 3.0D, 7.0D), this::isEnemy)) {
                        hurtEnemy(e, dmg(16.0F), e.position().subtract(position()).normalize(), 0.8D);
                        e.setDeltaMovement(e.getDeltaMovement().add(0.0D, 0.8D, 0.0D));
                    }
                    end(90);
                } else if (moveTicks > 60) {
                    end(40);
                }
            }
            case OCASO -> { // Paso Umbrio: aparece detras del objetivo
                if (moveTicks == 4 && t != null) {
                    sound(ModSounds.SHADOW_STEP.get(), 1.4F);
                    Vfx.distortion(sl, chest, c, 1.6F, 12);
                    Vec3 behind = t.position().subtract(flat(position().subtract(t.position())).scale(-1.6D));
                    Vec3 back = t.position().add(flat(t.position().subtract(position())).scale(1.6D));
                    teleportTo(back.x, t.getY(), back.z);
                    Vfx.distortion(sl, position().add(0.0D, 1.2D, 0.0D), c, 1.6F, 12);
                    getLookControl().setLookAt(t);
                    if (behind != null) {
                        hurtEnemy(t, dmg(18.0F), flat(t.position().subtract(position())), 0.6D);
                    }
                }
                if (moveTicks >= 16) {
                    end(80);
                }
            }
            case MEDIANOCHE -> { // Lluvia de Estrellas
                if (moveTicks == 12) {
                    sound(ModSounds.STAR_FALL.get(), 2.0F);
                }
                if (moveTicks >= 14 && moveTicks < 38 && moveTicks % 3 == 0) {
                    Vec3 center = t != null ? t.position() : position().add(look.scale(6.0D));
                    Vec3 at = center.add(random.nextGaussian() * 3.0D, 0.0D, random.nextGaussian() * 3.0D);
                    Vfx.beam(sl, at.add(0.0D, 26.0D, 0.0D), at, c, 0.35F, 8);
                    Vfx.ring(sl, at.add(0.0D, 0.1D, 0.0D), new Vec3(0, 1, 0), 0xFFFFFF, 1.6F, 10);
                    for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, Geo.box(at, 1.8D, 3.0D, 1.8D), this::isEnemy)) {
                        hurtEnemy(e, dmg(7.0F), new Vec3(0, -1, 0), 0.2D);
                    }
                }
                if (moveTicks >= 44) {
                    end(90);
                }
            }
            default -> { // Eclipse: Hora Detenida
                if (moveTicks == 10) {
                    sound(ModSounds.TIME_STOP.get(), 2.0F);
                    Vfx.timeFreeze(sl, position().add(0.0D, 1.0D, 0.0D), 8.0F, 60);
                    for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, Geo.box(position(), 8.0D, 4.0D, 8.0D), this::isEnemy)) {
                        if (getMode() != MODE_DUEL || !(e instanceof Player)) {
                            TimeStop.freeze(e, 60);
                        } else {
                            TimeStop.freeze(e, 25);
                        }
                    }
                }
                if (moveTicks >= 24) {
                    end(160);
                }
            }
        }
    }

    private void special2(ServerLevel sl, @Nullable LivingEntity t, Vec3 look, Vec3 chest, int c) {
        switch (hour()) {
            case ALBA -> { // Corona Solar: salto con giro
                if (moveTicks == 4) {
                    setDeltaMovement(0.0D, 0.7D, 0.0D);
                    hurtMarked = true;
                }
                if (moveTicks == 12) {
                    Vfx.ringExpanding(sl, chest, new Vec3(0, 1, 0), c, 5.0F, 14);
                    hitNear(sl, 4.5D, dmg(9.0F), c);
                    sound(ModSounds.SLASH_LIGHT.get(), 1.2F);
                }
                if (moveTicks >= 20) {
                    end(50);
                }
            }
            case CENIT -> { // Sol Cautivo: orbe solar
                if (moveTicks == 11 && t != null) {
                    Vec3 to = t.position().add(0.0D, t.getBbHeight() * 0.5D, 0.0D).subtract(chest).normalize();
                    BoltEntity.fire(sl, this, chest.add(to.scale(1.5D)), to, 1.6D, dmg(14.0F), 3.0F, c, 2.6F);
                    sound(ModSounds.SOLAR_FLARE.get(), 1.4F);
                }
                if (moveTicks >= 20) {
                    end(60);
                }
            }
            case OCASO -> { // Siega 360
                if (moveTicks == 9) {
                    Vfx.ring(sl, chest, new Vec3(0, 1, 0), c, 3.6F, 10);
                    Vfx.slash(sl, chest, getYRot(), 0.0F, 0.0F, 3.6F, c, 10);
                    hitNear(sl, 3.8D, dmg(11.0F), c);
                    sound(ModSounds.SLASH_HEAVY.get(), 1.2F);
                }
                if (moveTicks >= 18) {
                    end(40);
                }
            }
            case MEDIANOCHE -> { // Pozo Estelar
                Vec3 at = t != null ? t.position().add(0.0D, 1.0D, 0.0D) : chest.add(look.scale(5.0D));
                if (moveTicks == 10) {
                    Vfx.distortion(sl, at, c, 3.0F, 50);
                    sound(ModSounds.STAR_FALL.get(), 1.0F);
                }
                if (moveTicks > 10 && moveTicks < 60) {
                    for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, Geo.box(at, 7.0D, 4.0D, 7.0D), this::isEnemy)) {
                        Vec3 pull = at.subtract(e.position()).normalize().scale(0.12D);
                        e.setDeltaMovement(e.getDeltaMovement().add(pull));
                        e.hurtMarked = true;
                    }
                }
                if (moveTicks == 60) {
                    Vfx.burst(sl, at, 0xFFFFFF, 30, 0.9F);
                    Vfx.ringExpanding(sl, at, new Vec3(0, 1, 0), c, 5.0F, 14);
                    for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, Geo.box(at, 4.0D, 3.0D, 4.0D), this::isEnemy)) {
                        hurtEnemy(e, dmg(14.0F), e.position().subtract(at).normalize(), 0.6D);
                    }
                    end(100);
                }
            }
            default -> { // Eclipse: Eco (giro + golpe que se repite)
                if (moveTicks == 9 || moveTicks == 29) {
                    Vfx.slash(sl, chest, getYRot() + (moveTicks == 29 ? 180.0F : 0.0F), 0.0F, 0.0F, 4.5F, c, 10);
                    hitNear(sl, 4.6D, dmg(10.0F), c);
                    sound(ModSounds.SLASH_LIGHT.get(), moveTicks == 29 ? 0.8F : 1.2F);
                }
                if (moveTicks >= 34) {
                    end(50);
                }
            }
        }
    }

    private void strikeArc(ServerLevel sl, double reach, float dmg, int color, float arcDeg) {
        Vec3 look = Vec3.directionFromRotation(0.0F, getYRot());
        Vfx.slash(sl, position().add(0.0D, 1.25D, 0.0D).add(look.scale(0.8D)), getYRot(), random.nextFloat() * 50.0F - 25.0F,
                random.nextFloat() * 90.0F - 45.0F, (float) reach, color, 7);
        double cos = Math.cos(Math.toRadians(arcDeg / 2.0D));
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, Geo.box(position(), reach + 1.0D, 3.0D, reach + 1.0D), this::isEnemy)) {
            Vec3 to = e.position().subtract(position());
            if (to.length() <= reach + e.getBbWidth() && flat(to).dot(look) >= cos) {
                hurtEnemy(e, dmg, look, 0.5D);
            }
        }
    }

    private void hitNear(ServerLevel sl, double r, float dmg, int color) {
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, Geo.box(position(), r, 2.5D, r), this::isEnemy)) {
            if (e.distanceTo(this) <= r) {
                hurtEnemy(e, dmg, flat(e.position().subtract(position())), 0.5D);
                Vfx.burst(sl, e.position().add(0.0D, 1.0D, 0.0D), color, 6, 0.4F);
            }
        }
    }

    private void hurtEnemy(LivingEntity e, float dmg, Vec3 dir, double push) {
        LooreDamage.apply(e, damageSources().mobAttack(this), dmg, dir, push);
    }

    private void sound(SoundEvent s, float vol) {
        level().playSound(null, getX(), getY(), getZ(), s, SoundSource.NEUTRAL, vol, 0.9F + random.nextFloat() * 0.2F);
    }

    private static Vec3 flat(Vec3 v) {
        Vec3 f = new Vec3(v.x, 0.0D, v.z);
        return f.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : f.normalize();
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Hour", hour());
        tag.putInt("Mode", getMode());
        if (home != null) {
            tag.put("Home", NbtUtils.writeBlockPos(home));
        }
        if (followId != null) {
            tag.putUUID("Follow", followId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(HOUR, Math.floorMod(tag.getInt("Hour"), 5));
        entityData.set(MODE, tag.getInt("Mode"));
        NbtUtils.readBlockPos(tag, "Home").ifPresent(this::setHome);
        if (tag.hasUUID("Follow")) {
            followId = tag.getUUID("Follow");
        }
    }
}
