package com.ssj8.client.render;

import com.ssj8.client.ClientState;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

public class Ssj8PlayerModel extends PlayerModel<AbstractClientPlayer> {
    private static final float DEG_TO_RAD = (float) Math.PI / 180.0f;

    // Angulos de la animacion base.ki_charge de DragonMine Z
    // (src/main/resources/assets/dragonminez/animations/entity/races/movement.animation.json).
    private static final float CHARGE_BODY_X = 5.0f * DEG_TO_RAD;
    private static final float CHARGE_BODY_Y = -0.5f * DEG_TO_RAD;
    private static final float CHARGE_RIGHT_ARM_X = -43.46671f * DEG_TO_RAD;
    private static final float CHARGE_RIGHT_ARM_Y = 27.32892f * DEG_TO_RAD;
    private static final float CHARGE_RIGHT_ARM_Z = -5.13171f * DEG_TO_RAD;
    private static final float CHARGE_LEFT_ARM_X = -51.52381f * DEG_TO_RAD;
    private static final float CHARGE_LEFT_ARM_Y = -23.53061f * DEG_TO_RAD;
    private static final float CHARGE_LEFT_ARM_Z = 8.24946f * DEG_TO_RAD;
    private static final float CHARGE_RIGHT_LEG_Z = 10.0f * DEG_TO_RAD;
    private static final float CHARGE_LEFT_LEG_Z = -7.5f * DEG_TO_RAD;

    public Ssj8PlayerModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

    @Override
    public void setupAnim(AbstractClientPlayer entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        ClientState cs = ClientState.get(entity);
        if (cs == null || !cs.form) {
            return;
        }
        float partial = ageInTicks - (float) entity.tickCount;
        float age = cs.modeAge(partial);
        float shake = Mth.sin(ageInTicks * 2.3f);
        this.rightArm.zRot += 0.14f;
        this.leftArm.zRot -= 0.14f;
        this.rightLeg.zRot += 0.07f;
        this.leftLeg.zRot -= 0.07f;
        switch (cs.mode) {
            case 1: {
                // Carga de ki (Z): la pose de DMZ entra y sale suavemente y solo se aplica si el jugador esta quieto.
                float still = 1.0f - Mth.clamp(limbSwingAmount * 4.0f, 0.0f, 1.0f);
                float blend = cs.chargeBlend(partial) * still;
                if (blend > 0.0f) {
                    this.applyKiCharge(blend, ageInTicks);
                }
                break;
            }
            case 9: {
                float in = Shapes.smooth(0.0f, 12.0f, age);
                this.rightArm.xRot = -0.15f * in;
                this.leftArm.xRot = -0.15f * in;
                this.rightArm.zRot = 1.35f * in + 0.05f * shake;
                this.leftArm.zRot = -1.35f * in - 0.05f * shake;
                this.head.xRot = -0.35f * in;
                this.rightLeg.zRot = 0.22f * in;
                this.leftLeg.zRot = -0.22f * in;
                break;
            }
            case 2: {
                float p = Mth.clamp(age / 40.0f, 0.0f, 1.0f);
                float conv = Shapes.smooth(0.4f, 0.9f, p);
                this.leftArm.xRot = this.rightArm.xRot = Mth.lerp(conv, 0.0f, -1.5f);
                this.rightArm.zRot = Mth.lerp(conv, 1.5f, 0.0f);
                this.leftArm.zRot = -this.rightArm.zRot;
                this.rightArm.yRot = Mth.lerp(conv, 0.0f, -0.5f);
                this.leftArm.yRot = -this.rightArm.yRot;
                this.rightLeg.zRot = 0.22f;
                this.leftLeg.zRot = -0.22f;
                this.body.xRot = 0.05f * conv;
                break;
            }
            case 3: {
                this.rightArm.xRot = -1.52f;
                this.leftArm.xRot = -1.52f;
                this.rightArm.zRot = 0.0f;
                this.leftArm.zRot = 0.0f;
                this.rightArm.yRot = -0.5f;
                this.leftArm.yRot = 0.5f;
                this.rightLeg.xRot = -0.3f;
                this.leftLeg.xRot = 0.35f;
                this.rightLeg.zRot = 0.2f;
                this.leftLeg.zRot = -0.2f;
                this.body.xRot = -0.08f;
                break;
            }
            case 4: {
                this.rightArm.xRot = -3.05f;
                this.leftArm.xRot = -3.05f;
                this.rightArm.zRot = 0.2f + 0.02f * shake;
                this.leftArm.zRot = -0.2f - 0.02f * shake;
                this.head.xRot = -0.4f;
                this.rightLeg.zRot = 0.16f;
                this.leftLeg.zRot = -0.16f;
                break;
            }
            case 5: {
                float t = Shapes.smooth(0.0f, 5.0f, age);
                float x = Mth.lerp(t, -3.05f, -1.1f);
                this.rightArm.xRot = x;
                this.leftArm.xRot = x;
                this.rightArm.zRot = 0.1f;
                this.leftArm.zRot = -0.1f;
                this.body.xRot = 0.25f * t;
                break;
            }
            case 6: {
                this.rightArm.xRot = -1.57f + this.head.xRot;
                this.rightArm.yRot = -0.1f;
                this.rightArm.zRot = 0.0f;
                this.leftArm.xRot = 0.3f;
                this.leftArm.zRot = -0.35f;
                break;
            }
            case 7: {
                this.leftArm.xRot = -1.57f + this.head.xRot;
                this.leftArm.yRot = 0.1f;
                this.leftArm.zRot = 0.0f;
                this.rightArm.xRot = 0.3f;
                this.rightArm.zRot = 0.35f;
                break;
            }
            case 8: {
                this.rightArm.xRot = -0.9f;
                this.leftArm.xRot = -0.9f;
                this.rightArm.yRot = -0.8f;
                this.leftArm.yRot = 0.8f;
                break;
            }
            default:
                break;
        }
        this.hat.copyFrom(this.head);
        this.jacket.copyFrom(this.body);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
    }

    /**
     * Aplica la pose de carga de ki con peso {@code w} (0..1). La oscilacion usa la misma
     * frecuencia que DragonMine Z: cos(anim_time * 540 grados), con anim_time en segundos.
     */
    private void applyKiCharge(float w, float ageInTicks) {
        float shake = Mth.cos(ageInTicks * 27.0f * DEG_TO_RAD);
        this.body.xRot = Mth.lerp(w, this.body.xRot, CHARGE_BODY_X);
        this.body.yRot = Mth.lerp(w, this.body.yRot, CHARGE_BODY_Y);
        this.body.y = 0.15f * shake * w;
        this.rightArm.xRot = Mth.lerp(w, this.rightArm.xRot, CHARGE_RIGHT_ARM_X);
        this.rightArm.yRot = Mth.lerp(w, this.rightArm.yRot, CHARGE_RIGHT_ARM_Y);
        this.rightArm.zRot = Mth.lerp(w, this.rightArm.zRot, CHARGE_RIGHT_ARM_Z);
        this.rightArm.y = 2.0f - 0.25f * shake * w;
        this.leftArm.xRot = Mth.lerp(w, this.leftArm.xRot, CHARGE_LEFT_ARM_X);
        this.leftArm.yRot = Mth.lerp(w, this.leftArm.yRot, CHARGE_LEFT_ARM_Y);
        this.leftArm.zRot = Mth.lerp(w, this.leftArm.zRot, CHARGE_LEFT_ARM_Z);
        this.leftArm.y = 2.0f - 0.25f * shake * w;
        this.rightLeg.zRot = Mth.lerp(w, this.rightLeg.zRot, CHARGE_RIGHT_LEG_Z);
        this.leftLeg.zRot = Mth.lerp(w, this.leftLeg.zRot, CHARGE_LEFT_LEG_Z);
    }
}
