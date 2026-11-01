package com.ricardo.retorcido.amigo;

import com.mojang.brigadier.CommandDispatcher;
import com.ricardo.retorcido.RetorcidoMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/** Gestion del amigo: buscar, crear, asegurar; entrega al primer login y comando /amigo. */
@Mod.EventBusSubscriber(modid = RetorcidoMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AmigoManager {
    public static final String GIVEN_TAG = "retorcido_amigo_given";
    public static final String ENTITY_TAG = "retorcido_amigo";
    /** Valor clasico de Player.PERSISTED_NBT_TAG (no existe como constante en Forge 52). */
    public static final String PERSISTED_NBT_TAG = "PlayerPersisted";

    /** Jugadores pendientes de recibir su amigo -> ticks restantes. */
    private static final Map<UUID, Integer> PENDING = new HashMap<>();
    private static final Random RNG = new Random();

    private AmigoManager() {}

    /** Busca el amigo de este jugador en todos los niveles cargados. Null si no hay. */
    public static AmigoEntity findAmigo(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (e instanceof AmigoEntity a && a.isAlive() && player.getUUID().equals(a.getOwnerUuid())) return a;
            }
        }
        return null;
    }

    /** Crea un amigo junto al jugador (2-3 bloques). */
    public static AmigoEntity spawnAmigo(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        AmigoEntity amigo = RetorcidoMod.AMIGO.get().create(level);
        if (amigo == null) return null;
        Vec3 pos = findStandPos(level, player.position(), amigo.getType().getDimensions(), 3);
        amigo.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0.0F);
        amigo.setOwnerUuid(player.getUUID());
        amigo.addTag(ENTITY_TAG);
        level.addFreshEntity(amigo);
        return amigo;
    }

    /** Devuelve el amigo del jugador, creandolo si no existe. */
    public static AmigoEntity ensureAmigo(ServerPlayer player) {
        AmigoEntity a = findAmigo(player);
        return a != null ? a : spawnAmigo(player);
    }

    /** Hook: se llama (solo servidor) cuando un amigo muere. De momento no hace nada; el director puede ampliarlo. */
    public static void onAmigoDied(AmigoEntity amigo, DamageSource source) {
    }

    /** Busca un punto de pie (suelo solido, hueco libre) cerca de {@code center}; si no hay, devuelve center. */
    public static Vec3 findStandPos(ServerLevel level, Vec3 center, EntityDimensions dims, int radius) {
        for (int i = 0; i < 20; i++) {
            double x = center.x + (RNG.nextDouble() * 2 - 1) * radius;
            double z = center.z + (RNG.nextDouble() * 2 - 1) * radius;
            if (i < 12 && Math.abs(x - center.x) < 2 && Math.abs(z - center.z) < 2) continue; // 2-3 bloques
            for (int dy = 2; dy >= -4; dy--) {
                BlockPos below = BlockPos.containing(x, center.y + dy - 1, z);
                double y = below.getY() + 1;
                if (level.getBlockState(below).blocksMotion()
                        && level.noCollision(dims.makeBoundingBox(x, y, z))) {
                    return new Vec3(x, y, z);
                }
            }
        }
        return center;
    }

    // ------------------------------------------------------------------ eventos

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag persisted = player.getPersistentData().getCompound(PERSISTED_NBT_TAG);
        if (!persisted.getBoolean(GIVEN_TAG)) PENDING.put(player.getUUID(), 40);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) return;
        MinecraftServer server = event.getServer();
        Iterator<Map.Entry<UUID, Integer>> it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            if (entry.getValue() > 1) {
                entry.setValue(entry.getValue() - 1);
                continue;
            }
            it.remove();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) continue;
            ensureAmigo(player);
            CompoundTag data = player.getPersistentData();
            CompoundTag persisted = data.getCompound(PERSISTED_NBT_TAG);
            persisted.putBoolean(GIVEN_TAG, true);
            data.put(PERSISTED_NBT_TAG, persisted);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("amigo").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            AmigoEntity a = findAmigo(player);
            if (a == null) {
                spawnAmigo(player);
            } else {
                ServerLevel level = player.serverLevel();
                Vec3 pos = findStandPos(level, player.position(), a.getType().getDimensions(), 3);
                a.getNavigation().stop();
                a.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), player.getYRot(), 0.0F);
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Tu amigo está contigo."), false);
            return 1;
        }));
    }
}
