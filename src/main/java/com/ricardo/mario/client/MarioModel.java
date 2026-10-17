package com.ricardo.mario.client;

import com.ricardo.mario.MarioEntity;
import com.ricardo.mario.MarioMod;
import net.minecraft.client.model.HierarchicalModel;
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

/**
 * Modelo de Mario estilo chibi. Textura 128x128 (assets/mario/textures/entity/mario.png).
 * Cada texOffs apunta a la region de su caja en la textura (cara frontal = -Z).
 * Derecha del personaje = -X.
 */
public class MarioModel<T extends MarioEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MarioMod.MODID, "mario"), "main");

    // Curvas de interpolacion entre poses: la animacion no es lineal (arranca, frena, rebota)
    private static final int EASE_LINEAR = 0;
    private static final int EASE_IN_OUT = 1;
    private static final int EASE_OUT = 2;

    // Super Jump Punch: tiempos en ticks desde que empieza (agachado -> impacto -> apice -> caida).
    // Cada pose es {altura del cuerpo, brazo derecho, brazo izquierdo, pierna derecha, pierna izquierda, cabeza}.
    // Altura positiva = hacia abajo (aplastado); negativa = estirado hacia arriba.
    private static final float[] JUMP_TIMES = {0.0F, 6.0F, 9.0F, 13.0F, 18.0F};
    private static final float[][] JUMP_POSES = {
            {0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F},          // reposo
            {3.0F, 0.9F, -0.5F, -0.35F, -0.35F, 0.15F},   // agachado: anticipacion, puno hacia atras
            {-1.5F, -2.9F, 1.0F, -0.9F, -0.4F, -0.35F},   // impacto: estirado y puno arriba
            {-1.0F, -2.75F, 1.1F, -1.0F, -0.6F, -0.25F},  // apice: el puno se asienta (seguimiento)
            {0.0F, -1.2F, 0.5F, -0.2F, -0.2F, 0.1F}       // caida: brazos sueltos
    };
    private static final int[] JUMP_EASES = {EASE_IN_OUT, EASE_OUT, EASE_IN_OUT, EASE_IN_OUT};

    // Aterrizaje: tiempos medidos desde el tick en que toca el suelo
    private static final float[] LAND_TIMES = {0.0F, 2.0F, 6.0F};
    private static final float[][] LAND_POSES = {
            {0.0F, -1.2F, 0.5F, -0.2F, -0.2F, 0.1F},      // justo al tocar: sigue la caida
            {4.0F, -0.3F, 0.2F, 0.2F, 0.2F, 0.25F},       // impacto: aplastado contra el suelo
            {0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F}          // recuperado
    };
    private static final int[] LAND_EASES = {EASE_OUT, EASE_IN_OUT};

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public MarioModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Cabeza (pivote en el cuello): cara, gorra, visera, nariz, bigote, orejas y ojos
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, -12.0F, -6.0F, 12.0F, 12.0F, 12.0F)
                .texOffs(0, 24).addBox(-6.5F, -15.0F, -6.5F, 13.0F, 6.0F, 13.0F)
                .texOffs(64, 0).addBox(-5.0F, -10.0F, -10.0F, 10.0F, 2.0F, 4.0F)
                .texOffs(48, 0).addBox(-2.0F, -6.0F, -9.0F, 4.0F, 4.0F, 3.0F)
                .texOffs(48, 8).addBox(-3.5F, -2.0F, -8.0F, 7.0F, 2.0F, 2.0F)
                .texOffs(96, 0).addBox(6.0F, -7.0F, -2.0F, 1.0F, 3.0F, 2.0F)
                .addBox(-7.0F, -7.0F, -2.0F, 1.0F, 3.0F, 2.0F)
                .texOffs(96, 8).addBox(-4.5F, -7.0F, -7.0F, 3.0F, 4.0F, 1.0F)
                .addBox(1.5F, -7.0F, -7.0F, 3.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        // Torso: camiseta roja + peto azul con botones amarillos
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-4.0F, 8.0F, -2.5F, 8.0F, 8.0F, 5.0F)
                .texOffs(28, 44).addBox(-4.0F, 12.0F, -2.5F, 8.0F, 4.0F, 5.0F, new CubeDeformation(0.3F))
                .texOffs(104, 0).addBox(-3.5F, 12.5F, -3.6F, 2.0F, 2.0F, 1.0F)
                .addBox(1.5F, 12.5F, -3.6F, 2.0F, 2.0F, 1.0F),
                PartPose.ZERO);

        // Brazos: manga roja + guante blanco (ambos usan la misma region de textura)
        root.addOrReplaceChild("right_arm", armBuilder(), PartPose.offset(-5.5F, 9.0F, 0.0F));
        root.addOrReplaceChild("left_arm", armBuilder(), PartPose.offset(5.5F, 9.0F, 0.0F));

        // Piernas: overol azul + zapato marron (ambas usan la misma region de textura)
        root.addOrReplaceChild("right_leg", legBuilder(), PartPose.offset(-2.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("left_leg", legBuilder(), PartPose.offset(2.0F, 16.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    private static CubeListBuilder armBuilder() {
        return CubeListBuilder.create()
                .texOffs(56, 44).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F)
                .texOffs(56, 52).addBox(-2.0F, 4.0F, -2.0F, 4.0F, 4.0F, 4.0F);
    }

    private static CubeListBuilder legBuilder() {
        return CubeListBuilder.create()
                .texOffs(0, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F)
                .texOffs(16, 60).addBox(-2.5F, 4.0F, -4.5F, 5.0F, 4.0F, 7.0F);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.y = 8.0F + Mth.sin(ageInTicks * 0.067F) * 0.25F;
        this.root.y = 0.0F;

        float swing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = swing;
        this.leftLeg.xRot = -swing;

        if (entity.getTarget() != null) {
            // Brazos al frente cuando dispara
            this.rightArm.xRot = -1.5F + Mth.cos(ageInTicks * 0.3F) * 0.1F;
            this.leftArm.xRot = -1.5F + Mth.sin(ageInTicks * 0.3F) * 0.1F;
        } else {
            this.rightArm.xRot = -swing * 0.6F;
            this.leftArm.xRot = swing * 0.6F;
        }

        int punchTick = entity.getPunchTick();
        if (punchTick >= 0) {
            // ageInTicks = tickCount + parte fraccionaria del tick: asi la animacion se ve fluida a 60 fps
            float t = punchTick + (ageInTicks - entity.tickCount);
            this.applyJumpPose(entity.getLandedTick(), t);
        }
    }

    /** Pone la pose del Super Jump Punch: antes de tocar el suelo usa la linea de tiempo del salto; despues, la del aterrizaje. */
    private void applyJumpPose(int landedTick, float t) {
        float[] pose = landedTick < 0
                ? samplePose(t, JUMP_TIMES, JUMP_POSES, JUMP_EASES)
                : samplePose(t - landedTick, LAND_TIMES, LAND_POSES, LAND_EASES);
        this.root.y = pose[0];
        this.rightArm.xRot = pose[1];
        this.leftArm.xRot = pose[2];
        this.rightLeg.xRot = pose[3];
        this.leftLeg.xRot = pose[4];
        this.head.xRot += pose[5];
    }

    /** Interpola entre poses clave: cada tramo usa su curva de easing. */
    private static float[] samplePose(float t, float[] times, float[][] poses, int[] eases) {
        int last = times.length - 1;
        if (t <= times[0]) {
            return poses[0].clone();
        }
        if (t >= times[last]) {
            return poses[last].clone();
        }
        for (int i = 0; i < last; i++) {
            if (t < times[i + 1]) {
                float f = ease(eases[i], (t - times[i]) / (times[i + 1] - times[i]));
                float[] out = new float[poses[i].length];
                for (int j = 0; j < out.length; j++) {
                    out[j] = Mth.lerp(f, poses[i][j], poses[i + 1][j]);
                }
                return out;
            }
        }
        return poses[last].clone();
    }

    private static float ease(int type, float x) {
        float p = Mth.clamp(x, 0.0F, 1.0F);
        return switch (type) {
            case EASE_OUT -> 1.0F - (1.0F - p) * (1.0F - p) * (1.0F - p);
            case EASE_IN_OUT -> -(Mth.cos((float) Math.PI * p) - 1.0F) * 0.5F;
            default -> p;
        };
    }
}
