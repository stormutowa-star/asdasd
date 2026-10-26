package com.keinloree.story;

import com.keinloree.ability.Techniques;
import com.keinloree.entity.CalendarioEntity;
import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.entity.LooreMob;
import com.keinloree.entity.Rank;
import com.keinloree.entity.RazakoEntity;
import com.keinloree.net.AtmosphereS2C;
import com.keinloree.registry.ModEntities;
import com.keinloree.registry.ModItems;
import com.keinloree.ship.DestroyerEntity;
import com.keinloree.ship.FighterEntity;
import com.keinloree.ship.TurbolaserEntity;
import com.keinloree.vfx.Vfx;
import com.keinloree.world.LegionOps;
import com.keinloree.world.Pillars;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * La campana completa (WP12): prologo, tres actos y las rutas del Amanecer y del Nexo con sus tres finales.
 * Los pasos solo llevan el juego (objetivos, apariciones, condiciones); los dialogos y lo guionizado estan en el guion.
 */
public final class Saga {
    public static final String START = "prologo";
    private static final int MIN = 1200;

    private Saga() {
    }

    // ================================================================== ayudantes

    abstract static class S implements Step {
        private final String id;
        @Nullable
        private final String next;

        S(String id, @Nullable String next) {
            this.id = id;
            this.next = next;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String next() {
            return next;
        }

        int phase(StoryCtx c) {
            return c.counter("fase." + id);
        }

        void phase(StoryCtx c, int v) {
            c.campaign.setCounter("fase." + id, v);
            c.campaign.setCounter("fase_t." + id, (int) c.campaign.worldTicks);
        }

        long inPhase(StoryCtx c) {
            return c.campaign.worldTicks - c.counter("fase_t." + id);
        }

        @Override
        public void enter(StoryCtx c) {
            phase(c, 0);
        }
    }

    static boolean once(StoryCtx c, String key) {
        return c.campaign.setFlag("once." + key);
    }

    static boolean sceneOnce(StoryCtx c, String id) {
        if (once(c, "esc." + id)) {
            c.scene(id);
            return true;
        }
        return false;
    }

    /** La escena se lanzo (con sceneOnce) y ya termino. */
    static boolean sceneDone(StoryCtx c, String id) {
        return c.flag("once.esc." + id) && !ScenePlayer.isPlaying(id);
    }

    @Nullable
    static String choice(StoryCtx c, String id) {
        return c.campaign.choices.get(id);
    }

    static ServerPlayer hero(StoryCtx c) {
        return c.hero();
    }

    static double dist(Entity e, @Nullable BlockPos p) {
        return p == null ? Double.MAX_VALUE : Math.sqrt(p.distToCenterSqr(e.position()));
    }

    static double dist(Entity e, @Nullable Vec3 p) {
        return p == null ? Double.MAX_VALUE : e.position().distanceTo(p);
    }

    static int trained(StoryCtx c) {
        return c.counter("entrenado");
    }

    static boolean idle(StoryCtx c) {
        ServerPlayer h = c.hero();
        return ScenePlayer.isIdle() && (h == null || !Cinematics.isActive(h));
    }

    // ================================================================== registro

    public static void register() {
        prologue();
        actOne();
        actTwo();
        infiltration();
        actThree();
        dawnRoute();
        nexusRoute();
        Steps.register(new S("fin", null) {
            @Override
            public void tick(StoryCtx c) {
            }
        });
    }

    // ================================================================== prologo

    private static void prologue() {
        Steps.register(new S(START, "prologo.llegada") {
            @Override
            public void tick(StoryCtx c) {
                long t = c.ticksInStep();
                ServerPlayer h = hero(c);
                if (t >= 400 && once(c, "voz1")) {
                    c.scene("voz.1");
                }
                if (t >= 600 && once(c, "fragmento")) {
                    SagaActions.dropFragment(c.level, h);
                }
                if (t >= 900) {
                    sceneOnce(c, "prologo.inquietud");
                }
                if (t >= MIN && idle(c)) {
                    c.goTo("prologo.llegada");
                }
            }
        });
        Steps.register(new S("prologo.llegada", "prologo.transmision") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                ServerPlayer h = hero(c);
                if (SagaWorld.ship(c.level) == null && h != null) {
                    DestroyerEntity.arrive(c.level, h);
                }
                c.setFlag("llegada");
                c.campaign.setCounter("llegada_t", (int) c.campaign.worldTicks);
                c.cinematic("llegada_destructor");
                c.scene("prologo.llegada");
            }

            @Override
            public void tick(StoryCtx c) {
                long t = c.ticksInStep();
                DestroyerEntity d = SagaWorld.ship(c.level);
                if (d == null && t % 200 == 100 && hero(c) != null) {
                    DestroyerEntity.arrive(c.level, hero(c));
                }
                boolean anchored = d != null && d.getState() == DestroyerEntity.ANCHORED;
                if ((anchored && t > 200 || t > 3000) && ScenePlayer.isIdle()) {
                    c.goTo("prologo.transmision");
                }
            }
        });
        Steps.register(new S("prologo.transmision", "act1.desembarco") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                sceneOnce(c, "prologo.transmision");
            }

            @Override
            public void tick(StoryCtx c) {
                if (sceneDone(c, "prologo.transmision") || c.ticksInStep() > 6000) {
                    Atmosphere.set(c.level, 0.15F, 0.05F, 0);
                    c.goTo("act1.desembarco");
                }
            }
        });
    }

    // ================================================================== acto I

    private static void actOne() {
        Steps.register(new S("act1.desembarco", "act1.primer_disparo") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("act1.desembarco");
                c.setFlag("ritmo.bases");
                c.campaign.setCounter("ritmo.base_t", (int) c.campaign.worldTicks);
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                BlockPos base = c.pos("base1");
                if (base == null && c.ticksInStep() % 100 == 20) {
                    Vec3 near = LegionOps.groundAround(c.level, h.position(), 120.0D, 200.0D);
                    BlockPos g = LegionOps.launchBase(c.level, BlockPos.containing(near), false);
                    if (g != null) {
                        c.setPos("base1", g);
                    }
                }
                if (c.ticksInStep() >= 300) {
                    sceneOnce(c, "radio.1");
                }
                if ((dist(h, base) < 48.0D || c.ticksInStep() > 6000) && idle(c)) {
                    c.goTo("act1.primer_disparo");
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return "Investiga la zona de aterrizaje";
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                return c.pos("base1");
            }
        });

        Steps.register(new S("act1.primer_disparo", "act1.ecos") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("act1.patrulla");
                c.campaign.setCounter("bajas0", c.counter("bajas_cerca"));
                c.campaign.setCounter("muertes0", c.campaign.tale(hero(c).getUUID()).deaths);
                c.setFlag("omni");
                c.campaign.nextWhisperAt = c.campaign.worldTicks + 6 * MIN;
                LegionOps.dropPatrol(c.level, hero(c), Rank.SARGENTO, 4);
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                if (c.campaign.tale(h.getUUID()).deaths > c.counter("muertes0")) {
                    sceneOnce(c, "act1.muerte");
                }
                if (phase(c) == 0) {
                    if (progress(c) >= 5) {
                        c.scene("act1.primer_sangre");
                        phase(c, 1);
                    } else if (c.ticksInStep() > 10 * MIN) {
                        c.scene("act1.huida");
                        phase(c, 1);
                    } else if (c.ticksInStep() == 2400 && c.level.getEntitiesOfClass(LooreMob.class, h.getBoundingBox().inflate(96.0D)).isEmpty()) {
                        // la nave no llego: la patrulla baja igualmente
                        LegionOps.squad(c.level, LegionOps.groundAround(c.level, h.position(), 30.0D, 45.0D), h.getY(), Rank.SARGENTO, 4, h);
                    }
                } else if (idle(c)) {
                    c.goTo("act1.ecos");
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return "Sobrevive a la patrulla";
            }

            @Override
            public int progress(StoryCtx c) {
                return Math.min(5, c.counter("bajas_cerca") - c.counter("bajas0"));
            }

            @Override
            public int progressMax(StoryCtx c) {
                return 5;
            }
        });

        Steps.register(new S("act1.ecos", "act1.ejecutor") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("act1.ecos");
                c.campaign.setCounter("ecos.dia0", (int) (c.level.getDayTime() / 24000L));
                c.setFlag("ritmo.patrullas");
                c.setFlag("ritmo.cazas");
                c.setFlag("ritmo.inquietud");
            }

            int dawns(StoryCtx c) {
                return (int) Math.max(0, c.level.getDayTime() / 24000L - c.counter("ecos.dia0"));
            }

            @Override
            public void tick(StoryCtx c) {
                long t = c.ticksInStep();
                ServerPlayer h = hero(c);
                if (t >= 5 * MIN) {
                    sceneOnce(c, "voz.2");
                }
                if (t >= 10 * MIN && once(c, "base2")) {
                    Vec3 near = LegionOps.groundAround(c.level, h.position(), 120.0D, 200.0D);
                    LegionOps.launchBase(c.level, BlockPos.containing(near), false);
                }
                SagaActions.villagerEvent(c, t);
                if (progress(c) >= 2 && t >= 18000 && once(c, "cazadores")) {
                    LegionOps.dropPatrol(c.level, h, Rank.COMANDANTE, 3);
                    c.scene("act1.cazadores");
                }
                boolean done = progress(c) >= 3 && t >= 30 * MIN || t >= 70 * MIN;
                if (done && idle(c)) {
                    c.goTo("act1.ejecutor");
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return "Sobrevive hasta el tercer amanecer";
            }

            @Override
            public int progress(StoryCtx c) {
                return Math.min(3, dawns(c));
            }

            @Override
            public int progressMax(StoryCtx c) {
                return 3;
            }
        });

        Steps.register(new S("act1.ejecutor", "act1.calendarios") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("act1.ejecutor");
                ServerPlayer h = hero(c);
                BlockPos b = LegionOps.nearestBase(c.campaign, h.position());
                if (b == null) {
                    b = LegionOps.launchBase(c.level, BlockPos.containing(LegionOps.groundAround(c.level, h.position(), 90.0D, 150.0D)), false);
                }
                if (b != null) {
                    c.setPos("base.razako", b);
                }
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                BlockPos b = c.pos("base.razako");
                RazakoEntity rz = SagaWorld.razako(c.level);
                switch (phase(c)) {
                    case 0 -> {
                        if (b == null && c.ticksInStep() > 200) {
                            b = h.blockPosition();
                            c.setPos("base.razako", b);
                        }
                        if (b != null && dist(h, b) < 40.0D) {
                            Vec3 land = LegionOps.groundAround(c.level, Vec3.atCenterOf(b), LegionOps.OUTPOST_R + 4, LegionOps.OUTPOST_R + 9);
                            if (LegionOps.landShip(c.level, land, "razako") == null) {
                                SagaActions.spawnRazako(c.level, land);
                            }
                            phase(c, 1);
                        }
                    }
                    case 1 -> {
                        if (rz == null && inPhase(c) > 900) {
                            SagaActions.spawnRazako(c.level, LegionOps.groundAround(c.level, h.position(), 10.0D, 16.0D));
                        }
                        if (rz != null) {
                            rz.setScripted(true);
                            rz.setKneelEnabled(false);
                            c.scene("act1.razako");
                            phase(c, 2);
                        }
                    }
                    case 2 -> {
                        if (rz != null && ScenePlayer.isIdle() && rz.getTarget() == null) {
                            rz.setTarget(h);
                        }
                        if (ScenePlayer.isIdle() && once(c, "pelea_razako1")) {
                            c.campaign.setCounter("pelea_t", (int) c.campaign.worldTicks);
                        }
                        boolean fought = c.flag("once.pelea_razako1") && c.campaign.worldTicks - c.counter("pelea_t") > 60 * 20;
                        if ((h.getHealth() < 6.0F || fought || rz == null) && idle(c)) {
                            c.goTo("act1.calendarios");
                        }
                    }
                    default -> { }
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return phase(c) < 2 ? "Algo ha aterrizado en la base más cercana" : "Sobrevive al Ejecutor";
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                return phase(c) < 2 ? c.pos("base.razako") : null;
            }
        });

        Steps.register(new S("act1.calendarios", "act2.aprendiz") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("act1.calendarios_llegan");
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                RazakoEntity rz = SagaWorld.razako(c.level);
                switch (phase(c)) {
                    case 0 -> {
                        if (ScenePlayer.isIdle()) {
                            for (int i = 0; i < 5; i++) {
                                CalendarioEntity ce = SagaWorld.hour(c.level, i);
                                if (ce != null && rz != null) {
                                    ce.setTarget(rz);
                                }
                            }
                            phase(c, 1);
                        }
                    }
                    case 1 -> {
                        if (inPhase(c) > 200) {
                            c.scene("act1.razako_retirada");
                            c.scene("act1.base");
                            c.scene("act1.oferta");
                            phase(c, 2);
                        }
                    }
                    case 2 -> {
                        String ch = choice(c, "unirse");
                        if (ch != null && !ScenePlayer.isPlaying("act1.oferta")) {
                            if ("si".equals(ch)) {
                                c.scene("act1.equipo");
                                phase(c, 3);
                            } else {
                                c.scene("act1.rechazo");
                                phase(c, 4);
                            }
                        }
                    }
                    case 3 -> {
                        if (idle(c)) {
                            c.goTo("act2.aprendiz");
                        }
                    }
                    case 4 -> {
                        if (idle(c)) {
                            c.goTo("act1.espera_equipo");
                        }
                    }
                    default -> { }
                }
                if (h != null && h.getHealth() < 4.0F && phase(c) < 2) {
                    h.heal(2.0F);
                }
            }
        });

        Steps.register(new S("act1.espera_equipo", "act2.aprendiz") {
            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                if (phase(c) == 0 && SagaWorld.hasGear(h)) {
                    c.campaign.tale(h.getUUID()).receivedGear = true;
                    c.campaign.setDirty();
                    c.scene("act1.aceptado");
                    phase(c, 1);
                } else if (phase(c) == 1 && idle(c)) {
                    c.goTo("act2.aprendiz");
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return phase(c) == 0 ? "Recoge el equipo de los Calendarios" : null;
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                return phase(c) == 0 ? c.pos("equipo") : null;
            }
        });
    }

    // ================================================================== acto II: entrenamientos

    /** Logica comun de los entrenamientos con las cinco Horas (act2.aprendiz y act2.aprendiz2). */
    abstract static class Training extends S {
        Training(String id, String next) {
            super(id, next);
        }

        int active(StoryCtx c) {
            return c.counter("entreno.activo") - 1;
        }

        int mask(StoryCtx c) {
            return c.counter("entreno.mascara");
        }

        int untrainedMask(StoryCtx c) {
            int m = 0;
            for (int i = 0; i < 5; i++) {
                if ((mask(c) & (1 << i)) == 0 && !(i == 3 && c.flag("medianoche_capturada") && !c.flag("medianoche_libre"))) {
                    m |= 1 << i;
                }
            }
            return m;
        }

        void tickTraining(StoryCtx c) {
            ServerPlayer h = hero(c);
            int a = active(c);
            if (a < 0) {
                int hNear = Pillars.nearest(c.campaign, h.position(), untrainedMask(c));
                if (hNear < 0) {
                    return;
                }
                BlockPos p = Pillars.pos(c.campaign, hNear);
                double d = dist(h, p);
                if (d < 64.0D && SagaWorld.hour(c.level, hNear) == null && Pillars.built(c.campaign, hNear)) {
                    Pillars.sendHome(c.level, hNear);
                }
                if (d < 40.0D) {
                    sceneOnce(c, "act2.llegada_pilar");
                }
                if (d < 20.0D && idle(c) && SagaWorld.hour(c.level, hNear) != null) {
                    c.campaign.setCounter("entreno.activo", hNear + 1);
                    c.campaign.setCounter("entreno.t", (int) c.campaign.worldTicks);
                    c.scene("act2." + Pillars.id(hNear) + "_inicio");
                }
                return;
            }
            if (!ScenePlayer.isIdle()) {
                return;
            }
            if (SagaActions.trainingComplete(c, a)) {
                c.campaign.setCounter("entreno.mascara", mask(c) | (1 << a));
                c.campaign.setCounter("entrenado", Integer.bitCount(mask(c)));
                c.campaign.setCounter("entreno.activo", 0);
                SagaActions.trainingCleanup(c, a);
                c.scene("act2." + Pillars.id(a) + "_fin");
                c.scene("act2.tras_" + Pillars.id(a));
            } else {
                SagaActions.trainingTick(c, a);
            }
        }

        @Override
        public String objective(StoryCtx c) {
            int a = active(c);
            if (a >= 0) {
                return SagaActions.trainingObjective(c, a);
            }
            return "Entrena con los Calendarios";
        }

        @Override
        public BlockPos marker(StoryCtx c) {
            int a = active(c);
            if (a >= 0) {
                BlockPos m = SagaActions.trainingMarker(c, a);
                return m != null ? m : Pillars.pos(c.campaign, a);
            }
            int n = Pillars.nearest(c.campaign, hero(c).position(), untrainedMask(c));
            return n < 0 ? null : BlockPos.containing(Pillars.center(c.level, c.campaign, n));
        }

        @Override
        public int progress(StoryCtx c) {
            int a = active(c);
            return a >= 0 ? SagaActions.trainingProgress(c, a)[0] : trained(c);
        }

        @Override
        public int progressMax(StoryCtx c) {
            int a = active(c);
            return a >= 0 ? SagaActions.trainingProgress(c, a)[1] : 5;
        }
    }

    private static void actTwo() {
        Steps.register(new Training("act2.aprendiz", "act2.pilar_caido") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("act2.inicio");
                c.scene("voz.3");
                c.setFlag("fighters_unlocked");
            }

            @Override
            public void tick(StoryCtx c) {
                tickTraining(c);
                boolean done = trained(c) >= 3 || c.ticksInStep() > 90 * MIN;
                if (done && active(c) < 0 && idle(c)) {
                    c.goTo("act2.pilar_caido");
                }
            }
        });

        Steps.register(new S("act2.pilar_caido", "act2.preparacion") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("act2.alarma_pilar");
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                BlockPos p = Pillars.pos(c.campaign, 3);
                switch (phase(c)) {
                    case 0 -> {
                        if ((dist(h, p) < 120.0D || c.ticksInStep() > 3 * MIN) && idle(c)) {
                            SagaActions.stagePillarRaid(c);
                            c.scene("act2.pilar_cae");
                            c.scene("voz.4");
                            phase(c, 1);
                        }
                    }
                    case 1 -> {
                        if (idle(c)) {
                            Atmosphere.set(c.level, 0.3F, 0.15F, AtmosphereS2C.HORA_PERDIDA);
                            c.goTo("act2.preparacion");
                        }
                    }
                    default -> { }
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return phase(c) == 0 ? "Ve al Pilar de Medianoche" : null;
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                return phase(c) == 0 ? Pillars.pos(c.campaign, 3) : null;
            }
        });

        Steps.register(new S("act2.preparacion", "act2.infiltracion") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("act2.plan");
                c.setFlag("fighters_unlocked");
                c.setFlag("cargo_unlocked");
                ServerPlayer h = hero(c);
                if (LegionOps.nearestBase(c.campaign, h.position()) == null) {
                    LegionOps.launchBase(c.level, BlockPos.containing(LegionOps.groundAround(c.level, h.position(), 80.0D, 140.0D)), false);
                }
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                if (phase(c) == 0) {
                    if (h.getVehicle() instanceof FighterEntity) {
                        c.scene("act2.despegue");
                        phase(c, 1);
                    } else if (c.ticksInStep() > 15 * MIN && once(c, "sin_nave") && idle(c)) {
                        c.scene("act2.sin_nave");
                        Vec3 at = LegionOps.groundAround(c.level, h.position(), 5.0D, 8.0D);
                        FighterEntity.spawn(c.level, at.add(0, 0.2D, 0), h.getYRot(), false);
                        Vfx.burst(c.level, at.add(0, 1, 0), CalendarioEntity.COLORS[2], 30, 0.6F);
                    }
                } else if (idle(c)) {
                    c.goTo("act2.infiltracion");
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return phase(c) == 0 ? "Consigue una nave de la Legión" : null;
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                if (phase(c) != 0) {
                    return null;
                }
                List<FighterEntity> parked = c.level.getEntitiesOfClass(FighterEntity.class, hero(c).getBoundingBox().inflate(160.0D),
                        f -> !f.hasNpcPilot() && !f.isVehicle());
                FighterEntity best = null;
                for (FighterEntity f : parked) {
                    if (best == null || f.distanceToSqr(hero(c)) < best.distanceToSqr(hero(c))) {
                        best = f;
                    }
                }
                return best != null ? best.blockPosition() : LegionOps.nearestBase(c.campaign, hero(c).position());
            }
        });

        Steps.register(new Training("act2.aprendiz2", "act3.revelacion") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("act2.aprendiz2");
            }

            @Override
            public void tick(StoryCtx c) {
                tickTraining(c);
                if (trained(c) >= 5 && active(c) < 0 && idle(c)) {
                    c.goTo("act3.revelacion");
                }
            }
        });
    }

    // ================================================================== acto II: infiltracion en el Sentencia

    private static void infiltration() {
        Steps.register(new S("act2.infiltracion", "act2.aprendiz2") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("act2.infiltracion");
                c.scene("voz.5");
                c.campaign.setCounter("cazas0", c.counter("fighters_destroyed"));
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                ServerLevel l = c.level;
                if (phase(c) >= 1 && phase(c) <= 4 && SagaWorld.inShip(l, h)) {
                    for (LooreMob m : l.getEntitiesOfClass(LooreMob.class, h.getBoundingBox().inflate(20.0D), m -> m.getTarget() == h)) {
                        if (once(c, "alarma_inf")) {
                            c.scene("act2.alarma");
                        }
                        break;
                    }
                }
                switch (phase(c)) {
                    case 0 -> {
                        if (SagaWorld.inHangar(l, h) && idle(c)) {
                            c.scene("act2.hangar");
                            phase(c, 1);
                        }
                    }
                    case 1 -> {
                        if (dist(h, SagaWorld.cell(l)) < 6.0D && idle(c)) {
                            SagaActions.setupCell(c);
                            c.scene("act2.medianoche_celda");
                            phase(c, 2);
                        }
                    }
                    case 2 -> {
                        if (SagaActions.generatorsLeft(c) == 0 && idle(c)) {
                            c.scene("act2.liberada");
                            phase(c, 3);
                        }
                    }
                    case 3 -> {
                        SagaActions.medianocheHelp(c, false);
                        if (SagaWorld.inHangar(l, h) && idle(c)) {
                            SagaActions.spawnRazakoHangar(c);
                            c.scene("act2.razako_hangar");
                            phase(c, 4);
                        }
                    }
                    case 4 -> {
                        SagaActions.medianocheHelp(c, true);
                        RazakoEntity rz = SagaWorld.razako(l);
                        if (rz != null && rz.getHealth() < rz.getMaxHealth() * 0.5F) {
                            sceneOnce(c, "act2.razako_mitad");
                        }
                        if (rz != null && ScenePlayer.isIdle() && !rz.isKneeling() && rz.getTarget() == null) {
                            rz.setTarget(h);
                        }
                        if (c.flag("razako_arrodillado") && once(c, "razako_vencido")) {
                            c.scene("act2.razako_vencido");
                        }
                        String ch = choice(c, "perdon");
                        if (ch != null && !ScenePlayer.isPlaying("act2.razako_vencido") && once(c, "perdon_hecho")) {
                            c.scene("perdonar".equals(ch) ? "act2.razako_perdonado" : "act2.razako_muerto");
                        }
                        if (c.flag("once.perdon_hecho") && idle(c)) {
                            c.setFlag("fighters_unlocked");
                            phase(c, 5);
                        }
                        if (rz == null && !c.flag("razako_arrodillado") && inPhase(c) > 1200) {
                            // Razako desaparecio (p. ej. cayo de la nave): se da por vencido
                            c.setFlag("razako_arrodillado");
                        }
                    }
                    case 5 -> {
                        SagaActions.medianocheHelp(c, false);
                        AABB box = SagaWorld.shipBox(l);
                        boolean out = box == null || !box.inflate(8.0D).contains(h.position());
                        if (h.getVehicle() instanceof FighterEntity && out) {
                            SagaActions.pursuers(c, h);
                            c.scene("act2.escape");
                            c.campaign.setCounter("cazas0", c.counter("fighters_destroyed"));
                            phase(c, 6);
                        } else if (out && !(h.getVehicle() instanceof FighterEntity) && inPhase(c) > 6000) {
                            phase(c, 6);
                        }
                    }
                    case 6 -> {
                        DestroyerEntity d = SagaWorld.ship(l);
                        boolean far = d == null || h.distanceTo(d) > 400.0F;
                        if ((progress(c) >= 4 || far || inPhase(c) > 3 * MIN) && idle(c)) {
                            c.scene("act2.huida");
                            phase(c, 7);
                        }
                    }
                    case 7 -> {
                        if (idle(c)) {
                            c.goTo("act2.aprendiz2");
                        }
                    }
                    default -> { }
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return switch (phase(c)) {
                    case 0 -> "Entra en el hangar del Sentencia";
                    case 1 -> "Encuentra el bloque de detención";
                    case 2 -> "Destruye los generadores";
                    case 3 -> "Vuelve al hangar";
                    case 4 -> "Derrota a Razako";
                    case 5 -> "Escapa en un caza";
                    case 6 -> "Derriba o despista a los cazas";
                    default -> null;
                };
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                return switch (phase(c)) {
                    case 0 -> SagaWorld.hangarMouth(c.level);
                    case 1 -> {
                        Vec3 v = SagaWorld.cell(c.level);
                        yield v == null ? SagaWorld.hangarMouth(c.level) : BlockPos.containing(v);
                    }
                    case 3 -> {
                        Vec3 v = SagaWorld.hangarFloor(c.level);
                        yield v == null ? null : BlockPos.containing(v);
                    }
                    case 5 -> {
                        AABB hb = SagaWorld.hangarBox(c.level);
                        if (hb == null) {
                            yield null;
                        }
                        List<FighterEntity> f = c.level.getEntitiesOfClass(FighterEntity.class, hb, x -> !x.hasNpcPilot());
                        yield f.isEmpty() ? null : f.get(0).blockPosition();
                    }
                    default -> null;
                };
            }

            @Override
            public int progress(StoryCtx c) {
                return switch (phase(c)) {
                    case 2 -> 3 - SagaActions.generatorsLeft(c);
                    case 6 -> Math.min(4, c.counter("fighters_destroyed") - c.counter("cazas0"));
                    default -> 0;
                };
            }

            @Override
            public int progressMax(StoryCtx c) {
                return phase(c) == 2 ? 3 : phase(c) == 6 ? 4 : 0;
            }
        });
    }

    // ================================================================== acto III

    private static void actThree() {
        Steps.register(new S("act3.revelacion", "act3.oferta") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("act3.revelacion_titulo");
                c.campaign.clearFlag("ritmo.bases");
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                BlockPos p = Pillars.pos(c.campaign, 4);
                if (phase(c) == 0 && dist(h, p) < 16.0D && idle(c)) {
                    SagaActions.gatherHours(c, Pillars.center(c.level, c.campaign, 4));
                    c.scene("act3.revelacion");
                    c.scene("voz.6");
                    phase(c, 1);
                } else if (phase(c) == 0 && c.ticksInStep() > 40 * MIN && idle(c)) {
                    SagaWorld.blink(h, Pillars.center(c.level, c.campaign, 4).add(6, 0, 0), 90.0F);
                } else if (phase(c) == 1 && idle(c)) {
                    c.goTo("act3.oferta");
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return phase(c) == 0 ? "Ve al Pilar de Eclipse" : null;
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                return phase(c) == 0 ? Pillars.pos(c.campaign, 4) : null;
            }
        });

        Steps.register(new S("act3.oferta", "act3.escape") {
            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                ServerLevel l = c.level;
                switch (phase(c)) {
                    case 0 -> {
                        if (c.ticksInStep() > 2 * MIN && idle(c)) {
                            c.scene("act3.susurro_rareiri");
                            c.scene("voz.7");
                            c.setFlag("tregua");
                            c.campaign.clearFlag("ritmo.patrullas");
                            c.campaign.clearFlag("ritmo.cazas");
                            Vec3 land = LegionOps.groundAround(l, h.position(), 10.0D, 18.0D);
                            var ship = LegionOps.landShip(l, land, "shuttle");
                            if (ship != null) {
                                c.setEntity("lanzadera", ship);
                            }
                            phase(c, 1);
                        }
                    }
                    case 1 -> {
                        Entity s = c.entity("lanzadera");
                        boolean landed = s instanceof com.keinloree.entity.CargoShip cs && cs.getPhase() == com.keinloree.entity.CargoShip.PH_UNLOAD;
                        if ((landed || s == null && inPhase(c) > 200 || inPhase(c) > 1800) && idle(c)) {
                            c.scene("act3.lanzadera");
                            phase(c, 2);
                        }
                    }
                    case 2 -> {
                        Entity s = c.entity("lanzadera");
                        boolean board = s == null ? inPhase(c) > 200 : h.distanceTo(s) < 6.0F;
                        Vec3 floor = SagaWorld.hangarFloor(l);
                        if (board && floor != null && idle(c)) {
                            if (s instanceof com.keinloree.entity.CargoShip cs) {
                                cs.leaveNow();
                            }
                            SagaWorld.blink(h, floor, SagaWorld.ship(l).getYRot());
                            phase(c, 3);
                        }
                    }
                    case 3 -> {
                        if (SagaWorld.inShip(l, h)) {
                            sceneOnce(c, "act3.tripulacion");
                        }
                        KeinLooreEntity k = SagaWorld.kein(l);
                        if (k != null && h.distanceTo(k) < 8.0F && idle(c)) {
                            c.scene("act3.kein_historia");
                            phase(c, 4);
                        } else if (k == null && inPhase(c) > 600 && SagaWorld.ship(l) != null) {
                            SagaWorld.ship(l).spawnCrew(l);
                        }
                    }
                    case 4 -> {
                        String ch = choice(c, "oferta");
                        if (ch != null && !ScenePlayer.isPlaying("act3.kein_historia")) {
                            if ("saber".equals(ch)) {
                                c.setFlag("pregunto_mas");
                                c.scene("act3.kein_silencio");
                                phase(c, 5);
                            } else {
                                decide(c, ch);
                            }
                        }
                    }
                    case 5 -> {
                        String ch = choice(c, "oferta2");
                        if (ch != null && !ScenePlayer.isPlaying("act3.kein_silencio")) {
                            decide(c, ch);
                        }
                    }
                    default -> { }
                }
            }

            void decide(StoryCtx c, String ch) {
                if ("unirse".equals(ch)) {
                    c.campaign.path = Campaign.Path.NEXO;
                    c.goTo("nexo.marca");
                } else {
                    c.campaign.path = Campaign.Path.AMANECER;
                    c.scene("act3.rechazo");
                    c.goTo("act3.escape");
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return switch (phase(c)) {
                    case 2 -> "Sube a la lanzadera";
                    case 3 -> "Ve al puente";
                    default -> null;
                };
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                if (phase(c) == 2) {
                    Entity s = c.entity("lanzadera");
                    return s == null ? null : s.blockPosition();
                }
                if (phase(c) == 3) {
                    KeinLooreEntity k = SagaWorld.kein(c.level);
                    return k == null ? null : k.blockPosition();
                }
                return null;
            }
        });

        Steps.register(new S("act3.escape", "amanecer.bombardeo") {
            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                AABB box = SagaWorld.shipBox(c.level);
                boolean out = box == null || !box.inflate(4.0D).contains(h.position());
                if (out && c.ticksInStep() > 100 && idle(c)) {
                    c.goTo("amanecer.bombardeo");
                } else if (c.ticksInStep() > 10 * MIN && idle(c)) {
                    BlockPos alba = Pillars.pos(c.campaign, 0);
                    Vec3 to = alba != null ? Pillars.center(c.level, c.campaign, 0).add(5, 0, 0) : LegionOps.groundAround(c.level, h.position(), 150, 200);
                    SagaWorld.blink(h, to, 0.0F);
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return "Escapa del Sentencia";
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                return SagaWorld.hangarMouth(c.level);
            }
        });
    }

    // ================================================================== ruta del Amanecer

    private static void dawnRoute() {
        Steps.register(new S("amanecer.bombardeo", "amanecer.luces") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.campaign.clearFlag("tregua");
                c.scene("amanecer.bombardeo");
                c.campaign.setCounter("pesadas0", c.counter("turrets_heavy_destroyed"));
                c.campaign.setCounter("integridad", 100);
                DestroyerEntity d = SagaWorld.ship(c.level);
                Vec3 alba = Pillars.center(c.level, c.campaign, 0);
                if (d != null) {
                    Vec3 off = alba.add(70.0D, 0.0D, 0.0D);
                    Vec3 target = new Vec3(off.x, com.keinloree.ship.ShipTransform.DEFAULT_ANCHOR_Y, off.z);
                    if (!d.autopilotTo(target, 90.0F, () -> c.campaign.setFlag("bombardeo.llegada"))) {
                        c.setFlag("bombardeo.llegada");
                    }
                }
                for (int i : new int[]{0, 1}) {
                    Pillars.sendHome(c.level, i);
                }
            }

            @Override
            public void tick(StoryCtx c) {
                long t = c.ticksInStep();
                if (!c.flag("bombardeo.llegada") && t > 2 * 2400) {
                    c.setFlag("bombardeo.llegada");
                }
                if (c.flag("bombardeo.llegada")) {
                    sceneOnce(c, "amanecer.bombardeo_inicio");
                    SagaActions.bombard(c);
                }
                if (progress(c) >= 3) {
                    sceneOnce(c, "amanecer.bombardeo_mitad");
                }
                if (phase(c) == 0 && (progress(c) >= 6 || t > 30 * MIN) && idle(c)) {
                    c.scene("amanecer.bombardeo_fin");
                    phase(c, 1);
                } else if (phase(c) == 1 && idle(c)) {
                    c.goTo("amanecer.luces");
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return phase(c) == 0 ? "Destruye torretas pesadas del Sentencia" : null;
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                DestroyerEntity d = SagaWorld.ship(c.level);
                if (d == null) {
                    return null;
                }
                TurbolaserEntity best = null;
                for (TurbolaserEntity tl : c.level.getEntitiesOfClass(TurbolaserEntity.class, d.getBoundingBoxForCulling(),
                        x -> x.isHeavy() && !x.isDestroyed())) {
                    if (best == null || tl.distanceToSqr(hero(c)) < best.distanceToSqr(hero(c))) {
                        best = tl;
                    }
                }
                return best == null ? d.blockPosition() : best.blockPosition();
            }

            @Override
            public int progress(StoryCtx c) {
                return Math.min(6, c.counter("turrets_heavy_destroyed") - c.counter("pesadas0"));
            }

            @Override
            public int progressMax(StoryCtx c) {
                return 6;
            }
        });

        Steps.register(new S("amanecer.luces", "amanecer.asalto") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("amanecer.luces");
            }

            int lit(StoryCtx c) {
                return c.counter("luces");
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                if (phase(c) == 0) {
                    int next = Pillars.nearest(c.campaign, h.position(), ~lit(c) & 0x1F);
                    if (next >= 0) {
                        Vec3 ctr = Pillars.center(c.level, c.campaign, next);
                        if (dist(h, ctr) < 64.0D && SagaWorld.hour(c.level, next) == null && Pillars.built(c.campaign, next)) {
                            Pillars.sendHome(c.level, next);
                        }
                        if (h.position().distanceTo(ctr) < 5.0D) {
                            if (once(c, "luz_oleada." + next)) {
                                LegionOps.squad(c.level, LegionOps.groundAround(c.level, ctr, 16.0D, 22.0D), ctr.y, Rank.SARGENTO, 3, h);
                            }
                            int k = c.campaign.inc("luz_t." + next, 1);
                            if (k % 20 == 0) {
                                Vfx.beam(c.level, ctr.add(0, 1, 0), h.position().add(0, 1, 0), Pillars.color(next), 0.25F, 22);
                            }
                            if (k >= 200) {
                                c.campaign.setCounter("luces", lit(c) | (1 << next));
                                Vfx.pillar(c.level, ctr, 120.0F, 2.5F, Pillars.color(next), 80);
                                c.scene("amanecer.luz_" + Pillars.id(next));
                            }
                        }
                    }
                    if (Integer.bitCount(lit(c)) >= 5 && idle(c)) {
                        c.scene("amanecer.escudo_roto");
                        phase(c, 1);
                    }
                } else if (idle(c)) {
                    c.goTo("amanecer.asalto");
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return phase(c) == 0 ? "Enciende los pilares" : null;
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                int next = Pillars.nearest(c.campaign, hero(c).position(), ~lit(c) & 0x1F);
                return next < 0 ? null : BlockPos.containing(Pillars.center(c.level, c.campaign, next));
            }

            @Override
            public int progress(StoryCtx c) {
                return Integer.bitCount(lit(c));
            }

            @Override
            public int progressMax(StoryCtx c) {
                return 5;
            }
        });

        Steps.register(new S("amanecer.asalto", "amanecer.hora_cero") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("amanecer.asalto");
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                ServerLevel l = c.level;
                SagaActions.keepCompanions(c, h);
                if (c.flag("razako_perdonado") && SagaWorld.inShip(l, h)) {
                    sceneOnce(c, "amanecer.razako_aliado");
                }
                Vec3 bridge = SagaWorld.bridge(l);
                if (c.flag("razako_muerto") && dist(h, bridge) < 24.0D) {
                    sceneOnce(c, "amanecer.eco_razako");
                }
                boolean echoAlive = c.flag("once.esc.amanecer.eco_razako") && SagaWorld.razako(l) != null;
                if (dist(h, bridge) < 12.0D && !echoAlive && idle(c)) {
                    c.goTo("amanecer.hora_cero");
                } else if (bridge == null && c.ticksInStep() > 600) {
                    c.goTo("amanecer.hora_cero");
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return "Ábrete paso hasta el puente";
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                Vec3 b = SagaWorld.bridge(c.level);
                return b == null ? null : BlockPos.containing(b);
            }
        });

        Steps.register(new S("amanecer.hora_cero", "final.amanecer") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("amanecer.final_inicio");
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                SagaActions.keepCompanions(c, h);
                KeinLooreEntity k = SagaWorld.kein(c.level);
                switch (phase(c)) {
                    case 0 -> {
                        if (k != null && ScenePlayer.isIdle() && !k.isLoreDormant() && k.getTarget() == null) {
                            k.setTarget(h);
                        }
                        if (k != null && k.getHealth() <= k.getMaxHealth() * 0.6F) {
                            sceneOnce(c, "amanecer.fase2");
                        }
                        if (k != null && k.getHealth() <= k.getMaxHealth() * 0.3F) {
                            sceneOnce(c, "amanecer.fase3");
                        }
                        if (k == null && c.ticksInStep() > 400) {
                            c.setFlag("kein_vencido");
                        }
                        if (c.flag("kein_vencido") && idle(c)) {
                            boolean secret = c.flag("razako_perdonado") && trained(c) >= 5 && c.flag("pregunto_mas");
                            c.scene(secret ? "amanecer.kein_vencido_secreto" : "amanecer.kein_vencido");
                            phase(c, 1);
                        }
                    }
                    case 1 -> {
                        String ch = choice(c, "final");
                        if (ch != null && ScenePlayer.isIdle()) {
                            c.goTo("devolver".equals(ch) ? "final.sexta" : "final.amanecer");
                        }
                    }
                    default -> { }
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return phase(c) == 0 ? "Derrota a Kein" : null;
            }
        });

        ending("final.amanecer", "amanecer");
        ending("final.sexta", "sexta");
    }

    private static void ending(String id, String name) {
        Steps.register(new S(id, "fin") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.campaign.clearFlag("ritmo.patrullas");
                c.campaign.clearFlag("ritmo.cazas");
                c.campaign.clearFlag("ritmo.bases");
                c.campaign.clearFlag("ritmo.inquietud");
                c.campaign.clearFlag("omni");
                c.scene(id);
            }

            @Override
            public void tick(StoryCtx c) {
                if (c.ticksInStep() > 40 && idle(c)) {
                    c.setFlag("terminado");
                    c.setFlag("final." + name);
                    c.goTo("fin");
                }
            }
        });
    }

    // ================================================================== ruta del Nexo

    private static void nexusRoute() {
        Steps.register(new S("nexo.marca", "nexo.traicion") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.campaign.clearFlag("omni");
                c.scene("nexo.marca");
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                Entity who = target(c);
                if (who == null && !c.flag("razako_muerto") && c.ticksInStep() > 100 && once(c, "razako_hangar_nexo")) {
                    Vec3 f = SagaWorld.hangarFloor(c.level);
                    if (f != null) {
                        RazakoEntity rz = SagaActions.spawnRazako(c.level, f);
                        SagaWorld.makeAlly(rz);
                    }
                }
                if (phase(c) == 0 && who != null && h.distanceTo(who) < 5.0F && idle(c)) {
                    c.scene(c.flag("razako_muerto") ? "nexo.orrel" : "nexo.razako");
                    phase(c, 1);
                } else if (phase(c) == 0 && c.ticksInStep() > 20 * MIN && idle(c)) {
                    c.scene(c.flag("razako_muerto") ? "nexo.orrel" : "nexo.razako");
                    phase(c, 1);
                } else if (phase(c) == 1 && idle(c)) {
                    c.goTo("nexo.traicion");
                }
            }

            @Nullable
            Entity target(StoryCtx c) {
                return c.flag("razako_muerto") ? SagaWorld.crew(c.level, "orrel") : SagaWorld.razako(c.level);
            }

            @Override
            public String objective(StoryCtx c) {
                if (phase(c) != 0) {
                    return null;
                }
                return c.flag("razako_muerto") ? "Habla con el Almirante Orrel en el puente" : "Habla con Razako en el hangar";
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                Entity e = phase(c) == 0 ? target(c) : null;
                return e == null ? null : e.blockPosition();
            }
        });

        Steps.register(new S("nexo.traicion", "nexo.ultimo_pilar") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("nexo.traicion");
                c.setFlag("anclas_rompibles.cenit");
                c.campaign.setCounter("anclas_rotas.cenit", 0);
                CalendarioEntity cenit = SagaWorld.hour(c.level, 1);
                if (cenit != null) {
                    Vfx.pillar(c.level, cenit.position(), 30.0F, cenit.color(), 30);
                    cenit.discard();
                }
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                switch (phase(c)) {
                    case 0 -> {
                        if (progress(c) >= 3 && idle(c)) {
                            c.scene("nexo.anclas_rotas");
                            c.scene("nexo.ocaso");
                            phase(c, 1);
                        }
                    }
                    case 1 -> {
                        CalendarioEntity o = SagaWorld.hour(c.level, 2);
                        boolean started = sceneDone(c, "nexo.ocaso") || c.flag("once.esc.nexo.ocaso");
                        if (ScenePlayer.isIdle() && (o == null || o.getHealth() < o.getMaxHealth() * 0.15F)) {
                            c.scene("nexo.ocaso_huye");
                            phase(c, 2);
                        } else if (o != null && ScenePlayer.isIdle() && o.getTarget() == null && started) {
                            o.setTarget(h);
                        }
                    }
                    case 2 -> {
                        if (idle(c)) {
                            c.goTo("nexo.ultimo_pilar");
                        }
                    }
                    default -> { }
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return switch (phase(c)) {
                    case 0 -> "Rompe las anclas de luz";
                    case 1 -> "Derrota a Ocaso";
                    default -> null;
                };
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                return phase(c) == 0 ? Pillars.pos(c.campaign, 1) : null;
            }

            @Override
            public int progress(StoryCtx c) {
                return phase(c) == 0 ? Math.min(3, c.counter("anclas_rotas.cenit")) : 0;
            }

            @Override
            public int progressMax(StoryCtx c) {
                return phase(c) == 0 ? 3 : 0;
            }
        });

        Steps.register(new S("nexo.ultimo_pilar", "final.nexo") {
            @Override
            public void enter(StoryCtx c) {
                super.enter(c);
                c.scene("nexo.ultimo_pilar");
                Pillars.topple(c.level, 0);
                Pillars.topple(c.level, 3);
                ServerPlayer h = hero(c);
                RazakoEntity rz = SagaWorld.razako(c.level);
                if (rz != null) {
                    SagaWorld.makeAlly(rz);
                    rz.teleportTo(h.getX() + 2, h.getY(), h.getZ() + 2);
                } else {
                    for (LooreMob m : LegionOps.squad(c.level, LegionOps.groundAround(c.level, h.position(), 3.0D, 5.0D), h.getY(), Rank.GUARDIA_NEXO, 0, null)) {
                        SagaWorld.makeAlly(m);
                        c.setEntity("escolta.0", m);
                    }
                    for (int i = 1; i < 4; i++) {
                        for (LooreMob m : LegionOps.squad(c.level, LegionOps.groundAround(c.level, h.position(), 3.0D, 5.0D), h.getY(), Rank.GUARDIA_NEXO, 0, null)) {
                            SagaWorld.makeAlly(m);
                            c.setEntity("escolta." + i, m);
                        }
                    }
                }
            }

            @Override
            public void tick(StoryCtx c) {
                ServerPlayer h = hero(c);
                SagaActions.keepCompanions(c, h);
                BlockPos p = Pillars.pos(c.campaign, 4);
                CalendarioEntity e = SagaWorld.hour(c.level, 4);
                switch (phase(c)) {
                    case 0 -> {
                        if (dist(h, p) < 64.0D && e == null && Pillars.built(c.campaign, 4)) {
                            Pillars.sendHome(c.level, 4);
                        }
                        if (dist(h, p) < 20.0D && idle(c)) {
                            c.scene("nexo.eclipse");
                            phase(c, 1);
                        }
                    }
                    case 1 -> {
                        if (e != null && ScenePlayer.isIdle() && e.getTarget() == null && e.getMode() == CalendarioEntity.MODE_HOSTILE) {
                            e.setTarget(h);
                        }
                        if ((e == null || e.getHealth() <= e.getMaxHealth() * 0.12F) && ScenePlayer.isIdle() && inPhase(c) > 100) {
                            if (e != null) {
                                e.setMode(CalendarioEntity.MODE_STILL);
                                e.play(com.keinloree.entity.Animated.A_KNEEL);
                            }
                            c.scene("nexo.eclipse_cae");
                            phase(c, 2);
                        }
                    }
                    case 2 -> {
                        if (idle(c)) {
                            c.goTo("final.nexo");
                        }
                    }
                    default -> { }
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return switch (phase(c)) {
                    case 0 -> "Derriba el Pilar de Eclipse";
                    case 1 -> "Detén el tiempo de Eclipse";
                    default -> null;
                };
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                return phase(c) == 0 ? Pillars.pos(c.campaign, 4) : null;
            }
        });

        ending("final.nexo", "nexo");
    }

    // ================================================================== reinicio

    /** Devuelve el mundo al principio: retira entidades de la historia, la nave y el equipo dejado. */
    public static void resetWorld(ServerLevel level) {
        Campaign c = Campaign.get(level);
        for (var id : c.entities.values()) {
            Entity e = level.getEntity(id);
            if (e != null && !(e instanceof ServerPlayer)) {
                e.discard();
            }
        }
        DestroyerEntity d = SagaWorld.ship(level);
        if (d != null) {
            d.destroy();
        }
        Atmosphere.set(level, 0.0F, 0.0F, 0);
        Later.reset();
        LegionOps.reset();
        java.util.List<Entity> drop = new java.util.ArrayList<>();
        for (Entity e : level.getAllEntities()) {
            if (e instanceof ItemEntity ie && ie.getPersistentData().getBoolean("keinloree_story")) {
                drop.add(e);
            }
        }
        drop.forEach(Entity::discard);
        Pillars.forget(c);
        for (ServerPlayer p : level.players()) {
            c.tale(p.getUUID()).techniques = 0;
            c.tale(p.getUUID()).legionAlly = false;
            Techniques.sendState(p);
        }
    }
}
