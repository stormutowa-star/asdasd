package com.keinloree.registry;

import com.keinloree.KeinLooreMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

/** Materiales de armadura (la armadura de la Sexta Hora llegara en el WP10). */
public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, KeinLooreMod.MODID);

    private ModArmorMaterials() {}

    public static void register(IEventBus modBus) {
        MATERIALS.register(modBus);
    }
}
