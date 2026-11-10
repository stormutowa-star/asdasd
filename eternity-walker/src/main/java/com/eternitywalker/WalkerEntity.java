package com.eternitywalker;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Eternity Walker. Marco local: 1 unidad del modelo = 0.5 bloques, el frente del modelo es -X.
 * Con yaw de Minecraft = 90 el modelo queda sin rotar (mira al oeste).
 */
public class WalkerEntity extends Entity {
    public static final double S = 0.5;

    // silla del piloto (unidades del modelo)
    private static final double SEAT_X = -31.0 * S;
    private static final double SEAT_FLOOR_Y = 28.5 * S;
    private static final double SEAT_FEET_Y = 14.3;

    public static final EntityDataAccessor<Float> TURRET_YAW = SynchedEntityData.defineId(WalkerEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> TURRET_PITCH = SynchedEntityData.defineId(WalkerEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> SIDE_YAW = SynchedEntityData.defineId(WalkerEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> SIDE_PITCH = SynchedEntityData.defineId(WalkerEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Integer> SHOT_MAIN = SynchedEntityData.defineId(WalkerEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> SHOT_L = SynchedEntityData.defineId(WalkerEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> SHOT_R = SynchedEntityData.defineId(WalkerEntity.class, EntityDataSerializers.INT);

    // animacion (cliente y servidor)
    public float walkPhase, walkPhaseO, moveAmt, moveAmtO;

    // controles (servidor)
    public boolean fireMain, fireSide;
    public int lastControlTick;
    private int mainCd, sideCd, seatCooldown;

    // interpolacion de posicion (cliente)
    private double lx, ly, lz;
    private float lyr;
    private int lerpSteps;

    private static final int[][] FEET = {{-21, -10}, {-21, 10}, {0, -10}, {0, 10}, {21, -10}, {21, 10}};

    public WalkerEntity(EntityType<? extends WalkerEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TURRET_YAW, 0f);
        builder.define(TURRET_PITCH, 0f);
        builder.define(SIDE_YAW, 0f);
        builder.define(SIDE_PITCH, 0f);
        builder.define(SHOT_MAIN, -1000);
        builder.define(SHOT_L, -1000);
        builder.define(SHOT_R, -1000);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    // ---------------------------------------------------------------- transformaciones

    /** local (bloques, marco del modelo) -> mundo */
    public Vec3 toWorld(double x, double y, double z) {
        double a = Math.toRadians(getYRot() - 90.0);
        double c = Math.cos(a), s = Math.sin(a);
        return new Vec3(getX() + x * c - z * s, getY() + y, getZ() + x * s + z * c);
    }

    /** mundo -> local (bloques, marco del modelo) */
    public Vec3 toLocal(Vec3 w) {
        double a = Math.toRadians(getYRot() - 90.0);
        double c = Math.cos(a), s = Math.sin(a);
        double dx = w.x - getX(), dz = w.z - getZ();
        return new Vec3(dx * c + dz * s, w.y - getY(), -dx * s + dz * c);
    }

    private Vec3 rotDir(double x, double y, double z) {
        double a = Math.toRadians(getYRot() - 90.0);
        double c = Math.cos(a), s = Math.sin(a);
        return new Vec3(x * c - z * s, y, x * s + z * c);
    }

    // ---------------------------------------------------------------- reglas de entidad

    @Override public boolean isPickable() { return false; }
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean isInvulnerableTo(DamageSource source) { return true; }
    @Override public boolean isControlledByLocalInstance() { return false; }

    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(getX() - 27, getY() - 1, getZ() - 27, getX() + 27, getY() + 32, getZ() + 27);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity e = getFirstPassenger();
        return e instanceof LivingEntity le ? le : null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty();
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction cb) {
        if (!hasPassenger(passenger)) return;
        Vec3 p = toWorld(SEAT_X, SEAT_FEET_Y, 0);
        cb.accept(passenger, p.x, p.y, p.z);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return toWorld(-27.5 * S, 14.55, 0);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        seatCooldown = 30;
        fireMain = false;
        fireSide = false;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lx = x;
        this.ly = y;
        this.lz = z;
        this.lyr = yRot;
        this.lerpSteps = Math.max(steps, 1);
    }

    // ---------------------------------------------------------------- tick

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) clientTick();
        else serverTick();
        updateAnim();
    }

    private void clientTick() {
        if (lerpSteps > 0) {
            double k = 1.0 / lerpSteps;
            setPos(getX() + (lx - getX()) * k, getY() + (ly - getY()) * k, getZ() + (lz - getZ()) * k);
            setYRot(getYRot() + Mth.wrapDegrees(lyr - getYRot()) * (float) k);
            lerpSteps--;
        }
    }

    private void updateAnim() {
        double dx = getX() - xo, dz = getZ() - zo;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);
        walkPhaseO = walkPhase;
        moveAmtO = moveAmt;
        moveAmt += (Mth.clamp(dist * 6.0f, 0.0f, 1.0f) - moveAmt) * 0.25f;
        walkPhase += dist / 6.0f;
    }

    private static float approachAngle(float cur, float tgt, float max) {
        return cur + Mth.clamp(Mth.wrapDegrees(tgt - cur), -max, max);
    }

    private void serverTick() {
        LivingEntity ctrl = getControllingPassenger();

        if (tickCount - lastControlTick > 5) {
            fireMain = false;
            fireSide = false;
        }

        float tgtYaw = 0, tgtPitch = 0;
        if (ctrl instanceof Player p) {
            float turn = p.xxa, fwd = p.zza;
            if (Math.abs(turn) > 0.05f) setYRot(getYRot() - Math.signum(turn) * 2.2f);
            if (Math.abs(fwd) > 0.05f) {
                double sp = fwd > 0 ? 0.30 : -0.14;
                double yr = Math.toRadians(getYRot());
                setPos(getX() - Math.sin(yr) * sp, getY(), getZ() + Math.cos(yr) * sp);
            }
            tgtYaw = Mth.wrapDegrees(getYRot() - p.getYRot());
            tgtPitch = Mth.clamp(p.getXRot(), -50f, 8f);
        }

        // el casco sigue el terreno (se apoya en el punto mas alto bajo sus seis patas)
        double target = Double.NEGATIVE_INFINITY;
        for (int[] f : FEET) {
            Vec3 w = toWorld(f[0], 0, f[1]);
            BlockPos bp = BlockPos.containing(w.x, 64, w.z);
            if (!level().hasChunkAt(bp)) continue;
            int h = level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bp.getX(), bp.getZ());
            target = Math.max(target, h);
        }
        if (target > -1.0e8) {
            double dy = Mth.clamp(target - getY(), -0.5, 0.35);
            if (Math.abs(dy) > 1.0e-4) setPos(getX(), getY() + dy, getZ());
        }

        // apuntado de las torretas
        entityData.set(TURRET_YAW, approachAngle(entityData.get(TURRET_YAW), tgtYaw, 12f));
        entityData.set(TURRET_PITCH, approachAngle(entityData.get(TURRET_PITCH), tgtPitch, 8f));
        entityData.set(SIDE_YAW, approachAngle(entityData.get(SIDE_YAW), Mth.clamp(tgtYaw, -25f, 25f), 12f));
        entityData.set(SIDE_PITCH, approachAngle(entityData.get(SIDE_PITCH), Mth.clamp(tgtPitch, -20f, 5f), 8f));

        // disparo
        if (mainCd > 0) mainCd--;
        if (sideCd > 0) sideCd--;
        if (ctrl instanceof Player p) {
            if (fireMain && mainCd == 0) {
                fire(p, 0);
                mainCd = 26;
            }
            if (fireSide && sideCd == 0) {
                fire(p, 1);
                fire(p, 2);
                sideCd = 7;
            }
        }

        // sentarse en la silla del piloto: agacharse junto a ella
        if (seatCooldown > 0) {
            seatCooldown--;
        } else if (ctrl == null) {
            AABB box = new AABB(getX() - 30, getY(), getZ() - 30, getX() + 30, getY() + 25, getZ() + 30);
            for (Player p : level().getEntitiesOfClass(Player.class, box)) {
                if (!p.isShiftKeyDown() || p.isPassenger() || p.isSpectator()) continue;
                Vec3 l = toLocal(p.position());
                if (Math.hypot(l.x - SEAT_X, l.z) < 0.9 && Math.abs(l.y - SEAT_FLOOR_Y) < 1.3) {
                    p.startRiding(this);
                    break;
                }
            }
        }
    }

    // ---------------------------------------------------------------- disparo

    private static double[] rotZ(double[] p, double[] c, double deg) {
        double a = Math.toRadians(deg), cs = Math.cos(a), sn = Math.sin(a);
        double x = p[0] - c[0], y = p[1] - c[1];
        return new double[]{c[0] + x * cs - y * sn, c[1] + x * sn + y * cs, p[2]};
    }

    private static double[] rotY(double[] p, double[] c, double deg) {
        double a = Math.toRadians(deg), cs = Math.cos(a), sn = Math.sin(a);
        double x = p[0] - c[0], z = p[2] - c[2];
        return new double[]{c[0] + x * cs + z * sn, p[1], c[2] - x * sn + z * cs};
    }

    /** which: 0 = cañón central, 1 = lateral izquierdo (+z), 2 = lateral derecho (-z) */
    private void fire(Player shooter, int which) {
        double[] t, c, m;
        float ry, rz;
        if (which == 0) {
            t = new double[]{10.25, 43, 0};
            c = new double[]{-2.5, 49.8, 0};
            m = new double[]{-23.3, 49.95, 0};
            ry = entityData.get(TURRET_YAW);
            rz = entityData.get(TURRET_PITCH);
        } else {
            double z = which == 1 ? 12.25 : -12.25;
            t = new double[]{27, 43.2, z};
            c = new double[]{22, 43.2, z};
            m = new double[]{9.3, 43.2, z};
            ry = entityData.get(SIDE_YAW);
            rz = entityData.get(SIDE_PITCH);
        }
        double[] origin = {0, 0, 0};
        double[] mp = rotY(rotZ(m, c, rz), t, ry);
        double[] dv = rotY(rotZ(new double[]{-1, 0, 0}, origin, rz), origin, ry);
        Vec3 pos = toWorld(mp[0] * S, mp[1] * S, mp[2] * S);
        Vec3 dir = rotDir(dv[0], dv[1], dv[2]).normalize();

        if (which == 0) {
            LargeFireball fb = new LargeFireball(level(), shooter, dir, 3);
            fb.setPos(pos.x, pos.y, pos.z);
            fb.setDeltaMovement(dir.scale(2.2));
            level().addFreshEntity(fb);
            level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.NEUTRAL, 4.0f, 0.5f);
            entityData.set(SHOT_MAIN, tickCount);
        } else {
            SmallFireball sf = new SmallFireball(level(), shooter, dir);
            sf.setPos(pos.x, pos.y, pos.z);
            sf.setDeltaMovement(dir.scale(1.8));
            level().addFreshEntity(sf);
            level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL, 2.0f, 0.8f);
            entityData.set(which == 1 ? SHOT_L : SHOT_R, tickCount);
        }
    }
}
