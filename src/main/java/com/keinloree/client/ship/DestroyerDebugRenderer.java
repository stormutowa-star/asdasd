package com.keinloree.client.ship;

import com.keinloree.ship.DestroyerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * Renderer provisional del destructor: no dibuja nada (la malla llega con el WP6). Solo evita que el
 * cliente falle al ver la entidad. La caja de depuracion vanilla (F3+B) sigue funcionando.
 */
public class DestroyerDebugRenderer extends EntityRenderer<DestroyerEntity> {

    public DestroyerDebugRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(DestroyerEntity entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        // WP6: malla del diseno (ShipMesh) segun entity.getState()
    }

    @Override
    public ResourceLocation getTextureLocation(DestroyerEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
