package com.keinloree.ship;

import com.keinloree.world.design.ShipDesign;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Torretas y cazas del destructor: se generan al materializarse y responden a los mandos del piloto. */
public final class ShipArmament {
    private static boolean done;

    private ShipArmament() {
    }

    public static synchronized void init() {
        if (done) {
            return;
        }
        done = true;
        DestroyerEntity.ON_ANCHORED.add((level, ship, first) -> {
            if (first) {
                spawnTurrets(level, ship);
                spawnParkedFighters(level, ship);
            }
        });
        DestroyerEntity.onFirePrimary = ShipArmament::firePrimary;
        DestroyerEntity.onLaunchFighters = (ship, pilot) -> launchFighters((ServerLevel) ship.level(), ship, 3, null);
        MinecraftForge.EVENT_BUS.register(new ShipArmament.Events());
    }

    /** Posicion de mundo de un montaje (el pie de la torreta; si cuelga, el techo de la celda). */
    public static Vec3 mountWorld(DestroyerEntity ship, ShipDesign.Mount m) {
        Vec3 l = ShipTransform.cellFloor(new int[]{m.x(), m.y(), m.z()});
        if (m.inverted()) {
            l = l.add(0.0D, 1.0D, 0.0D);
        }
        return ShipTransform.toWorld(ship.position(), ship.getYRot(), l);
    }

    public static void spawnTurrets(ServerLevel level, DestroyerEntity ship) {
        for (ShipDesign.Mount m : ShipDesign.TURRET_MOUNTS) {
            Vec3 at = mountWorld(ship, m);
            int type = m.type() == ShipDesign.TurretType.HEAVY ? TurbolaserEntity.HEAVY : TurbolaserEntity.DEFENSE;
            TurbolaserEntity.create(level, at, type, m.inverted(), ship.getYRot());
        }
    }

    public static void spawnParkedFighters(ServerLevel level, DestroyerEntity ship) {
        for (int[] p : ShipDesign.FIGHTER_PARKING) {
            Vec3 at = ShipTransform.toWorld(ship.position(), ship.getYRot(), ShipTransform.cellFloor(p));
            FighterEntity.spawn(level, at.add(0.0D, 0.1D, 0.0D), ship.getYRot(), false);
        }
    }

    /** Lanza cazas con piloto IA desde la boca del hangar. */
    public static void launchFighters(ServerLevel level, DestroyerEntity ship, int count, @javax.annotation.Nullable Entity target) {
        Vec3 bay = ShipTransform.toWorld(ship.position(), ship.getYRot(),
                ShipTransform.cellFloor(new int[]{0, ShipDesign.DOCK_Y - 10, ShipDesign.DOCK_Z[1]}));
        for (int i = 0; i < count; i++) {
            Vec3 at = bay.add((i - count / 2.0D) * 6.0D, -i * 2.0D, 0.0D);
            FighterEntity f = FighterEntity.spawn(level, at, ship.getYRot() + 180.0F, true);
            if (target != null) {
                f.setPatrolCenter(target.blockPosition().above(20));
            }
        }
    }

    /** Las torretas pesadas que puedan apuntan al punto de mira del piloto. */
    private static void firePrimary(DestroyerEntity ship, ServerPlayer pilot) {
        ServerLevel sl = (ServerLevel) ship.level();
        Vec3 eye = pilot.getEyePosition();
        Vec3 look = pilot.getLookAngle();
        HitResult hit = sl.clip(new ClipContext(eye.add(look.scale(30.0D)), eye.add(look.scale(300.0D)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pilot));
        Vec3 at = hit.getLocation();
        List<TurbolaserEntity> turrets = sl.getEntitiesOfClass(TurbolaserEntity.class, ship.getBoundingBoxForCulling(),
                t -> t.isHeavy() && !t.isDestroyed() && !t.isInverted());
        for (TurbolaserEntity t : turrets) {
            t.forceAim(at, 60);
        }
    }

    public static final class Events {
        @SubscribeEvent
        public void onMount(EntityMountEvent event) {
            if (event.isDismounting() && event.getEntityBeingMounted() instanceof FighterEntity f
                    && event.getEntityMounting() instanceof Player p && !f.level().isClientSide
                    && f.isAlive() && p.isAlive() && !f.canEject()) {
                p.displayClientMessage(Component.translatable("message.keinloree.fighter_eject"), true);
                event.setCanceled(true);
            }
        }
    }
}
