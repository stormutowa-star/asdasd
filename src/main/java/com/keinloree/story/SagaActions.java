package com.keinloree.story;

import com.keinloree.ability.Techniques;
import com.keinloree.entity.Animated;
import com.keinloree.entity.CalendarioEntity;
import com.keinloree.entity.CargoShip;
import com.keinloree.entity.Droid;
import com.keinloree.entity.KeinHologramEntity;
import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.entity.LooreMob;
import com.keinloree.entity.Rank;
import com.keinloree.entity.RazakoEntity;
import com.keinloree.entity.TimeStop;
import com.keinloree.net.AtmosphereS2C;
import com.keinloree.registry.ModBlocks;
import com.keinloree.registry.ModEntities;
import com.keinloree.registry.ModItems;
import com.keinloree.registry.ModSounds;
import com.keinloree.ship.BoltEntity;
import com.keinloree.ship.DestroyerEntity;
import com.keinloree.ship.FighterEntity;
import com.keinloree.ship.TurbolaserEntity;
import com.keinloree.util.LooreDamage;
import com.keinloree.vfx.Vfx;
import com.keinloree.world.LegionOps;
import com.keinloree.world.Pillars;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Acciones (@accion) y cinematicas (@cinematica) que nombra el guion, y la logica de los retos de la campana. */
public final class SagaActions {
    private static final int GREEN = 0x4CFF7A;

    private SagaActions() {
    }

    @Nullable
    private static ServerPlayer hero(StoryCtx c) {
        ServerPlayer h = c.hero();
        if (h == null && !c.players().isEmpty()) {
            h = c.players().get(0);
        }
        return h;
    }

    private static Vec3 ground(ServerLevel l, double x, double z) {
        BlockPos p = BlockPos.containing(x, 0, z);
        if (!l.isLoaded(p)) {
            return new Vec3(x, 70, z);
        }
        return new Vec3(x, l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ()), z);
    }

    private static Vec3 flatLook(ServerPlayer h) {
        Vec3 l = h.getLookAngle();
        Vec3 f = new Vec3(l.x, 0, l.z);
        return f.lengthSqr() < 1.0E-4D ? new Vec3(0, 0, 1) : f.normalize();
    }

    private static float yawTo(Vec3 from, Vec3 to) {
        return (float) Math.toDegrees(Math.atan2(-(to.x - from.x), to.z - from.z));
    }

    private static int hourArg(String[] a) {
        return a.length > 0 ? Pillars.hourOf(a[0]) : -1;
    }

    // ================================================================== prologo

    public static void dropFragment(ServerLevel l, @Nullable ServerPlayer h) {
        if (h == null) {
            return;
        }
        Vec3 f = flatLook(h);
        double d = 12.0D + l.random.nextDouble() * 8.0D;
        Vec3 at = ground(l, h.getX() + f.x * d, h.getZ() + f.z * d);
        Vfx.beam(l, at.add(0, 90, 0), at, GREEN, 1.2F, 30);
        Vfx.explosion(l, at, 1.2F);
        Vfx.shockwave(l, at, GREEN, 4.0F, 16);
        l.playSound(null, BlockPos.containing(at), ModSounds.KEIN_BEAM.get(), SoundSource.AMBIENT, 3.0F, 0.6F);
        ItemEntity ie = new ItemEntity(l, at.x, at.y + 0.5D, at.z, new ItemStack(ModItems.DECODE_CORE.get()));
        ie.setUnlimitedLifetime();
        ie.setGlowingTag(true);
        ie.setDeltaMovement(Vec3.ZERO);
        l.addFreshEntity(ie);
    }

    // ================================================================== acto I

    public static void villagerEvent(StoryCtx c, long t) {
        ServerLevel l = c.level;
        ServerPlayer h = c.hero();
        if (h == null) {
            return;
        }
        int fase = c.counter("aldeano.fase");
        if (fase == 0) {
            AbstractVillager v = null;
            for (AbstractVillager x : l.getEntitiesOfClass(AbstractVillager.class, h.getBoundingBox().inflate(64.0D))) {
                v = x;
                break;
            }
            if (v == null && t >= 20 * 1200) {
                Vec3 at = LegionOps.groundAround(l, h.position(), 18.0D, 22.0D);
                v = new WanderingTrader(net.minecraft.world.entity.EntityType.WANDERING_TRADER, l);
                v.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
                l.addFreshEntity(v);
            }
            if (v == null || !ScenePlayer.isIdle()) {
                return;
            }
            c.setEntity("aldeano", v);
            v.setNoAi(true);
            Vec3 sky = SagaWorld.ship(l) != null ? SagaWorld.ship(l).position().add(0, -20, 0) : v.position().add(0, 90, 0);
            Vfx.beam(l, sky, v.position().add(0, 1, 0), GREEN, 0.6F, 70);
            l.playSound(null, v.blockPosition(), ModSounds.KEIN_BEAM.get(), SoundSource.HOSTILE, 3.0F, 0.7F);
            c.cinematic("aldeano");
            c.campaign.setCounter("aldeano.fase", 1);
            c.campaign.setCounter("aldeano.t", (int) c.campaign.worldTicks);
        } else if (fase == 1 && c.campaign.worldTicks - c.counter("aldeano.t") > 80) {
            Entity v = c.entity("aldeano");
            c.campaign.setCounter("aldeano.fase", 2);
            if (v == null) {
                return;
            }
            Droid d = ModEntities.DROID.get().create(l);
            if (d != null) {
                d.moveTo(v.getX(), v.getY(), v.getZ(), v.getYRot(), 0.0F);
                d.setRank(Rank.SOLDADO);
                d.setPersistenceRequired();
                l.addFreshEntity(d);
            }
            Vfx.codeRain(l, v.position(), 2.5F, 60);
            Vfx.burst(l, v.position().add(0, 1, 0), GREEN, 40, 0.5F);
            v.discard();
            c.scene("act1.aldeano");
        }
    }

    public static RazakoEntity spawnRazako(ServerLevel l, Vec3 at) {
        RazakoEntity old = SagaWorld.razako(l);
        if (old != null) {
            old.teleportTo(at.x, at.y, at.z);
            return old;
        }
        RazakoEntity rz = ModEntities.RAZAKO.get().create(l);
        LegionOps.placeSafe(l, rz, at.x, at.y, at.z, 0.0F);
        l.addFreshEntity(rz);
        Vfx.burst(l, rz.position().add(0, 1.5, 0), GREEN, 40, 0.6F);
        Campaign c = Campaign.get(l);
        c.entities.put("razako", rz.getUUID());
        c.setDirty();
        return rz;
    }

    // ================================================================== entrenamientos

    private static final String ECO = "keinloree_eco";
    private static final String WAVE = "keinloree_oleada";
    private static final String HUNT = "keinloree_ocaso";
    private static final int[] WAVES = {5, 6, 8};
    private static final int PLATFORMS = 10;

    private static Vec3 pillarCenter(StoryCtx c, int h) {
        return Pillars.center(c.level, c.campaign, h);
    }

    private static void tag(List<LooreMob> mobs, String tag) {
        for (LooreMob m : mobs) {
            m.getPersistentData().putBoolean(tag, true);
        }
    }

    private static int living(StoryCtx c, Vec3 at, double r, String tag) {
        return c.level.getEntitiesOfClass(LooreMob.class, new AABB(at, at).inflate(r), m -> m.isAlive() && m.getPersistentData().getBoolean(tag)).size();
    }

    private static void spawnEchoes(StoryCtx c, int n) {
        ServerPlayer h = c.hero();
        Vec3 ctr = pillarCenter(c, 0);
        for (int i = 0; i < n; i++) {
            Vec3 at = LegionOps.groundAround(c.level, ctr, 8.0D, 13.0D);
            List<LooreMob> m = LegionOps.squad(c.level, at, at.y, Rank.SOLDADO, 0, h);
            for (LooreMob x : m) {
                x.setCustomName(net.minecraft.network.chat.Component.translatable("entity.keinloree.eco"));
                Vfx.burst(c.level, x.position().add(0, 1, 0), CalendarioEntity.COLORS[0], 20, 0.4F);
            }
            tag(m, ECO);
        }
    }

    private static void spawnWave(StoryCtx c, int wave) {
        ServerPlayer h = c.hero();
        Vec3 ctr = pillarCenter(c, 1);
        int soldiers = WAVES[wave - 1] - 1;
        Rank leader = wave == 3 ? Rank.COMANDANTE : Rank.SARGENTO;
        Vec3 land = LegionOps.groundAround(c.level, ctr, 22.0D, 30.0D);
        CargoShip ship = h == null ? null : LegionOps.landShip(c.level, land, "patrol:" + leader.name() + ":" + soldiers + ":" + h.getUUID() + ":" + WAVE);
        if (ship == null) {
            tag(LegionOps.squad(c.level, land, land.y, leader, soldiers, h), WAVE);
        }
        c.campaign.setCounter("cenit.oleada_t", (int) c.campaign.worldTicks);
    }

    private static List<Vec3> platforms(StoryCtx c) {
        List<Vec3> out = new ArrayList<>();
        Vec3 ctr = pillarCenter(c, 3);
        for (int i = 0; i < PLATFORMS; i++) {
            double a = i * Math.toRadians(52.0D);
            double r = 9.0D + (i % 2) * 1.5D;
            out.add(new Vec3(Math.floor(ctr.x + Math.cos(a) * r), Math.floor(ctr.y + 1 + i * 3), Math.floor(ctr.z + Math.sin(a) * r)));
        }
        return out;
    }

    private static void platformBlocks(StoryCtx c, boolean place) {
        for (Vec3 p : platforms(c)) {
            BlockPos b = BlockPos.containing(p);
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    BlockPos q = b.offset(dx, 0, dz);
                    if (place && c.level.getBlockState(q).isAir()) {
                        c.level.setBlock(q, Blocks.SEA_LANTERN.defaultBlockState(), Block.UPDATE_CLIENTS);
                    } else if (!place && c.level.getBlockState(q).is(Blocks.SEA_LANTERN)) {
                        c.level.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
            if (place) {
                Vfx.burst(c.level, p.add(1, 1, 1), CalendarioEntity.COLORS[3], 10, 0.3F);
            }
        }
    }

    public static boolean trainingComplete(StoryCtx c, int h) {
        ServerPlayer hero = c.hero();
        return switch (h) {
            case 0 -> c.counter("ecos_muertos") >= 6;
            case 1 -> c.counter("cenit.oleada") >= 3 && c.counter("cenit.bajas") >= WAVES[2];
            case 2 -> c.counter("ocaso.bajas") >= 3;
            case 3 -> {
                List<Vec3> p = platforms(c);
                Vec3 top = p.get(p.size() - 1).add(1, 1, 1);
                yield hero != null && hero.getY() >= top.y - 0.5D && Math.hypot(hero.getX() - top.x, hero.getZ() - top.z) < 3.0D;
            }
            case 4 -> c.counter("eclipse.golpes") >= 10;
            default -> true;
        };
    }

    public static void trainingTick(StoryCtx c, int h) {
        long since = c.campaign.worldTicks - c.counter("entreno.t");
        switch (h) {
            case 0 -> {
                if (since > 400 && living(c, pillarCenter(c, 0), 80.0D, ECO) == 0 && c.counter("ecos_muertos") < 6 && c.flag("ecos_lanzados")) {
                    spawnEchoes(c, 6 - c.counter("ecos_muertos"));
                }
            }
            case 1 -> {
                int wave = c.counter("cenit.oleada");
                if (wave <= 0) {
                    return;
                }
                if (c.counter("cenit.bajas") >= WAVES[wave - 1] && wave < 3) {
                    c.scene(wave == 1 ? "act2.cenit_oleada" : "act2.cenit_oleada2");
                    c.campaign.setCounter("cenit.oleada", wave + 1);
                    c.campaign.setCounter("cenit.bajas", 0);
                    spawnWave(c, wave + 1);
                } else if (c.campaign.worldTicks - c.counter("cenit.oleada_t") > 900 && living(c, pillarCenter(c, 1), 96.0D, WAVE) == 0) {
                    tag(LegionOps.squad(c.level, LegionOps.groundAround(c.level, pillarCenter(c, 1), 18.0D, 24.0D), pillarCenter(c, 1).y,
                            Rank.SARGENTO, WAVES[wave - 1] - 1 - c.counter("cenit.bajas"), c.hero()), WAVE);
                    c.campaign.setCounter("cenit.oleada_t", (int) c.campaign.worldTicks);
                }
            }
            case 4 -> {
                CalendarioEntity e = SagaWorld.hour(c.level, 4);
                if (e != null) {
                    if (e.getMode() != CalendarioEntity.MODE_DUEL && c.flag("duelo_eclipse")) {
                        e.setMode(CalendarioEntity.MODE_DUEL);
                    }
                    if (e.getTarget() == null && c.hero() != null) {
                        e.setTarget(c.hero());
                    }
                    c.campaign.setCounter("eclipse.golpes", Math.max(c.counter("eclipse.golpes"), e.duelHits));
                }
            }
            case 3 -> {
                ServerPlayer hero = c.hero();
                if (hero != null && !Techniques.fullSet(hero) && once(c, "camino_aviso")) {
                    hero.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.keinloree.need_armor"), false);
                }
            }
            default -> { }
        }
    }

    private static boolean once(StoryCtx c, String k) {
        return c.campaign.setFlag("once." + k);
    }

    public static void trainingCleanup(StoryCtx c, int h) {
        switch (h) {
            case 3 -> platformBlocks(c, false);
            case 4 -> {
                CalendarioEntity e = SagaWorld.hour(c.level, 4);
                if (e != null) {
                    e.setMode(CalendarioEntity.MODE_GUARD);
                    e.setTarget(null);
                }
                c.campaign.clearFlag("duelo_eclipse");
            }
            default -> { }
        }
    }

    public static String trainingObjective(StoryCtx c, int h) {
        return switch (h) {
            case 0 -> "Derrota a los ecos de Alba";
            case 1 -> "Defiende el Pilar de Cénit (oleada " + Math.max(1, c.counter("cenit.oleada")) + "/3)";
            case 2 -> "Caza a los comandantes ocultos";
            case 3 -> "Sube por el camino de estrellas";
            case 4 -> "Acierta a Eclipse";
            default -> "";
        };
    }

    @Nullable
    public static BlockPos trainingMarker(StoryCtx c, int h) {
        ServerPlayer hero = c.hero();
        if (h == 2 && hero != null) {
            Entity best = null;
            for (int i = 0; i < 3; i++) {
                Entity e = c.entity("ocaso.cmd." + i);
                if (e != null && e.isAlive() && (best == null || e.distanceToSqr(hero) < best.distanceToSqr(hero))) {
                    best = e;
                }
            }
            return best == null ? null : best.blockPosition();
        }
        if (h == 3) {
            List<Vec3> p = platforms(c);
            return BlockPos.containing(p.get(p.size() - 1).add(0, 1, 0));
        }
        return null;
    }

    public static int[] trainingProgress(StoryCtx c, int h) {
        return switch (h) {
            case 0 -> new int[]{Math.min(6, c.counter("ecos_muertos")), 6};
            case 1 -> {
                int w = Math.max(1, c.counter("cenit.oleada"));
                yield new int[]{Math.min(WAVES[w - 1], c.counter("cenit.bajas")), WAVES[w - 1]};
            }
            case 2 -> new int[]{Math.min(3, c.counter("ocaso.bajas")), 3};
            case 4 -> new int[]{Math.min(10, c.counter("eclipse.golpes")), 10};
            default -> new int[]{0, 0};
        };
    }

    // ================================================================== acto II

    public static void stagePillarRaid(StoryCtx c) {
        ServerLevel l = c.level;
        Pillars.forceBuild(l, 3);
        Vec3 ctr = pillarCenter(c, 3);
        if (SagaWorld.hour(l, 3) == null) {
            Pillars.summon(l, 3, ctr.add(2.5D, 0.0D, 0.5D), 0.0F);
        }
        RazakoEntity rz = spawnRazako(l, ctr.add(-4.0D, 0.0D, 0.5D));
        rz.setScripted(true);
        rz.setTarget(null);
        LegionOps.squad(l, ctr.add(-8.0D, 0.0D, 4.0D), ctr.y, Rank.SARGENTO, 4, null);
    }

    public static void setupCell(StoryCtx c) {
        ServerLevel l = c.level;
        Vec3 cell = SagaWorld.cell(l);
        if (cell == null) {
            return;
        }
        BlockPos center = BlockPos.containing(cell);
        List<BlockPos> picked = new ArrayList<>();
        for (int r = 2; r <= 7 && picked.size() < 3; r++) {
            for (int dx = -r; dx <= r && picked.size() < 3; dx++) {
                for (int dz = -r; dz <= r && picked.size() < 3; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue;
                    }
                    for (int dy = 0; dy <= 1; dy++) {
                        BlockPos p = center.offset(dx, dy, dz);
                        boolean free = l.getBlockState(p).isAir() && l.getBlockState(p.above()).isAir() && l.getBlockState(p.below()).isSolid();
                        boolean spread = picked.stream().allMatch(q -> q.distSqr(p) >= 9.0D);
                        if (free && spread) {
                            picked.add(p);
                            break;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < picked.size(); i++) {
            l.setBlock(picked.get(i), ModBlocks.NEXO_GENERATOR.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            c.setPos("gen." + i, picked.get(i));
            Vfx.lightning(l, Vec3.atCenterOf(picked.get(i)), cell.add(0, 1.2, 0), GREEN, 40);
        }
        c.campaign.setCounter("gen.n", picked.size());
        CalendarioEntity m = Pillars.summon(l, 3, cell.add(0, 0.3D, 0), 0.0F);
        m.setMode(CalendarioEntity.MODE_STILL);
        m.setNoGravity(true);
        m.play(Animated.A_KNEEL);
    }

    public static int generatorsLeft(StoryCtx c) {
        int n = c.counter("gen.n");
        int left = 0;
        for (int i = 0; i < n; i++) {
            BlockPos p = c.pos("gen." + i);
            if (p != null && (!c.level.isLoaded(p) || c.level.getBlockState(p).is(ModBlocks.NEXO_GENERATOR.get()))) {
                left++;
            }
        }
        return left;
    }

    public static void medianocheHelp(StoryCtx c, boolean fighting) {
        ServerPlayer h = c.hero();
        CalendarioEntity m = SagaWorld.hour(c.level, 3);
        if (h == null || m == null || !c.flag("medianoche_libre")) {
            return;
        }
        if (m.distanceTo(h) > 16.0F && h.level() == m.level()) {
            m.teleportTo(h.getX() + 1.5D, h.getY() + 1.0D, h.getZ() + 1.5D);
        }
        RazakoEntity rz = SagaWorld.razako(c.level);
        if (fighting && rz != null && !rz.isKneeling() && c.level.getGameTime() % 300 == 0 && rz.distanceTo(m) < 30.0F) {
            Vec3 at = rz.position();
            m.play(Animated.A_CAST);
            for (int i = 0; i < 6; i++) {
                Vec3 s = at.add(c.level.random.nextGaussian() * 3, 18, c.level.random.nextGaussian() * 3);
                Vfx.beam(c.level, s, at.add(0, 1, 0), CalendarioEntity.COLORS[3], 0.35F, 14);
            }
            Vfx.ringExpanding(c.level, at, new Vec3(0, 1, 0), CalendarioEntity.COLORS[3], 4.0F, 14);
            LooreDamage.apply(rz, c.level.damageSources().indirectMagic(m, m), 10.0F, Vec3.ZERO, 0.1D);
            c.level.playSound(null, rz.blockPosition(), ModSounds.STAR_FALL.get(), SoundSource.NEUTRAL, 2.0F, 1.0F);
        }
    }

    public static void spawnRazakoHangar(StoryCtx c) {
        ServerLevel l = c.level;
        DestroyerEntity d = SagaWorld.anchoredShip(l);
        ServerPlayer h = c.hero();
        Vec3 at = d != null ? SagaWorld.local(d, 0, -2, 96) : h.position().add(6, 0, 0);
        RazakoEntity rz = spawnRazako(l, at);
        rz.setScripted(false);
        rz.setKneelEnabled(true);
        rz.getAttribute(Attributes.MAX_HEALTH).setBaseValue(900.0D);
        rz.setHealth(900.0F);
    }

    public static void pursuers(StoryCtx c, ServerPlayer h) {
        Vec3 back = h.getLookAngle().scale(-60.0D);
        for (int i = 0; i < 4; i++) {
            Vec3 at = h.position().add(back).add((i - 1.5D) * 8.0D, 10.0D + i * 2, 0);
            FighterEntity f = FighterEntity.spawn(c.level, at, h.getYRot(), true);
            f.setPatrolCenter(h.blockPosition());
        }
        c.level.playSound(null, h.blockPosition(), ModSounds.FIGHTER_PASS.get(), SoundSource.HOSTILE, 3.0F, 0.9F);
    }

    // ================================================================== acto III y Amanecer

    public static void gatherHours(StoryCtx c, Vec3 ctr) {
        for (int i = 0; i < 5; i++) {
            if (i == 3 && c.flag("medianoche_capturada") && !c.flag("medianoche_libre")) {
                continue;
            }
            double a = Math.toRadians(-90 + i * 45);
            Vec3 at = ctr.add(Math.cos(a) * 5.0D, 0.0D, Math.sin(a) * 5.0D);
            CalendarioEntity e = Pillars.summon(c.level, i, at, yawTo(at, ctr));
            e.setNoGravity(false);
            e.follow(null);
            e.setMode(CalendarioEntity.MODE_STILL);
            Vfx.pillar(c.level, at, 20.0F, e.color(), 20);
        }
        Later.run(1800, () -> {
            for (int i = 0; i < 5; i++) {
                CalendarioEntity e = SagaWorld.hour(c.level, i);
                if (e != null && e.getMode() == CalendarioEntity.MODE_STILL) {
                    e.setMode(CalendarioEntity.MODE_GUARD);
                }
            }
        });
    }

    public static void bombard(StoryCtx c) {
        ServerLevel l = c.level;
        if (l.getGameTime() % 25 != 0) {
            return;
        }
        DestroyerEntity d = SagaWorld.ship(l);
        if (d == null) {
            return;
        }
        List<TurbolaserEntity> guns = l.getEntitiesOfClass(TurbolaserEntity.class, d.getBoundingBoxForCulling(), t -> t.isHeavy() && !t.isDestroyed());
        if (guns.isEmpty()) {
            return;
        }
        TurbolaserEntity t = guns.get(l.random.nextInt(guns.size()));
        Vec3 ctr = Pillars.center(l, c.campaign, 0);
        double a = l.random.nextDouble() * Math.PI * 2.0D;
        double r = l.random.nextDouble() * 25.0D;
        Vec3 at = ground(l, ctr.x + Math.cos(a) * r, ctr.z + Math.sin(a) * r);
        t.forceAim(at, 30);
        Vec3 from = t.position().add(0, 1.5, 0);
        BoltEntity.fire(l, t, from, at.subtract(from).normalize(), 4.0D, 10.0F, 3.0F, BoltEntity.LEGION_GREEN, 2.0F);
        if (r < 6.0D && !c.flag("alba_caido")) {
            int left = c.campaign.inc("integridad", -1);
            if (left <= 0) {
                c.setFlag("alba_caido");
                Pillars.topple(l, 0);
                c.scene("amanecer.alba_caido");
            }
        }
    }

    public static void keepCompanions(StoryCtx c, ServerPlayer h) {
        if (h == null || c.level.getGameTime() % 40 != 0) {
            return;
        }
        List<Entity> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CalendarioEntity e = SagaWorld.hour(c.level, i);
            if (e != null && e.getMode() == CalendarioEntity.MODE_FOLLOW) {
                list.add(e);
            }
        }
        RazakoEntity rz = SagaWorld.razako(c.level);
        if (rz != null && SagaWorld.isAlly(rz)) {
            list.add(rz);
        }
        for (int i = 0; i < 4; i++) {
            Entity e = c.entity("escolta." + i);
            if (e != null && e.isAlive()) {
                list.add(e);
            }
        }
        for (Entity e : list) {
            if (e.distanceTo(h) > 28.0F && !Cinematics.isActive(h)) {
                e.teleportTo(h.getX() + c.level.random.nextGaussian() * 2, h.getY() + 0.5D, h.getZ() + c.level.random.nextGaussian() * 2);
                Vfx.burst(c.level, e.position().add(0, 1, 0), GREEN, 12, 0.3F);
            }
            if (e instanceof net.minecraft.world.entity.Mob m && (m.getTarget() == null || m.getTarget() == h)) {
                if (m.getTarget() == h) {
                    m.setTarget(null);
                }
                m.getNavigation().moveTo(h, 1.1D);
            }
        }
    }

    // ================================================================== registro de acciones

    public static void register() {
        ScriptActions.register("holograma_aparece", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return;
            }
            Vec3 f = flatLook(h);
            DestroyerEntity d = SagaWorld.ship(c.level);
            if (d != null) {
                Vec3 to = d.position().subtract(h.position());
                f = new Vec3(to.x, 0, to.z).normalize();
            }
            Vec3 at = ground(c.level, h.getX() + f.x * 14.0D, h.getZ() + f.z * 14.0D).add(0, 1.0D, 0);
            KeinHologramEntity holo = KeinHologramEntity.create(c.level, at, yawTo(at, h.position()), 3.5F);
            c.setEntity("holograma", holo);
            Vfx.beam(c.level, at.add(0, 60, 0), at, GREEN, 0.8F, 30);
            Vfx.runeCircle(c.level, at.add(0, 0.05, 0), GREEN, 4.0F, 200);
            c.level.playSound(null, BlockPos.containing(at), ModSounds.HOLOGRAM.get(), SoundSource.HOSTILE, 3.0F, 1.0F);
        });
        ScriptActions.register("holograma_mira_heroe", (c, a) -> {
            ServerPlayer h = hero(c);
            if (c.entity("holograma") instanceof KeinHologramEntity holo && h != null) {
                holo.faceTowards(h.getEyePosition());
            }
        });
        ScriptActions.register("holograma_desaparece", (c, a) -> {
            if (c.entity("holograma") instanceof KeinHologramEntity holo) {
                holo.dissolve();
                Vfx.codeRain(c.level, holo.position(), 4.0F, 60);
            }
        });
        ScriptActions.register("cielo_parpadeo", (c, a) -> {
            c.fx(ScreenFx.TINT, 30, 0.6F, GREEN);
            Later.run(12, () -> c.fx(ScreenFx.GLITCH, 8, 0.6F, GREEN));
            c.sound(ModSounds.GLITCH.get(), 0.6F, 0.6F);
        });
        ScriptActions.register("animales_miran", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return;
            }
            DestroyerEntity d = SagaWorld.ship(c.level);
            Vec3 look = d != null ? d.position() : h.position().add(0, 80, 0);
            List<Animal> animals = c.level.getEntitiesOfClass(Animal.class, h.getBoundingBox().inflate(32.0D));
            Later.repeat(0, 60, n -> {
                for (Animal an : animals) {
                    if (an.isAlive()) {
                        an.getNavigation().stop();
                        an.getLookControl().setLookAt(look.x, look.y, look.z, 30.0F, 90.0F);
                    }
                }
                return true;
            });
        });
        ScriptActions.register("congelar_legion", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return;
            }
            for (LooreMob m : c.level.getEntitiesOfClass(LooreMob.class, h.getBoundingBox().inflate(64.0D), m -> Factions.isLegion(m))) {
                TimeStop.freeze(m, 1200);
            }
            Vfx.timeFreeze(c.level, h.position().add(0, 1, 0), 30.0F, 80);
            c.fx(ScreenFx.TIME_FREEZE, 60, 0.7F, 0xE8E8FF);
            c.sound(ModSounds.TIME_STOP.get(), 1.0F, 1.0F);
        });
        ScriptActions.register("descongelar_legion", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return;
            }
            for (LooreMob m : c.level.getEntitiesOfClass(LooreMob.class, h.getBoundingBox().inflate(80.0D), TimeStop::isFrozen)) {
                TimeStop.release(m);
            }
            c.sound(ModSounds.TIME_TICK.get(), 1.0F, 0.8F);
        });
        ScriptActions.register("calendario_entra", (c, a) -> {
            ServerPlayer h = hero(c);
            int hr = hourArg(a);
            if (h == null || hr < 0) {
                return;
            }
            double ang = Math.toRadians(hr * 72.0D) + Math.atan2(flatLook(h).z, flatLook(h).x);
            Vec3 at = ground(c.level, h.getX() + Math.cos(ang) * 6.0D, h.getZ() + Math.sin(ang) * 6.0D);
            CalendarioEntity e = Pillars.summon(c.level, hr, at, yawTo(at, h.position()));
            e.setNoGravity(false);
            e.clearRestriction();
            e.setMode(CalendarioEntity.MODE_GUARD);
            e.play(Animated.A_POSE);
            Vfx.pillar(c.level, at, 40.0F, e.color(), 30);
            Vfx.burst(c.level, at.add(0, 1, 0), e.color(), 30, 0.6F);
            Vfx.ringExpanding(c.level, at.add(0, 0.1, 0), new Vec3(0, 1, 0), e.color(), 4.0F, 20);
            c.level.playSound(null, BlockPos.containing(at), ModSounds.CHAPTER_STING.get(), SoundSource.NEUTRAL, 1.5F, 0.8F + hr * 0.1F);
        });
        ScriptActions.register("razako_retira", (c, a) -> {
            RazakoEntity rz = SagaWorld.razako(c.level);
            if (rz == null) {
                return;
            }
            rz.setTarget(null);
            rz.play(Animated.A_ROAR);
            Vfx.beam(c.level, rz.position().add(0, 70, 0), rz.position(), GREEN, 1.0F, 40);
            Later.run(30, () -> {
                Vfx.burst(c.level, rz.position().add(0, 1.5, 0), GREEN, 40, 0.8F);
                rz.discard();
            });
            c.setFlag("razako_a_bordo");
        });
        ScriptActions.register("base_destruir", (c, a) -> {
            BlockPos b = c.pos("base.razako");
            ServerPlayer h = hero(c);
            if (b == null && h != null) {
                b = LegionOps.nearestBase(c.campaign, h.position());
            }
            if (b != null) {
                LegionOps.destroyBase(c.level, b);
            }
        });
        ScriptActions.register("dar_equipo", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return;
            }
            for (ItemStack s : Techniques.gear()) {
                if (s.getItem() instanceof ArmorItem ai) {
                    EquipmentSlot slot = ai.getEquipmentSlot();
                    if (h.getItemBySlot(slot).isEmpty()) {
                        h.setItemSlot(slot, s);
                        continue;
                    }
                }
                if (!h.getInventory().add(s)) {
                    h.drop(s, false);
                }
            }
            c.campaign.tale(h.getUUID()).receivedGear = true;
            c.campaign.setDirty();
            Vfx.pillar(c.level, h.position(), 30.0F, 0xFFF2A8, 40);
            Vfx.burst(c.level, h.position().add(0, 1, 0), 0xFFF2A8, 40, 0.6F);
            c.sound(ModSounds.RAREIRI_HUM.get(), 1.2F, 1.0F);
            Techniques.sendState(h);
        });
        ScriptActions.register("dejar_equipo", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return;
            }
            Vec3 f = flatLook(h);
            Vec3 at = ground(c.level, h.getX() + f.x * 3.0D, h.getZ() + f.z * 3.0D);
            BlockPos ped = BlockPos.containing(at);
            c.level.setBlock(ped, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
            c.setPos("equipo", ped);
            for (ItemStack s : Techniques.gear()) {
                ItemEntity ie = new ItemEntity(c.level, ped.getX() + 0.5D, ped.getY() + 1.1D, ped.getZ() + 0.5D, s);
                ie.setUnlimitedLifetime();
                ie.setDeltaMovement(Vec3.ZERO);
                ie.getPersistentData().putBoolean("keinloree_story", true);
                c.level.addFreshEntity(ie);
            }
            Vfx.pillar(c.level, Vec3.atBottomCenterOf(ped.above()), 20.0F, 0xFFF2A8, 60);
        });
        ScriptActions.register("encender_pilares", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h != null) {
                Pillars.plan(c.level, h.position());
            }
            c.setFlag("pilares_encendidos");
            Pillars.syncBeams(c.level);
            c.sound(ModSounds.PILLAR_HUM.get(), 2.0F, 0.7F);
        });
        ScriptActions.register("calendarios_a_pilares", (c, a) -> Later.run(60, () -> {
            for (int i = 0; i < 5; i++) {
                if (i == 3 && c.flag("medianoche_capturada") && !c.flag("medianoche_libre")) {
                    continue;
                }
                Pillars.sendHome(c.level, i);
            }
        }));
        ScriptActions.register("desbloquear", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null || a.length == 0) {
                return;
            }
            for (int i = 0; i < Techniques.IDS.length; i++) {
                if (Techniques.IDS[i].equals(a[0])) {
                    Techniques.unlock(h, i);
                    Vfx.burst(c.level, h.position().add(0, 1, 0), 0xFFF2A8, 30, 0.5F);
                }
            }
        });
        ScriptActions.register("alba_ecos", (c, a) -> {
            c.campaign.setCounter("ecos_muertos", 0);
            c.setFlag("ecos_lanzados");
            spawnEchoes(c, 6);
            CalendarioEntity e = SagaWorld.hour(c.level, 0);
            if (e != null) {
                e.play(Animated.A_CAST);
            }
        });
        ScriptActions.register("cenit_oleadas", (c, a) -> {
            c.campaign.setCounter("cenit.oleada", 1);
            c.campaign.setCounter("cenit.bajas", 0);
            spawnWave(c, 1);
        });
        ScriptActions.register("ocaso_escuadras", (c, a) -> {
            c.campaign.setCounter("ocaso.bajas", 0);
            Vec3 ctr = pillarCenter(c, 2);
            for (int i = 0; i < 3; i++) {
                Vec3 at = LegionOps.groundAround(c.level, ctr, 40.0D, 80.0D);
                List<LooreMob> sq = LegionOps.squad(c.level, at, at.y, Rank.COMANDANTE, 2, null);
                if (!sq.isEmpty()) {
                    sq.get(0).getPersistentData().putBoolean(HUNT, true);
                    sq.get(0).setGlowingTag(true);
                    c.setEntity("ocaso.cmd." + i, sq.get(0));
                }
            }
        });
        ScriptActions.register("medianoche_camino", (c, a) -> {
            platformBlocks(c, true);
            c.sound(ModSounds.STAR_FALL.get(), 1.2F, 1.2F);
        });
        ScriptActions.register("eclipse_duelo", (c, a) -> {
            CalendarioEntity e = SagaWorld.hour(c.level, 4);
            c.campaign.setCounter("eclipse.golpes", 0);
            c.setFlag("duelo_eclipse");
            if (e != null) {
                e.duelHits = 0;
                e.setMode(CalendarioEntity.MODE_DUEL);
                e.setTarget(hero(c));
            }
        });
        ScriptActions.register("pilar_cae", (c, a) -> {
            int hr = hourArg(a);
            if (hr < 0) {
                return;
            }
            RazakoEntity rz = SagaWorld.razako(c.level);
            if (rz != null && hr == 3) {
                rz.play(Animated.A_HORIZON);
                Vfx.slash(c.level, rz.position().add(0, 1.5, 0), rz.getYRot(), 0.0F, 0.0F, 6.0F, GREEN, 14);
            }
            Pillars.forceBuild(c.level, hr);
            Vec3 ctr = Pillars.center(c.level, c.campaign, hr);
            Later.repeat(0, 40, n -> {
                if (n % 6 == 0) {
                    Vfx.explosion(c.level, ctr.add(c.level.random.nextGaussian() * 3, 4 + n * 0.5, c.level.random.nextGaussian() * 3), 2.5F);
                }
                return true;
            });
            Later.run(20, () -> Pillars.topple(c.level, hr));
            c.fx(ScreenFx.SHAKE, 40, 0.8F, 0xFFFFFF);
        });
        ScriptActions.register("medianoche_capturada", (c, a) -> {
            c.setFlag("medianoche_capturada");
            CalendarioEntity m = SagaWorld.hour(c.level, 3);
            RazakoEntity rz = SagaWorld.razako(c.level);
            if (m != null) {
                m.setMode(CalendarioEntity.MODE_STILL);
                m.play(Animated.A_KNEEL);
                if (rz != null) {
                    rz.play(Animated.A_CHAIN);
                    for (int i = 0; i < 3; i++) {
                        Vfx.lightning(c.level, rz.position().add(0, 1.5, 0), m.position().add(0, 1 + i * 0.4, 0), GREEN, 60);
                    }
                }
                c.level.playSound(null, m.blockPosition(), ModSounds.CHAIN.get(), SoundSource.HOSTILE, 2.0F, 0.8F);
                Later.run(70, () -> {
                    Vfx.beam(c.level, m.position().add(0, 80, 0), m.position(), GREEN, 1.0F, 30);
                    Vfx.burst(c.level, m.position().add(0, 1, 0), CalendarioEntity.COLORS[3], 40, 0.6F);
                    m.discard();
                });
            }
            if (rz != null) {
                Later.run(100, () -> {
                    Vfx.beam(c.level, rz.position().add(0, 80, 0), rz.position(), GREEN, 1.0F, 30);
                    rz.discard();
                });
            }
            Atmosphere.set(c.level, 0.3F, 0.15F, AtmosphereS2C.HORA_PERDIDA);
        });
        ScriptActions.register("alarma_nave", (c, a) -> {
            DestroyerEntity d = SagaWorld.ship(c.level);
            ServerPlayer h = hero(c);
            if (d != null) {
                d.setAlarm(true);
                c.campaign.setCounter("alarma_hasta", (int) c.campaign.worldTicks + 6000);
            }
            c.sound(ModSounds.ALARM.get(), 1.0F, 1.0F);
            AABB box = SagaWorld.shipBox(c.level);
            if (box != null && h != null) {
                for (LooreMob m : c.level.getEntitiesOfClass(LooreMob.class, box, m -> Factions.hostile(m, h) && !(m instanceof KeinLooreEntity))) {
                    if (m.distanceTo(h) < 48.0F) {
                        m.setTarget(h);
                    }
                }
            }
        });
        ScriptActions.register("liberar_medianoche", (c, a) -> {
            Vec3 cell = SagaWorld.cell(c.level);
            if (cell != null) {
                BlockPos ctr = BlockPos.containing(cell);
                for (BlockPos p : BlockPos.betweenClosed(ctr.offset(-4, -1, -4), ctr.offset(4, 3, 4))) {
                    if (c.level.getBlockState(p).is(Blocks.IRON_BARS)) {
                        c.level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
            c.setFlag("medianoche_libre");
            CalendarioEntity m = SagaWorld.hour(c.level, 3);
            if (m != null) {
                m.follow(hero(c));
                m.play(Animated.A_POSE);
                Vfx.burst(c.level, m.position().add(0, 1, 0), m.color(), 40, 0.5F);
            }
        });
        ScriptActions.register("razako_retirada_nave", (c, a) -> {
            c.setFlag("razako_perdonado");
            RazakoEntity rz = SagaWorld.razako(c.level);
            if (rz != null) {
                rz.standUp();
                rz.setTarget(null);
                Later.run(40, () -> {
                    Vfx.burst(c.level, rz.position().add(0, 1.5, 0), GREEN, 40, 0.6F);
                    rz.discard();
                });
            }
        });
        ScriptActions.register("razako_muere", (c, a) -> {
            c.setFlag("razako_muerto");
            RazakoEntity rz = SagaWorld.razako(c.level);
            if (rz != null) {
                Vfx.codeRain(c.level, rz.position(), 2.0F, 80);
                Later.run(30, () -> {
                    Vfx.explosion(c.level, rz.position().add(0, 1.5, 0), 2.0F);
                    Vfx.burst(c.level, rz.position().add(0, 1.5, 0), GREEN, 60, 0.8F);
                    rz.discard();
                });
            }
            c.campaign.entities.remove("razako");
        });
        ScriptActions.register("medianoche_teleporta", (c, a) -> {
            CalendarioEntity m = SagaWorld.hour(c.level, 3);
            if (m != null) {
                m.setNoGravity(false);
                m.follow(null);
            }
            Pillars.sendHome(c.level, 3);
        });
        ScriptActions.register("tregua_fin_cuenta", (c, a) -> c.campaign.setCounter("tregua_fin", (int) c.campaign.worldTicks + 1200));
        ScriptActions.register("kein_gira", (c, a) -> {
            KeinLooreEntity k = SagaWorld.kein(c.level);
            ServerPlayer h = hero(c);
            if (k != null && h != null) {
                float y = yawTo(k.position(), h.position());
                k.setYRot(y);
                k.setYHeadRot(y);
                k.setYBodyRot(y);
            }
        });
        ScriptActions.register("kein_nucleo", (c, a) -> {
            KeinLooreEntity k = SagaWorld.kein(c.level);
            if (k != null) {
                Vfx.orbOn(c.level, k, GREEN, 0.5F, 220);
                Vfx.distortion(c.level, k.position().add(0, 1.4, 0), GREEN, 1.2F, 60);
                c.sound(ModSounds.HEARTBEAT.get(), 1.0F, 0.6F);
            }
        });
        ScriptActions.register("escudo_romper", (c, a) -> {
            DestroyerEntity d = SagaWorld.ship(c.level);
            c.setFlag("escudo_roto");
            if (d == null) {
                return;
            }
            Vec3 p = d.position();
            Vfx.shockwave(c.level, p, GREEN, 140.0F, 40);
            Vfx.ringExpanding(c.level, p, new Vec3(0, 1, 0), GREEN, 160.0F, 30);
            Vfx.distortion(c.level, p, GREEN, 60.0F, 40);
            for (int i = 0; i < 12; i++) {
                Vfx.explosion(c.level, p.add(c.level.random.nextGaussian() * 50, c.level.random.nextGaussian() * 10, c.level.random.nextGaussian() * 50), 3.0F);
            }
            c.fx(ScreenFx.FLASH, 20, 1.0F, 0xFFFFFF);
            c.sound(ModSounds.EXPLOSION_BIG.get(), 2.0F, 0.6F);
        });
        ScriptActions.register("calendarios_suben", (c, a) -> {
            ServerPlayer h = hero(c);
            Vec3 floor = SagaWorld.hangarFloor(c.level);
            if (h == null || floor == null) {
                return;
            }
            float yaw = SagaWorld.ship(c.level).getYRot();
            SagaWorld.blink(h, floor, yaw);
            for (int i = 0; i < 5; i++) {
                CalendarioEntity e = SagaWorld.hour(c.level, i);
                if (e == null) {
                    Vec3 home = Pillars.center(c.level, c.campaign, i);
                    if (!c.level.isLoaded(BlockPos.containing(floor))) {
                        continue;
                    }
                    e = Pillars.summon(c.level, i, home, 0.0F);
                }
                CalendarioEntity ce = e;
                Vfx.pillar(c.level, ce.position(), 30.0F, ce.color(), 20);
                Later.run(24 + i * 4, () -> {
                    ce.teleportTo(floor.x + (ce.hour() - 2) * 2.0D, floor.y + 0.2D, floor.z + 3.0D);
                    ce.setNoGravity(false);
                    ce.follow(h);
                    Vfx.pillar(c.level, ce.position(), 12.0F, ce.color(), 20);
                });
            }
        });
        ScriptActions.register("razako_aliado", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return;
            }
            RazakoEntity rz = spawnRazako(c.level, h.position().add(2.0D, 0.0D, 2.0D));
            SagaWorld.makeAlly(rz);
            rz.setScripted(false);
            rz.setKneelEnabled(false);
            rz.play(Animated.A_ROAR);
        });
        ScriptActions.register("eco_razako", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return;
            }
            Vec3 at = h.position().add(flatLook(h).scale(7.0D));
            RazakoEntity rz = ModEntities.RAZAKO.get().create(c.level);
            LegionOps.placeSafe(c.level, rz, at.x, at.y, at.z, yawTo(at, h.position()));
            rz.setEcho(true);
            rz.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400.0D);
            rz.setHealth(400.0F);
            c.level.addFreshEntity(rz);
            c.campaign.entities.put("razako", rz.getUUID());
            c.campaign.setDirty();
            rz.setTarget(h);
            for (int i = 0; i < 4; i++) {
                for (LooreMob m : LegionOps.squad(c.level, at.add((i - 1.5D) * 2.0D, 0, -2), at.y, Rank.GUARDIA_NEXO, 0, h)) {
                    m.setTarget(h);
                }
            }
            Vfx.codeRain(c.level, at, 3.0F, 60);
        });
        ScriptActions.register("kein_combate", (c, a) -> {
            KeinLooreEntity k = SagaWorld.kein(c.level);
            if (k != null) {
                k.setLoreDormant(false);
                k.setNoAi(false);
                k.setTarget(hero(c));
            }
        });
        ScriptActions.register("carga_completa", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h != null) {
                Techniques.fillCharge(h);
                Vfx.pillar(c.level, h.position(), 20.0F, 0xFFFFFF, 40);
            }
        });
        ScriptActions.register("kein_arrodilla", (c, a) -> {
            KeinLooreEntity k = SagaWorld.kein(c.level);
            if (k != null) {
                k.setLoreDormant(true);
                k.setTarget(null);
                Vfx.orbOn(c.level, k, GREEN, 0.4F, 400);
            }
        });
        ScriptActions.register("nucleo_estalla", (c, a) -> {
            KeinLooreEntity k = SagaWorld.kein(c.level);
            if (k == null) {
                return;
            }
            Vec3 p = k.position().add(0, 1.4, 0);
            Vfx.explosion(c.level, p, 6.0F);
            Vfx.shockwave(c.level, p, GREEN, 24.0F, 30);
            Vfx.pillar(c.level, k.position(), 200.0F, 3.0F, 0xFFFFFF, 60);
            Vfx.codeRain(c.level, k.position(), 3.0F, 100);
            Later.run(60, () -> {
                Vfx.burst(c.level, p, GREEN, 80, 1.0F);
                k.discard();
            });
        });
        ScriptActions.register("nave_destruir", (c, a) -> {
            evacuate(c);
            Later.run(30, () -> {
                DestroyerEntity d = SagaWorld.ship(c.level);
                if (d != null) {
                    d.destroy();
                }
            });
        });
        ScriptActions.register("epilogo_pilar_alba", (c, a) -> {
            Pillars.forceBuild(c.level, 0);
            Vec3 ctr = Pillars.center(c.level, c.campaign, 0);
            for (ServerPlayer p : c.players()) {
                p.teleportTo(c.level, ctr.x + 4.0D, ctr.y, ctr.z + 4.0D, 0.0F, 0.0F);
                p.fallDistance = 0.0F;
            }
            for (int i = 0; i < 5; i++) {
                if (i == 4 && c.flag("eclipse_sacrificada")) {
                    continue;
                }
                double ang = Math.toRadians(i * 72.0D);
                Vec3 at = ctr.add(Math.cos(ang) * 6.0D, 0, Math.sin(ang) * 6.0D);
                CalendarioEntity e = Pillars.summon(c.level, i, at, yawTo(at, ctr));
                e.setNoGravity(false);
                e.follow(null);
                e.setMode(CalendarioEntity.MODE_STILL);
            }
        });
        ScriptActions.register("legion_deshacer", (c, a) -> {
            c.setFlag("legion_deshecha");
            Later.repeat(10, 400, n -> {
                if (n % 5 != 0) {
                    return true;
                }
                int left = 8;
                List<Entity> doomed = new ArrayList<>();
                for (Entity e : c.level.getAllEntities()) {
                    if (doomed.size() >= left) {
                        break;
                    }
                    boolean legion = Factions.isLegion(e) || e instanceof TurbolaserEntity || e instanceof BoltEntity;
                    if (legion && e.isAlive() && !(e instanceof DestroyerEntity)) {
                        doomed.add(e);
                    }
                }
                for (Entity e : doomed) {
                    Vfx.burst(c.level, e.position().add(0, 1, 0), GREEN, 20, 0.4F);
                    e.discard();
                }
                return true;
            });
        });
        ScriptActions.register("calendarios_duermen", (c, a) -> {
            for (int i = 0; i < 5; i++) {
                CalendarioEntity e = SagaWorld.hour(c.level, i);
                if (e != null) {
                    e.setMode(CalendarioEntity.MODE_STILL);
                    e.play(Animated.A_KNEEL);
                    Vfx.pillar(c.level, e.position(), 30.0F, e.color(), 60);
                }
            }
        });
        ScriptActions.register("creditos", (c, a) -> c.scene("creditos." + (a.length > 0 ? a[0] : "amanecer")));
        ScriptActions.register("kein_restaurado", (c, a) -> {
            c.setFlag("kein_restaurado");
            c.setFlag("final.sexta_en_curso");
            KeinLooreEntity k = SagaWorld.kein(c.level);
            if (k != null) {
                Vfx.pillar(c.level, k.position(), 60.0F, 2.0F, 0xFFD27A, 120);
                Vfx.orbOn(c.level, k, 0xFFD27A, 0.8F, 200);
                Vfx.burst(c.level, k.position().add(0, 1.5, 0), 0xFFD27A, 60, 0.6F);
            }
        });
        ScriptActions.register("eclipse_sacrificio", (c, a) -> {
            c.setFlag("eclipse_sacrificada");
            CalendarioEntity e = SagaWorld.hour(c.level, 4);
            KeinLooreEntity k = SagaWorld.kein(c.level);
            if (e != null) {
                if (k != null) {
                    Vfx.beam(c.level, e.position().add(0, 1.2, 0), k.position().add(0, 1.4, 0), 0xE8E8FF, 0.5F, 60);
                }
                Vfx.pillar(c.level, e.position(), 40.0F, 0xE8E8FF, 60);
                Later.run(40, () -> {
                    Vfx.burst(c.level, e.position().add(0, 1, 0), 0xFFFFFF, 60, 0.6F);
                    e.discard();
                });
            }
        });
        ScriptActions.register("nave_salto", (c, a) -> {
            evacuate(c);
            Later.run(40, () -> {
                DestroyerEntity d = SagaWorld.ship(c.level);
                if (d == null) {
                    return;
                }
                d.liftoff();
                Later.repeat(0, 1200, n -> {
                    DestroyerEntity s = SagaWorld.ship(c.level);
                    if (s == null) {
                        return false;
                    }
                    if (s.getState() == DestroyerEntity.FLYING || n >= 1199) {
                        Vec3 fwd = Vec3.directionFromRotation(0.0F, s.getYRot());
                        Vfx.hyperspace(c.level, s.position(), fwd, 4.0F);
                        c.sound(ModSounds.HYPERSPACE.get(), 2.0F, 1.0F);
                        s.discard();
                        return false;
                    }
                    return true;
                });
            });
        });
        ScriptActions.register("marca_nexo", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return;
            }
            c.campaign.tale(h.getUUID()).legionAlly = true;
            c.campaign.path = Campaign.Path.NEXO;
            c.setFlag("traicion_horas");
            c.campaign.clearFlag("tregua");
            c.campaign.setDirty();
            Techniques.sendState(h);
            Vfx.pillar(c.level, h.position(), 30.0F, GREEN, 40);
            Vfx.codeRain(c.level, h.position(), 2.0F, 60);
            for (Entity e : c.level.getEntitiesOfClass(LooreMob.class, h.getBoundingBox().inflate(64.0D))) {
                if (e instanceof LooreMob m && m.getTarget() == h) {
                    m.setTarget(null);
                }
            }
        });
        ScriptActions.register("ocaso_duelo", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return;
            }
            CalendarioEntity o = SagaWorld.hour(c.level, 2);
            Vec3 at = LegionOps.groundAround(c.level, h.position(), 5.0D, 7.0D);
            if (o == null) {
                o = Pillars.summon(c.level, 2, at, yawTo(at, h.position()));
            } else {
                o.teleportTo(at.x, at.y, at.z);
            }
            o.setNoGravity(false);
            o.follow(null);
            o.clearRestriction();
            o.setMode(CalendarioEntity.MODE_HOSTILE);
            o.setTarget(h);
            Vfx.distortion(c.level, at.add(0, 1, 0), CalendarioEntity.COLORS[2], 1.6F, 16);
        });
        ScriptActions.register("ocaso_huye", (c, a) -> {
            CalendarioEntity o = SagaWorld.hour(c.level, 2);
            if (o != null) {
                Vfx.distortion(c.level, o.position().add(0, 1, 0), o.color(), 1.6F, 16);
                o.discard();
                c.campaign.entities.remove("cal.ocaso");
                c.campaign.setDirty();
            }
        });
        ScriptActions.register("eclipse_duelo_final", (c, a) -> {
            ServerPlayer h = hero(c);
            CalendarioEntity e = SagaWorld.hour(c.level, 4);
            if (e == null && h != null) {
                e = Pillars.summon(c.level, 4, LegionOps.groundAround(c.level, h.position(), 6.0D, 8.0D), 0.0F);
            }
            if (e != null) {
                e.clearRestriction();
                e.follow(null);
                e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(600.0D);
                e.setHealth(600.0F);
                e.setMode(CalendarioEntity.MODE_HOSTILE);
                e.setTarget(h);
            }
        });
        ScriptActions.register("cielo_verde", (c, a) -> Atmosphere.set(c.level, 0.6F, 0.3F, AtmosphereS2C.NEXO_UNIDO));
        ScriptActions.register("congelar_calendarios", (c, a) -> {
            c.setFlag("calendarios_congelados");
            c.setFlag("pilares_apagados");
            for (int i = 0; i < 5; i++) {
                CalendarioEntity e = SagaWorld.hour(c.level, i);
                if (e != null) {
                    e.setMode(CalendarioEntity.MODE_FROZEN);
                    e.setNoAi(true);
                    Vfx.codeRain(c.level, e.position(), 1.2F, 120);
                }
            }
        });
        registerCinematics();
    }

    /** Saca de la nave (a la superficie, junto al Pilar del Alba si existe) a los jugadores que esten dentro. */
    private static void evacuate(StoryCtx c) {
        AABB box = SagaWorld.shipBox(c.level);
        Vec3 to = Pillars.planned(c.campaign) ? Pillars.center(c.level, c.campaign, 0).add(4, 0, 4) : null;
        for (ServerPlayer p : c.players()) {
            if (box != null && box.inflate(4.0D).contains(p.position()) || p.getVehicle() instanceof DestroyerEntity) {
                p.stopRiding();
                Vec3 dest = to != null ? to : LegionOps.groundAround(c.level, p.position(), 120.0D, 160.0D);
                if (to != null) {
                    Pillars.forceBuild(c.level, 0);
                }
                ScreenFx.send(p, ScreenFx.FADE_OUT, 15, 1.0F, 0);
                p.teleportTo(c.level, dest.x, dest.y + 0.5D, dest.z, p.getYRot(), 0.0F);
                p.fallDistance = 0.0F;
            }
        }
    }

    // ================================================================== cinematicas

    private static void registerCinematics() {
        Cinematics.register("llegada_destructor", (c, a) -> {
            ServerPlayer h = hero(c);
            DestroyerEntity d = SagaWorld.ship(c.level);
            if (h == null || d == null) {
                return List.of();
            }
            Vec3 eye = h.getEyePosition();
            Vec3 back = flatLook(h).scale(-3.0D);
            return List.of(
                    Shot.fixed(eye.add(back).add(0, 0.6, 0), 100).lookAtEntity(d.getId()).fov(70, 60),
                    Shot.track(d.getId(), new Vec3(30, -70, 60), 300).lookAtEntity(d.getId()),
                    Shot.orbit(h.position(), h.getId(), 9.0F, 3.0F, 0.0F, 120.0F, 200).lookAtEntity(d.getId()));
        });
        Cinematics.register("transmision", (c, a) -> {
            Entity holo = c.entity("holograma");
            ServerPlayer h = hero(c);
            if (holo == null || h == null) {
                return List.of();
            }
            Vec3 p = holo.position();
            Vec3 toHero = h.position().subtract(p).normalize();
            return List.of(
                    Shot.fixed(p.add(toHero.scale(5.0D)).add(0, 0.3, 0), 100).lookAt(p.add(0, 6, 0)).fov(80, 70),
                    Shot.dolly(p.add(toHero.scale(9.0D)).add(0, 4, 0), p.add(toHero.scale(4.0D)).add(0, 5, 0), 160).lookAt(p.add(0, 5.5, 0)).fov(60, 45));
        });
        Cinematics.register("aldeano", (c, a) -> {
            Entity v = c.entity("aldeano");
            if (v == null) {
                return List.of();
            }
            return List.of(Shot.orbit(v.position(), v.getId(), 5.0F, 1.8F, 0.0F, 140.0F, 120).lookAtEntity(v.getId()));
        });
        Cinematics.register("razako_llega", (c, a) -> {
            RazakoEntity rz = SagaWorld.razako(c.level);
            if (rz == null) {
                return List.of();
            }
            Vec3 f = Vec3.directionFromRotation(0.0F, rz.getYRot());
            Vec3 low = rz.position().add(f.scale(4.0D)).add(0, 0.4, 0);
            return List.of(
                    Shot.fixed(low, 100).lookAt(rz.position().add(0, 2.6, 0)).fov(75, 65),
                    Shot.orbit(rz.position(), rz.getId(), 4.5F, 1.6F, 0.0F, 90.0F, 120).lookAtEntity(rz.getId()));
        });
        Cinematics.register("calendarios_llegan", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return List.of();
            }
            List<Shot> shots = new ArrayList<>();
            double base = Math.atan2(flatLook(h).z, flatLook(h).x);
            for (int i = 0; i < 5; i++) {
                double ang = Math.toRadians(i * 72.0D) + base;
                Vec3 spot = ground(c.level, h.getX() + Math.cos(ang) * 6.0D, h.getZ() + Math.sin(ang) * 6.0D);
                Vec3 cam = h.position().add(Math.cos(ang) * 1.5D, 1.6D, Math.sin(ang) * 1.5D);
                shots.add(Shot.dolly(cam, cam.add(spot.subtract(cam).scale(0.25D)), 100).lookAt(spot.add(0, 1.4, 0)).fov(65, 55));
            }
            return shots;
        });
        Cinematics.register("base_destruida", (c, a) -> {
            ServerPlayer h = hero(c);
            BlockPos b = c.pos("base.razako");
            if (h == null || b == null) {
                return List.of();
            }
            Vec3 bc = Vec3.atCenterOf(b).add(0, 6, 0);
            Vec3 dir = bc.subtract(h.position()).normalize();
            Vec3 cam = h.position().subtract(dir.scale(10.0D)).add(0, 12, 0);
            return List.of(Shot.dolly(cam, cam.add(0, 10, 0), 160).lookAt(bc));
        });
        Cinematics.register("pilares_encendidos", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return List.of();
            }
            Vec3 look = h.position().add(0, 60, 0);
            int n = Pillars.nearest(c.campaign, h.position(), 0x1F);
            if (n >= 0) {
                Vec3 p = Pillars.center(c.level, c.campaign, n);
                look = new Vec3(p.x, h.getY() + 50, p.z);
            }
            return List.of(
                    Shot.dolly(h.getEyePosition(), h.position().add(0, 40, 0), 160).lookAt(look),
                    Shot.orbit(h.position(), h.getId(), 30.0F, 30.0F, 0.0F, 300.0F, 240).lookAtEntity(h.getId()));
        });
        Cinematics.register("pilar_orbita", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return List.of();
            }
            int n = Pillars.nearest(c.campaign, h.position(), 0x1F);
            if (n < 0) {
                return List.of();
            }
            Vec3 p = Pillars.center(c.level, c.campaign, n);
            return List.of(Shot.orbit(p.add(0, 2, 0), -1, 18.0F, 10.0F, 0.0F, 200.0F, 220).lookAt(p.add(0, 12, 0)));
        });
        Cinematics.register("pilar_cae", (c, a) -> {
            Vec3 p = Pillars.center(c.level, c.campaign, 3);
            return List.of(
                    Shot.fixed(p.add(20, 6, 20), 120).lookAt(p.add(0, 14, 0)),
                    Shot.orbit(p, -1, 26.0F, 12.0F, 45.0F, 200.0F, 220).lookAt(p.add(0, 8, 0)));
        });
        Cinematics.register("razako_hangar", (c, a) -> {
            RazakoEntity rz = SagaWorld.razako(c.level);
            ServerPlayer h = hero(c);
            if (rz == null || h == null) {
                return List.of();
            }
            Vec3 mid = rz.position().add(h.position()).scale(0.5D);
            return List.of(
                    Shot.fixed(mid.add(6, 3, 0), 100).lookAtEntity(rz.getId()).fov(60, 50),
                    Shot.orbit(rz.position(), rz.getId(), 5.0F, 1.5F, 0.0F, 100.0F, 120).lookAtEntity(rz.getId()));
        });
        Cinematics.register("kein_puente", (c, a) -> {
            KeinLooreEntity k = SagaWorld.kein(c.level);
            ServerPlayer h = hero(c);
            if (k == null || h == null) {
                return List.of();
            }
            Vec3 toK = k.position().subtract(h.position()).normalize();
            Vec3 shoulder = h.getEyePosition().subtract(toK.scale(1.8D)).add(toK.cross(new Vec3(0, 1, 0)).scale(0.8D));
            return List.of(
                    Shot.fixed(shoulder, 100).lookAtEntity(k.getId()).fov(60, 55),
                    Shot.dolly(k.getEyePosition().subtract(toK.scale(5.0D)), k.getEyePosition().subtract(toK.scale(2.2D)), 160)
                            .lookAt(k.getEyePosition()).fov(55, 40));
        });
        Cinematics.register("cinco_haces", (c, a) -> {
            DestroyerEntity d = SagaWorld.ship(c.level);
            ServerPlayer h = hero(c);
            if (d == null || h == null) {
                return List.of();
            }
            for (int i = 0; i < 5; i++) {
                if (!Pillars.planned(c.campaign)) {
                    break;
                }
                Vec3 p = Pillars.center(c.level, c.campaign, i).add(0, Pillars.HEIGHT, 0);
                Vfx.beam(c.level, p, d.position(), Pillars.color(i), 3.0F, 120);
            }
            Vec3 cam = h.position().add(0, 20, 0);
            return List.of(Shot.fixed(cam, 260).lookAtEntity(d.getId()).fov(70, 55));
        });
        Cinematics.register("kein_final", (c, a) -> {
            KeinLooreEntity k = SagaWorld.kein(c.level);
            if (k == null) {
                return List.of();
            }
            return List.of(Shot.orbit(k.position(), k.getId(), 6.0F, 2.0F, 180.0F, 320.0F, 160).lookAtEntity(k.getId()));
        });
        Cinematics.register("final_amanecer", (c, a) -> {
            KeinLooreEntity k = SagaWorld.kein(c.level);
            ServerPlayer h = hero(c);
            if (k == null || h == null) {
                return List.of();
            }
            return List.of(Shot.orbit(k.position(), k.getId(), 4.0F, 1.6F, 0.0F, 90.0F, 200).lookAtEntity(k.getId()));
        });
        Cinematics.register("final_sexta", (c, a) -> {
            KeinLooreEntity k = SagaWorld.kein(c.level);
            if (k == null) {
                return List.of();
            }
            return List.of(Shot.orbit(k.position(), k.getId(), 5.0F, 2.0F, 90.0F, 270.0F, 300).lookAtEntity(k.getId()).fov(60, 45));
        });
        Cinematics.register("final_nexo", (c, a) -> {
            ServerPlayer h = hero(c);
            if (h == null) {
                return List.of();
            }
            return List.of(Shot.dolly(h.getEyePosition(), h.position().add(0, 40, 0), 300).lookAt(h.position().add(30, 90, 0)).fov(70, 85));
        });
    }
}
