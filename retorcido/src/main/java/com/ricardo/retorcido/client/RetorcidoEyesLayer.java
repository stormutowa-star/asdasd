package com.ricardo.retorcido.client;

import com.ricardo.retorcido.RetorcidoEntity;
import com.ricardo.retorcido.RetorcidoMod;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Capa emisiva: dibuja retorcido_eyes.png con RenderType.eyes (brilla en la oscuridad). */
public class RetorcidoEyesLayer extends EyesLayer<RetorcidoEntity, RetorcidoModel> {
    private static final RenderType EYES = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(RetorcidoMod.MODID, "textures/entity/retorcido_eyes.png"));

    public RetorcidoEyesLayer(RenderLayerParent<RetorcidoEntity, RetorcidoModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
