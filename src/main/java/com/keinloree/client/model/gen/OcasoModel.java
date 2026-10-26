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

/** GENERADO por bb2java.mjs desde blockbench/ocaso.bbmodel. No editar a mano: edita la spec y regenera. */
public class OcasoModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("keinloree", "ocaso"), "main");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/ocaso.png");
    public static final ResourceLocation GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/ocaso_glow.png");
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
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;

    public OcasoModel(ModelPart layerRoot) {
        this.layerRoot = layerRoot;
        this.rootBone = layerRoot.getChild("root");
        this.body = this.rootBone.getChild("body");
        this.head = this.body.getChild("head");
        this.halo = this.head.getChild("halo");
        this.rightArm = this.body.getChild("right_arm");
        this.weapon = this.rightArm.getChild("weapon");
        this.leftArm = this.body.getChild("left_arm");
        this.cape = this.body.getChild("cape");
        this.rightLeg = this.rootBone.getChild("right_leg");
        this.leftLeg = this.rootBone.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition pd_rootBone = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pd_body = pd_rootBone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(66, 0).addBox(-3.5F, -12.0F, -2.25F, 7.0F, 12.0F, 4.5F).texOffs(0, 48).addBox(-3.0F, -10.68F, -2.9F, 6.0F, 5.04F, 5.0F, new CubeDeformation(0.25F)).texOffs(50, 59).addBox(-3.7F, -2.0F, -2.45F, 7.4F, 2.0F, 4.9F), PartPose.offset(0.0F, -13.0F, 0.0F));
        pd_body.addOrReplaceChild("cube_gem", CubeListBuilder.create().texOffs(114, 67).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -7.92F, -2.9F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_body.addOrReplaceChild("cube_sash", CubeListBuilder.create().texOffs(96, 67).addBox(-3.7F, -0.7F, -0.25F, 7.4F, 1.4F, 0.5F), PartPose.offsetAndRotation(0.0F, -6.0F, -2.6F, 0.0F, 0.0F, (float) Math.toRadians(25.0F)));
        pd_body.addOrReplaceChild("cube_coat_f", CubeListBuilder.create().texOffs(20, 59).addBox(-3.7F, -3.5F, -0.4F, 7.4F, 7.0F, 0.8F), PartPose.offsetAndRotation(0.0F, 3.0F, -2.2F, (float) Math.toRadians(8.0F), 0.0F, 0.0F));
        PartDefinition pd_head = pd_body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F).texOffs(26, 0).addBox(-4.7F, -9.2F, -4.2F, 9.4F, 9.4F, 9.4F).texOffs(108, 59).addBox(-3.3F, -6.4F, -4.8F, 6.6F, 5.6F, 0.8F).texOffs(120, 67).addBox(-2.4F, -5.0F, -5.05F, 1.6F, 0.8F, 0.3F).texOffs(0, 74).addBox(0.8F, -5.0F, -5.05F, 1.6F, 0.8F, 0.3F), PartPose.offset(0.0F, -12.0F, 0.0F));
        pd_head.addOrReplaceChild("cube_hood_tip", CubeListBuilder.create().texOffs(38, 48).addBox(-2.5F, -2.0F, -2.5F, 5.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, -10.0F, 2.5F, (float) Math.toRadians(25.0F), 0.0F, 0.0F));
        pd_head.addOrReplaceChild("cube_hood_tip2", CubeListBuilder.create().texOffs(10, 67).addBox(-1.3F, -1.5F, -1.3F, 2.6F, 3.0F, 2.6F), PartPose.offsetAndRotation(0.0F, -12.6F, 4.4F, (float) Math.toRadians(40.0F), 0.0F, 0.0F));
        PartDefinition pd_halo = pd_head.addOrReplaceChild("halo", CubeListBuilder.create().texOffs(34, 67).addBox(-6.3F, -2.0995F, -0.3F, 0.6F, 4.199F, 0.6F), PartPose.offset(0.0F, -5.0F, 6.0F));
        pd_halo.addOrReplaceChild("cube_halo_1", CubeListBuilder.create().texOffs(38, 67).addBox(-0.3F, -2.0995F, -0.3F, 0.6F, 4.199F, 0.6F), PartPose.offsetAndRotation(-4.8541F, -3.5267F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(36.0F)));
        pd_halo.addOrReplaceChild("cube_halo_2", CubeListBuilder.create().texOffs(42, 67).addBox(-0.3F, -2.0995F, -0.3F, 0.6F, 4.199F, 0.6F), PartPose.offsetAndRotation(-1.8541F, -5.7063F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(72.0F)));
        pd_halo.addOrReplaceChild("cube_halo_3", CubeListBuilder.create().texOffs(46, 67).addBox(-0.3F, -2.0995F, -0.3F, 0.6F, 4.199F, 0.6F), PartPose.offsetAndRotation(1.8541F, -5.7063F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(108.0F)));
        pd_halo.addOrReplaceChild("cube_halo_4", CubeListBuilder.create().texOffs(50, 67).addBox(-0.3F, -2.0995F, -0.3F, 0.6F, 4.199F, 0.6F), PartPose.offsetAndRotation(4.8541F, -3.5267F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(144.0F)));
        pd_halo.addOrReplaceChild("cube_halo_5", CubeListBuilder.create().texOffs(54, 67).addBox(-0.3F, -2.0995F, -0.3F, 0.6F, 4.199F, 0.6F), PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(180.0F)));
        pd_halo.addOrReplaceChild("cube_halo_6", CubeListBuilder.create().texOffs(58, 67).addBox(-0.3F, -2.0995F, -0.3F, 0.6F, 4.199F, 0.6F), PartPose.offsetAndRotation(4.8541F, 3.5267F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(216.0F)));
        pd_halo.addOrReplaceChild("cube_halo_7", CubeListBuilder.create().texOffs(62, 67).addBox(-0.3F, -2.0995F, -0.3F, 0.6F, 4.199F, 0.6F), PartPose.offsetAndRotation(1.8541F, 5.7063F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(252.0F)));
        pd_halo.addOrReplaceChild("cube_halo_8", CubeListBuilder.create().texOffs(66, 67).addBox(-0.3F, -2.0995F, -0.3F, 0.6F, 4.199F, 0.6F), PartPose.offsetAndRotation(-1.8541F, 5.7063F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(288.0F)));
        pd_halo.addOrReplaceChild("cube_halo_9", CubeListBuilder.create().texOffs(70, 67).addBox(-0.3F, -2.0995F, -0.3F, 0.6F, 4.199F, 0.6F), PartPose.offsetAndRotation(-4.8541F, 3.5267F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(324.0F)));
        PartDefinition pd_rightArm = pd_body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 32).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 12.0F, 3.0F).texOffs(76, 59).addBox(-1.6F, 8.5F, -1.6F, 3.2F, 3.0F, 3.2F, new CubeDeformation(0.2F)).texOffs(58, 48).addBox(-3.0F, -2.0F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.offset(-5.0F, -10.5F, 0.0F));
        PartDefinition pd_weapon = pd_rightArm.addOrReplaceChild("weapon", CubeListBuilder.create().texOffs(22, 67).addBox(-1.1F, -1.533F, -1.35F, 2.2F, 2.2F, 2.2F).texOffs(88, 67).addBox(-1.0F, -21.7846F, -13.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 11.5F, 0.0F));
        pd_weapon.addOrReplaceChild("cube_shaft", CubeListBuilder.create().texOffs(0, 0).addBox(-0.7F, -15.0F, -0.7F, 1.4F, 30.0F, 1.4F), PartPose.offsetAndRotation(0.0F, -7.7942F, -4.5F, (float) Math.toRadians(30.0F), 0.0F, 0.0F));
        pd_weapon.addOrReplaceChild("cube_blade_1", CubeListBuilder.create().texOffs(22, 48).addBox(-0.4F, -1.3F, -3.5F, 0.8F, 2.6F, 7.0F), PartPose.offsetAndRotation(0.0F, -20.7846F, -15.0F, (float) Math.toRadians(30.0F), 0.0F, 0.0F));
        pd_weapon.addOrReplaceChild("cube_blade_2", CubeListBuilder.create().texOffs(38, 59).addBox(-0.4F, -1.3F, -2.5F, 0.8F, 2.6F, 5.0F), PartPose.offsetAndRotation(0.0F, -22.2846F, -21.0F, (float) Math.toRadians(60.0F), 0.0F, 0.0F));
        pd_weapon.addOrReplaceChild("cube_blade_3", CubeListBuilder.create().texOffs(0, 67).addBox(-0.4F, -1.1F, -1.75F, 0.8F, 2.2F, 3.5F), PartPose.offsetAndRotation(0.0F, -25.2846F, -23.5F, (float) Math.toRadians(80.0F), 0.0F, 0.0F));
        pd_weapon.addOrReplaceChild("cube_edge", CubeListBuilder.create().texOffs(56, 32).addBox(-0.2F, -0.3F, -5.5F, 0.4F, 0.6F, 11.0F), PartPose.offsetAndRotation(0.0F, -21.3846F, -17.5F, (float) Math.toRadians(30.0F), 0.0F, 0.0F));
        PartDefinition pd_leftArm = pd_body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(44, 32).mirror().addBox(-1.5F, -0.5F, -1.5F, 3.0F, 12.0F, 3.0F).mirror(false).texOffs(92, 59).mirror().addBox(-1.6F, 8.5F, -1.6F, 3.2F, 3.0F, 3.2F, new CubeDeformation(0.2F)).mirror(false).texOffs(78, 48).mirror().addBox(-2.0F, -2.0F, -2.5F, 5.0F, 3.0F, 5.0F).mirror(false), PartPose.offset(5.0F, -10.5F, 0.0F));
        PartDefinition pd_cape = pd_body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(8, 0).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 22.0F, 0.8F), PartPose.offset(0.0F, -12.0F, 2.6F));
        pd_cape.addOrReplaceChild("cube_cape_tatter", CubeListBuilder.create().texOffs(74, 67).addBox(-3.0F, -1.5F, -0.4F, 6.0F, 3.0F, 0.8F), PartPose.offsetAndRotation(0.0F, 23.0F, 0.4F, 0.0F, 0.0F, (float) Math.toRadians(8.0F)));
        PartDefinition pd_rightLeg = pd_rootBone.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(90, 0).addBox(-1.75F, 0.0F, -2.0F, 3.5F, 13.0F, 4.0F).texOffs(80, 32).addBox(-1.95F, 5.2F, -2.5F, 3.9F, 6.5F, 4.4F).texOffs(98, 48).addBox(-2.05F, 10.0F, -2.8F, 4.1F, 3.0F, 4.8F), PartPose.offset(-1.75F, -13.0F, 0.0F));
        PartDefinition pd_leftLeg = pd_rootBone.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(106, 0).mirror().addBox(-1.75F, 0.0F, -2.0F, 3.5F, 13.0F, 4.0F).mirror(false).texOffs(98, 32).mirror().addBox(-1.95F, 5.2F, -2.5F, 3.9F, 6.5F, 4.4F).mirror(false).texOffs(0, 59).mirror().addBox(-2.05F, 10.0F, -2.8F, 4.1F, 3.0F, 4.8F).mirror(false), PartPose.offset(1.75F, -13.0F, 0.0F));
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
