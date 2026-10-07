package com.ricardo.blueeyes.client;

import java.util.Map;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.ricardo.blueeyes.BlueEyesDragon;
import com.ricardo.blueeyes.BlueEyesMod;

/** Modelo del Dragon Blanco de Ojos Azules. La geometria viene de BlueEyesLayer (generado); aqui solo se anima. */
public class BlueEyesModel extends HierarchicalModel<BlueEyesDragon> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(BlueEyesMod.MODID, "blue_eyes_white_dragon"), "main");

    private final ModelPart modelRoot;
    private final Map<String, ModelPart> p;
    private float charge;
    private float fire;

    public BlueEyesModel(ModelPart modelRoot) {
        this.modelRoot = modelRoot;
        this.p = BlueEyesLayer.collect(modelRoot);
    }

    @Override
    public ModelPart root() {
        return this.modelRoot;
    }

    @Override
    public void prepareMobModel(BlueEyesDragon dragon, float limbSwing, float limbSwingAmount, float partialTick) {
        int c = dragon.getCharge();
        this.charge = c <= 0 ? 0.0F : Mth.clamp((c + partialTick) / BlueEyesDragon.CHARGE_TIME, 0.0F, 1.0F);
        int f = dragon.getFireTicks();
        this.fire = f <= 0 ? 0.0F : Mth.clamp((f - partialTick) / BlueEyesDragon.FIRE_TIME, 0.0F, 1.0F);
    }

    @Override
    public void setupAnim(BlueEyesDragon dragon, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float t = ageInTicks;
        float c = this.charge;
        float f = this.fire;

        // flotar
        p.get("root").y += Mth.sin(t * 0.09F) * 1.6F;
        p.get("chest").xRot += Mth.sin(t * 0.09F + 0.6F) * 0.03F - c * 0.12F;

        // aleteo (las alas se abren mas al cargar el ataque)
        float flap = Mth.sin(t * 0.21F);
        float flapOut = Mth.sin(t * 0.21F - 0.8F);
        float open = c * 0.25F + f * 0.15F;
        p.get("wingR").zRot += flap * 0.32F + open;
        p.get("wingL").zRot -= flap * 0.32F + open;
        p.get("wingR").xRot += flap * 0.10F;
        p.get("wingL").xRot += flap * 0.10F;
        p.get("wingOutR").zRot += flapOut * 0.30F;
        p.get("wingOutL").zRot -= flapOut * 0.30F;

        // cuello en S que respira
        for (int i = 1; i <= 4; i++) {
            ModelPart n = p.get("neck" + i);
            n.xRot += Mth.sin(t * 0.07F + i * 0.5F) * 0.025F;
            n.yRot += Mth.sin(t * 0.05F + i * 0.4F) * 0.03F + netHeadYaw * Mth.DEG_TO_RAD * 0.08F;
        }
        // al cargar, echa la cabeza hacia atras; al disparar, la lanza hacia delante
        p.get("neck3").xRot -= c * 0.20F;
        p.get("neck4").xRot -= c * 0.20F - f * 0.25F;

        ModelPart head = p.get("head");
        head.yRot += Mth.clamp(netHeadYaw, -40.0F, 40.0F) * Mth.DEG_TO_RAD * 0.5F;
        head.xRot += Mth.clamp(headPitch, -35.0F, 40.0F) * Mth.DEG_TO_RAD * 0.7F + c * 0.15F - f * 0.15F;

        // fauces: respiracion, carga (se abre) y disparo (abiertas del todo)
        float jaw = 0.06F + Mth.sin(t * 0.1F) * 0.04F;
        jaw = Math.max(jaw, c * 0.55F);
        jaw = Math.max(jaw, f * 0.75F);
        p.get("jaw").xRot += jaw;

        // cola
        for (int i = 1; i <= 7; i++) {
            ModelPart s = p.get("tail" + i);
            s.yRot += Mth.sin(t * 0.08F - i * 0.55F) * 0.10F;
            s.xRot += Mth.sin(t * 0.06F - i * 0.4F) * 0.03F;
        }

        // brazos y garras
        float arm = Mth.sin(t * 0.09F) * 0.06F;
        p.get("armR").xRot += arm - c * 0.25F;
        p.get("armL").xRot += arm - c * 0.25F;
        p.get("forearmR").xRot -= arm * 0.5F;
        p.get("forearmL").xRot -= arm * 0.5F;

        // piernas colgando mientras vuela
        float leg = Mth.sin(t * 0.09F - 0.5F) * 0.05F;
        float speed = Math.min(1.0F, limbSwingAmount * 2.0F);
        p.get("thighR").xRot += leg + speed * 0.35F;
        p.get("thighL").xRot += leg + speed * 0.35F;
        p.get("shinR").xRot += speed * 0.25F;
        p.get("shinL").xRot += speed * 0.25F;
    }
}
