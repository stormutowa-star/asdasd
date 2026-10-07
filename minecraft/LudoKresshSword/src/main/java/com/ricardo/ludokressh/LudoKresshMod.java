package com.ricardo.ludokressh;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(LudoKresshMod.MODID)
public class LudoKresshMod {
    public static final String MODID = "ludokressh";

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<MobEffect> SITH_POISON = EFFECTS.register("sith_poison", SithPoisonEffect::new);
    public static final RegistryObject<MobEffect> SITH_TOXIN = EFFECTS.register("sith_toxin", SithToxinEffect::new);

    // Mismas stats que en Skyrim: daño 24 y velocidad normal de espada.
    // Daño mostrado = 1 (mano) + 4 (nivel netherita) + 19 = 24. Velocidad: 4 - 2.4 = 1.6 (la de cualquier espada).
    // Las armas de Skyrim no se rompen, así que es irrompible.
    public static final RegistryObject<Item> LUDO_KRESSH_SWORD = ITEMS.register("ludo_kressh_sword", () -> new LudoKresshSwordItem(
            new Item.Properties()
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                    .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 19, -2.4F))));

    public LudoKresshMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        EFFECTS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(LudoKresshMod::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(LUDO_KRESSH_SWORD.get());
        }
    }
}
