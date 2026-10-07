package com.ricardo.blueeyes.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** GENERADO por tools/build_assets.py - no editar a mano. */
public final class BlueEyesLayer {
    private BlueEyesLayer() {}

    public static final int TEX_W = 512;
    public static final int TEX_H = 256;

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition p_pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create()
                .texOffs(236, 49).addBox(-10F, -7F, -10F, 20F, 14F, 20F),
                PartPose.offset(0F, -33F, -3F));
        PartDefinition p_chest = p_pelvis.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(140, 49).addBox(-13F, -22F, -10F, 26F, 22F, 22F),
                PartPose.offsetAndRotation(0F, -5F, 5F, -0.18F, 0F, 0F));
        PartDefinition p_neck1 = p_chest.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(0, 98).addBox(-8.5F, -12F, -7.5F, 17F, 13F, 15F)
                .texOffs(488, 168).addBox(-1.5F, -14.5F, -7F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -21F, 5F, 0.5F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(160, 98).addBox(-8F, -12F, -7F, 16F, 13F, 14F)
                .texOffs(0, 181).addBox(-1.5F, -14.5F, -6.5F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -11.5F, 0F, 0.12F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(220, 98).addBox(-7.5F, -12F, -7F, 15F, 13F, 14F)
                .texOffs(16, 181).addBox(-1.5F, -14.5F, -6.5F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -11.5F, 0F, -0.32F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(338, 98).addBox(-7F, -11F, -6.5F, 14F, 12F, 13F)
                .texOffs(32, 181).addBox(-1.5F, -13.5F, -6F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -11.5F, 0F, -0.46F, 0F, 0F));
        PartDefinition p_head = p_neck4.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(398, 49).addBox(-9F, -13F, -14.5F, 18F, 14F, 17F)
                .texOffs(36, 150).addBox(-10F, -7F, -8.5F, 20F, 7F, 10F)
                .texOffs(108, 126).addBox(-6.5F, -9.5F, -29F, 13F, 8F, 15F)
                .texOffs(362, 150).addBox(-5F, -8F, -36.5F, 10F, 7F, 8F)
                .texOffs(132, 190).addBox(-4F, -9.5F, -36F, 2F, 2F, 2F)
                .texOffs(140, 190).addBox(2F, -9.5F, -36F, 2F, 2F, 2F)
                .texOffs(488, 181).addBox(-5.5F, -2.3F, -16F, 1F, 3F, 2F)
                .texOffs(494, 181).addBox(4.5F, -2.3F, -16F, 1F, 3F, 2F)
                .texOffs(500, 181).addBox(-5.5F, -2.3F, -20F, 1F, 3F, 2F)
                .texOffs(506, 181).addBox(4.5F, -2.3F, -20F, 1F, 3F, 2F)
                .texOffs(0, 190).addBox(-5.5F, -2.3F, -24F, 1F, 3F, 2F)
                .texOffs(6, 190).addBox(4.5F, -2.3F, -24F, 1F, 3F, 2F)
                .texOffs(12, 190).addBox(-5.5F, -2.3F, -28F, 1F, 3F, 2F)
                .texOffs(18, 190).addBox(4.5F, -2.3F, -28F, 1F, 3F, 2F)
                .texOffs(24, 190).addBox(-5.5F, -2.3F, -32F, 1F, 3F, 2F)
                .texOffs(30, 190).addBox(4.5F, -2.3F, -32F, 1F, 3F, 2F)
                .texOffs(36, 190).addBox(-5.5F, -2.3F, -36F, 1F, 3F, 2F)
                .texOffs(42, 190).addBox(4.5F, -2.3F, -36F, 1F, 3F, 2F)
                .texOffs(104, 181).addBox(-4.6F, -1.9F, -37.5F, 2F, 7F, 2F)
                .texOffs(112, 181).addBox(2.6F, -1.9F, -37.5F, 2F, 7F, 2F)
                .texOffs(398, 150).addBox(-9.3F, -15F, -14F, 7F, 3F, 11F)
                .texOffs(434, 150).addBox(2.3F, -15F, -14F, 7F, 3F, 11F),
                PartPose.offsetAndRotation(0F, -11F, 0F, 0.5F, 0F, 0F));
        PartDefinition p_hornR = p_head.addOrReplaceChild("hornR", CubeListBuilder.create()
                .texOffs(214, 126).addBox(-2.5F, -2.5F, 0F, 5F, 5F, 18F),
                PartPose.offsetAndRotation(-6.5F, -12F, -0.5F, -0.18F, -0.2F, 0F));
        PartDefinition p_horn2R = p_hornR.addOrReplaceChild("horn2R", CubeListBuilder.create()
                .texOffs(454, 126).addBox(-2F, -2F, -1F, 4F, 4F, 14F),
                PartPose.offsetAndRotation(0F, 0F, 17F, 0.2F, -0.08F, 0F));
        PartDefinition p_hornL = p_head.addOrReplaceChild("hornL", CubeListBuilder.create()
                .texOffs(260, 126).addBox(-2.5F, -2.5F, 0F, 5F, 5F, 18F),
                PartPose.offsetAndRotation(6.5F, -12F, -0.5F, -0.18F, 0.2F, 0F));
        PartDefinition p_horn2L = p_hornL.addOrReplaceChild("horn2L", CubeListBuilder.create()
                .texOffs(0, 150).addBox(-2F, -2F, -1F, 4F, 4F, 14F),
                PartPose.offsetAndRotation(0F, 0F, 17F, 0.2F, 0.08F, 0F));
        PartDefinition p_crest0 = p_head.addOrReplaceChild("crest0", CubeListBuilder.create()
                .texOffs(348, 168).addBox(-1.5F, -3F, 0F, 3F, 4F, 7F),
                PartPose.offsetAndRotation(0F, -12.5F, -9F, 0.9F, 0F, 0F));
        PartDefinition p_crest1 = p_head.addOrReplaceChild("crest1", CubeListBuilder.create()
                .texOffs(0, 168).addBox(-1.5F, -3F, 0F, 3F, 4F, 9F),
                PartPose.offsetAndRotation(0F, -12.5F, -4F, 1F, 0F, 0F));
        PartDefinition p_crest2 = p_head.addOrReplaceChild("crest2", CubeListBuilder.create()
                .texOffs(180, 168).addBox(-1.5F, -3F, 0F, 3F, 4F, 8F),
                PartPose.offsetAndRotation(0F, -12.5F, 1F, 1.1F, 0F, 0F));
        PartDefinition p_cheekR = p_head.addOrReplaceChild("cheekR", CubeListBuilder.create()
                .texOffs(222, 150).addBox(-1.5F, -2F, 0F, 3F, 4F, 12F),
                PartPose.offsetAndRotation(-10F, -3F, -3F, 0F, -0.75F, 0F));
        PartDefinition p_cheek2R = p_head.addOrReplaceChild("cheek2R", CubeListBuilder.create()
                .texOffs(304, 168).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 8F),
                PartPose.offsetAndRotation(-10F, -7F, -7F, 0F, -0.55F, 0F));
        PartDefinition p_cheekL = p_head.addOrReplaceChild("cheekL", CubeListBuilder.create()
                .texOffs(252, 150).addBox(-1.5F, -2F, 0F, 3F, 4F, 12F),
                PartPose.offsetAndRotation(10F, -3F, -3F, 0F, 0.75F, 0F));
        PartDefinition p_cheek2L = p_head.addOrReplaceChild("cheek2L", CubeListBuilder.create()
                .texOffs(326, 168).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 8F),
                PartPose.offsetAndRotation(10F, -7F, -7F, 0F, 0.55F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(316, 49).addBox(-6.5F, -1F, -29F, 13F, 4F, 28F)
                .texOffs(120, 168).addBox(-4.5F, 0F, -38F, 9F, 3F, 9F)
                .texOffs(48, 190).addBox(-5.5F, -4.3F, -11F, 1F, 3F, 2F)
                .texOffs(54, 190).addBox(4.5F, -4.3F, -11F, 1F, 3F, 2F)
                .texOffs(60, 190).addBox(-5.5F, -4.3F, -15F, 1F, 3F, 2F)
                .texOffs(66, 190).addBox(4.5F, -4.3F, -15F, 1F, 3F, 2F)
                .texOffs(72, 190).addBox(-5.5F, -4.3F, -19F, 1F, 3F, 2F)
                .texOffs(78, 190).addBox(4.5F, -4.3F, -19F, 1F, 3F, 2F)
                .texOffs(84, 190).addBox(-5.5F, -4.3F, -23F, 1F, 3F, 2F)
                .texOffs(90, 190).addBox(4.5F, -4.3F, -23F, 1F, 3F, 2F)
                .texOffs(96, 190).addBox(-5.5F, -4.3F, -27F, 1F, 3F, 2F)
                .texOffs(102, 190).addBox(4.5F, -4.3F, -27F, 1F, 3F, 2F)
                .texOffs(108, 190).addBox(-5.5F, -4.3F, -31F, 1F, 3F, 2F)
                .texOffs(114, 190).addBox(4.5F, -4.3F, -31F, 1F, 3F, 2F)
                .texOffs(120, 190).addBox(-5.5F, -4.3F, -35F, 1F, 3F, 2F)
                .texOffs(126, 190).addBox(4.5F, -4.3F, -35F, 1F, 3F, 2F)
                .texOffs(472, 181).addBox(-4.2F, -5.7F, -38F, 2F, 5F, 2F)
                .texOffs(480, 181).addBox(2.2F, -5.7F, -38F, 2F, 5F, 2F)
                .texOffs(274, 168).addBox(-3F, 2.5F, -12.5F, 6F, 2F, 9F),
                PartPose.offset(0F, -0.5F, -3F));
        PartDefinition p_armR = p_chest.addOrReplaceChild("armR", CubeListBuilder.create()
                .texOffs(36, 126).addBox(-4.5F, -2F, -4.5F, 9F, 15F, 9F),
                PartPose.offsetAndRotation(-17F, -17F, 4F, -0.7F, -0.28F, -0.15F));
        PartDefinition p_forearmR = p_armR.addOrReplaceChild("forearmR", CubeListBuilder.create()
                .texOffs(306, 126).addBox(-4F, -2F, -4F, 8F, 14F, 8F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -1F, 0F, 0F));
        PartDefinition p_handR = p_forearmR.addOrReplaceChild("handR", CubeListBuilder.create()
                .texOffs(282, 150).addBox(-5F, -1.5F, -5F, 10F, 5F, 10F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -0.15F, 0F, 0F));
        PartDefinition p_claw0R = p_handR.addOrReplaceChild("claw0R", CubeListBuilder.create()
                .texOffs(348, 181).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(-3.4F, 4F, 0F, 0.25F, -0.17F, 0F));
        PartDefinition p_claw20R = p_claw0R.addOrReplaceChild("claw20R", CubeListBuilder.create()
                .texOffs(202, 168).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_claw1R = p_handR.addOrReplaceChild("claw1R", CubeListBuilder.create()
                .texOffs(360, 181).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0.25F, 0F, 0F));
        PartDefinition p_claw21R = p_claw1R.addOrReplaceChild("claw21R", CubeListBuilder.create()
                .texOffs(214, 168).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_claw2R = p_handR.addOrReplaceChild("claw2R", CubeListBuilder.create()
                .texOffs(372, 181).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(3.4F, 4F, 0F, 0.25F, 0.17F, 0F));
        PartDefinition p_claw22R = p_claw2R.addOrReplaceChild("claw22R", CubeListBuilder.create()
                .texOffs(226, 168).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_thumbR = p_handR.addOrReplaceChild("thumbR", CubeListBuilder.create()
                .texOffs(368, 168).addBox(-1.5F, -0.5F, -1.5F, 3F, 8F, 3F),
                PartPose.offsetAndRotation(-5.5F, 1F, -2F, 0.4F, 0F, -0.5F));
        PartDefinition p_armL = p_chest.addOrReplaceChild("armL", CubeListBuilder.create()
                .texOffs(72, 126).addBox(-4.5F, -2F, -4.5F, 9F, 15F, 9F),
                PartPose.offsetAndRotation(17F, -17F, 4F, -0.7F, 0.28F, 0.15F));
        PartDefinition p_forearmL = p_armL.addOrReplaceChild("forearmL", CubeListBuilder.create()
                .texOffs(338, 126).addBox(-4F, -2F, -4F, 8F, 14F, 8F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -1F, 0F, 0F));
        PartDefinition p_handL = p_forearmL.addOrReplaceChild("handL", CubeListBuilder.create()
                .texOffs(322, 150).addBox(-5F, -1.5F, -5F, 10F, 5F, 10F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -0.15F, 0F, 0F));
        PartDefinition p_claw0L = p_handL.addOrReplaceChild("claw0L", CubeListBuilder.create()
                .texOffs(384, 181).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(-3.4F, 4F, 0F, 0.25F, -0.17F, 0F));
        PartDefinition p_claw20L = p_claw0L.addOrReplaceChild("claw20L", CubeListBuilder.create()
                .texOffs(238, 168).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_claw1L = p_handL.addOrReplaceChild("claw1L", CubeListBuilder.create()
                .texOffs(396, 181).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0.25F, 0F, 0F));
        PartDefinition p_claw21L = p_claw1L.addOrReplaceChild("claw21L", CubeListBuilder.create()
                .texOffs(250, 168).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_claw2L = p_handL.addOrReplaceChild("claw2L", CubeListBuilder.create()
                .texOffs(408, 181).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(3.4F, 4F, 0F, 0.25F, 0.17F, 0F));
        PartDefinition p_claw22L = p_claw2L.addOrReplaceChild("claw22L", CubeListBuilder.create()
                .texOffs(262, 168).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_thumbL = p_handL.addOrReplaceChild("thumbL", CubeListBuilder.create()
                .texOffs(380, 168).addBox(-1.5F, -0.5F, -1.5F, 3F, 8F, 3F),
                PartPose.offsetAndRotation(5.5F, 1F, -2F, 0.4F, 0F, 0.5F));
        PartDefinition p_wingR = p_chest.addOrReplaceChild("wingR", CubeListBuilder.create()
                .texOffs(24, 168).addBox(-18F, -3F, -3F, 18F, 6F, 6F)
                .texOffs(392, 168).addBox(-36.5F, -2.5F, -2.5F, 19F, 5F, 5F)
                .texOffs(0, 0).addBox(-36F, -0.5F, 0F, 36F, 1F, 48F),
                PartPose.offsetAndRotation(-11F, -19F, -8F, -0.85F, 0.55F, 0.8F));
        PartDefinition p_wingOutR = p_wingR.addOrReplaceChild("wingOutR", CubeListBuilder.create()
                .texOffs(336, 0).addBox(-22F, -0.5F, 0F, 22F, 1F, 48F)
                .texOffs(120, 181).addBox(-22F, -2F, -1F, 22F, 4F, 4F)
                .texOffs(420, 181).addBox(-2.5F, -6F, -1F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(-36F, 0F, 0F, 0F, 0.1F, 0.18F));
        PartDefinition p_wingL = p_chest.addOrReplaceChild("wingL", CubeListBuilder.create()
                .texOffs(72, 168).addBox(0F, -3F, -3F, 18F, 6F, 6F)
                .texOffs(440, 168).addBox(17.5F, -2.5F, -2.5F, 19F, 5F, 5F)
                .texOffs(168, 0).addBox(0F, -0.5F, 0F, 36F, 1F, 48F),
                PartPose.offsetAndRotation(11F, -19F, -8F, -0.85F, -0.55F, -0.8F));
        PartDefinition p_wingOutL = p_wingL.addOrReplaceChild("wingOutL", CubeListBuilder.create()
                .texOffs(0, 49).addBox(0F, -0.5F, 0F, 22F, 1F, 48F)
                .texOffs(172, 181).addBox(0F, -2F, -1F, 22F, 4F, 4F)
                .texOffs(432, 181).addBox(-0.5F, -6F, -1F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(36F, 0F, 0F, 0F, -0.1F, -0.18F));
        PartDefinition p_tail1 = p_pelvis.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(278, 98).addBox(-8F, -6F, -0.5F, 16F, 12F, 14F)
                .texOffs(48, 181).addBox(-1F, -9F, 4.5F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -0.5F, 10F, -0.1F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(392, 98).addBox(-7F, -5.5F, -0.5F, 14F, 11F, 13F)
                .texOffs(62, 181).addBox(-1F, -8.5F, 4F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 13F, 0.02F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(164, 126).addBox(-6F, -5F, -0.5F, 12F, 10F, 13F)
                .texOffs(76, 181).addBox(-1F, -8F, 4F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 12F, 0.16F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(370, 126).addBox(-5F, -4.5F, -0.5F, 10F, 9F, 12F)
                .texOffs(90, 181).addBox(-1F, -7.5F, 3.5F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 12F, 0.28F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(414, 126).addBox(-4F, -3.5F, -0.5F, 8F, 7F, 12F)
                .texOffs(320, 181).addBox(-1F, -6F, 3.5F, 2F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.34F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(188, 150).addBox(-3F, -2.5F, -0.5F, 6F, 5F, 11F)
                .texOffs(334, 181).addBox(-1F, -5F, 3F, 2F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.4F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(470, 150).addBox(-2F, -1.5F, -0.5F, 4F, 3F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.34F, 0F, 0F));
        PartDefinition p_tailtip = p_tail7.addOrReplaceChild("tailtip", CubeListBuilder.create()
                .texOffs(156, 168).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.25F, 0F, 0F));
        PartDefinition p_thighR = p_pelvis.addOrReplaceChild("thighR", CubeListBuilder.create()
                .texOffs(64, 98).addBox(-5.5F, -1F, -6.5F, 11F, 15F, 13F),
                PartPose.offsetAndRotation(-12F, 3F, 0F, -0.55F, 0F, 0F));
        PartDefinition p_shinR = p_thighR.addOrReplaceChild("shinR", CubeListBuilder.create()
                .texOffs(446, 98).addBox(-4.5F, -1F, -4.5F, 9F, 15F, 9F),
                PartPose.offsetAndRotation(0F, 13F, -0.5F, 1.05F, 0F, 0F));
        PartDefinition p_footR = p_shinR.addOrReplaceChild("footR", CubeListBuilder.create()
                .texOffs(96, 150).addBox(-5.5F, -1F, -9F, 11F, 5F, 12F),
                PartPose.offsetAndRotation(0F, 13.5F, 0F, -0.5F, 0F, 0F));
        PartDefinition p_toe0R = p_footR.addOrReplaceChild("toe0R", CubeListBuilder.create()
                .texOffs(224, 181).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(-3.2F, 2F, -7.2F, 0.35F, -0.16F, 0F));
        PartDefinition p_toe1R = p_footR.addOrReplaceChild("toe1R", CubeListBuilder.create()
                .texOffs(240, 181).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(0F, 2F, -7.2F, 0.35F, 0F, 0F));
        PartDefinition p_toe2R = p_footR.addOrReplaceChild("toe2R", CubeListBuilder.create()
                .texOffs(256, 181).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(3.2F, 2F, -7.2F, 0.35F, 0.16F, 0F));
        PartDefinition p_heelR = p_footR.addOrReplaceChild("heelR", CubeListBuilder.create()
                .texOffs(444, 181).addBox(-1F, -1F, 0F, 2F, 2F, 5F),
                PartPose.offsetAndRotation(0F, 1.5F, 2.5F, 0.5F, 0F, 0F));
        PartDefinition p_thighL = p_pelvis.addOrReplaceChild("thighL", CubeListBuilder.create()
                .texOffs(112, 98).addBox(-5.5F, -1F, -6.5F, 11F, 15F, 13F),
                PartPose.offsetAndRotation(12F, 3F, 0F, -0.55F, 0F, 0F));
        PartDefinition p_shinL = p_thighL.addOrReplaceChild("shinL", CubeListBuilder.create()
                .texOffs(0, 126).addBox(-4.5F, -1F, -4.5F, 9F, 15F, 9F),
                PartPose.offsetAndRotation(0F, 13F, -0.5F, 1.05F, 0F, 0F));
        PartDefinition p_footL = p_shinL.addOrReplaceChild("footL", CubeListBuilder.create()
                .texOffs(142, 150).addBox(-5.5F, -1F, -9F, 11F, 5F, 12F),
                PartPose.offsetAndRotation(0F, 13.5F, 0F, -0.5F, 0F, 0F));
        PartDefinition p_toe0L = p_footL.addOrReplaceChild("toe0L", CubeListBuilder.create()
                .texOffs(272, 181).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(-3.2F, 2F, -7.2F, 0.35F, -0.16F, 0F));
        PartDefinition p_toe1L = p_footL.addOrReplaceChild("toe1L", CubeListBuilder.create()
                .texOffs(288, 181).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(0F, 2F, -7.2F, 0.35F, 0F, 0F));
        PartDefinition p_toe2L = p_footL.addOrReplaceChild("toe2L", CubeListBuilder.create()
                .texOffs(304, 181).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(3.2F, 2F, -7.2F, 0.35F, 0.16F, 0F));
        PartDefinition p_heelL = p_footL.addOrReplaceChild("heelL", CubeListBuilder.create()
                .texOffs(458, 181).addBox(-1F, -1F, 0F, 2F, 2F, 5F),
                PartPose.offsetAndRotation(0F, 1.5F, 2.5F, 0.5F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    /** Mapa nombre -> parte, para animar. */
    public static Map<String, ModelPart> collect(ModelPart modelRoot) {
        Map<String, ModelPart> m = new HashMap<>();
        ModelPart root = modelRoot.getChild("root");
        m.put("root", root);
        m.put("pelvis", root.getChild("pelvis"));
        m.put("chest", root.getChild("pelvis").getChild("chest"));
        m.put("neck1", root.getChild("pelvis").getChild("chest").getChild("neck1"));
        m.put("neck2", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2"));
        m.put("neck3", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3"));
        m.put("neck4", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4"));
        m.put("head", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head"));
        m.put("hornR", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("hornR"));
        m.put("horn2R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("hornR").getChild("horn2R"));
        m.put("hornL", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("hornL"));
        m.put("horn2L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("hornL").getChild("horn2L"));
        m.put("crest0", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("crest0"));
        m.put("crest1", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("crest1"));
        m.put("crest2", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("crest2"));
        m.put("cheekR", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("cheekR"));
        m.put("cheek2R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("cheek2R"));
        m.put("cheekL", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("cheekL"));
        m.put("cheek2L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("cheek2L"));
        m.put("jaw", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("jaw"));
        m.put("tail1", root.getChild("pelvis").getChild("tail1"));
        m.put("tail2", root.getChild("pelvis").getChild("tail1").getChild("tail2"));
        m.put("tail3", root.getChild("pelvis").getChild("tail1").getChild("tail2").getChild("tail3"));
        m.put("tail4", root.getChild("pelvis").getChild("tail1").getChild("tail2").getChild("tail3").getChild("tail4"));
        m.put("tail5", root.getChild("pelvis").getChild("tail1").getChild("tail2").getChild("tail3").getChild("tail4").getChild("tail5"));
        m.put("tail6", root.getChild("pelvis").getChild("tail1").getChild("tail2").getChild("tail3").getChild("tail4").getChild("tail5").getChild("tail6"));
        m.put("tail7", root.getChild("pelvis").getChild("tail1").getChild("tail2").getChild("tail3").getChild("tail4").getChild("tail5").getChild("tail6").getChild("tail7"));
        m.put("tailtip", root.getChild("pelvis").getChild("tail1").getChild("tail2").getChild("tail3").getChild("tail4").getChild("tail5").getChild("tail6").getChild("tail7").getChild("tailtip"));
        m.put("thighR", root.getChild("pelvis").getChild("thighR"));
        m.put("shinR", root.getChild("pelvis").getChild("thighR").getChild("shinR"));
        m.put("footR", root.getChild("pelvis").getChild("thighR").getChild("shinR").getChild("footR"));
        m.put("toe0R", root.getChild("pelvis").getChild("thighR").getChild("shinR").getChild("footR").getChild("toe0R"));
        m.put("toe1R", root.getChild("pelvis").getChild("thighR").getChild("shinR").getChild("footR").getChild("toe1R"));
        m.put("toe2R", root.getChild("pelvis").getChild("thighR").getChild("shinR").getChild("footR").getChild("toe2R"));
        m.put("heelR", root.getChild("pelvis").getChild("thighR").getChild("shinR").getChild("footR").getChild("heelR"));
        m.put("thighL", root.getChild("pelvis").getChild("thighL"));
        m.put("shinL", root.getChild("pelvis").getChild("thighL").getChild("shinL"));
        m.put("footL", root.getChild("pelvis").getChild("thighL").getChild("shinL").getChild("footL"));
        m.put("toe0L", root.getChild("pelvis").getChild("thighL").getChild("shinL").getChild("footL").getChild("toe0L"));
        m.put("toe1L", root.getChild("pelvis").getChild("thighL").getChild("shinL").getChild("footL").getChild("toe1L"));
        m.put("toe2L", root.getChild("pelvis").getChild("thighL").getChild("shinL").getChild("footL").getChild("toe2L"));
        m.put("heelL", root.getChild("pelvis").getChild("thighL").getChild("shinL").getChild("footL").getChild("heelL"));
        m.put("armR", root.getChild("pelvis").getChild("chest").getChild("armR"));
        m.put("forearmR", root.getChild("pelvis").getChild("chest").getChild("armR").getChild("forearmR"));
        m.put("handR", root.getChild("pelvis").getChild("chest").getChild("armR").getChild("forearmR").getChild("handR"));
        m.put("claw0R", root.getChild("pelvis").getChild("chest").getChild("armR").getChild("forearmR").getChild("handR").getChild("claw0R"));
        m.put("claw20R", root.getChild("pelvis").getChild("chest").getChild("armR").getChild("forearmR").getChild("handR").getChild("claw0R").getChild("claw20R"));
        m.put("claw1R", root.getChild("pelvis").getChild("chest").getChild("armR").getChild("forearmR").getChild("handR").getChild("claw1R"));
        m.put("claw21R", root.getChild("pelvis").getChild("chest").getChild("armR").getChild("forearmR").getChild("handR").getChild("claw1R").getChild("claw21R"));
        m.put("claw2R", root.getChild("pelvis").getChild("chest").getChild("armR").getChild("forearmR").getChild("handR").getChild("claw2R"));
        m.put("claw22R", root.getChild("pelvis").getChild("chest").getChild("armR").getChild("forearmR").getChild("handR").getChild("claw2R").getChild("claw22R"));
        m.put("thumbR", root.getChild("pelvis").getChild("chest").getChild("armR").getChild("forearmR").getChild("handR").getChild("thumbR"));
        m.put("armL", root.getChild("pelvis").getChild("chest").getChild("armL"));
        m.put("forearmL", root.getChild("pelvis").getChild("chest").getChild("armL").getChild("forearmL"));
        m.put("handL", root.getChild("pelvis").getChild("chest").getChild("armL").getChild("forearmL").getChild("handL"));
        m.put("claw0L", root.getChild("pelvis").getChild("chest").getChild("armL").getChild("forearmL").getChild("handL").getChild("claw0L"));
        m.put("claw20L", root.getChild("pelvis").getChild("chest").getChild("armL").getChild("forearmL").getChild("handL").getChild("claw0L").getChild("claw20L"));
        m.put("claw1L", root.getChild("pelvis").getChild("chest").getChild("armL").getChild("forearmL").getChild("handL").getChild("claw1L"));
        m.put("claw21L", root.getChild("pelvis").getChild("chest").getChild("armL").getChild("forearmL").getChild("handL").getChild("claw1L").getChild("claw21L"));
        m.put("claw2L", root.getChild("pelvis").getChild("chest").getChild("armL").getChild("forearmL").getChild("handL").getChild("claw2L"));
        m.put("claw22L", root.getChild("pelvis").getChild("chest").getChild("armL").getChild("forearmL").getChild("handL").getChild("claw2L").getChild("claw22L"));
        m.put("thumbL", root.getChild("pelvis").getChild("chest").getChild("armL").getChild("forearmL").getChild("handL").getChild("thumbL"));
        m.put("wingR", root.getChild("pelvis").getChild("chest").getChild("wingR"));
        m.put("wingOutR", root.getChild("pelvis").getChild("chest").getChild("wingR").getChild("wingOutR"));
        m.put("wingL", root.getChild("pelvis").getChild("chest").getChild("wingL"));
        m.put("wingOutL", root.getChild("pelvis").getChild("chest").getChild("wingL").getChild("wingOutL"));
        return m;
    }
}
