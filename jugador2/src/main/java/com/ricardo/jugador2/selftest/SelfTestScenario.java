package com.ricardo.jugador2.selftest;

import com.ricardo.jugador2.Jugador2Mod;
import com.ricardo.jugador2.npc.NpcManager;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Escenario automático en un servidor de desarrollo. Solo corre si la variable JUGADOR2_SELFTEST vale 1
 * o la propiedad jugador2.selftest es true. Imprime PASS o FAIL por paso y detiene el servidor.
 */
public final class SelfTestScenario {
    private static final String PREFIX = "[JUGADOR2-SELFTEST] ";
    private static final int PLATFORM_SIZE = 20;
    private static final String NPC_NAME = "Jugador2";

    /** Un paso: se prepara una vez y luego se comprueba cada tick hasta que cumple o agota el límite. */
    private record Step(String name, int timeoutTicks, Runnable setup, Predicate<MinecraftServer> check) {}

    private static boolean running;
    private static List<Step> steps;
    private static int stepIndex;
    private static int ticksInStep;
    private static Vec3 platformCenter;

    private SelfTestScenario() {
    }

    public static boolean enabled() {
        return Boolean.getBoolean("jugador2.selftest") || "1".equals(System.getenv("JUGADOR2_SELFTEST"));
    }

    public static boolean isRunning() {
        return running;
    }

    /** Construye la plataforma de prueba, fija las reglas del mundo y prepara los pasos. */
    public static void start(MinecraftServer server) {
        ServerLevel level = server.overworld();
        server.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        server.setDifficulty(Difficulty.EASY, true);
        buildPlatform(level);

        steps = new ArrayList<>();
        steps.add(new Step("NPC en la lista de jugadores", 40,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    manager.spawn(NpcManager.offlineProfile(NPC_NAME), level, platformCenter);
                    manager.spawn(NpcManager.offlineProfile(NPC_NAME), level, platformCenter);
                },
                s -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    if (npc == null) {
                        return false;
                    }
                    long copies = s.getPlayerList().getPlayers().stream()
                            .filter(p -> p.getGameProfile().getName().equals(NPC_NAME))
                            .count();
                    return copies == 1 && s.getPlayerList().getPlayers().contains((ServerPlayer) npc);
                }));

        running = true;
        stepIndex = 0;
        ticksInStep = 0;
        System.out.println(PREFIX + "START");
    }

    /** Avanza el escenario un tick. Llamar en el fin de cada tick del servidor. */
    public static void tick(MinecraftServer server) {
        if (!running) {
            return;
        }
        if (stepIndex >= steps.size()) {
            finish(server, true, 0);
            return;
        }
        Step step = steps.get(stepIndex);
        try {
            if (ticksInStep == 0) {
                step.setup().run();
            }
            if (step.check().test(server)) {
                System.out.println(PREFIX + "STEP " + (stepIndex + 1) + " " + step.name() + " PASS");
                stepIndex++;
                ticksInStep = 0;
                return;
            }
        } catch (RuntimeException e) {
            System.out.println(PREFIX + "STEP " + (stepIndex + 1) + " " + step.name() + " FAIL " + e);
            finish(server, false, stepIndex + 1);
            return;
        }
        ticksInStep++;
        if (ticksInStep > step.timeoutTicks()) {
            System.out.println(PREFIX + "STEP " + (stepIndex + 1) + " " + step.name() + " FAIL timeout");
            finish(server, false, stepIndex + 1);
        }
    }

    private static void finish(MinecraftServer server, boolean passed, int failedStep) {
        running = false;
        if (passed) {
            System.out.println(PREFIX + "RESULT PASS");
        } else {
            System.out.println(PREFIX + "RESULT FAIL step " + failedStep);
        }
        server.halt(false);
    }

    /** Plataforma de piedra de 20×20, con aire encima, varios bloques por encima del terreno. */
    private static void buildPlatform(ServerLevel level) {
        int x0 = 0;
        int z0 = 0;
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, x0 + PLATFORM_SIZE / 2, z0 + PLATFORM_SIZE / 2);
        int y = surface + 5;
        for (int x = x0; x < x0 + PLATFORM_SIZE; x++) {
            for (int z = z0; z < z0 + PLATFORM_SIZE; z++) {
                level.setBlock(new BlockPos(x, y - 1, z), Blocks.STONE.defaultBlockState(), 3);
                for (int dy = 0; dy < 3; dy++) {
                    level.setBlock(new BlockPos(x, y + dy, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        platformCenter = new Vec3(x0 + PLATFORM_SIZE / 2.0 + 0.5, y, z0 + PLATFORM_SIZE / 2.0 + 0.5);
    }
}
