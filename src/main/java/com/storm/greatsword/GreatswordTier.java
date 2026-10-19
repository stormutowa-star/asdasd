package com.storm.greatsword;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeTier;

/**
 * Iron-tier greatsword: slow, durable and made for heavy hits. It is repaired with iron ingots.
 */
public final class GreatswordTier {
    private GreatswordTier() {}

    public static final Tier INSTANCE = new ForgeTier(
            2031,                                  // durability
            4.0F,                                  // mining speed
            8.0F,                                  // attack damage bonus
            15,                                    // enchantment value
            BlockTags.NEEDS_IRON_TOOL,
            () -> Ingredient.of(Items.IRON_INGOT),
            BlockTags.INCORRECT_FOR_IRON_TOOL
    );
}
