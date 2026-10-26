package com.keinloree.ship;

import com.keinloree.registry.ModEntities;
import com.keinloree.story.StoryCommands;
import com.keinloree.test.ShipTests;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

/** Eventos del destructor: proteccion del casco, pasajeros, desconexiones y recuperacion. */
public class ShipEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initDone;
    private int missingChecks;

    /** Llamar en el constructor del mod (antes de AutoTest.init). */
    public static synchronized void init() {
        if (initDone) {
            return;
        }
        initDone = true;
        MinecraftForge.EVENT_BUS.register(new ShipEvents());
        StoryCommands.extend(ShipCommands::register);
        ShipTests.register();
    }

    // ------------------------------------------------------------------ proteccion

    private static boolean protectedAt(ServerLevel level, BlockPos pos) {
        if (level != level.getServer().overworld()) {
            return false;
        }
        AABB box = ShipData.get(level.getServer()).protectedBox();
        return box != null && box.contains(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    private static void warn(Entity e) {
        if (e instanceof Player p) {
            p.displayClientMessage(Component.translatable("message.keinloree.hull_protected"), true);
        }
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel sl && !event.getPlayer().isCreative() && protectedAt(sl, event.getPos())) {
            event.setCanceled(true);
            warn(event.getPlayer());
        }
    }

    @SubscribeEvent
    public void onPlace(BlockEvent.EntityPlaceEvent event) {
        Entity e = event.getEntity();
        if (e instanceof Player p && p.isCreative()) {
            return;
        }
        if (event.getLevel() instanceof ServerLevel sl && protectedAt(sl, event.getPos())) {
            event.setCanceled(true);
            warn(e);
        }
    }

    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel sl && ShipData.get(sl.getServer()).hasBlocks()) {
            event.getAffectedBlocks().removeIf(p -> protectedAt(sl, p));
        }
    }

    @SubscribeEvent
    public void onGrief(EntityMobGriefingEvent event) {
        if (event.getEntity() instanceof EnderMan em && em.level() instanceof ServerLevel sl) {
            AABB box = ShipData.get(sl.getServer()).protectedBox();
            if (box != null && box.inflate(4.0D).contains(em.position())) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    // ------------------------------------------------------------------ pasajeros

    /** En vuelo nadie se baja salvo que lo haga la nave (o el pasajero muera / desaparezca). */
    @SubscribeEvent
    public void onMount(EntityMountEvent event) {
        if (!event.isDismounting() || !(event.getEntityBeingMounted() instanceof DestroyerEntity ship)) {
            return;
        }
        Entity rider = event.getEntityMounting();
        if (ship.level().isClientSide || ship.isReleasing() || ship.isRemoved() || rider.isRemoved() || !rider.isAlive()) {
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && p.getVehicle() instanceof DestroyerEntity ship) {
            ship.detachPlayer(p);
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) {
            return;
        }
        ShipData d = ShipData.get(p.getServer());
        float[] off = d.offline.remove(p.getUUID());
        if (off == null) {
            return;
        }
        d.setDirty();
        ServerLevel ow = p.getServer().overworld();
        DestroyerEntity ship = DestroyerEntity.find(ow);
        if (ship == null && d.ship != null) {
            // la nave puede estar en un chunk sin cargar: cargalo un momento
            ChunkPos c = new ChunkPos(BlockPos.containing(d.pos));
            ow.getChunk(c.x, c.z);
            ship = DestroyerEntity.find(ow);
        }
        if (ship != null) {
            ship.reattachPlayer(p, off);
        } else if (d.hasBlocks()) {
            Vec3 w = ShipTransform.toWorld(ShipTransform.entityPos(d.anchor), ShipTransform.yawFor(d.rot),
                    new Vec3(off[0], off[1], off[2]));
            p.teleportTo(ow, w.x, w.y, w.z, p.getYRot(), p.getXRot());
        }
    }

    /** Antes de guardar a los jugadores al parar: bajalos (vanilla guardaria la nave dentro del jugador). */
    @SubscribeEvent
    public void onStopping(ServerStoppingEvent event) {
        for (ServerPlayer p : event.getServer().getPlayerList().getPlayers()) {
            if (p.getVehicle() instanceof DestroyerEntity ship) {
                ship.detachPlayer(p);
            }
        }
    }

    // ------------------------------------------------------------------ recuperacion

    /** Si la nave anclada desaparece (/kill) con su chunk cargado, se re-genera en su ancla. */
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 40 != 0) {
            return;
        }
        ShipData d = ShipData.get(server);
        if (d.ship == null || d.state != DestroyerEntity.ANCHORED) {
            missingChecks = 0;
            return;
        }
        ServerLevel ow = server.overworld();
        BlockPos a = d.anchor;
        if (!ow.isPositionEntityTicking(a) || ow.getEntity(d.ship) != null) {
            missingChecks = 0;
            return;
        }
        if (++missingChecks < 3) {
            return;
        }
        missingChecks = 0;
        DestroyerEntity s = ModEntities.DESTROYER.get().create(ow);
        if (s == null) {
            return;
        }
        s.restoreAnchored(a, d.rot);
        ow.addFreshEntity(s);
        LOGGER.warn("[Destructor] La nave anclada no estaba: re-generada en {}", a);
    }
}
