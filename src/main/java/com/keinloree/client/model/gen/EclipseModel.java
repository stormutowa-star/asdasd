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

/** GENERADO por bb2java.mjs desde blockbench/eclipse.bbmodel. No editar a mano: edita la spec y regenera. */
public class EclipseModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("keinloree", "eclipse"), "main");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/eclipse.png");
    public static final ResourceLocation GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/eclipse_glow.png");
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

    public EclipseModel(ModelPart layerRoot) {
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
        PartDefinition pd_body = pd_rootBone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(36, 38).addBox(-4.0F, -13.0F, -2.25F, 8.0F, 13.0F, 4.5F).texOffs(64, 56).addBox(-3.5F, -11.57F, -2.9F, 7.0F, 5.46F, 5.0F, new CubeDeformation(0.25F)).texOffs(40, 82).addBox(-4.2F, -2.0F, -2.45F, 8.4F, 2.0F, 4.9F).texOffs(100, 82).addBox(-0.4F, -12.0F, -2.9F, 0.8F, 6.0F, 0.4F), PartPose.offset(0.0F, -14.0F, 0.0F));
        pd_body.addOrReplaceChild("cube_gem", CubeListBuilder.create().texOffs(112, 90).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -8.58F, -2.9F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_body.addOrReplaceChild("cube_tasset_r", CubeListBuilder.create().texOffs(42, 71).addBox(-1.5F, -2.5F, -2.5F, 3.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(-2.4F, 2.0F, -1.0F, 0.0F, 0.0F, (float) Math.toRadians(8.0F)));
        pd_body.addOrReplaceChild("cube_tasset_l", CubeListBuilder.create().texOffs(58, 71).addBox(-1.5F, -2.5F, -2.5F, 3.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(2.4F, 2.0F, -1.0F, 0.0F, 0.0F, (float) Math.toRadians(-8.0F)));
        PartDefinition pd_head = pd_body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(94, 38).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F).texOffs(0, 38).addBox(-4.3F, -8.5F, -4.3F, 8.6F, 8.6F, 8.6F).texOffs(0, 96).addBox(-3.0F, -4.8F, -4.55F, 6.0F, 0.8F, 0.3F).texOffs(22, 71).addBox(-0.5F, -9.4F, -4.4F, 1.0F, 1.2F, 8.8F), PartPose.offset(0.0F, -13.0F, 0.0F));
        PartDefinition pd_halo = pd_head.addOrReplaceChild("halo", CubeListBuilder.create().texOffs(104, 82).addBox(-8.1F, -1.6418F, -0.1F, 1.2F, 3.2837F, 1.2F).texOffs(88, 56).addBox(-5.0F, -5.0F, 0.6F, 10.0F, 10.0F, 0.4F), PartPose.offset(0.0F, -5.0F, 6.0F));
        pd_halo.addOrReplaceChild("cube_halo_1", CubeListBuilder.create().texOffs(112, 82).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(-6.9291F, -2.8701F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(22.5F)));
        pd_halo.addOrReplaceChild("cube_halo_2", CubeListBuilder.create().texOffs(120, 82).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(-5.3033F, -5.3033F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_halo.addOrReplaceChild("cube_halo_3", CubeListBuilder.create().texOffs(0, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(-2.8701F, -6.9291F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(67.5F)));
        pd_halo.addOrReplaceChild("cube_halo_4", CubeListBuilder.create().texOffs(8, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(0.0F, -7.5F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(90.0F)));
        pd_halo.addOrReplaceChild("cube_halo_5", CubeListBuilder.create().texOffs(16, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(2.8701F, -6.9291F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(112.5F)));
        pd_halo.addOrReplaceChild("cube_halo_6", CubeListBuilder.create().texOffs(24, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(5.3033F, -5.3033F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(135.0F)));
        pd_halo.addOrReplaceChild("cube_halo_7", CubeListBuilder.create().texOffs(32, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(6.9291F, -2.8701F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(157.5F)));
        pd_halo.addOrReplaceChild("cube_halo_8", CubeListBuilder.create().texOffs(40, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(7.5F, 0.0F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(180.0F)));
        pd_halo.addOrReplaceChild("cube_halo_9", CubeListBuilder.create().texOffs(48, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(6.9291F, 2.8701F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(202.5F)));
        pd_halo.addOrReplaceChild("cube_halo_10", CubeListBuilder.create().texOffs(56, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(5.3033F, 5.3033F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(225.0F)));
        pd_halo.addOrReplaceChild("cube_halo_11", CubeListBuilder.create().texOffs(64, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(2.8701F, 6.9291F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(247.5F)));
        pd_halo.addOrReplaceChild("cube_halo_12", CubeListBuilder.create().texOffs(72, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(0.0F, 7.5F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(270.0F)));
        pd_halo.addOrReplaceChild("cube_halo_13", CubeListBuilder.create().texOffs(80, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(-2.8701F, 6.9291F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(292.5F)));
        pd_halo.addOrReplaceChild("cube_halo_14", CubeListBuilder.create().texOffs(88, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(-5.3033F, 5.3033F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(315.0F)));
        pd_halo.addOrReplaceChild("cube_halo_15", CubeListBuilder.create().texOffs(96, 90).addBox(-0.6F, -1.6418F, -0.6F, 1.2F, 3.2837F, 1.2F), PartPose.offsetAndRotation(-6.9291F, 2.8701F, 0.5F, 0.0F, 0.0F, (float) Math.toRadians(337.5F)));
        pd_halo.addOrReplaceChild("cube_disk2", CubeListBuilder.create().texOffs(0, 71).addBox(-5.0F, -5.0F, -0.2F, 10.0F, 10.0F, 0.4F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.8F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        PartDefinition pd_rightArm = pd_body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 56).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 12.0F, 3.0F).texOffs(68, 82).addBox(-1.6F, 8.5F, -1.6F, 3.2F, 3.0F, 3.2F, new CubeDeformation(0.2F)).texOffs(88, 71).addBox(-3.0F, -2.0F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.offset(-5.5F, -11.5F, 0.0F));
        PartDefinition pd_weapon = pd_rightArm.addOrReplaceChild("weapon", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, -0.6F, -26.0F, 1.2F, 1.2F, 36.0F).texOffs(74, 71).addBox(-0.8F, -1.6F, -31.0F, 1.6F, 3.2F, 5.0F).texOffs(14, 96).addBox(-1.8F, -0.5F, -26.1F, 3.6F, 1.0F, 1.0F).texOffs(104, 90).addBox(-0.9F, -0.9F, 9.7F, 1.8F, 1.8F, 1.4F), PartPose.offset(0.0F, 12.5F, 0.0F));
        PartDefinition pd_leftArm = pd_body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 56).mirror().addBox(-1.5F, -0.5F, -1.5F, 3.0F, 12.0F, 3.0F).mirror(false).texOffs(84, 82).mirror().addBox(-1.6F, 8.5F, -1.6F, 3.2F, 3.0F, 3.2F, new CubeDeformation(0.2F)).mirror(false).texOffs(108, 71).mirror().addBox(-2.0F, -2.0F, -2.5F, 5.0F, 3.0F, 5.0F).mirror(false), PartPose.offset(5.5F, -11.5F, 0.0F));
        PartDefinition pd_cape = pd_body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(98, 0).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 20.0F, 0.8F).texOffs(76, 0).addBox(-4.6F, 0.0F, 0.75F, 9.2F, 20.0F, 0.3F), PartPose.offset(0.0F, -13.0F, 2.6F));
        PartDefinition pd_rightLeg = pd_rootBone.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(62, 38).addBox(-1.9F, 0.0F, -2.0F, 3.8F, 14.0F, 4.0F).texOffs(24, 56).addBox(-2.1F, 5.6F, -2.5F, 4.2F, 7.0F, 4.4F).texOffs(0, 82).addBox(-2.2F, 11.0F, -2.8F, 4.4F, 3.0F, 4.8F), PartPose.offset(-1.9F, -14.0F, 0.0F));
        PartDefinition pd_leftLeg = pd_rootBone.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(78, 38).mirror().addBox(-1.9F, 0.0F, -2.0F, 3.8F, 14.0F, 4.0F).mirror(false).texOffs(44, 56).mirror().addBox(-2.1F, 5.6F, -2.5F, 4.2F, 7.0F, 4.4F).mirror(false).texOffs(20, 82).mirror().addBox(-2.2F, 11.0F, -2.8F, 4.4F, 3.0F, 4.8F).mirror(false), PartPose.offset(1.9F, -14.0F, 0.0F));
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
