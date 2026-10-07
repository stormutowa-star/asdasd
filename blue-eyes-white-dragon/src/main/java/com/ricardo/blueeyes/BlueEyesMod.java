package com.ricardo.blueeyes;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.ricardo.blueeyes.client.ClientSetup;

@Mod(BlueEyesMod.MODID)
public class BlueEyesMod {
    public static final String MODID = "blueeyes";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    /** La carta fisica. Clic derecho sobre el campo para invocar. */
    public static final RegistryObject<Item> CARD = ITEMS.register("blue_eyes_card", () -> new CardItem(
            new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<EntityType<BlueEyesDragon>> DRAGON = ENTITIES.register("blue_eyes_white_dragon",
            () -> EntityType.Builder.<BlueEyesDragon>of(BlueEyesDragon::new, MobCategory.MISC)
                    .sized(2.8F, 5.0F)
                    .eyeHeight(4.6F)
                    .fireImmune()
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("blue_eyes_white_dragon"));

    /** La carta boca arriba sobre el terreno (ancla de la invocacion). */
    public static final RegistryObject<EntityType<FieldCardEntity>> FIELD_CARD = ENTITIES.register("field_card",
            () -> EntityType.Builder.<FieldCardEntity>of(FieldCardEntity::new, MobCategory.MISC)
                    .sized(0.95F, 0.16F)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build("field_card"));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("blueeyes_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.blueeyes"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> CARD.get().getDefaultInstance())
            .displayItems((parameters, output) -> output.accept(CARD.get()))
            .build());

    public BlueEyesMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        TABS.register(modEventBus);
        modEventBus.addListener(BlueEyesMod::onAttributes);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ClientSetup::registerRenderers);
            modEventBus.addListener(ClientSetup::registerLayers);
        }
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(DRAGON.get(), BlueEyesDragon.createAttributes().build());
    }
}
