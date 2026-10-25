package com.keinloree.client;

import com.keinloree.KeinLooreMod;
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
import net.minecraft.world.entity.Mob;

/**
 * Droide soldado de Kein Loree: armadura verde oscura por placas, visor y núcleo luminosos, mochila con antena,
 * articulaciones en codo y rodilla y un cañón blaster integrado en el brazo derecho.
 */
public class DroidModel<T extends Mob> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "droid"), "main");

    private static final float DEG = (float) (Math.PI / 180.0D);

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;

    public DroidModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftShin = this.leftLeg.getChild("left_shin");
    }

    /** Geometria generada por el script de modelos: no editar a mano. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition v_pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create()
                .texOffs(62, 12).addBox(-5.0F, -3.0F, -2.0F, 10.0F, 3.0F, 4.0F)
                , PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition v_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(90, 12).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 3.0F, 4.0F)
                .texOffs(40, 0).addBox(-4.0F, -9.0F, -2.0F, 8.0F, 6.0F, 4.0F)
                .texOffs(52, 28).addBox(-1.0F, -7.0F, -3.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(10, 28).addBox(1.0F, -9.0F, -3.0F, 3.0F, 3.0F, 1.0F)
                .texOffs(10, 28).mirror().addBox(-4.0F, -9.0F, -3.0F, 3.0F, 3.0F, 1.0F).mirror(false)
                .texOffs(102, 0).addBox(-3.0F, -10.0F, 2.0F, 6.0F, 6.0F, 3.0F)
                .texOffs(58, 21).addBox(1.0F, -15.0F, 3.0F, 1.0F, 5.0F, 1.0F)
                .texOffs(68, 28).addBox(1.0F, -16.0F, 3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(36, 28).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 1.0F, 2.0F)
                , PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition v_head = v_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 6.0F, 6.0F)
                .texOffs(26, 28).addBox(-2.0F, -5.0F, -4.0F, 4.0F, 2.0F, 1.0F)
                .texOffs(108, 21).addBox(3.0F, -5.0F, -1.0F, 1.0F, 3.0F, 2.0F)
                .texOffs(108, 21).mirror().addBox(-4.0F, -5.0F, -1.0F, 1.0F, 3.0F, 2.0F).mirror(false)
                .texOffs(62, 21).addBox(3.0F, -10.0F, 1.0F, 1.0F, 5.0F, 1.0F)
                .texOffs(110, 12).addBox(-1.0F, -8.0F, -3.0F, 2.0F, 1.0F, 6.0F)
                .texOffs(58, 28).addBox(-2.0F, -2.0F, -4.0F, 4.0F, 1.0F, 1.0F)
                , PartPose.offset(0.0F, -9.0F, 0.0F));
        PartDefinition v_right_arm = v_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(32, 12).addBox(-3.0F, -2.0F, -3.0F, 5.0F, 2.0F, 6.0F)
                .texOffs(16, 21).addBox(-3.0F, 0.0F, -2.0F, 1.0F, 3.0F, 4.0F)
                .texOffs(0, 12).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F)
                , PartPose.offset(-6.0F, -8.0F, 0.0F));
        PartDefinition v_left_arm = v_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(32, 12).mirror().addBox(-2.0F, -2.0F, -3.0F, 5.0F, 2.0F, 6.0F).mirror(false)
                .texOffs(16, 21).mirror().addBox(2.0F, 0.0F, -2.0F, 1.0F, 3.0F, 4.0F).mirror(false)
                .texOffs(0, 12).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F).mirror(false)
                , PartPose.offset(6.0F, -8.0F, 0.0F));
        PartDefinition v_right_forearm = v_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(18, 28).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(16, 12).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 5.0F, 4.0F)
                .texOffs(114, 21).addBox(-2.0F, 2.0F, -3.0F, 4.0F, 3.0F, 1.0F)
                .texOffs(54, 12).addBox(-1.0F, 6.0F, -1.0F, 2.0F, 6.0F, 2.0F)
                .texOffs(66, 21).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 1.0F, 4.0F)
                .texOffs(82, 21).addBox(-2.0F, 9.0F, -2.0F, 4.0F, 1.0F, 4.0F)
                .texOffs(44, 28).addBox(-1.0F, 12.0F, -1.0F, 2.0F, 1.0F, 2.0F)
                , PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition v_left_forearm = v_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(18, 28).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false)
                .texOffs(16, 12).mirror().addBox(-2.0F, 1.0F, -2.0F, 4.0F, 5.0F, 4.0F).mirror(false)
                .texOffs(114, 21).mirror().addBox(-2.0F, 2.0F, -3.0F, 4.0F, 3.0F, 1.0F).mirror(false)
                .texOffs(0, 21).addBox(-2.0F, 6.0F, -2.0F, 4.0F, 3.0F, 4.0F)
                , PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition v_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(64, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F)
                .texOffs(98, 21).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 4.0F, 1.0F)
                , PartPose.offset(-3.0F, 8.0F, 0.0F));
        PartDefinition v_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(64, 0).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F).mirror(false)
                .texOffs(98, 21).mirror().addBox(-2.0F, 0.0F, -3.0F, 4.0F, 4.0F, 1.0F).mirror(false)
                , PartPose.offset(3.0F, 8.0F, 0.0F));
        PartDefinition v_right_shin = v_right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(32, 21).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F)
                .texOffs(24, 0).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 7.0F, 4.0F)
                .texOffs(48, 21).addBox(-2.0F, 2.0F, -3.0F, 4.0F, 5.0F, 1.0F)
                .texOffs(0, 28).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 3.0F, 1.0F)
                .texOffs(26, 21).addBox(-1.0F, 1.0F, 2.0F, 2.0F, 6.0F, 1.0F)
                .texOffs(80, 0).addBox(-2.0F, 8.0F, -5.0F, 4.0F, 2.0F, 7.0F)
                , PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition v_left_shin = v_left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(32, 21).mirror().addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F).mirror(false)
                .texOffs(24, 0).mirror().addBox(-2.0F, 1.0F, -2.0F, 4.0F, 7.0F, 4.0F).mirror(false)
                .texOffs(48, 21).mirror().addBox(-2.0F, 2.0F, -3.0F, 4.0F, 5.0F, 1.0F).mirror(false)
                .texOffs(0, 28).mirror().addBox(-2.0F, -2.0F, -3.0F, 4.0F, 3.0F, 1.0F).mirror(false)
                .texOffs(26, 21).mirror().addBox(-1.0F, 1.0F, 2.0F, 2.0F, 6.0F, 1.0F).mirror(false)
                .texOffs(80, 0).mirror().addBox(-2.0F, 8.0F, -5.0F, 4.0F, 2.0F, 7.0F).mirror(false)
                , PartPose.offset(0.0F, 6.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = Mth.clamp(netHeadYaw, -70.0F, 70.0F) * DEG;
        this.head.xRot = Mth.clamp(headPitch, -45.0F, 45.0F) * DEG;

        float swing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = swing;
        this.leftLeg.xRot = -swing;
        // La rodilla se flexiona cuando la pierna pasa hacia atrás
        this.rightShin.xRot = Math.max(0.0F, swing) * 0.85F;
        this.leftShin.xRot = Math.max(0.0F, -swing) * 0.85F;

        this.body.y = Mth.sin(ageInTicks * 0.1F) * 0.15F;
        this.body.xRot = 0.0F;
        this.body.yRot = 0.0F;

        if (entity.getTarget() != null) {
            // Apunta con el cañón del brazo derecho y lo apoya con la mano izquierda, con una leve vibración de carga
            this.rightArm.xRot = -1.4F + Mth.sin(ageInTicks * 0.4F) * 0.02F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.rightForearm.xRot = -0.08F;
            this.leftArm.xRot = -0.95F;
            this.leftArm.yRot = 0.32F;
            this.leftArm.zRot = 0.0F;
            this.leftForearm.xRot = -0.65F;
            this.body.xRot = 0.06F;
            this.head.xRot -= 0.06F;
        } else {
            this.rightArm.xRot = -swing * 0.6F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.07F + Mth.sin(ageInTicks * 0.07F) * 0.02F;
            this.rightForearm.xRot = -0.12F - Math.max(0.0F, swing) * 0.3F;
            this.leftArm.xRot = swing * 0.6F;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = -0.07F - Mth.sin(ageInTicks * 0.07F) * 0.02F;
            this.leftForearm.xRot = -0.12F - Math.max(0.0F, -swing) * 0.3F;
        }
    }
}
