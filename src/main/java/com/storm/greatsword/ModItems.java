package com.storm.greatsword;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class ModItems {
    private ModItems() {}

    public static Item greatsword() {
        return new GreatswordItem(
                GreatswordTier.INSTANCE,
                new Item.Properties()
                        .stacksTo(1)
                        .durability(2031)
                        .rarity(Rarity.RARE)
                        .fireResistant()
                        .attributes(GreatswordItem.createAttributes(false))
        );
    }
}
