package com.ricardo.retorcido.terror;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import com.ricardo.retorcido.RetorcidoEntity;
import com.ricardo.retorcido.RetorcidoMod;
import com.ricardo.retorcido.amigo.AmigoEntity;
import com.ricardo.retorcido.amigo.AmigoManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Director de terror: 100% servidor. Decide que fase vive cada jugador, lanza sucesos raros,
 * lleva el guion del secuestro de Dani, los cazadores y el regreso. Estado en memoria por jugador
 * (se limpia al desconectar, morir o cambiar de dimension) + progreso persistente en {@link TerrorData}.
 */
@Mod.EventBusSubscriber(modid = RetorcidoMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TerrorDirector {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Guion del secuestro (no se guarda). */
    static final class Script {
        int step = 1; // 1 = Dani camina al punto, 2 = mirada previa, 3 = tras la desaparicion
        int timer;
        Vec3 point;
        Vec3 away;
        UUID amigo;
    }

    static final class PlayerState {
        int cooldown = -1;
        Script script;
        boolean daniLocked;
        String lastLine = "";
        UUID watcher;
    }

    private static final class Task {
        final UUID player;
        final long due;
        final Consumer<ServerPlayer> action;

        Task(UUID player, long due, Consumer<ServerPlayer> action) {
            this.player = player;
            this.due = due;
            this.action = action;
        }
    }

    private static final Map<UUID, PlayerState> STATES = new HashMap<>();
    private static final List<Task> TASKS = new ArrayList<>();
    private static long clock;

    private TerrorDirector() {}

    static PlayerState stateOf(ServerPlayer p) {
        return STATES.computeIfAbsent(p.getUUID(), k -> new PlayerState());
    }

    /** Programa una accion para dentro de {@code delay} ticks; se cancela si el jugador se va/muere/cambia de dimension. */
    static void schedule(ServerPlayer p, int delay, Consumer<ServerPlayer> action) {
        TASKS.add(new Task(p.getUUID(), clock + delay, action));
    }

    // ------------------------------------------------------------------ observadores y cazadores

    static void setWatcher(UUID player, UUID entity) {
        PlayerState st = STATES.computeIfAbsent(player, k -> new PlayerState());
        st.watcher = entity;
    }

    /** Observador vivo del jugador, o null. */
    static Entity watcherOf(ServerLevel level, UUID player) {
        PlayerState st = STATES.get(player);
        if (st == null || st.watcher == null) return null;
        Entity e = level.getEntity(st.watcher);
        if (e == null || !e.isAlive()) {
            st.watcher = null;
            return null;
        }
        return e;
    }

    private static List<? extends RetorcidoEntity> hunters(ServerLevel level, UUID owner) {
        String tag = TerrorConfig.HUNTER_TAG + "_of_" + owner;
        return level.getEntities(RetorcidoMod.RETORCIDO.get(), e -> e.isAlive() && e.getTags().contains(tag));
    }

    private static void vanishEntity(ServerLevel level, Entity e) {
        level.sendParticles(ParticleTypes.SMOKE, e.getX(), e.getY() + 1.0D, e.getZ(), 25, 0.4D, 0.8D, 0.4D, 0.02D);
        level.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.5F);
        e.discard();
    }

    // ------------------------------------------------------------------ limpieza

    private static void abortScript(ServerPlayer p, PlayerState st) {
        if (st.script != null) {
            Entity e = p.serverLevel().getEntity(st.script.amigo);
            if (e instanceof AmigoEntity a) a.setScripted(false);
            st.script = null;
        }
    }

    /** Limpia todo el estado en memoria del jugador (desconexion, muerte, cambio de dimension). */
    static void cleanup(ServerPlayer p) {
        try {
            PlayerState st = STATES.get(p.getUUID());
            if (st != null) {
                abortScript(p, st);
                st.daniLocked = false;
                st.cooldown = -1;
                if (st.watcher != null) {
                    Entity w = p.serverLevel().getEntity(st.watcher);
                    if (w != null) w.discard();
                    st.watcher = null;
                }
            }
            UUID id = p.getUUID();
            TASKS.removeIf(t -> t.player.equals(id));
            AmigoEntity a = AmigoManager.findAmigo(p);
            if (a != null && a.isScripted()) a.setScripted(false);
        } catch (Exception e) {
            LOGGER.warn("[retorcido] fallo limpiando el estado del jugador", e);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            cleanup(p);
            STATES.remove(p.getUUID());
        }
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) cleanup(p);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        try {
            if (event.getEntity() instanceof ServerPlayer p) {
                cleanup(p);
            } else if (event.getEntity() instanceof RetorcidoEntity r && r.getTags().contains(TerrorConfig.HUNTER_TAG)
                    && r.level() instanceof ServerLevel level) {
                onHunterKilled(level, r);
            }
        } catch (Exception e) {
            LOGGER.warn("[retorcido] fallo en onDeath", e);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        STATES.clear();
        TASKS.clear();
    }

    private static void onHunterKilled(ServerLevel level, RetorcidoEntity r) {
        String prefix = TerrorConfig.HUNTER_TAG + "_of_";
        for (String tag : r.getTags()) {
            if (!tag.startsWith(prefix)) continue;
            UUID owner;
            try {
                owner = UUID.fromString(tag.substring(prefix.length()));
            } catch (IllegalArgumentException ex) {
                continue;
            }
            MinecraftServer server = level.getServer();
            TerrorData data = TerrorData.get(server);
            long day = server.overworld().getDayTime() / 24000L;
            data.get(owner).huntCooldownUntilDay = day + TerrorConfig.HUNT_COOLDOWN_DAYS;
            data.setDirty();
            ServerPlayer p = server.getPlayerList().getPlayer(owner);
            if (p != null) {
                AmigoEntity a = AmigoManager.findAmigo(p);
                if (a != null && !a.isScripted()) a.say(TerrorLines.HUNTER_KILLED);
            }
        }
    }

    /** Hook desde {@link AmigoManager#onAmigoDied}: si muere antes del secuestro, se re-crea tras 60 s. */
    public static void onAmigoDied(AmigoEntity amigo, DamageSource source) {
        try {
            UUID owner = amigo.getOwnerUuid();
            if (owner == null || !(amigo.level() instanceof ServerLevel level)) return;
            PlayerState st = STATES.get(owner);
            if (st != null && st.script != null && amigo.getUUID().equals(st.script.amigo)) {
                st.script = null;
                st.daniLocked = false;
            }
            MinecraftServer server = level.getServer();
            TerrorData data = TerrorData.get(server);
            TerrorData.PlayerData d = data.get(owner);
            if (d.abducted && !d.returned) return; // secuestrado: no se re-crea hasta el regreso
            d.daniRespawnAt = server.overworld().getGameTime() + TerrorConfig.DANI_RESPAWN_DELAY;
            data.setDirty();
        } catch (Exception e) {
            LOGGER.warn("[retorcido] fallo en onAmigoDied", e);
        }
    }

    // ------------------------------------------------------------------ tick

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        clock = server.getTickCount();
        try {
            runTasks(server);
            if (clock % 20 == 0) fleeHuntersAtDay(server);
            if (server.getPlayerList().getPlayers().isEmpty()) return;
            TerrorData data = TerrorData.get(server);
            for (ServerPlayer p : new ArrayList<>(server.getPlayerList().getPlayers())) {
                try {
                    tickPlayer(server, data, p);
                } catch (Exception e) {
                    LOGGER.warn("[retorcido] fallo en el director para {}", p.getGameProfile().getName(), e);
                    PlayerState st = STATES.get(p.getUUID());
                    if (st != null) {
                        abortScript(p, st);
                        st.cooldown = TerrorConfig.EVENT_RETRY_TICKS;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[retorcido] fallo en el tick del director", e);
        }
    }

    private static void runTasks(MinecraftServer server) {
        if (TASKS.isEmpty()) return;
        List<Task> due = new ArrayList<>();
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            if (t.due <= clock) {
                due.add(t);
                it.remove();
            }
        }
        for (Task t : due) {
            ServerPlayer p = server.getPlayerList().getPlayer(t.player);
            if (p == null || !p.isAlive()) continue;
            try {
                t.action.accept(p);
            } catch (Exception e) {
                LOGGER.warn("[retorcido] fallo en una accion programada", e);
            }
        }
    }

    private static void fleeHuntersAtDay(MinecraftServer server) {
        ServerLevel ow = server.overworld();
        if (!ow.isDay()) return;
        for (RetorcidoEntity h : ow.getEntities(RetorcidoMod.RETORCIDO.get(),
                e -> e.isAlive() && e.getTags().contains(TerrorConfig.HUNTER_TAG))) {
            vanishEntity(ow, h);
        }
    }

    private static int cooldownFor(int phase, RandomSource rnd) {
        int i = Math.max(0, Math.min(4, phase - 1));
        int min = TerrorConfig.EVENT_COOLDOWN_MIN[i];
        int max = TerrorConfig.EVENT_COOLDOWN_MAX[i];
        return min + rnd.nextInt(Math.max(1, max - min + 1));
    }

    private static void tickPlayer(MinecraftServer server, TerrorData data, ServerPlayer p) {
        if (!p.isAlive()) return;
        PlayerState st = stateOf(p);
        boolean overworld = p.level().dimension() == Level.OVERWORLD;
        if (!overworld) {
            if (st.script != null) cleanup(p);
            return;
        }
        TerrorData.PlayerData d = data.get(p.getUUID());
        ServerLevel level = p.serverLevel();

        if (clock % 20 == 0 && !p.isSpectator()) {
            d.playTicks += 20;
            data.setDirty();
        }
        int phase = d.phase();

        if (st.script != null) {
            tickScript(server, data, d, p, st);
            return;
        }
        if (clock % 20 == 10) periodic(server, data, d, p, st, level, phase);
        if (clock % 100 == 50) housekeeping(p, st, d, phase);
        if (st.script != null || p.isSpectator()) return;

        if (st.cooldown < 0) {
            st.cooldown = cooldownFor(phase, p.getRandom());
        } else if (--st.cooldown <= 0) {
            List<String> pool = TerrorEvents.pool(phase);
            String name = pool.get(p.getRandom().nextInt(pool.size()));
            boolean ok = TerrorEvents.run(name, p, phase);
            st.cooldown = ok ? cooldownFor(phase, p.getRandom()) : TerrorConfig.EVENT_RETRY_TICKS;
        }
    }

    /** Cada segundo: respawn de Dani, cacerias, regreso y disparo del secuestro. */
    private static void periodic(MinecraftServer server, TerrorData data, TerrorData.PlayerData d, ServerPlayer p,
                                 PlayerState st, ServerLevel level, int phase) {
        long gameTime = server.overworld().getGameTime();
        long dayTime = level.getDayTime();
        long tod = dayTime % 24000L;
        long day = dayTime / 24000L;

        // Dani muerto antes del secuestro: vuelve a los 60 s.
        if (d.daniRespawnAt > 0 && gameTime >= d.daniRespawnAt) {
            d.daniRespawnAt = 0;
            data.setDirty();
            if (!(d.abducted && !d.returned) && AmigoManager.findAmigo(p) == null) {
                AmigoEntity a = AmigoManager.spawnAmigo(p);
                if (a != null) {
                    a.setMood(d.returned ? 2 : (phase >= 2 ? 1 : 0));
                    a.say(TerrorLines.RESPAWN);
                }
            }
        }

        // Cacería nocturna (Fase 5).
        if (phase == 5 && tod >= TerrorConfig.HUNT_START && tod <= TerrorConfig.HUNT_END
                && d.lastHuntDay != day && day >= d.huntCooldownUntilDay) {
            if (hunters(level, p.getUUID()).isEmpty() && TerrorEvents.spawnHunter(p)) {
                d.lastHuntDay = day;
                data.setDirty();
            }
        }

        // Regreso de Dani al amanecer.
        if (phase == 5 && d.abducted && !d.returned && tod >= TerrorConfig.RETURN_START && tod <= TerrorConfig.RETURN_END) {
            TerrorEvents.run("regreso", p, phase);
        }

        // Secuestro: F3 -> F4, de noche.
        if (phase == 4 && !d.abducted && tod >= TerrorConfig.NIGHT_START && tod <= TerrorConfig.NIGHT_END && !p.isSpectator()) {
            startKidnap(p, false);
        }
    }

    /** Cada 5 s: mood de Dani y desbloqueo si se quedo "scripted" sin guion (p.ej. tras reiniciar el servidor). */
    private static void housekeeping(ServerPlayer p, PlayerState st, TerrorData.PlayerData d, int phase) {
        AmigoEntity a = AmigoManager.findAmigo(p);
        if (a == null) return;
        int target = d.returned ? 2 : (phase >= 2 && !d.abducted ? 1 : 0);
        if (a.getMood() < target) a.setMood(target);
        if (a.isScripted() && !st.daniLocked && st.script == null) a.setScripted(false);
    }

    // ------------------------------------------------------------------ secuestro

    /** Inicia el guion del secuestro. {@code force} ignora distancia a Dani (lo trae al lado) y la hora. */
    public static boolean startKidnap(ServerPlayer p, boolean force) {
        PlayerState st = stateOf(p);
        ServerLevel level = p.serverLevel();
        if (st.script != null || st.daniLocked || level.dimension() != Level.OVERWORLD) return false;
        AmigoEntity a = AmigoManager.findAmigo(p);
        if (force) {
            if (a == null) {
                a = AmigoManager.spawnAmigo(p);
            } else if (a.level() != level || a.distanceTo(p) > TerrorConfig.KIDNAP_MAX_DANI_DIST) {
                Vec3 near = AmigoManager.findStandPos(level, p.position(), a.getType().getDimensions(), 3);
                a.getNavigation().stop();
                a.teleportTo(level, near.x, near.y, near.z, java.util.Set.of(), p.getYRot(), 0.0F);
            }
        } else if (a != null && (a.level() != level || a.distanceTo(p) >= TerrorConfig.KIDNAP_MAX_DANI_DIST)) {
            return false;
        }
        if (a == null || a.isScripted() || a.getStareTicks() > 0) return false;

        Vec3 point = TerrorEvents.findSpot(level, p, TerrorConfig.KIDNAP_POINT_MIN, TerrorConfig.KIDNAP_POINT_MAX,
                false, a.getType().getDimensions());
        if (point == null) return false;

        a.setScripted(true);
        a.say(TerrorLines.KIDNAP_START);
        if (!a.walkTo(point, 1.0D)) { // sin camino: lo teletransportamos
            a.getNavigation().stop();
            a.moveTo(point.x, point.y, point.z, a.getYRot(), 0.0F);
        }
        Script s = new Script();
        s.point = point;
        Vec3 dir = point.subtract(p.position());
        dir = new Vec3(dir.x, 0.0D, dir.z).normalize();
        s.away = point.add(dir.scale(TerrorConfig.KIDNAP_STARE_EXTRA)).add(0.0D, 1.6D, 0.0D);
        s.amigo = a.getUUID();
        st.script = s;
        st.daniLocked = true;
        return true;
    }

    private static void tickScript(MinecraftServer server, TerrorData data, TerrorData.PlayerData d, ServerPlayer p, PlayerState st) {
        Script s = st.script;
        ServerLevel level = p.serverLevel();
        Entity e = level.getEntity(s.amigo);
        AmigoEntity a = e instanceof AmigoEntity ae && ae.isAlive() ? ae : null;
        s.timer++;
        switch (s.step) {
            case 1 -> {
                if (a == null) { st.script = null; st.daniLocked = false; return; }
                if (s.timer >= TerrorConfig.KIDNAP_WALK_TIMEOUT || a.isNear(s.point, TerrorConfig.KIDNAP_ARRIVE_DIST)) {
                    a.startStaring(s.away, TerrorConfig.KIDNAP_STARE_TICKS);
                    a.say("...");
                    s.step = 2;
                    s.timer = 0;
                }
            }
            case 2 -> {
                if (a == null) { st.script = null; st.daniLocked = false; return; }
                if (s.timer >= TerrorConfig.KIDNAP_STARE_TICKS) {
                    agony(server, data, d, p, level, a, s);
                    s.step = 3;
                    s.timer = 0;
                }
            }
            case 3 -> {
                if (s.timer >= TerrorConfig.KIDNAP_AFTER_TICKS) {
                    TerrorEvents.darknessAndHeartbeats(p, TerrorConfig.KIDNAP_DARKNESS_TICKS);
                    st.script = null;
                    st.daniLocked = false;
                    st.cooldown = -1;
                    TerrorEvents.spawnHunter(p);
                }
            }
            default -> st.script = null;
        }
    }

    private static void agony(MinecraftServer server, TerrorData data, TerrorData.PlayerData d, ServerPlayer p,
                              ServerLevel level, AmigoEntity a, Script s) {
        Vec3 pos = a.position();
        String name = a.getName().getString();
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.PLAYER_HURT, SoundSource.HOSTILE, 4.0F, 0.8F);
        schedule(p, 8, pl -> pl.serverLevel().playSound(null, pos.x, pos.y, pos.z, SoundEvents.RAVAGER_ROAR,
                SoundSource.HOSTILE, 4.0F, 0.5F));
        schedule(p, 14, pl -> pl.serverLevel().playSound(null, pos.x, pos.y, pos.z, SoundEvents.WARDEN_ATTACK_IMPACT,
                SoundSource.HOSTILE, 4.0F, 1.0F));
        TerrorEvents.bloodBurst(level, pos);
        ItemEntity torch = new ItemEntity(level, pos.x, pos.y + 0.5D, pos.z, new ItemStack(Items.TORCH));
        level.addFreshEntity(torch);
        // Rastro de sangre arrastrandose lejos del jugador.
        Vec3 dir = pos.subtract(p.position());
        dir = new Vec3(dir.x, 0.0D, dir.z).normalize();
        BlockParticleOption blood = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.NETHER_WART_BLOCK.defaultBlockState());
        for (int i = 1; i <= 10; i++) {
            Vec3 q = pos.add(dir.scale(i * 0.9D));
            level.sendParticles(blood, q.x, q.y + 0.1D, q.z, 6, 0.15D, 0.05D, 0.15D, 0.0D);
        }
        a.discard();
        d.abducted = true;
        d.daniRespawnAt = 0;
        data.setDirty();
        server.getPlayerList().broadcastSystemMessage(
                Component.translatable("multiplayer.player.left", name).withStyle(ChatFormatting.YELLOW), false);
    }

    // ------------------------------------------------------------------ comandos

    private static final int[] PHASE_TICKS = {
            0, 0, TerrorConfig.PHASE2_TICKS, TerrorConfig.PHASE3_TICKS, TerrorConfig.PHASE4_TICKS, TerrorConfig.PHASE4_TICKS};

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("terror").requires(s -> s.hasPermission(2));

        root.then(Commands.literal("fase").then(Commands.argument("n", IntegerArgumentType.integer(0, 5)).executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            int n = IntegerArgumentType.getInteger(ctx, "n");
            TerrorData data = TerrorData.get(ctx.getSource().getServer());
            TerrorData.PlayerData d = data.get(p.getUUID());
            d.forcedPhase = n == 0 ? -1 : n;
            d.playTicks = PHASE_TICKS[n];
            data.setDirty();
            stateOf(p).cooldown = -1;
            ctx.getSource().sendSuccess(() -> Component.literal(n == 0
                    ? "Fase automática restablecida (tiempo de juego a 0)."
                    : "Fase forzada a " + n + "."), false);
            return 1;
        })));

        LiteralArgumentBuilder<CommandSourceStack> ev = Commands.literal("evento");
        for (String name : TerrorEvents.NAMES) {
            ev.then(Commands.literal(name).executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                TerrorData.PlayerData d = TerrorData.get(ctx.getSource().getServer()).get(p.getUUID());
                int phase = d.phase();
                if (name.equals("observador")) phase = Math.max(2, phase);
                if (name.equals("dani_dice")) phase = Math.max(1, phase);
                boolean ok = TerrorEvents.run(name, p, phase);
                ctx.getSource().sendSuccess(() -> Component.literal(ok
                        ? "Suceso '" + name + "' ejecutado."
                        : "No se pudo ejecutar '" + name + "' (sin sitio válido, sin Dani o ya activo)."), false);
                return ok ? 1 : 0;
            }));
        }
        root.then(ev);

        root.then(Commands.literal("reset").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            MinecraftServer server = ctx.getSource().getServer();
            cleanup(p);
            STATES.remove(p.getUUID());
            TerrorData.get(server).remove(p.getUUID());
            for (RetorcidoEntity h : hunters(p.serverLevel(), p.getUUID())) h.discard();
            AmigoEntity a = AmigoManager.findAmigo(p);
            if (a != null) {
                a.setMood(0);
                a.setScripted(false);
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Datos de terror borrados. Dani vuelve a la normalidad."), false);
            return 1;
        }));

        root.then(Commands.literal("estado").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            TerrorData.PlayerData d = TerrorData.get(ctx.getSource().getServer()).get(p.getUUID());
            String msg = "Fase: " + d.phase() + (d.forcedPhase >= 1 ? " (forzada)" : "")
                    + " | playTicks: " + d.playTicks + " (" + (d.playTicks / 1200) + " min)"
                    + " | secuestrado: " + (d.abducted ? "sí" : "no")
                    + " | regresó: " + (d.returned ? "sí" : "no");
            ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
            return 1;
        }));

        dispatcher.register(root);
    }
}
