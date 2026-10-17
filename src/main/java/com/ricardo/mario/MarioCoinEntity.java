package com.ricardo.mario;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Moneda que sale disparada al golpear con el Super Jump Punch.
 * Es solo visual: atraviesa a los monstruos, cae con gravedad y desaparece al tocar un bloque
 * o al cumplir su tiempo de vida.
 */
public class MarioCoinEntity extends ThrowableItemProjectile {

    private static final int LIFETIME_TICKS = 40;

    public MarioCoinEntity(EntityType<? extends MarioCoinEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() {
        return MarioMod.COIN_ITEM.get();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false; // solo visual: no dana ni se detiene con los monstruos
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount > LIFETIME_TICKS) {
            this.discard();
        }
    }
}
