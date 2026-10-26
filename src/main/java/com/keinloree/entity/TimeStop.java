package com.keinloree.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * "Hora Detenida": congela seres vivos unos ticks (sin IA ni movimiento). Se guarda en los datos de la entidad, asi
 * que nadie se queda congelado para siempre aunque el servidor se reinicie.
 */
public final class TimeStop {
    private static final String KEY = "keinloree_frozen_until";
    private static final String KEY_AI = "keinloree_frozen_hadai";
    private static boolean registered;

    private TimeStop() {
    }

    public static synchronized void init() {
        if (!registered) {
            registered = true;
            MinecraftForge.EVENT_BUS.register(new TimeStop.Events());
        }
    }

    public static void freeze(LivingEntity e, int ticks) {
        if (!(e.level() instanceof ServerLevel sl)) {
            return;
        }
        long until = sl.getGameTime() + ticks;
        e.getPersistentData().putLong(KEY, Math.max(until, e.getPersistentData().getLong(KEY)));
        if (e instanceof Mob m && !e.getPersistentData().contains(KEY_AI)) {
            e.getPersistentData().putBoolean(KEY_AI, !m.isNoAi());
            m.setNoAi(true);
        }
        if (e instanceof Player p) {
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 9, false, false));
            p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, ticks, 4, false, false));
        }
        e.setDeltaMovement(0.0D, Math.min(0.0D, e.getDeltaMovement().y), 0.0D);
    }

    public static boolean isFrozen(Entity e) {
        return e.getPersistentData().contains(KEY);
    }

    private static void check(LivingEntity e, long now) {
        long until = e.getPersistentData().getLong(KEY);
        if (now >= until) {
            release(e);
        } else {
            e.setDeltaMovement(0.0D, Math.min(0.0D, e.getDeltaMovement().y), 0.0D);
        }
    }

    public static void release(LivingEntity e) {
        boolean hadAi = e.getPersistentData().getBoolean(KEY_AI);
        if (e instanceof Mob m && e.getPersistentData().contains(KEY_AI)) {
            m.setNoAi(!hadAi ? m.isNoAi() : false);
        }
        e.getPersistentData().remove(KEY);
        e.getPersistentData().remove(KEY_AI);
    }

    public static final class Events {
        @SubscribeEvent
        public void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
            LivingEntity e = event.getEntity();
            if (!e.level().isClientSide && e.getPersistentData().contains(KEY)) {
                check(e, e.level().getGameTime());
            }
        }

        @SubscribeEvent
        public void onJoin(EntityJoinLevelEvent event) {
            if (!event.getLevel().isClientSide && event.getEntity() instanceof LivingEntity e && e.getPersistentData().contains(KEY)) {
                check(e, event.getLevel().getGameTime());
            }
        }
    }
}
