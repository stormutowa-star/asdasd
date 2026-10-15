package com.ricardo.transformacion.client;

import com.ricardo.transformacion.TransformacionMod;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Modelos de la transformacion, hechos con cajas de Minecraft.
 * Las coordenadas van en pixeles de modelo (16 = 1 bloque) con el origen en la articulacion de cada parte.
 * Las texturas de src/main/resources/assets/transformacion/textures/entity salen de las imagenes de referencia.
 */
public final class TransformModels {
    public static final ModelLayerLocation HEAD = layer("head");
    public static final ModelLayerLocation WINGS = layer("wings");
    public static final ModelLayerLocation GAUNTLET_WIDE = layer("gauntlet_wide");
    public static final ModelLayerLocation GAUNTLET_SLIM = layer("gauntlet_slim");

    private TransformModels() {
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TransformacionMod.MODID, name), "main");
    }

    /** Cabeza: rostro que cubre la piel, flequillo, melena detras y pinchos. Origen en el cuello. */
    public static LayerDefinition createHead() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // El rostro va un poco mas grande que la cabeza para tapar la piel y el sombrero.
        root.addOrReplaceChild("face", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F)), PartPose.ZERO);
        // Pelo de la coronilla y flequillo, justo sobre la frente.
        root.addOrReplaceChild("hair_top", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(1.0F)), PartPose.ZERO);
        // Melena que cae detras de la cabeza.
        root.addOrReplaceChild("hair_back", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-5.0F, -9.0F, 3.0F, 10.0F, 18.0F, 2.0F), PartPose.ZERO);
        // Pinchos sobre la cabeza.
        root.addOrReplaceChild("spike_left", spike(), PartPose.offsetAndRotation(-2.5F, -8.5F, 0.0F, 0.0F, 0.0F, 0.35F));
        root.addOrReplaceChild("spike_center", spike(), PartPose.offsetAndRotation(0.0F, -9.0F, -0.5F, -0.2F, 0.0F, 0.0F));
        root.addOrReplaceChild("spike_right", spike(), PartPose.offsetAndRotation(2.5F, -8.5F, 0.0F, 0.0F, 0.0F, -0.35F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static CubeListBuilder spike() {
        return CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F);
    }

    /** Alas: dos planos detras de la espalda, con un leve aleteo. Origen entre los hombros. */
    public static LayerDefinition createWings() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0.0F, -4.0F, 0.0F, 14.0F, 18.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.35F, 0.0F));
        root.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(0, 20)
                .addBox(-14.0F, -4.0F, 0.0F, 14.0F, 18.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, 0.35F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * Guantelete del brazo con garras en la punta de la mano.
     * Mismas medidas que la caja del brazo (ancho 4, o 3 en el modelo delgado).
     */
    public static LayerDefinition createGauntlet(boolean slim) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        float width = slim ? 3.0F : 4.0F;
        float x = slim ? -2.0F : -3.0F;
        int u = slim ? 16 : 0;
        root.addOrReplaceChild("gauntlet", CubeListBuilder.create().texOffs(u, 0)
                .addBox(x, -2.0F, -2.0F, width, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.ZERO);
        for (int i = 0; i < 3; i++) {
            float clawX = x + i * (width - 1.0F) / 2.0F;
            root.addOrReplaceChild("claw_" + i, CubeListBuilder.create().texOffs(32, 0)
                    .addBox(clawX, 10.0F, -1.0F, 1.0F, 5.0F, 1.0F), PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 64, 64);
    }
}
