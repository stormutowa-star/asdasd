package com.ricardo.jugador2.npc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.UUID;

/**
 * Datos del NPC guardados en el overworld: identidad (nombre y UUID, fijados una vez por mundo)
 * y estado entre reinicios.
 */
public final class NpcSavedData extends SavedData {
    public static final String ID = "jugador2_npc";

    private String name;
    private UUID uuid;
    private boolean active;
    private NpcLifecycle.Phase phase = NpcLifecycle.Phase.INACTIVE;
    private int ticksUntilRespawn;
    private String respawnDimension;
    private BlockPos respawnPos;

    private NpcSavedData() {
    }

    public static SavedData.Factory<NpcSavedData> factory() {
        return new SavedData.Factory<>(NpcSavedData::new, NpcSavedData::load, null);
    }

    public String name() {
        return name;
    }

    public UUID uuid() {
        return uuid;
    }

    public boolean active() {
        return active;
    }

    public NpcLifecycle.Phase phase() {
        return phase;
    }

    public int ticksUntilRespawn() {
        return ticksUntilRespawn;
    }

    public String respawnDimension() {
        return respawnDimension;
    }

    public BlockPos respawnPos() {
        return respawnPos;
    }

    /** Fija la identidad del NPC en este mundo. Se llama una sola vez, en el primer arranque. */
    public void initialize(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
        setDirty();
    }

    public void setActive(boolean active) {
        this.active = active;
        setDirty();
    }

    public void saveLifecycle(NpcLifecycle lifecycle) {
        this.phase = lifecycle.phase();
        this.ticksUntilRespawn = lifecycle.ticksUntilRespawn();
        setDirty();
    }

    public void setRespawnPoint(String dimension, BlockPos pos) {
        this.respawnDimension = dimension;
        this.respawnPos = pos;
        setDirty();
    }

    private static NpcSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        NpcSavedData data = new NpcSavedData();
        if (tag.contains("name")) {
            data.name = tag.getString("name");
        }
        if (tag.hasUUID("uuid")) {
            data.uuid = tag.getUUID("uuid");
        }
        data.active = tag.getBoolean("active");
        if (tag.contains("phase")) {
            data.phase = NpcLifecycle.Phase.valueOf(tag.getString("phase"));
        }
        data.ticksUntilRespawn = tag.getInt("ticksUntilRespawn");
        if (tag.contains("respawnDimension")) {
            data.respawnDimension = tag.getString("respawnDimension");
            int[] pos = tag.getIntArray("respawnPos");
            data.respawnPos = new BlockPos(pos[0], pos[1], pos[2]);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        if (name != null) {
            tag.putString("name", name);
        }
        if (uuid != null) {
            tag.putUUID("uuid", uuid);
        }
        tag.putBoolean("active", active);
        tag.putString("phase", phase.name());
        tag.putInt("ticksUntilRespawn", ticksUntilRespawn);
        if (respawnDimension != null && respawnPos != null) {
            tag.putString("respawnDimension", respawnDimension);
            tag.putIntArray("respawnPos", new int[] {respawnPos.getX(), respawnPos.getY(), respawnPos.getZ()});
        }
        return tag;
    }
}
