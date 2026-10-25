package com.keinloree.story;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

/** Un paso de la campana. El Director llama a {@link #tick} mientras sea el paso actual. */
public interface Step {
    String id();

    default void enter(StoryCtx c) {}

    void tick(StoryCtx c);

    default void exit(StoryCtx c) {}

    /** Texto del HUD de objetivo (null = sin objetivo). */
    @Nullable
    default String objective(StoryCtx c) {
        return null;
    }

    /** Marcador del HUD (null = sin marcador). */
    @Nullable
    default BlockPos marker(StoryCtx c) {
        return null;
    }

    default int progress(StoryCtx c) {
        return 0;
    }

    default int progressMax(StoryCtx c) {
        return 0;
    }

    /** Paso que sigue a este (usado por /keinloree story skip). */
    @Nullable
    default String next() {
        return null;
    }

    /** Salta este paso. */
    default void skip(StoryCtx c) {
        String n = next();
        if (n != null) {
            c.goTo(n);
        }
    }
}
