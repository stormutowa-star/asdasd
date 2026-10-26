package com.keinloree.item;

import java.util.EnumMap;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/** Material de la Armadura de la Sexta Hora y nivel de RareIri. */
public final class KitItems {
    private KitItems() {
    }

    public static final Holder<ArmorMaterial> SEXTA_HORA = Holder.direct(new ArmorMaterial(
            Util.defense(4, 7, 9, 4), 20, SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(Items.NETHERITE_INGOT),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("keinloree", "sexta_hora"))),
            4.0F, 0.15F));

    public static final Tier RAREIRI_TIER = new Tier() {
        @Override
        public int getUses() {
            return 4000;
        }

        @Override
        public float getSpeed() {
            return 9.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 5.0F;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 20;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.NETHERITE_INGOT);
        }
    };

    static final class Util {
        static EnumMap<ArmorItem.Type, Integer> defense(int boots, int legs, int chest, int helmet) {
            EnumMap<ArmorItem.Type, Integer> m = new EnumMap<>(ArmorItem.Type.class);
            m.put(ArmorItem.Type.BOOTS, boots);
            m.put(ArmorItem.Type.LEGGINGS, legs);
            m.put(ArmorItem.Type.CHESTPLATE, chest);
            m.put(ArmorItem.Type.HELMET, helmet);
            m.put(ArmorItem.Type.BODY, chest);
            return m;
        }
    }
}
