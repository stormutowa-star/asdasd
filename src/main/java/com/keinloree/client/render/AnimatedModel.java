package com.keinloree.client.render;

import com.keinloree.entity.Animated;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/**
 * Envuelve un modelo generado desde Blockbench y le aplica las animaciones: la accion sincronizada (ataques,
 * especiales, arrodillarse...) y, si no hay accion, reposo/caminar/correr. La cabeza sigue la mirada.
 */
public class AnimatedModel<T extends LivingEntity & Animated> extends HierarchicalModel<T> {
    private static final Vector3f CACHE = new Vector3f();
    private final ModelPart root;
    private final ModelPart head;
    private AnimSet set;

    public AnimatedModel(HierarchicalModel<Entity> inner, ModelPart head, AnimSet set) {
        this.root = inner.root();
        this.head = head;
        this.set = set;
    }

    public void setSet(AnimSet set) {
        this.set = set;
    }

    public AnimSet set() {
        return set;
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(T e, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        int[] st = e.clientAnimState();
        if (st[1] != e.animSeq()) {
            st[1] = e.animSeq();
            st[0] = e.tickCount;
        }
        AnimationDefinition act = set.actions.get(e.animId());
        float ms = (ageInTicks - st[0]) * 50.0F;
        boolean hold = e.animId() == Animated.A_KNEEL || e.animId() == Animated.A_POSE;
        if (act != null && ms >= 0.0F && (hold || ms <= act.lengthInSeconds() * 1000.0F)) {
            KeyframeAnimations.animate(this, act, (long) ms, 1.0F, CACHE);
            return;
        }
        float speed = Mth.clamp(limbSwingAmount, 0.0F, 1.5F);
        if (speed > 0.08F) {
            boolean running = speed > 0.75F;
            AnimationDefinition loc = running ? set.run : set.walk;
            float cycle = running ? 1.6F : 2.4F;
            KeyframeAnimations.animate(this, loc, (long) (limbSwing / cycle * loc.lengthInSeconds() * 1000.0F), 1.0F, CACHE);
        } else {
            KeyframeAnimations.animate(this, set.idle, (long) (ageInTicks * 50.0F), 1.0F, CACHE);
        }
        if (head != null) {
            head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
            head.xRot += headPitch * Mth.DEG_TO_RAD;
        }
    }
}
