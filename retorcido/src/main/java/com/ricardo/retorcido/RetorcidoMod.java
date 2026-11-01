package com.ricardo.retorcido;

import com.ricardo.retorcido.amigo.AmigoEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(RetorcidoMod.MODID)
public class RetorcidoMod {
    public static final String MODID = "retorcido";

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<EntityType<RetorcidoEntity>> RETORCIDO = ENTITY_TYPES.register("retorcido", () ->
            EntityType.Builder.of(RetorcidoEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.0F)
                    .clientTrackingRange(8)
                    .build("retorcido"));

    public static final RegistryObject<EntityType<AmigoEntity>> AMIGO = ENTITY_TYPES.register("amigo", () ->
            EntityType.Builder.of(AmigoEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .build("amigo"));

    public static final RegistryObject<Item> AMIGO_SPAWN_EGG = ITEMS.register("amigo_spawn_egg", () ->
            new ForgeSpawnEggItem(AMIGO, 0x3a7bd5, 0x2b2b2b, new Item.Properties()));

    public static final RegistryObject<Item> RETORCIDO_SPAWN_EGG = ITEMS.register("retorcido_spawn_egg", () ->
            new ForgeSpawnEggItem(RETORCIDO, 0x1a0f12, 0x8b0000, new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("retorcido_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.retorcido"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> RETORCIDO_SPAWN_EGG.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(RETORCIDO_SPAWN_EGG.get());
                output.accept(AMIGO_SPAWN_EGG.get());
            })
            .build());

    public RetorcidoMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ENTITY_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
    }
}
