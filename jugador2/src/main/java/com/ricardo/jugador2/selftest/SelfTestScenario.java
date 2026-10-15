package com.ricardo.jugador2.selftest;

import com.ricardo.jugador2.Jugador2Config;
import com.ricardo.jugador2.Jugador2Mod;
import com.ricardo.jugador2.actions.NpcActions;
import com.ricardo.jugador2.nav.Cell;
import com.ricardo.jugador2.nav.NpcNavigator;
import com.ricardo.jugador2.nav.WorldBlockAccess;
import com.ricardo.jugador2.npc.NpcManager;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
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

    /** Un paso: se prepara una vez y luego se comprueba cada tick hasta que cumple o agota el límite. */
    private record Step(String name, int timeoutTicks, Runnable setup, Predicate<MinecraftServer> check) {}

    private static boolean running;
    private static List<Step> steps;
    private static int stepIndex;
    private static int ticksInStep;
    private static Vec3 platformCenter;
    private static Vec3 deathPoint;
    private static NpcPlayer deadNpc;
    private static boolean droppedSeen;
    private static BlockPos walkGoal;
    private static BlockPos mineTarget;
    private static NpcActions miner;
    private static Zombie testZombie;

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
        steps.add(new Step("NPC en la lista de jugadores y de pie en la plataforma", 40,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    // Parte de un NPC limpio: el que se restaura al arrancar puede estar en el spawn del mundo.
                    manager.remove();
                    manager.spawn(manager.profile(), level, platformCenter);
                    manager.spawn(manager.profile(), level, platformCenter);
                },
                s -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    if (npc == null) {
                        return false;
                    }
                    long copies = s.getPlayerList().getPlayers().stream()
                            .filter(p -> p.getGameProfile().getName().equals(manager.name()))
                            .count();
                    return copies == 1 && s.getPlayerList().getPlayers().contains(npc) && npc.onGround();
                }));

        steps.add(new Step("camina 8 bloques por la ruta", 400,
                () -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    BlockPos start = npc.blockPosition();
                    walkGoal = start.offset(8, 0, 0);
                    List<Cell> path = NpcNavigator.findPath(new WorldBlockAccess(level),
                            new Cell(start.getX(), start.getY(), start.getZ()),
                            new Cell(walkGoal.getX(), walkGoal.getY(), walkGoal.getZ()),
                            Jugador2Config.read().pathSearchNodeLimit());
                    Jugador2Mod.manager().followPath(path == null ? List.of() : path);
                },
                s -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    double dx = npc.getX() - (walkGoal.getX() + 0.5);
                    double dz = npc.getZ() - (walkGoal.getZ() + 0.5);
                    return Math.sqrt(dx * dx + dz * dz) <= 0.6 && npc.onGround();
                }));

        steps.add(new Step("mina piedra con pico de hierro", 200,
                () -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    mineTarget = npc.blockPosition().east();
                    level.setBlock(mineTarget, Blocks.STONE.defaultBlockState(), 3);
                    npc.getInventory().clearContent();
                    npc.getInventory().setItem(0, new ItemStack(Items.IRON_PICKAXE));
                    miner = new NpcActions(npc);
                    miner.selectHotbarSlot(0);
                    miner.lookAt(Vec3.atCenterOf(mineTarget));
                    if (!miner.startMining(mineTarget)) {
                        throw new IllegalStateException("no hay nada que minar en " + mineTarget);
                    }
                },
                s -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    // El primer tick del paso es el del inicio del minado; el progreso avanza desde el siguiente.
                    if (ticksInStep > 0) {
                        miner.continueMining();
                    }
                    boolean mined = level.getBlockState(mineTarget).isAir();
                    boolean hasStone = npc.getInventory().countItem(Items.COBBLESTONE) > 0
                            || !level.getEntitiesOfClass(ItemEntity.class,
                                    AABB.ofSize(Vec3.atCenterOf(mineTarget), 3, 3, 3)).isEmpty();
                    return mined && hasStone;
                }));

        steps.add(new Step("coloca un bloque de piedra", 100,
                () -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    npc.getInventory().setItem(1, new ItemStack(Items.STONE, 4));
                    new NpcActions(npc).placeBlock(mineTarget, Direction.UP, 1);
                },
                s -> level.getBlockState(mineTarget).is(Blocks.STONE)));

        steps.add(new Step("come cuando tiene hambre", 100,
                () -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    npc.getFoodData().setFoodLevel(6);
                    npc.getInventory().setItem(2, new ItemStack(Items.BREAD, 1));
                    new NpcActions(npc).eat(2);
                },
                s -> Jugador2Mod.manager().npc().getFoodData().getFoodLevel() >= 7));

        steps.add(new Step("ataca a un zombi", 300,
                () -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    npc.getInventory().setItem(3, new ItemStack(Items.IRON_SWORD));
                    new NpcActions(npc).selectHotbarSlot(3);
                    testZombie = EntityType.ZOMBIE.create(level);
                    testZombie.setPos(npc.getX() - 2.5, npc.getY(), npc.getZ());
                    level.addFreshEntity(testZombie);
                },
                s -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    NpcActions combat = new NpcActions(npc);
                    combat.lookAt(testZombie.position());
                    if (npc.distanceTo(testZombie) <= 3.0) {
                        combat.attack(testZombie);
                    }
                    boolean hurt = testZombie.getHealth() < testZombie.getMaxHealth() || testZombie.isDeadOrDying();
                    if (hurt) {
                        testZombie.discard();
                    }
                    return hurt;
                }));

        steps.add(new Step("muere, suelta sus objetos y reaparece con el mismo UUID",
                Jugador2Config.read().respawnDelayTicks() + 40,
                () -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    npc.getInventory().add(new ItemStack(Items.COBBLESTONE, 3));
                    deathPoint = npc.position();
                    deadNpc = npc;
                    droppedSeen = false;
                    npc.kill();
                },
                s -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    // Los objetos se miran mientras el chunk sigue cargado: al irse el NPC, el chunk se descarga y los guarda.
                    droppedSeen |= !level.getEntitiesOfClass(ItemEntity.class, AABB.ofSize(deathPoint, 12, 12, 12)).isEmpty();
                    if (npc == null || npc == deadNpc) {
                        return false;
                    }
                    boolean sameUuid = npc.getUUID().equals(manager.profile().getId());
                    boolean inList = s.getPlayerList().getPlayers().contains(npc);
                    boolean emptyInventory = npc.getInventory().isEmpty();
                    return droppedSeen && sameUuid && inList && emptyInventory;
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
