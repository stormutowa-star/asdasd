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
    public static final int TEX_H = 512;

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition p_pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create()
                .texOffs(388, 54).addBox(-12F, -8F, -11F, 24F, 16F, 22F)
                .texOffs(0, 103).addBox(-10F, -10F, -9F, 20F, 20F, 18F),
                PartPose.offset(0F, -27F, -3F));
        PartDefinition p_chest = p_pelvis.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(280, 54).addBox(-15F, -24F, -11F, 30F, 24F, 24F)
                .texOffs(0, 0).addBox(-12F, -26F, -12F, 24F, 28F, 26F),
                PartPose.offsetAndRotation(0F, -5F, 5F, -0.3F, 0F, 0F));
        PartDefinition p_neck1 = p_chest.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(44, 141).addBox(-10F, -8F, -9F, 20F, 9F, 18F)
                .texOffs(192, 103).addBox(-8F, -8F, -10F, 16F, 9F, 20F)
                .texOffs(62, 259).addBox(-1.5F, -11F, -9.5F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -24F, 2F, -0.62F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(246, 141).addBox(-9.5F, -8F, -8.5F, 19F, 9F, 17F)
                .texOffs(264, 103).addBox(-7.5F, -8F, -9.5F, 15F, 9F, 19F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, 0.1F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(318, 141).addBox(-9F, -8F, -8.5F, 18F, 9F, 17F)
                .texOffs(332, 103).addBox(-7F, -8F, -9.5F, 14F, 9F, 19F)
                .texOffs(78, 259).addBox(-1.5F, -11F, -9F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, 0.3F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(88, 169).addBox(-9F, -8F, -8F, 18F, 9F, 16F)
                .texOffs(120, 141).addBox(-7F, -8F, -9F, 14F, 9F, 18F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, 0.38F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(156, 169).addBox(-8.5F, -8F, -8F, 17F, 9F, 16F)
                .texOffs(184, 141).addBox(-6.5F, -8F, -9F, 13F, 9F, 18F)
                .texOffs(94, 259).addBox(-1.5F, -11F, -8.5F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, 0.4F, 0F, 0F));
        PartDefinition p_neck6 = p_neck5.addOrReplaceChild("neck6", CubeListBuilder.create()
                .texOffs(360, 169).addBox(-8F, -8F, -7.5F, 16F, 9F, 15F)
                .texOffs(388, 141).addBox(-6F, -8F, -8.5F, 12F, 9F, 17F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, 0.38F, 0F, 0F));
        PartDefinition p_neck7 = p_neck6.addOrReplaceChild("neck7", CubeListBuilder.create()
                .texOffs(422, 169).addBox(-7.5F, -8F, -7.5F, 15F, 9F, 15F)
                .texOffs(446, 141).addBox(-5.5F, -8F, -8.5F, 11F, 9F, 17F)
                .texOffs(110, 259).addBox(-1.5F, -11F, -8F, 3F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, 0.3F, 0F, 0F));
        PartDefinition p_head = p_neck7.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(376, 268).addBox(-3.4F, -8F, -32F, 1F, 2F, 2F)
                .texOffs(382, 268).addBox(2.4F, -8F, -32F, 1F, 2F, 2F)
                .texOffs(96, 236).addBox(-4.5F, -3F, -10F, 9F, 4F, 8F),
                PartPose.offsetAndRotation(0F, -9F, -1F, -0.9F, 0F, 0F));
        PartDefinition p_skull0 = p_head.addOrReplaceChild("skull0", CubeListBuilder.create()
                .texOffs(242, 236).addBox(-6F, -3.5F, -2F, 12F, 7F, 4F)
                .texOffs(312, 219).addBox(-4.5F, -4.5F, -2F, 9F, 9F, 4F),
                PartPose.offsetAndRotation(0F, -6.7322F, 1.5F, 0.0157F, 0F, 0F));
        PartDefinition p_skull1 = p_head.addOrReplaceChild("skull1", CubeListBuilder.create()
                .texOffs(274, 236).addBox(-5.5F, -3.5F, -2F, 11F, 7F, 4F)
                .texOffs(390, 219).addBox(-4F, -4.5F, -2F, 8F, 9F, 4F),
                PartPose.offsetAndRotation(0F, -6.6682F, -1.5F, 0.0245F, 0F, 0F));
        PartDefinition p_skull2 = p_head.addOrReplaceChild("skull2", CubeListBuilder.create()
                .texOffs(304, 236).addBox(-5.5F, -3.5F, -2F, 11F, 7F, 4F)
                .texOffs(414, 219).addBox(-4F, -4.5F, -2F, 8F, 9F, 4F),
                PartPose.offsetAndRotation(0F, -6.5878F, -4.5F, 0.0287F, 0F, 0F));
        PartDefinition p_skull3 = p_head.addOrReplaceChild("skull3", CubeListBuilder.create()
                .texOffs(136, 248).addBox(-5.5F, -3F, -2F, 11F, 6F, 4F)
                .texOffs(130, 236).addBox(-4F, -4F, -2F, 8F, 8F, 4F),
                PartPose.offsetAndRotation(0F, -6.4969F, -7.5F, 0.0317F, 0F, 0F));
        PartDefinition p_skull4 = p_head.addOrReplaceChild("skull4", CubeListBuilder.create()
                .texOffs(166, 248).addBox(-5F, -3F, -2F, 10F, 6F, 4F)
                .texOffs(178, 236).addBox(-3.5F, -4F, -2F, 7F, 8F, 4F),
                PartPose.offsetAndRotation(0F, -6.3981F, -10.5F, 0.034F, 0F, 0F));
        PartDefinition p_skull5 = p_head.addOrReplaceChild("skull5", CubeListBuilder.create()
                .texOffs(194, 248).addBox(-5F, -3F, -2F, 10F, 6F, 4F)
                .texOffs(200, 236).addBox(-3.5F, -4F, -2F, 7F, 8F, 4F),
                PartPose.offsetAndRotation(0F, -6.293F, -13.5F, 0.036F, 0F, 0F));
        PartDefinition p_skull6 = p_head.addOrReplaceChild("skull6", CubeListBuilder.create()
                .texOffs(410, 248).addBox(-4.5F, -2.5F, -2F, 9F, 5F, 4F)
                .texOffs(0, 248).addBox(-3F, -3.5F, -2F, 6F, 7F, 4F),
                PartPose.offsetAndRotation(0F, -6.1823F, -16.5F, 0.0377F, 0F, 0F));
        PartDefinition p_skull7 = p_head.addOrReplaceChild("skull7", CubeListBuilder.create()
                .texOffs(436, 248).addBox(-4.5F, -2.5F, -2F, 9F, 5F, 4F)
                .texOffs(20, 248).addBox(-3F, -3.5F, -2F, 6F, 7F, 4F),
                PartPose.offsetAndRotation(0F, -6.0669F, -19.5F, 0.0392F, 0F, 0F));
        PartDefinition p_skull8 = p_head.addOrReplaceChild("skull8", CubeListBuilder.create()
                .texOffs(302, 259).addBox(-4.5F, -2F, -2F, 9F, 4F, 4F)
                .texOffs(222, 248).addBox(-3F, -3F, -2F, 6F, 6F, 4F),
                PartPose.offsetAndRotation(0F, -5.9472F, -22.5F, 0.0405F, 0F, 0F));
        PartDefinition p_skull9 = p_head.addOrReplaceChild("skull9", CubeListBuilder.create()
                .texOffs(328, 259).addBox(-4F, -2F, -2F, 8F, 4F, 4F)
                .texOffs(242, 248).addBox(-2.5F, -3F, -2F, 5F, 6F, 4F),
                PartPose.offsetAndRotation(0F, -5.8237F, -25.5F, 0.0418F, 0F, 0F));
        PartDefinition p_skull10 = p_head.addOrReplaceChild("skull10", CubeListBuilder.create()
                .texOffs(352, 259).addBox(-4F, -2F, -2F, 8F, 4F, 4F)
                .texOffs(260, 248).addBox(-2.5F, -3F, -2F, 5F, 6F, 4F),
                PartPose.offsetAndRotation(0F, -5.6966F, -28.5F, 0.0429F, 0F, 0F));
        PartDefinition p_skull11 = p_head.addOrReplaceChild("skull11", CubeListBuilder.create()
                .texOffs(130, 268).addBox(-3.5F, -1.5F, -2F, 7F, 3F, 4F)
                .texOffs(126, 259).addBox(-2F, -2.5F, -2F, 4F, 5F, 4F),
                PartPose.offsetAndRotation(0F, -5.5663F, -31.5F, 0.0439F, 0F, 0F));
        PartDefinition p_beak = p_head.addOrReplaceChild("beak", CubeListBuilder.create()
                .texOffs(24, 259).addBox(-2.5F, -1.5F, -4F, 5F, 4F, 5F)
                .texOffs(214, 268).addBox(-1.5F, 0.5F, -5.5F, 3F, 4F, 3F),
                PartPose.offsetAndRotation(0F, -6F, -32.5F, 0.55F, 0F, 0F));
        PartDefinition p_visor0 = p_head.addOrReplaceChild("visor0", CubeListBuilder.create()
                .texOffs(222, 219).addBox(-4F, -2F, 0F, 8F, 4F, 10F),
                PartPose.offsetAndRotation(0F, -9.6F, -33F, -0.2F, 0F, 0F));
        PartDefinition p_visor1 = p_visor0.addOrReplaceChild("visor1", CubeListBuilder.create()
                .texOffs(28, 219).addBox(-4F, -3F, 0F, 8F, 6F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 9.2F, 0.08F, 0F, 0F));
        PartDefinition p_visor2 = p_visor1.addOrReplaceChild("visor2", CubeListBuilder.create()
                .texOffs(428, 195).addBox(-3F, -3.5F, 0F, 6F, 7F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 9.2F, 0.12F, 0F, 0F));
        PartDefinition p_visor3 = p_visor2.addOrReplaceChild("visor3", CubeListBuilder.create()
                .texOffs(460, 195).addBox(-2.5F, -3.5F, 0F, 5F, 7F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 9.2F, 0.16F, 0F, 0F));
        PartDefinition p_visor4 = p_visor3.addOrReplaceChild("visor4", CubeListBuilder.create()
                .texOffs(0, 219).addBox(-2F, -3.5F, 0F, 4F, 7F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 9.2F, 0.2F, 0F, 0F));
        PartDefinition p_visor5 = p_visor4.addOrReplaceChild("visor5", CubeListBuilder.create()
                .texOffs(124, 219).addBox(-2F, -3F, 0F, 4F, 6F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 9.2F, 0.22F, 0F, 0F));
        PartDefinition p_visor6 = p_visor5.addOrReplaceChild("visor6", CubeListBuilder.create()
                .texOffs(288, 219).addBox(-1.5F, -2.5F, 0F, 3F, 5F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 8.2F, 0.24F, 0F, 0F));
        PartDefinition p_visor7 = p_visor6.addOrReplaceChild("visor7", CubeListBuilder.create()
                .texOffs(222, 236).addBox(-1F, -2F, 0F, 2F, 4F, 8F),
                PartPose.offsetAndRotation(0F, 0F, 8.2F, 0.24F, 0F, 0F));
        PartDefinition p_visor8 = p_visor7.addOrReplaceChild("visor8", CubeListBuilder.create()
                .texOffs(44, 259).addBox(-1F, -1F, 0F, 2F, 2F, 7F),
                PartPose.offsetAndRotation(0F, 0F, 7.2F, 0.2F, 0F, 0F));
        PartDefinition p_eyeR = p_head.addOrReplaceChild("eyeR", CubeListBuilder.create()
                .texOffs(350, 195).addBox(-1.1F, -4.5F, -4.5F, 1F, 9F, 9F),
                PartPose.offsetAndRotation(-4.6F, -8.4F, -17F, 0F, -0.14F, 0F));
        PartDefinition p_earSpikeR0 = p_head.addOrReplaceChild("earSpikeR0", CubeListBuilder.create()
                .texOffs(64, 219).addBox(-1.5F, -2F, 0F, 3F, 4F, 12F),
                PartPose.offsetAndRotation(-4.5F, -7.5F, -4F, -0.1F, -0.3F, 0F));
        PartDefinition p_earSpikeR1 = p_earSpikeR0.addOrReplaceChild("earSpikeR1", CubeListBuilder.create()
                .texOffs(438, 219).addBox(-1F, -1.5F, 0F, 2F, 3F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 11.2F, 0.05F, 0F, 0F));
        PartDefinition p_earSpikeR2 = p_earSpikeR1.addOrReplaceChild("earSpikeR2", CubeListBuilder.create()
                .texOffs(278, 248).addBox(-0.5F, -1F, 0F, 1F, 2F, 8F),
                PartPose.offsetAndRotation(0F, 0F, 9.2F, 0.08F, 0F, 0F));
        PartDefinition p_eyeL = p_head.addOrReplaceChild("eyeL", CubeListBuilder.create()
                .texOffs(370, 195).addBox(0.1F, -4.5F, -4.5F, 1F, 9F, 9F),
                PartPose.offsetAndRotation(4.6F, -8.4F, -17F, 0F, 0.14F, 0F));
        PartDefinition p_earSpikeL0 = p_head.addOrReplaceChild("earSpikeL0", CubeListBuilder.create()
                .texOffs(94, 219).addBox(-1.5F, -2F, 0F, 3F, 4F, 12F),
                PartPose.offsetAndRotation(4.5F, -7.5F, -4F, -0.1F, 0.3F, 0F));
        PartDefinition p_earSpikeL1 = p_earSpikeL0.addOrReplaceChild("earSpikeL1", CubeListBuilder.create()
                .texOffs(462, 219).addBox(-1F, -1.5F, 0F, 2F, 3F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 11.2F, 0.05F, 0F, 0F));
        PartDefinition p_earSpikeL2 = p_earSpikeL1.addOrReplaceChild("earSpikeL2", CubeListBuilder.create()
                .texOffs(296, 248).addBox(-0.5F, -1F, 0F, 1F, 2F, 8F),
                PartPose.offsetAndRotation(0F, 0F, 9.2F, 0.08F, 0F, 0F));
        PartDefinition p_toothU0R = p_head.addOrReplaceChild("toothU0R", CubeListBuilder.create()
                .texOffs(388, 268).addBox(-0.5F, 0F, -1F, 1F, 2F, 2F)
                .texOffs(12, 276).addBox(-0.5F, 2F, -0.2F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(-3.9048F, -2.8722F, -14F, -0.15F, 0F, 0F));
        PartDefinition p_toothU0L = p_head.addOrReplaceChild("toothU0L", CubeListBuilder.create()
                .texOffs(394, 268).addBox(-0.5F, 0F, -1F, 1F, 2F, 2F)
                .texOffs(16, 276).addBox(-0.5F, 2F, -0.2F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(3.9048F, -2.8722F, -14F, -0.15F, 0F, 0F));
        PartDefinition p_toothU1R = p_head.addOrReplaceChild("toothU1R", CubeListBuilder.create()
                .texOffs(400, 268).addBox(-0.5F, 0F, -1F, 1F, 2F, 2F)
                .texOffs(20, 276).addBox(-0.5F, 2F, -0.2F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(-3.6616F, -2.9667F, -17.4F, -0.15F, 0F, 0F));
        PartDefinition p_toothU1L = p_head.addOrReplaceChild("toothU1L", CubeListBuilder.create()
                .texOffs(406, 268).addBox(-0.5F, 0F, -1F, 1F, 2F, 2F)
                .texOffs(24, 276).addBox(-0.5F, 2F, -0.2F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(3.6616F, -2.9667F, -17.4F, -0.15F, 0F, 0F));
        PartDefinition p_toothU2R = p_head.addOrReplaceChild("toothU2R", CubeListBuilder.create()
                .texOffs(412, 268).addBox(-0.5F, 0F, -1F, 1F, 2F, 2F)
                .texOffs(28, 276).addBox(-0.5F, 2F, -0.2F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(-3.4142F, -3.0611F, -20.8F, -0.15F, 0F, 0F));
        PartDefinition p_toothU2L = p_head.addOrReplaceChild("toothU2L", CubeListBuilder.create()
                .texOffs(418, 268).addBox(-0.5F, 0F, -1F, 1F, 2F, 2F)
                .texOffs(32, 276).addBox(-0.5F, 2F, -0.2F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(3.4142F, -3.0611F, -20.8F, -0.15F, 0F, 0F));
        PartDefinition p_toothU3R = p_head.addOrReplaceChild("toothU3R", CubeListBuilder.create()
                .texOffs(424, 268).addBox(-0.5F, 0F, -1F, 1F, 2F, 2F)
                .texOffs(36, 276).addBox(-0.5F, 2F, -0.2F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(-3.1633F, -3.1556F, -24.2F, -0.15F, 0F, 0F));
        PartDefinition p_toothU3L = p_head.addOrReplaceChild("toothU3L", CubeListBuilder.create()
                .texOffs(430, 268).addBox(-0.5F, 0F, -1F, 1F, 2F, 2F)
                .texOffs(40, 276).addBox(-0.5F, 2F, -0.2F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(3.1633F, -3.1556F, -24.2F, -0.15F, 0F, 0F));
        PartDefinition p_toothU4R = p_head.addOrReplaceChild("toothU4R", CubeListBuilder.create()
                .texOffs(436, 268).addBox(-0.5F, 0F, -1F, 1F, 2F, 2F)
                .texOffs(44, 276).addBox(-0.5F, 2F, -0.2F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(-2.9093F, -3.25F, -27.6F, -0.15F, 0F, 0F));
        PartDefinition p_toothU4L = p_head.addOrReplaceChild("toothU4L", CubeListBuilder.create()
                .texOffs(442, 268).addBox(-0.5F, 0F, -1F, 1F, 2F, 2F)
                .texOffs(48, 276).addBox(-0.5F, 2F, -0.2F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(2.9093F, -3.25F, -27.6F, -0.15F, 0F, 0F));
        PartDefinition p_toothU5R = p_head.addOrReplaceChild("toothU5R", CubeListBuilder.create()
                .texOffs(448, 268).addBox(-0.5F, 0F, -1F, 1F, 2F, 2F)
                .texOffs(52, 276).addBox(-0.5F, 2F, -0.2F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(-2.6523F, -3.3444F, -31F, -0.15F, 0F, 0F));
        PartDefinition p_toothU5L = p_head.addOrReplaceChild("toothU5L", CubeListBuilder.create()
                .texOffs(454, 268).addBox(-0.5F, 0F, -1F, 1F, 2F, 2F)
                .texOffs(56, 276).addBox(-0.5F, 2F, -0.2F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(2.6523F, -3.3444F, -31F, -0.15F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, -2.6F, -2F, 0.62F, 0F, 0F));
        PartDefinition p_jawSeg0 = p_jaw.addOrReplaceChild("jawSeg0", CubeListBuilder.create()
                .texOffs(38, 268).addBox(-5.5F, -1F, -2.5F, 11F, 2F, 5F)
                .texOffs(462, 248).addBox(-4F, -2F, -2.5F, 8F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 2.1573F, -1.6667F, -0.0519F, 0F, 0F));
        PartDefinition p_jawSeg1 = p_jaw.addOrReplaceChild("jawSeg1", CubeListBuilder.create()
                .texOffs(70, 268).addBox(-5F, -1F, -2.5F, 10F, 2F, 5F)
                .texOffs(488, 248).addBox(-3.5F, -2F, -2.5F, 7F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 2.0008F, -5F, -0.0449F, 0F, 0F));
        PartDefinition p_jawSeg2 = p_jaw.addOrReplaceChild("jawSeg2", CubeListBuilder.create()
                .texOffs(100, 268).addBox(-5F, -1F, -2.5F, 10F, 2F, 5F)
                .texOffs(0, 259).addBox(-3.5F, -2F, -2.5F, 7F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 1.8553F, -8.3333F, -0.0426F, 0F, 0F));
        PartDefinition p_jawSeg3 = p_jaw.addOrReplaceChild("jawSeg3", CubeListBuilder.create()
                .texOffs(226, 268).addBox(-4.5F, -0.5F, -2.5F, 9F, 1F, 5F)
                .texOffs(376, 259).addBox(-3F, -1.5F, -2.5F, 6F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 1.7157F, -11.6667F, -0.0412F, 0F, 0F));
        PartDefinition p_jawSeg4 = p_jaw.addOrReplaceChild("jawSeg4", CubeListBuilder.create()
                .texOffs(254, 268).addBox(-4F, -0.5F, -2.5F, 8F, 1F, 5F)
                .texOffs(398, 259).addBox(-2.5F, -1.5F, -2.5F, 5F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 1.5801F, -15F, -0.0402F, 0F, 0F));
        PartDefinition p_jawSeg5 = p_jaw.addOrReplaceChild("jawSeg5", CubeListBuilder.create()
                .texOffs(280, 268).addBox(-4F, -0.5F, -2.5F, 8F, 1F, 5F)
                .texOffs(418, 259).addBox(-2.5F, -1.5F, -2.5F, 5F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 1.4475F, -18.3333F, -0.0394F, 0F, 0F));
        PartDefinition p_jawSeg6 = p_jaw.addOrReplaceChild("jawSeg6", CubeListBuilder.create()
                .texOffs(306, 268).addBox(-3.5F, -0.5F, -2.5F, 7F, 1F, 5F)
                .texOffs(438, 259).addBox(-2F, -1.5F, -2.5F, 4F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 1.3174F, -21.6667F, -0.0387F, 0F, 0F));
        PartDefinition p_jawSeg7 = p_jaw.addOrReplaceChild("jawSeg7", CubeListBuilder.create()
                .texOffs(330, 268).addBox(-3.5F, -0.5F, -2.5F, 7F, 1F, 5F)
                .texOffs(152, 268).addBox(-2F, -1F, -2.5F, 4F, 2F, 5F),
                PartPose.offsetAndRotation(0F, 1.1892F, -25F, -0.0382F, 0F, 0F));
        PartDefinition p_jawSeg8 = p_jaw.addOrReplaceChild("jawSeg8", CubeListBuilder.create()
                .texOffs(354, 268).addBox(-3F, -0.5F, -2.5F, 6F, 1F, 5F)
                .texOffs(170, 268).addBox(-1.5F, -1F, -2.5F, 3F, 2F, 5F),
                PartPose.offsetAndRotation(0F, 1.0627F, -28.3333F, -0.0377F, 0F, 0F));
        PartDefinition p_toothD0R = p_jaw.addOrReplaceChild("toothD0R", CubeListBuilder.create()
                .texOffs(460, 268).addBox(-0.5F, -2F, -1F, 1F, 2F, 2F)
                .texOffs(60, 276).addBox(-0.5F, -3F, -0.8F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(-3.75F, 0.3F, -9F, 0.15F, 0F, 0F));
        PartDefinition p_toothD0L = p_jaw.addOrReplaceChild("toothD0L", CubeListBuilder.create()
                .texOffs(466, 268).addBox(-0.5F, -2F, -1F, 1F, 2F, 2F)
                .texOffs(64, 276).addBox(-0.5F, -3F, -0.8F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(3.75F, 0.3F, -9F, 0.15F, 0F, 0F));
        PartDefinition p_toothD1R = p_jaw.addOrReplaceChild("toothD1R", CubeListBuilder.create()
                .texOffs(472, 268).addBox(-0.5F, -2F, -1F, 1F, 2F, 2F)
                .texOffs(68, 276).addBox(-0.5F, -3F, -0.8F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(-3.4167F, 0.3F, -13F, 0.15F, 0F, 0F));
        PartDefinition p_toothD1L = p_jaw.addOrReplaceChild("toothD1L", CubeListBuilder.create()
                .texOffs(478, 268).addBox(-0.5F, -2F, -1F, 1F, 2F, 2F)
                .texOffs(72, 276).addBox(-0.5F, -3F, -0.8F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(3.4167F, 0.3F, -13F, 0.15F, 0F, 0F));
        PartDefinition p_toothD2R = p_jaw.addOrReplaceChild("toothD2R", CubeListBuilder.create()
                .texOffs(484, 268).addBox(-0.5F, -2F, -1F, 1F, 2F, 2F)
                .texOffs(76, 276).addBox(-0.5F, -3F, -0.8F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(-3.0833F, 0.3F, -17F, 0.15F, 0F, 0F));
        PartDefinition p_toothD2L = p_jaw.addOrReplaceChild("toothD2L", CubeListBuilder.create()
                .texOffs(490, 268).addBox(-0.5F, -2F, -1F, 1F, 2F, 2F)
                .texOffs(80, 276).addBox(-0.5F, -3F, -0.8F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(3.0833F, 0.3F, -17F, 0.15F, 0F, 0F));
        PartDefinition p_toothD3R = p_jaw.addOrReplaceChild("toothD3R", CubeListBuilder.create()
                .texOffs(496, 268).addBox(-0.5F, -2F, -1F, 1F, 2F, 2F)
                .texOffs(84, 276).addBox(-0.5F, -3F, -0.8F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(-2.75F, 0.3F, -21F, 0.15F, 0F, 0F));
        PartDefinition p_toothD3L = p_jaw.addOrReplaceChild("toothD3L", CubeListBuilder.create()
                .texOffs(502, 268).addBox(-0.5F, -2F, -1F, 1F, 2F, 2F)
                .texOffs(88, 276).addBox(-0.5F, -3F, -0.8F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(2.75F, 0.3F, -21F, 0.15F, 0F, 0F));
        PartDefinition p_toothD4R = p_jaw.addOrReplaceChild("toothD4R", CubeListBuilder.create()
                .texOffs(0, 276).addBox(-0.5F, -2F, -1F, 1F, 2F, 2F)
                .texOffs(92, 276).addBox(-0.5F, -3F, -0.8F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(-2.4167F, 0.3F, -25F, 0.15F, 0F, 0F));
        PartDefinition p_toothD4L = p_jaw.addOrReplaceChild("toothD4L", CubeListBuilder.create()
                .texOffs(6, 276).addBox(-0.5F, -2F, -1F, 1F, 2F, 2F)
                .texOffs(96, 276).addBox(-0.5F, -3F, -0.8F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(2.4167F, 0.3F, -25F, 0.15F, 0F, 0F));
        PartDefinition p_jawSpikeR0 = p_jaw.addOrReplaceChild("jawSpikeR0", CubeListBuilder.create()
                .texOffs(338, 219).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 10F),
                PartPose.offsetAndRotation(-4F, 2F, -1F, -0.35F, -0.25F, 0F));
        PartDefinition p_jawSpikeR1 = p_jawSpikeR0.addOrReplaceChild("jawSpikeR1", CubeListBuilder.create()
                .texOffs(334, 236).addBox(-1F, -1F, 0F, 2F, 2F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 9.2F, -0.05F, 0F, 0F));
        PartDefinition p_jawSpikeR2 = p_jawSpikeR1.addOrReplaceChild("jawSpikeR2", CubeListBuilder.create()
                .texOffs(456, 259).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 7F),
                PartPose.offsetAndRotation(0F, 0F, 8.2F, -0.05F, 0F, 0F));
        PartDefinition p_jawSpikeL0 = p_jaw.addOrReplaceChild("jawSpikeL0", CubeListBuilder.create()
                .texOffs(364, 219).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 10F),
                PartPose.offsetAndRotation(4F, 2F, -1F, -0.35F, 0.25F, 0F));
        PartDefinition p_jawSpikeL1 = p_jawSpikeL0.addOrReplaceChild("jawSpikeL1", CubeListBuilder.create()
                .texOffs(356, 236).addBox(-1F, -1F, 0F, 2F, 2F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 9.2F, -0.05F, 0F, 0F));
        PartDefinition p_jawSpikeL2 = p_jawSpikeL1.addOrReplaceChild("jawSpikeL2", CubeListBuilder.create()
                .texOffs(472, 259).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 7F),
                PartPose.offsetAndRotation(0F, 0F, 8.2F, -0.05F, 0F, 0F));
        PartDefinition p_armR = p_chest.addOrReplaceChild("armR", CubeListBuilder.create()
                .texOffs(462, 103).addBox(-5.5F, -2F, -5.5F, 11F, 17F, 11F),
                PartPose.offsetAndRotation(-17F, -19F, 6F, -0.95F, -0.25F, -0.2F));
        PartDefinition p_forearmR = p_armR.addOrReplaceChild("forearmR", CubeListBuilder.create()
                .texOffs(280, 169).addBox(-5F, -1.5F, -5F, 10F, 15F, 10F),
                PartPose.offsetAndRotation(0F, 13.5F, 0F, -0.75F, 0F, 0F));
        PartDefinition p_handR = p_forearmR.addOrReplaceChild("handR", CubeListBuilder.create()
                .texOffs(250, 195).addBox(-6.5F, -1.5F, -6F, 13F, 6F, 12F),
                PartPose.offsetAndRotation(0F, 13F, 0F, 1.05F, 0F, 0F));
        PartDefinition p_claw0R = p_handR.addOrReplaceChild("claw0R", CubeListBuilder.create()
                .texOffs(314, 248).addBox(-2F, -0.5F, -2F, 4F, 6F, 4F),
                PartPose.offsetAndRotation(-4.4F, 4.5F, 0F, 0.3F, -0.176F, 0F));
        PartDefinition p_claw20R = p_claw0R.addOrReplaceChild("claw20R", CubeListBuilder.create()
                .texOffs(150, 219).addBox(-1.5F, -0.5F, -1.5F, 3F, 12F, 3F),
                PartPose.offsetAndRotation(0F, 5.5F, 0F, 0.45F, 0F, 0F));
        PartDefinition p_claw1R = p_handR.addOrReplaceChild("claw1R", CubeListBuilder.create()
                .texOffs(330, 248).addBox(-2F, -0.5F, -2F, 4F, 6F, 4F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.3F, 0F, 0F));
        PartDefinition p_claw21R = p_claw1R.addOrReplaceChild("claw21R", CubeListBuilder.create()
                .texOffs(162, 219).addBox(-1.5F, -0.5F, -1.5F, 3F, 12F, 3F),
                PartPose.offsetAndRotation(0F, 5.5F, 0F, 0.45F, 0F, 0F));
        PartDefinition p_claw2R = p_handR.addOrReplaceChild("claw2R", CubeListBuilder.create()
                .texOffs(346, 248).addBox(-2F, -0.5F, -2F, 4F, 6F, 4F),
                PartPose.offsetAndRotation(4.4F, 4.5F, 0F, 0.3F, 0.176F, 0F));
        PartDefinition p_claw22R = p_claw2R.addOrReplaceChild("claw22R", CubeListBuilder.create()
                .texOffs(174, 219).addBox(-1.5F, -0.5F, -1.5F, 3F, 12F, 3F),
                PartPose.offsetAndRotation(0F, 5.5F, 0F, 0.45F, 0F, 0F));
        PartDefinition p_thumbR = p_handR.addOrReplaceChild("thumbR", CubeListBuilder.create()
                .texOffs(486, 219).addBox(-1.5F, -0.5F, -1.5F, 3F, 10F, 3F),
                PartPose.offsetAndRotation(-6.5F, 1.5F, -2.5F, 0.5F, 0F, -0.5F));
        PartDefinition p_armL = p_chest.addOrReplaceChild("armL", CubeListBuilder.create()
                .texOffs(0, 141).addBox(-5.5F, -2F, -5.5F, 11F, 17F, 11F),
                PartPose.offsetAndRotation(17F, -19F, 6F, -0.95F, 0.25F, 0.2F));
        PartDefinition p_forearmL = p_armL.addOrReplaceChild("forearmL", CubeListBuilder.create()
                .texOffs(320, 169).addBox(-5F, -1.5F, -5F, 10F, 15F, 10F),
                PartPose.offsetAndRotation(0F, 13.5F, 0F, -0.75F, 0F, 0F));
        PartDefinition p_handL = p_forearmL.addOrReplaceChild("handL", CubeListBuilder.create()
                .texOffs(300, 195).addBox(-6.5F, -1.5F, -6F, 13F, 6F, 12F),
                PartPose.offsetAndRotation(0F, 13F, 0F, 1.05F, 0F, 0F));
        PartDefinition p_claw0L = p_handL.addOrReplaceChild("claw0L", CubeListBuilder.create()
                .texOffs(362, 248).addBox(-2F, -0.5F, -2F, 4F, 6F, 4F),
                PartPose.offsetAndRotation(-4.4F, 4.5F, 0F, 0.3F, -0.176F, 0F));
        PartDefinition p_claw20L = p_claw0L.addOrReplaceChild("claw20L", CubeListBuilder.create()
                .texOffs(186, 219).addBox(-1.5F, -0.5F, -1.5F, 3F, 12F, 3F),
                PartPose.offsetAndRotation(0F, 5.5F, 0F, 0.45F, 0F, 0F));
        PartDefinition p_claw1L = p_handL.addOrReplaceChild("claw1L", CubeListBuilder.create()
                .texOffs(378, 248).addBox(-2F, -0.5F, -2F, 4F, 6F, 4F),
                PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.3F, 0F, 0F));
        PartDefinition p_claw21L = p_claw1L.addOrReplaceChild("claw21L", CubeListBuilder.create()
                .texOffs(198, 219).addBox(-1.5F, -0.5F, -1.5F, 3F, 12F, 3F),
                PartPose.offsetAndRotation(0F, 5.5F, 0F, 0.45F, 0F, 0F));
        PartDefinition p_claw2L = p_handL.addOrReplaceChild("claw2L", CubeListBuilder.create()
                .texOffs(394, 248).addBox(-2F, -0.5F, -2F, 4F, 6F, 4F),
                PartPose.offsetAndRotation(4.4F, 4.5F, 0F, 0.3F, 0.176F, 0F));
        PartDefinition p_claw22L = p_claw2L.addOrReplaceChild("claw22L", CubeListBuilder.create()
                .texOffs(210, 219).addBox(-1.5F, -0.5F, -1.5F, 3F, 12F, 3F),
                PartPose.offsetAndRotation(0F, 5.5F, 0F, 0.45F, 0F, 0F));
        PartDefinition p_thumbL = p_handL.addOrReplaceChild("thumbL", CubeListBuilder.create()
                .texOffs(498, 219).addBox(-1.5F, -0.5F, -1.5F, 3F, 10F, 3F),
                PartPose.offsetAndRotation(6.5F, 1.5F, -2.5F, 0.5F, 0F, 0.5F));
        PartDefinition p_wingR = p_chest.addOrReplaceChild("wingR", CubeListBuilder.create()
                .texOffs(0, 236).addBox(-18F, -3F, -3F, 18F, 6F, 6F)
                .texOffs(40, 248).addBox(-36.5F, -2.5F, -2.5F, 19F, 5F, 5F)
                .texOffs(100, 0).addBox(-36F, -0.5F, 0F, 36F, 1F, 48F),
                PartPose.offsetAndRotation(-12F, -24F, -9F, -0.3F, 0.65F, 1.4F));
        PartDefinition p_wingOutR = p_wingR.addOrReplaceChild("wingOutR", CubeListBuilder.create()
                .texOffs(0, 54).addBox(-22F, -0.5F, 0F, 22F, 1F, 48F)
                .texOffs(198, 259).addBox(-22F, -2F, -1F, 22F, 4F, 4F)
                .texOffs(14, 268).addBox(-2.5F, -6F, -1F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(-36F, 0F, 0F, 0F, 0.45F, -0.25F));
        PartDefinition p_wingL = p_chest.addOrReplaceChild("wingL", CubeListBuilder.create()
                .texOffs(48, 236).addBox(0F, -3F, -3F, 18F, 6F, 6F)
                .texOffs(88, 248).addBox(17.5F, -2.5F, -2.5F, 19F, 5F, 5F)
                .texOffs(268, 0).addBox(0F, -0.5F, 0F, 36F, 1F, 48F),
                PartPose.offsetAndRotation(12F, -24F, -9F, -0.3F, -0.65F, -1.4F));
        PartDefinition p_wingOutL = p_wingL.addOrReplaceChild("wingOutL", CubeListBuilder.create()
                .texOffs(140, 54).addBox(0F, -0.5F, 0F, 22F, 1F, 48F)
                .texOffs(250, 259).addBox(0F, -2F, -1F, 22F, 4F, 4F)
                .texOffs(26, 268).addBox(-0.5F, -6F, -1F, 3F, 5F, 3F),
                PartPose.offsetAndRotation(36F, 0F, 0F, 0F, -0.45F, 0.25F));
        PartDefinition p_tail1 = p_pelvis.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(398, 103).addBox(-9F, -7F, -0.5F, 18F, 14F, 14F)
                .texOffs(142, 259).addBox(-1F, -10F, 4.5F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -0.5F, 10F, -0.05F, -0.2F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(222, 169).addBox(-8F, -6F, -0.5F, 16F, 12F, 13F)
                .texOffs(156, 259).addBox(-1F, -9F, 4F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 13F, 0.2F, -0.14F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(0, 195).addBox(-7F, -5.5F, -0.5F, 14F, 11F, 13F)
                .texOffs(170, 259).addBox(-1F, -8.5F, 4F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 12F, 0.32F, -0.14F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(54, 195).addBox(-6F, -5F, -0.5F, 12F, 10F, 12F)
                .texOffs(184, 259).addBox(-1F, -8F, 3.5F, 2F, 4F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 12F, 0.42F, -0.14F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(102, 195).addBox(-5F, -4F, -0.5F, 10F, 8F, 12F)
                .texOffs(488, 259).addBox(-1F, -6.5F, 3.5F, 2F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.48F, -0.14F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(390, 195).addBox(-4F, -3F, -0.5F, 8F, 6F, 11F)
                .texOffs(0, 268).addBox(-1F, -5.5F, 3F, 2F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.5F, -0.14F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(258, 219).addBox(-2.5F, -2F, -0.5F, 5F, 4F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.45F, -0.14F, 0F));
        PartDefinition p_tailtip = p_tail7.addOrReplaceChild("tailtip", CubeListBuilder.create()
                .texOffs(154, 236).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.25F, 0F, 0F));
        PartDefinition p_thighR = p_pelvis.addOrReplaceChild("thighR", CubeListBuilder.create()
                .texOffs(76, 103).addBox(-7F, -1.5F, -7.5F, 14F, 16F, 15F),
                PartPose.offsetAndRotation(-13F, 3F, 0F, -1.05F, 0F, 0F));
        PartDefinition p_shinR = p_thighR.addOrReplaceChild("shinR", CubeListBuilder.create()
                .texOffs(0, 169).addBox(-5.5F, -1F, -5.5F, 11F, 15F, 11F),
                PartPose.offsetAndRotation(0F, 13F, -0.5F, 1.75F, 0F, 0F));
        PartDefinition p_footR = p_shinR.addOrReplaceChild("footR", CubeListBuilder.create()
                .texOffs(146, 195).addBox(-6.5F, -1F, -9.5F, 13F, 5F, 13F),
                PartPose.offsetAndRotation(0F, 13.5F, 0F, -0.7F, 0F, 0F));
        PartDefinition p_toe0R = p_footR.addOrReplaceChild("toe0R", CubeListBuilder.create()
                .texOffs(378, 236).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F),
                PartPose.offsetAndRotation(-4F, 2F, -7.8F, 0.45F, -0.2F, 0F));
        PartDefinition p_toe1R = p_footR.addOrReplaceChild("toe1R", CubeListBuilder.create()
                .texOffs(400, 236).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F),
                PartPose.offsetAndRotation(0F, 2F, -7.8F, 0.45F, 0F, 0F));
        PartDefinition p_toe2R = p_footR.addOrReplaceChild("toe2R", CubeListBuilder.create()
                .texOffs(422, 236).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F),
                PartPose.offsetAndRotation(4F, 2F, -7.8F, 0.45F, 0.2F, 0F));
        PartDefinition p_heelR = p_footR.addOrReplaceChild("heelR", CubeListBuilder.create()
                .texOffs(186, 268).addBox(-1F, -1F, 0F, 2F, 2F, 5F),
                PartPose.offsetAndRotation(0F, 1.5F, 2.5F, 0.5F, 0F, 0F));
        PartDefinition p_thighL = p_pelvis.addOrReplaceChild("thighL", CubeListBuilder.create()
                .texOffs(134, 103).addBox(-7F, -1.5F, -7.5F, 14F, 16F, 15F),
                PartPose.offsetAndRotation(13F, 3F, 0F, -1.05F, 0F, 0F));
        PartDefinition p_shinL = p_thighL.addOrReplaceChild("shinL", CubeListBuilder.create()
                .texOffs(44, 169).addBox(-5.5F, -1F, -5.5F, 11F, 15F, 11F),
                PartPose.offsetAndRotation(0F, 13F, -0.5F, 1.75F, 0F, 0F));
        PartDefinition p_footL = p_shinL.addOrReplaceChild("footL", CubeListBuilder.create()
                .texOffs(198, 195).addBox(-6.5F, -1F, -9.5F, 13F, 5F, 13F),
                PartPose.offsetAndRotation(0F, 13.5F, 0F, -0.7F, 0F, 0F));
        PartDefinition p_toe0L = p_footL.addOrReplaceChild("toe0L", CubeListBuilder.create()
                .texOffs(444, 236).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F),
                PartPose.offsetAndRotation(-4F, 2F, -7.8F, 0.45F, -0.2F, 0F));
        PartDefinition p_toe1L = p_footL.addOrReplaceChild("toe1L", CubeListBuilder.create()
                .texOffs(466, 236).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F),
                PartPose.offsetAndRotation(0F, 2F, -7.8F, 0.45F, 0F, 0F));
        PartDefinition p_toe2L = p_footL.addOrReplaceChild("toe2L", CubeListBuilder.create()
                .texOffs(488, 236).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F),
                PartPose.offsetAndRotation(4F, 2F, -7.8F, 0.45F, 0.2F, 0F));
        PartDefinition p_heelL = p_footL.addOrReplaceChild("heelL", CubeListBuilder.create()
                .texOffs(200, 268).addBox(-1F, -1F, 0F, 2F, 2F, 5F),
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
        m.get("head").xScale = 1.75F;
        m.get("head").yScale = 1.75F;
        m.get("head").zScale = 1.75F;
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
        m.put("beak", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("beak"));
        m.put("visor0", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("visor0"));
        m.put("visor1", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("visor0").getChild("visor1"));
        m.put("visor2", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("visor0").getChild("visor1").getChild("visor2"));
        m.put("visor3", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("visor0").getChild("visor1").getChild("visor2").getChild("visor3"));
        m.put("visor4", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("visor0").getChild("visor1").getChild("visor2").getChild("visor3").getChild("visor4"));
        m.put("visor5", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("visor0").getChild("visor1").getChild("visor2").getChild("visor3").getChild("visor4").getChild("visor5"));
        m.put("visor6", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("visor0").getChild("visor1").getChild("visor2").getChild("visor3").getChild("visor4").getChild("visor5").getChild("visor6"));
        m.put("visor7", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("visor0").getChild("visor1").getChild("visor2").getChild("visor3").getChild("visor4").getChild("visor5").getChild("visor6").getChild("visor7"));
        m.put("visor8", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("visor0").getChild("visor1").getChild("visor2").getChild("visor3").getChild("visor4").getChild("visor5").getChild("visor6").getChild("visor7").getChild("visor8"));
        m.put("eyeR", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("eyeR"));
        m.get("eyeR").xScale = 0.62F;
        m.get("eyeR").yScale = 0.62F;
        m.get("eyeR").zScale = 0.62F;
        m.put("earSpikeR0", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("earSpikeR0"));
        m.put("earSpikeR1", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("earSpikeR0").getChild("earSpikeR1"));
        m.put("earSpikeR2", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("earSpikeR0").getChild("earSpikeR1").getChild("earSpikeR2"));
        m.put("eyeL", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("eyeL"));
        m.get("eyeL").xScale = 0.62F;
        m.get("eyeL").yScale = 0.62F;
        m.get("eyeL").zScale = 0.62F;
        m.put("earSpikeL0", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("earSpikeL0"));
        m.put("earSpikeL1", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("earSpikeL0").getChild("earSpikeL1"));
        m.put("earSpikeL2", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("earSpikeL0").getChild("earSpikeL1").getChild("earSpikeL2"));
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
        m.put("jawSpikeR0", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSpikeR0"));
        m.put("jawSpikeR1", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSpikeR0").getChild("jawSpikeR1"));
        m.put("jawSpikeR2", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSpikeR0").getChild("jawSpikeR1").getChild("jawSpikeR2"));
        m.put("jawSpikeL0", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSpikeL0"));
        m.put("jawSpikeL1", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSpikeL0").getChild("jawSpikeL1"));
        m.put("jawSpikeL2", root.getChild("pelvis").getChild("chest").getChild("neck1").getChild("neck2").getChild("neck3").getChild("neck4").getChild("neck5").getChild("neck6").getChild("neck7").getChild("head").getChild("jaw").getChild("jawSpikeL0").getChild("jawSpikeL1").getChild("jawSpikeL2"));
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
        m.get("wingR").xScale = 1.35F;
        m.get("wingR").yScale = 1.35F;
        m.get("wingR").zScale = 1.35F;
        m.put("wingOutR", root.getChild("pelvis").getChild("chest").getChild("wingR").getChild("wingOutR"));
        m.put("wingL", root.getChild("pelvis").getChild("chest").getChild("wingL"));
        m.get("wingL").xScale = 1.35F;
        m.get("wingL").yScale = 1.35F;
        m.get("wingL").zScale = 1.35F;
        m.put("wingOutL", root.getChild("pelvis").getChild("chest").getChild("wingL").getChild("wingOutL"));
        return m;
    }
}
