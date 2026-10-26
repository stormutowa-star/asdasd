package com.keinloree.client.model.gen;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3f;

/** GENERADO por bb2java.mjs desde blockbench/medianoche.bbmodel. No editar a mano. */
public final class MedianocheAnims {
    public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(2.0F)
            .looping()
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, new Vector3f(0.0F, -4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-26.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(11.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("ring_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, -360.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("ring_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-360.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("ring_3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(1.0F)
            .looping()
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, new Vector3f(0.0F, -4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, new Vector3f(0.0F, -0.6F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, new Vector3f(0.0F, -0.6F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-36.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(4.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(21.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(27.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(21.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition RUN = AnimationDefinition.Builder.withLength(0.6F)
            .looping()
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, new Vector3f(0.0F, -4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.15F, new Vector3f(0.0F, -0.6F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.45F, new Vector3f(0.0F, -0.6F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-56.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-44.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(24.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-44.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition ATTACK_1 = AnimationDefinition.Builder.withLength(0.6F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, new Vector3f(0.0F, -4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.18F, KeyframeAnimations.degreeVec(0.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(8.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.18F, KeyframeAnimations.degreeVec(-150.0F, -10.0F, 40.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-40.0F, 30.0F, -10.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.18F, KeyframeAnimations.degreeVec(10.0F, 0.0F, -26.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, -16.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition ATTACK_2 = AnimationDefinition.Builder.withLength(0.6F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, new Vector3f(0.0F, -4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.18F, KeyframeAnimations.degreeVec(0.0F, -35.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 40.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.18F, KeyframeAnimations.degreeVec(-95.0F, -65.0F, 60.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-90.0F, 65.0F, -15.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.18F, KeyframeAnimations.degreeVec(10.0F, 0.0F, -26.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, -16.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition ATTACK_3 = AnimationDefinition.Builder.withLength(0.75F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.22F, new Vector3f(0.0F, -3.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.37F, new Vector3f(0.0F, -3.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.225F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.375F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.225F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.375F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.225F, KeyframeAnimations.degreeVec(10.0F, 0.0F, -26.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.375F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, -16.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition SPECIAL_1 = AnimationDefinition.Builder.withLength(1.4F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7F, new Vector3f(0.0F, -4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-170.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-170.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(-170.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-170.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("ring_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, -360.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("ring_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(-360.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("ring_3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition SPECIAL_2 = AnimationDefinition.Builder.withLength(1.0F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, new Vector3f(0.0F, -4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("ring_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, -360.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("ring_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-360.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("ring_3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition FLOAT = AnimationDefinition.Builder.withLength(2.0F)
            .looping()
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, new Vector3f(0.0F, -4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("ring_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, -360.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("ring_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-360.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("ring_3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition CAST = AnimationDefinition.Builder.withLength(1.0F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.5F, new Vector3f(0.0F, -4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-6.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-6.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-5.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-95.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition POSE = AnimationDefinition.Builder.withLength(2.0F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, new Vector3f(0.0F, -4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, new Vector3f(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-5.0F, -15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-5.0F, -15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-10.0F, 12.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-10.0F, 12.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-160.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-165.0F, 0.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(10.0F, 0.0F, -35.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, -35.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("cape", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition KNEEL = AnimationDefinition.Builder.withLength(1.0F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, new Vector3f(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, new Vector3f(0.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, new Vector3f(0.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(-80.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(-80.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition HURT = AnimationDefinition.Builder.withLength(0.3F)
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.1F, KeyframeAnimations.degreeVec(-16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.3F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("halo", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 360.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    private MedianocheAnims() {
    }
}
