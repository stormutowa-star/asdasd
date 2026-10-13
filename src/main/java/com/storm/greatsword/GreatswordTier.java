package com.storm.greatsword;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;

// Nivel (tier) de la gran espada. Firma de ForgeTier en 1.21.1:
// (int uses, float speed, float attackDamageBonus, int enchantmentValue,
//  TagKey<Block> tag, Supplier<Ingredient> repair, TagKey<Block> incorrectBlocks)
public final class GreatswordTier {
    private GreatswordTier() {}

    public static final Tier INSTANCE = new ForgeTier(
            2500,                                   // usos (durabilidad)
            8.0F,                                   // velocidad de minado
            8.0F,               // bonus de danio base (la espada es muy cortante)
            18,                                     // valor de encantamiento
            BlockTags.NEEDS_IRON_TOOL,              // herramientas que necesita para minar
            () -> Ingredient.of(net.minecraft.world.item.Items.NETHERITE_INGOT),
            BlockTags.INCORRECT_FOR_IRON_TOOL       // bloques que mina mal (evita eficiencia rara)
    );
}
