package com.keinloree.story;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Eventos de Forge que mueven la historia (bus de Forge). */
public class StoryEvents {

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        Script.load();
        Later.reset();
        Unease.reset();
        com.keinloree.world.LegionOps.reset();
        ScenePlayer.reset();
        Cinematics.reset();
        Director.reset();
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        ScenePlayer.reset();
        Cinematics.reset();
        Director.reset();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        Cinematics.tick(server);
        ScenePlayer.tick(server.overworld());
        Director.tick(server.overworld());
        Later.tick();
        ServerLevel ow = server.overworld();
        com.keinloree.world.LegionOps.tick(ow);
        Campaign cp = Campaign.get(ow);
        if (!cp.disabled && !ow.players().isEmpty()) {
            com.keinloree.world.Pillars.tick(ow);
            Unease.tick(new StoryCtx(ow));
        }
    }

    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent event) {
        StoryCommands.register(event.getDispatcher());
    }

    /** Invulnerable durante las cinematicas (salvo /kill y vacio). */
    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        if (Cinematics.isActive(event.getEntity()) && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
        }
    }

    /** Los mobs no eligen como objetivo a quien esta en una cinematica. */
    @SubscribeEvent
    public void onTarget(LivingChangeTargetEvent event) {
        if (Cinematics.isActive(event.getNewTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            Atmosphere.sendTo(p, Campaign.get(p.getServer()));
            Director.invalidateHud();
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            Cinematics.forget(p);
        }
    }

    @SubscribeEvent
    public void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            Cinematics.stop(p);
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            Cinematics.stop(p);
            Campaign c = Campaign.get(p.getServer());
            c.tale(p.getUUID()).deaths++;
            c.setDirty();
        } else if (event.getSource().getEntity() instanceof ServerPlayer killer && Factions.isLegion(event.getEntity())) {
            Campaign c = Campaign.get(killer.getServer());
            c.tale(killer.getUUID()).kills++;
            c.setDirty();
        }
    }
}
