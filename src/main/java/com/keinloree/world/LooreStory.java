package com.keinloree.world;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.Droid;
import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.entity.LooreCommander;
import com.keinloree.util.Fx;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Historia de la mision, guardada en el mundo. Etapas: nave, bases, fortaleza y combate.
 * Finales: vencer a Kein Loree, dejar que el mundo sea consumido, o perecer.
 */
public class LooreStory extends SavedData {

    public static final int IDLE = 0;
    public static final int SHIP = 1;
    public static final int BASES = 2;
    public static final int FORTRESS = 3;
    public static final int FIGHT = 4;

    public static final int END_NONE = 0;
    public static final int END_RESIST = 1;
    public static final int END_CONSUMED = 2;
    public static final int END_PERISHED = 3;

    /** Altura de la nave (base del casco). La torre llega 15 bloques mas arriba. */
    public static final int SHIP_Y = 236;
    private static final int BASE_INTERVAL = 2400;
    private static final int OUTPOSTS = 5;
    private static final String DATA_ID = "keinloree_story";
    private static final SavedData.Factory<LooreStory> FACTORY =
            new SavedData.Factory<>(LooreStory::new, LooreStory::load, null);

    private record Scene(long at, String title, String sub, String chat) {
    }

    /** Lineas del guion programadas (no se guardan: si se reinicia el servidor, se pierden). */
    private final List<Scene> scenes = new ArrayList<>();
    private long timer;

    private int stage = IDLE;
    private int ending = END_NONE;
    private int basesBuilt;
    private int nextBaseIn = BASE_INTERVAL;
    private int shipX;
    private int shipZ;
    private UUID bossId;

    public LooreStory() {
    }

    public static LooreStory get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
    }

    public static LooreStory load(CompoundTag tag, HolderLookup.Provider registries) {
        LooreStory s = new LooreStory();
        s.stage = tag.getInt("stage");
        s.ending = tag.getInt("ending");
        s.basesBuilt = tag.getInt("bases");
        s.nextBaseIn = tag.getInt("nextBase");
        s.shipX = tag.getInt("shipX");
        s.shipZ = tag.getInt("shipZ");
        if (tag.hasUUID("boss")) {
            s.bossId = tag.getUUID("boss");
        }
        return s;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("stage", this.stage);
        tag.putInt("ending", this.ending);
        tag.putInt("bases", this.basesBuilt);
        tag.putInt("nextBase", this.nextBaseIn);
        tag.putInt("shipX", this.shipX);
        tag.putInt("shipZ", this.shipZ);
        if (this.bossId != null) {
            tag.putUUID("boss", this.bossId);
        }
        return tag;
    }

    // ------------------------------------------------------------------ inicio y ciclo

    /** Lo llama el Nucleo de Decodificacion: empieza la mision si no hay una en curso. */
    public static void startMission(ServerLevel level, ServerPlayer player) {
        ServerLevel overworld = level.getServer().overworld();
        LooreStory s = get(overworld);
        if (s.stage != IDLE) {
            player.sendSystemMessage(Component.literal("La misión ya está en curso.").withStyle(ChatFormatting.RED));
            return;
        }
        s.begin(overworld, player);
    }

    private void begin(ServerLevel overworld, ServerPlayer player) {
        this.stage = SHIP;
        this.ending = END_NONE;
        this.basesBuilt = 0;
        this.nextBaseIn = BASE_INTERVAL;
        this.shipX = player.getBlockX();
        this.shipZ = player.getBlockZ();
        this.bossId = null;
        this.scenes.clear();
        this.timer = 0L;
        this.schedule(0L, "SEÑAL DETECTADA", "Núcleo de Decodificación activo",
                "Señal recibida. Este planeta es el siguiente en la red.");
        this.schedule(100L, null, null, "Decodificar un mundo es fácil. Resistirlo, no tanto.");
        this.schedule(200L, null, null, "Mi nave ya desciende sobre tu cielo.");
        Builds.enqueue(overworld, Builds.ship(new BlockPos(this.shipX, SHIP_Y, this.shipZ)),
                () -> this.onShipBuilt(overworld));
        this.setDirty();
    }

    /** Cada tick del servidor: guion, bases y fortaleza. */
    public void tick(ServerLevel overworld) {
        if (this.stage == IDLE && this.scenes.isEmpty()) {
            return;
        }
        this.timer++;
        Iterator<Scene> it = this.scenes.iterator();
        while (it.hasNext()) {
            Scene s = it.next();
            if (s.at() <= this.timer) {
                it.remove();
                if (s.title() != null) {
                    title(overworld, s.title(), s.sub());
                }
                if (s.chat() != null) {
                    say(overworld, s.chat());
                }
            }
        }
        if (this.stage == BASES) {
            this.nextBaseIn--;
            if (this.nextBaseIn <= 0) {
                if (this.basesBuilt < OUTPOSTS) {
                    this.placeOutpost(overworld);
                } else {
                    this.placeFortress(overworld);
                }
            }
        }
    }

    private void onShipBuilt(ServerLevel level) {
        KeinLooreEntity boss = KeinLooreMod.KEIN_LOREE.get().create(level);
        if (boss != null) {
            Vec3 throne = this.throne();
            boss.moveTo(throne.x, throne.y, throne.z, 0.0F, 0.0F);
            boss.setLoreDormant(true);
            boss.setNoGravity(true);
            level.addFreshEntity(boss);
            this.bossId = boss.getUUID();
        }
        this.stage = BASES;
        this.nextBaseIn = 600;
        this.schedule(0L, "NAVE LOREE", "8 kilómetros de código sólido",
                "Mi trono está listo. Mis lacayos empiezan a decodificar tu mundo.");
        this.setDirty();
    }

    private void placeOutpost(ServerLevel level) {
        Player p = randomPlayer(level);
        if (p == null) {
            this.nextBaseIn = 300;
            return;
        }
        double ang = Math.random() * Math.PI * 2.0D;
        double dist = 70.0D + Math.random() * 50.0D;
        int x = (int) Math.floor(p.getX() + Math.cos(ang) * dist);
        int z = (int) Math.floor(p.getZ() + Math.sin(ang) * dist);
        BlockPos g = groundPos(level, x, z);
        if (g == null) {
            this.nextBaseIn = 200;
            return;
        }
        this.basesBuilt++;
        this.nextBaseIn = BASE_INTERVAL;
        Builds.enqueue(level, Builds.fortress(g, 6, 4), () -> spawnGarrison(level, g, true));
        say(level, "Base militar digital establecida en X " + x + ", Z " + z + ". Ya son " + this.basesBuilt + ".");
        this.setDirty();
    }

    private void placeFortress(ServerLevel level) {
        BlockPos g = groundPos(level, this.shipX, this.shipZ);
        if (g == null) {
            this.nextBaseIn = 200;
            return;
        }
        this.stage = FORTRESS;
        Builds.enqueue(level, Builds.fortress(g, 12, 6), () -> this.onFortressBuilt(level, g));
        say(level, "Fortaleza Loree en construcción bajo mi nave. Preparen el descenso.");
        this.setDirty();
    }

    private void onFortressBuilt(ServerLevel level, BlockPos g) {
        spawnGarrison(level, g, true);
        this.stage = FIGHT;
        Vec3 landing = new Vec3(g.getX() + 0.5D, g.getY(), g.getZ() + 0.5D);
        KeinLooreEntity boss = this.findBoss(level);
        if (boss == null) {
            boss = KeinLooreMod.KEIN_LOREE.get().create(level);
            if (boss == null) {
                return;
            }
            boss.moveTo(landing.x, landing.y + 40.0D, landing.z, 0.0F, 0.0F);
            level.addFreshEntity(boss);
        }
        this.bossId = boss.getUUID();
        boss.beginDescent(landing);
        title(level, "KEIN LOREE", "Desciende a su base");
        say(level, "Bienvenido a mi fortaleza. Aquí termina tu historia... o empieza la mía.");
        this.setDirty();
    }

    // ------------------------------------------------------------------ eventos de combate

    /** El jugador entrego el Nucleo de Decodificacion a Kein Loree: el mundo es consumido. */
    public static void onOfferAccepted(ServerLevel level, KeinLooreEntity boss, Player player) {
        LooreStory s = get(level.getServer().overworld());
        boss.closeOffer();
        s.stage = IDLE;
        s.ending = END_CONSUMED;
        s.scenes.clear();
        boss.retreatToThrone(s.throne());
        title(level, "MUNDO DECODIFICADO", "El Imperio Loree se expande");
        say(level, player.getName().getString() + " aceptó. Tu mundo ya es parte de mi red. Descansa... y mira el cielo.");
        s.setDirty();
    }

    /** Kein Loree murio: resistencia vencedora y recompensa. */
    public static void onBossDeath(KeinLooreEntity boss) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        LooreStory s = get(level.getServer().overworld());
        if (s.bossId == null || !s.bossId.equals(boss.getUUID())) {
            return;
        }
        s.stage = IDLE;
        s.ending = END_RESIST;
        s.scenes.clear();
        s.bossId = null;
        title(level, "KEIN LOREE DERROTADO", "La resistencia ha vencido");
        say(level, "Imposible... mi código se fragmenta. Esto no ha terminado.");
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            p.getInventory().add(new ItemStack(KeinLooreMod.DECODE_CORE.get()));
        }
        s.setDirty();
    }

    /** Un jugador murio en supervivencia durante el combate: final de perecer. */
    public static void onPlayerDeath(ServerPlayer player) {
        ServerLevel overworld = player.getServer().overworld();
        LooreStory s = get(overworld);
        if (s.stage != FIGHT) {
            return;
        }
        s.stage = IDLE;
        s.ending = END_PERISHED;
        s.scenes.clear();
        KeinLooreEntity boss = s.findBoss(overworld);
        if (boss != null) {
            boss.retreatToThrone(s.throne());
        }
        title(overworld, "HAS PERECIDO", "Tu planeta será decodificado");
        say(overworld, player.getName().getString() + " ha perecido. La decodificación continúa sin resistencia.");
        s.setDirty();
    }

    // ------------------------------------------------------------------ utilidades

    private void schedule(long delay, String title, String sub, String chat) {
        this.scenes.add(new Scene(this.timer + delay, title, sub, chat));
    }

    private Vec3 throne() {
        return new Vec3(this.shipX + 0.5D, SHIP_Y + 4.0D, this.shipZ + 0.5D);
    }

    private KeinLooreEntity findBoss(ServerLevel level) {
        if (this.bossId == null) {
            return null;
        }
        Entity e = level.getEntity(this.bossId);
        return e instanceof KeinLooreEntity k && !k.isCopy() ? k : null;
    }

    private static BlockPos groundPos(ServerLevel level, int x, int z) {
        if (!level.hasChunkAt(new BlockPos(x, 64, z))) {
            return null;
        }
        int y = (int) Fx.groundY(level, x + 0.5D, z + 0.5D);
        return new BlockPos(x, y, z);
    }

    private static Player randomPlayer(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        return players.isEmpty() ? null : players.get((int) (Math.random() * players.size()));
    }

    private static void spawnGarrison(ServerLevel level, BlockPos g, boolean commander) {
        for (int i = 0; i < 3; i++) {
            Droid droid = KeinLooreMod.DROID.get().create(level);
            if (droid == null) {
                continue;
            }
            double a = i * (Math.PI * 2.0D / 3.0D);
            droid.moveTo(g.getX() + 0.5D + Math.cos(a) * 3.0D, g.getY(), g.getZ() + 0.5D + Math.sin(a) * 3.0D,
                    (float) (i * 120), 0.0F);
            level.addFreshEntity(droid);
        }
        if (commander) {
            LooreCommander c = KeinLooreMod.LOREE_COMMANDER.get().create(level);
            if (c != null) {
                c.moveTo(g.getX() + 0.5D, g.getY(), g.getZ() + 0.5D, 0.0F, 0.0F);
                level.addFreshEntity(c);
            }
        }
    }

    /** Mensaje de Kein Loree a todos los jugadores del servidor. */
    public static void say(ServerLevel level, String text) {
        Component msg = Component.literal("<Kein Loree> " + text).withStyle(ChatFormatting.GREEN);
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(msg);
        }
    }

    /** Titulo grande en pantalla para todos los jugadores. */
    public static void title(ServerLevel level, String title, String sub) {
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(sub)));
            p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
        }
    }
}
