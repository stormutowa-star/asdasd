package com.keinloree.client;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.CargoShip;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renderer de la nave de carga (el modelo esta hecho a escala 1/8 de bloque por unidad, de ahi el factor 2). */
public class CargoShipRenderer extends MobRenderer<CargoShip, CargoShipModel<CargoShip>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "textures/entity/cargo_ship.png");
    private static final ResourceLocation GLOW =
            ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "textures/entity/cargo_ship_glow.png");

    public CargoShipRenderer(EntityRendererProvider.Context context) {
        super(context, new CargoShipModel<CargoShip>(context.bakeLayer(CargoShipModel.LAYER)), 3.2F);
        this.addLayer(new GlowLayer<CargoShip, CargoShipModel<CargoShip>>(this, e -> GLOW));
    }

    @Override
    public ResourceLocation getTextureLocation(CargoShip entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(CargoShip entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
    }
}
