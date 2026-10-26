package com.keinloree.client.vfx;

import com.keinloree.net.VfxS2C;
import com.keinloree.vfx.VfxType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Fabrica y dibujo de todos los efectos del motor de VFX. */
public final class VfxEffects {
    private static long counter = 1;

    private VfxEffects() {
    }

    /** Crea el efecto del paquete o null si el tipo no existe. */
    public static VfxEffect create(VfxS2C m) {
        VfxType t = VfxType.byId(m.type());
        if (t == null) {
            return null;
        }
        long seed = (counter++) * 7919L + m.ticks();
        return switch (t) {
            case BEAM -> new Beam(m, seed);
            case RING -> new Ring(m, seed);
            case SHOCKWAVE -> new Shockwave(m, seed);
            case SLASH -> new Slash(m, seed);
            case ORB -> new Orb(m, seed);
            case PILLAR -> new Pillar(m, seed);
            case RUNE_CIRCLE -> new RuneCircle(m, seed);
            case LIGHTNING -> new Lightning(m, seed);
            case CODE_RAIN -> new CodeRain(m, seed);
            case BURST -> new Burst(m, seed);
            case DISTORTION -> new Distortion(m, seed);
            case TIME_FREEZE -> new TimeFreeze(m, seed);
            case HYPERSPACE -> new Hyperspace(m, seed);
            case TRAIL -> new Trail(m, seed);
            case EXPLOSION -> new Explosion(m, seed);
            case MUZZLE -> new Muzzle(m, seed);
        };
    }

    /** Color casi blanco con un toque del color del efecto (nucleo). */
    private static float whiten(float c) {
        return 0.55F + c * 0.45F;
    }

    // ================================================================== BEAM
    static final class Beam extends VfxEffect {
        Beam(VfxS2C m, long seed) {
            super(VfxType.BEAM, m, seed);
        }

        @Override
        public void render(VfxDraw d, float time) {
            float env = env(time, 2.0F, Math.min(8.0F, ticks * 0.5F));
            float vib = 1.0F + 0.08F * Mth.sin(time * 2.9F + seed) + 0.05F * Mth.sin(time * 5.3F);
            float w = scale * vib;
            Vec3 a = new Vec3(x, y, z);
            Vec3 b = new Vec3(dx, dy, dz);
            double len = a.distanceTo(b);
            // cuerpo: prisma hexagonal de color y nucleo blanco mas fino
            VertexConsumer f = d.flat();
            d.prism(f, a, b, w, w, 6, cr, cg, cb, 0.40F * env, 0.40F * env);
            d.prism(f, a, b, w * 0.4F, w * 0.4F, 6, 1.0F, 1.0F, 1.0F, 0.95F * env, 0.95F * env);
            // brillo con vetas de energia que corren por el haz
            VertexConsumer t = d.add(VfxEffect.BEAM);
            VfxDraw.Pts p = new VfxDraw.Pts(2);
            p.add(a.x, a.y, a.z, w * 5.0F, 0.75F * env);
            p.add(b.x, b.y, b.z, w * 5.0F, 0.75F * env);
            float u0 = -time * 0.5F;
            d.ribbon(t, p, u0, u0 + (float) len * 0.35F, cr, cg, cb);
            // halos en los extremos
            VertexConsumer g = d.add(VfxEffect.GLOW);
            d.billboard(g, a.x, a.y, a.z, w * 5.0F, 0.0F, cr, cg, cb, 0.8F * env);
            d.billboard(g, b.x, b.y, b.z, w * 5.0F, 0.0F, cr, cg, cb, 0.8F * env);
            d.billboard(g, a.x, a.y, a.z, w * 2.2F, 0.0F, 1.0F, 1.0F, 1.0F, 0.9F * env);
            d.billboard(g, b.x, b.y, b.z, w * 2.2F, 0.0F, 1.0F, 1.0F, 1.0F, 0.9F * env);
        }
    }

    // ================================================================== RING
    static final class Ring extends VfxEffect {
        private final Vec3[] bs;

        Ring(VfxS2C m, long seed) {
            super(VfxType.RING, m, seed);
            this.bs = basis(new Vec3(dx, dy, dz));
        }

        @Override
        public void render(VfxDraw d, float time) {
            float env = env(time, 2.0F, Math.min(8.0F, ticks * 0.4F));
            float t = time / ticks;
            float radius = extra > 0.0F ? scale * (0.15F + 0.85F * easeOut(Math.min(1.0F, t * 1.6F))) : scale * (0.92F + 0.08F * Mth.sin(time * 0.6F));
            float thick = scale * 0.16F;
            VertexConsumer r = d.add(VfxEffect.RING);
            d.annulus(r, x, y, z, bs[0], bs[1], radius, radius - thick, 64, cr, cg, cb, 1.0F * env);
            d.annulus(r, x, y, z, bs[0], bs[1], radius - thick * 0.15, radius - thick * 0.6, 64, 1.0F, 1.0F, 1.0F, 0.7F * env);
            VertexConsumer g = d.add(VfxEffect.GLOW);
            d.plane(g, x, y, z, bs[0], bs[1], radius * 1.05F, 0.0F, cr, cg, cb, 0.12F * env);
        }
    }

    // ================================================================== SHOCKWAVE
    static final class Shockwave extends VfxEffect {
        Shockwave(VfxS2C m, long seed) {
            super(VfxType.SHOCKWAVE, m, seed);
        }

        @Override
        public void render(VfxDraw d, float time) {
            float t = Mth.clamp(time / ticks, 0.0F, 1.0F);
            float radius = scale * easeOut(t);
            float thick = Math.max(0.15F, scale * Mth.lerp(t, 0.35F, 0.06F));
            float fade = (1.0F - t) * (1.0F - t) * Mth.clamp(time / 1.5F, 0.0F, 1.0F);
            Vec3 e1 = new Vec3(1, 0, 0);
            Vec3 e2 = new Vec3(0, 0, 1);
            VertexConsumer r = d.add(VfxEffect.RING);
            d.annulus(r, x, y + 0.05D, z, e1, e2, radius, Math.max(0.0F, radius - thick), 72, cr, cg, cb, 1.1F * fade);
            d.annulus(r, x, y + 0.07D, z, e1, e2, radius - thick * 0.1, radius - thick * 0.5, 72, 1.0F, 1.0F, 1.0F, 0.8F * fade);
            // segundo aro mas lento y suave, mas bajo
            float r2 = scale * 0.7F * easeOut(Math.max(0.0F, t - 0.1F) / 0.9F);
            d.annulus(r, x, y + 0.04D, z, e1, e2, r2, Math.max(0.0F, r2 - thick * 0.8F), 56, cr, cg, cb, 0.5F * fade);
            VertexConsumer g = d.add(VfxEffect.GLOW);
            d.plane(g, x, y + 0.03D, z, e1, e2, radius * 1.1F, 0.0F, cr, cg, cb, 0.35F * (1.0F - t) * (1.0F - t) * (1.0F - t));
            // destello central
            float fl = Mth.clamp(1.0F - time / 6.0F, 0.0F, 1.0F);
            if (fl > 0.0F) {
                d.billboard(g, x, y + 0.6D, z, scale * 0.35F, 0.0F, 1.0F, 1.0F, 1.0F, fl);
            }
        }
    }

    // ================================================================== SLASH
    static final class Slash extends VfxEffect {
        Slash(VfxS2C m, long seed) {
            super(VfxType.SLASH, m, seed);
        }

        @Override
        public void render(VfxDraw d, float time) {
            float yaw = (float) Math.toRadians(dx);
            float pitch = (float) Math.toRadians(dy);
            float roll = (float) Math.toRadians(dz);
            // ejes locales: forward segun yaw/pitch de Minecraft, right horizontal, up perpendicular
            Vec3 f = new Vec3(-Mth.sin(yaw) * Mth.cos(pitch), -Mth.sin(pitch), Mth.cos(yaw) * Mth.cos(pitch));
            Vec3 right = new Vec3(-Mth.cos(yaw), 0.0D, -Mth.sin(yaw));
            Vec3 up = f.cross(right).normalize();
            // el roll gira el plano del arco alrededor de f
            Vec3 rr = right.scale(Mth.cos(roll)).add(up.scale(Mth.sin(roll)));
            double arc = Math.toRadians(extra > 0.0F ? extra : 160.0F);
            float t = Mth.clamp(time / ticks, 0.0F, 1.0F);
            float sweep = easeOut(Math.min(1.0F, t / 0.45F));
            double head = -arc / 2.0D + arc * sweep;
            double tail = Math.max(-arc / 2.0D, head - arc * 0.85D);
            float fadeOut = Mth.clamp((1.0F - t) / 0.45F, 0.0F, 1.0F);
            // tras el barrido la cola alcanza a la cabeza
            if (t > 0.45F) {
                tail = Mth.lerp((t - 0.45F) / 0.55F, (float) tail, (float) head);
            }
            int n = 28;
            VfxDraw.Pts outer = new VfxDraw.Pts(n + 1);
            VfxDraw.Pts core = new VfxDraw.Pts(n + 1);
            for (int i = 0; i <= n; i++) {
                double a = Mth.lerp(i / (double) n, tail, head);
                double s = i / (double) n;
                Vec3 p = new Vec3(x, y, z).add(f.scale(Math.cos(a) * scale)).add(rr.scale(Math.sin(a) * scale));
                // mas ancha en el centro del arco total
                double mid = (a + arc / 2.0D) / arc;
                float width = scale * (0.2F + 0.55F * (float) Math.pow(Math.sin(Math.PI * Mth.clamp((float) mid, 0.0F, 1.0F)), 0.8D));
                float k = (float) Math.pow(s, 0.7D) * fadeOut;
                outer.add(p.x, p.y, p.z, width, 1.1F * k);
                core.add(p.x, p.y, p.z, width * 0.4F, 0.9F * k);
            }
            VertexConsumer sl = d.add(VfxEffect.SLASH);
            d.ribbon(sl, outer, 0.0F, 1.0F, cr, cg, cb);
            d.ribbon(sl, core, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            // chispas en la punta
            Vec3 tip = new Vec3(x, y, z).add(f.scale(Math.cos(head) * scale)).add(rr.scale(Math.sin(head) * scale));
            VertexConsumer g = d.add(VfxEffect.GLOW);
            d.billboard(g, tip.x, tip.y, tip.z, scale * 0.35F, 0.0F, cr, cg, cb, 0.8F * fadeOut);
            d.billboard(g, tip.x, tip.y, tip.z, scale * 0.14F, 0.0F, 1.0F, 1.0F, 1.0F, fadeOut);
        }
    }

    // ================================================================== ORB
    static final class Orb extends VfxEffect {
        Orb(VfxS2C m, long seed) {
            super(VfxType.ORB, m, seed);
        }

        @Override
        public void render(VfxDraw d, float time) {
            float env = env(time, 4.0F, Math.min(10.0F, ticks * 0.4F));
            float pulse = 1.0F + 0.12F * Mth.sin(time * 0.55F) + 0.05F * Mth.sin(time * 1.7F);
            float s = scale * pulse * (0.6F + 0.4F * easeOut(time / 6.0F));
            VertexConsumer g = d.add(VfxEffect.GLOW);
            d.billboard(g, x, y, z, s * 2.6F, 0.0F, cr, cg, cb, 0.45F * env);
            d.billboard(g, x, y, z, s * 1.5F, 0.0F, cr, cg, cb, 1.0F * env);
            d.billboard(g, x, y, z, s * 0.8F, 0.0F, whiten(cr), whiten(cg), whiten(cb), 1.0F * env);
            d.billboard(g, x, y, z, s * 0.4F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F * env);
            VertexConsumer st = d.add(VfxEffect.STAR);
            d.billboard(st, x, y, z, s * 2.2F, time * 0.05F, cr, cg, cb, 0.8F * env);
            d.billboard(st, x, y, z, s * 1.4F, -time * 0.08F, 1.0F, 1.0F, 1.0F, 0.6F * env);
            // motas orbitando
            for (int i = 0; i < 5; i++) {
                float a = time * (0.12F + 0.03F * i) + i * 1.3F;
                float h = Mth.sin(time * 0.1F + i * 2.1F);
                double ox = Math.cos(a) * s * 1.7F;
                double oz = Math.sin(a) * s * 1.7F;
                d.billboard(g, x + ox, y + h * s * 0.7F, z + oz, s * 0.2F, 0.0F, 1.0F, 1.0F, 1.0F, 0.9F * env);
            }
        }
    }

    // ================================================================== PILLAR
    static final class Pillar extends VfxEffect {
        Pillar(VfxS2C m, long seed) {
            super(VfxType.PILLAR, m, seed);
        }

        @Override
        public void render(VfxDraw d, float time) {
            float env = env(time, 8.0F, Math.min(15.0F, ticks * 0.4F));
            float h = extra > 0.0F ? extra : (float) Math.max(10.0D, 320.0D - y);
            float rad = scale * (0.4F + 0.6F * easeOut(time / 10.0F));
            int sides = 10;
            VertexConsumer t = d.add(VfxEffect.PILLAR);
            float scroll = -time * 0.045F;
            float repeat = Math.max(1.0F, h / 16.0F);
            for (int i = 0; i < sides; i++) {
                double a0 = Math.PI * 2.0D * i / sides;
                double a1 = Math.PI * 2.0D * (i + 1) / sides;
                double x0 = x + Math.cos(a0) * rad;
                double z0 = z + Math.sin(a0) * rad;
                double x1 = x + Math.cos(a1) * rad;
                double z1 = z + Math.sin(a1) * rad;
                // base mas brillante, se apaga hacia la cima
                d.tv(t, x0, y, z0, 0.0F, scroll, cr, cg, cb, 1.0F * env);
                d.tv(t, x1, y, z1, 1.0F, scroll, cr, cg, cb, 1.0F * env);
                d.tv(t, x1, y + h, z1, 1.0F, scroll + repeat, cr, cg, cb, 0.35F * env);
                d.tv(t, x0, y + h, z0, 0.0F, scroll + repeat, cr, cg, cb, 0.35F * env);
            }
            VertexConsumer f = d.flat();
            // nucleo blanco fino
            for (int i = 0; i < 6; i++) {
                double a0 = Math.PI * 2.0D * i / 6;
                double a1 = Math.PI * 2.0D * (i + 1) / 6;
                double cr0 = rad * 0.28D;
                d.fv(f, x + Math.cos(a0) * cr0, y, z + Math.sin(a0) * cr0, 1, 1, 1, 0.9F * env);
                d.fv(f, x + Math.cos(a1) * cr0, y, z + Math.sin(a1) * cr0, 1, 1, 1, 0.9F * env);
                d.fv(f, x + Math.cos(a1) * cr0, y + h, z + Math.sin(a1) * cr0, 1, 1, 1, 0.25F * env);
                d.fv(f, x + Math.cos(a0) * cr0, y + h, z + Math.sin(a0) * cr0, 1, 1, 1, 0.25F * env);
            }
            // resplandor del suelo
            VertexConsumer g = d.add(VfxEffect.GLOW);
            d.plane(g, x, y + 0.08D, z, new Vec3(1, 0, 0), new Vec3(0, 0, 1), rad * 4.0F, 0.0F, cr, cg, cb, 0.8F * env);
            d.billboard(g, x, y + rad, z, rad * 3.0F, 0.0F, cr, cg, cb, 0.6F * env);
            d.billboard(g, x, y + rad, z, rad * 1.2F, 0.0F, 1.0F, 1.0F, 1.0F, 0.8F * env);
        }
    }

    // ================================================================== RUNE_CIRCLE
    static final class RuneCircle extends VfxEffect {
        RuneCircle(VfxS2C m, long seed) {
            super(VfxType.RUNE_CIRCLE, m, seed);
        }

        @Override
        public void render(VfxDraw d, float time) {
            float env = env(time, 8.0F, Math.min(14.0F, ticks * 0.4F));
            float grow = 0.25F + 0.75F * easeOut(time / 12.0F);
            float rad = scale * grow;
            Vec3 e1 = new Vec3(1, 0, 0);
            Vec3 e2 = new Vec3(0, 0, 1);
            VertexConsumer t = d.add(VfxEffect.RUNE);
            d.plane(t, x, y + 0.04D, z, e1, e2, rad, time * 0.02F, cr, cg, cb, 1.15F * env);
            d.plane(t, x, y + 0.06D, z, e1, e2, rad * 0.62F, -time * 0.035F, whiten(cr), whiten(cg), whiten(cb), 0.9F * env);
            VertexConsumer g = d.add(VfxEffect.GLOW);
            d.plane(g, x, y + 0.03D, z, e1, e2, rad * 1.1F, 0.0F, cr, cg, cb, 0.22F * env);
            VertexConsumer r = d.add(VfxEffect.RING);
            d.annulus(r, x, y + 0.05D, z, e1, e2, rad * 1.02F, rad * 0.94F, 64, 1.0F, 1.0F, 1.0F, 0.35F * env);
            // chispas que suben desde el circulo
            VertexConsumer s = d.add(VfxEffect.STAR);
            for (int i = 0; i < 10; i++) {
                float life = 30.0F;
                float ph = ((time + rnd(i, 1) * life) % life) / life;
                float a = rnd(i, 2) * Mth.TWO_PI + time * 0.01F;
                float rr = rnd(i, 3) * rad * 0.95F;
                d.billboard(s, x + Math.cos(a) * rr, y + 0.2D + ph * scale * 0.9F, z + Math.sin(a) * rr, 0.16F, 0.0F,
                        cr, cg, cb, (1.0F - ph) * env);
            }
        }
    }

    // ================================================================== LIGHTNING
    static final class Lightning extends VfxEffect {
        Lightning(VfxS2C m, long seed) {
            super(VfxType.LIGHTNING, m, seed);
        }

        private float jag(int step, int i, int salt) {
            return rnd(step * 131 + i, salt) * 2.0F - 1.0F;
        }

        @Override
        public void render(VfxDraw d, float time) {
            float env = env(time, 1.0F, Math.min(6.0F, ticks * 0.4F));
            int step = (int) (time / 2.0F);
            Vec3 a = new Vec3(x, y, z);
            Vec3 b = new Vec3(dx, dy, dz);
            Vec3 axis = b.subtract(a);
            double len = axis.length();
            if (len < 1.0E-4D) {
                return;
            }
            Vec3[] bs = basis(axis);
            int n = Mth.clamp((int) (len / 0.9D), 5, 40);
            VertexConsumer t = d.add(VfxEffect.BEAM);
            VertexConsumer g = d.add(VfxEffect.GLOW);
            float flick = 0.75F + 0.25F * (rnd(step, 9) > 0.35F ? 1.0F : 0.4F);
            drawBolt(d, t, a, axis, bs, n, len, step, 0, scale, env * flick, 1.0F);
            // dos ramas
            for (int br = 0; br < 2; br++) {
                int at = 2 + (int) (rnd(step + br, 20) * (n - 4));
                Vec3 from = a.add(axis.scale(at / (double) n));
                Vec3 dir = bs[0].scale(jag(step, br, 21)).add(bs[1].scale(jag(step, br, 22))).add(axis.normalize().scale(0.6D)).normalize();
                Vec3 to = from.add(dir.scale(len * 0.25D));
                Vec3 ax2 = to.subtract(from);
                drawBolt(d, t, from, ax2, basis(ax2), Math.max(4, n / 3), ax2.length(), step, 100 + br * 50, scale * 0.6F, env * flick * 0.8F, 0.0F);
            }
            d.billboard(g, a.x, a.y, a.z, scale * 8.0F, 0.0F, cr, cg, cb, 0.6F * env * flick);
            d.billboard(g, b.x, b.y, b.z, scale * 10.0F, 0.0F, cr, cg, cb, 0.7F * env * flick);
            d.billboard(g, b.x, b.y, b.z, scale * 4.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.9F * env * flick);
        }

        private void drawBolt(VfxDraw d, VertexConsumer t, Vec3 a, Vec3 axis, Vec3[] bs, int n, double len, int step,
                              int salt, float width, float k, float unused) {
            VfxDraw.Pts outer = new VfxDraw.Pts(n + 1);
            VfxDraw.Pts core = new VfxDraw.Pts(n + 1);
            double amp = Math.min(1.4D, len * 0.07D) + 0.15D;
            for (int i = 0; i <= n; i++) {
                double f = i / (double) n;
                double edge = Math.sin(Math.PI * f);
                Vec3 p = a.add(axis.scale(f));
                if (i != 0 && i != n) {
                    p = p.add(bs[0].scale(jag(step, i + salt, 30) * amp * edge)).add(bs[1].scale(jag(step, i + salt, 31) * amp * edge));
                }
                outer.add(p.x, p.y, p.z, width * 6.0F, 0.9F * k);
                core.add(p.x, p.y, p.z, width * 1.8F, 1.0F * k);
            }
            d.ribbon(t, outer, 0.125F, 0.125F, cr, cg, cb);
            d.ribbon(t, core, 0.125F, 0.125F, 1.0F, 1.0F, 1.0F);
        }
    }

    // ================================================================== CODE_RAIN
    static final class CodeRain extends VfxEffect {
        CodeRain(VfxS2C m, long seed) {
            super(VfxType.CODE_RAIN, m, seed);
        }

        @Override
        public void render(VfxDraw d, float time) {
            float env = env(time, 8.0F, Math.min(15.0F, ticks * 0.4F));
            float height = extra > 0.0F ? extra : 12.0F;
            int cols = Mth.clamp((int) (scale * scale * 2.5F), 8, 70);
            int len = 9;
            float gap = 0.5F;
            VertexConsumer vc = d.add(VfxEffect.GLYPHS);
            for (int c = 0; c < cols; c++) {
                double ang = rnd(c, 1) * Math.PI * 2.0D;
                double rr = Math.sqrt(rnd(c, 2)) * scale;
                double cxw = x + Math.cos(ang) * rr;
                double czw = z + Math.sin(ang) * rr;
                float speed = 0.22F + rnd(c, 3) * 0.3F;
                float cycle = height + len * gap;
                float head = ((time * speed + rnd(c, 4) * cycle) % cycle);
                for (int i = 0; i < len; i++) {
                    double gy = y + height - (head - i * gap);
                    if (gy > y + height || gy < y) {
                        continue;
                    }
                    int cell = (int) (rnd(c * 31 + i, 5 + (int) (time / 5.0F + i)) * 64.0F) & 63;
                    float u0 = (cell % 8) / 8.0F;
                    float v0 = (cell / 8) / 8.0F;
                    float tailK = 1.0F - i / (float) len;
                    float fadeEnd = Mth.clamp((float) ((gy - y) / 1.5D), 0.0F, 1.0F);
                    float k = tailK * tailK * env * fadeEnd * 1.4F;
                    if (i == 0) {
                        d.billboard(vc, cxw, gy, czw, 0.24F, 0.0F, u0, v0, u0 + 0.125F, v0 + 0.125F, 1.0F, 1.0F, 1.0F, env * fadeEnd * 1.3F);
                    } else {
                        d.billboard(vc, cxw, gy, czw, 0.22F, 0.0F, u0, v0, u0 + 0.125F, v0 + 0.125F, cr, cg, cb, k);
                    }
                }
            }
            VertexConsumer g = d.add(VfxEffect.GLOW);
            d.plane(g, x, y + 0.05D, z, new Vec3(1, 0, 0), new Vec3(0, 0, 1), scale * 1.2F, 0.0F, cr, cg, cb, 0.25F * env);
        }
    }

    // ================================================================== BURST
    static final class Burst extends VfxEffect {
        private final int count;
        private final double[][] vel;

        Burst(VfxS2C m, long seed) {
            super(VfxType.BURST, m, seed);
            this.count = Mth.clamp((int) (extra > 0.0F ? extra : 24.0F), 1, 120);
            this.vel = new double[count][3];
            for (int i = 0; i < count; i++) {
                double th = rnd(i, 1) * Math.PI * 2.0D;
                double cz2 = rnd(i, 2) * 2.0D - 1.0D;
                double sr = Math.sqrt(1.0D - cz2 * cz2);
                double sp = scale * (0.45D + rnd(i, 3) * 0.9D);
                vel[i][0] = Math.cos(th) * sr * sp;
                vel[i][1] = cz2 * sp + 0.05D;
                vel[i][2] = Math.sin(th) * sr * sp;
            }
        }

        @Override
        public void render(VfxDraw d, float time) {
            float t = time / ticks;
            float env = Mth.clamp(1.0F - t, 0.0F, 1.0F);
            VertexConsumer s = d.add(VfxEffect.SPARK);
            VertexConsumer g = d.add(VfxEffect.GLOW);
            double drag = 0.88D;
            for (int i = 0; i < count; i++) {
                // integracion cerrada con rozamiento: pos = v * (1 - drag^t)/(-ln drag)
                double k = (1.0D - Math.pow(drag, time)) / -Math.log(drag);
                double px = x + vel[i][0] * k;
                double py = y + vel[i][1] * k - 0.012D * time * time;
                double pz = z + vel[i][2] * k;
                double kb = (1.0D - Math.pow(drag, Math.max(0.0F, time - 1.5F))) / -Math.log(drag);
                double bx = x + vel[i][0] * kb;
                double by = y + vel[i][1] * kb - 0.012D * Math.max(0.0F, time - 1.5F) * Math.max(0.0F, time - 1.5F);
                double bz = z + vel[i][2] * kb;
                float w = 0.09F + rnd(i, 4) * 0.07F;
                float kk = env * (0.8F + rnd(i, 5) * 0.5F);
                d.streak(s, bx, by, bz, px, py, pz, w * 2.0F, cr, cg, cb, kk);
                d.streak(s, (bx + px) * 0.5D, (by + py) * 0.5D, (bz + pz) * 0.5D, px, py, pz, w, 1.0F, 1.0F, 1.0F, kk * 0.8F);
            }
            float fl = Mth.clamp(1.0F - time / 5.0F, 0.0F, 1.0F);
            if (fl > 0.0F) {
                d.billboard(g, x, y, z, scale * 2.2F + 0.4F, 0.0F, cr, cg, cb, 0.8F * fl);
                d.billboard(g, x, y, z, scale * 1.0F + 0.2F, 0.0F, 1.0F, 1.0F, 1.0F, fl);
            }
        }
    }

    // ================================================================== DISTORTION
    static final class Distortion extends VfxEffect {
        Distortion(VfxS2C m, long seed) {
            super(VfxType.DISTORTION, m, seed);
        }

        @Override
        public void render(VfxDraw d, float time) {
            float env = env(time, 6.0F, Math.min(12.0F, ticks * 0.4F));
            float rad = scale * (0.3F + 0.7F * easeOut(time / 10.0F)) * (1.0F + 0.03F * Mth.sin(time * 0.7F));
            // esfera oscura: mas opaca de frente
            d.sphere(d.alphaFlat(), x, y, z, rad, 14, 24, false, (nv, la, lo, c) -> {
                c[0] = 0.0F;
                c[1] = 0.03F;
                c[2] = 0.02F;
                c[3] = (0.55F + 0.4F * nv) * env;
            });
            // borde luminoso (fresnel) con ondulacion
            final float tt = time;
            d.sphere(d.flat(), x, y, z, rad * 1.02F, 14, 24, true, (nv, la, lo, c) -> {
                float rim = (float) Math.pow(1.0F - nv, 2.2F);
                float wave = 0.75F + 0.25F * Mth.sin(la * 7.0F + lo * 3.0F + tt * 0.5F);
                c[0] = cr;
                c[1] = cg;
                c[2] = cb;
                c[3] = rim * wave * 1.5F * env;
            });
            // anillos orbitales inclinados
            VertexConsumer r = d.add(VfxEffect.RING);
            for (int i = 0; i < 2; i++) {
                float tilt = 0.6F + i * 1.1F;
                Vec3 n = new Vec3(Mth.sin(tilt) * Mth.cos(time * 0.05F + i), Mth.cos(tilt), Mth.sin(tilt) * Mth.sin(time * 0.05F + i));
                Vec3[] bs = basis(n);
                d.annulus(r, x, y, z, bs[0], bs[1], rad * 1.5F, rad * 1.3F, 56, cr, cg, cb, 0.8F * env);
            }
            VertexConsumer g = d.add(VfxEffect.GLOW);
            d.billboard(g, x, y, z, rad * 2.4F, 0.0F, cr, cg, cb, 0.12F * env);
        }
    }

    // ================================================================== TIME_FREEZE
    static final class TimeFreeze extends VfxEffect {
        TimeFreeze(VfxS2C m, long seed) {
            super(VfxType.TIME_FREEZE, m, seed);
        }

        @Override
        public void render(VfxDraw d, float time) {
            float env = env(time, 6.0F, Math.min(14.0F, ticks * 0.4F));
            float rad = scale * easeOut(time / 8.0F);
            // esfera gris azulada translucida y borde claro
            d.sphere(d.alphaFlat(), x, y, z, rad, 14, 24, false, (nv, la, lo, c) -> {
                c[0] = 0.45F;
                c[1] = 0.55F;
                c[2] = 0.7F;
                c[3] = (0.12F + 0.1F * (1.0F - nv)) * env;
            });
            d.sphere(d.flat(), x, y, z, rad * 1.01F, 14, 24, true, (nv, la, lo, c) -> {
                float rim = (float) Math.pow(1.0F - nv, 2.5F);
                c[0] = cr;
                c[1] = cg;
                c[2] = cb;
                c[3] = rim * 1.3F * env;
            });
            // reloj de cara a la camara (plano vertical)
            Vec3 toCam = new Vec3(d.cx - x, 0.0D, d.cz - z);
            if (toCam.lengthSqr() < 1.0E-6D) {
                toCam = new Vec3(0, 0, 1);
            }
            toCam = toCam.normalize();
            Vec3 e1 = new Vec3(-toCam.z, 0.0D, toCam.x);
            Vec3 e2 = new Vec3(0, 1, 0);
            float cs = rad * 0.8F;
            Vec3 face = toCam.scale(0.02D);
            VertexConsumer c = d.add(VfxEffect.CLOCK);
            d.plane(c, x + face.x, y + face.y, z + face.z, e1, e2, cs, 0.0F, 0.75F, 0.88F, 1.0F, 1.1F * env);
            // manecillas: la grande avanza a saltos y se detiene; la corta fija
            float stop = Mth.clamp(time / (ticks * 0.6F), 0.0F, 1.0F);
            double minute = Math.floor(time / 3.0F * (1.0F - stop * 0.9F)) * (Math.PI / 6.0D);
            double hour = 0.9D;
            VertexConsumer f = d.flat();
            hand(d, f, x + face.x * 2, y, z + face.z * 2, e1, e2, minute, cs * 0.82F, cs * 0.035F, env);
            hand(d, f, x + face.x * 2, y, z + face.z * 2, e1, e2, hour, cs * 0.52F, cs * 0.055F, env);
            VertexConsumer g = d.add(VfxEffect.GLOW);
            d.billboard(g, x, y, z, cs * 0.12F, 0.0F, 1.0F, 1.0F, 1.0F, env);
            d.billboard(g, x, y, z, rad * 1.8F, 0.0F, cr, cg, cb, 0.1F * env);
            // motas de polvo congeladas
            VertexConsumer s = d.add(VfxEffect.STAR);
            for (int i = 0; i < 14; i++) {
                double a = rnd(i, 1) * Math.PI * 2.0D;
                double b = (rnd(i, 2) - 0.5D) * Math.PI;
                double rr = rad * (0.4D + rnd(i, 3) * 0.55D);
                d.billboard(s, x + Math.cos(a) * Math.cos(b) * rr, y + Math.sin(b) * rr, z + Math.sin(a) * Math.cos(b) * rr,
                        0.14F, time * 0.01F, cr, cg, cb, (0.5F + 0.5F * Mth.sin(time * 0.2F + i)) * env);
            }
        }

        private void hand(VfxDraw d, VertexConsumer f, double x, double y, double z, Vec3 e1, Vec3 e2, double ang,
                          float len, float w, float env) {
            // angulo medido desde las 12 en sentido horario
            Vec3 dir = e2.scale(Math.cos(ang)).add(e1.scale(Math.sin(ang)));
            Vec3 side = e1.scale(Math.cos(ang)).subtract(e2.scale(Math.sin(ang))).scale(w);
            Vec3 tip = dir.scale(len);
            d.fv(f, x - side.x, y - side.y, z - side.z, 1.0F, 1.0F, 1.0F, env);
            d.fv(f, x + side.x, y + side.y, z + side.z, 1.0F, 1.0F, 1.0F, env);
            d.fv(f, x + tip.x + side.x * 0.3D, y + tip.y + side.y * 0.3D, z + tip.z + side.z * 0.3D, 0.6F, 0.8F, 1.0F, env);
            d.fv(f, x + tip.x - side.x * 0.3D, y + tip.y - side.y * 0.3D, z + tip.z - side.z * 0.3D, 0.6F, 0.8F, 1.0F, env);
        }
    }

    // ================================================================== HYPERSPACE
    static final class Hyperspace extends VfxEffect {
        Hyperspace(VfxS2C m, long seed) {
            super(VfxType.HYPERSPACE, m, seed);
        }

        @Override
        public void render(VfxDraw d, float time) {
            float t = Mth.clamp(time / ticks, 0.0F, 1.0F);
            float env = (1.0F - t) * Mth.clamp(time / 1.5F, 0.0F, 1.0F);
            Vec3[] bs = basis(new Vec3(dx, dy, dz));
            Vec3 dir = bs[2];
            VertexConsumer g = d.add(VfxEffect.GLOW);
            VertexConsumer s = d.add(VfxEffect.SPARK);
            VertexConsumer st = d.add(VfxEffect.STAR);
            // destello inicial
            float fl = Mth.clamp(1.0F - time / 10.0F, 0.0F, 1.0F);
            d.billboard(g, x, y, z, scale * (4.0F + 12.0F * fl), 0.0F, cr, cg, cb, 0.9F * fl + 0.1F * env);
            d.billboard(g, x, y, z, scale * 3.0F * (0.3F + fl), 0.0F, 1.0F, 1.0F, 1.0F, fl);
            // aro enorme perpendicular a la direccion
            float ringR = scale * 14.0F * easeOut(t * 1.4F);
            VertexConsumer r = d.add(VfxEffect.RING);
            d.annulus(r, x, y, z, bs[0], bs[1], ringR, ringR * 0.93D, 96, cr, cg, cb, 1.0F * env);
            d.annulus(r, x, y, z, bs[0], bs[1], ringR * 0.9, ringR * 0.84D, 96, 1.0F, 1.0F, 1.0F, 0.6F * env);
            // estelas radiales que se alargan hacia atras
            int n = 56;
            for (int i = 0; i < n; i++) {
                double ph = rnd(i, 1) * Math.PI * 2.0D;
                double rho = scale * (1.5D + rnd(i, 2) * 9.0D);
                double sp = 1.2D + rnd(i, 3) * 2.2D;
                double head = time * sp * scale * 1.5D;
                double tailLen = scale * (3.0D + rnd(i, 4) * 12.0D) * Math.min(1.0D, time / 6.0D);
                Vec3 rad = bs[0].scale(Math.cos(ph)).add(bs[1].scale(Math.sin(ph)));
                Vec3 base = new Vec3(x, y, z).add(rad.scale(rho));
                Vec3 a = base.add(dir.scale(head - tailLen));
                Vec3 b = base.add(dir.scale(head));
                float k = env * (0.6F + rnd(i, 5) * 0.6F);
                d.streak(s, a.x, a.y, a.z, b.x, b.y, b.z, 0.22F * scale + 0.08F, cr, cg, cb, k);
                d.streak(s, (a.x + b.x) * 0.5D, (a.y + b.y) * 0.5D, (a.z + b.z) * 0.5D, b.x, b.y, b.z, 0.09F * scale + 0.04F, 1.0F, 1.0F, 1.0F, k);
            }
            // estrellas que salen disparadas
            for (int i = 0; i < 40; i++) {
                double ph = rnd(i, 11) * Math.PI * 2.0D;
                double th = (rnd(i, 12) - 0.5D) * Math.PI;
                double sp = scale * (2.0D + rnd(i, 13) * 8.0D);
                Vec3 v = bs[0].scale(Math.cos(ph) * Math.cos(th)).add(bs[1].scale(Math.sin(ph) * Math.cos(th))).add(dir.scale(Math.sin(th)));
                double dd = sp * easeOut(t * 1.2F) * 2.0D;
                d.billboard(st, x + v.x * dd, y + v.y * dd, z + v.z * dd, 0.35F * scale + 0.1F, time * 0.1F * (i % 2 == 0 ? 1 : -1),
                        0.8F, 0.9F, 1.0F, env);
            }
        }
    }

    // ================================================================== TRAIL
    static final class Trail extends VfxEffect {
        private static final int MAX = 40;
        private static final int FADE = 18;
        private final List<double[]> pts = new ArrayList<>();
        private int lastAdd = -1;

        Trail(VfxS2C m, long seed) {
            super(VfxType.TRAIL, m, seed);
        }

        @Override
        protected int tail() {
            return FADE;
        }

        @Override
        protected void onFollow(Entity e) {
            // sigue al centro del cuerpo, no a los pies
            this.y = e.getBoundingBox().getCenter().y;
        }

        @Override
        protected void onTick(ClientLevel level) {
            if (age <= ticks) {
                pts.add(new double[]{x, y, z, age});
                if (pts.size() > MAX) {
                    pts.remove(0);
                }
            }
        }

        @Override
        public void render(VfxDraw d, float time) {
            if (pts.size() < 2) {
                return;
            }
            int n = pts.size();
            VfxDraw.Pts outer = new VfxDraw.Pts(n + 1);
            VfxDraw.Pts core = new VfxDraw.Pts(n + 1);
            for (int i = 0; i < n; i++) {
                double[] p = pts.get(i);
                float life = (time - (float) p[3]) / (float) (MAX * 0.8F);
                float k = Mth.clamp(1.0F - life, 0.0F, 1.0F);
                float w = scale * (0.25F + 0.75F * k);
                k = (float) Math.pow(k, 1.5D);
                outer.add(p[0], p[1], p[2], w, k * 1.3F);
                core.add(p[0], p[1], p[2], w * 0.35F, k);
            }
            VertexConsumer t = d.add(VfxEffect.BEAM);
            d.ribbon(t, outer, 0.125F, 0.125F, cr, cg, cb);
            d.ribbon(t, core, 0.125F, 0.125F, 1.0F, 1.0F, 1.0F);
            double[] head = pts.get(n - 1);
            float hk = Mth.clamp((ticks + FADE - time) / FADE, 0.0F, 1.0F);
            d.billboard(d.add(VfxEffect.GLOW), head[0], head[1], head[2], scale * 1.2F, 0.0F, cr, cg, cb, 0.7F * hk);
        }
    }

    // ================================================================== EXPLOSION
    static final class Explosion extends VfxEffect {
        private static final int SPARKS = 36;

        Explosion(VfxS2C m, long seed) {
            super(VfxType.EXPLOSION, m, seed);
        }

        @Override
        public void render(VfxDraw d, float time) {
            float t = Mth.clamp(time / ticks, 0.0F, 1.0F);
            VertexConsumer g = d.add(VfxEffect.GLOW);
            VertexConsumer st = d.add(VfxEffect.STAR);
            VertexConsumer sp = d.add(VfxEffect.SPARK);
            VertexConsumer r = d.add(VfxEffect.RING);
            // humo oscuro que sube (mezcla normal): dibujado primero
            VertexConsumer sm = d.alpha(VfxEffect.SMOKE);
            for (int i = 0; i < 9; i++) {
                float ph = rnd(i, 1) * Mth.TWO_PI;
                float rr = rnd(i, 2) * scale * 0.9F;
                float delay = rnd(i, 3) * 6.0F;
                float lt = Mth.clamp((time - delay) / (ticks - delay), 0.0F, 1.0F);
                if (lt <= 0.0F) {
                    continue;
                }
                double px = x + Math.cos(ph) * rr * (0.4F + lt);
                double pz = z + Math.sin(ph) * rr * (0.4F + lt);
                double py = y + 0.2D + lt * scale * (1.4F + rnd(i, 4));
                float size = scale * (0.7F + lt * 1.3F) * (0.7F + rnd(i, 5) * 0.5F);
                float a = (1.0F - lt) * Mth.clamp(lt * 6.0F, 0.0F, 1.0F) * 0.75F;
                float shade = 0.06F + 0.1F * rnd(i, 6);
                billboardAlpha(d, sm, px, py, pz, size, rnd(i, 7) * 6.0F + time * 0.01F, shade, shade * 1.15F, shade, a);
            }
            // destello inicial blanco
            float fl = Mth.clamp(1.0F - time / 7.0F, 0.0F, 1.0F);
            if (fl > 0.0F) {
                d.billboard(g, x, y, z, scale * 3.4F * (0.5F + 0.5F * easeOut(time / 3.0F)), 0.0F, cr, cg, cb, 0.9F * fl);
                d.billboard(g, x, y, z, scale * 2.0F, 0.0F, 1.0F, 1.0F, 1.0F, fl);
                d.billboard(st, x, y, z, scale * 4.0F, time * 0.03F, 1.0F, 1.0F, 1.0F, fl * 0.9F);
            }
            // bola de fuego que se enfria
            float fb = Mth.clamp(1.0F - time / 16.0F, 0.0F, 1.0F);
            if (fb > 0.0F) {
                d.billboard(g, x, y + scale * 0.2D * t, z, scale * (1.2F + 1.4F * easeOut(time / 10.0F)), 0.0F, cr, cg, cb, 0.8F * fb);
            }
            // aro de choque en el suelo
            float rad = scale * 4.2F * easeOut(Math.min(1.0F, time / 14.0F));
            float rk = Mth.clamp(1.0F - time / 14.0F, 0.0F, 1.0F);
            Vec3 e1 = new Vec3(1, 0, 0);
            Vec3 e2 = new Vec3(0, 0, 1);
            if (rk > 0.0F) {
                d.annulus(r, x, y + 0.05D, z, e1, e2, rad, Math.max(0.0F, rad - scale * 0.5F * rk - 0.1F), 64, cr, cg, cb, 1.0F * rk);
                Vec3 up = new Vec3(0, 1, 0);
                // aro vertical mirando a la camara en horizontal
                Vec3 toCam = new Vec3(d.cx - x, 0.0D, d.cz - z);
                toCam = toCam.lengthSqr() < 1.0E-6D ? new Vec3(0, 0, 1) : toCam.normalize();
                Vec3 side = new Vec3(-toCam.z, 0.0D, toCam.x);
                float rv = scale * 3.0F * easeOut(Math.min(1.0F, time / 10.0F));
                d.annulus(r, x, y, z, side, up, rv, Math.max(0.0F, rv - scale * 0.3F), 48, 1.0F, 1.0F, 1.0F, 0.6F * rk);
            }
            // chispas
            float sk = Mth.clamp(1.0F - time / (ticks * 0.6F), 0.0F, 1.0F);
            if (sk > 0.0F) {
                double drag = 0.9D;
                for (int i = 0; i < SPARKS; i++) {
                    double th = rnd(i, 21) * Math.PI * 2.0D;
                    double cz2 = rnd(i, 22) * 1.4D - 0.4D;
                    double sr = Math.sqrt(Math.max(0.0D, 1.0D - cz2 * cz2));
                    double spd = scale * (0.5D + rnd(i, 23) * 0.9D);
                    double vx = Math.cos(th) * sr * spd;
                    double vy = cz2 * spd;
                    double vz = Math.sin(th) * sr * spd;
                    double k = (1.0D - Math.pow(drag, time)) / -Math.log(drag);
                    double tb = Math.max(0.0F, time - 1.5F);
                    double kb = (1.0D - Math.pow(drag, tb)) / -Math.log(drag);
                    double px = x + vx * k;
                    double py = y + vy * k - 0.012D * time * time;
                    double pz = z + vz * k;
                    double bx = x + vx * kb;
                    double by = y + vy * kb - 0.012D * tb * tb;
                    double bz = z + vz * kb;
                    d.streak(sp, bx, by, bz, px, py, pz, 0.2F, cr, cg, cb, sk * 1.1F);
                    d.streak(sp, (bx + px) * 0.5D, (by + py) * 0.5D, (bz + pz) * 0.5D, px, py, pz, 0.09F, 1.0F, 1.0F, 1.0F, sk);
                }
            }
        }

        /** Billboard con mezcla normal (humo). */
        private void billboardAlpha(VfxDraw d, VertexConsumer vc, double px, double py, double pz, float half, float roll,
                                    float r, float g, float b, float a) {
            float c = Mth.cos(roll) * half;
            float s = Mth.sin(roll) * half;
            float ax = d.lx * c + d.ux * s;
            float ay = d.ly * c + d.uy * s;
            float az = d.lz * c + d.uz * s;
            float bx = d.ux * c - d.lx * s;
            float by = d.uy * c - d.ly * s;
            float bz = d.uz * c - d.lz * s;
            d.ta(vc, px + ax + bx, py + ay + by, pz + az + bz, 0.0F, 0.0F, r, g, b, a);
            d.ta(vc, px - ax + bx, py - ay + by, pz - az + bz, 1.0F, 0.0F, r, g, b, a);
            d.ta(vc, px - ax - bx, py - ay - by, pz - az - bz, 1.0F, 1.0F, r, g, b, a);
            d.ta(vc, px + ax - bx, py + ay - by, pz + az - bz, 0.0F, 1.0F, r, g, b, a);
        }
    }

    // ================================================================== MUZZLE
    static final class Muzzle extends VfxEffect {
        Muzzle(VfxS2C m, long seed) {
            super(VfxType.MUZZLE, m, seed);
        }

        @Override
        public void render(VfxDraw d, float time) {
            float t = Mth.clamp(time / ticks, 0.0F, 1.0F);
            float env = (1.0F - t) * (1.0F - t);
            Vec3 dir = new Vec3(dx, dy, dz);
            dir = dir.lengthSqr() < 1.0E-9D ? new Vec3(0, 0, 1) : dir.normalize();
            Vec3 a = new Vec3(x, y, z);
            float len = scale * (1.4F + 0.8F * easeOut(t * 2.0F));
            Vec3 b = a.add(dir.scale(len));
            VertexConsumer f = d.flat();
            // cono: punta en b, ancho en a
            d.prism(f, a, b, scale * 0.28D, 0.0D, 8, cr, cg, cb, 0.8F * env, 0.0F);
            d.prism(f, a, a.add(dir.scale(len * 0.7D)), scale * 0.12D, 0.0D, 8, 1.0F, 1.0F, 1.0F, 1.0F * env, 0.0F);
            VertexConsumer g = d.add(VfxEffect.GLOW);
            d.billboard(g, a.x, a.y, a.z, scale * 1.1F, 0.0F, cr, cg, cb, 0.9F * env);
            d.billboard(g, a.x, a.y, a.z, scale * 0.5F, 0.0F, 1.0F, 1.0F, 1.0F, env);
            VertexConsumer st = d.add(VfxEffect.STAR);
            d.billboard(st, a.x, a.y, a.z, scale * 1.4F, time * 0.4F, 1.0F, 1.0F, 1.0F, 0.9F * env);
        }
    }
}
