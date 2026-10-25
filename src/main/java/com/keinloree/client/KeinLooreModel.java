package com.keinloree.client;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.LooreVisual;
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
import net.minecraft.world.entity.Entity;

/**
 * Modelo de Kein Loree y de su facción (comandante y guerrero): misma geometría, tres texturas.
 * Abrigo largo abierto con faldones, capas exteriores en 3D (pelo, abrigo, mangas, puños, botas), colgante,
 * y las alas de código de la forma final. Hombreras de oficial (comandante), cuernos y hombreras pesadas (guerrero).
 * Todas las animaciones son procedurales: se calculan en setupAnim a partir del estado sincronizado.
 */
public class KeinLooreModel<T extends Entity & LooreVisual> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "kein_loree"), "main");

    private static final float DEG = (float) (Math.PI / 180.0D);
    /** Altura de la figura en píxeles de modelo (los pies están en y=24): para emerger del suelo. */
    private static final float FIGURE_HEIGHT = 38.0F;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart hornsL;
    private final ModelPart hornsR;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart epauR;
    private final ModelPart epauL;
    private final ModelPart pauldR;
    private final ModelPart pauldL;
    private final ModelPart tailR;
    private final ModelPart tailL;
    private final ModelPart wingL;
    private final ModelPart wingR;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public KeinLooreModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.hornsL = this.head.getChild("horns_l");
        this.hornsR = this.head.getChild("horns_r");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.epauR = this.rightArm.getChild("epau_r");
        this.epauL = this.leftArm.getChild("epau_l");
        this.pauldR = this.rightArm.getChild("pauld_r");
        this.pauldL = this.leftArm.getChild("pauld_l");
        this.tailR = this.body.getChild("tail_r");
        this.tailL = this.body.getChild("tail_l");
        this.wingL = this.body.getChild("wing_l");
        this.wingR = this.body.getChild("wing_r");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    /** Geometria generada por el script de modelos: no editar a mano. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition v_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(88, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
                .texOffs(0, 23).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
                .texOffs(18, 51).addBox(-4.0F, 11.0F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.75F))
                .texOffs(96, 58).addBox(-1.0F, 11.0F, -3.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(104, 51).addBox(-2.0F, 3.0F, -3.0F, 4.0F, 4.0F, 1.0F)
                .texOffs(0, 51).addBox(4.0F, -3.0F, -1.0F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.5F))
                .texOffs(0, 51).mirror().addBox(-5.0F, -3.0F, -1.0F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_head = v_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(24, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                .texOffs(56, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_horns_l = v_head.addOrReplaceChild("horns_l", CubeListBuilder.create()
                .texOffs(26, 58).addBox(5.0F, -6.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(114, 51).addBox(6.0F, -9.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(0, 58).addBox(7.0F, -12.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_horns_r = v_head.addOrReplaceChild("horns_r", CubeListBuilder.create()
                .texOffs(26, 58).mirror().addBox(-7.0F, -6.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false)
                .texOffs(114, 51).mirror().addBox(-8.0F, -9.0F, -1.0F, 2.0F, 3.0F, 2.0F).mirror(false)
                .texOffs(0, 58).mirror().addBox(-9.0F, -12.0F, -1.0F, 2.0F, 3.0F, 2.0F).mirror(false)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_right_arm = v_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(24, 23).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                .texOffs(78, 23).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.5F))
                .texOffs(110, 39).addBox(-3.0F, 4.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.9F))
                , PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition v_left_arm = v_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(24, 23).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror(false)
                .texOffs(78, 23).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false)
                .texOffs(110, 39).mirror().addBox(-1.0F, 4.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.9F)).mirror(false)
                , PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition v_epau_r = v_right_arm.addOrReplaceChild("epau_r", CubeListBuilder.create()
                .texOffs(86, 51).addBox(-4.0F, -3.0F, -2.0F, 5.0F, 1.0F, 4.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_epau_l = v_left_arm.addOrReplaceChild("epau_l", CubeListBuilder.create()
                .texOffs(86, 51).mirror().addBox(-1.0F, -3.0F, -2.0F, 5.0F, 1.0F, 4.0F).mirror(false)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_pauld_r = v_right_arm.addOrReplaceChild("pauld_r", CubeListBuilder.create()
                .texOffs(78, 39).addBox(-5.0F, -3.0F, -3.0F, 6.0F, 2.0F, 6.0F)
                .texOffs(94, 23).addBox(-6.0F, -3.0F, -3.0F, 1.0F, 7.0F, 6.0F)
                .texOffs(10, 51).addBox(-4.0F, -8.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_pauld_l = v_left_arm.addOrReplaceChild("pauld_l", CubeListBuilder.create()
                .texOffs(78, 39).mirror().addBox(-1.0F, -3.0F, -3.0F, 6.0F, 2.0F, 6.0F).mirror(false)
                .texOffs(94, 23).mirror().addBox(5.0F, -3.0F, -3.0F, 1.0F, 7.0F, 6.0F).mirror(false)
                .texOffs(10, 51).mirror().addBox(2.0F, -8.0F, -1.0F, 2.0F, 5.0F, 2.0F).mirror(false)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_tail_r = v_body.addOrReplaceChild("tail_r", CubeListBuilder.create()
                .texOffs(0, 39).addBox(-3.0F, 1.0F, -3.0F, 5.0F, 6.0F, 6.0F)
                .texOffs(12, 63).addBox(-2.0F, 7.0F, -3.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(66, 58).addBox(0.0F, 7.0F, -3.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(16, 63).addBox(-3.0F, 7.0F, -1.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(20, 63).addBox(-1.0F, 7.0F, 2.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(24, 63).addBox(1.0F, 7.0F, 2.0F, 1.0F, 1.0F, 1.0F)
                , PartPose.offset(-2.0F, 12.0F, 0.0F));
        PartDefinition v_tail_l = v_body.addOrReplaceChild("tail_l", CubeListBuilder.create()
                .texOffs(0, 39).mirror().addBox(-2.0F, 1.0F, -3.0F, 5.0F, 6.0F, 6.0F).mirror(false)
                .texOffs(12, 63).mirror().addBox(1.0F, 7.0F, -3.0F, 1.0F, 2.0F, 1.0F).mirror(false)
                .texOffs(66, 58).mirror().addBox(-1.0F, 7.0F, -3.0F, 1.0F, 3.0F, 1.0F).mirror(false)
                .texOffs(16, 63).mirror().addBox(2.0F, 7.0F, -1.0F, 1.0F, 2.0F, 1.0F).mirror(false)
                .texOffs(20, 63).mirror().addBox(0.0F, 7.0F, 2.0F, 1.0F, 2.0F, 1.0F).mirror(false)
                .texOffs(24, 63).mirror().addBox(-2.0F, 7.0F, 2.0F, 1.0F, 1.0F, 1.0F).mirror(false)
                , PartPose.offset(2.0F, 12.0F, 0.0F));
        PartDefinition v_wing_l = v_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(34, 39).addBox(5.0F, 1.0F, -1.0F, 6.0F, 8.0F, 2.0F)
                .texOffs(0, 0).addBox(4.0F, -13.0F, -1.0F, 1.0F, 21.0F, 2.0F)
                .texOffs(66, 39).addBox(11.0F, 1.0F, -1.0F, 1.0F, 7.0F, 2.0F)
                .texOffs(6, 0).addBox(0.0F, -9.0F, -1.0F, 4.0F, 16.0F, 2.0F)
                .texOffs(102, 58).addBox(12.0F, 6.0F, -1.0F, 1.0F, 1.0F, 2.0F)
                .texOffs(62, 51).addBox(12.0F, 1.0F, -1.0F, 1.0F, 4.0F, 2.0F)
                .texOffs(34, 58).addBox(14.0F, 3.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(8, 58).addBox(13.0F, 1.0F, -1.0F, 1.0F, 3.0F, 2.0F)
                .texOffs(18, 0).addBox(14.0F, -13.0F, -1.0F, 1.0F, 16.0F, 2.0F)
                .texOffs(22, 39).addBox(15.0F, -7.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(56, 23).addBox(5.0F, -13.0F, -1.0F, 5.0F, 14.0F, 2.0F)
                .texOffs(42, 51).addBox(10.0F, -5.0F, -1.0F, 4.0F, 4.0F, 2.0F)
                .texOffs(80, 58).addBox(18.0F, -4.0F, -1.0F, 2.0F, 1.0F, 2.0F)
                .texOffs(42, 58).addBox(16.0F, -6.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(108, 58).addBox(19.0F, -5.0F, -1.0F, 1.0F, 1.0F, 2.0F)
                .texOffs(70, 23).addBox(10.0F, -19.0F, -1.0F, 2.0F, 14.0F, 2.0F)
                .texOffs(114, 58).addBox(17.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F)
                .texOffs(102, 39).addBox(12.0F, -13.0F, -1.0F, 2.0F, 6.0F, 2.0F)
                .texOffs(68, 51).addBox(15.0F, -12.0F, -1.0F, 1.0F, 4.0F, 2.0F)
                .texOffs(50, 58).addBox(2.0F, -11.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(54, 51).addBox(16.0F, -13.0F, -1.0F, 2.0F, 4.0F, 2.0F)
                .texOffs(58, 58).addBox(18.0F, -13.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(14, 58).addBox(6.0F, -15.0F, -1.0F, 4.0F, 2.0F, 2.0F)
                .texOffs(28, 39).addBox(12.0F, -22.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(120, 58).addBox(16.0F, -14.0F, -1.0F, 1.0F, 1.0F, 2.0F)
                .texOffs(72, 39).addBox(13.0F, -21.0F, -1.0F, 1.0F, 7.0F, 2.0F)
                .texOffs(0, 63).addBox(6.0F, -16.0F, -1.0F, 1.0F, 1.0F, 2.0F)
                .texOffs(88, 58).addBox(8.0F, -16.0F, -1.0F, 2.0F, 1.0F, 2.0F)
                .texOffs(6, 63).addBox(8.0F, -17.0F, -1.0F, 1.0F, 1.0F, 2.0F)
                .texOffs(74, 51).addBox(14.0F, -21.0F, -1.0F, 1.0F, 4.0F, 2.0F)
                .texOffs(80, 51).addBox(15.0F, -22.0F, -1.0F, 1.0F, 4.0F, 2.0F)
                , PartPose.offset(3.0F, 2.0F, 6.0F));
        PartDefinition v_wing_r = v_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(34, 39).mirror().addBox(-11.0F, 1.0F, -1.0F, 6.0F, 8.0F, 2.0F).mirror(false)
                .texOffs(0, 0).mirror().addBox(-5.0F, -13.0F, -1.0F, 1.0F, 21.0F, 2.0F).mirror(false)
                .texOffs(66, 39).mirror().addBox(-12.0F, 1.0F, -1.0F, 1.0F, 7.0F, 2.0F).mirror(false)
                .texOffs(6, 0).mirror().addBox(-4.0F, -9.0F, -1.0F, 4.0F, 16.0F, 2.0F).mirror(false)
                .texOffs(102, 58).mirror().addBox(-13.0F, 6.0F, -1.0F, 1.0F, 1.0F, 2.0F).mirror(false)
                .texOffs(62, 51).mirror().addBox(-13.0F, 1.0F, -1.0F, 1.0F, 4.0F, 2.0F).mirror(false)
                .texOffs(34, 58).mirror().addBox(-16.0F, 3.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false)
                .texOffs(8, 58).mirror().addBox(-14.0F, 1.0F, -1.0F, 1.0F, 3.0F, 2.0F).mirror(false)
                .texOffs(18, 0).mirror().addBox(-15.0F, -13.0F, -1.0F, 1.0F, 16.0F, 2.0F).mirror(false)
                .texOffs(22, 39).mirror().addBox(-16.0F, -7.0F, -1.0F, 1.0F, 9.0F, 2.0F).mirror(false)
                .texOffs(56, 23).mirror().addBox(-10.0F, -13.0F, -1.0F, 5.0F, 14.0F, 2.0F).mirror(false)
                .texOffs(42, 51).mirror().addBox(-14.0F, -5.0F, -1.0F, 4.0F, 4.0F, 2.0F).mirror(false)
                .texOffs(80, 58).mirror().addBox(-20.0F, -4.0F, -1.0F, 2.0F, 1.0F, 2.0F).mirror(false)
                .texOffs(42, 58).mirror().addBox(-18.0F, -6.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false)
                .texOffs(108, 58).mirror().addBox(-20.0F, -5.0F, -1.0F, 1.0F, 1.0F, 2.0F).mirror(false)
                .texOffs(70, 23).mirror().addBox(-12.0F, -19.0F, -1.0F, 2.0F, 14.0F, 2.0F).mirror(false)
                .texOffs(114, 58).mirror().addBox(-18.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F).mirror(false)
                .texOffs(102, 39).mirror().addBox(-14.0F, -13.0F, -1.0F, 2.0F, 6.0F, 2.0F).mirror(false)
                .texOffs(68, 51).mirror().addBox(-16.0F, -12.0F, -1.0F, 1.0F, 4.0F, 2.0F).mirror(false)
                .texOffs(50, 58).mirror().addBox(-4.0F, -11.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false)
                .texOffs(54, 51).mirror().addBox(-18.0F, -13.0F, -1.0F, 2.0F, 4.0F, 2.0F).mirror(false)
                .texOffs(58, 58).mirror().addBox(-20.0F, -13.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false)
                .texOffs(14, 58).mirror().addBox(-10.0F, -15.0F, -1.0F, 4.0F, 2.0F, 2.0F).mirror(false)
                .texOffs(28, 39).mirror().addBox(-13.0F, -22.0F, -1.0F, 1.0F, 9.0F, 2.0F).mirror(false)
                .texOffs(120, 58).mirror().addBox(-17.0F, -14.0F, -1.0F, 1.0F, 1.0F, 2.0F).mirror(false)
                .texOffs(72, 39).mirror().addBox(-14.0F, -21.0F, -1.0F, 1.0F, 7.0F, 2.0F).mirror(false)
                .texOffs(0, 63).mirror().addBox(-7.0F, -16.0F, -1.0F, 1.0F, 1.0F, 2.0F).mirror(false)
                .texOffs(88, 58).mirror().addBox(-10.0F, -16.0F, -1.0F, 2.0F, 1.0F, 2.0F).mirror(false)
                .texOffs(6, 63).mirror().addBox(-9.0F, -17.0F, -1.0F, 1.0F, 1.0F, 2.0F).mirror(false)
                .texOffs(74, 51).mirror().addBox(-15.0F, -21.0F, -1.0F, 1.0F, 4.0F, 2.0F).mirror(false)
                .texOffs(80, 51).mirror().addBox(-16.0F, -22.0F, -1.0F, 1.0F, 4.0F, 2.0F).mirror(false)
                , PartPose.offset(-3.0F, 2.0F, 6.0F));
        PartDefinition v_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(40, 23).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                .texOffs(50, 39).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F))
                .texOffs(70, 58).addBox(-2.0F, 10.0F, -3.0F, 4.0F, 2.0F, 1.0F)
                , PartPose.offset(-2.0F, 12.0F, 0.0F));
        PartDefinition v_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(40, 23).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror(false)
                .texOffs(50, 39).mirror().addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false)
                .texOffs(70, 58).mirror().addBox(-2.0F, 10.0F, -3.0F, 4.0F, 2.0F, 1.0F).mirror(false)
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
        int skin = entity.getLoreSkin();
        float wing = Mth.clamp(entity.getWingOpen(), 0.0F, 1.0F);
        int spawn = entity.getSpawnTicks();

        // Emergencia desde el suelo (copias): la figura sube desde los pies, de abajo hacia arriba.
        this.root.y = FIGURE_HEIGHT * Mth.clamp(spawn / 60.0F, 0.0F, 1.0F);

        this.head.yRot = Mth.clamp(netHeadYaw, -75.0F, 75.0F) * DEG;
        this.head.xRot = Mth.clamp(headPitch, -55.0F, 55.0F) * DEG;

        float breath = Mth.sin(ageInTicks * 0.07F);
        float sway = Mth.sin(ageInTicks * 0.08F);
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = swing;
        this.leftLeg.xRot = -swing;
        this.rightArm.xRot = -swing * 0.6F;
        this.leftArm.xRot = swing * 0.6F;
        this.rightArm.zRot = 0.09F + breath * 0.025F;
        this.leftArm.zRot = -0.09F - breath * 0.025F;
        this.body.y = breath * 0.25F;
        // Los faldones del abrigo se abren hacia fuera y se mecen con el paso y con el viento de su propia energía
        float flare = 0.12F + 0.03F * sway;
        this.tailR.zRot = flare;
        this.tailL.zRot = -flare;
        this.tailR.xRot = 0.04F + sway * 0.03F + swing * 0.5F;
        this.tailL.xRot = 0.04F - sway * 0.03F - swing * 0.5F;

        float flapBoost = 1.0F;
        float sweepBack = 0.0F;
        if (entity.isLoreDormant() || pose == LooreVisual.POSE_SIT) {
            this.applySit();
        } else {
            switch (pose) {
                case LooreVisual.POSE_CAST -> {
                    this.rightArm.xRot = -1.25F;
                    this.leftArm.xRot = -1.05F;
                    this.rightArm.zRot = -0.10F;
                    this.leftArm.zRot = 0.12F;
                    this.body.xRot = 0.05F;
                }
                case LooreVisual.POSE_SPREAD -> {
                    this.rightArm.xRot = -0.25F;
                    this.leftArm.xRot = -0.25F;
                    this.rightArm.zRot = 1.25F;
                    this.leftArm.zRot = -1.25F;
                    this.head.xRot -= 0.15F;
                }
                case LooreVisual.POSE_FLY -> {
                    this.body.xRot = 0.3F;
                    this.head.xRot -= 0.3F;
                    this.rightArm.xRot = 0.35F;
                    this.leftArm.xRot = 0.35F;
                    this.rightArm.zRot = 0.45F;
                    this.leftArm.zRot = -0.45F;
                    this.rightLeg.xRot = 0.5F;
                    this.leftLeg.xRot = 0.35F;
                    this.tailR.xRot = 0.9F;
                    this.tailL.xRot = 0.9F;
                    flapBoost = 2.4F;
                    sweepBack = 0.18F;
                }
                case LooreVisual.POSE_CHARGE -> {
                    this.joinArms(-1.4F, 0.38F);
                    this.body.xRot = 0.1F;
                    this.body.yRot = Mth.sin(ageInTicks * 1.7F) * 0.03F;
                }
                case LooreVisual.POSE_FIRE -> {
                    this.joinArms(-1.5F, 0.3F);
                    this.body.xRot = -0.2F;
                }
                case LooreVisual.POSE_SUMMON -> {
                    this.rightArm.xRot = -2.55F;
                    this.leftArm.xRot = -2.55F;
                    this.rightArm.zRot = 0.3F;
                    this.leftArm.zRot = -0.3F;
                    this.head.xRot -= 0.3F;
                }
                default -> {
                    // POSE_IDLE: ya aplicada arriba
                }
            }
        }

        // Alas de código (solo Kein Loree en su forma final): plegadas (wing=0) o desplegadas (wing=1)
        boolean winged = skin == 0 && (entity.getLoreForm() == 1 || wing > 0.0F);
        this.wingL.visible = winged;
        this.wingR.visible = winged;
        if (winged) {
            float fold = 1.0F - wing;
            float flap = Mth.sin(ageInTicks * 0.22F) * (0.05F + 0.10F * wing) * flapBoost;
            float sx = 0.16F + 0.84F * wing;
            float sy = 0.62F + 0.38F * wing;
            this.wingL.xScale = sx;
            this.wingR.xScale = sx;
            this.wingL.yScale = sy;
            this.wingR.yScale = sy;
            float sweep = 0.26F * wing - 0.55F * fold - sweepBack;
            this.wingL.yRot = sweep;
            this.wingR.yRot = -sweep;
            this.wingL.zRot = -flap;
            this.wingR.zRot = flap;
        }

        // Elementos propios de cada variante
        boolean commander = skin == 1;
        boolean warlord = skin == 2;
        this.epauL.visible = commander;
        this.epauR.visible = commander;
        this.pauldL.visible = warlord;
        this.pauldR.visible = warlord;
        this.hornsL.visible = warlord;
        this.hornsR.visible = warlord;
    }

    private void joinArms(float xRot, float inward) {
        this.rightArm.xRot = xRot;
        this.leftArm.xRot = xRot;
        this.rightArm.yRot = -inward;
        this.leftArm.yRot = inward;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
    }

    private void applySit() {
        this.rightLeg.xRot = -1.5F;
        this.leftLeg.xRot = -1.5F;
        this.body.xRot = 0.04F;
        this.rightArm.xRot = -0.35F;
        this.leftArm.xRot = -0.35F;
        this.rightArm.zRot = 0.15F;
        this.leftArm.zRot = -0.15F;
        this.tailR.xRot = -1.0F;
        this.tailL.xRot = -1.0F;
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
        this.tailL.zRot = 0.0F;
        this.tailR.zRot = 0.0F;
        this.wingL.xScale = 1.0F;
        this.wingR.xScale = 1.0F;
        this.wingL.yScale = 1.0F;
        this.wingR.yScale = 1.0F;
        this.wingL.xRot = 0.0F;
        this.wingR.xRot = 0.0F;
        this.wingL.yRot = 0.0F;
        this.wingR.yRot = 0.0F;
        this.wingL.zRot = 0.0F;
        this.wingR.zRot = 0.0F;
    }
}
