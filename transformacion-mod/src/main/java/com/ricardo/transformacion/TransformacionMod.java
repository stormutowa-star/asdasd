package com.ricardo.transformacion;

import com.ricardo.transformacion.TransformEvents;
import com.ricardo.transformacion.TransformItem;
import com.ricardo.transformacion.TransformNetwork;
import com.ricardo.transformacion.client.TransformClient;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

@Mod(value="transformacion")
public class TransformacionMod {
    public static final String MODID = "transformacion";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "transformacion");
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "transformacion");
    public static final RegistryObject<Item> TRANSFORMACION = ITEMS.register("transformacion", () -> new TransformItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("transformacion_tab", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.transformacion")).withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> TRANSFORMACION.get().getDefaultInstance()).displayItems((parameters, output) -> output.accept(TRANSFORMACION.get())).build());

    public TransformacionMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ITEMS.register(modBus);
        TABS.register(modBus);
        TransformNetwork.register();
        MinecraftForge.EVENT_BUS.register(new TransformEvents());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            TransformClient.register(modBus);
        }
    }
}

