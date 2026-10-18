package com.keinloree.client;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.Droid;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renderer del droide verde oscuro con visor y blaster brillantes. */
public class DroidRenderer extends MobRenderer<Droid, DroidModel<Droid>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "textures/entity/droid.png");
    private static final ResourceLocation GLOW =
            ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "textures/entity/droid_glow.png");

    public DroidRenderer(EntityRendererProvider.Context context) {
        super(context, new DroidModel<Droid>(context.bakeLayer(DroidModel.LAYER)), 0.35F);
        this.addLayer(new GlowLayer<Droid, DroidModel<Droid>>(this, e -> GLOW));
    }

    @Override
    public ResourceLocation getTextureLocation(Droid entity) {
        return TEXTURE;
    }
}
