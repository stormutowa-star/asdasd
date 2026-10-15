package com.ricardo.transformacion.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** Modelos a escala Minecraft (1 pixel de textura = 1/16 de bloque, igual que la skin del jugador). */
public final class TransformModels {
    public static final ModelLayerLocation HEAD = layer("head");
    public static final ModelLayerLocation BODY = layer("body");
    public static final ModelLayerLocation WINGS = layer("wings");
    public static final ModelLayerLocation GAUNTLET_RIGHT_WIDE = layer("gauntlet_right_wide");
    public static final ModelLayerLocation GAUNTLET_RIGHT_SLIM = layer("gauntlet_right_slim");
    public static final ModelLayerLocation GAUNTLET_LEFT_WIDE = layer("gauntlet_left_wide");
    public static final ModelLayerLocation GAUNTLET_LEFT_SLIM = layer("gauntlet_left_slim");

    private TransformModels() {
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("transformacion", name), "main");
    }

    public static ModelLayerLocation gauntlet(boolean left, boolean slim) {
        if (left) {
            return slim ? GAUNTLET_LEFT_SLIM : GAUNTLET_LEFT_WIDE;
        }
        return slim ? GAUNTLET_RIGHT_SLIM : GAUNTLET_RIGHT_WIDE;
    }

    // ------------------------------------------------------------------ cabeza + pelo (textura 64x64)
    public static LayerDefinition createHead() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("face", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.ZERO);
        root.addOrReplaceChild("hair_cap", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.6F)), PartPose.ZERO);

        // mechones laterales que enmarcan la cara
        root.addOrReplaceChild("lock_right", CubeListBuilder.create().texOffs(24, 16)
                .addBox(-0.5F, 0.0F, -1.0F, 1.0F, 9.0F, 2.0F), PartPose.offset(-5.0F, -6.0F, -3.4F));
        root.addOrReplaceChild("lock_left", CubeListBuilder.create().texOffs(24, 16).mirror()
                .addBox(-0.5F, 0.0F, -1.0F, 1.0F, 9.0F, 2.0F), PartPose.offset(5.0F, -6.0F, -3.4F));

        // pinchos del pelo barridos hacia atras
        tuft(root, "tuft_0", 0.0F, -8.5F, -1.2F, -1.0F, 0.0F);
        tuft(root, "tuft_1", -2.4F, -8.4F, 0.4F, -0.85F, 0.45F);
        tuft(root, "tuft_2", 2.4F, -8.4F, 0.4F, -0.85F, -0.45F);
        tuft(root, "tuft_3", -3.5F, -7.8F, 2.0F, -0.65F, 0.75F);
        tuft(root, "tuft_4", 3.5F, -7.8F, 2.0F, -0.65F, -0.75F);

        // pelo largo en dos tramos (el segundo cuelga del primero y se balancea)
        PartDefinition back = root.addOrReplaceChild("hair_back", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-5.0F, 0.0F, 0.0F, 10.0F, 12.0F, 2.0F), PartPose.offset(0.0F, -7.0F, 4.5F));
        back.addOrReplaceChild("hair_back_2", CubeListBuilder.create().texOffs(0, 32)
                .addBox(-4.5F, 0.0F, 0.0F, 9.0F, 12.0F, 2.0F), PartPose.offset(0.0F, 11.5F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void tuft(PartDefinition root, String name, float x, float y, float z, float xRot, float zRot) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(48, 16)
                .addBox(-1.0F, -4.5F, -1.0F, 2.0F, 4.5F, 2.0F), PartPose.offsetAndRotation(x, y, z, xRot, 0.0F, zRot));
    }

    // ------------------------------------------------------------------ cuerpo: corpino, cuello y falda (textura 128x128)
    public static LayerDefinition createBody() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("bodice", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.ZERO);
        root.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(0, 20)
                .addBox(-4.0F, 0.9F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.6F)), PartPose.ZERO);

        // pinchos de los hombros
        shoulderSpike(root, "shoulder_right", -4.9F, 0.2F, -0.8F);
        shoulderSpike(root, "shoulder_left", 4.9F, 0.2F, 0.8F);

        // falda: cuatro paneles articulados en la cintura
        root.addOrReplaceChild("skirt_front", CubeListBuilder.create().texOffs(0, 30)
                .addBox(-5.0F, 0.0F, -0.5F, 10.0F, 14.0F, 1.0F), PartPose.offset(0.0F, 10.0F, -2.6F));
        root.addOrReplaceChild("skirt_back", CubeListBuilder.create().texOffs(0, 48)
                .addBox(-6.0F, 0.0F, -0.5F, 12.0F, 15.0F, 1.0F), PartPose.offset(0.0F, 10.0F, 2.6F));
        root.addOrReplaceChild("skirt_right", CubeListBuilder.create().texOffs(0, 66)
                .addBox(-0.5F, 0.0F, -3.5F, 1.0F, 14.0F, 7.0F), PartPose.offset(-4.8F, 10.0F, 0.0F));
        root.addOrReplaceChild("skirt_left", CubeListBuilder.create().texOffs(0, 66)
                .addBox(-0.5F, 0.0F, -3.5F, 1.0F, 14.0F, 7.0F), PartPose.offset(4.8F, 10.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void shoulderSpike(PartDefinition root, String name, float x, float y, float zRot) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(0, 20)
                .addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(x, y, 0.0F, -0.15F, 0.0F, zRot));
    }

    // ------------------------------------------------------------------ alas (textura 128x128)
    public static LayerDefinition createWings() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ala superior (29 x 32) y ala inferior (22 x 25) a cada lado; bisagra a la espalda
        root.addOrReplaceChild("wing_right_up", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-29.0F, -22.0F, 0.0F, 29.0F, 32.0F, 1.0F), PartPose.offset(-1.5F, 2.5F, 2.8F));
        root.addOrReplaceChild("wing_left_up", CubeListBuilder.create().texOffs(0, 36)
                .addBox(0.0F, -22.0F, 0.0F, 29.0F, 32.0F, 1.0F), PartPose.offset(1.5F, 2.5F, 2.8F));
        root.addOrReplaceChild("wing_right_low", CubeListBuilder.create().texOffs(0, 72)
                .addBox(-22.0F, -3.0F, 0.0F, 22.0F, 25.0F, 1.0F), PartPose.offset(-1.5F, 2.5F, 3.7F));
        root.addOrReplaceChild("wing_left_low", CubeListBuilder.create().texOffs(48, 72)
                .addBox(0.0F, -3.0F, 0.0F, 22.0F, 25.0F, 1.0F), PartPose.offset(1.5F, 2.5F, 3.7F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    // ------------------------------------------------------------------ guanteletes con pinchos (textura 64x32)
    public static LayerDefinition createGauntlet(boolean left, boolean slim) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // centro del brazo en X dentro del ModelPart del brazo
        float cx = (slim ? 0.5F : 1.0F) * (left ? 1.0F : -1.0F);
        float out = left ? 1.0F : -1.0F; // lado exterior del brazo

        root.addOrReplaceChild("cuff", CubeListBuilder.create().texOffs(0, 0)
                .addBox(cx - 2.0F, 2.5F, -2.0F, 4.0F, 7.5F, 4.0F, new CubeDeformation(0.6F)), PartPose.ZERO);

        // pincho grande cerca del codo, curvado hacia afuera y atras
        PartDefinition big = root.addOrReplaceChild("spike_big", CubeListBuilder.create().texOffs(16, 0)
                .addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(cx + out * 2.4F, 4.2F, 1.0F, -0.45F, 0.0F, out * 0.45F));
        big.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(16, 8)
                .addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offsetAndRotation(0.0F, -2.8F, 0.0F, -0.3F, 0.0F, out * 0.3F));
        // pincho mediano
        PartDefinition mid = root.addOrReplaceChild("spike_mid", CubeListBuilder.create().texOffs(16, 0)
                .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(cx + out * 2.4F, 7.4F, 0.6F, -0.35F, 0.0F, out * 0.4F));
        mid.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(16, 8)
                .addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -1.8F, 0.0F, -0.25F, 0.0F, out * 0.25F));
        // pincho de nudillo
        root.addOrReplaceChild("knuckle", CubeListBuilder.create().texOffs(16, 8)
                .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(cx, 9.8F, -2.5F, -0.5F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }
}
