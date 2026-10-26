package com.keinloree.client.model.gen;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
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
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

/** GENERADO por bb2java.mjs desde blockbench/medianoche.bbmodel. No editar a mano: edita la spec y regenera. */
public class MedianocheModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("keinloree", "medianoche"), "main");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/medianoche.png");
    public static final ResourceLocation GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/medianoche_glow.png");
    private static final Vector3f CACHE = new Vector3f();

    private final ModelPart layerRoot;
    public final ModelPart rootBone;
    public final ModelPart body;
    public final ModelPart head;
    public final ModelPart halo;
    public final ModelPart rightArm;
    public final ModelPart weapon;
    public final ModelPart ring1;
    public final ModelPart ring2;
    public final ModelPart ring3;
    public final ModelPart leftArm;
    public final ModelPart cape;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;

    public MedianocheModel(ModelPart layerRoot) {
        this.layerRoot = layerRoot;
        this.rootBone = layerRoot.getChild("root");
        this.body = this.rootBone.getChild("body");
        this.head = this.body.getChild("head");
        this.halo = this.head.getChild("halo");
        this.rightArm = this.body.getChild("right_arm");
        this.weapon = this.rightArm.getChild("weapon");
        this.ring1 = this.weapon.getChild("ring_1");
        this.ring2 = this.weapon.getChild("ring_2");
        this.ring3 = this.weapon.getChild("ring_3");
        this.leftArm = this.body.getChild("left_arm");
        this.cape = this.body.getChild("cape");
        this.rightLeg = this.rootBone.getChild("right_leg");
        this.leftLeg = this.rootBone.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition pd_rootBone = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pd_body = pd_rootBone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(56, 0).addBox(-3.5F, -12.0F, -2.25F, 7.0F, 12.0F, 4.5F).texOffs(36, 46).addBox(-3.0F, -10.68F, -2.9F, 6.0F, 5.04F, 5.0F, new CubeDeformation(0.25F)).texOffs(0, 67).addBox(-3.7F, -2.0F, -2.45F, 7.4F, 2.0F, 4.9F).texOffs(26, 0).addBox(-4.2F, 0.0F, -2.7F, 8.4F, 12.0F, 5.4F).texOffs(0, 58).addBox(-4.7F, 10.9F, -3.2F, 9.4F, 1.4F, 6.4F), PartPose.offset(0.0F, -13.0F, 0.0F));
        pd_body.addOrReplaceChild("cube_gem", CubeListBuilder.create().texOffs(8, 78).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -7.92F, -2.9F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_body.addOrReplaceChild("cube_star_0", CubeListBuilder.create().texOffs(26, 78).addBox(-0.4F, -0.4F, -0.15F, 0.8F, 0.8F, 0.3F), PartPose.offsetAndRotation(2.5F, -4.0F, -2.85F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_body.addOrReplaceChild("cube_star_1", CubeListBuilder.create().texOffs(30, 78).addBox(-0.4F, -0.4F, -0.15F, 0.8F, 0.8F, 0.3F), PartPose.offsetAndRotation(-2.0F, -7.0F, -2.85F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_body.addOrReplaceChild("cube_star_2", CubeListBuilder.create().texOffs(34, 78).addBox(-0.4F, -0.4F, -0.15F, 0.8F, 0.8F, 0.3F), PartPose.offsetAndRotation(1.0F, -10.0F, -2.85F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_body.addOrReplaceChild("cube_star_3", CubeListBuilder.create().texOffs(38, 78).addBox(-0.4F, -0.4F, -0.15F, 0.8F, 0.8F, 0.3F), PartPose.offsetAndRotation(-3.0F, -2.0F, -2.85F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_body.addOrReplaceChild("cube_star_4", CubeListBuilder.create().texOffs(42, 78).addBox(-0.4F, -0.4F, -0.15F, 0.8F, 0.8F, 0.3F), PartPose.offsetAndRotation(-0.5F, 4.0F, -2.85F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_body.addOrReplaceChild("cube_star_5", CubeListBuilder.create().texOffs(46, 78).addBox(-0.4F, -0.4F, -0.15F, 0.8F, 0.8F, 0.3F), PartPose.offsetAndRotation(3.0F, 7.0F, -2.85F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_body.addOrReplaceChild("cube_star_6", CubeListBuilder.create().texOffs(50, 78).addBox(-0.4F, -0.4F, -0.15F, 0.8F, 0.8F, 0.3F), PartPose.offsetAndRotation(-2.5F, 9.0F, -2.85F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        PartDefinition pd_head = pd_body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 30).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F).texOffs(76, 30).addBox(-4.3F, -8.1F, -3.5F, 8.6F, 3.4F, 8.6F).texOffs(32, 30).addBox(-4.0F, -5.5F, 2.8F, 8.0F, 13.0F, 1.6F).texOffs(58, 46).addBox(-4.6F, -8.6F, -2.1F, 9.2F, 2.0F, 7.4F).texOffs(14, 78).addBox(-2.5F, -4.6F, -4.15F, 1.4F, 0.8F, 0.2F).texOffs(20, 78).addBox(1.1F, -4.6F, -4.15F, 1.4F, 0.8F, 0.2F), PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition pd_halo = pd_head.addOrReplaceChild("halo", CubeListBuilder.create().texOffs(70, 67).addBox(-6.3F, -1.7577F, -0.3F, 0.6F, 3.5154F, 0.6F), PartPose.offset(0.0F, -5.0F, 6.0F));
        pd_halo.addOrReplaceChild("cube_halo_1", CubeListBuilder.create().texOffs(74, 67).addBox(-0.3F, -1.7577F, -0.3F, 0.6F, 3.5154F, 0.6F), PartPose.offsetAndRotation(-5.1961F, -3.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(30.0F)));
        pd_halo.addOrReplaceChild("cube_halo_2", CubeListBuilder.create().texOffs(78, 67).addBox(-0.3F, -1.7577F, -0.3F, 0.6F, 3.5154F, 0.6F), PartPose.offsetAndRotation(-3.0F, -5.1962F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(60.0F)));
        pd_halo.addOrReplaceChild("cube_halo_3", CubeListBuilder.create().texOffs(82, 67).addBox(-0.3F, -1.7577F, -0.3F, 0.6F, 3.5154F, 0.6F), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(90.0F)));
        pd_halo.addOrReplaceChild("cube_halo_4", CubeListBuilder.create().texOffs(86, 67).addBox(-0.3F, -1.7577F, -0.3F, 0.6F, 3.5154F, 0.6F), PartPose.offsetAndRotation(3.0F, -5.1962F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(120.0F)));
        pd_halo.addOrReplaceChild("cube_halo_5", CubeListBuilder.create().texOffs(90, 67).addBox(-0.3F, -1.7577F, -0.3F, 0.6F, 3.5154F, 0.6F), PartPose.offsetAndRotation(5.1962F, -3.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(150.0F)));
        pd_halo.addOrReplaceChild("cube_halo_6", CubeListBuilder.create().texOffs(94, 67).addBox(-0.3F, -1.7577F, -0.3F, 0.6F, 3.5154F, 0.6F), PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(180.0F)));
        pd_halo.addOrReplaceChild("cube_halo_7", CubeListBuilder.create().texOffs(98, 67).addBox(-0.3F, -1.7577F, -0.3F, 0.6F, 3.5154F, 0.6F), PartPose.offsetAndRotation(5.1962F, 3.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(210.0F)));
        pd_halo.addOrReplaceChild("cube_halo_8", CubeListBuilder.create().texOffs(102, 67).addBox(-0.3F, -1.7577F, -0.3F, 0.6F, 3.5154F, 0.6F), PartPose.offsetAndRotation(3.0F, 5.1961F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(240.0F)));
        pd_halo.addOrReplaceChild("cube_halo_9", CubeListBuilder.create().texOffs(106, 67).addBox(-0.3F, -1.7577F, -0.3F, 0.6F, 3.5154F, 0.6F), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(270.0F)));
        pd_halo.addOrReplaceChild("cube_halo_10", CubeListBuilder.create().texOffs(110, 67).addBox(-0.3F, -1.7577F, -0.3F, 0.6F, 3.5154F, 0.6F), PartPose.offsetAndRotation(-3.0F, 5.1961F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(300.0F)));
        pd_halo.addOrReplaceChild("cube_halo_11", CubeListBuilder.create().texOffs(114, 67).addBox(-0.3F, -1.7577F, -0.3F, 0.6F, 3.5154F, 0.6F), PartPose.offsetAndRotation(-5.1961F, 3.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(330.0F)));
        PartDefinition pd_rightArm = pd_body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(52, 30).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 12.0F, 3.0F).texOffs(26, 67).addBox(-1.6F, 8.5F, -1.6F, 3.2F, 3.0F, 3.2F, new CubeDeformation(0.2F)).texOffs(34, 58).addBox(-3.0F, -2.0F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.offset(-5.0F, -10.5F, 0.0F));
        pd_rightArm.addOrReplaceChild("cube_moon_r", CubeListBuilder.create().texOffs(94, 46).addBox(-0.6F, -2.0F, -3.0F, 1.2F, 4.0F, 6.0F), PartPose.offsetAndRotation(-1.6F, -1.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(-20.0F)));
        PartDefinition pd_weapon = pd_rightArm.addOrReplaceChild("weapon", CubeListBuilder.create().texOffs(0, 0).addBox(-0.7F, -22.0F, -0.7F, 1.4F, 28.0F, 1.4F).texOffs(58, 67).addBox(-1.3F, -23.3F, -1.3F, 2.6F, 2.6F, 2.6F), PartPose.offset(0.0F, 11.5F, 0.0F));
        PartDefinition pd_ring1 = pd_weapon.addOrReplaceChild("ring_1", CubeListBuilder.create().texOffs(118, 67).addBox(-3.45F, -1.1897F, -0.25F, 0.5F, 2.3795F, 0.5F), PartPose.offset(0.0F, -22.0F, 0.0F));
        pd_ring1.addOrReplaceChild("cube_r1_1", CubeListBuilder.create().texOffs(122, 67).addBox(-0.25F, -1.1897F, -0.25F, 0.5F, 2.3795F, 0.5F), PartPose.offsetAndRotation(-2.5888F, -1.8809F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(36.0F)));
        pd_ring1.addOrReplaceChild("cube_r1_2", CubeListBuilder.create().texOffs(0, 74).addBox(-0.25F, -1.1897F, -0.25F, 0.5F, 2.3795F, 0.5F), PartPose.offsetAndRotation(-0.9889F, -3.0434F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(72.0F)));
        pd_ring1.addOrReplaceChild("cube_r1_3", CubeListBuilder.create().texOffs(4, 74).addBox(-0.25F, -1.1897F, -0.25F, 0.5F, 2.3795F, 0.5F), PartPose.offsetAndRotation(0.9889F, -3.0434F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(108.0F)));
        pd_ring1.addOrReplaceChild("cube_r1_4", CubeListBuilder.create().texOffs(8, 74).addBox(-0.25F, -1.1897F, -0.25F, 0.5F, 2.3795F, 0.5F), PartPose.offsetAndRotation(2.5889F, -1.8809F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(144.0F)));
        pd_ring1.addOrReplaceChild("cube_r1_5", CubeListBuilder.create().texOffs(12, 74).addBox(-0.25F, -1.1897F, -0.25F, 0.5F, 2.3795F, 0.5F), PartPose.offsetAndRotation(3.2F, 0.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(180.0F)));
        pd_ring1.addOrReplaceChild("cube_r1_6", CubeListBuilder.create().texOffs(16, 74).addBox(-0.25F, -1.1897F, -0.25F, 0.5F, 2.3795F, 0.5F), PartPose.offsetAndRotation(2.5889F, 1.8809F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(216.0F)));
        pd_ring1.addOrReplaceChild("cube_r1_7", CubeListBuilder.create().texOffs(20, 74).addBox(-0.25F, -1.1897F, -0.25F, 0.5F, 2.3795F, 0.5F), PartPose.offsetAndRotation(0.9889F, 3.0434F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(252.0F)));
        pd_ring1.addOrReplaceChild("cube_r1_8", CubeListBuilder.create().texOffs(24, 74).addBox(-0.25F, -1.1897F, -0.25F, 0.5F, 2.3795F, 0.5F), PartPose.offsetAndRotation(-0.9889F, 3.0434F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(288.0F)));
        pd_ring1.addOrReplaceChild("cube_r1_9", CubeListBuilder.create().texOffs(28, 74).addBox(-0.25F, -1.1897F, -0.25F, 0.5F, 2.3795F, 0.5F), PartPose.offsetAndRotation(-2.5888F, 1.8809F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(324.0F)));
        PartDefinition pd_ring2 = pd_weapon.addOrReplaceChild("ring_2", CubeListBuilder.create().texOffs(32, 74).addBox(-4.45F, -1.2754F, -0.25F, 0.5F, 2.5508F, 0.5F), PartPose.offset(0.0F, -22.0F, 0.0F));
        pd_ring2.addOrReplaceChild("cube_r2_1", CubeListBuilder.create().texOffs(36, 74).addBox(-0.25F, -1.2754F, -0.25F, 0.5F, 2.5508F, 0.5F), PartPose.offsetAndRotation(-3.6373F, -2.1F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(30.0F)));
        pd_ring2.addOrReplaceChild("cube_r2_2", CubeListBuilder.create().texOffs(40, 74).addBox(-0.25F, -1.2754F, -0.25F, 0.5F, 2.5508F, 0.5F), PartPose.offsetAndRotation(-2.1F, -3.6373F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(60.0F)));
        pd_ring2.addOrReplaceChild("cube_r2_3", CubeListBuilder.create().texOffs(44, 74).addBox(-0.25F, -1.2754F, -0.25F, 0.5F, 2.5508F, 0.5F), PartPose.offsetAndRotation(0.0F, -4.2F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(90.0F)));
        pd_ring2.addOrReplaceChild("cube_r2_4", CubeListBuilder.create().texOffs(48, 74).addBox(-0.25F, -1.2754F, -0.25F, 0.5F, 2.5508F, 0.5F), PartPose.offsetAndRotation(2.1F, -3.6373F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(120.0F)));
        pd_ring2.addOrReplaceChild("cube_r2_5", CubeListBuilder.create().texOffs(52, 74).addBox(-0.25F, -1.2754F, -0.25F, 0.5F, 2.5508F, 0.5F), PartPose.offsetAndRotation(3.6373F, -2.1F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(150.0F)));
        pd_ring2.addOrReplaceChild("cube_r2_6", CubeListBuilder.create().texOffs(56, 74).addBox(-0.25F, -1.2754F, -0.25F, 0.5F, 2.5508F, 0.5F), PartPose.offsetAndRotation(4.2F, 0.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(180.0F)));
        pd_ring2.addOrReplaceChild("cube_r2_7", CubeListBuilder.create().texOffs(60, 74).addBox(-0.25F, -1.2754F, -0.25F, 0.5F, 2.5508F, 0.5F), PartPose.offsetAndRotation(3.6373F, 2.1F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(210.0F)));
        pd_ring2.addOrReplaceChild("cube_r2_8", CubeListBuilder.create().texOffs(64, 74).addBox(-0.25F, -1.2754F, -0.25F, 0.5F, 2.5508F, 0.5F), PartPose.offsetAndRotation(2.1F, 3.6373F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(240.0F)));
        pd_ring2.addOrReplaceChild("cube_r2_9", CubeListBuilder.create().texOffs(68, 74).addBox(-0.25F, -1.2754F, -0.25F, 0.5F, 2.5508F, 0.5F), PartPose.offsetAndRotation(0.0F, 4.2F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(270.0F)));
        pd_ring2.addOrReplaceChild("cube_r2_10", CubeListBuilder.create().texOffs(72, 74).addBox(-0.25F, -1.2754F, -0.25F, 0.5F, 2.5508F, 0.5F), PartPose.offsetAndRotation(-2.1F, 3.6373F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(300.0F)));
        pd_ring2.addOrReplaceChild("cube_r2_11", CubeListBuilder.create().texOffs(76, 74).addBox(-0.25F, -1.2754F, -0.25F, 0.5F, 2.5508F, 0.5F), PartPose.offsetAndRotation(-3.6373F, 2.1F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(330.0F)));
        PartDefinition pd_ring3 = pd_weapon.addOrReplaceChild("ring_3", CubeListBuilder.create().texOffs(80, 74).addBox(-5.45F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offset(0.0F, -22.0F, 0.0F));
        pd_ring3.addOrReplaceChild("cube_r3_1", CubeListBuilder.create().texOffs(84, 74).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(-4.685F, -2.2562F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(25.7143F)));
        pd_ring3.addOrReplaceChild("cube_r3_2", CubeListBuilder.create().texOffs(88, 74).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(-3.2422F, -4.0655F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(51.4286F)));
        pd_ring3.addOrReplaceChild("cube_r3_3", CubeListBuilder.create().texOffs(92, 74).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(-1.1571F, -5.0696F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(77.1429F)));
        pd_ring3.addOrReplaceChild("cube_r3_4", CubeListBuilder.create().texOffs(96, 74).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(1.1571F, -5.0696F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(102.8571F)));
        pd_ring3.addOrReplaceChild("cube_r3_5", CubeListBuilder.create().texOffs(100, 74).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(3.2422F, -4.0655F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(128.5714F)));
        pd_ring3.addOrReplaceChild("cube_r3_6", CubeListBuilder.create().texOffs(104, 74).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(4.685F, -2.2562F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(154.2857F)));
        pd_ring3.addOrReplaceChild("cube_r3_7", CubeListBuilder.create().texOffs(108, 74).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(5.2F, 0.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(180.0F)));
        pd_ring3.addOrReplaceChild("cube_r3_8", CubeListBuilder.create().texOffs(112, 74).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(4.685F, 2.2562F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(205.7143F)));
        pd_ring3.addOrReplaceChild("cube_r3_9", CubeListBuilder.create().texOffs(116, 74).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(3.2422F, 4.0655F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(231.4286F)));
        pd_ring3.addOrReplaceChild("cube_r3_10", CubeListBuilder.create().texOffs(120, 74).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(1.1571F, 5.0696F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(257.1429F)));
        pd_ring3.addOrReplaceChild("cube_r3_11", CubeListBuilder.create().texOffs(124, 74).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(-1.1571F, 5.0696F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(282.8571F)));
        pd_ring3.addOrReplaceChild("cube_r3_12", CubeListBuilder.create().texOffs(0, 78).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(-3.2422F, 4.0655F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(308.5714F)));
        pd_ring3.addOrReplaceChild("cube_r3_13", CubeListBuilder.create().texOffs(4, 78).addBox(-0.25F, -1.3369F, -0.25F, 0.5F, 2.6737F, 0.5F), PartPose.offsetAndRotation(-4.685F, 2.2562F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(334.2857F)));
        PartDefinition pd_leftArm = pd_body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(64, 30).mirror().addBox(-1.5F, -0.5F, -1.5F, 3.0F, 12.0F, 3.0F).mirror(false).texOffs(42, 67).mirror().addBox(-1.6F, 8.5F, -1.6F, 3.2F, 3.0F, 3.2F, new CubeDeformation(0.2F)).mirror(false).texOffs(54, 58).mirror().addBox(-2.0F, -2.0F, -2.5F, 5.0F, 3.0F, 5.0F).mirror(false), PartPose.offset(5.0F, -10.5F, 0.0F));
        pd_leftArm.addOrReplaceChild("cube_moon_l", CubeListBuilder.create().texOffs(110, 46).mirror().addBox(-0.6F, -2.0F, -3.0F, 1.2F, 4.0F, 6.0F).mirror(false), PartPose.offsetAndRotation(1.6F, -1.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(20.0F)));
        PartDefinition pd_cape = pd_body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(8, 0).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 18.0F, 0.8F), PartPose.offset(0.0F, -12.0F, 2.6F));
        PartDefinition pd_rightLeg = pd_rootBone.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(80, 0).addBox(-1.75F, 0.0F, -2.0F, 3.5F, 13.0F, 4.0F).texOffs(0, 46).addBox(-1.95F, 5.2F, -2.5F, 3.9F, 6.5F, 4.4F).texOffs(74, 58).addBox(-2.05F, 10.0F, -2.8F, 4.1F, 3.0F, 4.8F), PartPose.offset(-1.75F, -13.0F, 0.0F));
        PartDefinition pd_leftLeg = pd_rootBone.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(96, 0).mirror().addBox(-1.75F, 0.0F, -2.0F, 3.5F, 13.0F, 4.0F).mirror(false).texOffs(18, 46).mirror().addBox(-1.95F, 5.2F, -2.5F, 3.9F, 6.5F, 4.4F).mirror(false).texOffs(94, 58).mirror().addBox(-2.05F, 10.0F, -2.8F, 4.1F, 3.0F, 4.8F).mirror(false), PartPose.offset(1.75F, -13.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.layerRoot;
    }

    /** Vuelve todas las partes a su pose de reposo (llamar antes de aplicar animaciones). */
    public void resetPose() {
        this.layerRoot.getAllParts().forEach(ModelPart::resetPose);
    }

    /** Aplica una animacion en el instante dado (ms); acumula sobre la pose actual. */
    public void applyAnim(AnimationDefinition definition, long timeMs) {
        KeyframeAnimations.animate(this, definition, timeMs, 1.0F, CACHE);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // El renderer concreto decide que animacion aplicar
    }
}
