package com.keinloree.story;

import com.keinloree.entity.Rank;
import com.keinloree.registry.ModItems;
import com.keinloree.registry.ModSounds;
import com.keinloree.ship.DestroyerEntity;
import com.keinloree.vfx.Vfx;
import com.keinloree.world.LegionOps;
import com.keinloree.world.Pillars;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;

/**
 * Ritmo de fondo de la invasion (bases, patrullas, cazas), inquietud constante (eventos raros cada pocos minutos) y
 * omnisciencia (susurros de Kein que encajan con lo que hace el heroe).
 */
public final class Unease {
    private static final int MIN = 1200;
    private static final Deque<Long> PLACED = new ArrayDeque<>();
    private static final Map<String, Set<Integer>> USED = new HashMap<>();
    private static Vec3 lastPos;
    private static long lastDay = -1;

    private Unease() {
    }

    public static void reset() {
        PLACED.clear();
        USED.clear();
        lastPos = null;
        lastDay = -1;
    }

    public static void onPlaced(long now) {
        PLACED.addLast(now);
        while (PLACED.size() > 200) {
            PLACED.removeFirst();
        }
    }

    private static boolean calm(StoryCtx c, ServerPlayer h) {
        return ScenePlayer.isIdle() && !Cinematics.isActive(h) && !c.flag("terminado");
    }

    private static int between(RandomSource r, int a, int b) {
        return a + r.nextInt(Math.max(1, b - a));
    }

    public static void tick(StoryCtx c) {
        ServerPlayer h = c.hero();
        if (h == null || h.level() != c.level) {
            return;
        }
        long now = c.campaign.worldTicks;
        RandomSource r = c.level.random;
        trackDistance(c, h);
        // fin de la tregua (60 s tras rechazar a Kein) y fin de la alarma
        int tf = c.counter("tregua_fin");
        if (tf > 0 && now >= tf) {
            c.campaign.setCounter("tregua_fin", 0);
            c.campaign.clearFlag("tregua");
            c.scene("act3.tregua_fin");
        }
        int al = c.counter("alarma_hasta");
        if (al > 0 && now >= al) {
            c.campaign.setCounter("alarma_hasta", 0);
            DestroyerEntity d = SagaWorld.ship(c.level);
            if (d != null) {
                d.setAlarm(false);
            }
        }
        if (now % 20 != 0) {
            return;
        }
        boolean hostileWorld = !Factions.legionAlly(h) && !c.flag("tregua") && !c.flag("legion_deshecha") && !c.flag("terminado");
        boolean exposed = calm(c, h) && SagaWorld.openSky(h) && !Pillars.inSafeZone(c.level, h.position()) && !SagaWorld.inShip(c.level, h);
        // ---- ritmo de la invasion
        if (hostileWorld && exposed) {
            if (c.flag("ritmo.bases") && c.counter("bases") < 5 && now - c.counter("ritmo.base_t") > 30 * MIN) {
                c.campaign.setCounter("ritmo.base_t", (int) now);
                LegionOps.launchBase(c.level, BlockPos.containing(LegionOps.groundAround(c.level, h.position(), 110.0D, 180.0D)), false);
            }
            if (c.flag("ritmo.bases") && !c.flag("fortaleza") && now - c.counter("llegada_t") > 150 * MIN) {
                DestroyerEntity d = SagaWorld.ship(c.level);
                if (d != null && c.level.isLoaded(d.blockPosition())) {
                    c.setFlag("fortaleza");
                    LegionOps.launchBase(c.level, BlockPos.containing(d.getX(), 64, d.getZ()), true);
                }
            }
            if (c.flag("ritmo.patrullas") && now >= c.counter("ritmo.patrulla_next")) {
                if (c.counter("ritmo.patrulla_next") > 0) {
                    LegionOps.dropPatrol(c.level, h, r.nextInt(4) == 0 ? Rank.TENIENTE : Rank.SARGENTO, 3 + r.nextInt(2));
                }
                c.campaign.setCounter("ritmo.patrulla_next", (int) now + between(r, 8 * MIN, 12 * MIN));
            }
            if (c.flag("ritmo.cazas") && now >= c.counter("ritmo.cazas_next")) {
                if (c.counter("ritmo.cazas_next") > 0) {
                    LegionOps.flyover(c.level, h, 2 + r.nextInt(2));
                }
                boolean act2 = c.campaign.step.startsWith("act2");
                c.campaign.setCounter("ritmo.cazas_next", (int) now + (act2 ? between(r, 4 * MIN, 7 * MIN) : between(r, 6 * MIN, 10 * MIN)));
            }
        }
        // ---- inquietud
        if (c.flag("llegada") && !c.flag("terminado") && calm(c, h)) {
            if (c.campaign.nextUneaseAt == 0) {
                c.campaign.nextUneaseAt = now + between(r, 3 * MIN, 7 * MIN);
            } else if (now >= c.campaign.nextUneaseAt) {
                c.campaign.nextUneaseAt = now + between(r, 3 * MIN, 7 * MIN);
                uneaseEvent(c, h, r);
            }
        }
        // ---- omnisciencia
        if (c.flag("omni") && !Factions.legionAlly(h) && !c.flag("terminado") && calm(c, h)) {
            if (c.campaign.nextWhisperAt == 0) {
                c.campaign.nextWhisperAt = now + between(r, 6 * MIN, 12 * MIN);
            } else if (now >= c.campaign.nextWhisperAt) {
                c.campaign.nextWhisperAt = now + between(r, 6 * MIN, 12 * MIN);
                whisper(c, h, r);
            }
        }
    }

    private static void trackDistance(StoryCtx c, ServerPlayer h) {
        long day = c.level.getDayTime() / 24000L;
        if (day != lastDay) {
            lastDay = day;
            c.campaign.setCounter("distancia", 0);
        }
        if (c.campaign.worldTicks % 20 != 0) {
            return;
        }
        Vec3 p = h.position();
        if (lastPos != null && h.getVehicle() == null) {
            double d = Math.hypot(p.x - lastPos.x, p.z - lastPos.z);
            if (d < 30.0D && d > 0.5D) {
                c.campaign.counters.merge("distancia", (int) Math.round(d), Integer::sum);
            }
        }
        lastPos = p;
    }

    private static void uneaseEvent(StoryCtx c, ServerPlayer h, RandomSource r) {
        int last = c.counter("inquietud.ultimo");
        int ev;
        do {
            ev = r.nextInt(8);
        } while (ev == last);
        c.campaign.setCounter("inquietud.ultimo", ev);
        ServerLevel l = c.level;
        switch (ev) {
            case 0 -> h.playNotifySound(ModSounds.DESTROYER_HUM.get(), SoundSource.AMBIENT, 1.4F, 0.5F);
            case 1 -> h.playNotifySound(ModSounds.ALARM.get(), SoundSource.AMBIENT, 0.25F, 0.8F);
            case 2 -> {
                Vec3 back = h.position().subtract(h.getLookAngle().multiply(6, 0, 6));
                for (int i = 0; i < 4; i++) {
                    Later.run(i * 9, () -> l.playSound(null, BlockPos.containing(back), SoundEvents.STONE_STEP, SoundSource.HOSTILE, 0.9F, 0.8F));
                }
            }
            case 3 -> {
                ScreenFx.send(h, ScreenFx.TINT, 24, 0.5F, 0x3CFF7A);
                h.playNotifySound(ModSounds.GLITCH.get(), SoundSource.AMBIENT, 0.5F, 0.5F);
            }
            case 4 -> {
                DestroyerEntity d = SagaWorld.ship(l);
                Vec3 look = d != null ? d.position() : h.position().add(0, 80, 0);
                List<Animal> animals = l.getEntitiesOfClass(Animal.class, h.getBoundingBox().inflate(32.0D));
                Later.repeat(0, 60, n -> {
                    for (Animal a : animals) {
                        if (a.isAlive()) {
                            a.getNavigation().stop();
                            a.getLookControl().setLookAt(look.x, look.y, look.z, 30.0F, 90.0F);
                        }
                    }
                    return true;
                });
            }
            case 5 -> {
                if (l.isNight()) {
                    Vec3 at = LegionOps.groundAround(l, h.position(), 20.0D, 30.0D).add(0, 1.6, 0);
                    Vec3 side = h.position().subtract(at).normalize().cross(new Vec3(0, 1, 0)).scale(0.25D);
                    Vfx.orb(l, at.add(side), 0x4CFF7A, 0.12F, 120);
                    Vfx.orb(l, at.subtract(side), 0x4CFF7A, 0.12F, 120);
                } else {
                    ScreenFx.send(h, ScreenFx.GLITCH, 8, 0.5F, 0x3CFF7A);
                }
            }
            case 6 -> ScreenFx.send(h, ScreenFx.GLITCH, 10, 0.6F, 0x3CFF7A);
            default -> {
                int k = 2 + c.counter("radio.n") % 5;
                c.campaign.inc("radio.n", 1);
                c.scene("radio." + k);
            }
        }
    }

    private static void whisper(StoryCtx c, ServerPlayer h, RandomSource r) {
        ServerLevel l = c.level;
        List<String> cats = new ArrayList<>();
        boolean sky = SagaWorld.openSky(h);
        if (!sky && h.getY() < 50) {
            cats.add("cueva");
        }
        if (l.isNight() && sky) {
            cats.add("noche");
        }
        if (l.isRainingAt(h.blockPosition().above())) {
            cats.add("lluvia");
        }
        if (h.getY() > 120) {
            cats.add("altura");
        }
        if (c.campaign.tale(h.getUUID()).deaths >= 3) {
            cats.add("muertes");
        }
        ItemStack hand = h.getMainHandItem();
        if (hand.getItem() instanceof TieredItem || hand.getItem() instanceof BowItem || hand.getItem() instanceof CrossbowItem
                || hand.getItem() instanceof TridentItem) {
            cats.add("arma");
        }
        if (h.getInventory().contains(new ItemStack(ModItems.DECODE_CORE.get()))) {
            cats.add("fragmento");
        }
        DestroyerEntity d = SagaWorld.ship(l);
        if (d != null && h.distanceTo(d) < 120.0F) {
            cats.add("cerca_nave");
        }
        long now = c.campaign.worldTicks;
        long recent = PLACED.stream().filter(t -> now - t < 5 * MIN).count();
        if (recent > 40) {
            cats.add("construye");
        }
        if (now - c.counter("ult_muerte") < 600 && c.counter("ult_muerte") > 0) {
            cats.add("mata");
        }
        if (r.nextInt(5) == 0) {
            cats.add("bioma");
        }
        if (cats.isEmpty() || r.nextInt(4) == 0) {
            cats.add("generico");
        }
        String cat = cats.get(r.nextInt(cats.size()));
        Script.Scene sc = Script.get("omni." + cat);
        if (sc == null) {
            return;
        }
        List<String> lines = new ArrayList<>();
        for (Script.Instr in : sc.instrs) {
            if (in.op == Script.Op.SAY) {
                lines.add(in.body);
            }
        }
        if (lines.isEmpty()) {
            return;
        }
        Set<Integer> used = USED.computeIfAbsent(cat, k -> new HashSet<>());
        if (used.size() >= lines.size()) {
            used.clear();
        }
        int i;
        do {
            i = r.nextInt(lines.size());
        } while (used.contains(i));
        used.add(i);
        Whisper.send(h, Script.fill(lines.get(i), h, l, c.campaign));
    }
}
