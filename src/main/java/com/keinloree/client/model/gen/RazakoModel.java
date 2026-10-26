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

/** GENERADO por bb2java.mjs desde blockbench/razako.bbmodel. No editar a mano: edita la spec y regenera. */
public class RazakoModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("keinloree", "razako"), "main");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/razako.png");
    public static final ResourceLocation GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/razako_glow.png");
    private static final Vector3f CACHE = new Vector3f();

    private final ModelPart layerRoot;
    public final ModelPart rootBone;
    public final ModelPart body;
    public final ModelPart head;
    public final ModelPart rightArm;
    public final ModelPart rightForearm;
    public final ModelPart weapon;
    public final ModelPart leftArm;
    public final ModelPart leftForearm;
    public final ModelPart chain;
    public final ModelPart chainTail;
    public final ModelPart cape;
    public final ModelPart capeLow;
    public final ModelPart rightLeg;
    public final ModelPart rightShin;
    public final ModelPart leftLeg;
    public final ModelPart leftShin;

    public RazakoModel(ModelPart layerRoot) {
        this.layerRoot = layerRoot;
        this.rootBone = layerRoot.getChild("root");
        this.body = this.rootBone.getChild("body");
        this.head = this.body.getChild("head");
        this.rightArm = this.body.getChild("right_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.weapon = this.rightForearm.getChild("weapon");
        this.leftArm = this.body.getChild("left_arm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.chain = this.leftForearm.getChild("chain");
        this.chainTail = this.chain.getChild("chain_tail");
        this.cape = this.body.getChild("cape");
        this.capeLow = this.cape.getChild("cape_low");
        this.rightLeg = this.rootBone.getChild("right_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftLeg = this.rootBone.getChild("left_leg");
        this.leftShin = this.leftLeg.getChild("left_shin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition pd_rootBone = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pd_body = pd_rootBone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 36).addBox(-7.0F, -4.0F, -4.5F, 14.0F, 7.0F, 9.0F).texOffs(72, 68).addBox(-7.6F, -5.0F, -5.0F, 15.2F, 3.0F, 10.0F).texOffs(74, 114).addBox(-2.5F, -5.5F, -5.8F, 5.0F, 4.0F, 0.8F).texOffs(108, 82).addBox(-2.0F, -2.0F, -6.2F, 4.0F, 11.0F, 0.5F).texOffs(166, 68).addBox(-6.0F, -10.0F, -3.5F, 12.0F, 6.0F, 7.0F).texOffs(30, 0).addBox(-9.0F, -20.0F, -5.0F, 18.0F, 11.0F, 10.0F).texOffs(154, 82).addBox(-7.5F, -18.5F, -6.3F, 15.0F, 8.5F, 1.3F).texOffs(86, 114).addBox(-2.0F, -17.0F, -6.9F, 4.0F, 4.0F, 0.6F).texOffs(182, 106).addBox(-0.4F, -13.0F, -6.65F, 0.8F, 5.5F, 0.35F).texOffs(118, 82).addBox(-8.0F, -19.0F, 5.0F, 16.0F, 9.0F, 1.5F).texOffs(124, 68).addBox(-5.5F, -21.0F, -5.0F, 11.0F, 2.5F, 10.0F), PartPose.offset(0.0F, -20.0F, 0.0F));
        pd_body.addOrReplaceChild("cube_crack_a", CubeListBuilder.create().texOffs(210, 114).addBox(-2.15F, -0.45F, -0.175F, 4.3F, 0.9F, 0.35F), PartPose.offsetAndRotation(-4.65F, -14.95F, -6.475F, 0.0F, 0.0F, (float) Math.toRadians(28.0F)));
        pd_body.addOrReplaceChild("cube_crack_b", CubeListBuilder.create().texOffs(222, 114).addBox(-2.15F, -0.45F, -0.175F, 4.3F, 0.9F, 0.35F), PartPose.offsetAndRotation(4.65F, -11.95F, -6.475F, 0.0F, 0.0F, (float) Math.toRadians(-32.0F)));
        pd_body.addOrReplaceChild("cube_back_crack", CubeListBuilder.create().texOffs(84, 106).addBox(-1.0F, -3.5F, -0.2F, 2.0F, 7.0F, 0.4F), PartPose.offsetAndRotation(0.0F, -14.5F, 6.6F, 0.0F, 0.0F, (float) Math.toRadians(8.0F)));
        PartDefinition pd_head = pd_body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(216, 0).addBox(-4.5F, -8.0F, -4.5F, 9.0F, 7.5F, 9.0F).texOffs(128, 114).addBox(-4.8F, -7.5F, -5.4F, 9.6F, 2.3F, 0.9F).texOffs(150, 114).addBox(-4.8F, -4.2F, -5.4F, 9.6F, 2.7F, 0.9F).texOffs(192, 114).addBox(-3.8F, -5.2F, -5.1F, 7.6F, 1.0F, 0.6F).texOffs(54, 82).addBox(-0.8F, -10.5F, -4.5F, 1.6F, 2.5F, 9.0F).texOffs(216, 95).addBox(-4.0F, -6.5F, 4.5F, 8.0F, 5.5F, 1.1F).texOffs(76, 82).addBox(-5.5F, -5.5F, -3.5F, 1.0F, 4.5F, 6.5F).texOffs(92, 82).addBox(4.5F, -5.5F, -3.5F, 1.0F, 4.5F, 6.5F).texOffs(236, 95).addBox(-8.5F, -8.5F, -1.5F, 4.0F, 3.5F, 3.5F).texOffs(0, 106).addBox(4.5F, -8.5F, -1.5F, 4.0F, 3.5F, 3.5F).texOffs(48, 106).addBox(-10.5F, -9.2F, 1.2F, 3.0F, 3.2F, 3.8F).texOffs(62, 106).addBox(7.5F, -9.2F, 1.2F, 3.0F, 3.2F, 3.8F).texOffs(134, 106).addBox(-12.0F, -10.6F, 4.2F, 2.5F, 2.8F, 3.8F).texOffs(148, 106).addBox(9.5F, -10.6F, 4.2F, 2.5F, 2.8F, 3.8F).texOffs(12, 114).addBox(-12.6F, -13.0F, 7.0F, 2.0F, 3.0F, 2.8F).texOffs(22, 114).addBox(10.6F, -13.0F, 7.0F, 2.0F, 3.0F, 2.8F).texOffs(162, 106).addBox(-12.2F, -15.5F, 8.8F, 1.2F, 3.1F, 2.2F).texOffs(172, 106).addBox(11.0F, -15.5F, 8.8F, 1.2F, 3.1F, 2.2F), PartPose.offset(0.0F, -20.0F, 0.0F));
        PartDefinition pd_rightArm = pd_body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(46, 36).addBox(-4.0F, -5.0F, -6.5F, 8.0F, 2.5F, 13.0F).texOffs(96, 52).addBox(-4.8F, -2.5F, -6.0F, 7.8F, 3.0F, 12.0F).texOffs(0, 68).addBox(-4.5F, 0.5F, -5.5F, 6.5F, 3.0F, 11.0F).texOffs(0, 52).addBox(-2.5F, 1.0F, -3.5F, 5.0F, 9.0F, 7.0F), PartPose.offset(-12.0F, -17.0F, 0.0F));
        pd_rightArm.addOrReplaceChild("cube_spike_r", CubeListBuilder.create().texOffs(192, 95).addBox(-1.15F, -2.75F, -1.2F, 2.3F, 5.5F, 2.4F), PartPose.offsetAndRotation(-0.65F, -7.75F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(-14.0F)));
        PartDefinition pd_rightForearm = pd_rightArm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(204, 68).addBox(-3.0F, -2.5F, -4.3F, 6.0F, 4.0F, 8.6F).texOffs(48, 52).addBox(-2.5F, 1.0F, -3.5F, 5.0F, 8.5F, 7.0F).texOffs(64, 95).addBox(-2.5F, 9.5F, -3.5F, 5.0F, 4.0F, 7.0F).texOffs(210, 36).addBox(-3.0F, 2.0F, -4.3F, 6.0F, 7.0F, 8.6F), PartPose.offset(0.0F, 10.0F, 0.0F));
        pd_rightForearm.addOrReplaceChild("cube_arm_crack_r", CubeListBuilder.create().texOffs(32, 114).addBox(-0.6F, -2.5F, -0.2F, 1.2F, 5.0F, 0.4F), PartPose.offsetAndRotation(0.0F, 5.5F, -4.45F, 0.0F, 0.0F, (float) Math.toRadians(12.0F)));
        PartDefinition pd_weapon = pd_rightForearm.addOrReplaceChild("weapon", CubeListBuilder.create().texOffs(166, 0).addBox(-1.2F, -7.0F, -1.2F, 2.4F, 16.0F, 2.4F).texOffs(90, 106).addBox(-1.8F, 9.0F, -1.8F, 3.6F, 2.7F, 3.6F).texOffs(186, 106).addBox(-7.5F, -9.0F, -2.0F, 15.0F, 2.0F, 4.0F).texOffs(152, 95).addBox(-2.0F, -9.5F, -2.7F, 4.0F, 3.2F, 5.4F).texOffs(12, 0).addBox(-1.45F, -40.5F, 1.5F, 2.9F, 31.5F, 2.0F).texOffs(0, 0).addBox(-1.0F, -40.5F, -2.0F, 2.0F, 31.5F, 3.5F).texOffs(22, 0).addBox(-0.6F, -40.5F, -3.5F, 1.2F, 31.5F, 1.5F).texOffs(32, 106).addBox(-1.0F, -42.5F, -3.5F, 2.0F, 2.0F, 5.5F).texOffs(0, 114).addBox(-0.9F, -44.5F, -3.5F, 1.8F, 2.0F, 3.7F).texOffs(120, 114).addBox(-0.6F, -46.9F, -3.5F, 1.2F, 2.4F, 1.9F).texOffs(76, 106).addBox(-1.25F, -37.5F, -1.6F, 2.5F, 7.0F, 0.6F), PartPose.offset(0.0F, 11.5F, 0.0F));
        pd_weapon.addOrReplaceChild("cube_blade_crack_a", CubeListBuilder.create().texOffs(144, 95).addBox(-1.25F, -5.0F, -0.4F, 2.5F, 10.0F, 0.8F), PartPose.offsetAndRotation(0.0F, -17.5F, -0.5F, (float) Math.toRadians(-5.0F), 0.0F, 0.0F));
        pd_weapon.addOrReplaceChild("cube_blade_crack_b", CubeListBuilder.create().texOffs(46, 82).addBox(-1.25F, -5.75F, -0.4F, 2.5F, 11.5F, 0.8F), PartPose.offsetAndRotation(0.0F, -28.75F, 0.7F, (float) Math.toRadians(4.0F), 0.0F, 0.0F));
        PartDefinition pd_leftArm = pd_body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(88, 36).addBox(-4.0F, -5.0F, -6.5F, 8.0F, 2.5F, 13.0F).texOffs(136, 52).addBox(-3.0F, -2.5F, -6.0F, 7.8F, 3.0F, 12.0F).texOffs(36, 68).addBox(-2.0F, 0.5F, -5.5F, 6.5F, 3.0F, 11.0F).texOffs(24, 52).addBox(-2.5F, 1.0F, -3.5F, 5.0F, 9.0F, 7.0F), PartPose.offset(12.0F, -17.0F, 0.0F));
        pd_leftArm.addOrReplaceChild("cube_spike_l", CubeListBuilder.create().texOffs(204, 95).addBox(-1.15F, -2.75F, -1.2F, 2.3F, 5.5F, 2.4F), PartPose.offsetAndRotation(0.65F, -7.75F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(14.0F)));
        PartDefinition pd_leftForearm = pd_leftArm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(0, 82).addBox(-3.0F, -2.5F, -4.3F, 6.0F, 4.0F, 8.6F).texOffs(72, 52).addBox(-2.5F, 1.0F, -3.5F, 5.0F, 8.5F, 7.0F).texOffs(88, 95).addBox(-2.5F, 9.5F, -3.5F, 5.0F, 4.0F, 7.0F), PartPose.offset(0.0F, 10.0F, 0.0F));
        PartDefinition pd_chain = pd_leftForearm.addOrReplaceChild("chain", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 0.0F));
        pd_chain.addOrReplaceChild("cube_coil_0", CubeListBuilder.create().texOffs(188, 82).addBox(-3.2F, -0.55F, -4.4F, 6.4F, 1.1F, 8.8F), PartPose.offsetAndRotation(0.0F, 7.25F, 0.0F, 0.0F, (float) Math.toRadians(9.0F), 0.0F));
        pd_chain.addOrReplaceChild("cube_coil_1", CubeListBuilder.create().texOffs(220, 82).addBox(-3.2F, -0.55F, -4.4F, 6.4F, 1.1F, 8.8F), PartPose.offsetAndRotation(0.0F, 5.25F, 0.0F, 0.0F, (float) Math.toRadians(-9.0F), 0.0F));
        pd_chain.addOrReplaceChild("cube_coil_2", CubeListBuilder.create().texOffs(0, 95).addBox(-3.2F, -0.55F, -4.4F, 6.4F, 1.1F, 8.8F), PartPose.offsetAndRotation(0.0F, 3.25F, 0.0F, 0.0F, (float) Math.toRadians(9.0F), 0.0F));
        pd_chain.addOrReplaceChild("cube_coil_3", CubeListBuilder.create().texOffs(32, 95).addBox(-3.2F, -0.55F, -4.4F, 6.4F, 1.1F, 8.8F), PartPose.offsetAndRotation(0.0F, 1.25F, 0.0F, 0.0F, (float) Math.toRadians(-9.0F), 0.0F));
        PartDefinition pd_chainTail = pd_chain.addOrReplaceChild("chain_tail", CubeListBuilder.create().texOffs(96, 114).addBox(-1.0F, 0.0F, -0.8F, 2.0F, 3.0F, 1.6F).texOffs(104, 114).addBox(-0.8F, 3.0F, -1.0F, 1.6F, 3.0F, 2.0F).texOffs(112, 114).addBox(-1.0F, 5.5F, -0.8F, 2.0F, 3.0F, 1.6F).texOffs(16, 106).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F).texOffs(172, 114).addBox(-3.6F, 9.5F, -0.6F, 7.2F, 1.0F, 1.2F).texOffs(172, 95).addBox(-0.6F, 9.5F, -3.6F, 1.2F, 1.0F, 7.2F), PartPose.offset(0.0F, 12.5F, 0.0F));
        PartDefinition pd_cape = pd_body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(178, 0).addBox(-8.5F, -0.5F, 0.1F, 17.0F, 14.5F, 1.2F), PartPose.offset(0.0F, -18.0F, 6.5F));
        PartDefinition pd_capeLow = pd_cape.addOrReplaceChild("cape_low", CubeListBuilder.create().texOffs(30, 82).addBox(3.0F, 0.0F, -0.4F, 5.5F, 11.0F, 1.2F).texOffs(150, 0).addBox(-3.0F, 0.0F, -0.4F, 6.0F, 17.0F, 1.2F).texOffs(236, 52).addBox(-8.5F, 0.0F, -0.4F, 5.5F, 13.0F, 1.2F), PartPose.offset(0.0F, 14.0F, 1.0F));
        PartDefinition pd_rightLeg = pd_rootBone.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(86, 0).addBox(-3.5F, -1.0F, -4.5F, 7.0F, 11.0F, 9.0F).texOffs(112, 95).addBox(-3.0F, -1.5F, -5.6F, 6.0F, 9.0F, 1.1F).texOffs(224, 106).addBox(-3.0F, 7.5F, -6.2F, 6.0F, 4.0F, 1.7F), PartPose.offset(-5.0F, -20.0F, 0.0F));
        PartDefinition pd_rightShin = pd_rightLeg.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(176, 52).addBox(-3.5F, 0.0F, -4.0F, 7.0F, 7.0F, 8.0F).texOffs(106, 106).addBox(-3.0F, 0.5F, -5.0F, 6.0F, 6.0F, 1.0F).texOffs(130, 36).addBox(-4.0F, 6.0F, -6.5F, 8.0F, 4.0F, 11.5F).texOffs(38, 114).addBox(-3.5F, 7.0F, -8.0F, 7.0F, 3.0F, 1.5F), PartPose.offset(0.0F, 10.0F, 0.0F));
        PartDefinition pd_leftLeg = pd_rootBone.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(118, 0).addBox(-3.5F, -1.0F, -4.5F, 7.0F, 11.0F, 9.0F).texOffs(128, 95).addBox(-3.0F, -1.5F, -5.6F, 6.0F, 9.0F, 1.1F).texOffs(240, 106).addBox(-3.0F, 7.5F, -6.2F, 6.0F, 4.0F, 1.7F), PartPose.offset(5.0F, -20.0F, 0.0F));
        PartDefinition pd_leftShin = pd_leftLeg.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(206, 52).addBox(-3.5F, 0.0F, -4.0F, 7.0F, 7.0F, 8.0F).texOffs(120, 106).addBox(-3.0F, 0.5F, -5.0F, 6.0F, 6.0F, 1.0F).texOffs(170, 36).addBox(-4.0F, 6.0F, -6.5F, 8.0F, 4.0F, 11.5F).texOffs(56, 114).addBox(-3.5F, 7.0F, -8.0F, 7.0F, 3.0F, 1.5F), PartPose.offset(0.0F, 10.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 256, 256);
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
