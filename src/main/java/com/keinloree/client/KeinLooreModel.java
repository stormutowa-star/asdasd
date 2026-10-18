package com.keinloree.client;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.LooreVisual;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * Modelo de Kein Loree y de sus comandantes (misma geometria, distinta textura).
 * Forma base: sin alas. Forma final (Techno Winged Code): alas de codigo que se despliegan.
 * Todas las animaciones son procedurales: se calculan en setupAnim a partir del estado sincronizado.
 */
public class KeinLooreModel<T extends Entity & LooreVisual> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "kein_loree"), "main");

    private static final float DEG = (float) (Math.PI / 180.0D);
    /** Altura de la figura en pixeles de modelo (los pies estan en y=24). */
    private static final float FIGURE_HEIGHT = 24.0F;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tailL;
    private final ModelPart tailR;
    private final ModelPart pauldronL;
    private final ModelPart pauldronR;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart wingL;
    private final ModelPart wingR;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public KeinLooreModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.tailL = this.body.getChild("tail_l");
        this.tailR = this.body.getChild("tail_r");
        this.pauldronL = this.body.getChild("pauldron_l");
        this.pauldronR = this.body.getChild("pauldron_r");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.wingL = this.body.getChild("wing_l");
        this.wingR = this.body.getChild("wing_r");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    /** Geometria generada por gen_models.py: no editar a mano (regenerar el script). */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition v_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(32, 20).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
                .texOffs(0, 0).addBox(-5.0F, -1.0F, -3.0F, 10.0F, 14.0F, 6.0F)
                .texOffs(26, 52).addBox(-5.0F, -2.0F, -3.0F, 10.0F, 2.0F, 6.0F)
                .texOffs(14, 73).addBox(-2.0F, 3.0F, -4.0F, 4.0F, 4.0F, 1.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_head = v_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                .texOffs(48, 36).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 4.0F, 10.0F)
                .texOffs(24, 73).addBox(-5.0F, -6.0F, -5.0F, 10.0F, 2.0F, 1.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_tail_l = v_body.addOrReplaceChild("tail_l", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-5.0F, 11.0F, -3.0F, 5.0F, 13.0F, 6.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_tail_r = v_body.addOrReplaceChild("tail_r", CubeListBuilder.create()
                .texOffs(54, 0).addBox(0.0F, 11.0F, -3.0F, 5.0F, 13.0F, 6.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_pauldron_l = v_body.addOrReplaceChild("pauldron_l", CubeListBuilder.create()
                .texOffs(88, 36).addBox(-8.0F, -3.0F, -4.0F, 5.0F, 5.0F, 8.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_pauldron_r = v_body.addOrReplaceChild("pauldron_r", CubeListBuilder.create()
                .texOffs(0, 52).addBox(3.0F, -3.0F, -4.0F, 5.0F, 5.0F, 8.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_right_arm = v_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(96, 20).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                .texOffs(56, 20).addBox(-4.0F, -3.0F, -3.0F, 5.0F, 11.0F, 5.0F)
                , PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition v_left_arm = v_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(112, 20).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                .texOffs(76, 20).addBox(-1.0F, -3.0F, -3.0F, 5.0F, 11.0F, 5.0F)
                , PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition v_wing_l = v_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(32, 36).addBox(-1.0F, -14.0F, 0.0F, 2.0F, 14.0F, 2.0F)
                .texOffs(58, 52).addBox(-7.0F, -19.0F, 0.0F, 6.0F, 6.0F, 2.0F)
                .texOffs(74, 52).addBox(-13.0F, -24.0F, 0.0F, 6.0F, 6.0F, 2.0F)
                .texOffs(20, 65).addBox(-19.0F, -18.0F, 0.0F, 6.0F, 5.0F, 2.0F)
                .texOffs(52, 65).addBox(-12.0F, -10.0F, 0.0F, 5.0F, 5.0F, 2.0F)
                .texOffs(80, 65).addBox(-19.0F, -9.0F, 0.0F, 5.0F, 4.0F, 2.0F)
                .texOffs(108, 65).addBox(-25.0F, -14.0F, 0.0F, 5.0F, 3.0F, 2.0F)
                .texOffs(0, 65).addBox(-4.0F, -24.0F, 0.0F, 3.0F, 6.0F, 2.0F)
                , PartPose.offset(-3.0F, 1.0F, 3.0F));
        PartDefinition v_wing_r = v_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(40, 36).addBox(-1.0F, -14.0F, 0.0F, 2.0F, 14.0F, 2.0F)
                .texOffs(90, 52).addBox(1.0F, -19.0F, 0.0F, 6.0F, 6.0F, 2.0F)
                .texOffs(106, 52).addBox(7.0F, -24.0F, 0.0F, 6.0F, 6.0F, 2.0F)
                .texOffs(36, 65).addBox(13.0F, -18.0F, 0.0F, 6.0F, 5.0F, 2.0F)
                .texOffs(66, 65).addBox(7.0F, -10.0F, 0.0F, 5.0F, 5.0F, 2.0F)
                .texOffs(94, 65).addBox(14.0F, -9.0F, 0.0F, 5.0F, 4.0F, 2.0F)
                .texOffs(0, 73).addBox(20.0F, -14.0F, 0.0F, 5.0F, 3.0F, 2.0F)
                .texOffs(10, 65).addBox(1.0F, -24.0F, 0.0F, 3.0F, 6.0F, 2.0F)
                , PartPose.offset(3.0F, 1.0F, 3.0F));
        PartDefinition v_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                .texOffs(76, 0).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 12.0F, 6.0F)
                , PartPose.offset(-2.0F, 12.0F, 0.0F));
        PartDefinition v_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 36).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                .texOffs(100, 0).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 12.0F, 6.0F)
                , PartPose.offset(2.0F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetPose();

        int pose = entity.getLorePose();
        float wing = Mth.clamp(entity.getWingOpen(), 0.0F, 1.0F);
        int spawn = entity.getSpawnTicks();

        // Emergencia desde el suelo (copias): la figura sube desde los pies, de abajo hacia arriba.
        this.root.y = FIGURE_HEIGHT * Mth.clamp(spawn / 60.0F, 0.0F, 1.0F);

        this.head.yRot = netHeadYaw * DEG;
        this.head.xRot = headPitch * DEG;

        float breath = Mth.sin(ageInTicks * 0.07F);
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = swing;
        this.leftLeg.xRot = -swing;
        this.rightArm.xRot = -swing * 0.6F;
        this.leftArm.xRot = swing * 0.6F;
        this.body.y = breath * 0.25F;
        // Los faldones del abrigo se mueven con el viento de su propia energia
        this.tailL.xRot = 0.05F + Mth.sin(ageInTicks * 0.08F) * 0.06F + swing * 0.25F;
        this.tailR.xRot = 0.05F - Mth.sin(ageInTicks * 0.08F) * 0.06F - swing * 0.25F;

        if (entity.isLoreDormant() || pose == LooreVisual.POSE_SIT) {
            this.applySit();
        } else {
            switch (pose) {
                case LooreVisual.POSE_CAST -> {
                    this.rightArm.xRot = -1.25F;
                    this.leftArm.xRot = -1.05F;
                    this.rightArm.zRot = -0.12F;
                    this.leftArm.zRot = 0.12F;
                    this.body.xRot = -0.06F;
                }
                case LooreVisual.POSE_SPREAD -> {
                    this.rightArm.xRot = -0.6F;
                    this.leftArm.xRot = -0.6F;
                    this.rightArm.zRot = -1.05F;
                    this.leftArm.zRot = 1.05F;
                }
                case LooreVisual.POSE_FLY -> {
                    this.body.xRot = -0.35F;
                    this.head.xRot = -0.2F;
                    this.rightArm.xRot = -0.9F;
                    this.leftArm.xRot = -0.9F;
                    this.rightArm.zRot = -0.5F;
                    this.leftArm.zRot = 0.5F;
                    this.rightLeg.xRot = 0.6F;
                    this.leftLeg.xRot = 0.5F;
                    this.tailL.xRot = 0.9F;
                    this.tailR.xRot = 0.9F;
                }
                case LooreVisual.POSE_CHARGE -> {
                    this.joinArms(-1.35F, 0.7F);
                    this.body.yRot = Mth.sin(ageInTicks * 1.7F) * 0.03F;
                }
                case LooreVisual.POSE_FIRE -> {
                    this.joinArms(-1.5F, 0.85F);
                    this.body.xRot = -0.25F;
                }
                case LooreVisual.POSE_SUMMON -> {
                    this.rightArm.xRot = -2.6F;
                    this.leftArm.xRot = -2.6F;
                    this.rightArm.zRot = -0.25F;
                    this.leftArm.zRot = 0.25F;
                }
                default -> {
                    // POSE_IDLE: ya aplicada arriba
                }
            }
        }

        // Alas: plegadas (wing=0) o desplegadas (wing=1). Solo existen en la forma final.
        boolean winged = entity.getLoreForm() == 1 || wing > 0.0F;
        this.wingL.visible = winged;
        this.wingR.visible = winged;
        float fold = (1.0F - wing) * 1.0F;
        float flap = wing > 0.0F ? Mth.sin(ageInTicks * 0.25F) * 0.08F * wing : 0.0F;
        this.wingL.zRot = fold + flap;
        this.wingR.zRot = -fold - flap;

        // Hombreras: solo para el ejecutor (comandante fuerte)
        boolean heavy = entity.getLoreSkin() == 2;
        this.pauldronL.visible = heavy;
        this.pauldronR.visible = heavy;
    }

    private void joinArms(float xRot, float inward) {
        this.rightArm.xRot = xRot;
        this.leftArm.xRot = xRot;
        this.rightArm.yRot = inward;
        this.leftArm.yRot = -inward;
    }

    private void applySit() {
        this.rightLeg.xRot = -1.5F;
        this.leftLeg.xRot = -1.5F;
        this.body.xRot = 0.04F;
        this.rightArm.xRot = -0.35F;
        this.leftArm.xRot = -0.35F;
        this.rightArm.zRot = -0.12F;
        this.leftArm.zRot = 0.12F;
    }

    private void resetPose() {
        this.root.y = 0.0F;
        this.body.x = 0.0F;
        this.body.y = 0.0F;
        this.body.xRot = 0.0F;
        this.body.yRot = 0.0F;
        this.body.zRot = 0.0F;
        this.head.xRot = 0.0F;
        this.head.yRot = 0.0F;
        this.rightArm.xRot = 0.0F;
        this.rightArm.yRot = 0.0F;
        this.rightArm.zRot = 0.0F;
        this.leftArm.xRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = 0.0F;
        this.leftLeg.xRot = 0.0F;
        this.tailL.xRot = 0.0F;
        this.tailR.xRot = 0.0F;
    }
}
