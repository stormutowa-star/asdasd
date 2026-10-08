package com.ricardo.makuosaga.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/** Remembers which landmarks of Vharos were already built. */
public class VharosData extends SavedData {
	public static final String ID = "makuosaga_vharos";

	boolean campBuilt;
	boolean citadelBuilt;

	public static VharosData load(CompoundTag tag) {
		VharosData data = new VharosData();
		data.campBuilt = tag.getBoolean("CampBuilt");
		data.citadelBuilt = tag.getBoolean("CitadelBuilt");
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		tag.putBoolean("CampBuilt", campBuilt);
		tag.putBoolean("CitadelBuilt", citadelBuilt);
		return tag;
	}
}
