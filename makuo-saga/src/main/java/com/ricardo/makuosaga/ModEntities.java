package com.ricardo.makuosaga;

import com.dragonminez.common.init.entities.sagas.DBSagasEntity;
import com.ricardo.makuosaga.entity.MakuoSagaEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/**
 * Entity ids double as asset names: DragonMineZ's saga renderer loads
 * dragonminez:textures/entity/sagas/&lt;id path&gt;.png, so every path here has a texture of the same name.
 */
public final class ModEntities {
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MakuoSaga.MOD_ID);

	public static final RegistryObject<EntityType<MakuoSagaEntities.Zhar>> ZHAR =
			register("saga_zhar", MakuoSagaEntities.Zhar::new, 0.6f, 2.0f);
	public static final RegistryObject<EntityType<MakuoSagaEntities.ZharElite>> ZHAR_ELITE =
			register("saga_zhar_elite", MakuoSagaEntities.ZharElite::new, 0.6f, 2.0f);
	public static final RegistryObject<EntityType<MakuoSagaEntities.Garuk>> GARUK =
			register("saga_garuk", MakuoSagaEntities.Garuk::new, 0.9f, 2.7f);
	public static final RegistryObject<EntityType<MakuoSagaEntities.Seiryn>> SEIRYN =
			register("saga_seiryn", MakuoSagaEntities.Seiryn::new, 0.7f, 2.2f);
	public static final RegistryObject<EntityType<MakuoSagaEntities.Vokkar>> VOKKAR =
			register("saga_vokkar", MakuoSagaEntities.Vokkar::new, 0.6f, 2.1f);
	public static final RegistryObject<EntityType<MakuoSagaEntities.Drakhul>> DRAKHUL =
			register("saga_drakhul", MakuoSagaEntities.Drakhul::new, 0.9f, 2.6f);
	public static final RegistryObject<EntityType<MakuoSagaEntities.DrakhulGiant>> DRAKHUL_GIANT =
			register("saga_drakhul_giant", MakuoSagaEntities.DrakhulGiant::new, 2.8f, 8.5f);
	public static final RegistryObject<EntityType<MakuoSagaEntities.Makuo>> MAKUO =
			register("saga_makuo", MakuoSagaEntities.Makuo::new, 0.7f, 2.2f);
	public static final RegistryObject<EntityType<MakuoSagaEntities.MakuoWings>> MAKUO_WINGS =
			register("saga_makuo_wings", MakuoSagaEntities.MakuoWings::new, 0.8f, 2.4f);

	private ModEntities() {
	}

	public static List<RegistryObject<? extends EntityType<? extends DBSagasEntity>>> all() {
		return List.of(ZHAR, ZHAR_ELITE, GARUK, SEIRYN, VOKKAR, DRAKHUL, DRAKHUL_GIANT, MAKUO, MAKUO_WINGS);
	}

	private static <T extends DBSagasEntity> RegistryObject<EntityType<T>> register(String name, EntityType.EntityFactory<T> factory,
																					 float width, float height) {
		return ENTITY_TYPES.register(name, () -> EntityType.Builder.of(factory, MobCategory.MONSTER)
				.sized(width, height)
				.clientTrackingRange(10)
				.build(MakuoSaga.MOD_ID + ":" + name));
	}

	static void registerAttributes(EntityAttributeCreationEvent event) {
		AttributeSupplier attributes = DBSagasEntity.createAttributes().build();
		for (var type : all()) {
			event.put(type.get(), attributes);
		}
	}
}
