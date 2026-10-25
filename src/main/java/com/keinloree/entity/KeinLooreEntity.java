package com.keinloree.entity;

import com.keinloree.registry.ModEntities;
import com.keinloree.registry.ModItems;
import com.keinloree.util.Fx;
import com.keinloree.util.Geo;
import com.keinloree.util.LooreDamage;
import com.keinloree.world.LooreStory;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Kein Loree. Forma base: sin alas; crea copias de si mismo que emergen del suelo, forma un campo
 * digital circular y dispara rayos verdes desde cualquier punto del campo, y tiene el Green Byte.
 * Forma final (Techno Winged Code): vuela, decodifica un punto y se teletransporta o dispara desde
 * alli, sus rayos se curvan y su Codeux Order abre un surco de terreno.
 * Las copias usan esta misma clase con un tiempo de vida (copyLife).
 */
public class KeinLooreEntity extends LooreMob {

    static final int AB_NONE = 0;
    static final int AB_FIELD = 1;
    static final int AB_GREEN_BYTE = 2;
    static final int AB_COPY = 3;
    static final int AB_CHANGE = 4;
    static final int AB_DECODE = 5;
    static final int AB_CODEUX = 6;
    static final int AB_TALK = 7;
    static final int AB_DESCEND = 8;

    private static final double SIGHT = 96.0D;
    private static final int COPY_LIFE = 600;
    private static final int GB_COLLAPSE = 40;
    private static final int GB_IMPACT = 44;
    private static final int GB_END = 60;

    private final ServerBossEvent bossBar = new ServerBossEvent(Component.literal("Kein Loree"),
            BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
    private final Set<Integer> hitIds = new HashSet<>();

    /** -1 = Kein Loree principal; 0 o mas = copia con esa cantidad de ticks de vida. */
    private int copyLife = -1;
    private int ability = AB_NONE;
    private int abTick;
    private int abCooldown = 100;
    private boolean techno;
    private boolean talkUsed;
    private boolean codeuxUsed;
    private boolean choiceOpen;
    private int choiceTicks;
    private boolean fieldOn;
    private Vec3 fieldCenter = Vec3.ZERO;
    private double fieldRadius = 10.0D;
    private Vec3 gbStart = Vec3.ZERO;
    private double gbStartRadius = 6.0D;
    private Vec3 gbGoal = Vec3.ZERO;
    private Vec3 decodePoint = Vec3.ZERO;
    private Vec3 descendGoal = Vec3.ZERO;

    public KeinLooreEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 96.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    public boolean isCopy() {
        return this.copyLife >= 0;
    }

    /** Convierte esta entidad en una copia que vive `life` ticks y tiene `health` de vida. */
    public void makeCopy(int life, double health) {
        this.copyLife = life;
        AttributeInstance max = this.getAttribute(Attributes.MAX_HEALTH);
        if (max != null) {
            max.setBaseValue(health);
        }
        this.setHealth((float) health);
        this.setSpawnTicks(60);
    }

    /** Desde el trono de la nave, desciende hasta su base (la llama la historia). */
    public void beginDescent(Vec3 landing) {
        this.descendGoal = landing;
        this.setLoreDormant(false);
        this.abCooldown = 0;
        this.startAbility(AB_DESCEND);
    }

    /** Se queda inmovil en su trono (final de consumo del mundo). */
    public void retreatToThrone(Vec3 throne) {
        this.ability = AB_NONE;
        this.choiceOpen = false;
        this.setNoGravity(true);
        this.moveTo(throne.x, throne.y, throne.z, this.getYRot(), 0.0F);
        this.setDeltaMovement(Vec3.ZERO);
        this.setLoreDormant(true);
    }

    /** Cierra la oferta de Kein Loree (la acepto el jugador o expiro). */
    public void closeOffer() {
        this.choiceOpen = false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (this.choiceOpen && !this.isCopy() && held.is(ModItems.DECODE_CORE.get())
                && this.level() instanceof ServerLevel level) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            LooreStory.onOfferAccepted(level, this, player);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isLoreDormant() || this.getSpawnTicks() > 0 || this.ability == AB_CHANGE || this.ability == AB_DESCEND) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (!this.isCopy()) {
            this.bossBar.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel level) {
            if (this.isCopy()) {
                this.copyAi(level);
            } else {
                this.bossAi(level);
            }
        }
        super.aiStep();
    }

    // ------------------------------------------------------------------ IA general

    private void copyAi(ServerLevel level) {
        if (this.getSpawnTicks() > 0) {
            this.riseFx(level);
            return;
        }
        this.copyLife--;
        if (this.copyLife <= 0) {
            Fx.burst(level, Fx.CODE, this.position().add(0.0D, 1.0D, 0.0D), 40, 0.8D, 0.25D);
            Fx.sound(level, this.position(), SoundEvents.ENDERMAN_TELEPORT, 0.8F, 0.6F);
            this.discard();
            return;
        }
        LivingEntity t = this.findTarget();
        this.setTarget(t);
        if (t == null) {
            return;
        }
        this.getLookControl().setLookAt(t, 30.0F, 30.0F);
        double d2 = this.distanceToSqr(t);
        if (d2 > 9.0D) {
            if (this.tickCount % 10 == 0) {
                this.getNavigation().moveTo(t, 1.2D);
            }
        } else if (this.tickCount % 22 == 0) {
            this.strike(t, 9.0F, 0.6D);
        }
        if (this.tickCount % 36 == 0 && d2 < 900.0D) {
            this.laser(level, this.getEyePosition(), t.getEyePosition(), 4.0F, Fx.GREEN);
        }
    }

    /** Emergencia desde el suelo: una banda de brillo tipo Matrix sube mientras el modelo se forma. */
    private void riseFx(ServerLevel level) {
        int left = Math.max(0, this.getSpawnTicks() - 1);
        this.setSpawnTicks(left);
        double h = (1.0D - left / 60.0D) * 2.4D;
        Vec3 band = new Vec3(this.getX(), this.getY() + h, this.getZ());
        Fx.ring(level, band, 0.9D, 20, Fx.BRIGHT, this.tickCount * 0.4D);
        Fx.ring(level, band.add(0.0D, -0.5D, 0.0D), 0.7D, 12, Fx.CODE, this.tickCount * 0.7D);
        Fx.burst(level, Fx.CODE, band, 4, 0.6D, 0.05D);
        if (left == 0) {
            Fx.burst(level, Fx.BRIGHT, this.position().add(0.0D, 1.2D, 0.0D), 40, 0.9D, 0.3D);
            Fx.sound(level, this.position(), SoundEvents.ENDERMAN_TELEPORT, 0.6F, 1.4F);
        }
    }

    private void bossAi(ServerLevel level) {
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.isLoreDormant()) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        LivingEntity target = this.findTarget();
        this.setTarget(target);

        if (this.choiceOpen) {
            this.choiceTicks--;
            if (this.choiceTicks <= 0) {
                this.choiceOpen = false;
                LooreStory.say(level, "Elegiste resistir. Entonces que la resistencia te consuma.");
            }
        }
        if (this.ability != AB_NONE) {
            this.runAbility(level, target);
            return;
        }
        if (target == null) {
            return;
        }
        if (!this.talkUsed && this.getHealth() <= this.getMaxHealth() * 0.6F) {
            this.talkUsed = true;
            this.startAbility(AB_TALK);
            return;
        }
        if (!this.techno && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            this.startAbility(AB_CHANGE);
            return;
        }
        if (this.techno && !this.codeuxUsed && this.getHealth() <= this.getMaxHealth() * 0.3F) {
            this.codeuxUsed = true;
            this.startAbility(AB_CODEUX);
            return;
        }
        if (this.abCooldown > 0) {
            this.abCooldown--;
            this.basicAi(level, target);
            return;
        }
        this.pickAbility(level);
    }

    private void pickAbility(ServerLevel level) {
        int roll = this.random.nextInt(3);
        if (this.techno) {
            this.startAbility(roll == 0 ? AB_GREEN_BYTE : AB_DECODE);
            return;
        }
        boolean needCopies = this.getHealth() < this.getMaxHealth() * 0.8F && this.countCopies(level) < 3;
        if (needCopies && roll == 0) {
            this.startAbility(AB_COPY);
        } else if (roll == 1) {
            this.startAbility(AB_FIELD);
        } else {
            this.startAbility(AB_GREEN_BYTE);
        }
    }

    /** Comportamiento normal entre habilidades. */
    private void basicAi(ServerLevel level, LivingEntity target) {
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double d2 = this.distanceToSqr(target);
        if (this.techno) {
            this.setLorePose(POSE_FLY);
            double a = this.tickCount * 0.03D;
            this.flyToward(target.position().add(Math.cos(a) * 9.0D, 7.0D, Math.sin(a) * 9.0D), 0.55D);
        } else if (d2 > 9.0D) {
            if (this.tickCount % 10 == 0) {
                this.getNavigation().moveTo(target, 1.1D);
            }
        } else if (this.tickCount % 20 == 0) {
            this.strike(target, 16.0F, 1.2D);
        }
        if (this.tickCount % 40 == 0 && d2 < 2304.0D) {
            this.laser(level, this.getEyePosition(), target.getEyePosition(), 6.0F, Fx.GREEN);
        }
    }

    // ------------------------------------------------------------------ habilidades

    private void startAbility(int id) {
        this.ability = id;
        this.abTick = 0;
        this.hitIds.clear();
    }

    private void endAbility(int cooldown) {
        this.ability = AB_NONE;
        this.abTick = 0;
        this.abCooldown = cooldown;
        this.fieldOn = false;
        this.hitIds.clear();
        this.noPhysics = false;
        this.setNoGravity(this.techno);
        this.setLorePose(this.techno ? POSE_FLY : POSE_IDLE);
    }

    private void runAbility(ServerLevel level, LivingEntity target) {
        this.abTick++;
        if (target == null && (this.ability == AB_GREEN_BYTE || this.ability == AB_DECODE
                || this.ability == AB_CODEUX)) {
            this.endAbility(60);
            return;
        }
        switch (this.ability) {
            case AB_FIELD -> this.abField(level);
            case AB_GREEN_BYTE -> this.abGreenByte(level, target);
            case AB_COPY -> this.abCopy(level);
            case AB_CHANGE -> this.abChange(level);
            case AB_DECODE -> this.abDecode(level, target);
            case AB_CODEUX -> this.abCodeux(level, target);
            case AB_TALK -> this.abTalk(level);
            case AB_DESCEND -> this.abDescend(level);
            default -> this.endAbility(60);
        }
    }

    /** Campo digital: circulo de 10 bloques; se teletransporta dentro y dispara rayos a cualquier punto. */
    private void abField(ServerLevel level) {
        if (this.abTick == 1) {
            this.fieldOn = true;
            this.fieldCenter = this.groundBelow(level, this.position());
            this.fieldRadius = 10.0D;
            this.setLorePose(POSE_CAST);
            LooreStory.say(level, "Campo digital activo. Aquí no hay a dónde huir.");
            Fx.sound(level, this.position(), SoundEvents.BEACON_ACTIVATE, 1.0F, 1.5F);
        }
        if (this.abTick % 2 == 0) {
            Fx.ring(level, this.fieldCenter.add(0.0D, 0.15D, 0.0D), this.fieldRadius, 72, Fx.GREEN,
                    this.abTick * 0.04D);
        }
        if (this.abTick > 1 && this.abTick % 60 == 1) {
            Vec3 spot = this.randomFieldPoint(level);
            this.blinkTo(level, spot.x, spot.y, spot.z);
        }
        if (this.abTick % 20 == 0) {
            Vec3 from = this.getEyePosition();
            for (Player p : this.playersInField(level)) {
                this.laser(level, from, p.getEyePosition(), 4.0F, Fx.GREEN);
            }
            Vec3 any = this.randomFieldPoint(level).add(0.0D, 1.0D, 0.0D);
            this.laser(level, from, any, 3.0F, Fx.GREEN);
        }
        if (this.abTick >= 400) {
            this.endAbility(160);
        }
    }

    /** Green Byte: el campo se cierra sobre el objetivo y de su centro sube un rayo de codigo al cielo. */
    private void abGreenByte(ServerLevel level, LivingEntity target) {
        int t = this.abTick;
        if (t == 1) {
            this.setLorePose(POSE_CAST);
            this.gbStart = this.fieldOn ? this.fieldCenter : this.groundBelow(level, this.position());
            this.gbStartRadius = this.fieldOn ? this.fieldRadius : 6.0D;
            this.gbGoal = this.groundBelow(level, target.position());
            LooreStory.say(level, "Green Byte. El código te encontró.");
            Fx.sound(level, this.gbStart, SoundEvents.BEACON_ACTIVATE, 1.0F, 0.8F);
        }
        if (t <= GB_COLLAPSE) {
            double p = t / (double) GB_COLLAPSE;
            Vec3 center = this.gbStart.add(this.gbGoal.subtract(this.gbStart).scale(p));
            double radius = Mth.lerp(p, this.gbStartRadius, 1.5D);
            this.fieldCenter = center;
            this.fieldRadius = radius;
            this.fieldOn = true;
            Fx.ring(level, center.add(0.0D, 0.15D, 0.0D), radius, 48, Fx.GREEN, t * 0.2D);
            if (t % 4 == 0) {
                Fx.ring(level, center.add(0.0D, 0.3D, 0.0D), radius * 0.5D, 24, Fx.CODE, t * 0.3D);
            }
        } else if (t < GB_END) {
            Vec3 c = this.gbGoal;
            this.fieldOn = false;
            this.columnFx(level, c, 46.0D, 1.6D);
            if (t == GB_IMPACT) {
                Fx.burst(level, Fx.BRIGHT, c.add(0.0D, 0.5D, 0.0D), 60, 1.6D, 0.3D);
                Fx.sound(level, c, SoundEvents.BEACON_POWER_SELECT, 1.0F, 0.6F);
                List<LivingEntity> hit = level.getEntitiesOfClass(LivingEntity.class,
                        Geo.box(c.add(0.0D, 2.0D, 0.0D), 2.6D, 3.5D, 2.6D),
                        x -> x != this && x.isAlive() && !(x instanceof LooreMob));
                for (LivingEntity e : hit) {
                    LooreDamage.apply(e, this.damageSources().mobAttack(this), 18.0F, flat(c, e.position()), 0.9D);
                }
            }
        } else {
            this.endAbility(200);
        }
    }

    /** Copias: dos Kein Loree que emergen del suelo alrededor de el. */
    private void abCopy(ServerLevel level) {
        if (this.abTick == 1) {
            this.setLorePose(POSE_CAST);
            LooreStory.say(level, "¡Levántense, mis copias!");
            for (int i = 0; i < 2; i++) {
                this.summonCopy(level, this.random.nextDouble() * Math.PI * 2.0D);
            }
        }
        if (this.abTick >= 50) {
            this.endAbility(240);
        }
    }

    private void summonCopy(ServerLevel level, double angle) {
        KeinLooreEntity copy = ModEntities.KEIN_LOREE.get().create(level);
        if (copy == null) {
            return;
        }
        double x = this.getX() + Math.cos(angle) * 6.0D;
        double z = this.getZ() + Math.sin(angle) * 6.0D;
        double y = Fx.groundY(level, x, z);
        copy.moveTo(x, y, z, this.getYRot(), 0.0F);
        copy.makeCopy(COPY_LIFE, 150.0D);
        level.addFreshEntity(copy);
    }

    /** Cambio a la forma final: las alas se despliegan y el codigo cubre el cielo. */
    private void abChange(ServerLevel level) {
        Vec3 c = this.position().add(0.0D, 1.2D, 0.0D);
        if (this.abTick == 1) {
            this.setLorePose(POSE_SPREAD);
            LooreStory.title(level, "FORMA FINAL", "Techno Winged Code");
            LooreStory.say(level, "Has llegado más lejos que nadie. Ahora verás mi verdadera forma.");
            Fx.sound(level, c, SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.2F);
        }
        this.setWingOpen(Mth.clamp(this.abTick / 60.0F, 0.0F, 1.0F));
        if (this.abTick % 3 == 0) {
            Fx.ring(level, c, 2.2D, 32, Fx.BRIGHT, this.abTick * 0.1D);
        }
        if (this.abTick == 60) {
            this.techno = true;
            this.setLoreForm(1);
            this.setNoGravity(true);
            Fx.burst(level, Fx.BRIGHT, c, 90, 1.5D, 0.4D);
            LooreStory.say(level, "Cada copia que destruiste era un fragmento de tu propio núcleo. Tú también eres código, Decodificador.");
        }
        if (this.abTick >= 110) {
            this.setWingOpen(1.0F);
            this.endAbility(60);
        }
    }

    /** Techno Winged Code: decodifica el punto donde esta el objetivo y se teletransporta alli o dispara desde alli. */
    private void abDecode(ServerLevel level, LivingEntity target) {
        if (this.abTick == 1) {
            double a = this.random.nextDouble() * Math.PI * 2.0D;
            double r = 5.0D + this.random.nextDouble() * 5.0D;
            double x = target.getX() + Math.cos(a) * r;
            double z = target.getZ() + Math.sin(a) * r;
            this.decodePoint = new Vec3(x, Fx.groundY(level, x, z), z);
            this.setLorePose(POSE_CHARGE);
            LooreStory.say(level, "Te miro... y te decodifico.");
        }
        if (this.abTick < 26) {
            Vec3 p = this.decodePoint.add(0.0D, 0.1D, 0.0D);
            Fx.ring(level, p, 1.2D, 16, Fx.CODE, this.abTick * 0.5D);
            Fx.ring(level, p.add(0.0D, 2.0D, 0.0D), 0.6D, 10, Fx.BRIGHT, this.abTick * 0.3D);
        }
        if (this.abTick == 26) {
            if (this.random.nextBoolean()) {
                Vec3 p = this.decodePoint.add(0.0D, 3.0D, 0.0D);
                this.blinkTo(level, p.x, p.y, p.z);
            } else {
                Vec3 from = this.decodePoint.add(0.0D, 2.5D, 0.0D);
                Vec3 to = target.getEyePosition();
                this.curvedBeam(level, from, to, 5.0F);
                this.curvedBeam(level, from, to.add(1.5D, 0.0D, -1.5D), 5.0F);
            }
        }
        if (this.abTick >= 45) {
            this.endAbility(70 + this.random.nextInt(40));
        }
    }

    /** Codeux Order: sube, junta los brazos, carga energia y lanza una onda verde que arrasa el terreno. */
    private void abCodeux(ServerLevel level, LivingEntity target) {
        int t = this.abTick;
        if (t == 1) {
            this.setLorePose(POSE_FLY);
            LooreStory.title(level, "CODEUX ORDER", "Orden de decodificación total");
            LooreStory.say(level, "Codeux Order. Ni la tierra quedará en pie.");
            Fx.sound(level, this.position(), SoundEvents.ENDER_DRAGON_GROWL, 1.2F, 0.7F);
        }
        if (t <= 40) {
            double gy = Fx.groundY(level, target.getX(), target.getZ());
            this.flyToward(new Vec3(target.getX(), gy + 35.0D, target.getZ()), 1.2D);
            this.setWingOpen(1.0F);
        } else if (t <= 90) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setLorePose(POSE_CHARGE);
            Vec3 core = this.position().add(0.0D, 0.6D, 0.0D);
            double k = (t - 40) / 50.0D;
            double radius = 3.0D * (1.0D - k) + 0.2D;
            Fx.ring(level, core, radius, 20, Fx.BRIGHT, t * 0.3D);
            Fx.burst(level, Fx.CODE, core, 6, radius * 0.4D, 0.1D);
            if (t % 10 == 0) {
                Fx.sound(level, core, SoundEvents.BEACON_POWER_SELECT, 0.8F, (float) (0.5D + k));
            }
        } else if (t <= 150) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setLorePose(POSE_FIRE);
            Vec3 origin = this.groundBelow(level, this.position());
            Vec3 aimDir = flat(origin, this.groundBelow(level, target.position()));
            int w = t - 90;
            Vec3 front = origin.add(aimDir.scale(w));
            Fx.carve(level, front, 3.2D, 3, 8);
            if (t % 2 == 0) {
                Fx.line(level, this.position().add(0.0D, 1.5D, 0.0D), front.add(0.0D, 1.0D, 0.0D), Fx.BRIGHT, 1.0D);
            }
            Fx.ring(level, front.add(0.0D, 0.2D, 0.0D), 3.5D, 32, Fx.GREEN, t * 0.5D);
            List<LivingEntity> hit = level.getEntitiesOfClass(LivingEntity.class,
                    Geo.box(front.add(0.0D, 2.0D, 0.0D), 4.5D, 4.0D, 4.5D),
                    x -> x != this && x.isAlive() && !(x instanceof LooreMob));
            for (LivingEntity e : hit) {
                if (this.hitIds.add(e.getId())) {
                    LooreDamage.apply(e, this.damageSources().mobAttack(this), 20.0F, aimDir, 1.6D);
                }
            }
        } else {
            this.endAbility(900);
        }
    }

    /** Descenso desde el trono hasta la base (la llama la historia). */
    private void abDescend(ServerLevel level) {
        if (this.abTick == 1) {
            this.setNoGravity(true);
            this.noPhysics = true;
            this.setLorePose(POSE_FLY);
        }
        this.flyToward(this.descendGoal, 0.7D);
        if (this.abTick % 4 == 0) {
            Fx.ring(level, this.position(), 1.5D, 14, Fx.CODE, this.abTick * 0.2D);
        }
        if (this.position().distanceToSqr(this.descendGoal) < 4.0D || this.abTick > 260) {
            this.endAbility(40);
        }
    }

    private void abTalk(ServerLevel level) {
        if (this.abTick == 1) {
            this.setLorePose(POSE_SPREAD);
            LooreStory.say(level, "Ya viste lo que puedo hacer. Puedo ofrecerte un lugar a mi lado... o seguir siendo un obstáculo.");
            LooreStory.say(level, "Si aceptas, dame tu Núcleo de Decodificación con clic derecho en los próximos 40 segundos.");
        }
        if (this.abTick >= 60) {
            this.choiceOpen = true;
            this.choiceTicks = 800;
            this.endAbility(100);
        }
    }

    // ------------------------------------------------------------------ utilidades

    /** Rayo que sube (columna de codigo) desde la base. */
    private void columnFx(ServerLevel level, Vec3 base, double height, double radius) {
        for (int i = 0; i < 8; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0D;
            double r = this.random.nextDouble() * radius;
            double y = base.y + this.random.nextDouble() * height;
            ParticleOptions p = this.random.nextBoolean() ? Fx.CODE : ParticleTypes.END_ROD;
            level.sendParticles(p, base.x + Math.cos(a) * r, y, base.z + Math.sin(a) * r, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    /** Curva de Bezier que se dobla (estilo Darkseid); daña a los jugadores por donde pasa. */
    private void curvedBeam(ServerLevel level, Vec3 a, Vec3 b, float damage) {
        Vec3 mid = a.add(b).scale(0.5D);
        double bend = this.random.nextBoolean() ? 4.0D : -4.0D;
        Vec3 control = mid.add(Geo.side(b.subtract(a)).scale(bend)).add(0.0D, 3.0D, 0.0D);
        Fx.curve(level, a, control, b, Fx.GREEN, 28);
        Set<LivingEntity> seen = new HashSet<>();
        for (int i = 1; i < 28; i++) {
            double t = i / 28.0D;
            double u = 1.0D - t;
            Vec3 p = a.scale(u * u).add(control.scale(2.0D * u * t)).add(b.scale(t * t));
            for (Player pl : level.getEntitiesOfClass(Player.class, Geo.box(p, 0.8D, 0.8D, 0.8D), Player::isAlive)) {
                if (seen.add(pl)) {
                    this.strike(pl, damage, 0.4D);
                }
            }
        }
    }

    private void blinkTo(ServerLevel level, double x, double y, double z) {
        Fx.burst(level, Fx.CODE, this.position().add(0.0D, 1.2D, 0.0D), 30, 0.5D, 0.2D);
        this.moveTo(x, y, z, this.getYRot(), 0.0F);
        this.setDeltaMovement(Vec3.ZERO);
        Vec3 now = this.position().add(0.0D, 1.2D, 0.0D);
        Fx.burst(level, Fx.CODE, now, 30, 0.5D, 0.2D);
        Fx.sound(level, now, SoundEvents.ENDERMAN_TELEPORT, 0.8F, 0.8F);
    }

    private void flyToward(Vec3 goal, double speed) {
        Vec3 d = goal.subtract(this.position());
        double len = d.length();
        Vec3 v = len < 1.0E-6D ? Vec3.ZERO : d.scale(Math.min(1.0D, speed / len));
        this.setDeltaMovement(v);
        this.move(MoverType.SELF, v);
    }

    private Vec3 groundBelow(ServerLevel level, Vec3 p) {
        return new Vec3(p.x, Fx.groundY(level, p.x, p.z), p.z);
    }

    private Vec3 randomFieldPoint(ServerLevel level) {
        double a = this.random.nextDouble() * Math.PI * 2.0D;
        double r = Math.sqrt(this.random.nextDouble()) * (this.fieldRadius - 2.0D);
        double x = this.fieldCenter.x + Math.cos(a) * r;
        double z = this.fieldCenter.z + Math.sin(a) * r;
        return new Vec3(x, Fx.groundY(level, x, z), z);
    }

    private List<Player> playersInField(ServerLevel level) {
        List<Player> out = new ArrayList<>();
        for (Player p : level.getEntitiesOfClass(Player.class,
                Geo.box(this.fieldCenter.add(0.0D, 3.0D, 0.0D), this.fieldRadius, 8.0D, this.fieldRadius), Player::isAlive)) {
            if (!p.isSpectator() && Geo.horizontalDist(p.position(), this.fieldCenter) <= this.fieldRadius) {
                out.add(p);
            }
        }
        return out;
    }

    private int countCopies(ServerLevel level) {
        return level.getEntitiesOfClass(KeinLooreEntity.class, Geo.box(this.position(), 64.0D, 64.0D, 64.0D),
                KeinLooreEntity::isCopy).size();
    }

    private LivingEntity findTarget() {
        Player p = this.level().getNearestPlayer(this, SIGHT);
        return p != null && p.isAlive() && !p.isSpectator() && !com.keinloree.story.Cinematics.isActive(p) ? p : null;
    }
}
