package com.keinloree.story;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

/** Registro de pasos de la campana por id. */
public final class Steps {
    public static final String START = Saga.START;

    private static final Map<String, Step> STEPS = new LinkedHashMap<>();

    private Steps() {}

    public static void register(Step step) {
        STEPS.put(step.id(), step);
    }

    @Nullable
    public static Step get(String id) {
        return STEPS.get(id);
    }

    public static Set<String> ids() {
        return STEPS.keySet();
    }

    /** Pasos de prueba del WP1 (el WP12 los sustituye). */
    static void registerBuiltins() {
        register(new Step() {
            @Override
            public String id() {
                return START;
            }

            @Override
            public void tick(StoryCtx c) {
                if (c.ticksInStep() >= 1200) {
                    c.goTo("prueba.escena");
                }
            }

            @Override
            public String objective(StoryCtx c) {
                return "Explora el mundo (prueba)";
            }

            @Override
            public int progress(StoryCtx c) {
                return (int) Math.min(60, c.ticksInStep() / 20);
            }

            @Override
            public int progressMax(StoryCtx c) {
                return 60;
            }

            @Override
            public BlockPos marker(StoryCtx c) {
                return null;
            }

            @Override
            public String next() {
                return "prueba.escena";
            }
        });
        register(new Step() {
            @Override
            public String id() {
                return "prueba.escena";
            }

            @Override
            public void enter(StoryCtx c) {
                c.scene("prueba");
            }

            @Override
            public void tick(StoryCtx c) {
                if (c.ticksInStep() > 10 && ScenePlayer.isIdle()) {
                    c.goTo("fin.prueba");
                }
            }

            @Override
            public String next() {
                return "fin.prueba";
            }
        });
        register(new Step() {
            @Override
            public String id() {
                return "fin.prueba";
            }

            @Override
            public void tick(StoryCtx c) {
            }

            @Override
            public String objective(StoryCtx c) {
                return "Fin de la prueba";
            }
        });
    }
}
