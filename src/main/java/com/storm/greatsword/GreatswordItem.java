package com.storm.greatsword;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

// La gran espada. Al desenfundarla se sostiene con dos manos: el ataque se vuelve
// mucho mas potente pero mas lento, y ocupa la mano secundaria (no puedes tener nada ahi).
//
// En 1.21.1 los atributos de un item son un COMPONENTE (ATTRIBUTE_MODIFIERS) del ItemStack,
// asi que los intercambiamos directamente al alternar el estado desenfundado/enfundado.
public class GreatswordItem extends SwordItem {

    // Estado desenfundado/enfundado guardado en el componente DRAWN del propio stack.
    public static boolean isDrawn(ItemStack stack) {
        Boolean drawn = stack.get(GreatswordMod.DRAWN.get());
        return Boolean.TRUE.equals(drawn);
    }

    public static void setDrawn(ItemStack stack, boolean drawn) {
        stack.set(GreatswordMod.DRAWN.get(), drawn);
        // Atributos de dos manos solo al desenfundar: +danio y -velocidad de ataque.
        ItemAttributeModifiers mods = SwordItem.createAttributes(
                GreatswordTier.INSTANCE,
                drawn ? 8 : 0,     // danio extra con dos manos
                drawn ? -2.6F : -2.4F  // mas lento al desenfundar
        );
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, mods);
    }

    public GreatswordItem(net.minecraft.world.item.Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean drawn = isDrawn(stack);

        if (!drawn) {
            // Para desenfundar, la mano secundaria debe estar libre (arma a dos manos).
            if (!player.getOffhandItem().isEmpty()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("msg.greatsword.need_free_offhand"), true);
                }
                return InteractionResultHolder.fail(stack);
            }
        }

        if (!level.isClientSide) {
            setDrawn(stack, !drawn);
            level.playSound(null, player.blockPosition(),
                    SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.6F, 1.4F);
            player.displayClientMessage(Component.translatable(
                    drawn ? "msg.greatsword.sheathed" : "msg.greatsword.drawn"), true);
            // Reevalua los atributos del inventario del jugador en el servidor.
            player.inventoryMenu.sendAllDataToRemote();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
