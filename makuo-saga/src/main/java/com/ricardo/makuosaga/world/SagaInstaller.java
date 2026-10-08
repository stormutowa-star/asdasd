package com.ricardo.makuosaga.world;

import com.mojang.logging.LogUtils;
import com.ricardo.makuosaga.MakuoSaga;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * DragonMineZ reads sagas from &lt;world&gt;/dragonminez/{sagas,quests}. This copies the Makuo saga there
 * before DragonMineZ loads them (same event, higher priority). Files are only replaced when the addon ships
 * a new story version; the previous copy is kept in dragonminez/oldBackup/makuosaga.
 */
@Mod.EventBusSubscriber(modid = MakuoSaga.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SagaInstaller {
	public static final String STORY_VERSION = "1";
	private static final String ROOT = "/makuosaga_story/";
	private static final Logger LOGGER = LogUtils.getLogger();

	private SagaInstaller() {
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onServerStarting(ServerStartingEvent event) {
		Path dmzDir = event.getServer().getWorldPath(LevelResource.ROOT).resolve("dragonminez");
		try {
			install(dmzDir);
		} catch (IOException e) {
			LOGGER.error("[Makuo Saga] Could not install the saga files into {}", dmzDir, e);
		}
	}

	private static void install(Path dmzDir) throws IOException {
		Path marker = dmzDir.resolve("makuosaga_version.txt");
		String installed = Files.exists(marker) ? Files.readString(marker, StandardCharsets.UTF_8).trim() : "";
		boolean upgrade = !STORY_VERSION.equals(installed);

		int written = 0;
		for (String relative : readIndex()) {
			Path target = dmzDir.resolve(relative);
			if (Files.exists(target) && !upgrade) {
				continue;
			}
			if (Files.exists(target)) {
				Path backup = dmzDir.resolve("oldBackup").resolve(MakuoSaga.MOD_ID).resolve(relative);
				Files.createDirectories(backup.getParent());
				Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);
			}
			try (InputStream in = open(relative)) {
				Files.createDirectories(target.getParent());
				Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
				written++;
			}
		}
		Files.createDirectories(dmzDir);
		Files.writeString(marker, STORY_VERSION, StandardCharsets.UTF_8);
		if (written > 0) {
			LOGGER.info("[Makuo Saga] Installed {} saga file(s) into {}", written, dmzDir);
		}
	}

	private static List<String> readIndex() throws IOException {
		List<String> files = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(open("index.txt"), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isBlank()) files.add(line.trim());
			}
		}
		return files;
	}

	private static InputStream open(String relative) throws IOException {
		InputStream in = SagaInstaller.class.getResourceAsStream(ROOT + relative);
		if (in == null) throw new IOException("Missing bundled saga file " + relative);
		return in;
	}
}
