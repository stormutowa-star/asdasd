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

    static void registerChars() {
        ClientAutoTest.register("chars", s -> s.wait(200).cmd("keinloree story disable").cmd("time set noon").cmd("gamemode creative")
                .cmd("keinloree ship remove").cmd("tp @s 1000 151 1000 180 0").wait(60)
                .cmd("fill 980 150 980 1020 150 1020 minecraft:smooth_stone").cmd("fill 980 151 980 1020 170 1020 minecraft:air")
                .cmd("kill @e[type=!player,distance=..80]").wait(10)
                .cmd("tp @s 1000 151 1000 180 0")
                .cmd("summon keinloree:razako 996 151 992 {NoAI:1b,Rotation:[0f,0f]}")
                .cmd("summon keinloree:calendario 999 151 993 {NoAI:1b,Hour:0,Rotation:[0f,0f]}")
                .cmd("summon keinloree:calendario 1001 151 993 {NoAI:1b,Hour:1,Rotation:[0f,0f]}")
                .cmd("summon keinloree:calendario 1003 151 993 {NoAI:1b,Hour:2,Rotation:[0f,0f]}")
                .cmd("summon keinloree:calendario 1005 151 993 {NoAI:1b,Hour:3,Rotation:[0f,0f]}")
                .cmd("summon keinloree:calendario 1007 151 993 {NoAI:1b,Hour:4,Rotation:[0f,0f]}")
                .cmd("summon keinloree:turbolaser 993 151 985 {Kind:0}")
                .cmd("summon keinloree:turbolaser 998 151 985 {Kind:1}")
                .cmd("summon keinloree:fighter 1005 153 984")
                .cmd("clear @s").wait(60).hideHud(true).wait(2).shot("chars_front")
                .run(mc -> {
                    for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
                        if (e.getType().toString().contains("keinloree")) {
                            com.mojang.logging.LogUtils.getLogger().info("[chars] {} at {} render={}", e.getType(), e.position(),
                                    mc.getEntityRenderDispatcher().shouldRender(e, new net.minecraft.client.renderer.culling.Frustum(
                                            new org.joml.Matrix4f(), new org.joml.Matrix4f()), 0, 0, 0));
                        }
                    }
                })
                .cmd("tp @s 995 152 990 160 15").wait(30).shot("chars_turret_close")
                .cmd("tp @s 1000 151 1000 180 0")
                .cmd("summon keinloree:kein_hologram 1000 151 960 {Scale:10f}")
                .wait(40).run(mc -> mc.player.setXRot(-25.0F)).wait(5).shot("chars_holo").quit());
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
