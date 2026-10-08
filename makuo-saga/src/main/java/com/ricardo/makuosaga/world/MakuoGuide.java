package com.ricardo.makuosaga.world;

import com.dragonminez.common.quest.PlayerQuestData;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import com.dragonminez.server.world.structure.helper.StructureLocator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ricardo.makuosaga.MakuoSaga;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * DragonMineZ's tracked-quest HUD only lists objectives, so this tells players of the Makuo saga where to go:
 * an action-bar arrow with the distance to the current target, plus /makuo commands (where, travel, invincible).
 */
@Mod.EventBusSubscriber(modid = MakuoSaga.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MakuoGuide {
	private static final String SAGA = "makuo_saga";
	private static final int QUESTS = 15;
	private static final String GUIDE_OFF_TAG = "makuo_guia_off";
	private static final ResourceKey<Level> NAMEK = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("dragonminez", "namek"));
	private static final ResourceKey<Structure> GURU_HOUSE = ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("dragonminez", "elder_guru"));
	private static final BlockPos SAIEN = new BlockPos(0, 0, -2);
	private static final BlockPos CITADEL_GATE = new BlockPos(VharosBuilder.CITADEL_X, VharosBuilder.CITADEL_Y + 1, VharosBuilder.CITADEL_Z + 20);
	private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
	private static final Map<Long, BlockPos> GURU_CACHE = new HashMap<>();

	private MakuoGuide() {
	}

	/** What the player should do now: a short text and, optionally, a place in a given dimension to point at. */
	private record Step(int quest, boolean started, Component text, ResourceKey<Level> dimension, BlockPos target, String targetName) {
	}

	// ------------------------------------------------------------------ HUD

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 20 != 0) return;
		for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
			if (player.getTags().contains(GUIDE_OFF_TAG)) continue;
			Step step = currentStep(player);
			if (step == null || !step.started()) continue;
			player.displayClientMessage(actionBar(player, step), true);
		}
	}

	private static Component actionBar(ServerPlayer player, Step step) {
		MutableComponent line = Component.literal("Makuo ▸ ").withStyle(ChatFormatting.LIGHT_PURPLE);
		if (step.target() != null && player.level().dimension().equals(step.dimension())) {
			line.append(Component.literal(step.targetName() + " " + arrow(player, step.target()) + " ").withStyle(ChatFormatting.WHITE))
					.append(Component.literal(distance(player, step.target()) + " bloques").withStyle(ChatFormatting.GRAY));
		} else {
			line.append(step.text().copy().withStyle(ChatFormatting.WHITE));
		}
		return line;
	}

	// ------------------------------------------------------------------ steps

	/** First quest of the saga that is not completed yet, or null when the saga is finished. */
	private static Step currentStep(ServerPlayer player) {
		PlayerQuestData pqd = StatsProvider.get(StatsCapability.INSTANCE, player).map(d -> d.getPlayerQuestData()).orElse(null);
		if (pqd == null) return null;
		for (int q = 1; q <= QUESTS; q++) {
			String key = PlayerQuestData.sagaQuestKey(SAGA, q);
			if (pqd.isQuestCompleted(key)) continue;
			if (pqd.isQuestAccepted(key)) return stepFor(player, q, key);
			if (pqd.getQuestStatus(key) == PlayerQuestData.QuestStatus.FAILED) {
				return new Step(q, true, Component.literal("La misión falló: vuelve a iniciarla en el árbol de misiones (V)"), null, null, null);
			}
			// Between quests the HUD reminds you to start the next one; before the first, it stays quiet.
			return new Step(q, q > 1, startText(q), null, null, null);
		}
		return null;
	}

	private static Component startText(int quest) {
		return Component.literal("Abre el árbol de misiones (V) e inicia: ")
				.append(Component.translatable("dmz.quest.makuo" + quest + ".name"));
	}

	private static Step stepFor(ServerPlayer player, int q, String key) {
		ResourceKey<Level> here = player.level().dimension();
		boolean onNamek = here.equals(NAMEK);
		boolean onVharos = here.equals(VharosEvents.VHAROS);
		switch (q) {
			case 1:
				return text(q, "Viaja a Namek: nave espacial (tecla H) o /makuo ir namek");
			case 2:
			case 15:
				if (!onNamek) return text(q, "Viaja a Namek: nave espacial (tecla H) o /makuo ir namek");
				BlockPos guru = guruPos(player.server);
				if (guru == null) return text(q, "Busca al Gran Patriarca en su casa de Namek y haz clic derecho sobre él");
				return new Step(q, true, Component.literal("Habla con el Gran Patriarca (clic derecho)"), NAMEK, guru, "Gran Patriarca");
			case 3:
			case 4:
				if (!onNamek) return text(q, "Vuelve a Namek: los enemigos de esta misión están allí");
				return enemyStep(player, q, key, NAMEK);
			case 5:
				return new Step(q, true, itemsText(player), null, null, null);
			case 6:
				return text(q, "Viaja a Vharos: nave espacial (tecla H) o /makuo ir vharos");
			case 7:
				if (!onVharos) return text(q, "Viaja a Vharos: nave espacial (tecla H) o /makuo ir vharos");
				return new Step(q, true, Component.literal("Habla con Saien (clic derecho)"), VharosEvents.VHAROS, SAIEN, "Saien");
			case 12:
				if (!onVharos) return text(q, "Vuelve a Vharos: /makuo ir vharos");
				return new Step(q, true, Component.literal("Llega a la Ciudadela de Makuo"), VharosEvents.VHAROS, CITADEL_GATE, "Ciudadela");
			default:
				if (!onVharos) return text(q, "Vuelve a Vharos: los enemigos de esta misión están allí");
				return enemyStep(player, q, key, VharosEvents.VHAROS);
		}
	}

	private static Step text(int q, String text) {
		return new Step(q, true, Component.literal(text), null, null, null);
	}

	/** Points at the nearest enemy spawned for this quest; DragonMineZ tags them with the quest key. */
	private static Step enemyStep(ServerPlayer player, int q, String key, ResourceKey<Level> dimension) {
		Mob nearest = null;
		double best = Double.MAX_VALUE;
		AABB area = player.getBoundingBox().inflate(256.0D);
		for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, area,
				m -> key.equals(m.getPersistentData().getString("dmz_quest_key")))) {
			double d = mob.distanceToSqr(player);
			if (d < best) {
				best = d;
				nearest = mob;
			}
		}
		if (nearest == null) {
			return text(q, "Derrota a los enemigos de la misión (aparecen a tu alrededor al iniciarla)");
		}
		return new Step(q, true, Component.literal("Derrota a " + nearest.getDisplayName().getString()), dimension,
				nearest.blockPosition(), nearest.getDisplayName().getString());
	}

	private static Component itemsText(ServerPlayer player) {
		return Component.literal("Consigue: ")
				.append(count(player, Items.AMETHYST_SHARD, 16, "amatista")).append(" · ")
				.append(count(player, Items.IRON_INGOT, 12, "hierro")).append(" · ")
				.append(count(player, Items.GLOWSTONE_DUST, 8, "piedra luminosa"));
	}

	private static Component count(ServerPlayer player, Item item, int needed, String name) {
		int have = Math.min(needed, player.getInventory().countItem(item));
		return Component.literal(name + " " + have + "/" + needed)
				.withStyle(have >= needed ? ChatFormatting.GREEN : ChatFormatting.WHITE);
	}

	// ------------------------------------------------------------------ geometry

	private static BlockPos guruPos(MinecraftServer server) {
		ServerLevel namek = server.getLevel(NAMEK);
		if (namek == null) return null;
		long seed = namek.getSeed();
		if (!GURU_CACHE.containsKey(seed)) {
			GURU_CACHE.put(seed, StructureLocator.locateStructure(namek, GURU_HOUSE, BlockPos.ZERO));
		}
		return GURU_CACHE.get(seed);
	}

	private static int distance(ServerPlayer player, BlockPos target) {
		double dx = target.getX() + 0.5D - player.getX();
		double dz = target.getZ() + 0.5D - player.getZ();
		return (int) Math.sqrt(dx * dx + dz * dz);
	}

	/** Arrow relative to where the player is looking (yaw grows when turning right). */
	private static String arrow(ServerPlayer player, BlockPos target) {
		double dx = target.getX() + 0.5D - player.getX();
		double dz = target.getZ() + 0.5D - player.getZ();
		float targetYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		float relative = Mth.wrapDegrees(targetYaw - player.getYRot());
		int index = Math.floorMod(Math.round(relative / 45.0F), 8);
		return ARROWS[index];
	}

	private static String compass(ServerPlayer player, BlockPos target) {
		double dx = target.getX() + 0.5D - player.getX();
		double dz = target.getZ() + 0.5D - player.getZ();
		String[] names = {"sur", "suroeste", "oeste", "noroeste", "norte", "noreste", "este", "sureste"};
		float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		return names[Math.floorMod(Math.round(Mth.wrapDegrees(yaw) / 45.0F), 8)];
	}

	// ------------------------------------------------------------------ commands

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		register(event.getDispatcher());
	}

	private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("makuo")
				.then(Commands.literal("donde").executes(MakuoGuide::where))
				.then(Commands.literal("guia")
						.then(Commands.literal("on").executes(ctx -> guide(ctx, true)))
						.then(Commands.literal("off").executes(ctx -> guide(ctx, false))))
				.then(Commands.literal("invencible")
						.requires(MakuoGuide::allowed)
						.executes(MakuoGuide::toggleInvincible))
				.then(Commands.literal("ir")
						.requires(MakuoGuide::allowed)
						.then(Commands.literal("namek").executes(ctx -> travel(ctx, "namek")))
						.then(Commands.literal("tierra").executes(ctx -> travel(ctx, "tierra")))
						.then(Commands.literal("vharos").executes(ctx -> travel(ctx, "vharos")))));
	}

	/** Travel and invincibility are free in single player; on a server they need operator rights. */
	private static boolean allowed(CommandSourceStack source) {
		return source.hasPermission(2) || source.getServer().isSingleplayer();
	}

	private static int where(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		Step step = currentStep(player);
		if (step == null) {
			player.sendSystemMessage(Component.literal("Ya terminaste la Saga de Makuo (o aún no está desbloqueada: termina la Saga de Buu).")
					.withStyle(ChatFormatting.LIGHT_PURPLE));
			return 1;
		}
		player.sendSystemMessage(Component.literal("Saga de Makuo — misión " + step.quest() + ": ").withStyle(ChatFormatting.LIGHT_PURPLE)
				.append(Component.translatable("dmz.quest.makuo" + step.quest() + ".name").withStyle(ChatFormatting.GOLD)));
		player.sendSystemMessage(step.text());
		if (step.target() != null && player.level().dimension().equals(step.dimension())) {
			player.sendSystemMessage(Component.literal(step.targetName() + ": X " + step.target().getX() + ", Z " + step.target().getZ()
					+ " — " + distance(player, step.target()) + " bloques al " + compass(player, step.target())).withStyle(ChatFormatting.GRAY));
		}
		return 1;
	}

	private static int guide(CommandContext<CommandSourceStack> ctx, boolean on) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		if (on) player.removeTag(GUIDE_OFF_TAG);
		else player.addTag(GUIDE_OFF_TAG);
		player.sendSystemMessage(Component.literal(on ? "Guía de la Saga de Makuo activada." : "Guía de la Saga de Makuo desactivada.")
				.withStyle(ChatFormatting.LIGHT_PURPLE));
		return 1;
	}

	private static int toggleInvincible(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		boolean nowOn = !player.getTags().contains(InvincibleHandler.TAG);
		if (nowOn) player.addTag(InvincibleHandler.TAG);
		else player.removeTag(InvincibleHandler.TAG);
		player.sendSystemMessage(Component.literal(nowOn
				? "Modo invencible ACTIVADO: te golpean pero no pierdes vida (juega en supervivencia)."
				: "Modo invencible desactivado: vuelves a recibir daño.").withStyle(ChatFormatting.LIGHT_PURPLE));
		return 1;
	}

	private static int travel(CommandContext<CommandSourceStack> ctx, String where) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		MinecraftServer server = player.server;
		ServerLevel level = null;
		int x = 0, z = 0;
		switch (where) {
			case "namek" -> {
				level = server.getLevel(NAMEK);
				if (level == null) return fail(player, "No se encontró Namek.");
				BlockPos guru = guruPos(server);
				x = guru != null ? guru.getX() : 0;
				z = guru != null ? guru.getZ() + 24 : 0;
			}
			case "vharos" -> {
				boolean unlocked = StatsProvider.get(StatsCapability.INSTANCE, player)
						.map(d -> d.getPlayerQuestData().isQuestCompleted(PlayerQuestData.sagaQuestKey(SAGA, 5))).orElse(false);
				if (!unlocked && !player.isCreative()) {
					return fail(player, "Primero completa la misión 5, «Preparar el salto».");
				}
				level = server.getLevel(VharosEvents.VHAROS);
				if (level == null) return fail(player, "No se encontró Vharos.");
				x = 0;
				z = 7;
			}
			default -> {
				level = server.overworld();
				BlockPos spawn = level.getSharedSpawnPos();
				x = spawn.getX();
				z = spawn.getZ();
			}
		}
		level.getChunk(x >> 4, z >> 4);
		int y = Math.max(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), level.getSeaLevel()) + 1;
		player.teleportTo(level, x + 0.5D, y, z + 0.5D, player.getYRot(), player.getXRot());
		player.sendSystemMessage(Component.literal("Viajando a " + where + "…").withStyle(ChatFormatting.LIGHT_PURPLE));
		return 1;
	}

	private static int fail(ServerPlayer player, String message) {
		player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
		return 0;
	}
}
