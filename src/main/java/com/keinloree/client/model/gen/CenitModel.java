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

/** GENERADO por bb2java.mjs desde blockbench/cenit.bbmodel. No editar a mano: edita la spec y regenera. */
public class CenitModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("keinloree", "cenit"), "main");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/cenit.png");
    public static final ResourceLocation GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/cenit_glow.png");
    private static final Vector3f CACHE = new Vector3f();

    private final ModelPart layerRoot;
    public final ModelPart rootBone;
    public final ModelPart body;
    public final ModelPart head;
    public final ModelPart halo;
    public final ModelPart rightArm;
    public final ModelPart weapon;
    public final ModelPart leftArm;
    public final ModelPart cape;
    public final ModelPart sunDisk;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;

    public CenitModel(ModelPart layerRoot) {
        this.layerRoot = layerRoot;
        this.rootBone = layerRoot.getChild("root");
        this.body = this.rootBone.getChild("body");
        this.head = this.body.getChild("head");
        this.halo = this.head.getChild("halo");
        this.rightArm = this.body.getChild("right_arm");
        this.weapon = this.rightArm.getChild("weapon");
        this.leftArm = this.body.getChild("left_arm");
        this.cape = this.body.getChild("cape");
        this.sunDisk = this.body.getChild("sun_disk");
        this.rightLeg = this.rootBone.getChild("right_leg");
        this.leftLeg = this.rootBone.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition pd_rootBone = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pd_body = pd_rootBone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(48, 0).addBox(-5.0F, -14.0F, -2.25F, 10.0F, 14.0F, 4.5F).texOffs(32, 53).addBox(-4.5F, -12.46F, -2.9F, 9.0F, 5.88F, 5.0F, new CubeDeformation(0.25F)).texOffs(60, 74).addBox(-5.2F, -2.0F, -2.45F, 10.4F, 2.0F, 4.9F).texOffs(92, 74).addBox(-4.0F, -6.5F, -3.1F, 8.0F, 5.0F, 1.0F).texOffs(118, 82).addBox(-1.5F, -2.2F, -3.0F, 3.0F, 2.4F, 0.6F), PartPose.offset(0.0F, -15.0F, 0.0F));
        pd_body.addOrReplaceChild("cube_gem", CubeListBuilder.create().texOffs(0, 88).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -9.24F, -2.9F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        PartDefinition pd_head = pd_body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 24).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F).texOffs(0, 40).addBox(-4.4F, -8.1F, -4.4F, 8.8F, 3.4F, 8.8F).texOffs(104, 53).addBox(-0.6F, -10.5F, -4.0F, 1.2F, 3.0F, 8.0F).texOffs(32, 82).addBox(-3.3F, -2.8F, -4.5F, 6.6F, 3.0F, 1.4F).texOffs(6, 88).addBox(-2.5F, -4.8F, -4.15F, 1.4F, 1.0F, 0.2F).texOffs(12, 88).addBox(1.1F, -4.8F, -4.15F, 1.4F, 1.0F, 0.2F), PartPose.offset(0.0F, -14.0F, 0.0F));
        PartDefinition pd_halo = pd_head.addOrReplaceChild("halo", CubeListBuilder.create().texOffs(70, 82).addBox(-5.9F, -1.6237F, -0.4F, 0.8F, 3.2474F, 0.8F), PartPose.offset(0.0F, -5.0F, 6.0F));
        pd_halo.addOrReplaceChild("cube_halo_1", CubeListBuilder.create().texOffs(74, 82).addBox(-0.4F, -1.6237F, -0.4F, 0.8F, 3.2474F, 0.8F), PartPose.offsetAndRotation(-4.7631F, -2.75F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(30.0F)));
        pd_halo.addOrReplaceChild("cube_halo_2", CubeListBuilder.create().texOffs(78, 82).addBox(-0.4F, -1.6237F, -0.4F, 0.8F, 3.2474F, 0.8F), PartPose.offsetAndRotation(-2.75F, -4.7631F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(60.0F)));
        pd_halo.addOrReplaceChild("cube_halo_3", CubeListBuilder.create().texOffs(82, 82).addBox(-0.4F, -1.6237F, -0.4F, 0.8F, 3.2474F, 0.8F), PartPose.offsetAndRotation(0.0F, -5.5F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(90.0F)));
        pd_halo.addOrReplaceChild("cube_halo_4", CubeListBuilder.create().texOffs(86, 82).addBox(-0.4F, -1.6237F, -0.4F, 0.8F, 3.2474F, 0.8F), PartPose.offsetAndRotation(2.75F, -4.7631F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(120.0F)));
        pd_halo.addOrReplaceChild("cube_halo_5", CubeListBuilder.create().texOffs(90, 82).addBox(-0.4F, -1.6237F, -0.4F, 0.8F, 3.2474F, 0.8F), PartPose.offsetAndRotation(4.7631F, -2.75F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(150.0F)));
        pd_halo.addOrReplaceChild("cube_halo_6", CubeListBuilder.create().texOffs(94, 82).addBox(-0.4F, -1.6237F, -0.4F, 0.8F, 3.2474F, 0.8F), PartPose.offsetAndRotation(5.5F, 0.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(180.0F)));
        pd_halo.addOrReplaceChild("cube_halo_7", CubeListBuilder.create().texOffs(98, 82).addBox(-0.4F, -1.6237F, -0.4F, 0.8F, 3.2474F, 0.8F), PartPose.offsetAndRotation(4.7631F, 2.75F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(210.0F)));
        pd_halo.addOrReplaceChild("cube_halo_8", CubeListBuilder.create().texOffs(102, 82).addBox(-0.4F, -1.6237F, -0.4F, 0.8F, 3.2474F, 0.8F), PartPose.offsetAndRotation(2.75F, 4.7631F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(240.0F)));
        pd_halo.addOrReplaceChild("cube_halo_9", CubeListBuilder.create().texOffs(106, 82).addBox(-0.4F, -1.6237F, -0.4F, 0.8F, 3.2474F, 0.8F), PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(270.0F)));
        pd_halo.addOrReplaceChild("cube_halo_10", CubeListBuilder.create().texOffs(110, 82).addBox(-0.4F, -1.6237F, -0.4F, 0.8F, 3.2474F, 0.8F), PartPose.offsetAndRotation(-2.75F, 4.7631F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(300.0F)));
        pd_halo.addOrReplaceChild("cube_halo_11", CubeListBuilder.create().texOffs(114, 82).addBox(-0.4F, -1.6237F, -0.4F, 0.8F, 3.2474F, 0.8F), PartPose.offsetAndRotation(-4.7631F, 2.75F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(330.0F)));
        PartDefinition pd_rightArm = pd_body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(62, 24).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 12.0F, 4.0F).texOffs(0, 74).addBox(-2.1F, 8.5F, -2.1F, 4.2F, 3.0F, 4.2F, new CubeDeformation(0.2F)).texOffs(18, 64).addBox(-3.5F, -2.0F, -3.0F, 6.0F, 3.0F, 6.0F).texOffs(76, 40).addBox(-4.7F, -2.7F, -3.7F, 7.4F, 2.6F, 7.4F), PartPose.offset(-7.0F, -12.5F, 0.0F));
        PartDefinition pd_weapon = pd_rightArm.addOrReplaceChild("weapon", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8F, -0.8F, -21.0F, 1.6F, 1.6F, 22.0F).texOffs(32, 24).addBox(-3.0F, -3.5F, -25.5F, 6.0F, 7.0F, 9.0F).texOffs(0, 64).addBox(-3.2F, -3.7F, -22.0F, 6.4F, 7.4F, 2.0F).texOffs(60, 82).addBox(-1.2F, -1.2F, 0.8F, 2.4F, 2.4F, 1.6F), PartPose.offset(0.0F, 13.5F, 0.0F));
        pd_weapon.addOrReplaceChild("cube_head_sun_r", CubeListBuilder.create().texOffs(40, 74).addBox(-0.2F, -2.0F, -2.0F, 0.4F, 4.0F, 4.0F), PartPose.offsetAndRotation(-3.3F, 0.0F, -21.0F, (float) Math.toRadians(-45.0F), 0.0F, 0.0F));
        pd_weapon.addOrReplaceChild("cube_head_sun_l", CubeListBuilder.create().texOffs(50, 74).addBox(-0.2F, -2.0F, -2.0F, 0.4F, 4.0F, 4.0F), PartPose.offsetAndRotation(3.3F, 0.0F, -21.0F, (float) Math.toRadians(-45.0F), 0.0F, 0.0F));
        PartDefinition pd_leftArm = pd_body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(78, 24).mirror().addBox(-2.0F, -0.5F, -2.0F, 4.0F, 12.0F, 4.0F).mirror(false).texOffs(20, 74).mirror().addBox(-2.1F, 8.5F, -2.1F, 4.2F, 3.0F, 4.2F, new CubeDeformation(0.2F)).mirror(false).texOffs(42, 64).mirror().addBox(-2.5F, -2.0F, -3.0F, 6.0F, 3.0F, 6.0F).mirror(false).texOffs(0, 53).mirror().addBox(-2.7F, -2.7F, -3.7F, 7.4F, 2.6F, 7.4F).mirror(false), PartPose.offset(7.0F, -12.5F, 0.0F));
        PartDefinition pd_cape = pd_body.addOrReplaceChild("cape", CubeListBuilder.create(), PartPose.offset(0.0F, -14.0F, 2.6F));
        PartDefinition pd_sunDisk = pd_body.addOrReplaceChild("sun_disk", CubeListBuilder.create().texOffs(82, 53).addBox(-5.0F, -5.0F, -0.3F, 10.0F, 10.0F, 0.8F).texOffs(110, 74).addBox(-7.9F, -2.1596F, 0.2F, 0.8F, 4.3192F, 0.8F), PartPose.offset(0.0F, -9.0F, 4.5F));
        pd_sunDisk.addOrReplaceChild("cube_disk", CubeListBuilder.create().texOffs(60, 53).addBox(-5.0F, -5.0F, -0.4F, 10.0F, 10.0F, 0.8F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_sunDisk.addOrReplaceChild("cube_disk_core", CubeListBuilder.create().texOffs(50, 82).addBox(-2.0F, -2.0F, -0.3F, 4.0F, 4.0F, 0.6F), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_sunDisk.addOrReplaceChild("cube_disk_ring_1", CubeListBuilder.create().texOffs(114, 74).addBox(-0.4F, -2.1596F, -0.4F, 0.8F, 4.3192F, 0.8F), PartPose.offsetAndRotation(-6.4952F, -3.75F, 0.6F, 0.0F, 0.0F, (float) Math.toRadians(30.0F)));
        pd_sunDisk.addOrReplaceChild("cube_disk_ring_2", CubeListBuilder.create().texOffs(118, 74).addBox(-0.4F, -2.1596F, -0.4F, 0.8F, 4.3192F, 0.8F), PartPose.offsetAndRotation(-3.75F, -6.4952F, 0.6F, 0.0F, 0.0F, (float) Math.toRadians(60.0F)));
        pd_sunDisk.addOrReplaceChild("cube_disk_ring_3", CubeListBuilder.create().texOffs(122, 74).addBox(-0.4F, -2.1596F, -0.4F, 0.8F, 4.3192F, 0.8F), PartPose.offsetAndRotation(0.0F, -7.5F, 0.6F, 0.0F, 0.0F, (float) Math.toRadians(90.0F)));
        pd_sunDisk.addOrReplaceChild("cube_disk_ring_4", CubeListBuilder.create().texOffs(0, 82).addBox(-0.4F, -2.1596F, -0.4F, 0.8F, 4.3192F, 0.8F), PartPose.offsetAndRotation(3.75F, -6.4952F, 0.6F, 0.0F, 0.0F, (float) Math.toRadians(120.0F)));
        pd_sunDisk.addOrReplaceChild("cube_disk_ring_5", CubeListBuilder.create().texOffs(4, 82).addBox(-0.4F, -2.1596F, -0.4F, 0.8F, 4.3192F, 0.8F), PartPose.offsetAndRotation(6.4952F, -3.75F, 0.6F, 0.0F, 0.0F, (float) Math.toRadians(150.0F)));
        pd_sunDisk.addOrReplaceChild("cube_disk_ring_6", CubeListBuilder.create().texOffs(8, 82).addBox(-0.4F, -2.1596F, -0.4F, 0.8F, 4.3192F, 0.8F), PartPose.offsetAndRotation(7.5F, 0.0F, 0.6F, 0.0F, 0.0F, (float) Math.toRadians(180.0F)));
        pd_sunDisk.addOrReplaceChild("cube_disk_ring_7", CubeListBuilder.create().texOffs(12, 82).addBox(-0.4F, -2.1596F, -0.4F, 0.8F, 4.3192F, 0.8F), PartPose.offsetAndRotation(6.4952F, 3.75F, 0.6F, 0.0F, 0.0F, (float) Math.toRadians(210.0F)));
        pd_sunDisk.addOrReplaceChild("cube_disk_ring_8", CubeListBuilder.create().texOffs(16, 82).addBox(-0.4F, -2.1596F, -0.4F, 0.8F, 4.3192F, 0.8F), PartPose.offsetAndRotation(3.75F, 6.4952F, 0.6F, 0.0F, 0.0F, (float) Math.toRadians(240.0F)));
        pd_sunDisk.addOrReplaceChild("cube_disk_ring_9", CubeListBuilder.create().texOffs(20, 82).addBox(-0.4F, -2.1596F, -0.4F, 0.8F, 4.3192F, 0.8F), PartPose.offsetAndRotation(0.0F, 7.5F, 0.6F, 0.0F, 0.0F, (float) Math.toRadians(270.0F)));
        pd_sunDisk.addOrReplaceChild("cube_disk_ring_10", CubeListBuilder.create().texOffs(24, 82).addBox(-0.4F, -2.1596F, -0.4F, 0.8F, 4.3192F, 0.8F), PartPose.offsetAndRotation(-3.75F, 6.4952F, 0.6F, 0.0F, 0.0F, (float) Math.toRadians(300.0F)));
        pd_sunDisk.addOrReplaceChild("cube_disk_ring_11", CubeListBuilder.create().texOffs(28, 82).addBox(-0.4F, -2.1596F, -0.4F, 0.8F, 4.3192F, 0.8F), PartPose.offsetAndRotation(-6.4952F, 3.75F, 0.6F, 0.0F, 0.0F, (float) Math.toRadians(330.0F)));
        PartDefinition pd_rightLeg = pd_rootBone.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(78, 0).addBox(-2.25F, 0.0F, -2.0F, 4.5F, 15.0F, 4.0F).texOffs(36, 40).addBox(-2.45F, 6.0F, -2.5F, 4.9F, 7.5F, 4.4F).texOffs(66, 64).addBox(-2.55F, 12.0F, -2.8F, 5.1F, 3.0F, 4.8F), PartPose.offset(-2.25F, -15.0F, 0.0F));
        PartDefinition pd_leftLeg = pd_rootBone.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(96, 0).mirror().addBox(-2.25F, 0.0F, -2.0F, 4.5F, 15.0F, 4.0F).mirror(false).texOffs(56, 40).mirror().addBox(-2.45F, 6.0F, -2.5F, 4.9F, 7.5F, 4.4F).mirror(false).texOffs(88, 64).mirror().addBox(-2.55F, 12.0F, -2.8F, 5.1F, 3.0F, 4.8F).mirror(false), PartPose.offset(2.25F, -15.0F, 0.0F));
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
