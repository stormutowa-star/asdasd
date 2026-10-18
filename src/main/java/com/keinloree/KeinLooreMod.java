package com.keinloree;

import com.keinloree.client.KeinLooreClient;
import com.keinloree.entity.Droid;
import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.entity.LooreCommander;
import com.keinloree.entity.LooreWarlord;
import com.keinloree.events.LooreEvents;
import com.keinloree.item.DecodeCoreItem;
import com.keinloree.item.LooreEggItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Kein Loree: villano de Minecraft con mision RPG, copias, campo digital y forma final con alas. */
@Mod(KeinLooreMod.MODID)
public class KeinLooreMod {
    public static final String MODID = "keinloree";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    /** Kein Loree. MISC: no aparece solo en el mundo; lo invoca la historia o el huevo. */
    public static final RegistryObject<EntityType<KeinLooreEntity>> KEIN_LOREE = ENTITY_TYPES.register("kein_loree", () -> EntityType.Builder
            .of(KeinLooreEntity::new, MobCategory.MISC)
            .sized(1.0F, 2.6F)
            .clientTrackingRange(16)
            .build(MODID + ":kein_loree"));

    public static final RegistryObject<EntityType<Droid>> DROID = ENTITY_TYPES.register("droid", () -> EntityType.Builder
            .of(Droid::new, MobCategory.MISC)
            .sized(0.6F, 1.9F)
            .clientTrackingRange(12)
            .build(MODID + ":droid"));

    public static final RegistryObject<EntityType<LooreCommander>> LOREE_COMMANDER = ENTITY_TYPES.register("loree_commander", () -> EntityType.Builder
            .of(LooreCommander::new, MobCategory.MISC)
            .sized(0.9F, 2.4F)
            .clientTrackingRange(14)
            .build(MODID + ":loree_commander"));

    public static final RegistryObject<EntityType<LooreWarlord>> LOREE_WARLORD = ENTITY_TYPES.register("loree_warlord", () -> EntityType.Builder
            .of(LooreWarlord::new, MobCategory.MISC)
            .sized(1.2F, 2.8F)
            .clientTrackingRange(16)
            .build(MODID + ":loree_warlord"));

    /** Nucleo de Decodificacion: activa la señal (usar) y acepta la oferta de Kein Loree (clic sobre el). */
    public static final RegistryObject<Item> DECODE_CORE = ITEMS.register("decode_core",
            () -> new DecodeCoreItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> KEIN_LOREE_EGG = ITEMS.register("kein_loree_egg",
            () -> new LooreEggItem(() -> KEIN_LOREE.get(), new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> DROID_EGG = ITEMS.register("droid_egg",
            () -> new LooreEggItem(() -> DROID.get(), new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> COMMANDER_EGG = ITEMS.register("commander_egg",
            () -> new LooreEggItem(() -> LOREE_COMMANDER.get(), new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> WARLORD_EGG = ITEMS.register("warlord_egg",
            () -> new LooreEggItem(() -> LOREE_WARLORD.get(), new Item.Properties().stacksTo(16)));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("keinloree_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.keinloree"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> DECODE_CORE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(DECODE_CORE.get());
                output.accept(KEIN_LOREE_EGG.get());
                output.accept(DROID_EGG.get());
                output.accept(COMMANDER_EGG.get());
                output.accept(WARLORD_EGG.get());
            })
            .build());

    public KeinLooreMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ITEMS.register(modBus);
        ENTITY_TYPES.register(modBus);
        TABS.register(modBus);
        modBus.addListener(this::onEntityAttributes);

        MinecraftForge.EVENT_BUS.register(new LooreEvents());

        if (FMLEnvironment.dist == Dist.CLIENT) {
            KeinLooreClient.register(modBus);
        }
    }

    private void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(KEIN_LOREE.get(), KeinLooreEntity.createAttributes().build());
        event.put(DROID.get(), Droid.createAttributes().build());
        event.put(LOREE_COMMANDER.get(), LooreCommander.createAttributes().build());
        event.put(LOREE_WARLORD.get(), LooreWarlord.createAttributes().build());
    }
}
