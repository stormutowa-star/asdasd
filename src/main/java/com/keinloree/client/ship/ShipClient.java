package com.keinloree.client.ship;

import com.keinloree.ship.DestroyerEntity;
import com.keinloree.ship.ShipTransform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Lista de destructores conocidos por el cliente y utilidades comunes (hilo principal). */
@Mod.EventBusSubscriber(modid = "keinloree", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ShipClient {
    private static final List<DestroyerEntity> SHIPS = new ArrayList<>();

    private ShipClient() {
    }

    public static List<DestroyerEntity> ships() {
        return SHIPS;
    }

    /** Destructor cuya caja (anclado o en vuelo) contiene el punto, o null. */
    public static DestroyerEntity shipContaining(Vec3 p) {
        for (DestroyerEntity s : SHIPS) {
            if (s.isRemoved()) {
                continue;
            }
            AABB box = ShipTransform.worldAABB(s.position(), s.getYRot());
            if (box.contains(p)) {
                return s;
            }
        }
        return null;
    }

    /** Destructor que pilota el jugador local, o null. */
    public static DestroyerEntity localPilotedShip() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof DestroyerEntity s && s.getPilotId() == mc.player.getId()) {
            return s;
        }
        return null;
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide && e.getEntity() instanceof DestroyerEntity d && !SHIPS.contains(d)) {
            SHIPS.add(d);
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent e) {
        if (e.getLevel().isClientSide && e.getEntity() instanceof DestroyerEntity d) {
            SHIPS.remove(d);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        SHIPS.clear();
        ShipMesh.release();
        DestroyerRenderer.clear();
    }
}
