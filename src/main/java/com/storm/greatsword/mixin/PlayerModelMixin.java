package com.storm.greatsword.mixin;

import com.storm.greatsword.GreatswordItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Poses both player arms around the grip of the drawn greatsword. This runs after vanilla has set up its
 * arm poses, so it can properly override the one-handed item pose.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void greatsword$poseTwoHanded(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                          float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GreatswordItem)) {
            return;
        }

        PlayerModel<?> model = (PlayerModel<?>) (Object) this;
        boolean rightHanded = player.getMainArm() == HumanoidArm.RIGHT;

        if (!GreatswordItem.isDrawn(stack)) {
            // The sword is on the back. Relax the hand that would otherwise be holding it.
            ModelPart mainArm = rightHanded ? model.rightArm : model.leftArm;
            mainArm.xRot = 0.0F;
            mainArm.yRot = 0.0F;
            mainArm.zRot = 0.0F;

            model.rightSleeve.copyFrom(model.rightArm);
            model.leftSleeve.copyFrom(model.leftArm);
            return;
        }

        ModelPart mainArm = rightHanded ? model.rightArm : model.leftArm;
        ModelPart offArm = rightHanded ? model.leftArm : model.rightArm;

        if (player.isUsingItem()) {
            // Charged slam stance: both arms raised above the head.
            mainArm.xRot = -2.55F;
            mainArm.yRot = rightHanded ? -0.10F : 0.10F;
            mainArm.zRot = rightHanded ? 0.12F : -0.12F;

            offArm.xRot = -2.35F;
            offArm.yRot = rightHanded ? 0.20F : -0.20F;
            offArm.zRot = rightHanded ? -0.20F : 0.20F;
        } else {
            float swing = player.getAttackAnim(0.0F);

            if (swing > 0.0F) {
                // Vanilla swings the main arm. Make the offhand follow the same motion so the grip
                // still reads as two-handed during combos.
                offArm.xRot = mainArm.xRot * 0.9F - 0.15F;
                offArm.yRot = mainArm.yRot * 0.6F + (rightHanded ? 0.25F : -0.25F);
                offArm.zRot = mainArm.zRot * 0.6F + (rightHanded ? -0.15F : 0.15F);
            } else {
                // Idle guard: the main hand holds low, the offhand reaches across to the upper grip.
                mainArm.xRot = -0.62F;
                mainArm.yRot = rightHanded ? -0.05F : 0.05F;
                mainArm.zRot = rightHanded ? 0.10F : -0.10F;

                offArm.xRot = -0.88F;
                offArm.yRot = rightHanded ? 0.10F : -0.10F;
                offArm.zRot = rightHanded ? -0.30F : 0.30F;
            }
        }

        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
    }
}
