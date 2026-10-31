package com.ricardo.retorcido.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricardo.retorcido.RetorcidoEntity;
import com.ricardo.retorcido.RetorcidoMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * El Retorcido: criatura encorvada de cuatro brazos.
 *
 * Jerarquia: body -> pelvis -> s1 -> s2 -> s3 -> s4 -> n1 -> n2 -> n3 -> headPivot -> head -> jaw.
 * Cada segmento de la columna gira un poco mas en X que el anterior (ver REL_DEG), asi el lomo
 * dibuja un arco hacia delante y la cabeza cuelga baja y adelantada. Los brazos y la cabeza
 * compensan la rotacion acumulada (CUM) para que sus animaciones se hagan en ejes del mundo.
 *
 * Las regiones UV de abajo ({u, v, ancho, alto, fondo}) las lee tools/make_textures.py:
 * el prefijo del nombre elige el material (S piel, B hueso, D hueco oscuro, E ojo).
 * Todos los cubos que comparten una region caben dentro de sus dimensiones.
 */
public class RetorcidoModel extends EntityModel<RetorcidoEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(RetorcidoMod.MODID, "retorcido"), "main");

    // ===== REGIONES UV (u, v, w, h, d) - leidas por tools/make_textures.py =====
    //@UV-BEGIN
    private static final int[] UV_S_ARM_F = {0, 0, 2, 10, 2};
    private static final int[] UV_S_ARM_U = {9, 0, 2, 9, 2};
    private static final int[] UV_S_PELVIS = {18, 0, 7, 5, 5};
    private static final int[] UV_S_SPINE_A = {43, 0, 5, 6, 4};
    private static final int[] UV_S_CRANIUM = {62, 0, 5, 4, 6};
    private static final int[] UV_S_THIGH = {85, 0, 3, 7, 3};
    private static final int[] UV_S_SHIN = {98, 0, 2, 8, 2};
    private static final int[] UV_S_NECK = {107, 0, 3, 6, 3};
    private static final int[] UV_S_JAW = {0, 13, 3, 2, 7};
    private static final int[] UV_S_SPINE_B = {21, 13, 4, 5, 3};
    private static final int[] UV_S_FOOT = {36, 13, 3, 6, 2};
    private static final int[] UV_S_BELLY = {47, 13, 4, 5, 2};
    private static final int[] UV_S_SNOUT = {60, 13, 3, 3, 4};
    private static final int[] UV_B_THIN = {75, 13, 1, 6, 1};
    private static final int[] UV_S_TAIL = {80, 13, 2, 2, 4};
    private static final int[] UV_S_LUMP = {93, 13, 3, 3, 3};
    private static final int[] UV_S_MUSCLE = {106, 13, 2, 4, 2};
    private static final int[] UV_B_VERT = {115, 13, 3, 3, 3};
    private static final int[] UV_B_HIP = {0, 23, 2, 4, 2};
    private static final int[] UV_S_HAND = {9, 23, 3, 3, 2};
    private static final int[] UV_B_SPIKE = {20, 23, 2, 3, 2};
    private static final int[] UV_B_TAILTIP = {29, 23, 1, 1, 4};
    private static final int[] UV_S_SCAP = {40, 23, 4, 1, 3};
    private static final int[] UV_B_JOINT = {55, 23, 2, 2, 2};
    private static final int[] UV_D_SOCKET = {64, 23, 3, 3, 1};
    private static final int[] UV_S_BROW = {73, 23, 6, 1, 2};
    private static final int[] UV_E_EYE = {90, 23, 2, 2, 1};
    private static final int[] UV_B_RIB = {97, 23, 5, 1, 1};
    //@UV-END

    // ===== COLUMNA CURVA =====
    private static final String[] SEG = {"pelvis", "s1", "s2", "s3", "s4", "n1", "n2", "n3"};
    /** Altura de cada segmento (pixeles). */
    private static final float[] LEN = {4, 4, 5, 5, 4, 5, 5, 4};
    /** Rotacion X de cada segmento respecto al anterior (grados): crece hacia delante. */
    private static final float[] REL_DEG = {24, 14, 15, 16, 17, 18, 18, 18};
    /** Rotacion X acumulada (radianes) de cada segmento respecto al mundo. */
    private static final float[] CUM = cumulative();
    private static final float PELVIS_Y = 12.0F;
    private static final float PELVIS_Z = 15.0F;
    /** Inclinacion del craneo respecto al mundo (rad): cara mirando adelante y algo hacia abajo. */
    private static final float HEAD_TILT = rad(38);

    /** Costillas: {segmento, numero de pares}. */
    private static final int[][] RIBS = {{1, 1}, {2, 2}, {3, 2}};
    /** Indices de los segmentos donde se enganchan los brazos. */
    private static final int UPPER_SEG = 4;
    private static final int LOWER_SEG = 2;

    private static float rad(float deg) {
        return deg * (float) Math.PI / 180.0F;
    }

    private static float[] cumulative() {
        float[] out = new float[REL_DEG.length];
        float sum = 0;
        for (int i = 0; i < out.length; i++) {
            sum += rad(REL_DEG[i]);
            out[i] = sum;
        }
        return out;
    }

    // ===== AYUDAS PARA CONSTRUIR =====
    private static final class Cubes {
        private final CubeListBuilder builder = CubeListBuilder.create();

        Cubes box(int[] uv, float x, float y, float z, float w, float h, float d) {
            builder.texOffs(uv[0], uv[1]).addBox(x, y, z, w, h, d);
            return this;
        }

        /** Igual, pero con "grow" (negativo = adelgaza el cubo sin tocar las UV). */
        Cubes box(int[] uv, float x, float y, float z, float w, float h, float d, float grow) {
            builder.texOffs(uv[0], uv[1]).addBox(x, y, z, w, h, d, new CubeDeformation(grow));
            return this;
        }
    }

    private static Cubes cubes() {
        return new Cubes();
    }

    private static PartDefinition add(PartDefinition parent, String name, Cubes c,
                                      float x, float y, float z, float rx, float ry, float rz) {
        return parent.addOrReplaceChild(name, c.builder, PartPose.offsetAndRotation(x, y, z, rx, ry, rz));
    }

    // ===== CAPA =====
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        // ---- columna: pelvis alta detras, cada segmento hijo del anterior ----
        PartDefinition[] seg = new PartDefinition[SEG.length];
        seg[0] = add(body, "pelvis", cubes()
                        .box(UV_S_PELVIS, -3.5F, -4.5F, -2.5F, 7, 5, 5)
                        .box(UV_B_VERT, -1.5F, -3.5F, 2.0F, 3, 3, 3),
                0, -PELVIS_Y, PELVIS_Z, CUM[0], 0, 0);
        seg[1] = add(seg[0], SEG[1], cubes()
                        .box(UV_S_SPINE_A, -2.5F, -4.5F, -2.0F, 5, 5, 4)
                        .box(UV_B_VERT, -1.5F, -3.5F, 1.5F, 3, 3, 3),
                0, -LEN[0], 0, rad(REL_DEG[1]), 0, 0);
        seg[2] = add(seg[1], SEG[2], cubes()
                        .box(UV_S_SPINE_A, -2.5F, -5.5F, -2.0F, 5, 6, 4)
                        .box(UV_B_VERT, -1.5F, -4.5F, 1.5F, 3, 3, 3),
                0, -LEN[1], 0, rad(REL_DEG[2]), 0, 0);
        seg[3] = add(seg[2], SEG[3], cubes()
                        .box(UV_S_SPINE_A, -2.5F, -5.5F, -2.0F, 5, 6, 4)
                        .box(UV_B_VERT, -1.5F, -4.0F, 1.5F, 3, 3, 3),
                0, -LEN[2], 0, rad(REL_DEG[3]), 0, 0);
        seg[4] = add(seg[3], SEG[4], cubes()
                        .box(UV_S_SPINE_B, -2.0F, -4.5F, -1.5F, 4, 5, 3)
                        .box(UV_B_VERT, -1.5F, -3.5F, 1.0F, 3, 3, 3),
                0, -LEN[3], 0, rad(REL_DEG[4]), 0, 0);
        seg[5] = add(seg[4], SEG[5], cubes()
                        .box(UV_S_NECK, -1.5F, -5.5F, -1.5F, 3, 6, 3)
                        .box(UV_B_VERT, -1.0F, -4.0F, 1.0F, 2, 2, 2),
                0, -LEN[4], 0, rad(REL_DEG[5]), 0, 0);
        seg[6] = add(seg[5], SEG[6], cubes()
                        .box(UV_S_NECK, -1.5F, -5.5F, -1.5F, 3, 6, 3)
                        .box(UV_B_VERT, -1.0F, -4.0F, 1.0F, 2, 2, 2),
                0, -LEN[5], 0, rad(REL_DEG[6]), 0, 0);
        seg[7] = add(seg[6], SEG[7], cubes()
                        .box(UV_S_NECK, -1.5F, -4.5F, -1.5F, 3, 5, 3)
                        .box(UV_B_VERT, -1.0F, -3.5F, 1.0F, 2, 2, 2),
                0, -LEN[6], 0, rad(REL_DEG[7]), 0, 0);

        // ---- espinas dorsales afiladas (huesos finos rotados, cada una con su punta curvada) ----
        spike(seg[1], 1, 0.4F, 3.0F, 35, 1.0F);
        spike(seg[2], 2, -0.5F, 3.5F, 28, 1.3F);
        spike(seg[3], 3, 0.3F, 3.5F, 40, 1.5F);
        spike(seg[4], 4, -0.4F, 3.0F, 22, 1.2F);
        spike(seg[5], 5, 0.0F, 3.5F, 45, 0.9F);
        spike(seg[6], 6, 0.3F, 3.5F, 30, 0.8F);
        spike(seg[7], 7, -0.2F, 2.5F, 50, 0.7F);
        // espina de la pelvis, mas corta y torcida
        PartDefinition pelvisSpike = add(seg[0], "spike", cubes().box(UV_B_SPIKE, -1, -3, -1, 2, 3, 2),
                0.8F, -2.5F, 2.5F, -(CUM[0] + rad(55)), 0, 0.25F);
        add(pelvisSpike, "tip", cubes().box(UV_B_THIN, -0.5F, -4, -0.5F, 1, 4, 1, -0.1F), 0, -3, 0, rad(-25), 0, 0);

        // ---- cola ósea: tres vertebras colgando de la pelvis ----
        PartDefinition tail1 = add(seg[0], "tail1", cubes().box(UV_S_TAIL, -1, -1, 0, 2, 2, 4),
                0, -1.5F, 2.0F, rad(-62), rad(8), 0);
        PartDefinition tail2 = add(tail1, "tail2", cubes().box(UV_S_TAIL, -1, -1, 0, 2, 2, 4, -0.2F),
                0, 0, 3.6F, rad(-28), rad(-12), 0);
        add(tail2, "tail3", cubes().box(UV_B_TAILTIP, -0.5F, -0.5F, 0, 1, 1, 4),
                0, 0, 3.4F, rad(-32), rad(15), 0);

        // ---- alas iliacas (huesos de la cadera, asimetricos) ----
        add(seg[0], "hipR", cubes().box(UV_B_HIP, -1, -3, -1, 2, 4, 2), -3.8F, -2.5F, 0.5F, rad(10), 0, rad(-32));
        add(seg[0], "hipL", cubes().box(UV_B_HIP, -1, -3, -1, 2, 4, 2, -0.2F), 3.8F, -2.5F, -0.5F, rad(-15), 0, rad(24));

        // ---- vientre hundido, torcido ----
        add(seg[1], "belly", cubes().box(UV_S_BELLY, -2, -4.5F, -1.5F, 4, 5, 2), 0, 0, -2.0F, rad(4), rad(6), 0);
        add(seg[2], "belly", cubes().box(UV_S_BELLY, -2, -4.5F, -1.5F, 4, 5, 2, -0.2F), 0.3F, -0.5F, -2.0F, rad(-3), rad(-9), 0);

        // ---- costillas finas y curvadas (3 huesos encadenados por costilla) ----
        for (int[] spec : RIBS) {
            int i = spec[0];
            int pairs = spec[1];
            for (int k = 0; k < pairs; k++) {
                float y = -LEN[i] + 1.0F + k * 2.4F;
                float scale = (i == 2) ? 1.0F : 0.85F;
                rib(seg[i], "rib" + k + "R", 1.0F, y, k, scale);
                rib(seg[i], "rib" + k + "L", -1.0F, y + 0.3F, k + 1, scale * 0.9F);
            }
        }

        // ---- placas escapulares ----
        add(seg[UPPER_SEG], "scapR", cubes().box(UV_S_SCAP, -2, -0.5F, -1.5F, 4, 1, 3),
                3.6F, -3.8F, 0.8F, rad(14), rad(10), rad(-48));
        add(seg[UPPER_SEG], "scapL", cubes().box(UV_S_SCAP, -2, -0.5F, -1.5F, 4, 1, 3, -0.3F),
                -3.4F, -3.2F, 1.0F, rad(-20), rad(-16), rad(62));

        // ---- cuatro brazos: dos pares, cada brazo con hombro / codo / mano ----
        // pares superiores: largos y finos, casi tocan el suelo (el izquierdo mas largo y con 4 dedos)
        arm(seg[UPPER_SEG], "uArmR", 1.0F, UPPER_SEG, 3.8F, -2.5F, -0.5F, rad(-12), 8, 9, 3, 0.5F);
        arm(seg[UPPER_SEG], "uArmL", -1.0F, UPPER_SEG, 3.8F, -2.0F, -0.5F, rad(-4), 9, 9, 4, 0.7F);
        // pares inferiores: mas cortos, salen del costillar
        arm(seg[LOWER_SEG], "lArmR", 1.0F, LOWER_SEG, 2.9F, -2.5F, -1.5F, rad(-8), 5, 6, 3, 0.9F);
        arm(seg[LOWER_SEG], "lArmL", -1.0F, LOWER_SEG, 2.9F, -3.0F, -1.5F, rad(-18), 6, 5, 3, 0.4F);

        // ---- cabeza ----
        PartDefinition headPivot = add(seg[7], "headPivot", cubes(), 0, -LEN[7], 0, -CUM[7], 0, 0);
        head(headPivot);

        // ---- patas digitigradas: muslo -> espinilla invertida -> pie, en posicion de acecho ----
        leg(body, "R", 1.0F, 3.9F, -14.3F, 12.6F, -0.70F, 1.35F, -2.05F);
        leg(body, "L", -1.0F, 4.1F, -14.6F, 12.2F, -0.60F, 1.25F, -1.95F);

        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Espina dorsal: base + punta curvada hacia atras. Apunta hacia arriba/atras en el mundo. */
    private static void spike(PartDefinition segment, int i, float x, float y, float backDeg, float size) {
        float rx = -(CUM[i] + rad(backDeg));
        PartDefinition base = add(segment, "spike", cubes().box(UV_B_SPIKE, -1, -3, -1, 2, 3, 2, size > 1.0F ? 0.0F : -0.25F),
                x, y - LEN[i] * 0.5F - 1.0F, 2.0F, rx, rad(x * 10), 0);
        add(base, "tip", cubes().box(UV_B_THIN, -0.5F, -(size > 1.0F ? 6 : 4), -0.5F, 1, (size > 1.0F ? 6 : 4), 1, -0.1F),
                0, -3, 0, rad(-28), 0, rad(x * 18));
    }

    /** Costilla de 3 huesos encadenados que se curvan alrededor del torso hacia el vientre. */
    private static void rib(PartDefinition segment, String name, float side, float y, int variant, float scale) {
        float g = -(1.0F - scale) * 0.5F; // costillas mas finas hacia los extremos del costillar
        PartDefinition a = add(segment, name, side > 0
                        ? cubes().box(UV_B_RIB, 0, -0.5F, -0.5F, 3, 1, 1, g)
                        : cubes().box(UV_B_RIB, -3, -0.5F, -0.5F, 3, 1, 1, g),
                side * 1.6F, y, 0.2F - variant * 0.3F, 0, side * rad(25 + variant * 6), side * rad(14));
        PartDefinition b = add(a, "b", side > 0
                        ? cubes().box(UV_B_RIB, 0, -0.5F, -0.5F, 3, 1, 1, g - 0.05F)
                        : cubes().box(UV_B_RIB, -3, -0.5F, -0.5F, 3, 1, 1, g - 0.05F),
                side * 3.0F, 0, 0, 0, side * rad(32), side * rad(10));
        add(b, "c", side > 0
                        ? cubes().box(UV_B_RIB, 0, -0.5F, -0.5F, 2, 1, 1, g - 0.12F)
                        : cubes().box(UV_B_RIB, -2, -0.5F, -0.5F, 2, 1, 1, g - 0.12F),
                side * 3.0F, 0, 0, 0, side * rad(40), side * rad(28));
    }

    /** Un brazo de 3 segmentos (hombro, antebrazo, mano) con codo articulado y dedos-garra. */
    private static void arm(PartDefinition segment, String name, float side, int segIndex,
                            float x, float y, float z, float restX, int lenUpper, int lenFore, int fingers, float splay) {
        PartDefinition shoulder = add(segment, name, cubes()
                        .box(UV_B_JOINT, -1, -1, -1, 2, 2, 2, 0.3F)
                        .box(UV_S_ARM_U, -1, 0, -1, 2, lenUpper, 2, -0.15F),
                side * x, y, z, restX - CUM[segIndex], -side * splay * 0.25F, 0);
        PartDefinition elbow = add(shoulder, "elbow", cubes()
                        .box(UV_B_JOINT, -1, -1, -1, 2, 2, 2, 0.15F)
                        .box(UV_S_ARM_F, -1, 0, -1, 2, lenFore, 2, -0.2F),
                0, lenUpper, 0, rad(-34), 0, 0);
        // hueso del codo que sobresale hacia atras
        add(elbow, "olecranon", cubes().box(UV_B_THIN, -0.5F, -3, -0.5F, 1, 3, 1, -0.1F),
                0, 0.3F, 0.8F, rad(-70), 0, side * rad(10));
        PartDefinition hand = add(elbow, "hand", cubes()
                        .box(UV_S_HAND, -1.5F, 0, -1, 3, 3, 2, -0.2F),
                0, lenFore, 0, rad(-14), 0, side * rad(6));
        // dedos largos como garras: falange + garra curvada
        for (int f = 0; f < fingers; f++) {
            float fx = (f - (fingers - 1) / 2.0F) * 0.95F;
            int proximal = (f == 1 || f == 2) ? 4 : 3;
            float spread = (f - (fingers - 1) / 2.0F) * 0.22F;
            PartDefinition finger = add(hand, "f" + f, cubes().box(UV_B_THIN, -0.5F, 0, -0.5F, 1, proximal, 1, -0.1F),
                    fx, 2.6F, 0.1F, rad(-22 - f * 5), 0, spread);
            add(finger, "t", cubes().box(UV_B_THIN, -0.5F, 0, -0.5F, 1, 4, 1, -0.2F),
                    0, proximal - 0.2F, 0, rad(-48 - f * 4), 0, 0);
        }
        // pulgar
        PartDefinition thumb = add(hand, "thumb", cubes().box(UV_B_THIN, -0.5F, 0, -0.5F, 1, 3, 1, -0.1F),
                side * -1.7F, 1.0F, -0.2F, rad(-35), 0, side * rad(-38));
        add(thumb, "t", cubes().box(UV_B_THIN, -0.5F, 0, -0.5F, 1, 3, 1, -0.2F), 0, 2.8F, 0, rad(-50), 0, 0);
    }

    /** Cabeza alargada tipo craneo deforme, con mandibula separada, colmillos y cuencas huecas. */
    private static void head(PartDefinition pivot) {
        PartDefinition head = add(pivot, "head", cubes(), 0, 0, 0, HEAD_TILT, 0, 0);

        // craneo alargado y ligeramente torcido
        add(head, "skull", cubes()
                        .box(UV_S_CRANIUM, -2.5F, -5.0F, -6.0F, 5, 4, 6)
                        .box(UV_S_BROW, -3.0F, -5.6F, -6.2F, 6, 1, 2),
                0, 0, 0, rad(-4), rad(3), rad(5));
        // hocico estrecho que cae hacia delante
        PartDefinition snout = add(head, "snout", cubes().box(UV_S_SNOUT, -1.5F, -3.0F, -4.0F, 3, 3, 4),
                0, -1.2F, -5.8F, rad(9), rad(-4), 0);
        // bulto deforme en un lado del craneo
        add(head, "lump", cubes().box(UV_S_LUMP, -1.5F, -1.5F, -1.5F, 3, 3, 3, -0.3F),
                2.4F, -4.4F, -2.6F, rad(25), rad(20), rad(30));
        // cuernos/crestas de hueso barridos hacia atras (uno roto)
        PartDefinition hornR = add(head, "hornR", cubes().box(UV_B_THIN, -0.5F, -6, -0.5F, 1, 6, 1),
                1.6F, -4.6F, -0.8F, rad(-100), rad(8), rad(-20));
        add(hornR, "tip", cubes().box(UV_B_THIN, -0.5F, -4, -0.5F, 1, 4, 1, -0.2F), 0, -5.6F, 0, rad(-25), 0, 0);
        add(head, "hornL", cubes().box(UV_B_THIN, -0.5F, -3, -0.5F, 1, 3, 1, -0.1F),
                -1.9F, -4.4F, -1.0F, rad(-80), rad(-12), rad(28));

        // cuencas huecas (oscuras) y ojos que sobresalen un poco: el ojo izquierdo es mas pequeño
        add(head, "socketR", cubes().box(UV_D_SOCKET, -1.5F, -1.5F, -1.0F, 3, 3, 1),
                1.3F, -3.4F, -6.3F, rad(4), 0, rad(-14));
        add(head, "socketL", cubes().box(UV_D_SOCKET, -1.5F, -1.5F, -1.0F, 3, 3, 1, -0.2F),
                -1.4F, -3.3F, -6.2F, rad(-3), 0, rad(20));
        add(head, "eyeR", cubes().box(UV_E_EYE, -1, -1, -1, 2, 2, 1, -0.2F), 1.3F, -3.4F, -7.0F, 0, 0, rad(-14));
        add(head, "eyeL", cubes().box(UV_E_EYE, -1, -1, -1, 2, 2, 1, -0.45F), -1.4F, -3.3F, -6.9F, 0, 0, rad(20));

        // colmillos superiores finos, desiguales
        add(snout, "fang1", cubes().box(UV_B_THIN, -0.5F, 0, -0.5F, 1, 3, 1, -0.15F), 1.1F, 0.0F, -3.4F, rad(-8), 0, rad(-10));
        add(snout, "fang2", cubes().box(UV_B_THIN, -0.5F, 0, -0.5F, 1, 4, 1, -0.2F), -1.2F, 0.0F, -3.0F, rad(5), 0, rad(14));
        add(snout, "fang3", cubes().box(UV_B_THIN, -0.5F, 0, -0.5F, 1, 2, 1, -0.2F), 0.9F, 0.0F, -1.2F, 0, 0, rad(-6));
        add(snout, "fang4", cubes().box(UV_B_THIN, -0.5F, 0, -0.5F, 1, 2, 1, -0.2F), -1.0F, 0.0F, -0.8F, rad(6), 0, rad(8));

        // mandibula inferior, separada, gira sobre su bisagra bajo el craneo
        PartDefinition jaw = add(head, "jaw", cubes()
                        .box(UV_S_JAW, -1.5F, -0.5F, -7.5F, 3, 2, 7, -0.2F),
                0, -1.3F, -0.6F, rad(4), rad(-3), 0);
        add(jaw, "chin", cubes().box(UV_B_THIN, -0.5F, 0, -0.5F, 1, 3, 1, -0.2F), 0.2F, 1.4F, -7.0F, rad(-30), 0, rad(6));
        add(jaw, "lfang1", cubes().box(UV_B_THIN, -0.5F, -3, -0.5F, 1, 3, 1, -0.2F), 1.0F, -0.2F, -6.6F, rad(8), 0, rad(-6));
        add(jaw, "lfang2", cubes().box(UV_B_THIN, -0.5F, -3, -0.5F, 1, 3, 1, -0.25F), -1.0F, -0.2F, -6.0F, rad(-6), 0, rad(8));
        add(jaw, "lfang3", cubes().box(UV_B_THIN, -0.5F, -2, -0.5F, 1, 2, 1, -0.2F), 0.9F, -0.2F, -3.6F, 0, 0, rad(-4));
    }

    /** Pata digitigrada: muslo adelantado, espinilla invertida hacia atras, pie largo con garras. */
    private static void leg(PartDefinition body, String id, float side, float x, float y, float z,
                            float thighX, float shinX, float footX) {
        PartDefinition thigh = add(body, "thigh" + id, cubes()
                        .box(UV_S_THIGH, -1.5F, -1, -1.5F, 3, 7, 3, -0.2F),
                side * x, y, z, thighX, 0, side * rad(-4));
        add(thigh, "quad", cubes().box(UV_S_MUSCLE, -1, 0, -1, 2, 4, 2), 0, 0.8F, -1.6F, rad(-18), 0, 0);
        PartDefinition shin = add(thigh, "shin" + id, cubes()
                        .box(UV_S_SHIN, -1, -0.5F, -1, 2, 8, 2, -0.1F),
                0, 6.0F, 0, shinX, 0, 0);
        add(shin, "calf", cubes().box(UV_S_MUSCLE, -1, 0, -1, 2, 4, 2, -0.2F), 0, 0.5F, 1.2F, rad(14), 0, 0);
        PartDefinition foot = add(shin, "foot" + id, cubes()
                        .box(UV_S_FOOT, -1.5F, -1, -1, 3, 6, 2, -0.2F),
                0, 7.0F, 0, footX, 0, 0);
        // garras de los dedos: apuntan adelante y se curvan hacia el suelo
        for (int t = 0; t < 3; t++) {
            float tx = (t - 1) * 1.2F;
            add(foot, "toe" + t, cubes().box(UV_B_THIN, -0.5F, 0, -0.5F, 1, 3 + (t == 1 ? 1 : 0), 1, -0.15F),
                    tx, 4.6F, 0.4F, rad(26), 0, side * rad(-8) + (t - 1) * 0.22F);
        }
        // espolon del talon (hueso hacia atras)
        add(foot, "spur", cubes().box(UV_B_THIN, -0.5F, -4, -0.5F, 1, 4, 1, -0.2F), 0, -0.5F, 0.2F, rad(15), 0, 0);
    }

    // ===== INSTANCIA / ANIMACION =====
    /** Pose base de una pieza: setupAnim siempre parte de aqui (nada se acumula entre frames). */
    private static final class Joint {
        final ModelPart part;
        final float x, y, z, rx, ry, rz;

        Joint(ModelPart part) {
            this.part = part;
            this.x = part.x;
            this.y = part.y;
            this.z = part.z;
            this.rx = part.xRot;
            this.ry = part.yRot;
            this.rz = part.zRot;
        }

        void rot(float dx, float dy, float dz) {
            part.xRot = rx + dx;
            part.yRot = ry + dy;
            part.zRot = rz + dz;
        }

        void pos(float dx, float dy, float dz) {
            part.x = x + dx;
            part.y = y + dy;
            part.z = z + dz;
        }
    }

    private final ModelPart rootPart;
    private final Joint body;
    private final Joint[] seg = new Joint[SEG.length];
    private final Joint headPivot;
    private final Joint head;
    private final Joint jaw;
    private final Joint tail1, tail2, tail3;
    private final Joint thighR, shinR, footR, thighL, shinL, footL;
    /** Brazos: [0]=uArmR [1]=uArmL [2]=lArmR [3]=lArmL; cada uno {hombro, codo, mano}. */
    private final Joint[][] arms = new Joint[4][3];
    /** Dedos (sin pulgar) de cada brazo: uArmR, uArmL, lArmR, lArmL. */
    private static final int[] FINGERS = {3, 4, 3, 3};
    private static final String[] ARM_NAMES = {"uArmR", "uArmL", "lArmR", "lArmL"};
    private final List<Joint> fingers = new ArrayList<>();
    private final List<Joint> ribsR = new ArrayList<>();
    private final List<Joint> ribsL = new ArrayList<>();

    public RetorcidoModel(ModelPart root) {
        this.rootPart = root;
        ModelPart bodyPart = root.getChild("body");
        this.body = new Joint(bodyPart);

        ModelPart[] parts = new ModelPart[SEG.length];
        ModelPart cur = bodyPart;
        for (int i = 0; i < SEG.length; i++) {
            cur = cur.getChild(SEG[i]);
            parts[i] = cur;
            this.seg[i] = new Joint(cur);
        }
        this.headPivot = new Joint(parts[7].getChild("headPivot"));
        ModelPart headPart = parts[7].getChild("headPivot").getChild("head");
        this.head = new Joint(headPart);
        this.jaw = new Joint(headPart.getChild("jaw"));

        ModelPart t1 = parts[0].getChild("tail1");
        ModelPart t2 = t1.getChild("tail2");
        this.tail1 = new Joint(t1);
        this.tail2 = new Joint(t2);
        this.tail3 = new Joint(t2.getChild("tail3"));

        ModelPart tR = bodyPart.getChild("thighR");
        ModelPart sR = tR.getChild("shinR");
        this.thighR = new Joint(tR);
        this.shinR = new Joint(sR);
        this.footR = new Joint(sR.getChild("footR"));
        ModelPart tL = bodyPart.getChild("thighL");
        ModelPart sL = tL.getChild("shinL");
        this.thighL = new Joint(tL);
        this.shinL = new Joint(sL);
        this.footL = new Joint(sL.getChild("footL"));

        for (int a = 0; a < 4; a++) {
            ModelPart host = parts[a < 2 ? UPPER_SEG : LOWER_SEG];
            ModelPart shoulder = host.getChild(ARM_NAMES[a]);
            ModelPart elbow = shoulder.getChild("elbow");
            ModelPart hand = elbow.getChild("hand");
            this.arms[a][0] = new Joint(shoulder);
            this.arms[a][1] = new Joint(elbow);
            this.arms[a][2] = new Joint(hand);
            for (int f = 0; f < FINGERS[a]; f++) {
                this.fingers.add(new Joint(hand.getChild("f" + f)));
                this.fingers.add(new Joint(hand.getChild("f" + f).getChild("t")));
            }
            this.fingers.add(new Joint(hand.getChild("thumb")));
        }

        for (int[] spec : RIBS) {
            for (int k = 0; k < spec[1]; k++) {
                this.ribsR.add(new Joint(parts[spec[0]].getChild("rib" + k + "R")));
                this.ribsL.add(new Joint(parts[spec[0]].getChild("rib" + k + "L")));
            }
        }
    }

    @Override
    public void setupAnim(RetorcidoEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float walk = limbSwing * 0.6662F;
        float a = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float t = ageInTicks;
        boolean attacking = entity.isAggressive();
        float breath = Mth.sin(t * 0.09F);

        // --- cuerpo: vaiven al caminar ---
        body.pos(0.0F, -Math.abs(Mth.cos(walk)) * 0.7F * a, 0.0F);
        body.rot(0.0F, 0.0F, Mth.cos(walk) * 0.05F * a);

        // --- columna: respiracion (el costillar sube/baja) + torsion al andar + embestida ---
        float lean = attacking ? 0.14F + Mth.sin(t * 0.5F) * 0.03F : 0.0F;
        seg[0].rot(0.0F, 0.0F, 0.0F);
        seg[1].rot(lean * 0.5F, Mth.cos(walk) * 0.12F * a, 0.0F);
        seg[2].rot(breath * 0.035F + lean, 0.0F, 0.0F);
        seg[3].rot(-breath * 0.03F + lean * 0.5F, -Mth.cos(walk) * 0.10F * a, 0.0F);
        seg[4].rot(breath * 0.02F, 0.0F, 0.0F);
        seg[5].rot(Mth.sin(t * 0.05F) * 0.02F, 0.0F, 0.0F);
        seg[6].rot(0.0F, 0.0F, 0.0F);
        seg[7].rot(0.0F, 0.0F, 0.0F);
        for (int i = 0; i < ribsR.size(); i++) {
            ribsR.get(i).rot(0.0F, breath * 0.06F, breath * 0.05F);
            ribsL.get(i).rot(0.0F, -breath * 0.06F, -breath * 0.05F);
        }

        // --- cabeza: sigue al objetivo (limitado); headPivot gira en el eje vertical del mundo ---
        float yaw = Mth.clamp(netHeadYaw, -50.0F, 50.0F) * Mth.DEG_TO_RAD * 0.8F;
        float pitch = Mth.clamp(headPitch, -30.0F, 30.0F) * Mth.DEG_TO_RAD * 0.8F;
        headPivot.rot(0.0F, yaw, 0.0F);
        head.rot(pitch + Mth.sin(t * 0.04F) * 0.04F, 0.0F, Mth.sin(t * 0.031F) * 0.06F);

        // --- mandibula: entreabierta y temblorosa; abierta de par en par al atacar ---
        float open = attacking
                ? 0.60F + Mth.sin(t * 0.8F) * 0.14F
                : 0.07F + (breath * 0.5F + 0.5F) * 0.07F;
        jaw.rot(open, 0.0F, 0.0F);

        // --- cola ---
        tail1.rot(Mth.sin(t * 0.06F) * 0.05F, Mth.sin(walk) * 0.25F * a + Mth.sin(t * 0.05F) * 0.08F, 0.0F);
        tail2.rot(0.0F, Mth.sin(walk - 0.8F) * 0.30F * a + Mth.sin(t * 0.05F - 0.7F) * 0.12F, 0.0F);
        tail3.rot(0.0F, Mth.sin(walk - 1.6F) * 0.35F * a + Mth.sin(t * 0.05F - 1.4F) * 0.15F, 0.0F);

        // --- patas: alternadas ---
        animLeg(thighR, shinR, footR, walk, a);
        animLeg(thighL, shinL, footL, walk + Mth.PI, a);

        // --- cuatro brazos: oscilan con desfase; los superiores se alzan al atacar ---
        animArm(arms[0], Mth.PI, 0.55F, -1.15F, attacking, walk, a, t);
        animArm(arms[1], 0.0F, 0.60F, -1.30F, attacking, walk, a, t);
        animArm(arms[2], Mth.PI * 0.5F, 0.45F, -0.55F, attacking, walk, a, t);
        animArm(arms[3], -Mth.PI * 0.5F, 0.50F, -0.70F, attacking, walk, a, t);

        // --- dedos: tics al estar quieto, zarpazos al atacar ---
        for (int i = 0; i < fingers.size(); i++) {
            float twitch = attacking
                    ? Mth.sin(t * 0.6F + i * 0.8F) * 0.28F - 0.15F
                    : Mth.sin(t * 0.09F + i * 1.3F) * 0.07F;
            fingers.get(i).rot(twitch, 0.0F, 0.0F);
        }
    }

    private void animLeg(Joint thigh, Joint shin, Joint foot, float phase, float a) {
        float swing = Mth.cos(phase);
        float lift = Math.max(0.0F, Mth.cos(phase + 1.5708F));
        thigh.rot(swing * 0.75F * a, 0.0F, 0.0F);
        shin.rot(lift * 0.80F * a, 0.0F, 0.0F);
        foot.rot(-swing * 0.35F * a - lift * 0.55F * a, 0.0F, 0.0F);
    }

    private void animArm(Joint[] arm, float phase, float amp, float raise, boolean attacking,
                         float walk, float a, float t) {
        float swing = Mth.cos(walk + phase) * amp * a;
        float idle = Mth.sin(t * 0.07F + phase * 1.7F) * 0.05F;
        float shoulder = swing + idle;
        float elbow = -(Mth.cos(walk + phase + 1.2F) * 0.5F + 0.5F) * 0.45F * a - idle * 0.8F;
        float wrist = Mth.sin(t * 0.08F + phase) * 0.06F;
        if (attacking) {
            float claw = Mth.sin(t * 0.55F + phase) * 0.22F;
            shoulder = raise + claw + swing * 0.3F;
            elbow = -0.45F + claw * 0.8F;
            wrist = -0.25F + claw * 0.5F;
        }
        arm[0].rot(shoulder, 0.0F, 0.0F);
        arm[1].rot(elbow, 0.0F, 0.0F);
        arm[2].rot(wrist, 0.0F, 0.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        rootPart.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
