package com.ricardo.makuosaga.world;

import com.dragonminez.common.config.ConfigManager;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Installs the addon's DragonMineZ form groups (config/dragonminez/races/&lt;race&gt;/forms) the first time,
 * then reloads DragonMineZ's config so they are available right away. Existing files are never overwritten,
 * so players can tweak them like any other DragonMineZ form.
 */
public final class FormInstaller {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String[][] FORMS = {
			{"saiyan", "orangesaiyan.json"},
	};

	private FormInstaller() {
	}

	public static void install() {
		Path racesDir = FMLPaths.CONFIGDIR.get().resolve("dragonminez").resolve("races");
		boolean installed = false;
		for (String[] form : FORMS) {
			Path target = racesDir.resolve(form[0]).resolve("forms").resolve(form[1]);
			if (Files.exists(target)) continue;
			try (InputStream in = FormInstaller.class.getResourceAsStream("/makuosaga_forms/" + form[1])) {
				if (in == null) throw new IOException("missing bundled form " + form[1]);
				Files.createDirectories(target.getParent());
				Files.copy(in, target);
				installed = true;
				LOGGER.info("[Makuo Saga] Installed form group {}", target);
			} catch (IOException e) {
				LOGGER.error("[Makuo Saga] Could not install form group {}", target, e);
			}
		}
		if (installed) {
			ConfigManager.reload();
		}
	}
}
