package com.keinloree.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;

/** RareIri, la hoja que corta las horas. Sus tecnicas viven en ability/Techniques. */
public class RareIriItem extends SwordItem {
    public RareIriItem(Properties props) {
        super(KitItems.RAREIRI_TIER, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.keinloree.rareiri.desc").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        lines.add(Component.translatable("item.keinloree.rareiri.keys").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }
}
