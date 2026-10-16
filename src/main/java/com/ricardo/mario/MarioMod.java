package com.ricardo.mario;

import com.ricardo.mario.client.MarioClient;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(MarioMod.MODID)
public class MarioMod {
    public static final String MODID = "mario";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<EntityType<MarioEntity>> MARIO = ENTITY_TYPES.register("mario", () -> EntityType.Builder
            .of(MarioEntity::new, MobCategory.CREATURE)
            .sized(0.9F, 1.95F)
            .clientTrackingRange(10)
            .build(MODID + ":mario"));

    public static final RegistryObject<Item> MARIO_SPAWN_EGG = ITEMS.register("mario_spawn_egg",
            () -> new MarioSpawnItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("mario_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.mario"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> MARIO_SPAWN_EGG.get().getDefaultInstance())
            .displayItems((parameters, output) -> output.accept(MARIO_SPAWN_EGG.get()))
            .build());

    public MarioMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        TABS.register(modEventBus);
        modEventBus.addListener(this::onEntityAttributes);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MarioClient.register(modEventBus);
        }
    }

    private void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(MARIO.get(), MarioEntity.createAttributes().build());
    }
}
