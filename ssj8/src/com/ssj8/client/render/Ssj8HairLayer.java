package com.ssj8.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ssj8.client.ClientState;
import com.ssj8.client.render.HairModel;
import com.ssj8.client.render.ModRenderTypes;
import com.ssj8.client.render.Prism;
import com.ssj8.client.render.Sinks;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class Ssj8HairLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public Ssj8HairLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ClientState cs = ClientState.get(player);
        if (cs == null || !cs.form || player.isInvisible()) {
            return;
        }
        updateWind(player, cs, partialTick);
        float grow = cs.growth(partialTick);
        float light = (float) Math.max(LightTexture.block(packedLight), LightTexture.sky(packedLight)) / 15.0f;
        PlayerModel<AbstractClientPlayer> model = this.getParentModel();
        VertexConsumer vc = buffer.getBuffer(ModRenderTypes.SOLID);
        float k = 0.6f + 0.4f * light;

        // Dorado y plateado comparten el mismo transform de cabeza: si el plateado tuviera una
        // rotacion extra, se separaria del dorado al mirar hacia abajo.
        poseStack.pushPose();
        model.head.translateAndRotate(poseStack);
        HairModel.drawHair(Sinks.colorScaled(vc, poseStack.last().pose(), k), ageInTicks, cs.windR, cs.windU, cs.windB, grow, false);
        poseStack.popPose();

        poseStack.pushPose();
        model.head.translateAndRotate(poseStack);
        HairModel.drawHair(Sinks.colorScaled(vc, poseStack.last().pose(), k), ageInTicks, cs.windR, cs.windU, cs.windB, grow, true);
        poseStack.popPose();

        poseStack.pushPose();
        model.body.translateAndRotate(poseStack);
        HairModel.drawTail(Sinks.colorScaled(vc, poseStack.last().pose(), k), ageInTicks, cs.windR, cs.windU, cs.windB, grow);
        poseStack.popPose();
    }

    private static void updateWind(AbstractClientPlayer p, ClientState cs, float partial) {
        double vx = p.getX() - p.xo;
        double vy = p.getY() - p.yo;
        double vz = p.getZ() - p.zo;
        float yaw = Mth.rotLerp(partial, p.yBodyRotO, p.yBodyRot) * ((float) Math.PI / 180);
        double fx = -Mth.sin(yaw);
        double fz = Mth.cos(yaw);
        double rx = -Mth.cos(yaw);
        double rz = -Mth.sin(yaw);
        float k = 3.2f;
        float tb = Mth.clamp((float) (vx * fx + vz * fz) * k, -1.2f, 1.2f);
        float tr = Mth.clamp((float) (-(vx * rx + vz * rz)) * k, -1.0f, 1.0f);
        float tu = Mth.clamp((float) (-vy) * k * 0.8f, -1.0f, 1.0f);
        cs.windB += (tb - cs.windB) * 0.12f;
        cs.windR += (tr - cs.windR) * 0.12f;
        cs.windU += (tu - cs.windU) * 0.12f;
    }
}
