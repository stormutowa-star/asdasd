package com.keinloree.story;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import org.slf4j.Logger;

/**
 * Tareas diferidas de la historia (no se guardan: un reinicio las pierde, por eso solo se usan para efectos y
 * remates de pocos segundos; lo importante va en Campaign).
 */
public final class Later {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final class Task {
        int delay;
        int tick;
        final int max;
        final IntPredicate body;

        Task(int delay, int max, IntPredicate body) {
            this.delay = delay;
            this.max = max;
            this.body = body;
        }
    }

    private static final List<Task> TASKS = new ArrayList<>();
    private static final List<Task> ADDED = new ArrayList<>();

    private Later() {
    }

    /** Ejecuta {@code r} dentro de {@code ticks} ticks. */
    public static void run(int ticks, Runnable r) {
        ADDED.add(new Task(Math.max(0, ticks), 1, t -> {
            r.run();
            return false;
        }));
    }

    /** Llama a {@code body(n)} cada tick (n = 0..max-1) hasta que devuelva false o se llegue a max. */
    public static void repeat(int delay, int max, IntPredicate body) {
        ADDED.add(new Task(Math.max(0, delay), max, body));
    }

    public static void tick() {
        if (!ADDED.isEmpty()) {
            TASKS.addAll(ADDED);
            ADDED.clear();
        }
        if (TASKS.isEmpty()) {
            return;
        }
        List<Task> keep = new ArrayList<>(TASKS.size());
        for (Task t : TASKS) {
            if (t.delay > 0) {
                t.delay--;
                keep.add(t);
                continue;
            }
            boolean more;
            try {
                more = t.body.test(t.tick);
            } catch (RuntimeException e) {
                LOGGER.error("[Later] Fallo en una tarea diferida", e);
                more = false;
            }
            t.tick++;
            if (more && t.tick < t.max) {
                keep.add(t);
            }
        }
        TASKS.clear();
        TASKS.addAll(keep);
    }

    public static void reset() {
        TASKS.clear();
        ADDED.clear();
    }
}
