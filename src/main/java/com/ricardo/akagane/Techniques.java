package com.ricardo.akagane;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.joml.Vector3f;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Toda la logica de Akagane (Oro Rojo).
 *
 * Estados: SELLADA -> SHIKAI -> BANKAI.
 * Las marcas viven en el servidor; se dibujan con particulas rojas.
 * El reiatsu (energia espiritual) se guarda en el propio item y se ve como barra.
 */
public final class Techniques {
    private Techniques() {}

    public static final int MAX_REI = 1000;
    public static final int SEALED = 0;
    public static final int SHIKAI = 1;
    public static final int BANKAI = 2;

    private static final int SHIKAI_CAP = 5;
    private static final int BANKAI_CAP = 40;

    // Costes de reiatsu
    private static final float RELEASE_MIN = 300F;
    private static final float RELEASE_COST = 100F;
    private static final float BANKAI_MIN = 500F;
    private static final float BANKAI_COST = 150F;
    private static final float MARK_COST_SHIKAI = 40F;
    private static final float MARK_COST_BANKAI = 25F;
    private static final float COST_ONE = 60F;
    private static final float COST_GYAKUZAN = 200F;
    private static final float COST_CONNECT = 50F;
    private static final float COST_CHAIN = 250F;
    private static final float COST_RETSUDAN = 500F;

    // Debilidad: por debajo de este % las marcas se disipan
    private static final float WEAK_FRACTION = 0.08F;

    private static final DustParticleOptions RED = new DustParticleOptions(new Vector3f(1.0F, 0.05F, 0.05F), 1.2F);
    private static final DustParticleOptions RED_BIG = new DustParticleOptions(new Vector3f(0.9F, 0.02F, 0.06F), 1.8F);
    private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.62F, 0.12F), 1.4F);

    private static final double[] ANGLES = {-38, 32, -22, 46, -52, 14};

    // ------------------------------------------------------------------ datos

    private static final class Mark {
        final ResourceKey<Level> dim;
        final Vec3 a;
        final Vec3 b;
        final Vec3 center;
        int chain = -1;

        Mark(ResourceKey<Level> dim, Vec3 a, Vec3 b) {
            this.dim = dim;
            this.a = a;
            this.b = b;
            this.center = a.add(b).scale(0.5D);
        }
    }

    private static final class Data {
        final List<Mark> marks = new ArrayList<>();
        float rei = -1F;
        long lastSeen = 0L;
        long lastProcessed = -1L;
        long lastMark = -100L;
        long lastUse = -100L;
        long lastSync = 0L;
        int counter = 0;
        int nextChain = 0;
    }

    private static final Map<UUID, Data> DATA = new HashMap<>();

    public static void forget(UUID id) {
        DATA.remove(id);
    }

    private static Data data(ServerPlayer p, ItemStack stack) {
        Data d = DATA.computeIfAbsent(p.getUUID(), k -> new Data());
        if (d.rei < 0F) {
            d.rei = rei(stack);
        }
        return d;
    }

    // ---------------------------------------------------------- estado en el item

    public static CompoundTag tag(ItemStack s) {
        return s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static int state(ItemStack s) {
        return tag(s).getInt("state");
    }

    public static int rei(ItemStack s) {
        CompoundTag t = tag(s);
        return t.contains("rei") ? t.getInt("rei") : MAX_REI;
    }

    private static void setState(ItemStack s, int st) {
        CustomData.update(DataComponents.CUSTOM_DATA, s, t -> t.putInt("state", st));
        s.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(st));
        if (st > 0) {
            s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, Boolean.TRUE);
        } else {
            s.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }
    }

    private static void sync(ItemStack s, Data d) {
        int v = Math.round(d.rei);
        if (rei(s) != v) {
            CustomData.update(DataComponents.CUSTOM_DATA, s, t -> t.putInt("rei", v));
        }
    }

    private static ItemStack held(ServerPlayer p) {
        if (p.getMainHandItem().getItem() instanceof AkaganeItem) {
            return p.getMainHandItem();
        }
        if (p.getOffhandItem().getItem() instanceof AkaganeItem) {
            return p.getOffhandItem();
        }
        return null;
    }

    private static ServerLevel lvl(ServerPlayer p) {
        return (ServerLevel) p.level();
    }

    // -------------------------------------------------------------- tick

    public static void tick(ServerPlayer p, ItemStack stack, boolean selected) {
        ServerLevel l = lvl(p);
        long now = l.getGameTime();
        Data d = data(p, stack);

        // Si la espada estuvo fuera del inventario un buen rato, las marcas se pierden.
        if (now - d.lastSeen > 60L && !d.marks.isEmpty()) {
            d.marks.clear();
        }
        d.lastSeen = now;

        if (d.lastProcessed == now) {
            return;
        }
        d.lastProcessed = now;

        int st = state(stack);

        float delta;
        if (st == SEALED) {
            delta = 1.5F;
        } else if (st == SHIKAI) {
            delta = 0.12F - 0.05F;
        } else {
            delta = 0.05F - 0.15F;
        }
        delta -= d.marks.size() * 0.03F;
        d.rei = Mth.clamp(d.rei + delta, 0F, (float) MAX_REI);

        if (st != SEALED && d.rei <= 0.5F) {
            seal(p, stack, d, "§7Tu reiatsu se agoto. Akagane vuelve a su forma sellada.");
            sync(stack, d);
            return;
        }

        // Debilidad: sin energia espiritual suficiente las marcas se disipan.
        if (!d.marks.isEmpty() && d.rei < MAX_REI * WEAK_FRACTION) {
            for (Mark m : d.marks) {
                if (m.dim.equals(l.dimension())) {
                    line(l, ParticleTypes.SMOKE, m.a, m.b, 0.35D);
                }
            }
            d.marks.clear();
            bar(p, "§cTu reiatsu es demasiado bajo: las marcas se disipan...");
        }

        if (now - d.lastSync >= 5L) {
            sync(stack, d);
            d.lastSync = now;
        }

        if (now % 4L == 0L && !d.marks.isEmpty()) {
            drawMarks(l, d);
        }
        if (st == BANKAI && now % 3L == 0L) {
            scar(p, l);
        }
        if (selected && now % 10L == 0L) {
            int pct = Math.round(d.rei * 100F / MAX_REI);
            String rc = pct < 20 ? "§c" : "§f";
            if (st == SEALED) {
                bar(p, "§7Sellada §8| §7Reiatsu " + rc + pct + "%");
            } else {
                int cap = st == SHIKAI ? SHIKAI_CAP : BANKAI_CAP;
                String name = st == SHIKAI ? "§cShikai" : "§4Bankai";
                bar(p, name + " §8| §7Marcas §f" + d.marks.size() + "/" + cap + " §8| §7Reiatsu " + rc + pct + "%");
            }
        }
    }

    // ------------------------------------------------- cada corte deja una marca

    public static void onSwing(ServerPlayer p, ItemStack stack) {
        int st = state(stack);
        if (st == SEALED) {
            return;
        }
        ServerLevel l = lvl(p);
        long now = l.getGameTime();
        Data d = data(p, stack);
        if (now - d.lastUse <= 3L || now - d.lastMark < 10L) {
            return;
        }
        d.lastMark = now;

        float cost = st == SHIKAI ? MARK_COST_SHIKAI : MARK_COST_BANKAI;
        if (d.rei < cost + MAX_REI * WEAK_FRACTION + 5F) {
            bar(p, "§7Reiatsu demasiado bajo para marcar.");
            return;
        }
        d.rei -= cost;

        int cap = st == SHIKAI ? SHIKAI_CAP : BANKAI_CAP;
        while (d.marks.size() >= cap) {
            d.marks.remove(0);
        }
        Mark m = makeMark(p, l, d.counter++, st);
        d.marks.add(m);

        line(l, RED_BIG, m.a, m.b, 0.12D);
        l.sendParticles(ParticleTypes.CRIT, m.center.x, m.center.y, m.center.z, 8, 0.3D, 0.3D, 0.3D, 0.2D);
        sound(l, m.center, SoundEvents.PLAYER_ATTACK_SWEEP, 0.6F, 0.6F);
        sync(stack, d);
    }

    private static Mark makeMark(ServerPlayer p, ServerLevel l, int idx, int st) {
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        double reach = 3.6D;
        Vec3 end = eye.add(look.scale(reach));

        BlockHitResult bhr = l.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        double dist = bhr.getType() == HitResult.Type.MISS ? reach : bhr.getLocation().distanceTo(eye);
        Vec3 rayEnd = eye.add(look.scale(dist));
        EntityHitResult ehr = ProjectileUtil.getEntityHitResult(
                p, eye, rayEnd, new AABB(eye, rayEnd).inflate(1.0D),
                e -> !e.isSpectator() && e.isPickable(), dist * dist);

        Vec3 c;
        Vec3 n = null;
        if (ehr != null) {
            c = ehr.getEntity().getBoundingBox().getCenter();
        } else if (bhr.getType() == HitResult.Type.BLOCK) {
            Direction dir = bhr.getDirection();
            n = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
            c = bhr.getLocation().add(n.scale(0.03D));
        } else {
            c = eye.add(look.scale(2.6D));
        }

        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = look.cross(up);
        if (right.lengthSqr() < 1.0E-4D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize();
        Vec3 upv = right.cross(look).normalize();

        double ang = Math.toRadians(ANGLES[idx % ANGLES.length]);
        Vec3 dv = right.scale(Math.cos(ang)).add(upv.scale(Math.sin(ang)));
        if (n != null) {
            dv = dv.subtract(n.scale(dv.dot(n)));
            if (dv.lengthSqr() < 0.04D) {
                dv = upv.subtract(n.scale(upv.dot(n)));
                if (dv.lengthSqr() < 0.04D) {
                    dv = right.subtract(n.scale(right.dot(n)));
                }
            }
        }
        dv = dv.normalize();

        double half = (st == BANKAI ? 3.8D : 3.2D) / 2.0D;
        return new Mark(l.dimension(), c.subtract(dv.scale(half)), c.add(dv.scale(half)));
    }

    // ------------------------------------------------------ clic derecho

    public static void use(ServerPlayer p, ItemStack stack) {
        ServerLevel l = lvl(p);
        Data d = data(p, stack);
        d.lastUse = l.getGameTime();
        int st = state(stack);
        boolean sneak = p.isShiftKeyDown();
        float pitch = p.getXRot();

        if (st == SEALED) {
            releaseShikai(p, stack, d);
        } else if (st == SHIKAI) {
            if (sneak && pitch > 70F) {
                bankai(p, stack, d);
            } else if (sneak) {
                gyakuzan(p, d);
            } else {
                fireOldest(p, d);
            }
        } else {
            if (sneak && pitch > 70F) {
                seal(p, stack, d, "§7Guardas a Akagane. Vuelve a su forma sellada.");
            } else if (sneak && pitch < -60F) {
                retsudan(p, d);
            } else if (sneak) {
                fireChains(p, d);
            } else {
                connect(p, d);
            }
        }
        sync(stack, d);
    }

    // ------------------------------------------------------------ chat

    public static void onChat(ServerPlayer p, String raw) {
        ItemStack stack = held(p);
        if (stack == null) {
            return;
        }
        String s = Normalizer.normalize(raw, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        Data d = data(p, stack);
        d.lastUse = lvl(p).getGameTime();
        int st = state(stack);

        if (s.contains("arde") && s.contains("akagane")) {
            if (st == SEALED) {
                releaseShikai(p, stack, d);
            }
        } else if (s.contains("retsudan")) {
            if (st == BANKAI) {
                retsudan(p, d);
            } else {
                bar(p, "§7Guren Retsudan solo existe en Bankai.");
            }
        } else if (s.contains("gyakuzan")) {
            if (st == SHIKAI) {
                gyakuzan(p, d);
            } else {
                bar(p, "§7Gyakuzan es una tecnica del Shikai.");
            }
        } else if (s.contains("bankai") || s.contains("guren no kiba")) {
            bankai(p, stack, d);
        } else if (s.equals("sellar") || s.equals("sellada") || s.equals("seal") || s.equals("sealed")) {
            if (st != SEALED) {
                seal(p, stack, d, "§7Akagane vuelve a su forma sellada.");
            }
        } else {
            return;
        }
        sync(stack, d);
    }

    // -------------------------------------------------- transformaciones

    private static void releaseShikai(ServerPlayer p, ItemStack stack, Data d) {
        if (state(stack) != SEALED) {
            return;
        }
        if (d.rei < RELEASE_MIN) {
            bar(p, "§7Necesitas mas reiatsu para liberar a Akagane.");
            return;
        }
        d.rei -= RELEASE_COST;
        d.marks.clear();
        setState(stack, SHIKAI);
        ServerLevel l = lvl(p);
        sound(l, p.position(), SoundEvents.BLAZE_SHOOT, 1.0F, 0.6F);
        sound(l, p.position(), SoundEvents.FIRECHARGE_USE, 1.0F, 0.8F);
        l.sendParticles(RED_BIG, p.getX(), p.getY() + 1.0D, p.getZ(), 70, 0.6D, 0.8D, 0.6D, 0.05D);
        l.sendParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 1.0D, p.getZ(), 25, 0.5D, 0.7D, 0.5D, 0.06D);
        title(p, "§4Akagane", "§c«Arde, Akagane.»");
        p.sendSystemMessage(Component.literal("§c«Arde, Akagane.»"));
    }

    private static void bankai(ServerPlayer p, ItemStack stack, Data d) {
        if (state(stack) != SHIKAI) {
            bar(p, "§7Primero debes liberar el Shikai.");
            return;
        }
        if (d.rei < BANKAI_MIN) {
            bar(p, "§7Necesitas mas reiatsu para el Bankai.");
            return;
        }
        d.rei -= BANKAI_COST;
        setState(stack, BANKAI);
        ServerLevel l = lvl(p);
        sound(l, p.position(), SoundEvents.WITHER_SPAWN, 0.6F, 1.5F);
        sound(l, p.position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 0.8F, 1.2F);
        l.sendParticles(RED_BIG, p.getX(), p.getY() + 1.0D, p.getZ(), 140, 1.0D, 1.0D, 1.0D, 0.08D);
        l.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY() + 1.0D, p.getZ(), 2, 0.4D, 0.4D, 0.4D, 0.0D);
        title(p, "§4Guren no Kiba", "§cBankai — Akagane: Colmillo Carmesi");
        p.sendSystemMessage(Component.literal("§4Bankai — Akagane: Guren no Kiba"));
    }

    private static void seal(ServerPlayer p, ItemStack stack, Data d, String msg) {
        d.marks.clear();
        setState(stack, SEALED);
        ServerLevel l = lvl(p);
        l.sendParticles(ParticleTypes.SMOKE, p.getX(), p.getY() + 1.0D, p.getZ(), 25, 0.4D, 0.6D, 0.4D, 0.02D);
        sound(l, p.position(), SoundEvents.FIRECHARGE_USE, 0.6F, 0.5F);
        bar(p, msg);
    }

    // ----------------------------------------------------- tecnicas Shikai

    /** Clic derecho en Shikai: el corte mas antiguo reaparece exactamente donde se hizo. */
    private static void fireOldest(ServerPlayer p, Data d) {
        ServerLevel l = lvl(p);
        Mark m = null;
        for (Mark x : d.marks) {
            if (x.dim.equals(l.dimension())) {
                m = x;
                break;
            }
        }
        if (m == null) {
            bar(p, "§7No hay marcas. Corta algo primero.");
            return;
        }
        if (!pay(p, d, COST_ONE)) {
            return;
        }
        d.marks.remove(m);
        fireMark(p, l, m, 1.0F, 1.25D, false);
        sound(l, m.center, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.7F);
        sound(l, m.center, SoundEvents.BLAZE_SHOOT, 0.4F, 1.6F);
    }

    /** Gyakuzan: las marcas (hasta 5) reaparecen todas de golpe. */
    private static void gyakuzan(ServerPlayer p, Data d) {
        ServerLevel l = lvl(p);
        if (d.marks.isEmpty()) {
            bar(p, "§7No hay marcas que activar.");
            return;
        }
        if (!pay(p, d, COST_GYAKUZAN)) {
            return;
        }
        int n = 0;
        for (Mark m : d.marks) {
            fireMark(p, l, m, 1.15F, 1.3D, true);
            n++;
        }
        d.marks.clear();
        sound(l, p.position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 0.6F, 1.6F);
        sound(l, p.position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.5F);
        title(p, "§4Gyakuzan", "§c" + n + " cortes reaparecen");
    }

    // ----------------------------------------------------- tecnicas Bankai

    /** Clic derecho en Bankai: une todas las marcas sueltas en un solo corte. */
    private static void connect(ServerPlayer p, Data d) {
        ServerLevel l = lvl(p);
        List<Mark> loose = new ArrayList<>();
        for (Mark m : d.marks) {
            if (m.chain < 0 && m.dim.equals(l.dimension())) {
                loose.add(m);
            }
        }
        if (loose.size() < 2) {
            bar(p, "§7Necesitas al menos 2 marcas sin conectar.");
            return;
        }
        if (!pay(p, d, COST_CONNECT)) {
            return;
        }
        int id = d.nextChain++;
        for (Mark m : loose) {
            m.chain = id;
        }
        for (int i = 0; i + 1 < loose.size(); i++) {
            line(l, GOLD, loose.get(i).center, loose.get(i + 1).center, 0.2D);
        }
        sound(l, p.position(), SoundEvents.PLAYER_ATTACK_CRIT, 0.8F, 0.6F);
        bar(p, "§6" + loose.size() + " cortes conectados en uno solo.");
    }

    /** Agachado + clic derecho en Bankai: libera los cortes conectados. */
    private static void fireChains(ServerPlayer p, Data d) {
        ServerLevel l = lvl(p);
        Map<Integer, List<Mark>> chains = groupChains(d, l);
        if (chains.isEmpty()) {
            bar(p, "§7No hay cortes conectados. Usa clic derecho para conectar.");
            return;
        }
        if (!pay(p, d, COST_CHAIN)) {
            return;
        }
        float base = baseDamage(p);
        for (List<Mark> c : chains.values()) {
            for (Mark m : c) {
                fireMark(p, l, m, 1.0F, 1.3D, false);
            }
            chainCuts(p, l, c, base * 1.6F);
        }
        d.marks.removeIf(m -> m.chain >= 0);
        sound(l, p.position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 0.7F, 1.4F);
        sound(l, p.position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.4F);
    }

    /** Guren Retsudan: todas las marcas del combate se encienden y cortan a la vez. */
    private static void retsudan(ServerPlayer p, Data d) {
        ServerLevel l = lvl(p);
        if (d.marks.isEmpty()) {
            bar(p, "§7No hay marcas que encender.");
            return;
        }
        if (!pay(p, d, COST_RETSUDAN)) {
            return;
        }
        float base = baseDamage(p);
        for (Mark m : d.marks) {
            fireMark(p, l, m, 1.3F, 1.4D, true);
        }
        for (List<Mark> c : groupChains(d, l).values()) {
            chainCuts(p, l, c, base * 1.6F);
        }
        int n = d.marks.size();
        d.marks.clear();
        sound(l, p.position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 1.2F, 1.0F);
        sound(l, p.position(), SoundEvents.WITHER_SPAWN, 0.5F, 1.8F);
        sound(l, p.position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.3F);
        title(p, "§4Guren Retsudan", "§c" + n + " lineas carmesi cortan a la vez");
        p.sendSystemMessage(Component.literal("§4Guren Retsudan"));
    }

    private static Map<Integer, List<Mark>> groupChains(Data d, ServerLevel l) {
        Map<Integer, List<Mark>> chains = new LinkedHashMap<>();
        for (Mark m : d.marks) {
            if (m.chain >= 0 && m.dim.equals(l.dimension())) {
                chains.computeIfAbsent(m.chain, k -> new ArrayList<>()).add(m);
            }
        }
        return chains;
    }

    private static void chainCuts(ServerPlayer p, ServerLevel l, List<Mark> c, float dmg) {
        for (int i = 0; i + 1 < c.size(); i++) {
            Vec3 a = c.get(i).center;
            Vec3 b = c.get(i + 1).center;
            if (a.distanceTo(b) > 64.0D) {
                continue;
            }
            line(l, GOLD, a, b, 0.15D);
            line(l, ParticleTypes.END_ROD, a, b, 0.5D);
            damage(p, l, a, b, dmg, 1.6D);
        }
    }

    // ---------------------------------------------------------- utilidades

    private static boolean pay(ServerPlayer p, Data d, float cost) {
        if (d.rei < cost) {
            bar(p, "§7Reiatsu insuficiente (" + Math.round(cost / 10F) + "% necesario).");
            return false;
        }
        d.rei -= cost;
        return true;
    }

    private static float baseDamage(ServerPlayer p) {
        return (float) p.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private static void fireMark(ServerPlayer p, ServerLevel l, Mark m, float mul, double radius, boolean big) {
        if (!m.dim.equals(l.dimension())) {
            return;
        }
        slashFx(l, m.a, m.b, big);
        damage(p, l, m.a, m.b, baseDamage(p) * mul, radius);
    }

    private static void damage(ServerPlayer p, ServerLevel l, Vec3 a, Vec3 b, float dmg, double radius) {
        AABB area = new AABB(a, b).inflate(radius + 1.0D);
        List<LivingEntity> list = l.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != p && e.isAlive() && !e.isAlliedTo(p) && !(e instanceof TamableAnimal t && t.isOwnedBy(p)));
        for (LivingEntity e : list) {
            AABB box = e.getBoundingBox().inflate(radius);
            if (box.clip(a, b).isPresent() || box.contains(a) || box.contains(b)) {
                e.invulnerableTime = 0;
                e.hurt(l.damageSources().playerAttack(p), dmg);
            }
        }
    }

    private static void slashFx(ServerLevel l, Vec3 a, Vec3 b, boolean big) {
        Vec3 ab = b.subtract(a);
        int n = (int) Math.min(60.0D, Math.max(4.0D, ab.length() / 0.14D));
        for (int i = 0; i <= n; i++) {
            Vec3 q = a.add(ab.scale(i / (double) n));
            l.sendParticles(big ? RED_BIG : RED, q.x, q.y, q.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            if (i % 3 == 0) {
                l.sendParticles(ParticleTypes.END_ROD, q.x, q.y, q.z, 1, 0.02D, 0.02D, 0.02D, 0.01D);
            }
            if (i % 5 == 0) {
                l.sendParticles(ParticleTypes.CRIT, q.x, q.y, q.z, 2, 0.15D, 0.15D, 0.15D, 0.2D);
            }
        }
        Vec3 mid = a.add(b).scale(0.5D);
        l.sendParticles(ParticleTypes.SWEEP_ATTACK, mid.x, mid.y, mid.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static void drawMarks(ServerLevel l, Data d) {
        Map<Integer, List<Mark>> chains = new LinkedHashMap<>();
        for (Mark m : d.marks) {
            if (!m.dim.equals(l.dimension())) {
                continue;
            }
            line(l, RED, m.a, m.b, 0.28D);
            if (m.chain >= 0) {
                chains.computeIfAbsent(m.chain, k -> new ArrayList<>()).add(m);
            }
        }
        for (List<Mark> c : chains.values()) {
            for (int i = 0; i + 1 < c.size(); i++) {
                line(l, GOLD, c.get(i).center, c.get(i + 1).center, 0.45D);
            }
        }
    }

    private static void line(ServerLevel l, ParticleOptions o, Vec3 a, Vec3 b, double step) {
        Vec3 ab = b.subtract(a);
        int n = (int) Math.min(80.0D, Math.max(1.0D, ab.length() / step));
        for (int i = 0; i <= n; i++) {
            Vec3 q = a.add(ab.scale(i / (double) n));
            l.sendParticles(o, q.x, q.y, q.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    /** Bankai: enorme cicatriz roja en zigzag sobre el brazo derecho. */
    private static void scar(ServerPlayer p, ServerLevel l) {
        double r = Math.toRadians(p.yBodyRot);
        Vec3 right = new Vec3(-Math.cos(r), 0.0D, -Math.sin(r));
        Vec3 fwd = new Vec3(-Math.sin(r), 0.0D, Math.cos(r));
        Vec3 base = p.position();
        Vec3 shoulder = base.add(0.0D, 1.45D, 0.0D).add(right.scale(0.36D));
        Vec3 hand = base.add(0.0D, 0.8D, 0.0D).add(right.scale(0.40D)).add(fwd.scale(0.30D));
        Vec3 ab = hand.subtract(shoulder);
        for (int i = 0; i <= 10; i++) {
            double z = (i % 2 == 0 ? 1.0D : -1.0D) * 0.05D;
            Vec3 q = shoulder.add(ab.scale(i / 10.0D)).add(right.scale(z)).add(fwd.scale(z * 0.6D));
            l.sendParticles(RED_BIG, q.x, q.y, q.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        l.sendParticles(ParticleTypes.SMALL_FLAME, hand.x, hand.y, hand.z, 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    private static void sound(ServerLevel l, Vec3 pos, SoundEvent ev, float vol, float pitch) {
        l.playSound(null, pos.x, pos.y, pos.z, ev, SoundSource.PLAYERS, vol, pitch);
    }

    private static void bar(ServerPlayer p, String msg) {
        p.displayClientMessage(Component.literal(msg), true);
    }

    private static void title(ServerPlayer p, String title, String sub) {
        p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 40, 10));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(sub)));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
    }
}
