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
                .texOffs(378, 49).addBox(-8.5F, -12F, -7.5F, 17F, 13F, 15F)
                .texOffs(124, 180).addBox(-1.5F, -14.5F, -7F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -21F, 5F, 0.5F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(96, 98).addBox(-8F, -12F, -7F, 16F, 13F, 14F)
                .texOffs(140, 180).addBox(-1.5F, -14.5F, -6.5F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -11.5F, 0F, 0.12F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(156, 98).addBox(-7.5F, -12F, -7F, 15F, 13F, 14F)
                .texOffs(156, 180).addBox(-1.5F, -14.5F, -6.5F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -11.5F, 0F, -0.32F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(274, 98).addBox(-7F, -11F, -6.5F, 14F, 12F, 13F)
                .texOffs(172, 180).addBox(-1.5F, -13.5F, -6F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -11.5F, 0F, -0.46F, 0F, 0F));
        PartDefinition p_head = p_neck4.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(442, 49).addBox(-8F, -14F, -11F, 16F, 14F, 14F)
                .texOffs(194, 126).addBox(-6.5F, -12F, -20.5F, 13F, 10F, 10F)
                .texOffs(0, 150).addBox(-5.5F, -10.5F, -29F, 11F, 8F, 9F)
                .texOffs(0, 167).addBox(-4.5F, -8.5F, -36F, 9F, 6F, 7F)
                .texOffs(268, 190).addBox(-3.4F, -9.5F, -34.5F, 2F, 1F, 2F)
                .texOffs(276, 190).addBox(1.4F, -9.5F, -34.5F, 2F, 1F, 2F)
                .texOffs(174, 150).addBox(-9.6F, -8F, -7.5F, 2F, 7F, 9F)
                .texOffs(196, 150).addBox(7.6F, -8F, -7.5F, 2F, 7F, 9F)
                .texOffs(116, 190).addBox(-6.1F, -2.5F, -12F, 1F, 3F, 1F)
                .texOffs(120, 190).addBox(5.1F, -2.5F, -12F, 1F, 3F, 1F)
                .texOffs(124, 190).addBox(-6.1F, -2.5F, -14.5F, 1F, 3F, 1F)
                .texOffs(128, 190).addBox(5.1F, -2.5F, -14.5F, 1F, 3F, 1F)
                .texOffs(132, 190).addBox(-6.1F, -2.5F, -17F, 1F, 3F, 1F)
                .texOffs(136, 190).addBox(5.1F, -2.5F, -17F, 1F, 3F, 1F)
                .texOffs(140, 190).addBox(-6.1F, -2.5F, -19.5F, 1F, 3F, 1F)
                .texOffs(144, 190).addBox(5.1F, -2.5F, -19.5F, 1F, 3F, 1F)
                .texOffs(148, 190).addBox(-5.1F, -2.5F, -22F, 1F, 3F, 1F)
                .texOffs(152, 190).addBox(4.1F, -2.5F, -22F, 1F, 3F, 1F)
                .texOffs(156, 190).addBox(-5.1F, -2.5F, -24.5F, 1F, 3F, 1F)
                .texOffs(160, 190).addBox(4.1F, -2.5F, -24.5F, 1F, 3F, 1F)
                .texOffs(164, 190).addBox(-5.1F, -2.5F, -27F, 1F, 3F, 1F)
                .texOffs(168, 190).addBox(4.1F, -2.5F, -27F, 1F, 3F, 1F)
                .texOffs(172, 190).addBox(-4.1F, -2.5F, -30F, 1F, 3F, 1F)
                .texOffs(176, 190).addBox(3.1F, -2.5F, -30F, 1F, 3F, 1F)
                .texOffs(180, 190).addBox(-4.1F, -2.5F, -32.5F, 1F, 3F, 1F)
                .texOffs(184, 190).addBox(3.1F, -2.5F, -32.5F, 1F, 3F, 1F)
                .texOffs(244, 180).addBox(-4F, -2.7F, -35.5F, 2F, 7F, 2F)
                .texOffs(252, 180).addBox(2F, -2.7F, -35.5F, 2F, 7F, 2F)
                .texOffs(208, 167).addBox(-5F, -2.5F, -11F, 10F, 4F, 8F),
                PartPose.offsetAndRotation(0F, -11F, 0F, 0.42F, 0F, 0F));
        PartDefinition p_ridge = p_head.addOrReplaceChild("ridge", CubeListBuilder.create()
                .texOffs(316, 49).addBox(-2.5F, -2F, -25F, 5F, 3F, 26F),
                PartPose.offsetAndRotation(0F, -12F, -8F, 0.16F, 0F, 0F));
        PartDefinition p_browR = p_head.addOrReplaceChild("browR", CubeListBuilder.create()
                .texOffs(342, 150).addBox(-2.5F, -1.5F, -2F, 5F, 3F, 11F),
                PartPose.offsetAndRotation(-6.2F, -12.8F, -11.5F, 0.3F, 0.18F, -0.25F));
        PartDefinition p_browL = p_head.addOrReplaceChild("browL", CubeListBuilder.create()
                .texOffs(374, 150).addBox(-2.5F, -1.5F, -2F, 5F, 3F, 11F),
                PartPose.offsetAndRotation(6.2F, -12.8F, -11.5F, 0.3F, -0.18F, 0.25F));
        PartDefinition p_spike0R = p_head.addOrReplaceChild("spike0R", CubeListBuilder.create()
                .texOffs(280, 126).addBox(-2F, -2F, 0F, 4F, 4F, 15F),
                PartPose.offsetAndRotation(-5F, -13F, -1F, 0.75F, -0.22F, 0F));
        PartDefinition p_spikeTip0R = p_spike0R.addOrReplaceChild("spikeTip0R", CubeListBuilder.create()
                .texOffs(60, 167).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 14.5F, 0.22F, -0.08F, 0F));
        PartDefinition p_spike1R = p_head.addOrReplaceChild("spike1R", CubeListBuilder.create()
                .texOffs(40, 150).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 14F),
                PartPose.offsetAndRotation(-7F, -9.5F, 0F, 0.25F, -0.55F, 0F));
        PartDefinition p_spikeTip1R = p_spike1R.addOrReplaceChild("spikeTip1R", CubeListBuilder.create()
                .texOffs(394, 167).addBox(-1F, -1F, 0F, 2F, 2F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 13.5F, 0.22F, -0.08F, 0F));
        PartDefinition p_spike2R = p_head.addOrReplaceChild("spike2R", CubeListBuilder.create()
                .texOffs(406, 150).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 11F),
                PartPose.offsetAndRotation(-8F, -5F, -1.5F, -0.15F, -0.75F, 0F));
        PartDefinition p_spikeTip2R = p_spike2R.addOrReplaceChild("spikeTip2R", CubeListBuilder.create()
                .texOffs(88, 180).addBox(-1F, -1F, 0F, 2F, 2F, 7F),
                PartPose.offsetAndRotation(0F, 0F, 10.5F, 0.22F, -0.08F, 0F));
        PartDefinition p_spike0L = p_head.addOrReplaceChild("spike0L", CubeListBuilder.create()
                .texOffs(318, 126).addBox(-2F, -2F, 0F, 4F, 4F, 15F),
                PartPose.offsetAndRotation(5F, -13F, -1F, 0.75F, 0.22F, 0F));
        PartDefinition p_spikeTip0L = p_spike0L.addOrReplaceChild("spikeTip0L", CubeListBuilder.create()
                .texOffs(86, 167).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 14.5F, 0.22F, 0.08F, 0F));
        PartDefinition p_spike1L = p_head.addOrReplaceChild("spike1L", CubeListBuilder.create()
                .texOffs(74, 150).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 14F),
                PartPose.offsetAndRotation(7F, -9.5F, 0F, 0.25F, 0.55F, 0F));
        PartDefinition p_spikeTip1L = p_spike1L.addOrReplaceChild("spikeTip1L", CubeListBuilder.create()
                .texOffs(416, 167).addBox(-1F, -1F, 0F, 2F, 2F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 13.5F, 0.22F, 0.08F, 0F));
        PartDefinition p_spike2L = p_head.addOrReplaceChild("spike2L", CubeListBuilder.create()
                .texOffs(434, 150).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 11F),
                PartPose.offsetAndRotation(8F, -5F, -1.5F, -0.15F, 0.75F, 0F));
        PartDefinition p_spikeTip2L = p_spike2L.addOrReplaceChild("spikeTip2L", CubeListBuilder.create()
                .texOffs(106, 180).addBox(-1F, -1F, 0F, 2F, 2F, 7F),
                PartPose.offsetAndRotation(0F, 0F, 10.5F, 0.22F, 0.08F, 0F));
        PartDefinition p_crest0 = p_head.addOrReplaceChild("crest0", CubeListBuilder.create()
                .texOffs(274, 167).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 9F),
                PartPose.offsetAndRotation(0F, -13.5F, -5F, 0.75F, 0F, 0F));
        PartDefinition p_crest1 = p_head.addOrReplaceChild("crest1", CubeListBuilder.create()
                .texOffs(142, 150).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 13F),
                PartPose.offsetAndRotation(0F, -13.5F, 0F, 0.95F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(356, 126).addBox(-6F, -1F, -14F, 12F, 4F, 14F)
                .texOffs(218, 150).addBox(-5F, -1F, -25.5F, 10F, 3F, 12F)
                .texOffs(462, 150).addBox(-4F, -1F, -35F, 8F, 3F, 10F)
                .texOffs(188, 190).addBox(-5.5F, -3.7F, -9.5F, 1F, 3F, 1F)
                .texOffs(192, 190).addBox(4.5F, -3.7F, -9.5F, 1F, 3F, 1F)
                .texOffs(196, 190).addBox(-5.5F, -3.7F, -12F, 1F, 3F, 1F)
                .texOffs(200, 190).addBox(4.5F, -3.7F, -12F, 1F, 3F, 1F)
                .texOffs(204, 190).addBox(-4.5F, -3.7F, -15F, 1F, 3F, 1F)
                .texOffs(208, 190).addBox(3.5F, -3.7F, -15F, 1F, 3F, 1F)
                .texOffs(212, 190).addBox(-4.5F, -3.7F, -17.5F, 1F, 3F, 1F)
                .texOffs(216, 190).addBox(3.5F, -3.7F, -17.5F, 1F, 3F, 1F)
                .texOffs(220, 190).addBox(-4.5F, -3.7F, -20F, 1F, 3F, 1F)
                .texOffs(224, 190).addBox(3.5F, -3.7F, -20F, 1F, 3F, 1F)
                .texOffs(228, 190).addBox(-4.5F, -3.7F, -22.5F, 1F, 3F, 1F)
                .texOffs(232, 190).addBox(3.5F, -3.7F, -22.5F, 1F, 3F, 1F)
                .texOffs(236, 190).addBox(-4.5F, -3.7F, -25F, 1F, 3F, 1F)
                .texOffs(240, 190).addBox(3.5F, -3.7F, -25F, 1F, 3F, 1F)
                .texOffs(244, 190).addBox(-3.5F, -3.7F, -27.5F, 1F, 3F, 1F)
                .texOffs(248, 190).addBox(2.5F, -3.7F, -27.5F, 1F, 3F, 1F)
                .texOffs(252, 190).addBox(-3.5F, -3.7F, -30F, 1F, 3F, 1F)
                .texOffs(256, 190).addBox(2.5F, -3.7F, -30F, 1F, 3F, 1F)
                .texOffs(260, 190).addBox(-3.5F, -3.7F, -32.5F, 1F, 3F, 1F)
                .texOffs(264, 190).addBox(2.5F, -3.7F, -32.5F, 1F, 3F, 1F)
                .texOffs(100, 190).addBox(-3.6F, -5.1F, -34.8F, 2F, 5F, 2F)
                .texOffs(108, 190).addBox(1.6F, -5.1F, -34.8F, 2F, 5F, 2F)
                .texOffs(244, 167).addBox(-2.5F, 2.5F, -17F, 5F, 2F, 10F),
                PartPose.offsetAndRotation(0F, -2F, -0.5F, 0.42F, 0F, 0F));
        PartDefinition p_jawSpikeR = p_jaw.addOrReplaceChild("jawSpikeR", CubeListBuilder.create()
                .texOffs(48, 180).addBox(-1F, -1F, 0F, 2F, 2F, 8F),
                PartPose.offsetAndRotation(-5.5F, 1.5F, -2F, -0.2F, -0.6F, 0F));
        PartDefinition p_jawSpikeL = p_jaw.addOrReplaceChild("jawSpikeL", CubeListBuilder.create()
                .texOffs(68, 180).addBox(-1F, -1F, 0F, 2F, 2F, 8F),
                PartPose.offsetAndRotation(5.5F, 1.5F, -2F, -0.2F, 0.6F, 0F));
        PartDefinition p_armR = p_chest.addOrReplaceChild("armR", CubeListBuilder.create()
                .texOffs(454, 98).addBox(-4.5F, -2F, -4.5F, 9F, 15F, 9F),
                PartPose.offsetAndRotation(-17F, -17F, 4F, -0.7F, -0.28F, -0.15F));
        PartDefinition p_forearmR = p_armR.addOrReplaceChild("forearmR", CubeListBuilder.create()
                .texOffs(86, 126).addBox(-4F, -2F, -4F, 8F, 14F, 8F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -1F, 0F, 0F));
        PartDefinition p_handR = p_forearmR.addOrReplaceChild("handR", CubeListBuilder.create()
                .texOffs(262, 150).addBox(-5F, -1.5F, -5F, 10F, 5F, 10F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -0.15F, 0F, 0F));
        PartDefinition p_claw0R = p_handR.addOrReplaceChild("claw0R", CubeListBuilder.create()
                .texOffs(488, 180).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(-3.4F, 4F, 0F, 0.25F, -0.17F, 0F));
        PartDefinition p_claw20R = p_claw0R.addOrReplaceChild("claw20R", CubeListBuilder.create()
                .texOffs(322, 167).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_claw1R = p_handR.addOrReplaceChild("claw1R", CubeListBuilder.create()
                .texOffs(500, 180).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0.25F, 0F, 0F));
        PartDefinition p_claw21R = p_claw1R.addOrReplaceChild("claw21R", CubeListBuilder.create()
                .texOffs(334, 167).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_claw2R = p_handR.addOrReplaceChild("claw2R", CubeListBuilder.create()
                .texOffs(0, 190).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(3.4F, 4F, 0F, 0.25F, 0.17F, 0F));
        PartDefinition p_claw22R = p_claw2R.addOrReplaceChild("claw22R", CubeListBuilder.create()
                .texOffs(346, 167).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_thumbR = p_handR.addOrReplaceChild("thumbR", CubeListBuilder.create()
                .texOffs(438, 167).addBox(-1.5F, -0.5F, -1.5F, 3F, 8F, 3F),
                PartPose.offsetAndRotation(-5.5F, 1F, -2F, 0.4F, 0F, -0.5F));
        PartDefinition p_armL = p_chest.addOrReplaceChild("armL", CubeListBuilder.create()
                .texOffs(0, 126).addBox(-4.5F, -2F, -4.5F, 9F, 15F, 9F),
                PartPose.offsetAndRotation(17F, -17F, 4F, -0.7F, 0.28F, 0.15F));
        PartDefinition p_forearmL = p_armL.addOrReplaceChild("forearmL", CubeListBuilder.create()
                .texOffs(118, 126).addBox(-4F, -2F, -4F, 8F, 14F, 8F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -1F, 0F, 0F));
        PartDefinition p_handL = p_forearmL.addOrReplaceChild("handL", CubeListBuilder.create()
                .texOffs(302, 150).addBox(-5F, -1.5F, -5F, 10F, 5F, 10F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -0.15F, 0F, 0F));
        PartDefinition p_claw0L = p_handL.addOrReplaceChild("claw0L", CubeListBuilder.create()
                .texOffs(12, 190).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(-3.4F, 4F, 0F, 0.25F, -0.17F, 0F));
        PartDefinition p_claw20L = p_claw0L.addOrReplaceChild("claw20L", CubeListBuilder.create()
                .texOffs(358, 167).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_claw1L = p_handL.addOrReplaceChild("claw1L", CubeListBuilder.create()
                .texOffs(24, 190).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0.25F, 0F, 0F));
        PartDefinition p_claw21L = p_claw1L.addOrReplaceChild("claw21L", CubeListBuilder.create()
                .texOffs(370, 167).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_claw2L = p_handL.addOrReplaceChild("claw2L", CubeListBuilder.create()
                .texOffs(36, 190).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(3.4F, 4F, 0F, 0.25F, 0.17F, 0F));
        PartDefinition p_claw22L = p_claw2L.addOrReplaceChild("claw22L", CubeListBuilder.create()
                .texOffs(382, 167).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_thumbL = p_handL.addOrReplaceChild("thumbL", CubeListBuilder.create()
                .texOffs(450, 167).addBox(-1.5F, -0.5F, -1.5F, 3F, 8F, 3F),
                PartPose.offsetAndRotation(5.5F, 1F, -2F, 0.4F, 0F, 0.5F));
        PartDefinition p_wingR = p_chest.addOrReplaceChild("wingR", CubeListBuilder.create()
                .texOffs(112, 167).addBox(-18F, -3F, -3F, 18F, 6F, 6F)
                .texOffs(462, 167).addBox(-36.5F, -2.5F, -2.5F, 19F, 5F, 5F)
                .texOffs(0, 0).addBox(-36F, -0.5F, 0F, 36F, 1F, 48F),
                PartPose.offsetAndRotation(-11F, -19F, -8F, -0.85F, 0.55F, 0.8F));
        PartDefinition p_wingOutR = p_wingR.addOrReplaceChild("wingOutR", CubeListBuilder.create()
                .texOffs(336, 0).addBox(-22F, -0.5F, 0F, 22F, 1F, 48F)
                .texOffs(260, 180).addBox(-22F, -2F, -1F, 22F, 4F, 4F)
                .texOffs(48, 190).addBox(-2.5F, -6F, -1F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(-36F, 0F, 0F, 0F, 0.1F, 0.18F));
        PartDefinition p_wingL = p_chest.addOrReplaceChild("wingL", CubeListBuilder.create()
                .texOffs(160, 167).addBox(0F, -3F, -3F, 18F, 6F, 6F)
                .texOffs(0, 180).addBox(17.5F, -2.5F, -2.5F, 19F, 5F, 5F)
                .texOffs(168, 0).addBox(0F, -0.5F, 0F, 36F, 1F, 48F),
                PartPose.offsetAndRotation(11F, -19F, -8F, -0.85F, -0.55F, -0.8F));
        PartDefinition p_wingOutL = p_wingL.addOrReplaceChild("wingOutL", CubeListBuilder.create()
                .texOffs(0, 49).addBox(0F, -0.5F, 0F, 22F, 1F, 48F)
                .texOffs(312, 180).addBox(0F, -2F, -1F, 22F, 4F, 4F)
                .texOffs(60, 190).addBox(-0.5F, -6F, -1F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(36F, 0F, 0F, 0F, -0.1F, -0.18F));
        PartDefinition p_tail1 = p_pelvis.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(214, 98).addBox(-8F, -6F, -0.5F, 16F, 12F, 14F)
                .texOffs(188, 180).addBox(-1F, -9F, 4.5F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -0.5F, 10F, -0.1F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(328, 98).addBox(-7F, -5.5F, -0.5F, 14F, 11F, 13F)
                .texOffs(202, 180).addBox(-1F, -8.5F, 4F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 13F, 0.02F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(36, 126).addBox(-6F, -5F, -0.5F, 12F, 10F, 13F)
                .texOffs(216, 180).addBox(-1F, -8F, 4F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 12F, 0.16F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(150, 126).addBox(-5F, -4.5F, -0.5F, 10F, 9F, 12F)
                .texOffs(230, 180).addBox(-1F, -7.5F, 3.5F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 12F, 0.28F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(240, 126).addBox(-4F, -3.5F, -0.5F, 8F, 7F, 12F)
                .texOffs(460, 180).addBox(-1F, -6F, 3.5F, 2F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.34F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(108, 150).addBox(-3F, -2.5F, -0.5F, 6F, 5F, 11F)
                .texOffs(474, 180).addBox(-1F, -5F, 3F, 2F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.4F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(32, 167).addBox(-2F, -1.5F, -0.5F, 4F, 3F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.34F, 0F, 0F));
        PartDefinition p_tailtip = p_tail7.addOrReplaceChild("tailtip", CubeListBuilder.create()
                .texOffs(298, 167).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.25F, 0F, 0F));
        PartDefinition p_thighR = p_pelvis.addOrReplaceChild("thighR", CubeListBuilder.create()
                .texOffs(0, 98).addBox(-5.5F, -1F, -6.5F, 11F, 15F, 13F),
                PartPose.offsetAndRotation(-12F, 3F, 0F, -0.55F, 0F, 0F));
        PartDefinition p_shinR = p_thighR.addOrReplaceChild("shinR", CubeListBuilder.create()
                .texOffs(382, 98).addBox(-4.5F, -1F, -4.5F, 9F, 15F, 9F),
                PartPose.offsetAndRotation(0F, 13F, -0.5F, 1.05F, 0F, 0F));
        PartDefinition p_footR = p_shinR.addOrReplaceChild("footR", CubeListBuilder.create()
                .texOffs(408, 126).addBox(-5.5F, -1F, -9F, 11F, 5F, 12F),
                PartPose.offsetAndRotation(0F, 13.5F, 0F, -0.5F, 0F, 0F));
        PartDefinition p_toe0R = p_footR.addOrReplaceChild("toe0R", CubeListBuilder.create()
                .texOffs(364, 180).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(-3.2F, 2F, -7.2F, 0.35F, -0.16F, 0F));
        PartDefinition p_toe1R = p_footR.addOrReplaceChild("toe1R", CubeListBuilder.create()
                .texOffs(380, 180).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(0F, 2F, -7.2F, 0.35F, 0F, 0F));
        PartDefinition p_toe2R = p_footR.addOrReplaceChild("toe2R", CubeListBuilder.create()
                .texOffs(396, 180).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(3.2F, 2F, -7.2F, 0.35F, 0.16F, 0F));
        PartDefinition p_heelR = p_footR.addOrReplaceChild("heelR", CubeListBuilder.create()
                .texOffs(72, 190).addBox(-1F, -1F, 0F, 2F, 2F, 5F),
                PartPose.offsetAndRotation(0F, 1.5F, 2.5F, 0.5F, 0F, 0F));
        PartDefinition p_thighL = p_pelvis.addOrReplaceChild("thighL", CubeListBuilder.create()
                .texOffs(48, 98).addBox(-5.5F, -1F, -6.5F, 11F, 15F, 13F),
                PartPose.offsetAndRotation(12F, 3F, 0F, -0.55F, 0F, 0F));
        PartDefinition p_shinL = p_thighL.addOrReplaceChild("shinL", CubeListBuilder.create()
                .texOffs(418, 98).addBox(-4.5F, -1F, -4.5F, 9F, 15F, 9F),
                PartPose.offsetAndRotation(0F, 13F, -0.5F, 1.05F, 0F, 0F));
        PartDefinition p_footL = p_shinL.addOrReplaceChild("footL", CubeListBuilder.create()
                .texOffs(454, 126).addBox(-5.5F, -1F, -9F, 11F, 5F, 12F),
                PartPose.offsetAndRotation(0F, 13.5F, 0F, -0.5F, 0F, 0F));
        PartDefinition p_toe0L = p_footL.addOrReplaceChild("toe0L", CubeListBuilder.create()
                .texOffs(412, 180).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(-3.2F, 2F, -7.2F, 0.35F, -0.16F, 0F));
        PartDefinition p_toe1L = p_footL.addOrReplaceChild("toe1L", CubeListBuilder.create()
                .texOffs(428, 180).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(0F, 2F, -7.2F, 0.35F, 0F, 0F));
        PartDefinition p_toe2L = p_footL.addOrReplaceChild("toe2L", CubeListBuilder.create()
                .texOffs(444, 180).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(3.2F, 2F, -7.2F, 0.35F, 0.16F, 0F));
        PartDefinition p_heelL = p_footL.addOrReplaceChild("heelL", CubeListBuilder.create()
                .texOffs(86, 190).addBox(-1F, -1F, 0F, 2F, 2F, 5F),
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
        m.put("ridge", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("ridge"));
        m.put("browR", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("browR"));
        m.put("browL", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("browL"));
        m.put("spike0R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("spike0R"));
        m.put("spikeTip0R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("spike0R").getChild("spikeTip0R"));
        m.put("spike1R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("spike1R"));
        m.put("spikeTip1R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("spike1R").getChild("spikeTip1R"));
        m.put("spike2R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("spike2R"));
        m.put("spikeTip2R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("spike2R").getChild("spikeTip2R"));
        m.put("spike0L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("spike0L"));
        m.put("spikeTip0L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("spike0L").getChild("spikeTip0L"));
        m.put("spike1L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("spike1L"));
        m.put("spikeTip1L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("spike1L").getChild("spikeTip1L"));
        m.put("spike2L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("spike2L"));
        m.put("spikeTip2L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("spike2L").getChild("spikeTip2L"));
        m.put("crest0", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("crest0"));
        m.put("crest1", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("crest1"));
        m.put("jaw", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("jaw"));
        m.put("jawSpikeR", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("jaw").getChild("jawSpikeR"));
        m.put("jawSpikeL", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("head").getChild("jaw").getChild("jawSpikeL"));
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
