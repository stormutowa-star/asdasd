package com.keinloree.client.test;

import com.keinloree.ship.DestroyerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Escenarios de cliente del destructor: llegada, vuelo y nubes. */
final class ShipScenarios {
    private ShipScenarios() {}

    static void registerAll() {
        ClientAutoTest.register("destroyer_arrive", s -> s.wait(200).cmd("keinloree story disable").cmd("time set noon")
                .cmd("gamemode creative").wait(20).cmd("keinloree ship arrive")
                .wait(100).run(ShipScenarios::lookAtShip).wait(2).shot("ship_arrive_05")
                .wait(300).run(ShipScenarios::lookAtShip).wait(2).shot("ship_arrive_20")
                .wait(500).run(ShipScenarios::lookAtShip).wait(2).shot("ship_arrive_45")
                .wait(100).run(ShipScenarios::toBridge).wait(60).shot("ship_bridge")
                .run(ShipScenarios::toHangar).wait(60).shot("ship_hangar").quit());
        ClientAutoTest.register("destroyer_fly", s -> s.wait(200).cmd("keinloree story disable").cmd("time set noon")
                .cmd("gamemode creative").wait(20).cmd("keinloree ship arrive")
                .wait(1000).run(ShipScenarios::toBridge).wait(40).cmd("keinloree ship helm")
                .wait(120).shot("fly_helm_start")
                .run(ShipScenarios::gotoAway).wait(200).shot("fly_helm_moving")
                .run(ShipScenarios::outside).wait(4).shot("fly_outside")
                .wait(500).run(ShipScenarios::toBridge).wait(40).shot("fly_after_anchor").quit());
    }

    private static DestroyerEntity ship(Minecraft mc) {
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof DestroyerEntity d) {
                return d;
            }
        }
        return null;
    }

    private static void lookAtShip(Minecraft mc) {
        DestroyerEntity d = ship(mc);
        if (d == null || mc.player == null) {
            return;
        }
        Vec3 to = d.position().subtract(mc.player.getEyePosition());
        mc.player.setYRot((float) Math.toDegrees(Math.atan2(-to.x, to.z)));
        mc.player.setXRot((float) -Math.toDegrees(Math.atan2(to.y, Math.sqrt(to.x * to.x + to.z * to.z))));
    }

    /** Coloca la camara en el puente (2 bloques detras del timon) mirando a proa. */
    private static void toBridge(Minecraft mc) {
        DestroyerEntity d = ship(mc);
        if (d == null) {
            return;
        }
        float yaw = d.getYRot();
        Vec3 fwd = Vec3.directionFromRotation(0, yaw);
        Vec3 p = d.position().subtract(fwd.scale(2.0)).add(0, 0.2, 0);
        mc.player.connection.sendCommand(String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f 10", p.x, p.y, p.z, yaw));
    }

    private static void toHangar(Minecraft mc) {
        DestroyerEntity d = ship(mc);
        if (d == null) {
            return;
        }
        float yaw = d.getYRot();
        Vec3 fwd = Vec3.directionFromRotation(0, yaw);
        // El hangar esta ~45 bloques a proa del timon y unos 36 mas abajo
        Vec3 p = d.position().add(fwd.scale(45)).add(0, -36, 0);
        mc.player.connection.sendCommand(String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f 0", p.x, p.y, p.z, yaw));
    }

    private static void gotoAway(Minecraft mc) {
        DestroyerEntity d = ship(mc);
        if (d != null) {
            mc.player.connection.sendCommand(String.format(java.util.Locale.ROOT, "keinloree ship goto %.1f %.1f", d.getX() + 120, d.getZ() + 40));
        }
    }

    private static void outside(Minecraft mc) {
        DestroyerEntity d = ship(mc);
        if (d == null) {
            return;
        }
        mc.player.stopRiding();
        Vec3 p = d.position().add(140, 10, 140);
        mc.player.connection.sendCommand(String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f", p.x, p.y, p.z));
        lookAtShip(mc);
    }
}
