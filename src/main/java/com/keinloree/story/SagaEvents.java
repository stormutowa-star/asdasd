package com.keinloree.story;

import com.keinloree.entity.CalendarioEntity;
import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.entity.RazakoEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Eventos de juego que alimentan la campana: bajas, golpes, bloques puestos y los ganchos de Razako y Kein. */
public final class SagaEvents {
    private static boolean registered;

    private SagaEvents() {
    }

    public static synchronized void init() {
        if (registered) {
            return;
        }
        registered = true;
        MinecraftForge.EVENT_BUS.register(new SagaEvents());
        RazakoEntity.ON_KNEEL = r -> {
            if (r.level() instanceof ServerLevel sl && !r.isEcho()) {
                Campaign.get(sl.getServer()).setFlag("razako_arrodillado");
            }
        };
        KeinLooreEntity.ON_DEFEATED = k -> {
            if (k.level() instanceof ServerLevel sl) {
                Campaign.get(sl.getServer()).setFlag("kein_vencido");
            }
        };
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (!(dead.level() instanceof ServerLevel sl) || sl != sl.getServer().overworld()) {
            return;
        }
        Campaign c = Campaign.get(sl.getServer());
        ServerPlayer hero = c.hero == null ? null : sl.getServer().getPlayerList().getPlayer(c.hero);
        if (hero != null && Factions.isLegion(dead) && dead.distanceTo(hero) < 64.0F) {
            c.inc("bajas_cerca", 1);
        }
        var data = dead.getPersistentData();
        if (data.getBoolean("keinloree_eco")) {
            c.inc("ecos_muertos", 1);
        }
        if (data.getBoolean("keinloree_oleada")) {
            c.inc("cenit.bajas", 1);
        }
        if (data.getBoolean("keinloree_ocaso")) {
            c.inc("ocaso.bajas", 1);
        }
        Entity killer = e.getSource().getEntity();
        if (hero != null && killer == hero) {
            c.setCounter("ult_muerte", (int) c.worldTicks);
        }
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent e) {
        // Los Calendarios en duelo de entrenamiento nunca hacen mas de 3 de dano (por si acaso)
        if (e.getSource().getEntity() instanceof CalendarioEntity ce && ce.getMode() == CalendarioEntity.MODE_DUEL) {
            e.setAmount(Math.min(3.0F, e.getAmount()));
        }
        if (!(e.getEntity() instanceof ServerPlayer p) || !(e.getSource().getEntity() instanceof RazakoEntity)) {
            return;
        }
        Campaign c = Campaign.get(p.getServer());
        if ("act1.ejecutor".equals(c.step) && e.getAmount() >= 3.0F && p.getUUID().equals(c.hero)) {
            StoryCtx ctx = new StoryCtx(p.getServer().overworld());
            Saga.sceneOnce(ctx, "act1.razako_golpe");
        }
    }

    @SubscribeEvent
    public void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            Campaign c = Campaign.get(p.getServer());
            if (p.getUUID().equals(c.hero)) {
                Unease.onPlaced(c.worldTicks);
            }
        }
    }
}
