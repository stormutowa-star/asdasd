package com.keinloree.story;

/** Registros de la historia que se hacen una vez al arrancar (pasos, cinematicas, acciones). */
public final class StoryBootstrap {
    private static boolean done;

    private StoryBootstrap() {}

    public static synchronized void init() {
        if (done) {
            return;
        }
        done = true;
        Saga.register();
        Cinematics.registerBuiltins();
        SagaActions.register();
        SagaEvents.init();
        com.keinloree.world.Pillars.init();
        // @accion atmosfera <tinte> [niebla] [flags]
        ScriptActions.register("atmosfera", (ctx, a) -> {
            float tint = a.length > 0 ? parse(a[0], 0.0F) : 0.0F;
            float fog = a.length > 1 ? parse(a[1], ctx.campaign.atmoFog) : ctx.campaign.atmoFog;
            int flags = a.length > 2 ? (int) parse(a[2], 0.0F) : ctx.campaign.atmoFlags;
            Atmosphere.set(ctx.level, tint, fog, flags);
        });
    }

    private static float parse(String s, float def) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
