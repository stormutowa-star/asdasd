package com.keinloree.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/** Huevo de creacion de la faccion Loree: al usarlo sobre un bloque aparece el ser correspondiente. */
public class LooreEggItem extends Item {

    private final Supplier<? extends EntityType<?>> type;

    public LooreEggItem(Supplier<? extends EntityType<?>> type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Entity entity = this.type.get().create(level);
        if (entity != null) {
            entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, context.getRotation(), 0.0F);
            level.addFreshEntity(entity);
            level.playSound(null, pos, SoundEvents.VILLAGER_CELEBRATE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
