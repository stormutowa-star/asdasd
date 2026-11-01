package com.ricardo.retorcido.terror;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Progreso de la historia por jugador, guardado en el overworld ("retorcido_terror"). */
public class TerrorData extends SavedData {
    public static final String NAME = "retorcido_terror";

    public static final class PlayerData {
        public long playTicks;
        /** -1 = ninguna; 1..5 = fase forzada. */
        public int forcedPhase = -1;
        public boolean abducted;
        public boolean returned;
        public long huntCooldownUntilDay;
        public long lastHuntDay = -1;
        /** Tiempo de juego (gameTime) en el que re-crear a Dani tras morir; 0 = nada pendiente. */
        public long daniRespawnAt;

        /** Fase actual (1-5). */
        public int phase() {
            if (forcedPhase >= 1) return Math.min(5, forcedPhase);
            if (abducted) return 5;
            if (playTicks >= TerrorConfig.PHASE4_TICKS) return 4;
            if (playTicks >= TerrorConfig.PHASE3_TICKS) return 3;
            if (playTicks >= TerrorConfig.PHASE2_TICKS) return 2;
            return 1;
        }
    }

    private final Map<UUID, PlayerData> players = new HashMap<>();

    public static SavedData.Factory<TerrorData> factory() {
        return new SavedData.Factory<>(TerrorData::new, TerrorData::load, null);
    }

    public static TerrorData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), NAME);
    }

    /** Datos del jugador; los crea si no existen. */
    public PlayerData get(UUID id) {
        return players.computeIfAbsent(id, k -> new PlayerData());
    }

    public void remove(UUID id) {
        players.remove(id);
        setDirty();
    }

    public static TerrorData load(CompoundTag tag, HolderLookup.Provider provider) {
        TerrorData data = new TerrorData();
        ListTag list = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            if (!t.hasUUID("id")) continue;
            PlayerData p = new PlayerData();
            p.playTicks = t.getLong("playTicks");
            p.forcedPhase = t.contains("forcedPhase") ? t.getInt("forcedPhase") : -1;
            p.abducted = t.getBoolean("abducted");
            p.returned = t.getBoolean("returned");
            p.huntCooldownUntilDay = t.getLong("huntCooldownUntilDay");
            p.lastHuntDay = t.contains("lastHuntDay") ? t.getLong("lastHuntDay") : -1;
            p.daniRespawnAt = t.getLong("daniRespawnAt");
            data.players.put(t.getUUID("id"), p);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, PlayerData> e : players.entrySet()) {
            PlayerData p = e.getValue();
            CompoundTag t = new CompoundTag();
            t.putUUID("id", e.getKey());
            t.putLong("playTicks", p.playTicks);
            t.putInt("forcedPhase", p.forcedPhase);
            t.putBoolean("abducted", p.abducted);
            t.putBoolean("returned", p.returned);
            t.putLong("huntCooldownUntilDay", p.huntCooldownUntilDay);
            t.putLong("lastHuntDay", p.lastHuntDay);
            t.putLong("daniRespawnAt", p.daniRespawnAt);
            list.add(t);
        }
        tag.put("players", list);
        return tag;
    }
}
