package com.keinloree.client.test;

import com.keinloree.entity.CalendarioEntity;
import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.entity.RazakoEntity;
import com.keinloree.ship.DestroyerEntity;
import com.keinloree.ship.FighterEntity;
import com.keinloree.story.Campaign;
import com.keinloree.story.SagaWorld;
import com.keinloree.story.ScenePlayer;
import com.keinloree.world.Pillars;
import com.mojang.logging.LogUtils;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/**
 * Recorrido automatico de la campana (con escenas rapidas): fuerza las condiciones de cada paso desde el servidor
 * integrado y comprueba que la historia avanza sin errores hasta los tres finales.
 */
final class SagaScenario {
    private static final Logger LOGGER = LogUtils.getLogger();

    private SagaScenario() {
    }

    /** Ejecuta algo en el hilo del servidor integrado con el nivel y el heroe. */
    private static void srv(Minecraft mc, BiConsumer<ServerLevel, ServerPlayer> body) {
        var server = mc.getSingleplayerServer();
        if (server == null) {
            return;
        }
        server.execute(() -> {
            ServerLevel l = server.overworld();
            ServerPlayer p = server.getPlayerList().getPlayers().isEmpty() ? null : server.getPlayerList().getPlayers().get(0);
            try {
                body.accept(l, p);
            } catch (RuntimeException e) {
                LOGGER.error("[saga] fallo en la accion de prueba", e);
            }
        });
    }

    private static void log(Minecraft mc, String what) {
        srv(mc, (l, p) -> LOGGER.info("[saga] {} -> paso={} escena={} cola={}", what, Campaign.get(l).step, Campaign.get(l).sceneId,
                Campaign.get(l).sceneQueue));
    }

    private static void tpTo(ServerPlayer p, Vec3 v) {
        p.stopRiding();
        p.teleportTo(p.serverLevel(), v.x, v.y + 0.2D, v.z, p.getYRot(), 10.0F);
    }

    private static void toPillar(ServerLevel l, ServerPlayer p, int h, double off) {
        Campaign c = Campaign.get(l);
        Pillars.forceBuild(l, h);
        Vec3 ctr = Pillars.center(l, c, h);
        tpTo(p, ctr.add(off, 0, 0.5D));
    }

    private static void counter(ServerLevel l, String k, int v) {
        Campaign.get(l).setCounter(k, v);
    }

    private static String step(Minecraft mc) {
        var server = mc.getSingleplayerServer();
        return server == null ? "" : Campaign.get(server).step;
    }

    private static java.util.function.Predicate<Minecraft> at(String id) {
        return mc -> id.equals(step(mc));
    }

    /** Elige en cuanto la eleccion este pendiente. */
    private static void choose(Minecraft mc, String id, String key) {
        srv(mc, (l, p) -> com.keinloree.story.Later.repeat(0, 30000, n -> !ScenePlayer.choose(l, id, key)));
    }

    static void register() {
        ClientAutoTest.register("saga", s -> {
            s.wait(200).cmd("gamemode creative").cmd("time set 1000").cmd("gamerule doDaylightCycle false")
                    .cmd("keinloree ship remove").cmd("keinloree story reset")
                    .run(mc -> ScenePlayer.fast = true).wait(20)
                    .cmd("keinloree story goto prologo")
                    .waitUntil("llegada", at("prologo.llegada"), 2000).wait(300).run(ShipLook::look).wait(2).shot("saga_01_llegada")
                    .waitUntil("desembarco", at("act1.desembarco"), 4000).run(mc -> log(mc, "desembarco")).wait(300)
                    .cmd("keinloree story goto act1.primer_disparo").wait(600).run(mc -> log(mc, "primer_disparo"))
                    .cmd("keinloree story goto act1.ecos").wait(300).run(mc -> log(mc, "ecos"))
                    .cmd("keinloree story goto act1.ejecutor").wait(200)
                    .run(mc -> srv(mc, (l, p) -> {
                        BlockPos b = Campaign.get(l).positions.get("base.razako");
                        if (b != null) {
                            tpTo(p, Vec3.atCenterOf(b).add(25, 2, 0));
                        }
                    }))
                    .wait(1500).run(mc -> log(mc, "ejecutor")).shot("saga_02_razako")
                    .waitUntil("calendarios", at("act1.calendarios"), 2500).wait(300).shot("saga_03_calendarios")
                    .run(mc -> choose(mc, "unirse", "si"))
                    .waitUntil("aprendiz", at("act2.aprendiz"), 3000).run(mc -> log(mc, "aprendiz"));
            // acto II: tres entrenamientos
            for (int h = 0; h < 3; h++) {
                int hour = h;
                s.run(mc -> srv(mc, (l, p) -> toPillar(l, p, hour, 30)))
                        .wait(200).run(mc -> srv(mc, (l, p) -> toPillar(l, p, hour, 8)))
                        .waitUntil("entreno " + hour, mc -> mc.getSingleplayerServer() != null
                                && Campaign.get(mc.getSingleplayerServer()).counter("entreno.activo") == hour + 1, 1500)
                        .wait(300).run(mc -> log(mc, "entreno " + hour)).shot("saga_04_pilar_" + hour)
                        .run(mc -> srv(mc, (l, p) -> {
                            counter(l, "ecos_muertos", 6);
                            counter(l, "cenit.oleada", 3);
                            counter(l, "cenit.bajas", 8);
                            counter(l, "ocaso.bajas", 3);
                        }))
                        .waitUntil("fin entreno " + hour, mc -> mc.getSingleplayerServer() != null
                                && Campaign.get(mc.getSingleplayerServer()).counter("entreno.activo") == 0, 1500)
                        .wait(200).run(mc -> log(mc, "entreno fin " + hour));
            }
            s.waitUntil("pilar_caido", at("act2.pilar_caido"), 2000)
                    .run(mc -> srv(mc, (l, p) -> toPillar(l, p, 3, 40)))
                    .wait(500).shot("saga_05_pilar_cae")
                    .waitUntil("preparacion", at("act2.preparacion"), 3000).wait(200)
                    .run(mc -> srv(mc, (l, p) -> {
                        FighterEntity f = FighterEntity.spawn(l, p.position().add(3, 1, 0), 0.0F, false);
                        p.startRiding(f, true);
                    }))
                    .waitUntil("infiltracion", at("act2.infiltracion"), 2000).wait(200)
                    .run(mc -> srv(mc, (l, p) -> {
                        Vec3 f = SagaWorld.hangarFloor(l);
                        if (f != null) {
                            tpTo(p, f);
                        }
                    }))
                    .wait(300).run(mc -> log(mc, "hangar")).shot("saga_06_hangar")
                    .run(mc -> srv(mc, (l, p) -> {
                        Vec3 c = SagaWorld.cell(l);
                        if (c != null) {
                            tpTo(p, c.add(-3, 0, 0));
                        }
                    }))
                    .wait(400).run(mc -> log(mc, "celda")).shot("saga_07_celda")
                    .run(mc -> srv(mc, (l, p) -> {
                        Campaign c = Campaign.get(l);
                        for (int i = 0; i < 3; i++) {
                            BlockPos g = c.positions.get("gen." + i);
                            if (g != null) {
                                l.setBlock(g, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }))
                    .wait(400).run(mc -> log(mc, "liberada"))
                    .run(mc -> srv(mc, (l, p) -> {
                        Vec3 f = SagaWorld.hangarFloor(l);
                        if (f != null) {
                            tpTo(p, f);
                        }
                    }))
                    .wait(500).run(mc -> log(mc, "razako hangar")).shot("saga_08_razako_hangar")
                    .run(mc -> srv(mc, (l, p) -> Campaign.get(l).setFlag("razako_arrodillado")))
                    .run(mc -> choose(mc, "perdon", "perdonar")).wait(600).run(mc -> log(mc, "perdon"))
                    .run(mc -> srv(mc, (l, p) -> {
                        DestroyerEntity d = SagaWorld.ship(l);
                        FighterEntity f = FighterEntity.spawn(l, d.position().add(0, -80, 0), 0.0F, false);
                        tpTo(p, f.position());
                        p.startRiding(f, true);
                    }))
                    .wait(300).run(mc -> log(mc, "escape"))
                    .run(mc -> srv(mc, (l, p) -> Campaign.get(l).inc("fighters_destroyed", 4)))
                    .waitUntil("aprendiz2", at("act2.aprendiz2"), 2000)
                    .run(mc -> srv(mc, (l, p) -> {
                        p.stopRiding();
                        counter(l, "entreno.mascara", 31);
                        counter(l, "entrenado", 5);
                    }))
                    .waitUntil("revelacion", at("act3.revelacion"), 2000)
                    // acto III
                    .run(mc -> srv(mc, (l, p) -> toPillar(l, p, 4, 6)))
                    .wait(600).run(mc -> log(mc, "revelacion")).shot("saga_09_revelacion")
                    .waitUntil("oferta", at("act3.oferta"), 3000)
                    .waitUntil("lanzadera", mc -> mc.getSingleplayerServer() != null
                            && Campaign.get(mc.getSingleplayerServer()).counter("fase.act3.oferta") >= 2, 5000)
                    .run(mc -> srv(mc, (l, p) -> {
                        var e = Campaign.get(l).entities.get("lanzadera");
                        var en = e == null ? null : l.getEntity(e);
                        if (en != null) {
                            tpTo(p, en.position().add(2, 0, 0));
                        }
                    }))
                    .wait(400).run(mc -> log(mc, "a bordo"))
                    .run(mc -> srv(mc, (l, p) -> {
                        KeinLooreEntity k = SagaWorld.kein(l);
                        if (k != null) {
                            tpTo(p, k.position().add(0, 0, -5));
                        }
                    }))
                    .wait(300).shot("saga_10_kein")
                    .run(mc -> choose(mc, "oferta", "saber")).run(mc -> choose(mc, "oferta2", "rechazar"))
                    .waitUntil("escape", at("act3.escape"), 3000)
                    .run(mc -> srv(mc, (l, p) -> toPillar(l, p, 0, 20)))
                    .waitUntil("bombardeo", at("amanecer.bombardeo"), 1000)
                    .wait(600).run(mc -> log(mc, "bombardeo")).shot("saga_11_bombardeo")
                    .run(mc -> srv(mc, (l, p) -> Campaign.get(l).inc("turrets_heavy_destroyed", 6)))
                    .waitUntil("luces", at("amanecer.luces"), 3000)
                    .run(mc -> srv(mc, (l, p) -> counter(l, "luces", 31)))
                    .waitUntil("asalto", at("amanecer.asalto"), 3000).wait(300).shot("saga_12_asalto")
                    .run(mc -> srv(mc, (l, p) -> {
                        Vec3 b = SagaWorld.bridge(l);
                        if (b != null) {
                            tpTo(p, b.add(0, 0, -4));
                        }
                    }))
                    .waitUntil("hora cero", at("amanecer.hora_cero"), 1500).wait(400).shot("saga_13_kein_final")
                    .run(mc -> srv(mc, (l, p) -> Campaign.get(l).setFlag("kein_vencido")))
                    .run(mc -> choose(mc, "final", "romper"))
                    .waitUntil("final", at("final.amanecer"), 3000).wait(300).shot("saga_14_final")
                    .waitUntil("fin", at("fin"), 4000).run(mc -> log(mc, "fin amanecer"))
                    // ruta del Nexo con una nave nueva
                    .cmd("keinloree ship arrive").wait(2200)
                    .run(mc -> srv(mc, (l, p) -> {
                        Campaign c = Campaign.get(l);
                        c.clearFlag("terminado");
                        c.clearFlag("legion_deshecha");
                        c.choices.clear();
                    }))
                    .cmd("keinloree story goto nexo.marca").wait(400)
                    .run(mc -> srv(mc, (l, p) -> {
                        RazakoEntity r = SagaWorld.razako(l);
                        if (r != null) {
                            tpTo(p, r.position().add(2, 0, 0));
                        }
                    }))
                    .waitUntil("traicion", at("nexo.traicion"), 2500)
                    .run(mc -> srv(mc, (l, p) -> {
                        toPillar(l, p, 1, 10);
                        counter(l, "anclas_rotas.cenit", 3);
                    }))
                    .wait(800).run(mc -> log(mc, "ocaso")).shot("saga_15_ocaso")
                    .run(mc -> srv(mc, (l, p) -> {
                        CalendarioEntity o = SagaWorld.hour(l, 2);
                        if (o != null) {
                            o.setHealth(1.0F);
                        }
                    }))
                    .waitUntil("ultimo pilar", at("nexo.ultimo_pilar"), 2500)
                    .run(mc -> srv(mc, (l, p) -> toPillar(l, p, 4, 10)))
                    .wait(800).run(mc -> srv(mc, (l, p) -> {
                        CalendarioEntity e = SagaWorld.hour(l, 4);
                        if (e != null) {
                            e.setHealth(1.0F);
                        }
                    }))
                    .waitUntil("final nexo", at("final.nexo"), 3000).wait(300).shot("saga_16_nexo")
                    .waitUntil("fin nexo", at("fin"), 4000).run(mc -> log(mc, "fin nexo"))
                    .run(mc -> ScenePlayer.fast = false).quit();
        });
    }

    /** Mira al destructor. */
    static final class ShipLook {
        private ShipLook() {
        }

        static void look(Minecraft mc) {
            for (var e : mc.level.entitiesForRendering()) {
                if (e instanceof DestroyerEntity d && mc.player != null) {
                    Vec3 to = d.position().subtract(mc.player.getEyePosition());
                    mc.player.setYRot((float) Math.toDegrees(Math.atan2(-to.x, to.z)));
                    mc.player.setXRot((float) -Math.toDegrees(Math.atan2(to.y, Math.sqrt(to.x * to.x + to.z * to.z))));
                }
            }
        }
    }
}
