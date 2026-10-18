package com.keinloree.client;

import com.keinloree.KeinLooreMod;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/** Droide soldado de Kein Loree: cuerpo verde oscuro, visor brillante y blaster en el brazo derecho. */
public class DroidModel<T extends Mob> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "droid"), "main");

    private static final float DEG = (float) (Math.PI / 180.0D);

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public DroidModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    /** Geometria generada por gen_models.py: no editar a mano (regenerar el script). */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition v_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_head = v_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                .texOffs(22, 32).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 4.0F, 2.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_right_arm = v_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                .texOffs(0, 32).addBox(-2.0F, 4.0F, -9.0F, 3.0F, 4.0F, 8.0F)
                , PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition v_left_arm = v_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                , PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition v_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(32, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                , PartPose.offset(-2.0F, 12.0F, 0.0F));
        PartDefinition v_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(48, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                , PartPose.offset(2.0F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * DEG;
        this.head.xRot = headPitch * DEG;

        float swing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = swing;
        this.leftLeg.xRot = -swing;
        this.leftArm.xRot = swing * 0.6F;
        this.leftArm.zRot = 0.0F;
        this.body.y = Mth.sin(ageInTicks * 0.1F) * 0.15F;

        if (entity.getTarget() != null) {
            // Blaster en posicion de tiro, con una leve vibracion de carga
            this.rightArm.xRot = -1.45F + Mth.sin(ageInTicks * 0.4F) * 0.03F;
            this.rightArm.zRot = 0.0F;
        } else {
            this.rightArm.xRot = -swing * 0.6F;
            this.rightArm.zRot = 0.0F;
        }
    }
}
