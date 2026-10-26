package com.keinloree.client.render;

import com.keinloree.entity.Animated;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.resources.ResourceLocation;

/** Animaciones de un personaje: reposo, caminar, correr y una tabla id de accion -> animacion. */
public final class AnimSet {
    final AnimationDefinition idle;
    final AnimationDefinition walk;
    final AnimationDefinition run;
    final Map<Integer, AnimationDefinition> actions = new HashMap<>();
    final ResourceLocation texture;
    final ResourceLocation glow;

    public AnimSet(ResourceLocation texture, ResourceLocation glow, AnimationDefinition idle, AnimationDefinition walk,
                   AnimationDefinition run) {
        this.texture = texture;
        this.glow = glow;
        this.idle = idle;
        this.walk = walk;
        this.run = run;
    }

    public AnimSet on(int id, AnimationDefinition def) {
        actions.put(id, def);
        return this;
    }

    /** Las acciones comunes de los personajes humanoides. */
    public AnimSet humanoid(AnimationDefinition a1, AnimationDefinition a2, AnimationDefinition a3, AnimationDefinition s1,
                            AnimationDefinition s2, AnimationDefinition cast, AnimationDefinition pose,
                            AnimationDefinition kneel, AnimationDefinition hurt) {
        return on(Animated.A_ATTACK_1, a1).on(Animated.A_ATTACK_2, a2).on(Animated.A_ATTACK_3, a3)
                .on(Animated.A_SPECIAL_1, s1).on(Animated.A_SPECIAL_2, s2).on(Animated.A_CAST, cast)
                .on(Animated.A_POSE, pose).on(Animated.A_KNEEL, kneel).on(Animated.A_HURT, hurt);
    }
}
