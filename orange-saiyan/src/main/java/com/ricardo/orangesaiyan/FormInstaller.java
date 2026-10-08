package com.ricardo.orangesaiyan;

import com.dragonminez.common.config.ConfigManager;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Installs the Orange Saiyan form group into config/dragonminez/races/saiyan/forms the first time,
 * then reloads DragonMineZ's config so it is available right away. A file the player already has is kept,
 * except the one an earlier build placed (it pointed the crack texture at the Makuo saga addon).
 */
public final class FormInstaller {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String FILE = "orangesaiyan.json";
	private static final String OLD_TEXTURE = "makuosaga:textures/entity/forms/orange_cracks.png";

	private FormInstaller() {
	}

	public static void install() {
		Path target = FMLPaths.CONFIGDIR.get().resolve("dragonminez").resolve("races").resolve("saiyan").resolve("forms").resolve(FILE);
		try {
			if (Files.exists(target) && !Files.readString(target, StandardCharsets.UTF_8).contains(OLD_TEXTURE)) {
				return;
			}
			try (InputStream in = FormInstaller.class.getResourceAsStream("/orangesaiyan_forms/" + FILE)) {
				if (in == null) throw new IOException("missing bundled form " + FILE);
				Files.createDirectories(target.getParent());
				Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
			}
			LOGGER.info("[Orange Saiyan] Installed form group {}", target);
			ConfigManager.reload();
		} catch (IOException e) {
			LOGGER.error("[Orange Saiyan] Could not install form group {}", target, e);
		}
	}
}
