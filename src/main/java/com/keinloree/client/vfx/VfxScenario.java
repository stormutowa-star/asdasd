package com.keinloree.client.vfx;

import com.keinloree.client.test.ClientAutoTest;
import com.keinloree.vfx.VfxType;
import net.minecraft.client.CameraType;
import net.minecraft.client.CloudStatus;

/** Escenario de cliente "vfx_all": lanza cada efecto y saca capturas de dia y de noche. */
public final class VfxScenario {
    private VfxScenario() {
    }

    /** Ticks de espera antes de la captura de cada tipo (el momento mas representativo). */
    private static int delay(VfxType t) {
        return switch (t) {
            case BEAM, RING -> 14;
            case SHOCKWAVE -> 12;
            case SLASH -> 7;
            case ORB, DISTORTION, TIME_FREEZE -> 22;
            case PILLAR, RUNE_CIRCLE -> 30;
            case LIGHTNING -> 5;
            case CODE_RAIN -> 40;
            case BURST -> 6;
            case HYPERSPACE -> 9;
            case TRAIL -> 20;
            case EXPLOSION -> 9;
            case MUZZLE -> 2;
        };
    }

    public static void register() {
        ClientAutoTest.register("vfx_all", s -> {
            s.wait(200).cmd("gamemode creative").cmd("weather clear").cmd("time set noon")
                    .cmd("tp @s 0.5 200 0.5")
                    // plataforma oscura para ver bien los efectos
                    .wait(40).cmd("fill -40 199 -10 40 199 60 minecraft:polished_deepslate")
                    .cmd("fill -40 200 -10 40 230 60 minecraft:air")
                    .wait(20).tp(0.5, 200, 0.5).look(0, 4).hideHud(true).run(mc -> {
                        mc.options.cloudStatus().set(CloudStatus.OFF);
                        VfxRenderer.clear();
                    }).wait(40);
            for (int pass = 0; pass < 2; pass++) {
                String tag = pass == 0 ? "day" : "night";
                s.cmd(pass == 0 ? "time set noon" : "time set midnight").wait(30);
                for (VfxType t : VfxType.values()) {
                    boolean trail = t == VfxType.TRAIL;
                    s.run(mc -> VfxRenderer.clear());
                    if (trail) {
                        s.run(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK));
                    }
                    s.cmd("keinloree vfx " + t.cmd());
                    if (trail) {
                        for (int i = 0; i < 10; i++) {
                            s.wait(2).cmd("tp @s ~1.2 ~ ~");
                        }
                    }
                    s.wait(delay(t)).shot("vfx_" + t.cmd() + "_" + tag);
                    if (trail) {
                        s.run(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON)).tp(0.5, 200, 0.5).look(0, 4);
                    }
                    s.wait(6);
                }
                s.run(mc -> VfxRenderer.clear()).wait(5).cmd("keinloree vfx all").wait(25).shot("vfx_all_" + tag);
                s.wait(10);
            }
            s.run(mc -> VfxRenderer.clear()).wait(10).quit();
        });
    }
}
