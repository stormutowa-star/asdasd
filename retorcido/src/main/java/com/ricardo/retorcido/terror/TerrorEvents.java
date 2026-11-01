package com.ricardo.retorcido.terror;

import com.mojang.logging.LogUtils;
import com.ricardo.retorcido.RetorcidoEntity;
import com.ricardo.retorcido.RetorcidoMod;
import com.ricardo.retorcido.amigo.AmigoEntity;
import com.ricardo.retorcido.amigo.AmigoManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/** Sucesos raros del director. Cada uno devuelve true si se hizo. Solo servidor. */
public final class TerrorEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final List<String> NAMES = List.of("pasos", "susurro", "puerta", "antorcha", "oscuridad",
            "observador", "dani_dice", "dani_mira", "secuestro", "cazador", "regreso");

    private static final String[] WHISPER_SOUNDS = {
            "ambient.cave", "ambient.soul_sand_valley.mood", "ambient.crimson_forest.mood",
            "ambient.warped_forest.mood", "ambient.nether_wastes.mood", "entity.warden.nearby_close",
    };

    private TerrorEvents() {}

    /** Sucesos que el director puede lanzar solo en la fase dada. */
    public static List<String> pool(int phase) {
        List<String> l = new ArrayList<>(List.of("pasos", "susurro", "puerta", "antorcha", "dani_dice"));
        if (phase >= 2) l.addAll(List.of("observador", "oscuridad", "dani_mira"));
        if (phase >= 3) l.add("observador"); // mas peso al observador
        return l;
    }

    /** Ejecuta un suceso por nombre sin dejar escapar excepciones. */
    public static boolean run(String name, ServerPlayer p, int phase) {
        try {
            return switch (name) {
                case "pasos" -> pasos(p);
                case "susurro" -> susurro(p);
                case "puerta" -> puerta(p);
                case "antorcha" -> antorcha(p);
                case "oscuridad" -> oscuridad(p);
                case "observador" -> observador(p, phase);
                case "dani_dice" -> daniDice(p, phase);
                case "dani_mira" -> daniMira(p);
                case "secuestro" -> TerrorDirector.startKidnap(p, true);
                case "cazador" -> spawnHunter(p);
                case "regreso" -> regreso(p);
                default -> false;
            };
        } catch (Exception e) {
            LOGGER.warn("[retorcido] fallo en el suceso '{}'", name, e);
            return false;
        }
    }

    // ------------------------------------------------------------------ utilidades

    static SoundEvent snd(String path) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.withDefaultNamespace(path));
    }

    private static void play(ServerLevel level, double x, double y, double z, SoundEvent s, SoundSource src, float vol, float pitch) {
        if (s != null) level.playSound(null, x, y, z, s, src, vol, pitch);
    }

    /** Punto de pie valido a min..max bloques del jugador (12 intentos), o null. */
    public static Vec3 findSpot(ServerLevel level, ServerPlayer p, double min, double max, boolean behind, EntityDimensions dims) {
        RandomSource rnd = p.getRandom();
        double yaw = Math.toRadians(p.getYRot());
        double backAngle = Math.atan2(-Math.cos(yaw), Math.sin(yaw)); // direccion opuesta a la mirada
        for (int i = 0; i < TerrorConfig.SPAWN_ATTEMPTS; i++) {
            double ang = behind ? backAngle + (rnd.nextDouble() - 0.5D) * Math.toRadians(90.0D) : rnd.nextDouble() * Math.PI * 2.0D;
            double d = min + rnd.nextDouble() * (max - min);
            double x = p.getX() + Math.cos(ang) * d;
            double z = p.getZ() + Math.sin(ang) * d;
            if (!level.hasChunkAt(BlockPos.containing(x, p.getY(), z))) continue;
            for (int dy = 12; dy >= -12; dy--) {
                BlockPos below = BlockPos.containing(x, p.getY() + dy - 1, z);
                BlockState bs = level.getBlockState(below);
                if (!bs.blocksMotion() || bs.is(BlockTags.LEAVES)) continue;
                double y = below.getY() + 1;
                if (level.noCollision(dims.makeBoundingBox(x, y, z))
                        && level.getFluidState(below.above()).isEmpty()
                        && level.getFluidState(below.above(2)).isEmpty()) {
                    return new Vec3(x, y, z);
                }
            }
        }
        return null;
    }

    private static List<BlockPos> collect(ServerLevel level, BlockPos center, int r, Predicate<BlockState> test) {
        List<BlockPos> out = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            if (test.test(level.getBlockState(pos))) out.add(pos.immutable());
        }
        return out;
    }

    private static void heartbeats(ServerPlayer p, int count) {
        for (int i = 0; i < count; i++) {
            float t = count <= 1 ? 1.0F : (float) i / (count - 1);
            float vol = TerrorConfig.HEARTBEAT_VOLUME_START + (TerrorConfig.HEARTBEAT_VOLUME_END - TerrorConfig.HEARTBEAT_VOLUME_START) * t;
            TerrorDirector.schedule(p, i * TerrorConfig.HEARTBEAT_INTERVAL, pl ->
                    pl.serverLevel().playSound(null, pl.getX(), pl.getY(), pl.getZ(), SoundEvents.WARDEN_HEARTBEAT,
                            SoundSource.HOSTILE, vol, 1.0F));
        }
    }

    /** Oscuridad + latidos (la usa tambien el secuestro). */
    public static void darknessAndHeartbeats(ServerPlayer p, int ticks) {
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, ticks, 0, false, false));
        RandomSource rnd = p.getRandom();
        heartbeats(p, TerrorConfig.HEARTBEAT_MIN_COUNT
                + rnd.nextInt(TerrorConfig.HEARTBEAT_MAX_COUNT - TerrorConfig.HEARTBEAT_MIN_COUNT + 1));
    }

    private static void daniSay(ServerPlayer p, AmigoEntity a, String[] list) {
        TerrorDirector.PlayerState st = TerrorDirector.stateOf(p);
        st.lastLine = TerrorLines.pick(list, st.lastLine, p.getRandom());
        a.say(st.lastLine);
    }

    // ------------------------------------------------------------------ F1+

    public static boolean pasos(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        BlockState below = level.getBlockState(p.blockPosition().below());
        SoundEvent step = below.isAir() ? SoundEvents.STONE_STEP : below.getSoundType().getStepSound();
        double yaw = Math.toRadians(p.getYRot());
        double bx = Math.sin(yaw);
        double bz = -Math.cos(yaw);
        RandomSource rnd = p.getRandom();
        int n = TerrorConfig.STEPS_MIN_COUNT + rnd.nextInt(TerrorConfig.STEPS_MAX_COUNT - TerrorConfig.STEPS_MIN_COUNT + 1);
        for (int i = 0; i < n; i++) {
            double d = n <= 1 ? TerrorConfig.STEPS_DIST_FAR
                    : TerrorConfig.STEPS_DIST_FAR + (TerrorConfig.STEPS_DIST_NEAR - TerrorConfig.STEPS_DIST_FAR) * i / (n - 1);
            float pitch = TerrorConfig.STEPS_PITCH + rnd.nextFloat() * 0.1F;
            TerrorDirector.schedule(p, i * TerrorConfig.STEPS_INTERVAL, pl ->
                    pl.serverLevel().playSound(null, pl.getX() + bx * d, pl.getY(), pl.getZ() + bz * d, step,
                            SoundSource.HOSTILE, TerrorConfig.STEPS_VOLUME, pitch));
        }
        return true;
    }

    public static boolean susurro(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        RandomSource rnd = p.getRandom();
        SoundEvent s = snd(WHISPER_SOUNDS[rnd.nextInt(WHISPER_SOUNDS.length)]);
        if (s == null) return false;
        double ang = rnd.nextDouble() * Math.PI * 2.0D;
        double d = TerrorConfig.WHISPER_MIN_DIST + rnd.nextDouble() * (TerrorConfig.WHISPER_MAX_DIST - TerrorConfig.WHISPER_MIN_DIST);
        float pitch = TerrorConfig.WHISPER_PITCH_MIN + rnd.nextFloat() * (TerrorConfig.WHISPER_PITCH_MAX - TerrorConfig.WHISPER_PITCH_MIN);
        play(level, p.getX() + Math.cos(ang) * d, p.getY(), p.getZ() + Math.sin(ang) * d, s, SoundSource.AMBIENT,
                TerrorConfig.WHISPER_VOLUME, pitch);
        return true;
    }

    public static boolean puerta(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        List<BlockPos> list = collect(level, p.blockPosition(), TerrorConfig.DOOR_RADIUS, s ->
                (s.getBlock() instanceof DoorBlock && s.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
                        || s.getBlock() instanceof TrapDoorBlock || s.getBlock() instanceof FenceGateBlock);
        if (list.isEmpty()) return false;
        BlockPos pos = list.get(p.getRandom().nextInt(list.size()));
        BlockState st = level.getBlockState(pos);
        Block b = st.getBlock();
        boolean open = st.getValue(BlockStateProperties.OPEN);
        level.setBlock(pos, st.setValue(BlockStateProperties.OPEN, !open), 10);
        String sound;
        if (b instanceof DoorBlock) {
            BlockPos up = pos.above();
            BlockState us = level.getBlockState(up);
            if (us.is(b) && us.hasProperty(BlockStateProperties.OPEN)) level.setBlock(up, us.setValue(BlockStateProperties.OPEN, !open), 10);
            sound = (b == Blocks.IRON_DOOR ? "block.iron_door." : "block.wooden_door.") + (open ? "close" : "open");
        } else if (b instanceof TrapDoorBlock) {
            sound = (b == Blocks.IRON_TRAPDOOR ? "block.iron_trapdoor." : "block.wooden_trapdoor.") + (open ? "close" : "open");
        } else {
            sound = "block.fence_gate." + (open ? "close" : "open");
        }
        play(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, snd(sound), SoundSource.BLOCKS, 1.0F, 0.9F);
        return true;
    }

    public static boolean antorcha(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        if (level.isDay() && level.canSeeSky(p.blockPosition())) return false; // de dia al aire libre no
        List<BlockPos> list = collect(level, p.blockPosition(), TerrorConfig.TORCH_RADIUS, s ->
                s.is(Blocks.TORCH) || s.is(Blocks.WALL_TORCH) || s.is(Blocks.SOUL_TORCH)
                        || s.is(Blocks.SOUL_WALL_TORCH) || s.is(Blocks.LANTERN) || s.is(Blocks.SOUL_LANTERN));
        if (list.isEmpty()) return false;
        BlockPos pos = list.get(p.getRandom().nextInt(list.size()));
        level.destroyBlock(pos, true);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.0F);
        return true;
    }

    public static boolean daniDice(ServerPlayer p, int phase) {
        AmigoEntity a = AmigoManager.findAmigo(p);
        if (a == null || a.isScripted() || a.getStareTicks() > 0 || a.level() != p.level()) return false;
        boolean sinister = phase >= 3 && p.getRandom().nextInt(10) < 7;
        daniSay(p, a, sinister ? TerrorLines.SINISTER : TerrorLines.UNEASY);
        return true;
    }

    // ------------------------------------------------------------------ F2+

    public static boolean oscuridad(ServerPlayer p) {
        darknessAndHeartbeats(p, TerrorConfig.DARKNESS_TICKS);
        return true;
    }

    public static boolean daniMira(ServerPlayer p) {
        AmigoEntity a = AmigoManager.findAmigo(p);
        if (a == null || a.isScripted() || a.getStareTicks() > 0 || a.level() != p.level()
                || a.distanceTo(p) > TerrorConfig.DANI_MAX_DIST) return false;
        TerrorDirector.PlayerState st = TerrorDirector.stateOf(p);
        if (st.script != null || st.daniLocked) return false;
        double ang = p.getRandom().nextDouble() * Math.PI * 2.0D;
        Vec3 target = new Vec3(a.getX() + Math.cos(ang) * TerrorConfig.DANI_STARE_DISTANCE, a.getEyeY(),
                a.getZ() + Math.sin(ang) * TerrorConfig.DANI_STARE_DISTANCE);
        st.daniLocked = true;
        a.setScripted(true);
        a.startStaring(target, TerrorConfig.DANI_STARE_TICKS);
        TerrorDirector.schedule(p, TerrorConfig.DANI_STARE_TICKS + 5, pl -> {
            TerrorDirector.stateOf(pl).daniLocked = false;
            AmigoEntity a2 = AmigoManager.findAmigo(pl);
            if (a2 != null) {
                a2.setScripted(false);
                a2.say(TerrorLines.STARE_END);
            }
        });
        return true;
    }

    public static boolean observador(ServerPlayer p, int phase) {
        ServerLevel level = p.serverLevel();
        if (TerrorDirector.watcherOf(level, p.getUUID()) != null) return false; // maximo uno
        boolean close = phase >= 3;
        EntityDimensions dims = RetorcidoMod.RETORCIDO.get().getDimensions();
        Vec3 spot = findSpot(level, p, close ? TerrorConfig.WATCHER_F3_MIN : TerrorConfig.WATCHER_F2_MIN,
                close ? TerrorConfig.WATCHER_F3_MAX : TerrorConfig.WATCHER_F2_MAX, close, dims);
        if (spot == null) return false;
        RetorcidoEntity w = RetorcidoMod.RETORCIDO.get().create(level);
        if (w == null) return false;
        w.moveTo(spot.x, spot.y, spot.z, p.getYRot() + 180.0F, 0.0F);
        w.makeWatcher(p.getUUID());
        level.addFreshEntity(w);
        TerrorDirector.setWatcher(p.getUUID(), w.getUUID());
        return true;
    }

    /** Llamado por el observador cuando el jugador lo ha mirado el tiempo suficiente. */
    public static void onWatcherSeen(ServerPlayer p) {
        try {
            if (p.getRandom().nextDouble() > TerrorConfig.DANI_SPEAK_AFTER_WATCHER_CHANCE) return;
            AmigoEntity a = AmigoManager.findAmigo(p);
            if (a == null || a.isScripted() || a.level() != p.level() || a.distanceTo(p) > TerrorConfig.DANI_SPEAK_RANGE) return;
            daniSay(p, a, TerrorLines.SINISTER);
        } catch (Exception e) {
            LOGGER.warn("[retorcido] fallo en onWatcherSeen", e);
        }
    }

    // ------------------------------------------------------------------ cazador y regreso

    public static boolean spawnHunter(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        EntityDimensions dims = RetorcidoMod.RETORCIDO.get().getDimensions();
        Vec3 spot = findSpot(level, p, TerrorConfig.HUNTER_MIN, TerrorConfig.HUNTER_MAX, true, dims);
        if (spot == null) return false;
        RetorcidoEntity h = RetorcidoMod.RETORCIDO.get().create(level);
        if (h == null) return false;
        h.moveTo(spot.x, spot.y, spot.z, p.getRandom().nextFloat() * 360.0F, 0.0F);
        h.addTag(TerrorConfig.HUNTER_TAG);
        h.addTag(TerrorConfig.HUNTER_TAG + "_of_" + p.getUUID());
        h.setPersistenceRequired();
        AttributeInstance follow = h.getAttribute(Attributes.FOLLOW_RANGE);
        if (follow != null) follow.setBaseValue(TerrorConfig.HUNTER_FOLLOW_RANGE);
        h.setTarget(p);
        level.addFreshEntity(h);
        return true;
    }

    public static boolean regreso(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        EntityDimensions dims = RetorcidoMod.AMIGO.get().getDimensions();
        Vec3 pos = findSpot(level, p, TerrorConfig.RETURN_MIN, TerrorConfig.RETURN_MAX, false, dims);
        if (pos == null) pos = AmigoManager.findStandPos(level, p.position(), dims, 7);
        AmigoEntity a = AmigoManager.findAmigo(p);
        if (a == null) {
            a = RetorcidoMod.AMIGO.get().create(level);
            if (a == null) return false;
            a.setOwnerUuid(p.getUUID());
            a.addTag(AmigoManager.ENTITY_TAG);
            a.moveTo(pos.x, pos.y, pos.z, p.getYRot() + 180.0F, 0.0F);
            level.addFreshEntity(a);
        } else {
            a.setScripted(false);
            a.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), p.getYRot() + 180.0F, 0.0F);
        }
        a.setMood(2);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("multiplayer.player.joined", a.getName().getString()).withStyle(ChatFormatting.YELLOW), false);
        TerrorData.PlayerData d = TerrorData.get(level.getServer()).get(p.getUUID());
        d.returned = true;
        d.daniRespawnAt = 0;
        TerrorData.get(level.getServer()).setDirty();
        TerrorDirector.schedule(p, TerrorConfig.RETURN_SAY_DELAY, pl -> {
            AmigoEntity a2 = AmigoManager.findAmigo(pl);
            if (a2 != null) a2.say(TerrorLines.RETURN);
        });
        return true;
    }

    /** Particulas de sangre + marcas, usadas en la escena del secuestro. */
    static void bloodBurst(ServerLevel level, Vec3 pos) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.NETHER_WART_BLOCK.defaultBlockState()),
                pos.x, pos.y + 1.0D, pos.z, 40, 0.4D, 0.7D, 0.4D, 0.1D);
    }
}
