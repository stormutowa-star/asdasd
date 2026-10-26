package com.keinloree.ability;

import com.keinloree.entity.CalendarioEntity;
import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.entity.TimeStop;
import com.keinloree.net.AbilityStateS2C;
import com.keinloree.net.Net;
import com.keinloree.registry.ModItems;
import com.keinloree.registry.ModSounds;
import com.keinloree.story.Campaign;
import com.keinloree.story.Factions;
import com.keinloree.story.ScreenFx;
import com.keinloree.util.LooreDamage;
import com.keinloree.vfx.Vfx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Tecnicas de RareIri (servidor): Primer Rayo, Llamarada, Paso Umbrio, Pozo Estelar, Hora Detenida y Hora Cero; combo
 * de tres golpes con onda, carga de la Hora Cero, dash y doble salto con la Armadura de la Sexta Hora.
 */
public final class Techniques {
    public static final int PRIMER_RAYO = 0;
    public static final int LLAMARADA = 1;
    public static final int PASO_UMBRIO = 2;
    public static final int POZO_ESTELAR = 3;
    public static final int HORA_DETENIDA = 4;
    public static final int HORA_CERO = 5;
    public static final int COUNT = 6;
    public static final String[] IDS = {"primer_rayo", "llamarada", "paso_umbrio", "pozo_estelar", "hora_detenida", "hora_cero"};
    private static final int[] COOLDOWN = {120, 200, 160, 280, 500, 40};
    private static final int[] COLORS = {0xFFC861, 0xFFF2A8, 0xC24CFF, 0x6F7BFF, 0xE8E8FF, 0xFFFFFF};
    private static final int DASH_CD = 50;

    private static final class State {
        final long[] readyAt = new long[COUNT];
        long dashReady;
        boolean airJumpUsed;
        long lastHit;
        int combo;
        int charge;
        int lastSentCharge = -1;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    /** Mientras es true, el golpe que se esta aplicando es la Hora Cero (Kein solo cae con ella). */
    private static boolean horaCeroStrike;
    private static boolean done;

    private Techniques() {
    }

    public static synchronized void init() {
        if (done) {
            return;
        }
        done = true;
        MinecraftForge.EVENT_BUS.register(new Techniques.Events());
        KeinLooreEntity.HORA_CERO = s -> horaCeroStrike;
    }

    private static State st(Player p) {
        return STATES.computeIfAbsent(p.getUUID(), u -> new State());
    }

    // ------------------------------------------------------------------ desbloqueos

    public static int unlocked(ServerPlayer p) {
        return Campaign.get(p.server).tale(p.getUUID()).techniques;
    }

    public static boolean has(ServerPlayer p, int t) {
        return (unlocked(p) & (1 << t)) != 0;
    }

    public static void unlock(ServerPlayer p, int t) {
        Campaign c = Campaign.get(p.server);
        c.tale(p.getUUID()).techniques |= 1 << t;
        c.setDirty();
        sendState(p);
        p.displayClientMessage(Component.translatable("message.keinloree.technique_unlocked",
                Component.translatable("technique.keinloree." + IDS[t])), false);
    }

    /** La Hora Cero se carga a tope (la historia lo hace en la fase final). */
    public static void fillCharge(ServerPlayer p) {
        st(p).charge = 100;
        sendState(p);
    }

    public static boolean corrupted(ServerPlayer p) {
        return Factions.legionAlly(p);
    }

    public static void sendState(ServerPlayer p) {
        State s = st(p);
        long now = p.level().getGameTime();
        int[] cd = new int[COUNT + 1];
        for (int i = 0; i < COUNT; i++) {
            cd[i] = (int) Math.max(0, s.readyAt[i] - now);
        }
        cd[COUNT] = (int) Math.max(0, s.dashReady - now);
        s.lastSentCharge = s.charge;
        Net.toPlayer(p, new AbilityStateS2C(unlocked(p), cd, s.charge, corrupted(p)));
    }

    public static boolean holdsRareIri(Player p) {
        return p.getMainHandItem().is(ModItems.RAREIRI.get());
    }

    public static boolean fullSet(Player p) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SEXTA_HORA_HELMET.get())
                && p.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.SEXTA_HORA_CHESTPLATE.get())
                && p.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SEXTA_HORA_LEGGINGS.get())
                && p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.SEXTA_HORA_BOOTS.get());
    }

    /** Enemigos de las tecnicas del jugador: la Legion, los monstruos y lo que le sea hostil. */
    public static boolean enemyOf(Player p, Entity e) {
        if (e == p || !e.isAlive() || !(e instanceof LivingEntity) || e instanceof Player) {
            return false;
        }
        if (e instanceof CalendarioEntity c) {
            return c.getMode() == CalendarioEntity.MODE_HOSTILE || c.getMode() == CalendarioEntity.MODE_DUEL;
        }
        if (Factions.isLegion(e)) {
            return !Factions.legionAlly(p);
        }
        return e instanceof Enemy;
    }

    // ------------------------------------------------------------------ peticiones

    public static void onRequest(ServerPlayer p, int action, int t) {
        if (!p.isAlive() || p.isSpectator() || com.keinloree.story.Cinematics.isActive(p)) {
            return;
        }
        State s = st(p);
        long now = p.level().getGameTime();
        ServerLevel sl = p.serverLevel();
        switch (action) {
            case 0 -> {
                if (t < 0 || t >= COUNT || !holdsRareIri(p) || !has(p, t) || now < s.readyAt[t]) {
                    return;
                }
                if (t == HORA_CERO && s.charge < 100) {
                    p.displayClientMessage(Component.translatable("message.keinloree.hora_cero_charge", s.charge), true);
                    return;
                }
                if (use(sl, p, t)) {
                    s.readyAt[t] = now + COOLDOWN[t];
                    if (t == HORA_CERO) {
                        s.charge = 0;
                    }
                }
                sendState(p);
            }
            case 1 -> {
                if (!fullSet(p) || now < s.dashReady || p.isPassenger()) {
                    return;
                }
                Vec3 look = p.getLookAngle();
                Vec3 dir = new Vec3(look.x, 0.0D, look.z).normalize();
                p.setDeltaMovement(dir.x * 1.6D, Math.max(0.2D, p.getDeltaMovement().y), dir.z * 1.6D);
                p.hurtMarked = true;
                s.dashReady = now + DASH_CD;
                Vfx.trail(sl, p, color(p, 0x9FFFE8), 12);
                sound(sl, p, ModSounds.RAREIRI_SWING.get(), 1.0F, 1.4F);
                sendState(p);
            }
            case 2 -> {
                if (!fullSet(p) || p.onGround() || s.airJumpUsed || p.isPassenger() || p.getAbilities().flying) {
                    return;
                }
                s.airJumpUsed = true;
                p.setDeltaMovement(p.getDeltaMovement().x, 0.62D, p.getDeltaMovement().z);
                p.hurtMarked = true;
                p.fallDistance = 0.0F;
                Vfx.ringExpanding(sl, p.position(), new Vec3(0, 1, 0), color(p, 0x9FFFE8), 1.6F, 10);
                sound(sl, p, ModSounds.RAREIRI_SWING.get(), 0.7F, 1.7F);
            }
            default -> { }
        }
    }

    private static int color(ServerPlayer p, int normal) {
        return corrupted(p) ? 0x2EE86A : normal;
    }

    private static boolean use(ServerLevel sl, ServerPlayer p, int t) {
        int c = color(p, COLORS[t]);
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        switch (t) {
            case PRIMER_RAYO -> {
                Vec3 dir = new Vec3(look.x, 0.0D, look.z).normalize();
                Vec3 from = p.position();
                Vec3 to = from.add(dir.scale(8.0D));
                HitResult hit = sl.clip(new ClipContext(from.add(0, 0.6, 0), to.add(0, 0.6, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
                Vec3 end = hit.getType() == HitResult.Type.MISS ? to : hit.getLocation().subtract(dir.scale(0.6D)).subtract(0, 0.6, 0);
                for (Entity e : sl.getEntities(p, new AABB(from, end).inflate(1.4D), e -> enemyOf(p, e))) {
                    hurt(p, (LivingEntity) e, 12.0F, dir);
                }
                Vfx.beam(sl, from.add(0, 1.0, 0), end.add(0, 1.0, 0), c, 0.5F, 12);
                p.teleportTo(end.x, end.y, end.z);
                p.fallDistance = 0.0F;
                sound(sl, p, ModSounds.SLASH_LIGHT.get(), 1.4F, 1.3F);
                return true;
            }
            case LLAMARADA -> {
                Vfx.shockwave(sl, p.position(), c, 6.0F, 18);
                Vfx.burst(sl, p.position().add(0, 0.3, 0), c, 30, 0.8F);
                for (Entity e : sl.getEntities(p, p.getBoundingBox().inflate(6.0D, 2.5D, 6.0D), e -> enemyOf(p, e))) {
                    hurt(p, (LivingEntity) e, 14.0F, e.position().subtract(p.position()).normalize());
                    e.setDeltaMovement(e.getDeltaMovement().add(0, 0.8, 0));
                }
                sound(sl, p, ModSounds.SOLAR_FLARE.get(), 1.6F, 1.0F);
                return true;
            }
            case PASO_UMBRIO -> {
                EntityHitResult eh = ProjectileUtil.getEntityHitResult(sl, p, eye, eye.add(look.scale(16.0D)),
                        p.getBoundingBox().expandTowards(look.scale(16.0D)).inflate(1.0D), e -> enemyOf(p, e));
                if (eh == null) {
                    p.displayClientMessage(Component.translatable("message.keinloree.no_target"), true);
                    return false;
                }
                LivingEntity tg = (LivingEntity) eh.getEntity();
                Vec3 away = new Vec3(tg.getX() - p.getX(), 0, tg.getZ() - p.getZ()).normalize();
                Vec3 behind = tg.position().add(away.scale(1.4D));
                Vfx.distortion(sl, p.position().add(0, 1, 0), c, 1.4F, 12);
                p.teleportTo(behind.x, tg.getY(), behind.z);
                p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, tg.getEyePosition());
                Vfx.distortion(sl, p.position().add(0, 1, 0), c, 1.4F, 12);
                hurt(p, tg, 18.0F, away.scale(-1));
                sound(sl, p, ModSounds.SHADOW_STEP.get(), 1.4F, 1.0F);
                return true;
            }
            case POZO_ESTELAR -> {
                HitResult hit = sl.clip(new ClipContext(eye, eye.add(look.scale(12.0D)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
                Vec3 at = hit.getLocation();
                Vfx.distortion(sl, at, c, 2.8F, 60);
                sound(sl, p, ModSounds.STAR_FALL.get(), 1.4F, 0.8F);
                schedule(60, tick -> {
                    if (tick < 59) {
                        for (Entity e : sl.getEntities(p, new AABB(at, at).inflate(7.0D, 4.0D, 7.0D), e -> enemyOf(p, e))) {
                            Vec3 pull = at.subtract(e.position()).normalize().scale(0.12D);
                            e.setDeltaMovement(e.getDeltaMovement().add(pull));
                            e.hurtMarked = true;
                        }
                    } else {
                        Vfx.burst(sl, at, 0xFFFFFF, 30, 0.9F);
                        Vfx.ringExpanding(sl, at, new Vec3(0, 1, 0), c, 5.0F, 14);
                        for (Entity e : sl.getEntities(p, new AABB(at, at).inflate(4.0D, 3.0D, 4.0D), e -> enemyOf(p, e))) {
                            hurt(p, (LivingEntity) e, 16.0F, e.position().subtract(at).normalize());
                        }
                    }
                });
                return true;
            }
            case HORA_DETENIDA -> {
                Vfx.timeFreeze(sl, p.position().add(0, 1, 0), 6.0F, 80);
                ScreenFx.send(p, ScreenFx.parse("tiempo_detenido"), 80, 0.6F, 0xE8E8FF);
                for (Entity e : sl.getEntities(p, p.getBoundingBox().inflate(6.0D, 3.0D, 6.0D), e -> enemyOf(p, e))) {
                    TimeStop.freeze((LivingEntity) e, 80);
                }
                sound(sl, p, ModSounds.TIME_STOP.get(), 1.6F, 1.0F);
                return true;
            }
            case HORA_CERO -> {
                Vec3 dir = new Vec3(look.x, 0.0D, look.z).normalize();
                Vfx.slash(sl, p.position().add(0, 1.2, 0), p.getYRot(), 0.0F, 0.0F, 18.0F, c, 16);
                Vfx.slash(sl, p.position().add(0, 1.4, 0), p.getYRot(), 0.0F, 15.0F, 14.0F, 0xFFFFFF, 14);
                ScreenFx.send(p, ScreenFx.parse("destello"), 20, 1.0F, 0xFFFFFF);
                sound(sl, p, ModSounds.RAREIRI_ULTIMATE.get(), 3.0F, 1.0F);
                horaCeroStrike = true;
                try {
                    for (Entity e : sl.getEntities(p, p.getBoundingBox().inflate(20.0D, 6.0D, 20.0D), e -> enemyOf(p, e))) {
                        Vec3 to = e.position().subtract(p.position());
                        if (to.length() <= 20.0D && new Vec3(to.x, 0, to.z).normalize().dot(dir) > 0.2D) {
                            hurt(p, (LivingEntity) e, 60.0F, dir);
                        }
                    }
                } finally {
                    horaCeroStrike = false;
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static void hurt(ServerPlayer p, LivingEntity e, float dmg, Vec3 dir) {
        LooreDamage.apply(e, p.damageSources().playerAttack(p), dmg + Integer.bitCount(unlocked(p)), dir, 0.6D);
    }

    private static void sound(ServerLevel sl, Player p, SoundEvent s, float vol, float pitch) {
        sl.playSound(null, p.getX(), p.getY(), p.getZ(), s, SoundSource.PLAYERS, vol, pitch);
    }

    private interface Task {
        void run(int tick);
    }

    private static final List<Object[]> SCHEDULED = new ArrayList<>();

    private static void schedule(int ticks, Task task) {
        SCHEDULED.add(new Object[]{task, new int[]{0, ticks}});
    }

    // ------------------------------------------------------------------ eventos

    public static final class Events {
        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent e) {
            if (e.phase != TickEvent.Phase.END || SCHEDULED.isEmpty()) {
                return;
            }
            Iterator<Object[]> it = SCHEDULED.iterator();
            List<Object[]> keep = new ArrayList<>();
            while (it.hasNext()) {
                Object[] o = it.next();
                int[] t = (int[]) o[1];
                try {
                    ((Task) o[0]).run(t[0]);
                } catch (Exception ex) {
                    t[0] = t[1];
                }
                t[0]++;
                if (t[0] < t[1]) {
                    keep.add(o);
                }
            }
            SCHEDULED.clear();
            SCHEDULED.addAll(keep);
        }

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent e) {
            if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) {
                return;
            }
            State s = st(p);
            if (p.onGround()) {
                s.airJumpUsed = false;
            }
            if (p.tickCount % 20 == 0 && holdsRareIri(p) && s.lastSentCharge != s.charge) {
                sendState(p);
            }
        }

        @SubscribeEvent
        public void onHurt(LivingHurtEvent e) {
            if (!(e.getSource().getDirectEntity() instanceof ServerPlayer p) || e.getSource().getEntity() != p || !holdsRareIri(p)) {
                return;
            }
            State s = st(p);
            long now = p.level().getGameTime();
            // +1 de dano por tecnica aprendida
            e.setAmount(e.getAmount() + Integer.bitCount(unlocked(p) & 0x1F));
            s.combo = now - s.lastHit <= 24 ? s.combo + 1 : 1;
            s.lastHit = now;
            s.charge = Math.min(100, s.charge + 2);
            ServerLevel sl = p.serverLevel();
            int c = color(p, 0x9FFFE8);
            Vfx.slash(sl, p.position().add(0, 1.3, 0).add(p.getLookAngle().scale(0.8D)), p.getYRot(),
                    (s.combo % 2 == 0 ? 25.0F : -25.0F), (s.combo % 2 == 0 ? 35.0F : -35.0F), 2.6F, c, 6);
            sound(sl, p, ModSounds.RAREIRI_SWING.get(), 0.8F, 0.9F + s.combo * 0.1F);
            if (s.combo >= 3) {
                s.combo = 0;
                Vec3 dir = new Vec3(p.getLookAngle().x, 0, p.getLookAngle().z).normalize();
                Vfx.slash(sl, p.position().add(0, 1.0, 0).add(dir.scale(2.0D)), p.getYRot(), 0.0F, 0.0F, 5.0F, c, 12);
                for (Entity en : sl.getEntities(p, p.getBoundingBox().inflate(5.0D, 2.0D, 5.0D), x -> enemyOf(p, x) && x != e.getEntity())) {
                    Vec3 to = en.position().subtract(p.position());
                    if (to.length() < 5.5D && new Vec3(to.x, 0, to.z).normalize().dot(dir) > 0.3D) {
                        hurt(p, (LivingEntity) en, 7.0F, dir);
                    }
                }
            }
        }

        /** Con la armadura completa de la Sexta Hora el heroe no retrocede al recibir golpes. */
        @SubscribeEvent
        public void onKnockback(LivingKnockBackEvent e) {
            if (e.getEntity() instanceof Player p && fullSet(p)) {
                e.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onFall(LivingFallEvent e) {
            if (e.getEntity() instanceof Player p && fullSet(p)) {
                e.setDistance(Math.max(0.0F, e.getDistance() - 12.0F));
            }
        }

        @SubscribeEvent
        public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
            if (e.getEntity() instanceof ServerPlayer p) {
                sendState(p);
            }
        }

        @SubscribeEvent
        public void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
            STATES.remove(e.getEntity().getUUID());
        }
    }

    /** Objeto para la historia: el equipo completo. */
    public static List<ItemStack> gear() {
        return List.of(new ItemStack(ModItems.RAREIRI.get()), new ItemStack(ModItems.SEXTA_HORA_HELMET.get()),
                new ItemStack(ModItems.SEXTA_HORA_CHESTPLATE.get()), new ItemStack(ModItems.SEXTA_HORA_LEGGINGS.get()),
                new ItemStack(ModItems.SEXTA_HORA_BOOTS.get()));
    }
}
