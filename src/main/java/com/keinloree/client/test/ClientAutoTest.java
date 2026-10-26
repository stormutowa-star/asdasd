package com.keinloree.client.test;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

/**
 * Banco de pruebas del cliente. No hace nada salvo que se arranque con
 * -Dkeinloree.autotest=escenario (o con el archivo run/kl_autotest.txt).
 * Abre (o crea) el mundo "kltest" y ejecuta los pasos del escenario.
 */
public final class ClientAutoTest {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String WORLD = "kltest";
    private static final long SEED = 20251025L;
    private static final Map<String, Consumer<Script>> SCENARIOS = new HashMap<>();

    static {
        register("basic", s -> s.wait(200).cmd("time set noon").wait(40).shot("basic").wait(20).quit());
        BbTestRender.registerScenario();
        StoryScenarios.registerAll();
        com.keinloree.client.vfx.VfxScenario.register();
        ShipPreview.registerScenario();
    }

    private final List<Step> steps;
    private final String name;
    private boolean worldRequested;
    private int sinceRequest;
    private int index;
    private int wait;
    private int ticksInWorld;
    private boolean finished;

    private ClientAutoTest(String name, List<Step> steps) {
        this.name = name;
        this.steps = steps;
    }

    /** Registra un escenario de cliente (otros paquetes anaden los suyos). */
    public static void register(String name, Consumer<Script> definition) {
        SCENARIOS.put(name, definition);
    }

    /** Se llama desde el registro de cliente; solo engancha eventos si hay escenario. */
    public static void init() {
        String name = System.getProperty("keinloree.autotest");
        if (name == null || name.isBlank()) {
            name = fromFile();
        }
        if (name == null || name.isBlank()) {
            return;
        }
        Consumer<Script> definition = SCENARIOS.get(name.trim());
        if (definition == null) {
            LOGGER.error("[ClientAutoTest] Escenario desconocido: {} (hay {})", name, SCENARIOS.keySet());
            return;
        }
        Script script = new Script();
        definition.accept(script);
        LOGGER.info("[ClientAutoTest] Escenario '{}' con {} pasos", name.trim(), script.steps.size());
        MinecraftForge.EVENT_BUS.register(new ClientAutoTest(name.trim(), script.steps));
    }

    private static String fromFile() {
        try {
            Path file = Minecraft.getInstance().gameDirectory.toPath().resolve("kl_autotest.txt");
            return Files.exists(file) ? Files.readString(file).trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || finished) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            enterWorld(mc);
            return;
        }
        // Dentro del mundo: espera a que cierre la pantalla de carga
        if (mc.screen != null) {
            return;
        }
        ticksInWorld++;
        if (wait > 0) {
            wait--;
            return;
        }
        while (index < steps.size() && wait == 0 && !finished) {
            Step step = steps.get(index++);
            try {
                wait = step.run(mc);
            } catch (Exception e) {
                LOGGER.error("[ClientAutoTest] Fallo en el paso {}", index, e);
            }
        }
    }

    /** Pantalla de titulo: abre o crea el mundo de pruebas. */
    private void enterWorld(Minecraft mc) {
        if (mc.getOverlay() != null) {
            return;
        }
        if (mc.screen instanceof AccessibilityOnboardingScreen) {
            mc.setScreen(new TitleScreen());
            return;
        }
        if (worldRequested) {
            // Si tras mucho tiempo sigue sin mundo, informa una vez
            if (++sinceRequest == 6000) {
                LOGGER.error("[ClientAutoTest] El mundo '{}' no ha cargado tras 5 minutos", WORLD);
            }
            return;
        }
        if (!(mc.screen instanceof TitleScreen)) {
            return;
        }
        worldRequested = true;
        if (mc.getLevelSource().levelExists(WORLD)) {
            LOGGER.info("[ClientAutoTest] Abriendo el mundo existente '{}'", WORLD);
            mc.createWorldOpenFlows().openWorld(WORLD, () -> mc.setScreen(new TitleScreen()));
        } else {
            LOGGER.info("[ClientAutoTest] Creando el mundo '{}' (creativo, semilla {})", WORLD, SEED);
            LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.NORMAL, true,
                    new GameRules(), WorldDataConfiguration.DEFAULT);
            mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(SEED, true, false),
                    WorldPresets::createNormalWorldDimensions, new TitleScreen());
        }
    }

    /** Un paso devuelve los ticks que hay que esperar antes del siguiente. */
    @FunctionalInterface
    private interface Step {
        int run(Minecraft mc);
    }

    /** Constructor de escenarios: acciones en orden. */
    public static final class Script {
        private final List<Step> steps = new ArrayList<>();

        /** Espera ticks. */
        public Script wait(int ticks) {
            steps.add(mc -> ticks);
            return this;
        }

        /** Envia un comando (sin barra) como el jugador. */
        public Script cmd(String command) {
            steps.add(mc -> {
                LOGGER.info("[ClientAutoTest] /{}", command);
                mc.player.connection.sendCommand(command);
                return 0;
            });
            return this;
        }

        /** Orienta la camara. */
        public Script look(float yaw, float pitch) {
            steps.add(mc -> {
                mc.player.setYRot(yaw);
                mc.player.setXRot(pitch);
                mc.player.setYHeadRot(yaw);
                return 0;
            });
            return this;
        }

        /** Teletransporta al jugador con /tp. */
        public Script tp(double x, double y, double z) {
            return cmd("tp @s " + x + " " + y + " " + z);
        }

        /** Muestra u oculta el HUD. */
        public Script hideHud(boolean hide) {
            steps.add(mc -> {
                mc.options.hideGui = hide;
                return 0;
            });
            return this;
        }

        /** Captura a run/screenshots/nombre.png. */
        public Script shot(String shotName) {
            steps.add(mc -> {
                File dir = mc.gameDirectory;
                LOGGER.info("[ClientAutoTest] Captura {}.png", shotName);
                Screenshot.grab(dir, shotName + ".png", mc.getMainRenderTarget(), msg -> { });
                return 0;
            });
            return this;
        }

        /** Accion libre con acceso al cliente. */
        public Script run(Consumer<Minecraft> action) {
            steps.add(mc -> {
                action.accept(mc);
                return 0;
            });
            return this;
        }

        /** Cierra el juego. */
        public Script quit() {
            steps.add(mc -> {
                LOGGER.info("[ClientAutoTest] Fin del escenario, cerrando el cliente");
                mc.stop();
                return 0;
            });
            return this;
        }
    }
}
