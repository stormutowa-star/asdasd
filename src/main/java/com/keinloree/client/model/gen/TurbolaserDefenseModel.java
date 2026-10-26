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

/** GENERADO por bb2java.mjs desde blockbench/turbolaser_defense.bbmodel. No editar a mano: edita la spec y regenera. */
public class TurbolaserDefenseModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("keinloree", "turbolaser_defense"), "main");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/turbolaser_defense.png");
    public static final ResourceLocation GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath("keinloree", "textures/entity/turbolaser_defense_glow.png");
    private static final Vector3f CACHE = new Vector3f();

    private final ModelPart layerRoot;
    public final ModelPart rootBone;
    public final ModelPart base;
    public final ModelPart yaw;
    public final ModelPart pitch;
    public final ModelPart barrel0;
    public final ModelPart barrel1;
    public final ModelPart barrel2;
    public final ModelPart barrel3;

    public TurbolaserDefenseModel(ModelPart layerRoot) {
        this.layerRoot = layerRoot;
        this.rootBone = layerRoot.getChild("root");
        this.base = this.rootBone.getChild("base");
        this.yaw = this.base.getChild("yaw");
        this.pitch = this.yaw.getChild("pitch");
        this.barrel0 = this.pitch.getChild("barrel_0");
        this.barrel1 = this.pitch.getChild("barrel_1");
        this.barrel2 = this.pitch.getChild("barrel_2");
        this.barrel3 = this.pitch.getChild("barrel_3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition pd_rootBone = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pd_base = pd_rootBone.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 54).addBox(-5.949F, -3.0F, 8.0F, 11.898F, 3.0F, 6.0F).texOffs(24, 93).addBox(-4.7063F, -5.0F, 7.0F, 9.4127F, 2.0F, 4.0F).texOffs(54, 91).addBox(-5.3277F, -3.5F, 11.5F, 10.6553F, 0.6F, 1.0F).texOffs(72, 67).addBox(-12.6257F, -3.9F, 4.0984F, 1.6F, 1.0F, 1.6F).texOffs(94, 91).addBox(4.0984F, -3.9F, 11.0257F, 1.6F, 1.0F, 1.6F).texOffs(80, 124).addBox(11.0257F, -3.9F, -5.6983F, 1.6F, 1.0F, 1.6F).texOffs(88, 124).addBox(-5.6983F, -3.9F, -12.6257F, 1.6F, 1.0F, 1.6F).texOffs(96, 124).addBox(-14.7F, -2.2F, -1.0F, 0.8F, 1.4F, 2.0F).texOffs(98, 105).addBox(13.9F, -2.2F, -1.0F, 0.8F, 1.4F, 2.0F).texOffs(98, 111).addBox(-1.0F, -2.2F, 13.9F, 2.0F, 1.4F, 0.8F).texOffs(0, 0).addBox(-7.5F, -5.5F, -7.5F, 15.0F, 2.6F, 15.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        pd_base.addOrReplaceChild("cube_plate_1", CubeListBuilder.create().texOffs(0, 63).addBox(-5.949F, -1.5F, -3.0F, 11.898F, 3.0F, 6.0F), PartPose.offsetAndRotation(-7.7782F, -1.5F, 7.7782F, 0.0F, (float) Math.toRadians(-45.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_plate_2", CubeListBuilder.create().texOffs(80, 52).addBox(-5.949F, -1.5F, -3.0F, 11.898F, 3.0F, 6.0F), PartPose.offsetAndRotation(-11.0F, -1.5F, 0.0F, 0.0F, (float) Math.toRadians(-90.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_plate_3", CubeListBuilder.create().texOffs(80, 61).addBox(-5.949F, -1.5F, -3.0F, 11.898F, 3.0F, 6.0F), PartPose.offsetAndRotation(-7.7782F, -1.5F, -7.7782F, 0.0F, (float) Math.toRadians(-135.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_plate_4", CubeListBuilder.create().texOffs(36, 67).addBox(-5.949F, -1.5F, -3.0F, 11.898F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, -1.5F, -11.0F, 0.0F, (float) Math.toRadians(-180.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_plate_5", CubeListBuilder.create().texOffs(0, 72).addBox(-5.949F, -1.5F, -3.0F, 11.898F, 3.0F, 6.0F), PartPose.offsetAndRotation(7.7782F, -1.5F, -7.7782F, 0.0F, (float) Math.toRadians(-225.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_plate_6", CubeListBuilder.create().texOffs(72, 70).addBox(-5.949F, -1.5F, -3.0F, 11.898F, 3.0F, 6.0F), PartPose.offsetAndRotation(11.0F, -1.5F, 0.0F, 0.0F, (float) Math.toRadians(-270.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_plate_7", CubeListBuilder.create().texOffs(36, 76).addBox(-5.949F, -1.5F, -3.0F, 11.898F, 3.0F, 6.0F), PartPose.offsetAndRotation(7.7782F, -1.5F, 7.7782F, 0.0F, (float) Math.toRadians(-315.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_ring_1", CubeListBuilder.create().texOffs(0, 109).addBox(-4.7063F, -1.0F, -2.0F, 9.4127F, 2.0F, 4.0F), PartPose.offsetAndRotation(-6.364F, -4.0F, 6.364F, 0.0F, (float) Math.toRadians(-45.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_ring_2", CubeListBuilder.create().texOffs(0, 115).addBox(-4.7064F, -1.0F, -2.0F, 9.4127F, 2.0F, 4.0F), PartPose.offsetAndRotation(-9.0F, -4.0F, 0.0F, 0.0F, (float) Math.toRadians(-90.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_ring_3", CubeListBuilder.create().texOffs(0, 121).addBox(-4.7063F, -1.0F, -2.0F, 9.4127F, 2.0F, 4.0F), PartPose.offsetAndRotation(-6.364F, -4.0F, -6.364F, 0.0F, (float) Math.toRadians(-135.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_ring_4", CubeListBuilder.create().texOffs(28, 109).addBox(-4.7063F, -1.0F, -2.0F, 9.4127F, 2.0F, 4.0F), PartPose.offsetAndRotation(0.0F, -4.0F, -9.0F, 0.0F, (float) Math.toRadians(-180.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_ring_5", CubeListBuilder.create().texOffs(30, 99).addBox(-4.7063F, -1.0F, -2.0F, 9.4127F, 2.0F, 4.0F), PartPose.offsetAndRotation(6.364F, -4.0F, -6.364F, 0.0F, (float) Math.toRadians(-225.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_ring_6", CubeListBuilder.create().texOffs(52, 93).addBox(-4.7063F, -1.0F, -2.0F, 9.4127F, 2.0F, 4.0F), PartPose.offsetAndRotation(9.0F, -4.0F, 0.0F, 0.0F, (float) Math.toRadians(-270.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_ring_7", CubeListBuilder.create().texOffs(54, 85).addBox(-4.7063F, -1.0F, -2.0F, 9.4127F, 2.0F, 4.0F), PartPose.offsetAndRotation(6.364F, -4.0F, 6.364F, 0.0F, (float) Math.toRadians(-315.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_glowline_1", CubeListBuilder.create().texOffs(84, 103).addBox(-5.3277F, -0.3F, -0.5F, 10.6553F, 0.6F, 1.0F), PartPose.offsetAndRotation(-8.4853F, -3.2F, 8.4853F, 0.0F, (float) Math.toRadians(-45.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_glowline_2", CubeListBuilder.create().texOffs(80, 122).addBox(-5.3277F, -0.3F, -0.5F, 10.6553F, 0.6F, 1.0F), PartPose.offsetAndRotation(-12.0F, -3.2F, 0.0F, 0.0F, (float) Math.toRadians(-90.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_glowline_3", CubeListBuilder.create().texOffs(104, 105).addBox(-5.3277F, -0.3F, -0.5F, 10.6553F, 0.6F, 1.0F), PartPose.offsetAndRotation(-8.4853F, -3.2F, -8.4853F, 0.0F, (float) Math.toRadians(-135.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_glowline_4", CubeListBuilder.create().texOffs(104, 107).addBox(-5.3277F, -0.3F, -0.5F, 10.6553F, 0.6F, 1.0F), PartPose.offsetAndRotation(0.0F, -3.2F, -12.0F, 0.0F, (float) Math.toRadians(-180.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_glowline_5", CubeListBuilder.create().texOffs(104, 109).addBox(-5.3277F, -0.3F, -0.5F, 10.6553F, 0.6F, 1.0F), PartPose.offsetAndRotation(8.4853F, -3.2F, -8.4853F, 0.0F, (float) Math.toRadians(-225.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_glowline_6", CubeListBuilder.create().texOffs(104, 111).addBox(-5.3277F, -0.3F, -0.5F, 10.6553F, 0.6F, 1.0F), PartPose.offsetAndRotation(12.0F, -3.2F, 0.0F, 0.0F, (float) Math.toRadians(-270.0F), 0.0F));
        pd_base.addOrReplaceChild("cube_glowline_7", CubeListBuilder.create().texOffs(104, 113).addBox(-5.3277F, -0.3F, -0.5F, 10.6553F, 0.6F, 1.0F), PartPose.offsetAndRotation(8.4853F, -3.2F, 8.4853F, 0.0F, (float) Math.toRadians(-315.0F), 0.0F));
        PartDefinition pd_yaw = pd_base.addOrReplaceChild("yaw", CubeListBuilder.create().texOffs(60, 0).addBox(-7.0F, -7.0F, -2.0F, 14.0F, 7.0F, 11.0F).texOffs(82, 86).addBox(-2.5F, -9.2F, 4.0F, 5.0F, 1.0F, 4.0F).texOffs(36, 48).addBox(-7.5F, -11.0F, -6.0F, 2.2F, 11.0F, 8.0F).texOffs(0, 81).addBox(-7.7F, -11.5F, -6.2F, 2.6F, 0.6F, 8.4F).texOffs(108, 26).addBox(-7.9F, -1.35F, 0.5F, 0.4F, 0.7F, 8.0F).texOffs(110, 0).addBox(-7.8F, -6.0F, 1.0F, 0.8F, 5.0F, 8.0F).texOffs(116, 35).addBox(-8.05F, -5.5F, 3.0F, 0.3F, 4.0F, 5.0F).texOffs(58, 99).addBox(-8.35F, -2.45F, 3.0F, 0.5F, 0.5F, 5.0F).texOffs(116, 53).addBox(-8.35F, -3.75F, 3.0F, 0.5F, 0.5F, 5.0F).texOffs(116, 59).addBox(-8.35F, -5.05F, 3.0F, 0.5F, 0.5F, 5.0F).texOffs(120, 79).addBox(-8.2F, -8.9F, -2.787F, 0.8F, 3.8F, 1.574F).texOffs(116, 121).addBox(-7.3F, -7.7F, 8.1F, 1.0F, 1.0F, 1.0F).texOffs(66, 126).addBox(-6.9F, -12.3F, -5.9F, 1.0F, 1.0F, 1.0F).texOffs(58, 48).addBox(5.3F, -11.0F, -6.0F, 2.2F, 11.0F, 8.0F).texOffs(0, 91).addBox(5.1F, -11.5F, -6.2F, 2.6F, 0.6F, 8.4F).texOffs(108, 70).addBox(7.5F, -1.35F, 0.5F, 0.4F, 0.7F, 8.0F).texOffs(110, 13).addBox(7.0F, -6.0F, 1.0F, 0.8F, 5.0F, 8.0F).texOffs(116, 44).addBox(7.75F, -5.5F, 3.0F, 0.3F, 4.0F, 5.0F).texOffs(28, 122).addBox(7.85F, -2.45F, 3.0F, 0.5F, 0.5F, 5.0F).texOffs(40, 122).addBox(7.85F, -3.75F, 3.0F, 0.5F, 0.5F, 5.0F).texOffs(96, 79).addBox(7.85F, -5.05F, 3.0F, 0.5F, 0.5F, 5.0F).texOffs(104, 121).addBox(7.4F, -8.9F, -2.787F, 0.8F, 3.8F, 1.574F).texOffs(32, 81).addBox(6.3F, -7.7F, 8.1F, 1.0F, 1.0F, 1.0F).texOffs(32, 83).addBox(5.9F, -12.3F, -5.9F, 1.0F, 1.0F, 1.0F).texOffs(124, 85).addBox(-2.1F, -8.0F, 9.6F, 0.6F, 8.0F, 0.6F).texOffs(94, 94).addBox(1.5F, -8.0F, 9.6F, 0.6F, 8.0F, 0.6F).texOffs(92, 109).addBox(-1.8F, -1.45F, 9.65F, 3.6F, 0.5F, 0.5F).texOffs(56, 126).addBox(-1.8F, -3.65F, 9.65F, 3.6F, 0.5F, 0.5F).texOffs(116, 123).addBox(-1.8F, -5.85F, 9.65F, 3.6F, 0.5F, 0.5F).texOffs(116, 65).addBox(1.0F, -9.9F, 2.5F, 3.0F, 1.4F, 3.0F).texOffs(24, 99).addBox(1.9F, -9.6F, 2.25F, 1.2F, 0.8F, 0.3F).texOffs(52, 122).addBox(3.7F, -13.0F, 7.7F, 0.6F, 5.0F, 0.6F).texOffs(52, 105).addBox(3.55F, -13.75F, 7.55F, 0.9F, 0.9F, 0.9F).texOffs(98, 94).addBox(-4.8F, -16.5F, 6.7F, 0.6F, 8.0F, 0.6F).texOffs(52, 107).addBox(-5.0F, -17.3F, 6.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, -5.0F, 0.0F));
        pd_yaw.addOrReplaceChild("cube_roof", CubeListBuilder.create().texOffs(36, 36).addBox(-6.5F, -0.7F, -5.0F, 13.0F, 1.4F, 10.0F), PartPose.offsetAndRotation(0.0F, -7.6F, 4.0F, (float) Math.toRadians(6.0F), 0.0F, 0.0F));
        pd_yaw.addOrReplaceChild("cube_boss_r_1", CubeListBuilder.create().texOffs(90, 115).addBox(-0.42F, -1.9F, -0.787F, 0.84F, 3.8F, 1.574F), PartPose.offsetAndRotation(-7.8F, -7.0F, -2.0F, (float) Math.toRadians(-90.0F), 0.0F, 0.0F));
        pd_yaw.addOrReplaceChild("cube_boss_r_2", CubeListBuilder.create().texOffs(96, 115).addBox(-0.44F, -1.9F, -0.787F, 0.88F, 3.8F, 1.574F), PartPose.offsetAndRotation(-7.8F, -7.0F, -2.0F, (float) Math.toRadians(-45.0F), 0.0F, 0.0F));
        pd_yaw.addOrReplaceChild("cube_boss_r_3", CubeListBuilder.create().texOffs(102, 115).addBox(-0.46F, -1.9F, -0.787F, 0.92F, 3.8F, 1.574F), PartPose.offsetAndRotation(-7.8F, -7.0F, -2.0F, (float) Math.toRadians(45.0F), 0.0F, 0.0F));
        pd_yaw.addOrReplaceChild("cube_boss_l_1", CubeListBuilder.create().texOffs(108, 115).addBox(-0.42F, -1.9F, -0.787F, 0.84F, 3.8F, 1.574F), PartPose.offsetAndRotation(7.8F, -7.0F, -2.0F, (float) Math.toRadians(-90.0F), 0.0F, 0.0F));
        pd_yaw.addOrReplaceChild("cube_boss_l_2", CubeListBuilder.create().texOffs(110, 121).addBox(-0.44F, -1.9F, -0.787F, 0.88F, 3.8F, 1.574F), PartPose.offsetAndRotation(7.8F, -7.0F, -2.0F, (float) Math.toRadians(-45.0F), 0.0F, 0.0F));
        pd_yaw.addOrReplaceChild("cube_boss_l_3", CubeListBuilder.create().texOffs(114, 115).addBox(-0.46F, -1.9F, -0.787F, 0.92F, 3.8F, 1.574F), PartPose.offsetAndRotation(7.8F, -7.0F, -2.0F, (float) Math.toRadians(45.0F), 0.0F, 0.0F));
        PartDefinition pd_pitch = pd_yaw.addOrReplaceChild("pitch", CubeListBuilder.create().texOffs(82, 36).addBox(-4.5F, -4.0F, -3.0F, 9.0F, 8.0F, 8.0F).texOffs(0, 101).addBox(-4.0F, -4.5F, -2.5F, 8.0F, 0.6F, 7.0F).texOffs(24, 85).addBox(-4.0F, 3.7F, -2.5F, 8.0F, 0.6F, 7.0F).texOffs(116, 125).addBox(-2.0F, -4.8F, -0.9F, 4.0F, 0.4F, 0.8F).texOffs(108, 91).addBox(-2.0F, -4.8F, 1.5F, 4.0F, 0.4F, 0.8F).texOffs(30, 105).addBox(-4.7F, -1.5F, -3.8F, 9.4F, 3.0F, 0.8F).texOffs(28, 115).addBox(-5.4F, -4.8F, -5.0F, 4.4F, 4.4F, 2.0F).texOffs(56, 105).addBox(1.0F, -4.8F, -5.0F, 4.4F, 4.4F, 2.0F).texOffs(56, 119).addBox(-5.4F, 0.4F, -5.0F, 4.4F, 4.4F, 2.0F).texOffs(80, 91).addBox(1.0F, 0.4F, -5.0F, 4.4F, 4.4F, 2.0F), PartPose.offset(0.0F, -7.0F, -2.0F));
        PartDefinition pd_barrel0 = pd_pitch.addOrReplaceChild("barrel_0", CubeListBuilder.create().texOffs(0, 18).addBox(-1.5F, -1.5F, -15.0F, 3.0F, 3.0F, 15.0F).texOffs(72, 79).addBox(-1.85F, -1.85F, -4.5F, 3.7F, 3.7F, 1.0F).texOffs(70, 112).addBox(-1.85F, -1.85F, -10.0F, 3.7F, 3.7F, 1.0F).texOffs(108, 79).addBox(-1.9F, -1.9F, -16.2F, 3.8F, 3.8F, 1.2F).texOffs(42, 115).addBox(-2.4F, -2.4F, -17.4F, 4.8F, 4.8F, 1.2F).texOffs(24, 81).addBox(-1.1F, -1.1F, -17.7F, 2.2F, 2.2F, 0.4F), PartPose.offset(-3.2F, -2.6F, -3.0F));
        PartDefinition pd_barrel1 = pd_pitch.addOrReplaceChild("barrel_1", CubeListBuilder.create().texOffs(36, 18).addBox(-1.5F, -1.5F, -15.0F, 3.0F, 3.0F, 15.0F).texOffs(70, 117).addBox(-1.85F, -1.85F, -4.5F, 3.7F, 3.7F, 1.0F).texOffs(70, 122).addBox(-1.85F, -1.85F, -10.0F, 3.7F, 3.7F, 1.0F).texOffs(100, 85).addBox(-1.9F, -1.9F, -16.2F, 3.8F, 3.8F, 1.2F).texOffs(56, 112).addBox(-2.4F, -2.4F, -17.4F, 4.8F, 4.8F, 1.2F).texOffs(90, 111).addBox(-1.1F, -1.1F, -17.7F, 2.2F, 2.2F, 0.4F), PartPose.offset(3.2F, -2.6F, -3.0F));
        PartDefinition pd_barrel2 = pd_pitch.addOrReplaceChild("barrel_2", CubeListBuilder.create().texOffs(72, 18).addBox(-1.5F, -1.5F, -15.0F, 3.0F, 3.0F, 15.0F).texOffs(80, 112).addBox(-1.85F, -1.85F, -4.5F, 3.7F, 3.7F, 1.0F).texOffs(82, 106).addBox(-1.85F, -1.85F, -10.0F, 3.7F, 3.7F, 1.0F).texOffs(112, 85).addBox(-1.9F, -1.9F, -16.2F, 3.8F, 3.8F, 1.2F).texOffs(70, 99).addBox(-2.4F, -2.4F, -17.4F, 4.8F, 4.8F, 1.2F).texOffs(120, 115).addBox(-1.1F, -1.1F, -17.7F, 2.2F, 2.2F, 0.4F), PartPose.offset(-3.2F, 2.6F, -3.0F));
        PartDefinition pd_barrel3 = pd_pitch.addOrReplaceChild("barrel_3", CubeListBuilder.create().texOffs(0, 36).addBox(-1.5F, -1.5F, -15.0F, 3.0F, 3.0F, 15.0F).texOffs(84, 98).addBox(-1.85F, -1.85F, -4.5F, 3.7F, 3.7F, 1.0F).texOffs(80, 117).addBox(-1.85F, -1.85F, -10.0F, 3.7F, 3.7F, 1.0F).texOffs(70, 106).addBox(-1.9F, -1.9F, -16.2F, 3.8F, 3.8F, 1.2F).texOffs(82, 79).addBox(-2.4F, -2.4F, -17.4F, 4.8F, 4.8F, 1.2F).texOffs(120, 119).addBox(-1.1F, -1.1F, -17.7F, 2.2F, 2.2F, 0.4F), PartPose.offset(3.2F, 2.6F, -3.0F));
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
