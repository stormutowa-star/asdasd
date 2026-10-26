package com.keinloree.client.vfx;

import com.keinloree.KeinLooreMod;
import com.keinloree.net.VfxS2C;
import com.keinloree.vfx.VfxType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Un efecto activo del cliente: datos del paquete, edad y dibujo. */
public abstract class VfxEffect {
    public static final ResourceLocation GLOW = tex("glow");
    public static final ResourceLocation RING = tex("ring");
    public static final ResourceLocation SLASH = tex("slash");
    public static final ResourceLocation SPARK = tex("spark");
    public static final ResourceLocation GLYPHS = tex("glyphs");
    public static final ResourceLocation RUNE = tex("rune");
    public static final ResourceLocation CLOCK = tex("clock");
    public static final ResourceLocation STAR = tex("star");
    public static final ResourceLocation SMOKE = tex("smoke");
    public static final ResourceLocation BEAM = tex("beam");
    public static final ResourceLocation PILLAR = tex("pillar");
    /** Texturas aditivas que usan los efectos (se reservan buffers propios para ellas). */
    public static final ResourceLocation[] ADDITIVE_TEXTURES = {GLOW, RING, SLASH, SPARK, GLYPHS, RUNE, CLOCK, STAR, BEAM, PILLAR};

    public final VfxType type;
    public double x;
    public double y;
    public double z;
    public final double dx;
    public final double dy;
    public final double dz;
    public final int color;
    public final float scale;
    public final int ticks;
    public final int entityId;
    public final float extra;
    public final long seed;
    public final float cr;
    public final float cg;
    public final float cb;
    public int age;
    /** Desplazamiento respecto a la entidad seguida. */
    protected Vec3 followOffset = Vec3.ZERO;
    private boolean offsetSet;

    protected VfxEffect(VfxType type, VfxS2C m, long seed) {
        this.type = type;
        this.x = m.x();
        this.y = m.y();
        this.z = m.z();
        this.dx = m.dx();
        this.dy = m.dy();
        this.dz = m.dz();
        this.color = m.color();
        this.scale = m.scale() <= 0.0F ? 1.0F : m.scale();
        this.ticks = Math.max(1, m.ticks());
        this.entityId = m.entityId();
        this.extra = m.extra();
        this.seed = seed;
        this.cr = (color >> 16 & 255) / 255.0F;
        this.cg = (color >> 8 & 255) / 255.0F;
        this.cb = (color & 255) / 255.0F;
    }

    private static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "textures/vfx/" + name + ".png");
    }

    /** Ticks extra tras {@link #ticks} (estelas y humo que se desvanecen). */
    protected int tail() {
        return 0;
    }

    public boolean dead() {
        return age >= ticks + tail();
    }

    public void tick(ClientLevel level) {
        age++;
        if (entityId >= 0) {
            Entity e = level.getEntity(entityId);
            if (e != null && !e.isRemoved()) {
                if (!offsetSet) {
                    followOffset = new Vec3(x, y, z).subtract(e.position());
                    offsetSet = true;
                }
                this.x = e.getX() + followOffset.x;
                this.y = e.getY() + followOffset.y;
                this.z = e.getZ() + followOffset.z;
                onFollow(e);
            }
        }
        onTick(level);
    }

    /** Gancho tras mover el efecto con la entidad. */
    protected void onFollow(Entity e) {
    }

    protected void onTick(ClientLevel level) {
    }

    /** Dibuja con tiempo fraccionario (edad + parcial). */
    public abstract void render(VfxDraw d, float time);

    // ------------------------------------------------------------------ utilidades

    /** Fundido de entrada y salida (0..1). */
    protected float env(float time, float in, float out) {
        float a = in <= 0.0F ? 1.0F : Mth.clamp(time / in, 0.0F, 1.0F);
        float b = out <= 0.0F ? 1.0F : Mth.clamp((ticks - time) / out, 0.0F, 1.0F);
        return a * b;
    }

    protected float env(float time) {
        return env(time, Math.min(3.0F, ticks * 0.25F), Math.min(10.0F, ticks * 0.5F));
    }

    protected static float easeOut(float t) {
        float u = 1.0F - Mth.clamp(t, 0.0F, 1.0F);
        return 1.0F - u * u * u;
    }

    /** Numero pseudoaleatorio estable 0..1 a partir de un indice. */
    protected float rnd(int i, int salt) {
        long h = seed * 0x9E3779B97F4A7C15L + i * 0xBF58476D1CE4E5B9L + salt * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        h *= 0xD6E8FEB86659FD93L;
        h ^= h >>> 29;
        return (h >>> 11 & 0xFFFFFFL) / (float) 0x1000000;
    }

    /** Base ortonormal (e1, e2) del plano perpendicular a n. */
    protected static Vec3[] basis(Vec3 n) {
        Vec3 nn = n.lengthSqr() < 1.0E-9D ? new Vec3(0, 1, 0) : n.normalize();
        Vec3 h = Math.abs(nn.y) < 0.99D ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 e1 = nn.cross(h).normalize();
        Vec3 e2 = nn.cross(e1).normalize();
        return new Vec3[]{e1, e2, nn};
    }
}
