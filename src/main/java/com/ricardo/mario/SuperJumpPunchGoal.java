package com.ricardo.mario;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Super Jump Punch de Mario (inspirado en Super Smash Bros.): se acerca a un monstruo cercano,
 * se agacha, salta con un golpe ascendente y suelta monedas en cada impacto. El ultimo golpe
 * lanza al monstruo con fuerza. Los tiempos estan en ticks (20 por segundo).
 */
public class SuperJumpPunchGoal extends Goal {

    private static final double APPROACH_RANGE = 9.0D;   // se acerca si el monstruo esta a <= 9 bloques
    private static final double STRIKE_RANGE = 2.6D;     // distancia horizontal para empezar el salto
    private static final int APPROACH_MAX_TICKS = 60;
    private static final int WINDUP_TICKS = 6;           // agachado (anticipacion)
    private static final int[] HIT_TICKS = {9, 11, 13};  // momentos en que el puno impacta
    private static final int FINAL_HIT = 2;              // el ultimo golpe lanza fuerte
    private static final int AIR_MAX_TICKS = 100;        // corte de seguridad
    private static final int RECOVER_TICKS = 6;          // recuperacion tras aterrizar
    private static final int COOLDOWN_TICKS = 100;       // 5 segundos entre usos
    private static final float LAUNCH_VY = 0.95F;        // ~5 bloques de altura
    private static final double LUNGE = 0.12D;           // empuje horizontal hacia el monstruo
    private static final int COINS_PER_HIT = 2;

    private enum Phase { APPROACH, JUMP, DONE }

    private final MarioEntity mario;
    private Phase phase = Phase.DONE;
    private int approachTicks;
    private int elapsed;
    private int nextHit;
    private boolean launched;
    private boolean landed;
    private int landedAt;
    private double forwardX;
    private double forwardZ;
    private Vec3 origin = Vec3.ZERO;
    private long nextAllowedTick;

    public SuperJumpPunchGoal(MarioEntity mario) {
        this.mario = mario;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mario.getTarget();
        if (target == null || !target.isAlive() || this.mario.isSuperJumpPunching()) {
            return false;
        }
        if (this.mario.tickCount < this.nextAllowedTick || !this.mario.onGround()) {
            return false;
        }
        if (this.mario.distanceToSqr(target) > APPROACH_RANGE * APPROACH_RANGE) {
            return false;
        }
        return this.mario.getSensing().hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        return this.phase != Phase.DONE && this.mario.isAlive();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.phase = Phase.APPROACH;
        this.approachTicks = 0;
        this.elapsed = 0;
        this.nextHit = 0;
        this.launched = false;
        this.landed = false;
        this.landedAt = -1;
    }

    @Override
    public void stop() {
        this.mario.setPunchTick(-1);
        this.mario.setLandedTick(-1);
        this.mario.getNavigation().stop();
        this.phase = Phase.DONE;
        this.nextAllowedTick = this.mario.tickCount + COOLDOWN_TICKS;
    }

    @Override
    public void tick() {
        if (this.phase == Phase.APPROACH) {
            this.tickApproach();
        } else if (this.phase == Phase.JUMP) {
            this.tickJump();
        }
    }

    /** Se acerca al monstruo hasta quedar a distancia de golpe. */
    private void tickApproach() {
        LivingEntity target = this.mario.getTarget();
        this.approachTicks++;
        if (target == null || !target.isAlive() || this.approachTicks > APPROACH_MAX_TICKS) {
            this.phase = Phase.DONE;
            return;
        }
        this.mario.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double dx = target.getX() - this.mario.getX();
        double dz = target.getZ() - this.mario.getZ();
        double dy = target.getY() - this.mario.getY();
        boolean inStrikeRange = dx * dx + dz * dz <= STRIKE_RANGE * STRIKE_RANGE && dy > -1.5D && dy < 4.0D;
        if (inStrikeRange && this.mario.onGround()) {
            this.startWindup();
        } else if (this.approachTicks % 5 == 1) {
            this.mario.getNavigation().moveTo(target, 1.3D);
        }
    }

    private void startWindup() {
        this.phase = Phase.JUMP;
        this.elapsed = 0;
        this.mario.getNavigation().stop();
        this.mario.getMoveControl().setWantedPosition(this.mario.getX(), this.mario.getY(), this.mario.getZ(), 0.0D);
        this.mario.setPunchTick(0);
    }

    /** Agachado, salto con golpes y aterrizaje. */
    private void tickJump() {
        this.elapsed++;
        LivingEntity target = this.mario.getTarget();
        if (target != null) {
            this.mario.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        if (!this.launched && this.elapsed >= WINDUP_TICKS) {
            this.launch(target);
        }
        if (this.launched) {
            if (this.nextHit < HIT_TICKS.length && this.elapsed >= HIT_TICKS[this.nextHit]) {
                this.punch(this.nextHit == FINAL_HIT);
                this.nextHit++;
            }
            if (!this.landed && this.elapsed > WINDUP_TICKS + 3 && this.mario.onGround()) {
                this.land();
            }
        }
        this.mario.setPunchTick(this.elapsed);
        boolean recovered = this.landed && this.elapsed - this.landedAt >= RECOVER_TICKS;
        if (recovered || this.elapsed >= AIR_MAX_TICKS) {
            this.phase = Phase.DONE;
        }
    }

    private void launch(LivingEntity target) {
        this.launched = true;
        this.origin = this.mario.position();
        double dx = 0.0D;
        double dz = 0.0D;
        if (target != null) {
            dx = target.getX() - this.mario.getX();
            dz = target.getZ() - this.mario.getZ();
        }
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 1.0E-4D) {
            double yaw = Math.toRadians(this.mario.getYRot());
            this.forwardX = -Math.sin(yaw);
            this.forwardZ = Math.cos(yaw);
        } else {
            this.forwardX = dx / len;
            this.forwardZ = dz / len;
        }
        this.mario.setDeltaMovement(this.forwardX * LUNGE, LAUNCH_VY, this.forwardZ * LUNGE);
        this.mario.hurtMarked = true;
        this.mario.playSound(SoundEvents.SLIME_JUMP, 1.0F, 1.6F);
    }

    /** Golpe ascendente: dana a los monstruos dentro de la columna que barre el puno. */
    private void punch(boolean finalHit) {
        if (!(this.mario.level() instanceof ServerLevel level)) {
            return;
        }
        double cx = this.origin.x + this.forwardX * 1.5D;
        double cz = this.origin.z + this.forwardZ * 1.5D;
        AABB reach = new AABB(cx - 1.5D, this.origin.y - 0.5D, cz - 1.5D, cx + 1.5D, this.origin.y + 6.0D, cz + 1.5D);
        float damage = (float) this.mario.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, reach, Monster::isAlive);

        for (Monster monster : monsters) {
            monster.hurt(this.mario.damageSources().mobAttack(this.mario), damage);
            double lift = finalHit ? 1.1D : 0.55D;
            double push = finalHit ? 0.45D : 0.0D;
            Vec3 motion = monster.getDeltaMovement();
            monster.setDeltaMovement(motion.x + this.forwardX * push, lift, motion.z + this.forwardZ * push);
            monster.hurtMarked = true;
            this.spawnCoins(level, monster);
        }
        if (!monsters.isEmpty()) {
            level.sendParticles(ParticleTypes.CRIT, cx, this.origin.y + 2.0D, cz, 8, 0.4D, 0.4D, 0.4D, 0.1D);
            this.mario.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, finalHit ? 0.8F : 1.1F);
        }
    }

    /** Las monedas salen del monstruo golpeado: vuelan en arco, giran y no dan dano. */
    private void spawnCoins(ServerLevel level, LivingEntity hit) {
        double x = hit.getX();
        double y = hit.getY(0.6D);
        double z = hit.getZ();
        for (int i = 0; i < COINS_PER_HIT; i++) {
            MarioCoinEntity coin = MarioMod.COIN_ENTITY.get().create(level);
            if (coin == null) {
                continue;
            }
            double angle = this.mario.getRandom().nextDouble() * Math.PI * 2.0D;
            double speed = 0.10D + this.mario.getRandom().nextDouble() * 0.08D;
            double up = 0.30D + this.mario.getRandom().nextDouble() * 0.12D;
            coin.setPos(x, y, z);
            coin.setDeltaMovement(Math.cos(angle) * speed, up, Math.sin(angle) * speed);
            level.addFreshEntity(coin);
        }
        level.playSound(null, x, y, z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.HOSTILE, 0.6F, 1.7F);
    }

    private void land() {
        this.landed = true;
        this.landedAt = this.elapsed;
        this.mario.setLandedTick(this.elapsed);
        if (this.mario.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CLOUD, this.mario.getX(), this.mario.getY(), this.mario.getZ(), 12, 0.5D, 0.05D, 0.5D, 0.02D);
        }
        this.mario.playSound(SoundEvents.PLAYER_BIG_FALL, 0.6F, 1.2F);
    }
}
