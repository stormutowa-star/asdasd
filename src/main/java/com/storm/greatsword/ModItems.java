package com.storm.greatsword;

import net.minecraft.world.item.Item;

// Fabrica del item de la gran espada.
public final class ModItems {
    private ModItems() {}

    public static Item greatsword() {
        return new GreatswordItem(
                // El atributo de durabilidad y tier se aplica en GreatswordItem via properties
                GreatswordTier.INSTANCE,
                new Item.Properties()
                        .stacksTo(1)
                        .durability(2500)
                        .rarity(net.minecraft.world.item.Rarity.UNCOMMON)
                        .fireResistant()
        );
    }
}
