package com.keinloree.story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import javax.annotation.Nullable;

/**
 * Estado persistente de la campana ("keinloree_campaign" en el Overworld).
 * Todo lo que la historia necesita recordar tras reiniciar vive aqui.
 */
public class Campaign extends SavedData {
    public static final String NAME = "keinloree_campaign";
    public static final int VERSION = 1;

    /** Ruta elegida por el heroe. */
    public enum Path { NONE, AMANECER, NEXO }

    /** Lo que la historia recuerda de cada jugador. */
    public static final class PlayerTale {
        /** Tecnicas desbloqueadas (mascara de bits). */
        public int techniques;
        public boolean legionAlly;
        public int deaths;
        public int kills;
        public boolean receivedGear;
    }

    public int version = VERSION;
    public boolean disabled;
    /** Ticks de mundo; solo avanzan con al menos un jugador en el Overworld. */
    public long worldTicks;
    public String step = "";
    public long stepStart;
    public final Set<String> flags = new LinkedHashSet<>();
    public final Map<String, Integer> counters = new LinkedHashMap<>();
    public final Map<String, BlockPos> positions = new LinkedHashMap<>();
    public final Map<String, UUID> entities = new LinkedHashMap<>();
    @Nullable
    public UUID hero;
    public Path path = Path.NONE;
    public final Map<String, String> choices = new LinkedHashMap<>();
    public final Map<UUID, PlayerTale> players = new HashMap<>();

    // Estado del reproductor de escenas (ScenePlayer): escena actual, siguiente instruccion y cola
    public String sceneId = "";
    public int scenePc;
    public final List<String> sceneQueue = new ArrayList<>();

    public long nextWhisperAt;
    public long nextUneaseAt;

    // Ultima atmosfera enviada (para reenviarla al reconectar)
    public float atmoTint;
    public float atmoFog;
    public int atmoFlags;

    public static Campaign get(ServerLevel overworld) {
        return get(overworld.getServer());
    }

    public static Campaign get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(Campaign::new, Campaign::load, null), NAME);
    }

    // ---- ayudantes ----

    public boolean hasFlag(String f) {
        return flags.contains(f);
    }

    /** Marca la bandera; devuelve true si era nueva. */
    public boolean setFlag(String f) {
        boolean added = flags.add(f);
        if (added) {
            setDirty();
        }
        return added;
    }

    public void clearFlag(String f) {
        if (flags.remove(f)) {
            setDirty();
        }
    }

    public int counter(String key) {
        return counters.getOrDefault(key, 0);
    }

    public int inc(String key, int by) {
        int v = counter(key) + by;
        counters.put(key, v);
        setDirty();
        return v;
    }

    public void setCounter(String key, int value) {
        counters.put(key, value);
        setDirty();
    }

    public PlayerTale tale(UUID player) {
        return players.computeIfAbsent(player, k -> new PlayerTale());
    }

    /** Vuelve al principio de la historia (conserva solo "disabled"). */
    public void resetRun() {
        step = "";
        stepStart = worldTicks;
        flags.clear();
        counters.clear();
        positions.clear();
        entities.clear();
        hero = null;
        path = Path.NONE;
        choices.clear();
        players.clear();
        sceneId = "";
        scenePc = 0;
        sceneQueue.clear();
        nextWhisperAt = 0;
        nextUneaseAt = 0;
        atmoTint = 0;
        atmoFog = 0;
        atmoFlags = 0;
        setDirty();
    }

    // ---- NBT ----

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("version", version);
        tag.putBoolean("disabled", disabled);
        tag.putLong("worldTicks", worldTicks);
        tag.putString("step", step);
        tag.putLong("stepStart", stepStart);
        ListTag fl = new ListTag();
        for (String f : flags) {
            fl.add(StringTag.valueOf(f));
        }
        tag.put("flags", fl);
        CompoundTag co = new CompoundTag();
        counters.forEach(co::putInt);
        tag.put("counters", co);
        CompoundTag po = new CompoundTag();
        positions.forEach((k, v) -> po.putLong(k, v.asLong()));
        tag.put("positions", po);
        CompoundTag en = new CompoundTag();
        entities.forEach((k, v) -> en.putUUID(k, v));
        tag.put("entities", en);
        if (hero != null) {
            tag.putUUID("hero", hero);
        }
        tag.putString("path", path.name());
        CompoundTag ch = new CompoundTag();
        choices.forEach(ch::putString);
        tag.put("choices", ch);
        CompoundTag pl = new CompoundTag();
        players.forEach((id, t) -> {
            CompoundTag p = new CompoundTag();
            p.putInt("techniques", t.techniques);
            p.putBoolean("legionAlly", t.legionAlly);
            p.putInt("deaths", t.deaths);
            p.putInt("kills", t.kills);
            p.putBoolean("receivedGear", t.receivedGear);
            pl.put(id.toString(), p);
        });
        tag.put("players", pl);
        tag.putString("sceneId", sceneId);
        tag.putInt("scenePc", scenePc);
        ListTag q = new ListTag();
        for (String s : sceneQueue) {
            q.add(StringTag.valueOf(s));
        }
        tag.put("sceneQueue", q);
        tag.putLong("nextWhisperAt", nextWhisperAt);
        tag.putLong("nextUneaseAt", nextUneaseAt);
        tag.putFloat("atmoTint", atmoTint);
        tag.putFloat("atmoFog", atmoFog);
        tag.putInt("atmoFlags", atmoFlags);
        return tag;
    }

    public static Campaign load(CompoundTag tag, HolderLookup.Provider registries) {
        Campaign c = new Campaign();
        c.version = tag.contains("version") ? tag.getInt("version") : VERSION;
        c.disabled = tag.getBoolean("disabled");
        c.worldTicks = tag.getLong("worldTicks");
        c.step = tag.getString("step");
        c.stepStart = tag.getLong("stepStart");
        for (Tag t : tag.getList("flags", Tag.TAG_STRING)) {
            c.flags.add(t.getAsString());
        }
        CompoundTag co = tag.getCompound("counters");
        for (String k : co.getAllKeys()) {
            c.counters.put(k, co.getInt(k));
        }
        CompoundTag po = tag.getCompound("positions");
        for (String k : po.getAllKeys()) {
            c.positions.put(k, BlockPos.of(po.getLong(k)));
        }
        CompoundTag en = tag.getCompound("entities");
        for (String k : en.getAllKeys()) {
            if (en.hasUUID(k)) {
                c.entities.put(k, en.getUUID(k));
            }
        }
        c.hero = tag.hasUUID("hero") ? tag.getUUID("hero") : null;
        try {
            c.path = Path.valueOf(tag.getString("path"));
        } catch (IllegalArgumentException e) {
            c.path = Path.NONE;
        }
        CompoundTag ch = tag.getCompound("choices");
        for (String k : ch.getAllKeys()) {
            c.choices.put(k, ch.getString(k));
        }
        CompoundTag pl = tag.getCompound("players");
        for (String k : pl.getAllKeys()) {
            try {
                CompoundTag p = pl.getCompound(k);
                PlayerTale t = new PlayerTale();
                t.techniques = p.getInt("techniques");
                t.legionAlly = p.getBoolean("legionAlly");
                t.deaths = p.getInt("deaths");
                t.kills = p.getInt("kills");
                t.receivedGear = p.getBoolean("receivedGear");
                c.players.put(UUID.fromString(k), t);
            } catch (IllegalArgumentException ignored) {
                // clave de jugador corrupta: se descarta
            }
        }
        c.sceneId = tag.getString("sceneId");
        c.scenePc = tag.getInt("scenePc");
        for (Tag t : tag.getList("sceneQueue", Tag.TAG_STRING)) {
            c.sceneQueue.add(t.getAsString());
        }
        c.nextWhisperAt = tag.getLong("nextWhisperAt");
        c.nextUneaseAt = tag.getLong("nextUneaseAt");
        c.atmoTint = tag.getFloat("atmoTint");
        c.atmoFog = tag.getFloat("atmoFog");
        c.atmoFlags = tag.getInt("atmoFlags");
        return c;
    }
}
