package com.ricardo.retorcido.amigo;

import com.ricardo.retorcido.RetorcidoEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * El AMIGO: un NPC con aspecto de jugador que acompana al dueno (un jugador).
 *
 * <p>API para el director de terror: {@link #setScripted(boolean)}, {@link #isScripted()},
 * {@link #startStaring(Vec3, int)}, {@link #walkTo(Vec3, double)}, {@link #isNear(Vec3, double)},
 * {@link #say(String)} y {@link #getOwner()}. Mientras el amigo esta "scripted" o mirando fijamente,
 * ni sigue al dueno, ni pasea, ni charla solo: el director manda.
 */
public class AmigoEntity extends PathfinderMob {
    public static final String DEFAULT_NAME = "Dani";

    private static final String[] DAY = {
            "Menudo dia mas bueno hace, ¿no?",
            "Con este sol da gusto explorar.",
            "¿Vamos a buscar recursos mientras hay luz?",
            "Aprovechemos el dia, que luego se complica.",
    };
    private static final String[] NIGHT = {
            "Se hace de noche, ¿hacemos un refugio?",
            "Ya es de noche, no me fio de salir mucho.",
            "Deberiamos dormir, ¿tienes cama?",
            "Con tanta oscuridad salen bichos por todos lados.",
    };
    private static final String[] RAIN = {
            "Esta lloviendo otra vez...",
            "Con esta lluvia me quedaria dentro, la verdad.",
            "Menos mal que no hay tormenta, ¿no?",
    };
    private static final String[] OWNER_LOW = {
            "Oye, ¿estas bien? Te veo mal de vida.",
            "Come algo, que vas muy justo.",
            "Ten cuidado, estas bastante herido.",
    };
    private static final String[] SELF_LOW = {
            "Ay, estoy un poco tocado, voy a comer algo.",
            "Me han dado bien... necesito un respiro.",
            "Creo que me vendria bien descansar un poco.",
    };
    private static final String[] UNDERGROUND = {
            "Que oscuro esta esto, ¿tienes antorchas?",
            "¿Seguro que sabes volver desde aqui abajo?",
            "Escucha los ruidos de la cueva, da algo de yuyu.",
            "Vamos poniendo antorchas para no perdernos.",
    };
    private static final String[] HOSTILE = {
            "¡Cuidado, hay algo cerca!",
            "Mira, un bicho. Yo me encargo.",
            "Ojo, que viene un monstruo.",
    };
    private static final String[] FRIENDLY = {
            "¿Que construimos hoy?",
            "¿Te acuerdas de donde dejamos el cofre?",
            "Podriamos hacer una granja, ¿no?",
            "Me encanta explorar contigo.",
            "¿Tienes hambre? Yo un poco.",
            "¿Y si buscamos diamantes?",
            "Hoy esta siendo un buen dia de aventura.",
            "Me pregunto que habra detras de esa montana.",
    };

    private UUID ownerUuid;
    private boolean scripted;
    private int stareTicks;
    private Vec3 stareTarget;
    private int talkCooldown;
    private int eatCooldown;
    private String lastPhrase = "";

    public AmigoEntity(EntityType<? extends AmigoEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setCustomName(Component.literal(DEFAULT_NAME));
        this.setCustomNameVisible(true);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TORCH));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.talkCooldown = 2400 + this.random.nextInt(2401);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, RetorcidoEntity.class, 18.0F, 1.2D, 1.5D,
                e -> e instanceof RetorcidoEntity r && !r.isWatcher()));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new AmigoFollowGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                AmigoEntity self = AmigoEntity.this;
                if (self.scripted || self.stareTicks > 0) return false;
                ServerPlayer owner = self.getOwner();
                if (owner != null && owner.level() == self.level() && owner.distanceToSqr(self) < 16.0D * 16.0D) {
                    return false; // hay dueno cerca: no vagabundea
                }
                return super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // La cobardia ante el Retorcido es intencional: nunca lo ataca.
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                e -> !(e instanceof RetorcidoEntity)));
    }

    // ------------------------------------------------------------------ API para el director

    /** Dueno (jugador conectado) o null si no esta online / no hay dueno. Solo servidor. */
    public ServerPlayer getOwner() {
        if (this.ownerUuid == null || !(this.level() instanceof ServerLevel sl)) return null;
        return sl.getServer().getPlayerList().getPlayer(this.ownerUuid);
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public void setOwnerUuid(UUID uuid) {
        this.ownerUuid = uuid;
    }

    /** Activa/desactiva el modo guion: scripted = no sigue, no pasea, no charla solo. */
    public void setScripted(boolean scripted) {
        this.scripted = scripted;
        if (scripted) this.getNavigation().stop();
    }

    public boolean isScripted() {
        return this.scripted;
    }

    /** Se queda quieto mirando fijamente {@code target} durante {@code ticks} ticks. */
    public void startStaring(Vec3 target, int ticks) {
        this.stareTarget = target;
        this.stareTicks = Math.max(0, ticks);
        this.getNavigation().stop();
    }

    /** Ticks restantes de mirada fija (0 = no esta mirando). */
    public int getStareTicks() {
        return this.stareTicks;
    }

    /** Camina hacia {@code pos} a la velocidad dada (1.0 = normal). Devuelve false si no hay camino. */
    public boolean walkTo(Vec3 pos, double speed) {
        Path path = this.getNavigation().createPath(pos.x, pos.y, pos.z, 0);
        if (path == null) return false;
        return this.getNavigation().moveTo(path, speed);
    }

    /** true si el amigo esta a {@code dist} bloques o menos de {@code pos}. */
    public boolean isNear(Vec3 pos, double dist) {
        return this.position().distanceToSqr(pos) <= dist * dist;
    }

    /** Dice un mensaje de chat a todos los jugadores: {@code <Nombre> msg}. Solo servidor. */
    public void say(String msg) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("<" + this.getName().getString() + "> " + msg), false);
    }

    // ------------------------------------------------------------------ logica

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.stareTicks > 0) {
            this.stareTicks--;
            this.getNavigation().stop();
            this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
            if (this.stareTarget != null) {
                this.getLookControl().setLookAt(this.stareTarget.x, this.stareTarget.y, this.stareTarget.z);
            }
            if (this.stareTicks == 0) this.stareTarget = null;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide || !this.isAlive()) return;

        // Comer: con poca vida se cura 2 puntos cada 5 s.
        if (this.eatCooldown > 0) this.eatCooldown--;
        if (this.getHealth() < 10.0F && this.eatCooldown <= 0) {
            this.heal(2.0F);
            this.eatCooldown = 100;
            this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }

        // Charla de companero.
        if (--this.talkCooldown <= 0) {
            this.talkCooldown = 2400 + this.random.nextInt(2401); // 2-4 min
            ServerPlayer owner = this.getOwner();
            if (owner != null && !this.scripted && this.stareTicks <= 0
                    && owner.level() == this.level() && owner.distanceTo(this) < 40.0F) {
                this.say(pickPhrase(owner));
            }
        }
    }

    private String pickPhrase(ServerPlayer owner) {
        List<String[]> urgent = new ArrayList<>();
        if (owner.getHealth() < 8.0F) urgent.add(OWNER_LOW);
        if (this.getHealth() < 8.0F) urgent.add(SELF_LOW);
        boolean hostileNear = !this.level().getEntitiesOfClass(Monster.class,
                this.getBoundingBox().inflate(16.0D), m -> !(m instanceof RetorcidoEntity)).isEmpty();
        if (hostileNear) urgent.add(HOSTILE);

        List<String[]> pool = new ArrayList<>();
        if (!urgent.isEmpty()) {
            pool = urgent;
        } else {
            pool.add(FRIENDLY);
            pool.add(this.level().isDay() ? DAY : NIGHT);
            if (this.level().isRaining()) pool.add(RAIN);
            if (this.getY() < 40 && !this.level().canSeeSky(this.blockPosition())) pool.add(UNDERGROUND);
        }
        String phrase = this.lastPhrase;
        for (int i = 0; i < 6 && phrase.equals(this.lastPhrase); i++) {
            String[] group = pool.get(this.random.nextInt(pool.size()));
            phrase = group[this.random.nextInt(group.length)];
        }
        this.lastPhrase = phrase;
        return phrase;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) AmigoManager.onAmigoDied(this, source);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerUuid != null) tag.putUUID("OwnerUuid", this.ownerUuid);
        tag.putBoolean("Scripted", this.scripted);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ownerUuid = tag.hasUUID("OwnerUuid") ? tag.getUUID("OwnerUuid") : null;
        this.scripted = tag.getBoolean("Scripted");
    }
}
