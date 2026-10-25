package com.keinloree.registry;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.CameraEntity;
import com.keinloree.entity.CargoShip;
import com.keinloree.entity.Droid;
import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.entity.LooreCommander;
import com.keinloree.entity.LooreWarlord;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Tipos de entidad del mod. */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, KeinLooreMod.MODID);

    private static final String ID = KeinLooreMod.MODID + ":";

    /** Kein Loree. MISC: no aparece solo en el mundo; lo invoca la historia o el huevo. */
    public static final RegistryObject<EntityType<KeinLooreEntity>> KEIN_LOREE = ENTITY_TYPES.register("kein_loree", () -> EntityType.Builder
            .of(KeinLooreEntity::new, MobCategory.MISC)
            .sized(1.0F, 2.6F)
            .clientTrackingRange(16)
            .build(ID + "kein_loree"));

    public static final RegistryObject<EntityType<Droid>> DROID = ENTITY_TYPES.register("droid", () -> EntityType.Builder
            .of(Droid::new, MobCategory.MISC)
            .sized(0.6F, 1.9F)
            .clientTrackingRange(12)
            .build(ID + "droid"));

    public static final RegistryObject<EntityType<LooreCommander>> LOREE_COMMANDER = ENTITY_TYPES.register("loree_commander", () -> EntityType.Builder
            .of(LooreCommander::new, MobCategory.MISC)
            .sized(0.9F, 2.4F)
            .clientTrackingRange(14)
            .build(ID + "loree_commander"));

    public static final RegistryObject<EntityType<LooreWarlord>> LOREE_WARLORD = ENTITY_TYPES.register("loree_warlord", () -> EntityType.Builder
            .of(LooreWarlord::new, MobCategory.MISC)
            .sized(1.2F, 2.8F)
            .clientTrackingRange(16)
            .build(ID + "loree_warlord"));

    /** Nave de carga de Kein Loree: vuela del hangar de la nave nodriza hasta las bases. */
    public static final RegistryObject<EntityType<CargoShip>> CARGO_SHIP = ENTITY_TYPES.register("cargo_ship", () -> EntityType.Builder
            .of(CargoShip::new, MobCategory.MISC)
            .sized(8.6F, 4.2F)
            .clientTrackingRange(16)
            .updateInterval(1)
            .build(ID + "cargo_ship"));

    /** Camara cinematica: solo existe en el cliente (no se guarda ni se invoca). */
    public static final RegistryObject<EntityType<CameraEntity>> CAMERA = ENTITY_TYPES.register("camera", () -> EntityType.Builder
            .of(CameraEntity::new, MobCategory.MISC)
            .sized(0.2F, 0.2F)
            .noSave()
            .noSummon()
            .fireImmune()
            .clientTrackingRange(0)
            .build(ID + "camera"));

    private ModEntities() {}

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        modBus.addListener(ModEntities::onAttributes);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(KEIN_LOREE.get(), KeinLooreEntity.createAttributes().build());
        event.put(DROID.get(), Droid.createAttributes().build());
        event.put(LOREE_COMMANDER.get(), LooreCommander.createAttributes().build());
        event.put(LOREE_WARLORD.get(), LooreWarlord.createAttributes().build());
        event.put(CARGO_SHIP.get(), CargoShip.createAttributes().build());
    }
}
