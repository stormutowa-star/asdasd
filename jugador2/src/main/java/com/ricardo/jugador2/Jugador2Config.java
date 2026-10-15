package com.ricardo.jugador2;

import com.ricardo.jugador2.npc.NpcSettings;
import net.minecraftforge.common.ForgeConfigSpec;

/** Configuración común del mod (jugador2-common.toml). Claves y rangos según la sección 10 del spec. */
public final class Jugador2Config {
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue ENABLED;
    private static final ForgeConfigSpec.BooleanValue SPAWN_ON_SERVER_START;
    private static final ForgeConfigSpec.IntValue AI_DECISION_INTERVAL_TICKS;
    private static final ForgeConfigSpec.DoubleValue HOSTILE_DETECTION_RADIUS;
    private static final ForgeConfigSpec.IntValue EAT_HUNGER_THRESHOLD;
    private static final ForgeConfigSpec.DoubleValue FLEE_HEALTH_THRESHOLD;
    private static final ForgeConfigSpec.IntValue RESPAWN_DELAY_TICKS;
    private static final ForgeConfigSpec.IntValue PATH_SEARCH_NODE_LIMIT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        NpcSettings defaults = NpcSettings.defaults();

        ENABLED = builder.define("enabled", defaults.enabled());
        SPAWN_ON_SERVER_START = builder.define("spawnOnServerStart", defaults.spawnOnServerStart());
        AI_DECISION_INTERVAL_TICKS = builder.defineInRange("aiDecisionIntervalTicks", defaults.aiDecisionIntervalTicks(), 1, 200);
        HOSTILE_DETECTION_RADIUS = builder.defineInRange("hostileDetectionRadius", defaults.hostileDetectionRadius(), 1.0, 32.0);
        EAT_HUNGER_THRESHOLD = builder.defineInRange("eatHungerThreshold", defaults.eatHungerThreshold(), 0, 20);
        FLEE_HEALTH_THRESHOLD = builder.defineInRange("fleeHealthThreshold", defaults.fleeHealthThreshold(), 0.0, 20.0);
        RESPAWN_DELAY_TICKS = builder.defineInRange("respawnDelayTicks", defaults.respawnDelayTicks(), 0, 6000);
        PATH_SEARCH_NODE_LIMIT = builder.defineInRange("pathSearchNodeLimit", defaults.pathSearchNodeLimit(), 100, 20000);

        SPEC = builder.build();
    }

    private Jugador2Config() {
    }

    /** Lee los valores actuales. Solo válido una vez cargada la configuración del mundo o del servidor. */
    public static NpcSettings read() {
        return new NpcSettings(
                ENABLED.get(),
                SPAWN_ON_SERVER_START.get(),
                AI_DECISION_INTERVAL_TICKS.get(),
                HOSTILE_DETECTION_RADIUS.get(),
                EAT_HUNGER_THRESHOLD.get(),
                FLEE_HEALTH_THRESHOLD.get().floatValue(),
                RESPAWN_DELAY_TICKS.get(),
                PATH_SEARCH_NODE_LIMIT.get());
    }
}
