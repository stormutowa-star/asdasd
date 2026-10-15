package com.ricardo.jugador2.npc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NpcSavedDataTest {
    @Test
    void bodyPositionAndHealthSurviveSaveAndLoad() {
        NpcSavedData data = NpcSavedData.load(new CompoundTag(), null);
        data.saveBody("minecraft:the_nether", new Vec3(1.5, 70.25, -3.5), 13.5F);

        NpcSavedData loaded = NpcSavedData.load(data.save(new CompoundTag(), null), null);

        assertEquals("minecraft:the_nether", loaded.bodyDimension());
        assertEquals(1.5, loaded.bodyPos().x);
        assertEquals(70.25, loaded.bodyPos().y);
        assertEquals(-3.5, loaded.bodyPos().z);
        assertEquals(13.5F, loaded.bodyHealth());
    }

    @Test
    void noBodySavedLoadsWithoutPosition() {
        assertNull(NpcSavedData.load(new CompoundTag(), null).bodyPos());
    }
}
