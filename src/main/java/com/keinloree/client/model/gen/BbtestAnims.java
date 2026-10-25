package com.keinloree.client.model.gen;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3f;

/** GENERADO por bb2java.mjs desde blockbench/bbtest.bbmodel. No editar a mano. */
public final class BbtestAnims {
    public static final AnimationDefinition WAVE = AnimationDefinition.Builder.withLength(2.0F)
            .looping()
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, new Vector3f(0.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 100.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 110.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 100.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    private BbtestAnims() {
    }
}
