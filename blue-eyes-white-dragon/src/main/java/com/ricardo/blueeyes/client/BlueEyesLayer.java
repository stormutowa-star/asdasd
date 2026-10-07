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
                .texOffs(286, 98).addBox(-8.5F, -8F, -7.5F, 17F, 9F, 15F)
                .texOffs(412, 49).addBox(-6.5F, -8F, -8.5F, 13F, 9F, 17F)
                .texOffs(318, 212).addBox(-1.5F, -11F, -8F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -21F, 5F, 0.3F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(350, 98).addBox(-8.5F, -8F, -7.5F, 17F, 9F, 15F)
                .texOffs(0, 98).addBox(-6.5F, -8F, -8.5F, 13F, 9F, 17F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, 0.18F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(246, 124).addBox(-8F, -8F, -7F, 16F, 9F, 14F)
                .texOffs(120, 98).addBox(-6F, -8F, -8F, 12F, 9F, 16F)
                .texOffs(334, 212).addBox(-1.5F, -11F, -7.5F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, 0.1F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(306, 124).addBox(-8F, -8F, -7F, 16F, 9F, 14F)
                .texOffs(176, 98).addBox(-6F, -8F, -8F, 12F, 9F, 16F),
                PartPose.offset(0F, -6.5F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(366, 124).addBox(-7.5F, -8F, -7F, 15F, 9F, 14F)
                .texOffs(232, 98).addBox(-5.5F, -8F, -8F, 11F, 9F, 16F)
                .texOffs(350, 212).addBox(-1.5F, -11F, -7.5F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, -0.18F, 0F, 0F));
        PartDefinition p_neck6 = p_neck5.addOrReplaceChild("neck6", CubeListBuilder.create()
                .texOffs(0, 148).addBox(-7.5F, -8F, -6.5F, 15F, 9F, 13F)
                .texOffs(0, 124).addBox(-5.5F, -8F, -7.5F, 11F, 9F, 15F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, -0.22F, 0F, 0F));
        PartDefinition p_neck7 = p_neck6.addOrReplaceChild("neck7", CubeListBuilder.create()
                .texOffs(56, 148).addBox(-7F, -8F, -6.5F, 14F, 9F, 13F)
                .texOffs(52, 124).addBox(-5F, -8F, -7.5F, 10F, 9F, 15F)
                .texOffs(366, 212).addBox(-1.5F, -11F, -7F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, -0.3F, 0F, 0F));
        PartDefinition p_head = p_neck7.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(286, 230).addBox(-3F, -5.7F, -39.3F, 6F, 3F, 3F)
                .texOffs(468, 230).addBox(-2.1F, -7.1F, -36F, 1F, 1F, 2F)
                .texOffs(474, 230).addBox(1.1F, -7.1F, -36F, 1F, 1F, 2F)
                .texOffs(438, 212).addBox(-3.2F, -3.5F, -36F, 2F, 7F, 2F)
                .texOffs(446, 212).addBox(1.2F, -3.5F, -36F, 2F, 7F, 2F)
                .texOffs(452, 186).addBox(-5F, -2.5F, -10F, 10F, 4F, 8F),
                PartPose.offsetAndRotation(0F, -11F, 0F, 0.4F, 0F, 0F));
        PartDefinition p_skull0 = p_head.addOrReplaceChild("skull0", CubeListBuilder.create()
                .texOffs(226, 170).addBox(-8F, -5.5F, -2F, 16F, 11F, 4F)
                .texOffs(258, 148).addBox(-6F, -7F, -2F, 12F, 14F, 4F),
                PartPose.offsetAndRotation(0F, -8.7317F, 2.5357F, 0.0182F, 0F, 0F));
        PartDefinition p_skull1 = p_head.addOrReplaceChild("skull1", CubeListBuilder.create()
                .texOffs(266, 170).addBox(-8F, -5.5F, -2F, 16F, 11F, 4F)
                .texOffs(290, 148).addBox(-6F, -7F, -2F, 12F, 14F, 4F),
                PartPose.offsetAndRotation(0F, -8.6528F, -0.3929F, 0.0322F, 0F, 0F));
        PartDefinition p_skull2 = p_head.addOrReplaceChild("skull2", CubeListBuilder.create()
                .texOffs(386, 170).addBox(-7.5F, -5.5F, -2F, 15F, 11F, 4F)
                .texOffs(322, 148).addBox(-5.5F, -7F, -2F, 11F, 14F, 4F),
                PartPose.offsetAndRotation(0F, -8.5464F, -3.3214F, 0.0397F, 0F, 0F));
        PartDefinition p_skull3 = p_head.addOrReplaceChild("skull3", CubeListBuilder.create()
                .texOffs(0, 186).addBox(-7.5F, -5F, -2F, 15F, 10F, 4F)
                .texOffs(444, 148).addBox(-5.5F, -6.5F, -2F, 11F, 13F, 4F),
                PartPose.offsetAndRotation(0F, -8.4214F, -6.25F, 0.0453F, 0F, 0F));
        PartDefinition p_skull4 = p_head.addOrReplaceChild("skull4", CubeListBuilder.create()
                .texOffs(38, 186).addBox(-7F, -5F, -2F, 14F, 10F, 4F)
                .texOffs(474, 148).addBox(-5F, -6.5F, -2F, 10F, 13F, 4F),
                PartPose.offsetAndRotation(0F, -8.2819F, -9.1786F, 0.0498F, 0F, 0F));
        PartDefinition p_skull5 = p_head.addOrReplaceChild("skull5", CubeListBuilder.create()
                .texOffs(204, 186).addBox(-7F, -4.5F, -2F, 14F, 9F, 4F)
                .texOffs(102, 170).addBox(-5F, -6F, -2F, 10F, 12F, 4F),
                PartPose.offsetAndRotation(0F, -8.1302F, -12.1071F, 0.0536F, 0F, 0F));
        PartDefinition p_skull6 = p_head.addOrReplaceChild("skull6", CubeListBuilder.create()
                .texOffs(240, 186).addBox(-6.5F, -4.5F, -2F, 13F, 9F, 4F)
                .texOffs(130, 170).addBox(-4.5F, -6F, -2F, 9F, 12F, 4F),
                PartPose.offsetAndRotation(0F, -7.9679F, -15.0357F, 0.057F, 0F, 0F));
        PartDefinition p_skull7 = p_head.addOrReplaceChild("skull7", CubeListBuilder.create()
                .texOffs(274, 186).addBox(-6.5F, -4.5F, -2F, 13F, 9F, 4F)
                .texOffs(156, 170).addBox(-4.5F, -6F, -2F, 9F, 12F, 4F),
                PartPose.offsetAndRotation(0F, -7.7962F, -17.9643F, 0.06F, 0F, 0F));
        PartDefinition p_skull8 = p_head.addOrReplaceChild("skull8", CubeListBuilder.create()
                .texOffs(0, 200).addBox(-6F, -4F, -2F, 12F, 8F, 4F)
                .texOffs(484, 170).addBox(-4F, -5.5F, -2F, 8F, 11F, 4F),
                PartPose.offsetAndRotation(0F, -7.6161F, -20.8929F, 0.0628F, 0F, 0F));
        PartDefinition p_skull9 = p_head.addOrReplaceChild("skull9", CubeListBuilder.create()
                .texOffs(196, 200).addBox(-6F, -3.5F, -2F, 12F, 7F, 4F)
                .texOffs(158, 186).addBox(-4F, -5F, -2F, 8F, 10F, 4F),
                PartPose.offsetAndRotation(0F, -7.4281F, -23.8214F, 0.0653F, 0F, 0F));
        PartDefinition p_skull10 = p_head.addOrReplaceChild("skull10", CubeListBuilder.create()
                .texOffs(228, 200).addBox(-5.5F, -3.5F, -2F, 11F, 7F, 4F)
                .texOffs(182, 186).addBox(-3.5F, -5F, -2F, 7F, 10F, 4F),
                PartPose.offsetAndRotation(0F, -7.233F, -26.75F, 0.0677F, 0F, 0F));
        PartDefinition p_skull11 = p_head.addOrReplaceChild("skull11", CubeListBuilder.create()
                .texOffs(48, 212).addBox(-5F, -3F, -2F, 10F, 6F, 4F)
                .texOffs(336, 186).addBox(-3F, -4.5F, -2F, 6F, 9F, 4F),
                PartPose.offsetAndRotation(0F, -6.9913F, -29.6786F, 0.1534F, 0F, 0F));
        PartDefinition p_skull12 = p_head.addOrReplaceChild("skull12", CubeListBuilder.create()
                .texOffs(228, 212).addBox(-5F, -2.5F, -2F, 10F, 5F, 4F)
                .texOffs(104, 200).addBox(-3F, -4F, -2F, 6F, 8F, 4F),
                PartPose.offsetAndRotation(0F, -6.265F, -32.6071F, 0.2821F, 0F, 0F));
        PartDefinition p_skull13 = p_head.addOrReplaceChild("skull13", CubeListBuilder.create()
                .texOffs(0, 230).addBox(-4.5F, -1.5F, -2F, 9F, 3F, 4F)
                .texOffs(210, 212).addBox(-2.5F, -3F, -2F, 5F, 6F, 4F),
                PartPose.offsetAndRotation(0F, -5.4645F, -35.5357F, 0.1959F, 0F, 0F));
        PartDefinition p_eyeR = p_head.addOrReplaceChild("eyeR", CubeListBuilder.create()
                .texOffs(374, 200).addBox(-0.9F, -2F, -3.5F, 1F, 4F, 7F),
                PartPose.offsetAndRotation(-6.6F, -9.6F, -8.5F, 0.1F, -0.2F, 0.1F));
        PartDefinition p_browR = p_head.addOrReplaceChild("browR", CubeListBuilder.create()
                .texOffs(0, 170).addBox(-2F, -1.5F, -4.5F, 4F, 3F, 13F),
                PartPose.offsetAndRotation(-5.6F, -12.4F, -9F, 0.38F, 0.22F, -0.3F));
        PartDefinition p_cheekPlateR = p_head.addOrReplaceChild("cheekPlateR", CubeListBuilder.create()
                .texOffs(182, 170).addBox(-1F, -3.5F, -4.5F, 2F, 7F, 9F),
                PartPose.offsetAndRotation(-7.4F, -6F, -2F, 0F, -0.25F, 0F));
        PartDefinition p_eyeL = p_head.addOrReplaceChild("eyeL", CubeListBuilder.create()
                .texOffs(390, 200).addBox(-0.1F, -2F, -3.5F, 1F, 4F, 7F),
                PartPose.offsetAndRotation(6.6F, -9.6F, -8.5F, 0.1F, 0.2F, -0.1F));
        PartDefinition p_browL = p_head.addOrReplaceChild("browL", CubeListBuilder.create()
                .texOffs(34, 170).addBox(-2F, -1.5F, -4.5F, 4F, 3F, 13F),
                PartPose.offsetAndRotation(5.6F, -12.4F, -9F, 0.38F, -0.22F, 0.3F));
        PartDefinition p_cheekPlateL = p_head.addOrReplaceChild("cheekPlateL", CubeListBuilder.create()
                .texOffs(204, 170).addBox(-1F, -3.5F, -4.5F, 2F, 7F, 9F),
                PartPose.offsetAndRotation(7.4F, -6F, -2F, 0F, 0.25F, 0F));
        PartDefinition p_toothU0R = p_head.addOrReplaceChild("toothU0R", CubeListBuilder.create()
                .texOffs(324, 230).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-5.736F, -2.3415F, -10F, -0.25F, 0F, 0F));
        PartDefinition p_toothU0L = p_head.addOrReplaceChild("toothU0L", CubeListBuilder.create()
                .texOffs(328, 230).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(5.736F, -2.3415F, -10F, -0.25F, 0F, 0F));
        PartDefinition p_toothU1R = p_head.addOrReplaceChild("toothU1R", CubeListBuilder.create()
                .texOffs(396, 230).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-5.4831F, -2.4146F, -13F, -0.25F, 0F, 0F));
        PartDefinition p_toothU1L = p_head.addOrReplaceChild("toothU1L", CubeListBuilder.create()
                .texOffs(400, 230).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(5.4831F, -2.4146F, -13F, -0.25F, 0F, 0F));
        PartDefinition p_toothU2R = p_head.addOrReplaceChild("toothU2R", CubeListBuilder.create()
                .texOffs(332, 230).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-5.221F, -2.4878F, -16F, -0.25F, 0F, 0F));
        PartDefinition p_toothU2L = p_head.addOrReplaceChild("toothU2L", CubeListBuilder.create()
                .texOffs(336, 230).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(5.221F, -2.4878F, -16F, -0.25F, 0F, 0F));
        PartDefinition p_toothU3R = p_head.addOrReplaceChild("toothU3R", CubeListBuilder.create()
                .texOffs(404, 230).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-4.951F, -2.561F, -19F, -0.25F, 0F, 0F));
        PartDefinition p_toothU3L = p_head.addOrReplaceChild("toothU3L", CubeListBuilder.create()
                .texOffs(408, 230).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(4.951F, -2.561F, -19F, -0.25F, 0F, 0F));
        PartDefinition p_toothU4R = p_head.addOrReplaceChild("toothU4R", CubeListBuilder.create()
                .texOffs(340, 230).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-4.6737F, -2.6341F, -22F, -0.25F, 0F, 0F));
        PartDefinition p_toothU4L = p_head.addOrReplaceChild("toothU4L", CubeListBuilder.create()
                .texOffs(344, 230).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(4.6737F, -2.6341F, -22F, -0.25F, 0F, 0F));
        PartDefinition p_toothU5R = p_head.addOrReplaceChild("toothU5R", CubeListBuilder.create()
                .texOffs(412, 230).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-4.39F, -2.7073F, -25F, -0.25F, 0F, 0F));
        PartDefinition p_toothU5L = p_head.addOrReplaceChild("toothU5L", CubeListBuilder.create()
                .texOffs(416, 230).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(4.39F, -2.7073F, -25F, -0.25F, 0F, 0F));
        PartDefinition p_toothU6R = p_head.addOrReplaceChild("toothU6R", CubeListBuilder.create()
                .texOffs(348, 230).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-4.1004F, -2.7805F, -28F, -0.25F, 0F, 0F));
        PartDefinition p_toothU6L = p_head.addOrReplaceChild("toothU6L", CubeListBuilder.create()
                .texOffs(352, 230).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(4.1004F, -2.7805F, -28F, -0.25F, 0F, 0F));
        PartDefinition p_toothU7R = p_head.addOrReplaceChild("toothU7R", CubeListBuilder.create()
                .texOffs(420, 230).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-3.8053F, -2.8537F, -31F, -0.25F, 0F, 0F));
        PartDefinition p_toothU7L = p_head.addOrReplaceChild("toothU7L", CubeListBuilder.create()
                .texOffs(424, 230).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(3.8053F, -2.8537F, -31F, -0.25F, 0F, 0F));
        PartDefinition p_toothU8R = p_head.addOrReplaceChild("toothU8R", CubeListBuilder.create()
                .texOffs(356, 230).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-3.505F, -2.9268F, -34F, -0.25F, 0F, 0F));
        PartDefinition p_toothU8L = p_head.addOrReplaceChild("toothU8L", CubeListBuilder.create()
                .texOffs(360, 230).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(3.505F, -2.9268F, -34F, -0.25F, 0F, 0F));
        PartDefinition p_spike0R = p_head.addOrReplaceChild("spike0R", CubeListBuilder.create()
                .texOffs(424, 170).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 12F),
                PartPose.offsetAndRotation(-4F, -15F, -1.5F, 0.8F, -0.18F, 0F));
        PartDefinition p_spikeB0R = p_spike0R.addOrReplaceChild("spikeB0R", CubeListBuilder.create()
                .texOffs(286, 200).addBox(-1F, -1F, 0F, 2F, 2F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 11.5F, 0.22F, -0.05F, 0F));
        PartDefinition p_spikeC0R = p_spikeB0R.addOrReplaceChild("spikeC0R", CubeListBuilder.create()
                .texOffs(26, 230).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 6F),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, 0.22F, -0.05F, 0F));
        PartDefinition p_spike1R = p_head.addOrReplaceChild("spike1R", CubeListBuilder.create()
                .texOffs(74, 186).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 11F),
                PartPose.offsetAndRotation(-6.5F, -12F, 0F, 0.35F, -0.5F, 0F));
        PartDefinition p_spikeB1R = p_spike1R.addOrReplaceChild("spikeB1R", CubeListBuilder.create()
                .texOffs(150, 212).addBox(-1F, -1F, 0F, 2F, 2F, 8F),
                PartPose.offsetAndRotation(0F, 0F, 10.5F, 0.18F, -0.05F, 0F));
        PartDefinition p_spikeC1R = p_spikeB1R.addOrReplaceChild("spikeC1R", CubeListBuilder.create()
                .texOffs(40, 230).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 6F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.18F, -0.05F, 0F));
        PartDefinition p_spike2R = p_head.addOrReplaceChild("spike2R", CubeListBuilder.create()
                .texOffs(32, 200).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 9F),
                PartPose.offsetAndRotation(-7.5F, -7.5F, -0.5F, 0F, -0.75F, 0F));
        PartDefinition p_spikeB2R = p_spike2R.addOrReplaceChild("spikeB2R", CubeListBuilder.create()
                .texOffs(106, 222).addBox(-1F, -1F, 0F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, 0.15F, -0.05F, 0F));
        PartDefinition p_spikeC2R = p_spikeB2R.addOrReplaceChild("spikeC2R", CubeListBuilder.create()
                .texOffs(304, 230).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, 0.15F, -0.05F, 0F));
        PartDefinition p_spike0L = p_head.addOrReplaceChild("spike0L", CubeListBuilder.create()
                .texOffs(454, 170).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 12F),
                PartPose.offsetAndRotation(4F, -15F, -1.5F, 0.8F, 0.18F, 0F));
        PartDefinition p_spikeB0L = p_spike0L.addOrReplaceChild("spikeB0L", CubeListBuilder.create()
                .texOffs(308, 200).addBox(-1F, -1F, 0F, 2F, 2F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 11.5F, 0.22F, 0.05F, 0F));
        PartDefinition p_spikeC0L = p_spikeB0L.addOrReplaceChild("spikeC0L", CubeListBuilder.create()
                .texOffs(54, 230).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 6F),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, 0.22F, 0.05F, 0F));
        PartDefinition p_spike1L = p_head.addOrReplaceChild("spike1L", CubeListBuilder.create()
                .texOffs(102, 186).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 11F),
                PartPose.offsetAndRotation(6.5F, -12F, 0F, 0.35F, 0.5F, 0F));
        PartDefinition p_spikeB1L = p_spike1L.addOrReplaceChild("spikeB1L", CubeListBuilder.create()
                .texOffs(170, 212).addBox(-1F, -1F, 0F, 2F, 2F, 8F),
                PartPose.offsetAndRotation(0F, 0F, 10.5F, 0.18F, 0.05F, 0F));
        PartDefinition p_spikeC1L = p_spikeB1L.addOrReplaceChild("spikeC1L", CubeListBuilder.create()
                .texOffs(68, 230).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 6F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.18F, 0.05F, 0F));
        PartDefinition p_spike2L = p_head.addOrReplaceChild("spike2L", CubeListBuilder.create()
                .texOffs(56, 200).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 9F),
                PartPose.offsetAndRotation(7.5F, -7.5F, -0.5F, 0F, 0.75F, 0F));
        PartDefinition p_spikeB2L = p_spike2L.addOrReplaceChild("spikeB2L", CubeListBuilder.create()
                .texOffs(122, 222).addBox(-1F, -1F, 0F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, 0.15F, 0.05F, 0F));
        PartDefinition p_spikeC2L = p_spikeB2L.addOrReplaceChild("spikeC2L", CubeListBuilder.create()
                .texOffs(314, 230).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, 0.15F, 0.05F, 0F));
        PartDefinition p_crest0 = p_head.addOrReplaceChild("crest0", CubeListBuilder.create()
                .texOffs(130, 186).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 11F),
                PartPose.offsetAndRotation(0F, -15.5F, -4F, 1.15F, 0F, 0F));
        PartDefinition p_crest1 = p_crest0.addOrReplaceChild("crest1", CubeListBuilder.create()
                .texOffs(190, 212).addBox(-1F, -1F, 0F, 2F, 2F, 8F),
                PartPose.offsetAndRotation(0F, 0F, 10.5F, 0.25F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(110, 230).addBox(-3F, -4.7F, -34F, 2F, 5F, 2F)
                .texOffs(118, 230).addBox(1F, -4.7F, -34F, 2F, 5F, 2F),
                PartPose.offsetAndRotation(0F, -2F, 0F, 0.45F, 0F, 0F));
        PartDefinition p_jawSeg0 = p_jaw.addOrReplaceChild("jawSeg0", CubeListBuilder.create()
                .texOffs(52, 222).addBox(-6.5F, -1.5F, -2.5F, 13F, 3F, 5F)
                .texOffs(258, 200).addBox(-4.5F, -3F, -2.5F, 9F, 6F, 5F),
                PartPose.offsetAndRotation(0F, 2.8819F, -1.75F, -0.0629F, 0F, 0F));
        PartDefinition p_jawSeg1 = p_jaw.addOrReplaceChild("jawSeg1", CubeListBuilder.create()
                .texOffs(390, 222).addBox(-6F, -1F, -2.5F, 12F, 2F, 5F)
                .texOffs(76, 212).addBox(-4F, -2.5F, -2.5F, 8F, 5F, 5F),
                PartPose.offsetAndRotation(0F, 2.6827F, -5.25F, -0.0545F, 0F, 0F));
        PartDefinition p_jawSeg2 = p_jaw.addOrReplaceChild("jawSeg2", CubeListBuilder.create()
                .texOffs(424, 222).addBox(-5.5F, -1F, -2.5F, 11F, 2F, 5F)
                .texOffs(102, 212).addBox(-3.5F, -2.5F, -2.5F, 7F, 5F, 5F),
                PartPose.offsetAndRotation(0F, 2.4974F, -8.75F, -0.0517F, 0F, 0F));
        PartDefinition p_jawSeg3 = p_jaw.addOrReplaceChild("jawSeg3", CubeListBuilder.create()
                .texOffs(456, 222).addBox(-5.5F, -1F, -2.5F, 11F, 2F, 5F)
                .texOffs(126, 212).addBox(-3.5F, -2.5F, -2.5F, 7F, 5F, 5F),
                PartPose.offsetAndRotation(0F, 2.3197F, -12.25F, -0.05F, 0F, 0F));
        PartDefinition p_jawSeg4 = p_jaw.addOrReplaceChild("jawSeg4", CubeListBuilder.create()
                .texOffs(126, 230).addBox(-5F, -0.5F, -2.5F, 10F, 1F, 5F)
                .texOffs(256, 212).addBox(-3F, -2F, -2.5F, 6F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 2.147F, -15.75F, -0.0487F, 0F, 0F));
        PartDefinition p_jawSeg5 = p_jaw.addOrReplaceChild("jawSeg5", CubeListBuilder.create()
                .texOffs(156, 230).addBox(-4.5F, -0.5F, -2.5F, 9F, 1F, 5F)
                .texOffs(278, 212).addBox(-2.5F, -2F, -2.5F, 5F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 1.9782F, -19.25F, -0.0477F, 0F, 0F));
        PartDefinition p_jawSeg6 = p_jaw.addOrReplaceChild("jawSeg6", CubeListBuilder.create()
                .texOffs(184, 230).addBox(-4.5F, -0.5F, -2.5F, 9F, 1F, 5F)
                .texOffs(298, 212).addBox(-2.5F, -2F, -2.5F, 5F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 1.8124F, -22.75F, -0.047F, 0F, 0F));
        PartDefinition p_jawSeg7 = p_jaw.addOrReplaceChild("jawSeg7", CubeListBuilder.create()
                .texOffs(212, 230).addBox(-4F, -0.5F, -2.5F, 8F, 1F, 5F)
                .texOffs(88, 222).addBox(-2F, -1.5F, -2.5F, 4F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 1.6492F, -26.25F, -0.0463F, 0F, 0F));
        PartDefinition p_jawSeg8 = p_jaw.addOrReplaceChild("jawSeg8", CubeListBuilder.create()
                .texOffs(238, 230).addBox(-3.5F, -0.5F, -2.5F, 7F, 1F, 5F)
                .texOffs(138, 222).addBox(-1.5F, -1.5F, -2.5F, 3F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 1.4881F, -29.75F, -0.0457F, 0F, 0F));
        PartDefinition p_jawSeg9 = p_jaw.addOrReplaceChild("jawSeg9", CubeListBuilder.create()
                .texOffs(262, 230).addBox(-3.5F, -0.5F, -2.5F, 7F, 1F, 5F)
                .texOffs(154, 222).addBox(-1.5F, -1.5F, -2.5F, 3F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 1.329F, -33.25F, -0.0452F, 0F, 0F));
        PartDefinition p_toothD0R = p_jaw.addOrReplaceChild("toothD0R", CubeListBuilder.create()
                .texOffs(428, 230).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-4.4107F, 0.5F, -8.5F, 0.25F, 0F, 0F));
        PartDefinition p_toothD0L = p_jaw.addOrReplaceChild("toothD0L", CubeListBuilder.create()
                .texOffs(432, 230).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(4.4107F, 0.5F, -8.5F, 0.25F, 0F, 0F));
        PartDefinition p_toothD1R = p_jaw.addOrReplaceChild("toothD1R", CubeListBuilder.create()
                .texOffs(364, 230).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-4.1414F, 0.5F, -11.4F, 0.25F, 0F, 0F));
        PartDefinition p_toothD1L = p_jaw.addOrReplaceChild("toothD1L", CubeListBuilder.create()
                .texOffs(368, 230).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(4.1414F, 0.5F, -11.4F, 0.25F, 0F, 0F));
        PartDefinition p_toothD2R = p_jaw.addOrReplaceChild("toothD2R", CubeListBuilder.create()
                .texOffs(436, 230).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-3.8721F, 0.5F, -14.3F, 0.25F, 0F, 0F));
        PartDefinition p_toothD2L = p_jaw.addOrReplaceChild("toothD2L", CubeListBuilder.create()
                .texOffs(440, 230).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(3.8721F, 0.5F, -14.3F, 0.25F, 0F, 0F));
        PartDefinition p_toothD3R = p_jaw.addOrReplaceChild("toothD3R", CubeListBuilder.create()
                .texOffs(372, 230).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-3.6029F, 0.5F, -17.2F, 0.25F, 0F, 0F));
        PartDefinition p_toothD3L = p_jaw.addOrReplaceChild("toothD3L", CubeListBuilder.create()
                .texOffs(376, 230).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(3.6029F, 0.5F, -17.2F, 0.25F, 0F, 0F));
        PartDefinition p_toothD4R = p_jaw.addOrReplaceChild("toothD4R", CubeListBuilder.create()
                .texOffs(444, 230).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-3.3336F, 0.5F, -20.1F, 0.25F, 0F, 0F));
        PartDefinition p_toothD4L = p_jaw.addOrReplaceChild("toothD4L", CubeListBuilder.create()
                .texOffs(448, 230).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(3.3336F, 0.5F, -20.1F, 0.25F, 0F, 0F));
        PartDefinition p_toothD5R = p_jaw.addOrReplaceChild("toothD5R", CubeListBuilder.create()
                .texOffs(380, 230).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-3.0643F, 0.5F, -23F, 0.25F, 0F, 0F));
        PartDefinition p_toothD5L = p_jaw.addOrReplaceChild("toothD5L", CubeListBuilder.create()
                .texOffs(384, 230).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(3.0643F, 0.5F, -23F, 0.25F, 0F, 0F));
        PartDefinition p_toothD6R = p_jaw.addOrReplaceChild("toothD6R", CubeListBuilder.create()
                .texOffs(452, 230).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-2.795F, 0.5F, -25.9F, 0.25F, 0F, 0F));
        PartDefinition p_toothD6L = p_jaw.addOrReplaceChild("toothD6L", CubeListBuilder.create()
                .texOffs(456, 230).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(2.795F, 0.5F, -25.9F, 0.25F, 0F, 0F));
        PartDefinition p_toothD7R = p_jaw.addOrReplaceChild("toothD7R", CubeListBuilder.create()
                .texOffs(388, 230).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-2.5257F, 0.5F, -28.8F, 0.25F, 0F, 0F));
        PartDefinition p_toothD7L = p_jaw.addOrReplaceChild("toothD7L", CubeListBuilder.create()
                .texOffs(392, 230).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(2.5257F, 0.5F, -28.8F, 0.25F, 0F, 0F));
        PartDefinition p_toothD8R = p_jaw.addOrReplaceChild("toothD8R", CubeListBuilder.create()
                .texOffs(460, 230).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-2.2564F, 0.5F, -31.7F, 0.25F, 0F, 0F));
        PartDefinition p_toothD8L = p_jaw.addOrReplaceChild("toothD8L", CubeListBuilder.create()
                .texOffs(464, 230).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(2.2564F, 0.5F, -31.7F, 0.25F, 0F, 0F));
        PartDefinition p_jawSpikeR = p_jaw.addOrReplaceChild("jawSpikeR", CubeListBuilder.create()
                .texOffs(330, 200).addBox(-1F, -1F, 0F, 2F, 2F, 9F),
                PartPose.offsetAndRotation(-5.5F, 2.5F, -3F, -0.25F, -0.55F, 0F));
        PartDefinition p_jawSpikeL = p_jaw.addOrReplaceChild("jawSpikeL", CubeListBuilder.create()
                .texOffs(352, 200).addBox(-1F, -1F, 0F, 2F, 2F, 9F),
                PartPose.offsetAndRotation(5.5F, 2.5F, -3F, -0.25F, 0.55F, 0F));
        PartDefinition p_armR = p_chest.addOrReplaceChild("armR", CubeListBuilder.create()
                .texOffs(174, 124).addBox(-4.5F, -2F, -4.5F, 9F, 15F, 9F),
                PartPose.offsetAndRotation(-17F, -17F, 4F, -0.7F, -0.28F, -0.15F));
        PartDefinition p_forearmR = p_armR.addOrReplaceChild("forearmR", CubeListBuilder.create()
                .texOffs(110, 148).addBox(-4F, -2F, -4F, 8F, 14F, 8F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -1F, 0F, 0F));
        PartDefinition p_handR = p_forearmR.addOrReplaceChild("handR", CubeListBuilder.create()
                .texOffs(306, 170).addBox(-5F, -1.5F, -5F, 10F, 5F, 10F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -0.15F, 0F, 0F));
        PartDefinition p_claw0R = p_handR.addOrReplaceChild("claw0R", CubeListBuilder.create()
                .texOffs(294, 222).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(-3.4F, 4F, 0F, 0.25F, -0.17F, 0F));
        PartDefinition p_claw20R = p_claw0R.addOrReplaceChild("claw20R", CubeListBuilder.create()
                .texOffs(124, 200).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_claw1R = p_handR.addOrReplaceChild("claw1R", CubeListBuilder.create()
                .texOffs(306, 222).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0.25F, 0F, 0F));
        PartDefinition p_claw21R = p_claw1R.addOrReplaceChild("claw21R", CubeListBuilder.create()
                .texOffs(136, 200).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_claw2R = p_handR.addOrReplaceChild("claw2R", CubeListBuilder.create()
                .texOffs(318, 222).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(3.4F, 4F, 0F, 0.25F, 0.17F, 0F));
        PartDefinition p_claw22R = p_claw2R.addOrReplaceChild("claw22R", CubeListBuilder.create()
                .texOffs(148, 200).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_thumbR = p_handR.addOrReplaceChild("thumbR", CubeListBuilder.create()
                .texOffs(406, 200).addBox(-1.5F, -0.5F, -1.5F, 3F, 8F, 3F),
                PartPose.offsetAndRotation(-5.5F, 1F, -2F, 0.4F, 0F, -0.5F));
        PartDefinition p_armL = p_chest.addOrReplaceChild("armL", CubeListBuilder.create()
                .texOffs(210, 124).addBox(-4.5F, -2F, -4.5F, 9F, 15F, 9F),
                PartPose.offsetAndRotation(17F, -17F, 4F, -0.7F, 0.28F, 0.15F));
        PartDefinition p_forearmL = p_armL.addOrReplaceChild("forearmL", CubeListBuilder.create()
                .texOffs(142, 148).addBox(-4F, -2F, -4F, 8F, 14F, 8F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -1F, 0F, 0F));
        PartDefinition p_handL = p_forearmL.addOrReplaceChild("handL", CubeListBuilder.create()
                .texOffs(346, 170).addBox(-5F, -1.5F, -5F, 10F, 5F, 10F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -0.15F, 0F, 0F));
        PartDefinition p_claw0L = p_handL.addOrReplaceChild("claw0L", CubeListBuilder.create()
                .texOffs(330, 222).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(-3.4F, 4F, 0F, 0.25F, -0.17F, 0F));
        PartDefinition p_claw20L = p_claw0L.addOrReplaceChild("claw20L", CubeListBuilder.create()
                .texOffs(160, 200).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_claw1L = p_handL.addOrReplaceChild("claw1L", CubeListBuilder.create()
                .texOffs(342, 222).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0.25F, 0F, 0F));
        PartDefinition p_claw21L = p_claw1L.addOrReplaceChild("claw21L", CubeListBuilder.create()
                .texOffs(172, 200).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_claw2L = p_handL.addOrReplaceChild("claw2L", CubeListBuilder.create()
                .texOffs(354, 222).addBox(-1.5F, -0.5F, -1.5F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(3.4F, 4F, 0F, 0.25F, 0.17F, 0F));
        PartDefinition p_claw22L = p_claw2L.addOrReplaceChild("claw22L", CubeListBuilder.create()
                .texOffs(184, 200).addBox(-1.5F, -0.5F, -1.5F, 3F, 9F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.55F, 0F, 0F));
        PartDefinition p_thumbL = p_handL.addOrReplaceChild("thumbL", CubeListBuilder.create()
                .texOffs(418, 200).addBox(-1.5F, -0.5F, -1.5F, 3F, 8F, 3F),
                PartPose.offsetAndRotation(5.5F, 1F, -2F, 0.4F, 0F, 0.5F));
        PartDefinition p_wingR = p_chest.addOrReplaceChild("wingR", CubeListBuilder.create()
                .texOffs(356, 186).addBox(-18F, -3F, -3F, 18F, 6F, 6F)
                .texOffs(430, 200).addBox(-36.5F, -2.5F, -2.5F, 19F, 5F, 5F)
                .texOffs(0, 0).addBox(-36F, -0.5F, 0F, 36F, 1F, 48F),
                PartPose.offsetAndRotation(-11F, -19F, -8F, -0.85F, 0.55F, 0.8F));
        PartDefinition p_wingOutR = p_wingR.addOrReplaceChild("wingOutR", CubeListBuilder.create()
                .texOffs(336, 0).addBox(-22F, -0.5F, 0F, 22F, 1F, 48F)
                .texOffs(454, 212).addBox(-22F, -2F, -1F, 22F, 4F, 4F)
                .texOffs(366, 222).addBox(-2.5F, -6F, -1F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(-36F, 0F, 0F, 0F, 0.1F, 0.18F));
        PartDefinition p_wingL = p_chest.addOrReplaceChild("wingL", CubeListBuilder.create()
                .texOffs(404, 186).addBox(0F, -3F, -3F, 18F, 6F, 6F)
                .texOffs(0, 212).addBox(17.5F, -2.5F, -2.5F, 19F, 5F, 5F)
                .texOffs(168, 0).addBox(0F, -0.5F, 0F, 36F, 1F, 48F),
                PartPose.offsetAndRotation(11F, -19F, -8F, -0.85F, -0.55F, -0.8F));
        PartDefinition p_wingOutL = p_wingL.addOrReplaceChild("wingOutL", CubeListBuilder.create()
                .texOffs(0, 49).addBox(0F, -0.5F, 0F, 22F, 1F, 48F)
                .texOffs(0, 222).addBox(0F, -2F, -1F, 22F, 4F, 4F)
                .texOffs(378, 222).addBox(-0.5F, -6F, -1F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(36F, 0F, 0F, 0F, -0.1F, -0.18F));
        PartDefinition p_tail1 = p_pelvis.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(60, 98).addBox(-8F, -6F, -0.5F, 16F, 12F, 14F)
                .texOffs(382, 212).addBox(-1F, -9F, 4.5F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -0.5F, 10F, -0.1F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(414, 98).addBox(-7F, -5.5F, -0.5F, 14F, 11F, 13F)
                .texOffs(396, 212).addBox(-1F, -8.5F, 4F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 13F, 0.02F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(424, 124).addBox(-6F, -5F, -0.5F, 12F, 10F, 13F)
                .texOffs(410, 212).addBox(-1F, -8F, 4F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 12F, 0.16F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(174, 148).addBox(-5F, -4.5F, -0.5F, 10F, 9F, 12F)
                .texOffs(424, 212).addBox(-1F, -7.5F, 3.5F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 12F, 0.28F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(218, 148).addBox(-4F, -3.5F, -0.5F, 8F, 7F, 12F)
                .texOffs(266, 222).addBox(-1F, -6F, 3.5F, 2F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.34F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(68, 170).addBox(-3F, -2.5F, -0.5F, 6F, 5F, 11F)
                .texOffs(280, 222).addBox(-1F, -5F, 3F, 2F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.4F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(308, 186).addBox(-2F, -1.5F, -0.5F, 4F, 3F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.34F, 0F, 0F));
        PartDefinition p_tailtip = p_tail7.addOrReplaceChild("tailtip", CubeListBuilder.create()
                .texOffs(80, 200).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.25F, 0F, 0F));
        PartDefinition p_thighR = p_pelvis.addOrReplaceChild("thighR", CubeListBuilder.create()
                .texOffs(316, 49).addBox(-5.5F, -1F, -6.5F, 11F, 15F, 13F),
                PartPose.offsetAndRotation(-12F, 3F, 0F, -0.55F, 0F, 0F));
        PartDefinition p_shinR = p_thighR.addOrReplaceChild("shinR", CubeListBuilder.create()
                .texOffs(102, 124).addBox(-4.5F, -1F, -4.5F, 9F, 15F, 9F),
                PartPose.offsetAndRotation(0F, 13F, -0.5F, 1.05F, 0F, 0F));
        PartDefinition p_footR = p_shinR.addOrReplaceChild("footR", CubeListBuilder.create()
                .texOffs(352, 148).addBox(-5.5F, -1F, -9F, 11F, 5F, 12F),
                PartPose.offsetAndRotation(0F, 13.5F, 0F, -0.5F, 0F, 0F));
        PartDefinition p_toe0R = p_footR.addOrReplaceChild("toe0R", CubeListBuilder.create()
                .texOffs(170, 222).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(-3.2F, 2F, -7.2F, 0.35F, -0.16F, 0F));
        PartDefinition p_toe1R = p_footR.addOrReplaceChild("toe1R", CubeListBuilder.create()
                .texOffs(186, 222).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(0F, 2F, -7.2F, 0.35F, 0F, 0F));
        PartDefinition p_toe2R = p_footR.addOrReplaceChild("toe2R", CubeListBuilder.create()
                .texOffs(202, 222).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(3.2F, 2F, -7.2F, 0.35F, 0.16F, 0F));
        PartDefinition p_heelR = p_footR.addOrReplaceChild("heelR", CubeListBuilder.create()
                .texOffs(82, 230).addBox(-1F, -1F, 0F, 2F, 2F, 5F),
                PartPose.offsetAndRotation(0F, 1.5F, 2.5F, 0.5F, 0F, 0F));
        PartDefinition p_thighL = p_pelvis.addOrReplaceChild("thighL", CubeListBuilder.create()
                .texOffs(364, 49).addBox(-5.5F, -1F, -6.5F, 11F, 15F, 13F),
                PartPose.offsetAndRotation(12F, 3F, 0F, -0.55F, 0F, 0F));
        PartDefinition p_shinL = p_thighL.addOrReplaceChild("shinL", CubeListBuilder.create()
                .texOffs(138, 124).addBox(-4.5F, -1F, -4.5F, 9F, 15F, 9F),
                PartPose.offsetAndRotation(0F, 13F, -0.5F, 1.05F, 0F, 0F));
        PartDefinition p_footL = p_shinL.addOrReplaceChild("footL", CubeListBuilder.create()
                .texOffs(398, 148).addBox(-5.5F, -1F, -9F, 11F, 5F, 12F),
                PartPose.offsetAndRotation(0F, 13.5F, 0F, -0.5F, 0F, 0F));
        PartDefinition p_toe0L = p_footL.addOrReplaceChild("toe0L", CubeListBuilder.create()
                .texOffs(218, 222).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(-3.2F, 2F, -7.2F, 0.35F, -0.16F, 0F));
        PartDefinition p_toe1L = p_footL.addOrReplaceChild("toe1L", CubeListBuilder.create()
                .texOffs(234, 222).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(0F, 2F, -7.2F, 0.35F, 0F, 0F));
        PartDefinition p_toe2L = p_footL.addOrReplaceChild("toe2L", CubeListBuilder.create()
                .texOffs(250, 222).addBox(-1F, -1F, -6F, 2F, 2F, 6F),
                PartPose.offsetAndRotation(3.2F, 2F, -7.2F, 0.35F, 0.16F, 0F));
        PartDefinition p_heelL = p_footL.addOrReplaceChild("heelL", CubeListBuilder.create()
                .texOffs(96, 230).addBox(-1F, -1F, 0F, 2F, 2F, 5F),
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
        m.put("neck5", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5"));
        m.put("neck6", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6"));
        m.put("neck7", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7"));
        m.put("head", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head"));
        m.put("skull0", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull0"));
        m.put("skull1", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull1"));
        m.put("skull2", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull2"));
        m.put("skull3", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull3"));
        m.put("skull4", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull4"));
        m.put("skull5", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull5"));
        m.put("skull6", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull6"));
        m.put("skull7", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull7"));
        m.put("skull8", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull8"));
        m.put("skull9", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull9"));
        m.put("skull10", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull10"));
        m.put("skull11", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull11"));
        m.put("skull12", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull12"));
        m.put("skull13", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("skull13"));
        m.put("eyeR", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("eyeR"));
        m.put("browR", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("browR"));
        m.put("cheekPlateR", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("cheekPlateR"));
        m.put("eyeL", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("eyeL"));
        m.put("browL", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("browL"));
        m.put("cheekPlateL", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("cheekPlateL"));
        m.put("toothU0R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU0R"));
        m.put("toothU0L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU0L"));
        m.put("toothU1R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU1R"));
        m.put("toothU1L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU1L"));
        m.put("toothU2R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU2R"));
        m.put("toothU2L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU2L"));
        m.put("toothU3R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU3R"));
        m.put("toothU3L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU3L"));
        m.put("toothU4R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU4R"));
        m.put("toothU4L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU4L"));
        m.put("toothU5R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU5R"));
        m.put("toothU5L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU5L"));
        m.put("toothU6R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU6R"));
        m.put("toothU6L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU6L"));
        m.put("toothU7R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU7R"));
        m.put("toothU7L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU7L"));
        m.put("toothU8R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU8R"));
        m.put("toothU8L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("toothU8L"));
        m.put("spike0R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike0R"));
        m.put("spikeB0R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike0R").getChild("spikeB0R"));
        m.put("spikeC0R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike0R").getChild("spikeB0R").getChild("spikeC0R"));
        m.put("spike1R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike1R"));
        m.put("spikeB1R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike1R").getChild("spikeB1R"));
        m.put("spikeC1R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike1R").getChild("spikeB1R").getChild("spikeC1R"));
        m.put("spike2R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike2R"));
        m.put("spikeB2R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike2R").getChild("spikeB2R"));
        m.put("spikeC2R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike2R").getChild("spikeB2R").getChild("spikeC2R"));
        m.put("spike0L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike0L"));
        m.put("spikeB0L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike0L").getChild("spikeB0L"));
        m.put("spikeC0L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike0L").getChild("spikeB0L").getChild("spikeC0L"));
        m.put("spike1L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike1L"));
        m.put("spikeB1L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike1L").getChild("spikeB1L"));
        m.put("spikeC1L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike1L").getChild("spikeB1L").getChild("spikeC1L"));
        m.put("spike2L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike2L"));
        m.put("spikeB2L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike2L").getChild("spikeB2L"));
        m.put("spikeC2L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("spike2L").getChild("spikeB2L").getChild("spikeC2L"));
        m.put("crest0", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("crest0"));
        m.put("crest1", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("crest0").getChild("crest1"));
        m.put("jaw", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw"));
        m.put("jawSeg0", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSeg0"));
        m.put("jawSeg1", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSeg1"));
        m.put("jawSeg2", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSeg2"));
        m.put("jawSeg3", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSeg3"));
        m.put("jawSeg4", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSeg4"));
        m.put("jawSeg5", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSeg5"));
        m.put("jawSeg6", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSeg6"));
        m.put("jawSeg7", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSeg7"));
        m.put("jawSeg8", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSeg8"));
        m.put("jawSeg9", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSeg9"));
        m.put("toothD0R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD0R"));
        m.put("toothD0L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD0L"));
        m.put("toothD1R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD1R"));
        m.put("toothD1L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD1L"));
        m.put("toothD2R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD2R"));
        m.put("toothD2L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD2L"));
        m.put("toothD3R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD3R"));
        m.put("toothD3L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD3L"));
        m.put("toothD4R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD4R"));
        m.put("toothD4L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD4L"));
        m.put("toothD5R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD5R"));
        m.put("toothD5L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD5L"));
        m.put("toothD6R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD6R"));
        m.put("toothD6L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD6L"));
        m.put("toothD7R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD7R"));
        m.put("toothD7L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD7L"));
        m.put("toothD8R", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD8R"));
        m.put("toothD8L", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("toothD8L"));
        m.put("jawSpikeR", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSpikeR"));
        m.put("jawSpikeL", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSpikeL"));
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
