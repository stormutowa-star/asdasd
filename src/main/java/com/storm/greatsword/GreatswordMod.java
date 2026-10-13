package com.storm.greatsword;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.core.component.DataComponentType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.mojang.serialization.Codec;
import org.slf4j.Logger;

// Punto de entrada principal del mod.
@Mod(GreatswordMod.MODID)
public class GreatswordMod {
    public static final String MODID = "greatsword";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Registro diferido de items
    public static final DeferredRegister<net.minecraft.world.item.Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // Registro diferido de componentes de datos (estado desenfundado/enfundado)
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    // Registro diferido de pestañas de creativo
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Componente booleano que guarda si la espada esta desenfundada (true) o enfundada (false).
    // Se sincroniza automaticamente al cliente porque esta en el ItemStack.
    public static final RegistryObject<DataComponentType<Boolean>> DRAWN =
            DATA_COMPONENTS.register("drawn", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    // Item de la gran espada
    public static final RegistryObject<net.minecraft.world.item.Item> GREATSWORD =
            ITEMS.register("greatsword", ModItems::greatsword);

    // Pestaña de creativo propia
    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("tab", () ->
            CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.greatsword"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> GREATSWORD.get().getDefaultInstance())
                    .displayItems((params, output) -> output.accept(GREATSWORD.get()))
                    .build());

    public GreatswordMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ITEMS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        TABS.register(modEventBus);

        // Red del mod (canal para desenfundar/enfundar)
        ModNetwork.register(modEventBus);

        // Eventos de cliente (teclas + render) se registran en ClientSetup
        modEventBus.addListener(ClientSetup::onClientSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Greatsword mod cargado. Pulsa Z para desenfundar/enfundar.");
    }
}
