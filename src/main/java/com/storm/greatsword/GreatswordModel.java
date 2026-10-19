package com.storm.greatsword;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GreatswordModel extends GeoModel<GreatswordItem> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(GreatswordMod.MODID, "geo/greatsword.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GreatswordMod.MODID, "textures/item/greatsword.png");
    private static final ResourceLocation ANIMATIONS =
            ResourceLocation.fromNamespaceAndPath(GreatswordMod.MODID, "animations/greatsword.animation.json");

    @Override
    public ResourceLocation getModelResource(GreatswordItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(GreatswordItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(GreatswordItem animatable) {
        return ANIMATIONS;
    }
}
