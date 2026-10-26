package com.keinloree.client.model.gen;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3f;

/** GENERADO por bb2java.mjs desde blockbench/razako.bbmodel. No editar a mano. */
public final class RazakoAnims {
    public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(2.0F)
            .looping()
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, new Vector3f(0.0F, 0.7F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-1.0F, -5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5F, KeyframeAnimations.degreeVec(-1.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.2F, -24.6F, -30.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-10.5F, -28.8F, -25.7F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-13.3F, -32.5F, -21.9F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5F, KeyframeAnimations.degreeVec(-15.1F, -33.8F, -17.1F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-16.7F, -35.2F, -13.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-2.3F, 19.6F, 41.2F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-1.7F, 24.7F, 39.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-1.6F, 29.8F, 37.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5F, KeyframeAnimations.degreeVec(-4.1F, 32.6F, 34.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-6.5F, 34.9F, 32.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-80.2F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-83.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5F, KeyframeAnimations.degreeVec(-80.2F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-48.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-53.3F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5F, KeyframeAnimations.degreeVec(-48.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(121.0F, -28.3F, 26.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(124.7F, -26.1F, 28.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(128.0F, -25.3F, 30.1F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5F, KeyframeAnimations.degreeVec(128.7F, -20.0F, 32.3F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(129.0F, -15.6F, 34.2F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(6.0F, 0.0F, -1.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(1.0F)
            .looping()
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, new Vector3f(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, new Vector3f(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 6.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(1.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(3.0F, -6.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(1.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(3.0F, 6.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-3.0F, -5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-3.0F, 5.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-3.0F, -5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-13.1F, -40.1F, -8.1F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-16.3F, -41.5F, -6.1F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-16.8F, -42.6F, -2.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-19.8F, -43.3F, -0.9F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-20.0F, -44.3F, 2.1F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-6.9F, 37.6F, 30.1F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-4.4F, 42.8F, 28.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-9.3F, 42.8F, 26.4F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-7.3F, 47.2F, 24.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-11.8F, 46.8F, 22.9F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-81.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-86.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-81.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-86.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-81.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-39.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-50.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-39.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-50.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-39.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(123.8F, -11.3F, 39.4F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(129.9F, -14.5F, 39.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(126.4F, -8.4F, 42.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(132.1F, -12.0F, 41.9F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(128.5F, -6.1F, 44.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-26.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-26.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(26.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-26.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(26.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(42.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(42.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(20.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(30.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition RUN = AnimationDefinition.Builder.withLength(0.6F)
            .looping()
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, new Vector3f(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, new Vector3f(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(16.0F, 8.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(16.0F, -8.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(16.0F, 8.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-16.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-16.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-16.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(30.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(42.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(30.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(45.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(20.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-52.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(48.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-52.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(48.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-52.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(48.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(95.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(95.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(42.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(52.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(42.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(55.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(72.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(55.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition ATTACK_1 = AnimationDefinition.Builder.withLength(0.85F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, new Vector3f(0.0F, 3.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.34F, new Vector3f(0.0F, 3.0F, 3.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.45F, new Vector3f(0.0F, 5.0F, -4.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.6F, new Vector3f(0.0F, 4.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(-16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.34F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-26.4F, -41.2F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(-40.1F, -52.8F, -4.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(-80.7F, -67.8F, -42.4F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.34F, KeyframeAnimations.degreeVec(-87.6F, -66.7F, -48.5F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-58.9F, -46.6F, -22.7F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(-25.2F, -46.1F, 3.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.52F, KeyframeAnimations.degreeVec(-23.6F, -49.9F, 4.2F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(-27.0F, -46.5F, 5.7F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(-29.8F, -42.6F, 7.7F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-13.9F, 47.3F, 20.9F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(-11.8F, 55.5F, 18.7F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(-57.7F, 51.9F, 58.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.34F, KeyframeAnimations.degreeVec(-68.4F, 50.1F, 66.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-25.9F, 42.5F, 29.8F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(-1.2F, 61.8F, 17.8F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.52F, KeyframeAnimations.degreeVec(0.0F, 66.2F, 16.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(-2.9F, 63.7F, 15.4F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(-18.0F, 51.5F, 15.5F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(-99.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.34F, KeyframeAnimations.degreeVec(-11.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-62.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(-51.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.52F, KeyframeAnimations.degreeVec(-49.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(-56.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(-87.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(-36.2F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.34F, KeyframeAnimations.degreeVec(-11.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-79.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(-71.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.52F, KeyframeAnimations.degreeVec(-69.3F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(-67.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(135.8F, -6.9F, 40.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(135.2F, -40.8F, 37.3F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(59.0F, -62.8F, 52.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.34F, KeyframeAnimations.degreeVec(38.2F, -56.4F, 61.7F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(131.2F, -42.8F, 30.3F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(195.6F, 12.5F, 44.8F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.52F, KeyframeAnimations.degreeVec(196.3F, 17.4F, 47.7F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(183.1F, 10.8F, 45.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(138.0F, -4.4F, 43.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-28.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(26.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(24.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(36.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.62F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.26F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition ATTACK_2 = AnimationDefinition.Builder.withLength(0.75F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, new Vector3f(0.0F, 3.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, new Vector3f(0.0F, 3.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.55F, new Vector3f(0.0F, 2.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(-4.0F, 48.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.28F, KeyframeAnimations.degreeVec(-4.0F, 48.0F, 3.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(8.0F, -62.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.55F, KeyframeAnimations.degreeVec(6.0F, -52.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(0.0F, -30.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-4.0F, 40.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.55F, KeyframeAnimations.degreeVec(-3.0F, 34.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-31.1F, -43.0F, 9.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(-8.3F, -47.7F, 26.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(-7.4F, -51.3F, 27.3F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-34.2F, -45.5F, 11.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-36.0F, -47.0F, 11.2F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-34.8F, -44.0F, 14.9F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-19.0F, 52.4F, 14.2F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(-10.7F, 79.7F, 15.7F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(-6.3F, 76.8F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-14.7F, 64.0F, 11.9F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-17.5F, 64.1F, 12.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-22.5F, 55.0F, 9.9F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(-111.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(-111.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-63.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-61.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(-65.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(-81.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-64.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-59.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(138.8F, -3.6F, 43.7F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(195.2F, 53.5F, 89.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(238.4F, 72.1F, 158.9F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(330.9F, 82.3F, 184.9F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(343.4F, 82.3F, 188.9F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(501.1F, -1.3F, 46.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 24.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.46F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -40.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 7.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(6.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(6.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 22.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(8.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.46F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 38.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.62F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 12.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition ATTACK_3 = AnimationDefinition.Builder.withLength(1.2F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, 45.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 40.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.82F, KeyframeAnimations.degreeVec(0.0F, -320.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.05F, KeyframeAnimations.degreeVec(0.0F, -358.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, -360.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, new Vector3f(0.0F, 6.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, new Vector3f(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.8F, new Vector3f(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.95F, new Vector3f(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(10.0F, 12.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-2.0F, -20.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-35.7F, -44.3F, 16.3F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.18F, KeyframeAnimations.degreeVec(-20.2F, -74.0F, 24.9F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-22.4F, -74.3F, 23.3F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-37.1F, -45.9F, 18.7F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-38.9F, -46.8F, 18.7F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.92F, KeyframeAnimations.degreeVec(-38.6F, -47.7F, 20.1F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-39.4F, -44.9F, 21.5F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-23.4F, 55.6F, 8.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.18F, KeyframeAnimations.degreeVec(-34.2F, 48.7F, 11.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-34.6F, 50.6F, 10.9F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-20.3F, 66.3F, 1.8F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-22.2F, 66.8F, 2.3F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.92F, KeyframeAnimations.degreeVec(-21.8F, 66.3F, 0.4F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-29.0F, 58.6F, 2.2F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.18F, KeyframeAnimations.degreeVec(-84.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-77.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-66.2F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-64.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.92F, KeyframeAnimations.degreeVec(-56.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.18F, KeyframeAnimations.degreeVec(-21.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-25.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-69.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-66.2F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.92F, KeyframeAnimations.degreeVec(-68.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(501.7F, -0.7F, 46.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.18F, KeyframeAnimations.degreeVec(370.8F, -33.8F, 67.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(340.3F, -75.6F, 105.7F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(314.2F, 77.1F, 181.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(323.7F, 77.3F, 183.9F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.92F, KeyframeAnimations.degreeVec(235.1F, 67.7F, 96.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(144.0F, 1.3F, 48.8F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(-75.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-26.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-16.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(30.0F, 0.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(18.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(12.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.55F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(70.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(80.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition CHARGE = AnimationDefinition.Builder.withLength(0.5F)
            .looping()
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.125F, new Vector3f(0.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, new Vector3f(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.375F, new Vector3f(0.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, new Vector3f(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(30.0F, 6.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(30.0F, -6.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(30.0F, 6.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-41.8F, -44.3F, 21.2F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-41.2F, -43.9F, 23.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-42.8F, -44.7F, 22.7F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-30.7F, 63.3F, 4.7F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-29.9F, 64.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-31.7F, 63.7F, 3.6F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-46.2F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-54.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-46.2F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-43.3F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.4F, 67.2F, -127.1F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-0.6F, 70.9F, -135.2F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(6.3F, 66.5F, -128.7F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(54.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.375F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(54.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.375F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(54.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.375F, KeyframeAnimations.degreeVec(100.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(100.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.375F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(58.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(66.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(58.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(70.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(86.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(70.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition LEAP = AnimationDefinition.Builder.withLength(1.2F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, new Vector3f(0.0F, 6.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, new Vector3f(0.0F, -8.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.62F, new Vector3f(0.0F, -20.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.78F, new Vector3f(0.0F, -18.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.88F, new Vector3f(0.0F, 4.0F, -4.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.96F, new Vector3f(0.0F, 5.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.65F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.78F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.88F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.62F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.88F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-41.8F, -45.2F, 24.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-41.0F, -47.0F, 26.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-70.1F, -49.6F, 8.4F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.65F, KeyframeAnimations.degreeVec(-103.7F, -64.1F, -17.3F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.78F, KeyframeAnimations.degreeVec(-110.8F, -63.6F, -23.3F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.88F, KeyframeAnimations.degreeVec(-46.3F, -53.9F, 32.1F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.96F, KeyframeAnimations.degreeVec(-46.2F, -58.9F, 31.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-47.0F, -45.6F, 32.2F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-30.8F, 59.4F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-27.1F, 65.2F, -2.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-37.0F, 60.0F, 3.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.65F, KeyframeAnimations.degreeVec(-75.9F, 52.8F, 36.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.78F, KeyframeAnimations.degreeVec(-85.9F, 51.9F, 43.3F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.88F, KeyframeAnimations.degreeVec(-27.8F, 70.7F, -11.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.96F, KeyframeAnimations.degreeVec(-27.1F, 74.2F, -11.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-36.8F, 61.5F, -6.9F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-78.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-90.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.65F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.78F, KeyframeAnimations.degreeVec(-11.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.88F, KeyframeAnimations.degreeVec(-59.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.96F, KeyframeAnimations.degreeVec(-49.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-56.2F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-91.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.65F, KeyframeAnimations.degreeVec(-36.2F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.78F, KeyframeAnimations.degreeVec(-11.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.88F, KeyframeAnimations.degreeVec(-75.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.96F, KeyframeAnimations.degreeVec(-69.3F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(145.5F, 2.6F, 50.2F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(159.7F, 3.8F, 52.3F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(155.0F, -41.6F, 30.9F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.65F, KeyframeAnimations.degreeVec(74.0F, -55.7F, 42.3F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.78F, KeyframeAnimations.degreeVec(56.6F, -50.4F, 50.2F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.88F, KeyframeAnimations.degreeVec(211.6F, 19.2F, 58.1F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.96F, KeyframeAnimations.degreeVec(211.0F, 27.3F, 60.4F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(148.8F, 5.2F, 53.5F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.55F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-44.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.65F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.78F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.88F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(44.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(30.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.65F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.78F, KeyframeAnimations.degreeVec(6.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.88F, KeyframeAnimations.degreeVec(22.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(70.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.65F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.88F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.65F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.88F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.55F, KeyframeAnimations.degreeVec(70.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(70.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition ROAR = AnimationDefinition.Builder.withLength(1.6F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, new Vector3f(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.55F, new Vector3f(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, new Vector3f(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.55F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, -1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.61F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, 1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.67F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, -1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.73F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, 1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.79F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, -1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.85F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, 1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.91F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, -1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.97F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, 1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.03F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, -1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.09F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, 1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.15F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, -1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.21F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, 1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.27F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, -1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.33F, KeyframeAnimations.degreeVec(-13.0F, 0.0F, 1.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, -32.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 150.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 150.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, -32.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(10.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(10.0F, 0.0F, -105.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(10.0F, 0.0F, -105.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(62.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -150.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -150.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(62.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(42.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(40.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.55F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1F, KeyframeAnimations.degreeVec(35.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(55.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition BLOCK = AnimationDefinition.Builder.withLength(0.5F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, new Vector3f(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, new Vector3f(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(8.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(8.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(-8.0F, 8.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-8.0F, 8.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-47.4F, -45.5F, 32.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(-61.3F, -43.4F, 24.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-62.6F, -43.1F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-62.5F, -43.2F, 26.4F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-37.3F, 61.7F, -7.4F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(-39.0F, 48.8F, -10.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-40.3F, 48.6F, -11.4F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-40.7F, 49.3F, -12.7F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(-72.3F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-73.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-72.3F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(-61.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-63.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-61.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(149.1F, 5.4F, 53.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(99.4F, 37.9F, -35.9F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(101.1F, 37.6F, -35.3F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(98.6F, 38.8F, -35.7F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(16.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(16.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition HORIZON = AnimationDefinition.Builder.withLength(2.0F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, new Vector3f(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, new Vector3f(0.0F, 7.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, new Vector3f(0.0F, 8.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, new Vector3f(0.0F, 8.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7F, new Vector3f(0.0F, 3.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-2.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(10.0F, 88.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(10.0F, 88.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(10.0F, 90.0F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.24F, KeyframeAnimations.degreeVec(10.0F, 86.0F, 2.5F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.28F, KeyframeAnimations.degreeVec(10.0F, 90.0F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.32F, KeyframeAnimations.degreeVec(10.0F, 86.0F, 2.5F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.36F, KeyframeAnimations.degreeVec(10.0F, 90.0F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(10.0F, 86.0F, 2.5F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(10.0F, 88.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(8.0F, -88.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.85F, KeyframeAnimations.degreeVec(4.0F, -82.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(-4.0F, -40.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(-4.0F, -42.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(-3.0F, 50.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-49.8F, -45.5F, 36.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-51.5F, -46.1F, 35.3F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(-38.6F, -59.2F, 44.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-38.2F, -59.3F, 45.2F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(-38.3F, -59.3F, 45.3F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.52F, KeyframeAnimations.degreeVec(-49.1F, -48.8F, 38.2F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(-52.4F, -49.1F, 36.2F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-50.9F, -45.5F, 37.7F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-40.0F, 62.4F, -10.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-38.5F, 65.3F, -12.1F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(-33.3F, 78.8F, -15.2F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-33.0F, 78.6F, -15.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(-33.2F, 78.6F, -16.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.52F, KeyframeAnimations.degreeVec(-34.4F, 69.0F, -16.5F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(-37.9F, 69.6F, -13.9F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-41.8F, 62.8F, -12.5F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-64.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(-71.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-72.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(-72.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.52F, KeyframeAnimations.degreeVec(-64.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(-63.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-49.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(-53.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(-54.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(-54.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.52F, KeyframeAnimations.degreeVec(-68.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(-65.3F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(150.6F, 6.4F, 55.2F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(168.4F, 15.1F, 54.1F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(313.4F, 61.3F, 221.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.degreeVec(310.1F, 61.1F, 221.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(310.0F, 61.0F, 221.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.52F, KeyframeAnimations.degreeVec(312.3F, 64.9F, 196.1F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(322.6F, 67.1F, 187.1F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(151.3F, 7.0F, 56.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.SCALE,
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.9F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.04F, 1.04F, 1.04F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.1F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.2F, KeyframeAnimations.scaleVec(1.06F, 1.06F, 1.06F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.3F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.4F, KeyframeAnimations.scaleVec(1.08F, 1.08F, 1.08F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 12.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 12.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 12.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(34.0F, 0.0F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(34.0F, 0.0F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(14.0F, 0.0F, -12.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(56.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(56.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.45F, KeyframeAnimations.degreeVec(18.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.78F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 40.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition CHAIN = AnimationDefinition.Builder.withLength(1.0F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, new Vector3f(0.0F, 2.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, new Vector3f(0.0F, 2.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, new Vector3f(0.0F, 1.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-4.0F, 34.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, KeyframeAnimations.degreeVec(8.0F, -32.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(4.0F, -20.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, -20.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, KeyframeAnimations.degreeVec(-3.0F, 28.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 16.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, -32.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, KeyframeAnimations.degreeVec(-36.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, -32.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.12F, KeyframeAnimations.degreeVec(40.0F, 0.0F, -40.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(110.0F, 0.0F, -50.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-88.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(62.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(100.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, KeyframeAnimations.degreeVec(110.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(62.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-360.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.42F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.SCALE,
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.3F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.45F, KeyframeAnimations.scaleVec(1.0F, 4.2F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.scaleVec(1.0F, 4.2F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.95F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, KeyframeAnimations.degreeVec(14.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.42F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.46F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition KNEEL = AnimationDefinition.Builder.withLength(1.0F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, new Vector3f(0.0F, 10.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, new Vector3f(0.0F, 10.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-51.1F, -45.5F, 38.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-42.7F, -67.4F, 45.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-51.6F, -61.7F, 38.5F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-42.2F, 62.9F, -12.9F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-51.1F, 52.2F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-41.4F, 58.2F, -17.8F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-83.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-52.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-36.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-73.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(151.5F, 7.1F, 56.1F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-6.7F, 11.5F, 74.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-82.5F, 25.5F, 65.7F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(-86.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-86.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(86.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(86.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(86.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(86.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition HURT = AnimationDefinition.Builder.withLength(0.35F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.07F, new Vector3f(0.0F, 1.5F, 3.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.35F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.07F, KeyframeAnimations.degreeVec(-16.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.07F, KeyframeAnimations.degreeVec(-14.0F, -8.0F, 2.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(-6.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-51.5F, -45.5F, 38.4F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.07F, KeyframeAnimations.degreeVec(-44.9F, -48.8F, 43.9F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(-50.3F, -47.5F, 40.2F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(-52.0F, -45.4F, 39.2F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-44.1F, 63.3F, -15.1F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.07F, KeyframeAnimations.degreeVec(-45.4F, 67.2F, -13.9F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(-42.0F, 67.3F, -17.7F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(-45.1F, 63.5F, -16.2F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.07F, KeyframeAnimations.degreeVec(-102.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(-81.3F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.07F, KeyframeAnimations.degreeVec(-44.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(-52.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-208.3F, 7.2F, 56.4F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.07F, KeyframeAnimations.degreeVec(-221.4F, -4.4F, 61.6F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.2F, KeyframeAnimations.degreeVec(-201.1F, 6.2F, 58.7F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(-207.9F, 7.5F, 56.7F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.07F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.07F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.07F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.07F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.28F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.35F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition POSE = AnimationDefinition.Builder.withLength(2.0F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, new Vector3f(0.0F, 0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-52.2F, -45.4F, 39.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-61.7F, -46.2F, 33.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-62.8F, -45.7F, 33.6F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-45.4F, 63.6F, -16.6F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-43.7F, 53.0F, -20.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-44.5F, 52.6F, -21.2F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-78.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-78.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-79.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_forearm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-44.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-61.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-63.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("weapon", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-207.8F, 7.5F, 56.8F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-262.3F, 35.2F, -30.9F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-260.5F, 35.6F, -30.4F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("chain_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, 9.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, 9.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_shin", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(20.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape_low", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9F, KeyframeAnimations.degreeVec(32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(28.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    private RazakoAnims() {
    }
}
