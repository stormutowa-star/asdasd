package com.lenadorbosque;

import com.lenadorbosque.entity.DeadDeerEntity;
import com.lenadorbosque.entity.WoodcutterEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, LenadorBosque.MODID);

    public static final RegistryObject<EntityType<WoodcutterEntity>> WOODCUTTER = ENTITIES.register("woodcutter",
            () -> EntityType.Builder.of(WoodcutterEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build("woodcutter"));

    public static final RegistryObject<EntityType<DeadDeerEntity>> DEAD_DEER = ENTITIES.register("dead_deer",
            () -> EntityType.Builder.of(DeadDeerEntity::new, MobCategory.CREATURE)
                    .sized(1.2F, 0.9F)
                    .build("dead_deer"));

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(WOODCUTTER.get(), WoodcutterEntity.createAttributes().build());
        event.put(DEAD_DEER.get(), DeadDeerEntity.createAttributes().build());
    }

    private ModEntities() {}
}
