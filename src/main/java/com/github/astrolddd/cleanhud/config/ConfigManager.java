package com.github.astrolddd.cleanhud.config;

import com.github.astrolddd.cleanhud.CleanHud;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
		.getConfigDir()
		.resolve(CleanHud.MOD_ID + ".json");

	private static ArmorHudConfig config = new ArmorHudConfig();

	private ConfigManager() {
	}

	public static ArmorHudConfig get() {
		config.validate();
		return config;
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			ArmorHudConfig loaded = GSON.fromJson(reader, ArmorHudConfig.class);
			if (loaded != null) {
				config = loaded;
				config.validate();
			}
		} catch (IOException exception) {
			CleanHud.LOGGER.error("Failed to load CleanHud config", exception);
		}
	}

	public static void save() {
		try {
			config.validate();
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException exception) {
			CleanHud.LOGGER.error("Failed to save CleanHud config", exception);
		}
	}
}



