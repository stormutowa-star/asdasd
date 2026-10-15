package com.ricardo.jugador2.selftest;

import com.mojang.authlib.GameProfile;
import com.ricardo.jugador2.Jugador2Config;
import com.ricardo.jugador2.Jugador2Mod;
import com.ricardo.jugador2.actions.NpcActions;
import com.ricardo.jugador2.ai.OrderState;
import com.ricardo.jugador2.ai.TaskResult;
import com.ricardo.jugador2.ai.WorldView;
import com.ricardo.jugador2.ai.tasks.CraftTask;
import com.ricardo.jugador2.ai.tasks.EatTask;
import com.ricardo.jugador2.ai.tasks.StoreTask;
import com.ricardo.jugador2.chat.Come;
import com.ricardo.jugador2.nav.Cell;
import com.ricardo.jugador2.nav.NpcNavigator;
import com.ricardo.jugador2.nav.WorldBlockAccess;
import com.ricardo.jugador2.npc.NpcManager;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Escenario automático en un servidor de desarrollo. Solo corre si la variable JUGADOR2_SELFTEST vale 1
 * o la propiedad jugador2.selftest es true. Imprime PASS o FAIL por paso y detiene el servidor.
 */
public final class SelfTestScenario {
    private static final String PREFIX = "[JUGADOR2-SELFTEST] ";
    /** Lado de la plataforma. Con 20 bloques el NPC se acorrala en el borde al huir y no llega a estar a 6 del zombi. */
    private static final int PLATFORM_SIZE = 40;

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
    private static NpcPlayer commander;
    private static NpcPlayer fleeing;
    private static CraftTask crafter;
    private static StoreTask storer;
    private static BlockPos chestPos;
    private static BlockPos bedPos;
    private static BlockPos leaflessLog;
    private static EatTask eater;

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
        // El cerebro se enciende en el paso 8: los pasos 1 a 7 comprueban solo el cuerpo y las acciones.
        Jugador2Mod.manager().setBrainEnabled(false);
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

        steps.add(new Step("el NPC va hacia quien le dio la orden", 400,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    placeOnPlatform(level, manager.npc());
                    commander = NpcPlayer.spawnNew(server, level, new GameProfile(UUID.randomUUID(), "Comandante"),
                            new Vec3(platformCenter.x - 9.0, platformCenter.y, platformCenter.z));
                    manager.setBrainEnabled(true);
                    manager.setOrder(new OrderState(new Come(), commander.getGameProfile().getName()));
                },
                s -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    return npc != null && npc.distanceTo(commander) <= 2.0;
                }));

        steps.add(new Step("defiende al NPC de un zombi cercano", 300,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    removeCommander(server);
                    discardZombie();
                    manager.setOrder(null);
                    testZombie = spawnZombie(level, npc.getX() + 4.0, npc.getY(), npc.getZ());
                },
                s -> testZombie.isDeadOrDying() || testZombie.getHealth() < testZombie.getMaxHealth()));

        steps.add(new Step("huye con poca vida y se aleja del zombi", 200,
                () -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    discardZombie();
                    placeOnPlatform(level, npc);
                    npc.setHealth(4.0F);
                    fleeing = npc;
                    testZombie = spawnZombie(level, platformCenter.x + 4.0, platformCenter.y, platformCenter.z);
                },
                s -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    return npc == fleeing && npc.isAlive() && npc.distanceTo(testZombie) >= 6.0;
                }));

        steps.add(new Step("recoge madera de dos troncos de un árbol cercano", 1200,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    discardZombie();
                    manager.setOrder(null);
                    // Tras huir el NPC puede estar al borde o abajo: sin plataforma no hay ruta a los troncos.
                    manager.followPath(List.of());
                    placeOnPlatform(level, npc);
                    npc.getInventory().clearContent();
                    for (int dz = 0; dz < 2; dz++) {
                        placeTreeLog(level, BlockPos.containing(platformCenter.x - 4.0, platformCenter.y, platformCenter.z + dz));
                    }
                    manager.setBrainEnabled(true);
                },
                s -> WorldView.count(Jugador2Mod.manager().npc().getInventory(), ItemTags.LOGS) >= 2));

        steps.add(new Step("craftea un pico de piedra con mesa, palos y piedra", 300,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    manager.setBrainEnabled(false);
                    manager.followPath(List.of());
                    npc.getInventory().clearContent();
                    npc.getInventory().setItem(0, new ItemStack(Items.COBBLESTONE, 3));
                    npc.getInventory().setItem(1, new ItemStack(Items.STICK, 2));
                    npc.getInventory().setItem(2, new ItemStack(Items.CRAFTING_TABLE, 1));
                    // La piedra solo sale con pico: el de madera ya lo tiene, como en una partida real.
                    npc.getInventory().setItem(3, new ItemStack(Items.WOODEN_PICKAXE, 1));
                    crafter = new CraftTask();
                },
                s -> crafter.tick(Jugador2Mod.manager().context()) == TaskResult.SUCCESS));

        steps.add(new Step("guarda la piedra en el cofre", 300,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    manager.followPath(List.of());
                    chestPos = BlockPos.containing(platformCenter.x - 4.0, platformCenter.y, platformCenter.z - 3.0);
                    level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
                    npc.getInventory().clearContent();
                    npc.getInventory().setItem(0, new ItemStack(Items.STONE, 40));
                    storer = new StoreTask();
                },
                s -> storer.tick(Jugador2Mod.manager().context()) == TaskResult.SUCCESS
                        && containerHas(level.getBlockEntity(chestPos), Items.STONE)));

        steps.add(new Step("obedece una orden dada por el chat", 400,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    manager.setOrder(null);
                    manager.setBrainEnabled(true);
                    // La orden llega como un mensaje de chat de Forge, igual que la de un jugador real.
                    String text = manager.name() + ", ve a 3 " + (int) platformCenter.y + " 3";
                    MinecraftForge.EVENT_BUS.post(new ServerChatEvent(npc, text, Component.literal(text)));
                },
                s -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    return npc != null && npc.position().distanceTo(new Vec3(3.5, platformCenter.y, 3.5)) <= 1.5;
                }));

        steps.add(new Step("sin pico, con tres troncos fabrica el de madera", 600,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    discardZombie();
                    manager.setOrder(null);
                    placeOnPlatform(level, npc);
                    npc.getInventory().clearContent();
                    npc.getInventory().setItem(0, new ItemStack(Items.OAK_LOG, 3));
                    manager.setBrainEnabled(true);
                },
                s -> WorldView.count(Jugador2Mod.manager().npc().getInventory(), ItemTags.PICKAXES) > 0));

        steps.add(new Step("muere y reaparece junto a su cama, no en el spawn del mundo",
                Jugador2Config.read().respawnDelayTicks() + 200,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    manager.setBrainEnabled(false);
                    manager.setOrder(null);
                    manager.followPath(List.of());
                    bedPos = BlockPos.containing(platformCenter.x + 4.0, platformCenter.y, platformCenter.z - 5.0);
                    placeBed(level, bedPos);
                    npc.setRespawnPosition(level.dimension(), bedPos, 0.0F, true, false);
                    deadNpc = npc;
                    npc.kill();
                },
                s -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    return npc != null && npc != deadNpc
                            && npc.position().distanceTo(Vec3.atBottomCenterOf(bedPos)) <= 2.0;
                }));

        steps.add(new Step("come la comida que lleva fuera de la barra", 200,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    manager.setBrainEnabled(false);
                    manager.followPath(List.of());
                    npc.getInventory().clearContent();
                    npc.getFoodData().setFoodLevel(6);
                    npc.getInventory().setItem(12, new ItemStack(Items.BREAD, 1));
                    eater = new EatTask();
                },
                s -> {
                    eater.tick(Jugador2Mod.manager().context());
                    return Jugador2Mod.manager().npc().getFoodData().getFoodLevel() >= 7;
                }));

        steps.add(new Step("no insiste en un tronco al que no llega y corta el siguiente", 600,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    manager.setOrder(null);
                    manager.followPath(List.of());
                    placeOnPlatform(level, npc);
                    npc.getInventory().clearContent();
                    // Más cerca que el árbol de verdad, pero en el aire y fuera de alcance desde el suelo: no hay ningún
                    // sitio desde el que alcanzarlo.
                    placeTreeLog(level, BlockPos.containing(platformCenter.x + 3.0, platformCenter.y + 3.0, platformCenter.z + 3.0));
                    placeTreeLog(level, BlockPos.containing(platformCenter.x - 6.0, platformCenter.y, platformCenter.z + 4.0));
                    manager.setBrainEnabled(true);
                },
                s -> WorldView.count(Jugador2Mod.manager().npc().getInventory(), ItemTags.LOGS) >= 1));

        steps.add(new Step("no corta un tronco sin hojas, de una construcción", 600,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    manager.setOrder(null);
                    manager.followPath(List.of());
                    placeOnPlatform(level, npc);
                    npc.getInventory().clearContent();
                    // Tronco sin hojas a 3 bloques del NPC, más cerca que el árbol: no es un árbol, así que no se toca.
                    leaflessLog = BlockPos.containing(platformCenter.x + 3.0, platformCenter.y, platformCenter.z);
                    level.setBlock(leaflessLog, Blocks.OAK_LOG.defaultBlockState(), 3);
                    placeTreeLog(level, BlockPos.containing(platformCenter.x - 6.0, platformCenter.y, platformCenter.z - 4.0));
                    manager.setBrainEnabled(true);
                },
                s -> WorldView.count(Jugador2Mod.manager().npc().getInventory(), ItemTags.LOGS) >= 1
                        && level.getBlockState(leaflessLog).is(BlockTags.LOGS)));

        steps.add(new Step("va a un punto en el aire y se queda de pie en el suelo de debajo", 400,
                () -> {
                    NpcManager manager = Jugador2Mod.manager();
                    NpcPlayer npc = manager.npc();
                    manager.setOrder(null);
                    manager.followPath(List.of());
                    placeOnPlatform(level, npc);
                    manager.setBrainEnabled(true);
                    String text = manager.name() + ", ve a 3 " + (int) (platformCenter.y + 6.0) + " 3";
                    MinecraftForge.EVENT_BUS.post(new ServerChatEvent(npc, text, Component.literal(text)));
                },
                s -> {
                    NpcPlayer npc = Jugador2Mod.manager().npc();
                    return npc != null && npc.position().distanceTo(new Vec3(3.5, platformCenter.y, 3.5)) <= 1.5;
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

    private static boolean containerHas(BlockEntity entity, Item item) {
        if (!(entity instanceof Container container)) {
            return false;
        }
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (container.getItem(slot).is(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tronco con hojas encima: lo que el NPC toma por un árbol. Las hojas van persistentes, o el juego las deshace al
     * azar: una hoja sin distancia calculada se quita en cuanto le toca un tick aleatorio, aunque tenga un tronco al lado.
     */
    private static void placeTreeLog(ServerLevel level, BlockPos log) {
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        level.setBlock(log, Blocks.OAK_LOG.defaultBlockState(), 3);
        level.setBlock(log.above(), leaves, 3);
        level.setBlock(log.above(2), leaves, 3);
    }

    /** Cama roja de dos mitades: el pie en foot y la cabeza al este. */
    private static void placeBed(ServerLevel level, BlockPos foot) {
        BlockState bed = Blocks.RED_BED.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.EAST);
        level.setBlock(foot, bed.setValue(BedBlock.PART, BedPart.FOOT), 3);
        level.setBlock(foot.east(), bed.setValue(BedBlock.PART, BedPart.HEAD), 3);
    }

    private static Zombie spawnZombie(ServerLevel level, double x, double y, double z) {
        Zombie zombie = EntityType.ZOMBIE.create(level);
        zombie.setPos(x, y, z);
        level.addFreshEntity(zombie);
        return zombie;
    }

    private static void discardZombie() {
        if (testZombie != null) {
            testZombie.discard();
            testZombie = null;
        }
    }

    private static void removeCommander(MinecraftServer server) {
        if (commander != null) {
            server.getPlayerList().remove(commander);
            commander = null;
        }
    }

    /** Vuelve a poner al NPC en la plataforma. Antes carga sus chunks: tras la muerte pudieron descargarse. */
    private static void placeOnPlatform(ServerLevel level, NpcPlayer npc) {
        loadArena(level);
        npc.teleportTo(level, platformCenter.x, platformCenter.y, platformCenter.z, 0.0F, 0.0F);
        level.getChunkSource().move(npc);
    }

    /**
     * Carga los chunks de la plataforma y de su margen. Sin ellos el mapa de alturas no es real: en un mundo nuevo
     * la plataforma salía enterrada, en una cueva, en vez de sobre el terreno como en un jugador real.
     */
    private static void loadArena(ServerLevel level) {
        int first = Math.floorDiv(-2, 16);
        int last = Math.floorDiv(PLATFORM_SIZE + 1, 16);
        for (int cx = first; cx <= last; cx++) {
            for (int cz = first; cz <= last; cz++) {
                level.getChunk(cx, cz);
            }
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

    /** Plataforma de piedra de PLATFORM_SIZE × PLATFORM_SIZE, con aire encima, varios bloques por encima del terreno. */
    private static void buildPlatform(ServerLevel level) {
        int x0 = 0;
        int z0 = 0;
        loadArena(level);
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, x0 + PLATFORM_SIZE / 2, z0 + PLATFORM_SIZE / 2);
        int y = surface + 5;
        // Un mundo que ya sirvió a una prueba anterior trae bloques de ella: se limpia el volumen entero primero.
        for (int x = x0 - 2; x < x0 + PLATFORM_SIZE + 2; x++) {
            for (int z = z0 - 2; z < z0 + PLATFORM_SIZE + 2; z++) {
                for (int dy = -3; dy < 8; dy++) {
                    level.setBlock(new BlockPos(x, y + dy, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        System.out.println(PREFIX + "PLATFORM y=" + y);
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
