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

/** GENERADO por bb2java.mjs desde blockbench/alba.bbmodel. No editar a mano: edita la spec y regenera. */
public class AlbaModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("keinloree", "alba"), "main");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/alba.png");
    public static final ResourceLocation GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/alba_glow.png");
    private static final Vector3f CACHE = new Vector3f();

    private final ModelPart layerRoot;
    public final ModelPart rootBone;
    public final ModelPart body;
    public final ModelPart head;
    public final ModelPart halo;
    public final ModelPart rightArm;
    public final ModelPart weapon;
    public final ModelPart leftArm;
    public final ModelPart weaponL;
    public final ModelPart cape;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;

    public AlbaModel(ModelPart layerRoot) {
        this.layerRoot = layerRoot;
        this.rootBone = layerRoot.getChild("root");
        this.body = this.rootBone.getChild("body");
        this.head = this.body.getChild("head");
        this.halo = this.head.getChild("halo");
        this.rightArm = this.body.getChild("right_arm");
        this.weapon = this.rightArm.getChild("weapon");
        this.leftArm = this.body.getChild("left_arm");
        this.weaponL = this.leftArm.getChild("weapon_l");
        this.cape = this.body.getChild("cape");
        this.rightLeg = this.rootBone.getChild("right_leg");
        this.leftLeg = this.rootBone.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition pd_rootBone = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pd_body = pd_rootBone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -12.0F, -2.25F, 7.0F, 12.0F, 4.5F).texOffs(18, 31).addBox(-3.0F, -10.68F, -2.9F, 6.0F, 5.04F, 5.0F, new CubeDeformation(0.25F)).texOffs(0, 43).addBox(-3.7F, -2.0F, -2.45F, 7.4F, 2.0F, 4.9F).texOffs(80, 43).addBox(-1.0F, -6.0F, -2.85F, 2.0F, 6.0F, 0.5F), PartPose.offset(0.0F, -13.0F, 0.0F));
        pd_body.addOrReplaceChild("cube_gem", CubeListBuilder.create().texOffs(108, 50).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -7.92F, -2.9F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_body.addOrReplaceChild("cube_skirt_f", CubeListBuilder.create().texOffs(4, 50).addBox(-3.7F, -2.0F, -0.4F, 7.4F, 4.0F, 0.8F), PartPose.offsetAndRotation(0.0F, 1.5F, -2.2F, (float) Math.toRadians(12.0F), 0.0F, 0.0F));
        pd_body.addOrReplaceChild("cube_skirt_b", CubeListBuilder.create().texOffs(22, 50).addBox(-3.7F, -2.0F, -0.4F, 7.4F, 4.0F, 0.8F), PartPose.offsetAndRotation(0.0F, 1.5F, 2.2F, (float) Math.toRadians(-12.0F), 0.0F, 0.0F));
        PartDefinition pd_head = pd_body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F).texOffs(52, 17).addBox(-4.3F, -8.2F, -3.7F, 8.6F, 3.4F, 8.6F).texOffs(26, 43).addBox(-4.2F, -6.0F, 2.6F, 8.4F, 5.0F, 1.6F).texOffs(40, 56).addBox(-2.4F, -4.5F, -4.15F, 1.2F, 1.0F, 0.2F).texOffs(46, 56).addBox(1.2F, -4.5F, -4.15F, 1.2F, 1.0F, 0.2F).texOffs(0, 56).addBox(-4.2F, -6.7F, -4.6F, 8.4F, 1.0F, 0.6F).texOffs(86, 43).addBox(-0.4F, -10.45F, -4.65F, 0.8F, 4.5F, 0.5F), PartPose.offset(0.0F, -12.0F, 0.0F));
        pd_head.addOrReplaceChild("cube_ray_0", CubeListBuilder.create().texOffs(60, 50).addBox(-0.4F, -1.5F, -0.25F, 0.8F, 3.0F, 0.5F), PartPose.offsetAndRotation(1.75F, -8.2F, -4.4F, 0.0F, 0.0F, (float) Math.toRadians(30.0F)));
        pd_head.addOrReplaceChild("cube_ray_1", CubeListBuilder.create().texOffs(64, 50).addBox(-0.4F, -1.5F, -0.25F, 0.8F, 3.0F, 0.5F), PartPose.offsetAndRotation(0.9059F, -8.2F, -4.4F, 0.0F, 0.0F, (float) Math.toRadians(15.0F)));
        pd_head.addOrReplaceChild("cube_ray_3", CubeListBuilder.create().texOffs(68, 50).addBox(-0.4F, -1.5F, -0.25F, 0.8F, 3.0F, 0.5F), PartPose.offsetAndRotation(-0.9059F, -8.2F, -4.4F, 0.0F, 0.0F, (float) Math.toRadians(-15.0F)));
        pd_head.addOrReplaceChild("cube_ray_4", CubeListBuilder.create().texOffs(72, 50).addBox(-0.4F, -1.5F, -0.25F, 0.8F, 3.0F, 0.5F), PartPose.offsetAndRotation(-1.75F, -8.2F, -4.4F, 0.0F, 0.0F, (float) Math.toRadians(-30.0F)));
        PartDefinition pd_halo = pd_head.addOrReplaceChild("halo", CubeListBuilder.create().texOffs(90, 43).addBox(-6.35F, -2.0995F, -0.35F, 0.7F, 4.199F, 0.7F).texOffs(84, 50).addBox(-0.3F, -9.25F, -0.3F, 0.6F, 2.5F, 0.6F), PartPose.offset(0.0F, -5.0F, 6.0F));
        pd_halo.addOrReplaceChild("cube_halo_1", CubeListBuilder.create().texOffs(94, 43).addBox(-0.35F, -2.0995F, -0.35F, 0.7F, 4.199F, 0.7F), PartPose.offsetAndRotation(-4.8541F, -3.5267F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(36.0F)));
        pd_halo.addOrReplaceChild("cube_halo_2", CubeListBuilder.create().texOffs(98, 43).addBox(-0.35F, -2.0995F, -0.35F, 0.7F, 4.199F, 0.7F), PartPose.offsetAndRotation(-1.8541F, -5.7063F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(72.0F)));
        pd_halo.addOrReplaceChild("cube_halo_3", CubeListBuilder.create().texOffs(102, 43).addBox(-0.35F, -2.0995F, -0.35F, 0.7F, 4.199F, 0.7F), PartPose.offsetAndRotation(1.8541F, -5.7063F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(108.0F)));
        pd_halo.addOrReplaceChild("cube_halo_4", CubeListBuilder.create().texOffs(106, 43).addBox(-0.35F, -2.0995F, -0.35F, 0.7F, 4.199F, 0.7F), PartPose.offsetAndRotation(4.8541F, -3.5267F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(144.0F)));
        pd_halo.addOrReplaceChild("cube_halo_5", CubeListBuilder.create().texOffs(110, 43).addBox(-0.35F, -2.0995F, -0.35F, 0.7F, 4.199F, 0.7F), PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(180.0F)));
        pd_halo.addOrReplaceChild("cube_halo_6", CubeListBuilder.create().texOffs(114, 43).addBox(-0.35F, -2.0995F, -0.35F, 0.7F, 4.199F, 0.7F), PartPose.offsetAndRotation(4.8541F, 3.5267F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(216.0F)));
        pd_halo.addOrReplaceChild("cube_halo_7", CubeListBuilder.create().texOffs(118, 43).addBox(-0.35F, -2.0995F, -0.35F, 0.7F, 4.199F, 0.7F), PartPose.offsetAndRotation(1.8541F, 5.7063F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(252.0F)));
        pd_halo.addOrReplaceChild("cube_halo_8", CubeListBuilder.create().texOffs(122, 43).addBox(-0.35F, -2.0995F, -0.35F, 0.7F, 4.199F, 0.7F), PartPose.offsetAndRotation(-1.8541F, 5.7063F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(288.0F)));
        pd_halo.addOrReplaceChild("cube_halo_9", CubeListBuilder.create().texOffs(0, 50).addBox(-0.35F, -2.0995F, -0.35F, 0.7F, 4.199F, 0.7F), PartPose.offsetAndRotation(-4.8541F, 3.5267F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(324.0F)));
        pd_halo.addOrReplaceChild("cube_hray_0", CubeListBuilder.create().texOffs(76, 50).addBox(-0.3F, -1.25F, -0.3F, 0.6F, 2.5F, 0.6F), PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(-90.0F)));
        pd_halo.addOrReplaceChild("cube_hray_1", CubeListBuilder.create().texOffs(80, 50).addBox(-0.3F, -1.25F, -0.3F, 0.6F, 2.5F, 0.6F), PartPose.offsetAndRotation(-5.6569F, -5.6568F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(-45.0F)));
        pd_halo.addOrReplaceChild("cube_hray_3", CubeListBuilder.create().texOffs(88, 50).addBox(-0.3F, -1.25F, -0.3F, 0.6F, 2.5F, 0.6F), PartPose.offsetAndRotation(5.6569F, -5.6568F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(45.0F)));
        pd_halo.addOrReplaceChild("cube_hray_4", CubeListBuilder.create().texOffs(92, 50).addBox(-0.3F, -1.25F, -0.3F, 0.6F, 2.5F, 0.6F), PartPose.offsetAndRotation(8.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(90.0F)));
        pd_halo.addOrReplaceChild("cube_hray_5", CubeListBuilder.create().texOffs(96, 50).addBox(-0.3F, -1.25F, -0.3F, 0.6F, 2.5F, 0.6F), PartPose.offsetAndRotation(5.6569F, 5.6568F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(135.0F)));
        pd_halo.addOrReplaceChild("cube_hray_6", CubeListBuilder.create().texOffs(100, 50).addBox(-0.3F, -1.25F, -0.3F, 0.6F, 2.5F, 0.6F), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(180.0F)));
        pd_halo.addOrReplaceChild("cube_hray_7", CubeListBuilder.create().texOffs(104, 50).addBox(-0.3F, -1.25F, -0.3F, 0.6F, 2.5F, 0.6F), PartPose.offsetAndRotation(-5.6569F, 5.6568F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(225.0F)));
        PartDefinition pd_rightArm = pd_body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(88, 0).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 12.0F, 3.0F).texOffs(48, 43).addBox(-1.6F, 8.5F, -1.6F, 3.2F, 3.0F, 3.2F, new CubeDeformation(0.2F)).texOffs(40, 31).addBox(-3.0F, -2.0F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.offset(-5.0F, -10.5F, 0.0F));
        PartDefinition pd_weapon = pd_rightArm.addOrReplaceChild("weapon", CubeListBuilder.create().texOffs(40, 50).addBox(-0.6F, -0.6F, -2.5F, 1.2F, 1.2F, 3.0F).texOffs(20, 56).addBox(-1.6F, -0.4F, -3.2F, 3.2F, 0.8F, 0.8F).texOffs(0, 17).addBox(-0.5F, -0.8F, -15.5F, 1.0F, 1.6F, 12.0F), PartPose.offset(0.0F, 11.5F, 0.0F));
        PartDefinition pd_leftArm = pd_body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(100, 0).mirror().addBox(-1.5F, -0.5F, -1.5F, 3.0F, 12.0F, 3.0F).mirror(false).texOffs(64, 43).mirror().addBox(-1.6F, 8.5F, -1.6F, 3.2F, 3.0F, 3.2F, new CubeDeformation(0.2F)).mirror(false).texOffs(60, 31).mirror().addBox(-2.0F, -2.0F, -2.5F, 5.0F, 3.0F, 5.0F).mirror(false), PartPose.offset(5.0F, -10.5F, 0.0F));
        PartDefinition pd_weaponL = pd_leftArm.addOrReplaceChild("weapon_l", CubeListBuilder.create().texOffs(50, 50).addBox(-0.6F, -0.6F, -2.5F, 1.2F, 1.2F, 3.0F).texOffs(30, 56).addBox(-1.6F, -0.4F, -3.2F, 3.2F, 0.8F, 0.8F).texOffs(26, 17).addBox(-0.5F, -0.8F, -15.5F, 1.0F, 1.6F, 12.0F), PartPose.offset(0.0F, 11.5F, 0.0F));
        PartDefinition pd_cape = pd_body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(88, 17).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 12.0F, 0.8F), PartPose.offset(0.0F, -12.0F, 2.6F));
        PartDefinition pd_rightLeg = pd_rootBone.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 0).addBox(-1.75F, 0.0F, -2.0F, 3.5F, 13.0F, 4.0F).texOffs(106, 17).addBox(-1.95F, 5.2F, -2.5F, 3.9F, 6.5F, 4.4F).texOffs(80, 31).addBox(-2.05F, 10.0F, -2.8F, 4.1F, 3.0F, 4.8F), PartPose.offset(-1.75F, -13.0F, 0.0F));
        PartDefinition pd_leftLeg = pd_rootBone.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 0).mirror().addBox(-1.75F, 0.0F, -2.0F, 3.5F, 13.0F, 4.0F).mirror(false).texOffs(0, 31).mirror().addBox(-1.95F, 5.2F, -2.5F, 3.9F, 6.5F, 4.4F).mirror(false).texOffs(100, 31).mirror().addBox(-2.05F, 10.0F, -2.8F, 4.1F, 3.0F, 4.8F).mirror(false), PartPose.offset(1.75F, -13.0F, 0.0F));
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
