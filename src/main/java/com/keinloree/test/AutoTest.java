package com.keinloree.test;

import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

/**
 * Banco de pruebas del servidor. No hace nada salvo que se arranque con
 * -Dkeinloree.servertest=escenario (o con el archivo run/kl_servertest.txt).
 * Otros paquetes pueden anadir escenarios con {@link #register}.
 */
public final class AutoTest {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Consumer<Script>> SCENARIOS = new HashMap<>();

    /** Entidades del mod que invoca el escenario smoke. */
    private static final String[] MOB_IDS = {"kein_loree", "droid", "loree_commander", "loree_warlord", "cargo_ship"};

    static {
        register("smoke", s -> {
            s.wait(100);
            s.cmd("keinloree story status");
            for (String id : MOB_IDS) {
                s.cmd("summon keinloree:" + id + " ~ ~ ~");
            }
            s.wait(400);
            for (String id : MOB_IDS) {
                s.cmd("kill @e[type=keinloree:" + id + "]");
            }
            s.wait(20);
            s.stop();
        });
    }

    private final List<Step> steps;
    private int index;
    private int wait;

    private AutoTest(List<Step> steps) {
        this.steps = steps;
    }

    /** Registra un escenario de servidor. */
    public static void register(String name, Consumer<Script> definition) {
        SCENARIOS.put(name, definition);
    }

    /** Se llama al construir el mod; solo engancha eventos si hay escenario. */
    public static void init() {
        String name = System.getProperty("keinloree.servertest");
        if (name == null || name.isBlank()) {
            name = fromFile();
        }
        if (name == null || name.isBlank()) {
            return;
        }
        Consumer<Script> definition = SCENARIOS.get(name.trim());
        if (definition == null) {
            LOGGER.error("[AutoTest] Escenario de servidor desconocido: {} (hay {})", name, SCENARIOS.keySet());
            return;
        }
        Script script = new Script();
        definition.accept(script);
        LOGGER.info("[AutoTest] Escenario de servidor '{}' con {} pasos", name.trim(), script.steps.size());
        MinecraftForge.EVENT_BUS.register(new AutoTest(script.steps));
    }

    private static String fromFile() {
        try {
            Path file = Path.of("kl_servertest.txt");
            return Files.exists(file) ? Files.readString(file).trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || index >= steps.size()) {
            return;
        }
        if (wait > 0) {
            wait--;
            return;
        }
        MinecraftServer server = event.getServer();
        // Ejecuta pasos hasta encontrar uno que espere
        while (index < steps.size() && wait == 0) {
            Step step = steps.get(index++);
            try {
                wait = step.run(server);
            } catch (Exception e) {
                LOGGER.error("[AutoTest] Fallo en el paso {}", index, e);
            }
        }
    }

    /** Un paso devuelve los ticks que hay que esperar antes del siguiente. */
    @FunctionalInterface
    private interface Step {
        int run(MinecraftServer server);
    }

    /** Constructor de escenarios: acciones en orden. */
    public static final class Script {
        private final List<Step> steps = new ArrayList<>();

        /** Espera ticks. */
        public Script wait(int ticks) {
            steps.add(server -> ticks);
            return this;
        }

        /** Ejecuta un comando como consola. */
        public Script cmd(String command) {
            steps.add(server -> {
                LOGGER.info("[AutoTest] /{}", command);
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
                return 0;
            });
            return this;
        }

        /** Accion libre con acceso al servidor. */
        public Script run(Consumer<MinecraftServer> action) {
            steps.add(server -> {
                action.accept(server);
                return 0;
            });
            return this;
        }

        /** Apaga el servidor. */
        public Script stop() {
            steps.add(server -> {
                LOGGER.info("[AutoTest] Fin del escenario, parando el servidor");
                server.halt(false);
                return 0;
            });
            return this;
        }
    }
}
