package com.keinloree.ship;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Registro persistente del destructor ("keinloree_ship" en el Overworld). Sirve aunque la entidad no este
 * cargada: proteccion del casco, comandos, jugadores desconectados en vuelo y recuperacion si alguien la borra.
 */
public class ShipData extends SavedData {
    public static final String NAME = "keinloree_ship";

    @Nullable
    public UUID ship;
    public int state = -1;
    public BlockPos anchor = BlockPos.ZERO;
    public int rot;
    public Vec3 pos = Vec3.ZERO;
    public float yaw;
    /** Jugadores que se desconectaron a bordo: x, y, z, yaw locales. */
    public final Map<UUID, float[]> offline = new LinkedHashMap<>();

    @Nullable
    private AABB box;

    public static ShipData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ShipData::new, ShipData::load, null), NAME);
    }

    /** Hay bloques del diseno en el mundo (protegidos). */
    public boolean hasBlocks() {
        return ship != null && DestroyerEntity.blocksPresent(state);
    }

    /** Caja de la nave anclada (null si no hay bloques). */
    @Nullable
    public AABB protectedBox() {
        if (!hasBlocks()) {
            return null;
        }
        if (box == null) {
            box = ShipTransform.worldAABB(anchor, rot);
        }
        return box;
    }

    public void update(UUID id, int state, BlockPos anchor, int rot, Vec3 pos, float yaw) {
        boolean changed = !id.equals(ship) || this.state != state || !anchor.equals(this.anchor) || this.rot != rot;
        this.ship = id;
        this.state = state;
        this.anchor = anchor.immutable();
        this.rot = rot;
        this.pos = pos;
        this.yaw = yaw;
        if (changed) {
            box = null;
        }
        setDirty();
    }

    public void clear() {
        ship = null;
        state = -1;
        offline.clear();
        box = null;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        if (ship != null) {
            tag.putUUID("ship", ship);
        }
        tag.putInt("state", state);
        tag.putLong("anchor", anchor.asLong());
        tag.putInt("rot", rot);
        tag.putDouble("px", pos.x);
        tag.putDouble("py", pos.y);
        tag.putDouble("pz", pos.z);
        tag.putFloat("yaw", yaw);
        CompoundTag off = new CompoundTag();
        offline.forEach((k, v) -> {
            CompoundTag o = new CompoundTag();
            o.putFloat("x", v[0]);
            o.putFloat("y", v[1]);
            o.putFloat("z", v[2]);
            o.putFloat("yaw", v[3]);
            off.put(k.toString(), o);
        });
        tag.put("offline", off);
        return tag;
    }

    public static ShipData load(CompoundTag tag, HolderLookup.Provider registries) {
        ShipData d = new ShipData();
        d.ship = tag.hasUUID("ship") ? tag.getUUID("ship") : null;
        d.state = tag.getInt("state");
        d.anchor = BlockPos.of(tag.getLong("anchor"));
        d.rot = tag.getInt("rot");
        d.pos = new Vec3(tag.getDouble("px"), tag.getDouble("py"), tag.getDouble("pz"));
        d.yaw = tag.getFloat("yaw");
        CompoundTag off = tag.getCompound("offline");
        for (String k : off.getAllKeys()) {
            try {
                CompoundTag o = off.getCompound(k);
                d.offline.put(UUID.fromString(k), new float[]{o.getFloat("x"), o.getFloat("y"), o.getFloat("z"), o.getFloat("yaw")});
            } catch (IllegalArgumentException ignored) {
                // clave corrupta: se descarta
            }
        }
        return d;
    }
}
