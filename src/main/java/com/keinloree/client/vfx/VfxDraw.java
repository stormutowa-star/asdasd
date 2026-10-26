package com.keinloree.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Ayudante de dibujo del motor de VFX. Todo se dibuja en coordenadas de mundo restando la posicion de la camara
 * (la matriz de vista ya la aplica el shader), sin PoseStack. El brillo aditivo se logra multiplicando el color
 * por la intensidad {@code k} (alfa fijo en 1).
 */
public final class VfxDraw {
    public final MultiBufferSource.BufferSource src;
    public final double cx;
    public final double cy;
    public final double cz;
    public final float lx;
    public final float ly;
    public final float lz;
    public final float ux;
    public final float uy;
    public final float uz;

    VfxDraw(MultiBufferSource.BufferSource src, Camera camera) {
        this.src = src;
        Vec3 p = camera.getPosition();
        this.cx = p.x;
        this.cy = p.y;
        this.cz = p.z;
        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();
        this.lx = left.x;
        this.ly = left.y;
        this.lz = left.z;
        this.ux = up.x;
        this.uy = up.y;
        this.uz = up.z;
    }

    // ------------------------------------------------------------------ buffers

    public VertexConsumer add(ResourceLocation tex) {
        return src.getBuffer(ModRenderTypes.glowAdditive(tex));
    }

    public VertexConsumer flat() {
        return src.getBuffer(ModRenderTypes.beamAdditive());
    }

    public VertexConsumer alpha(ResourceLocation tex) {
        return src.getBuffer(ModRenderTypes.glowAlpha(tex));
    }

    public VertexConsumer alphaFlat() {
        return src.getBuffer(ModRenderTypes.alphaFlat());
    }

    public VertexConsumer buf(RenderType type) {
        return src.getBuffer(type);
    }

    // ------------------------------------------------------------------ vertices

    /** Vertice con textura (aditivo: color premultiplicado por k, alfa 1). */
    public void tv(VertexConsumer vc, double x, double y, double z, float u, float v, float r, float g, float b, float k) {
        vc.addVertex((float) (x - cx), (float) (y - cy), (float) (z - cz)).setUv(u, v)
                .setColor(Mth.clamp(r * k, 0.0F, 1.0F), Mth.clamp(g * k, 0.0F, 1.0F), Mth.clamp(b * k, 0.0F, 1.0F), 1.0F);
    }

    /** Vertice con textura y alfa propio (mezcla normal). */
    public void ta(VertexConsumer vc, double x, double y, double z, float u, float v, float r, float g, float b, float a) {
        vc.addVertex((float) (x - cx), (float) (y - cy), (float) (z - cz)).setUv(u, v).setColor(r, g, b, Mth.clamp(a, 0.0F, 1.0F));
    }

    /** Vertice sin textura aditivo. */
    public void fv(VertexConsumer vc, double x, double y, double z, float r, float g, float b, float k) {
        vc.addVertex((float) (x - cx), (float) (y - cy), (float) (z - cz))
                .setColor(Mth.clamp(r * k, 0.0F, 1.0F), Mth.clamp(g * k, 0.0F, 1.0F), Mth.clamp(b * k, 0.0F, 1.0F), 1.0F);
    }

    /** Vertice sin textura con alfa (mezcla normal). */
    public void av(VertexConsumer vc, double x, double y, double z, float r, float g, float b, float a) {
        vc.addVertex((float) (x - cx), (float) (y - cy), (float) (z - cz)).setColor(r, g, b, Mth.clamp(a, 0.0F, 1.0F));
    }

    // ------------------------------------------------------------------ primitivas con textura (aditivas)

    /** Billboard mirando a la camara, con giro (radianes) opcional. UV completa o un trozo del atlas. */
    public void billboard(VertexConsumer vc, double x, double y, double z, float half, float roll,
                          float u0, float v0, float u1, float v1, float r, float g, float b, float k) {
        float c = Mth.cos(roll) * half;
        float s = Mth.sin(roll) * half;
        // ejes del billboard (izquierda y arriba de la camara, girados)
        float ax = lx * c + ux * s;
        float ay = ly * c + uy * s;
        float az = lz * c + uz * s;
        float bx = ux * c - lx * s;
        float by = uy * c - ly * s;
        float bz = uz * c - lz * s;
        tv(vc, x + ax + bx, y + ay + by, z + az + bz, u0, v0, r, g, b, k);
        tv(vc, x - ax + bx, y - ay + by, z - az + bz, u1, v0, r, g, b, k);
        tv(vc, x - ax - bx, y - ay - by, z - az - bz, u1, v1, r, g, b, k);
        tv(vc, x + ax - bx, y + ay - by, z + az - bz, u0, v1, r, g, b, k);
    }

    public void billboard(VertexConsumer vc, double x, double y, double z, float half, float roll,
                          float r, float g, float b, float k) {
        billboard(vc, x, y, z, half, roll, 0.0F, 0.0F, 1.0F, 1.0F, r, g, b, k);
    }

    /** Cuadrado en el plano (e1, e2) centrado en c, de lado 2*half y girado rot radianes. */
    public void plane(VertexConsumer vc, double x, double y, double z, Vec3 e1, Vec3 e2, float half, float rot,
                      float r, float g, float b, float k) {
        double cs = Math.cos(rot) * half;
        double sn = Math.sin(rot) * half;
        Vec3 a = e1.scale(cs).add(e2.scale(sn));
        Vec3 bb = e2.scale(cs).subtract(e1.scale(sn));
        tv(vc, x + a.x + bb.x, y + a.y + bb.y, z + a.z + bb.z, 0.0F, 0.0F, r, g, b, k);
        tv(vc, x - a.x + bb.x, y - a.y + bb.y, z - a.z + bb.z, 1.0F, 0.0F, r, g, b, k);
        tv(vc, x - a.x - bb.x, y - a.y - bb.y, z - a.z - bb.z, 1.0F, 1.0F, r, g, b, k);
        tv(vc, x + a.x - bb.x, y + a.y - bb.y, z + a.z - bb.z, 0.0F, 1.0F, r, g, b, k);
    }

    /**
     * Anillo plano (corona) en el plano (e1, e2); la textura ring.png se muestrea a lo largo de su radio
     * (rho 0.70 interior a 1.0 exterior, con el pico en 0.86).
     */
    public void annulus(VertexConsumer vc, double x, double y, double z, Vec3 e1, Vec3 e2, double rOut, double rIn,
                        int segs, float r, float g, float b, float k) {
        for (int i = 0; i < segs; i++) {
            double a0 = Math.PI * 2.0D * i / segs;
            double a1 = Math.PI * 2.0D * (i + 1) / segs;
            double c0 = Math.cos(a0);
            double s0 = Math.sin(a0);
            double c1 = Math.cos(a1);
            double s1 = Math.sin(a1);
            float uo = 1.0F;
            float ui = 0.85F;
            ringVertex(vc, x, y, z, e1, e2, c0, s0, rOut, uo, r, g, b, k);
            ringVertex(vc, x, y, z, e1, e2, c1, s1, rOut, uo, r, g, b, k);
            ringVertex(vc, x, y, z, e1, e2, c1, s1, rIn, ui, r, g, b, k);
            ringVertex(vc, x, y, z, e1, e2, c0, s0, rIn, ui, r, g, b, k);
        }
    }

    private void ringVertex(VertexConsumer vc, double x, double y, double z, Vec3 e1, Vec3 e2, double c, double s,
                            double rad, float u, float r, float g, float b, float k) {
        // u = 0.5 + 0.5 * rho; el interior va de rho 0.70 (u 0.85) al exterior rho 1.0 (u 1.0)
        double px = x + (e1.x * c + e2.x * s) * rad;
        double py = y + (e1.y * c + e2.y * s) * rad;
        double pz = z + (e1.z * c + e2.z * s) * rad;
        tv(vc, px, py, pz, u, 0.5F, r, g, b, k);
    }

    // ------------------------------------------------------------------ cintas

    /** Punto de una cinta: posicion, ancho total e intensidad. */
    public static final class Pts {
        public final double[] x;
        public final double[] y;
        public final double[] z;
        public final float[] w;
        public final float[] k;
        public int n;

        public Pts(int max) {
            x = new double[max];
            y = new double[max];
            z = new double[max];
            w = new float[max];
            k = new float[max];
        }

        public void add(double px, double py, double pz, float width, float intensity) {
            x[n] = px;
            y[n] = py;
            z[n] = pz;
            w[n] = width;
            k[n] = intensity;
            n++;
        }
    }

    /**
     * Cinta que siempre mira a la camara a lo largo de una polilinea. u0..u1 se reparte a lo largo; v cruza el ancho.
     * Aditiva con textura (usa un buffer aditivo de textura dado).
     */
    public void ribbon(VertexConsumer vc, Pts p, float u0, float u1, float r, float g, float b) {
        if (p.n < 2) {
            return;
        }
        double[] sx = new double[p.n];
        double[] sy = new double[p.n];
        double[] sz = new double[p.n];
        for (int i = 0; i < p.n; i++) {
            int a = Math.max(0, i - 1);
            int c = Math.min(p.n - 1, i + 1);
            double tx = p.x[c] - p.x[a];
            double ty = p.y[c] - p.y[a];
            double tz = p.z[c] - p.z[a];
            double vx = p.x[i] - cx;
            double vy = p.y[i] - cy;
            double vz = p.z[i] - cz;
            // lado = tangente x vista
            double nx = ty * vz - tz * vy;
            double ny = tz * vx - tx * vz;
            double nz = tx * vy - ty * vx;
            double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len < 1.0E-9D) {
                nx = lx;
                ny = ly;
                nz = lz;
                len = 1.0D;
            }
            double f = p.w[i] * 0.5D / len;
            sx[i] = nx * f;
            sy[i] = ny * f;
            sz[i] = nz * f;
        }
        for (int i = 0; i < p.n - 1; i++) {
            float ua = Mth.lerp(i / (float) (p.n - 1), u0, u1);
            float ub = Mth.lerp((i + 1) / (float) (p.n - 1), u0, u1);
            tv(vc, p.x[i] + sx[i], p.y[i] + sy[i], p.z[i] + sz[i], ua, 0.0F, r, g, b, p.k[i]);
            tv(vc, p.x[i] - sx[i], p.y[i] - sy[i], p.z[i] - sz[i], ua, 1.0F, r, g, b, p.k[i]);
            tv(vc, p.x[i + 1] - sx[i + 1], p.y[i + 1] - sy[i + 1], p.z[i + 1] - sz[i + 1], ub, 1.0F, r, g, b, p.k[i + 1]);
            tv(vc, p.x[i + 1] + sx[i + 1], p.y[i + 1] + sy[i + 1], p.z[i + 1] + sz[i + 1], ub, 0.0F, r, g, b, p.k[i + 1]);
        }
    }

    /** Segmento con textura (chispa alargada) de a a b con ancho w, mirando a la camara. */
    public void streak(VertexConsumer vc, double ax, double ay, double az, double bx, double by, double bz, float w,
                       float r, float g, float b, float k) {
        Pts p = new Pts(2);
        p.add(ax, ay, az, w, k);
        p.add(bx, by, bz, w, k);
        ribbon(vc, p, 0.0F, 1.0F, r, g, b);
    }

    // ------------------------------------------------------------------ volumenes sin textura

    /** Prisma de n lados alrededor del eje A-B con radios ra (en A) y rb (en B). Aditivo sin textura o con alfa. */
    public void prism(VertexConsumer vc, Vec3 a, Vec3 b, double ra, double rb, int sides, float r, float g, float bl,
                      float ka, float kb) {
        Vec3 axis = b.subtract(a);
        double len = axis.length();
        if (len < 1.0E-6D) {
            return;
        }
        Vec3 dir = axis.scale(1.0D / len);
        Vec3 h = Math.abs(dir.y) < 0.99D ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 e1 = dir.cross(h).normalize();
        Vec3 e2 = dir.cross(e1);
        for (int i = 0; i < sides; i++) {
            double a0 = Math.PI * 2.0D * i / sides;
            double a1 = Math.PI * 2.0D * (i + 1) / sides;
            Vec3 o0 = e1.scale(Math.cos(a0)).add(e2.scale(Math.sin(a0)));
            Vec3 o1 = e1.scale(Math.cos(a1)).add(e2.scale(Math.sin(a1)));
            fv(vc, a.x + o0.x * ra, a.y + o0.y * ra, a.z + o0.z * ra, r, g, bl, ka);
            fv(vc, a.x + o1.x * ra, a.y + o1.y * ra, a.z + o1.z * ra, r, g, bl, ka);
            fv(vc, b.x + o1.x * rb, b.y + o1.y * rb, b.z + o1.z * rb, r, g, bl, kb);
            fv(vc, b.x + o0.x * rb, b.y + o0.y * rb, b.z + o0.z * rb, r, g, bl, kb);
        }
    }

    /** Sombreado de esfera: rellena rgba segun el producto normal·vista (0 en el borde, 1 de frente). */
    public interface SphereShade {
        void shade(float nDotV, float lat, float lon, float[] rgba);
    }

    /** Esfera de lat x lon segmentos; la mezcla la decide el buffer (aditivo: usa rgb*a; normal: usa alfa). */
    public void sphere(VertexConsumer vc, double x, double y, double z, double rad, int latSeg, int lonSeg,
                       boolean additive, SphereShade shade) {
        float[] c = new float[4];
        for (int i = 0; i < latSeg; i++) {
            double la0 = Math.PI * (-0.5D + (double) i / latSeg);
            double la1 = Math.PI * (-0.5D + (double) (i + 1) / latSeg);
            for (int j = 0; j < lonSeg; j++) {
                double lo0 = Math.PI * 2.0D * j / lonSeg;
                double lo1 = Math.PI * 2.0D * (j + 1) / lonSeg;
                sphereVertex(vc, x, y, z, rad, la0, lo0, additive, shade, c);
                sphereVertex(vc, x, y, z, rad, la0, lo1, additive, shade, c);
                sphereVertex(vc, x, y, z, rad, la1, lo1, additive, shade, c);
                sphereVertex(vc, x, y, z, rad, la1, lo0, additive, shade, c);
            }
        }
    }

    private void sphereVertex(VertexConsumer vc, double x, double y, double z, double rad, double lat, double lon,
                              boolean additive, SphereShade shade, float[] c) {
        double nx = Math.cos(lat) * Math.cos(lon);
        double ny = Math.sin(lat);
        double nz = Math.cos(lat) * Math.sin(lon);
        double px = x + nx * rad;
        double py = y + ny * rad;
        double pz = z + nz * rad;
        double vx = cx - px;
        double vy = cy - py;
        double vz = cz - pz;
        double vl = Math.sqrt(vx * vx + vy * vy + vz * vz);
        float nv = vl < 1.0E-9D ? 1.0F : (float) Math.abs((nx * vx + ny * vy + nz * vz) / vl);
        shade.shade(nv, (float) lat, (float) lon, c);
        if (additive) {
            fv(vc, px, py, pz, c[0], c[1], c[2], c[3]);
        } else {
            av(vc, px, py, pz, c[0], c[1], c[2], c[3]);
        }
    }

    /** Distancia de un punto a la camara. */
    public double dist(double x, double y, double z) {
        double dx = x - cx;
        double dy = y - cy;
        double dz = z - cz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
