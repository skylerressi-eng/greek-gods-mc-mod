package com.olympus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.olympus.Olympus;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Loads {@code config/olympus.json}. Missing keys are filled with defaults and written back,
 * so new options appear automatically after updates.
 */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static OlympusConfig config = new OlympusConfig();
	/** The server's config as seen by this client (synced on join); used only on the client thread. */
	private static volatile OlympusConfig clientView;
	private static volatile Thread clientThread;

	private ConfigManager() {
	}

	/**
	 * The active config. On the client's render thread this is the server's synced copy (so
	 * tooltips and prediction match the server); everywhere else it is the local file.
	 */
	public static OlympusConfig get() {
		OlympusConfig view = clientView;
		if (view != null && Thread.currentThread() == clientThread) {
			return view;
		}
		return config;
	}

	public static String toJson() {
		return GSON.toJson(config);
	}

	/** Client only: install the config the server sent us. */
	public static void setClientView(String json, Thread renderThread) {
		try {
			OlympusConfig parsed = GSON.fromJson(json, OlympusConfig.class);
			if (parsed != null) {
				fillMissing(parsed);
				clientView = parsed;
				clientThread = renderThread;
			}
		} catch (Exception e) {
			Olympus.LOGGER.warn("Could not read the server's Olympus config: {}", e.getMessage());
		}
	}

	public static void clearClientView() {
		clientView = null;
	}

	public static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve(Olympus.MOD_ID + ".json");
	}

	/** @return null on success, otherwise an error message (the previous config is kept). */
	public static String load() {
		Path path = path();
		OlympusConfig loaded = new OlympusConfig();
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				OlympusConfig parsed = GSON.fromJson(reader, OlympusConfig.class);
				if (parsed != null) {
					loaded = parsed;
				}
			} catch (Exception e) {
				Olympus.LOGGER.error("Could not read {}: {}. Keeping previous values.", path, e.getMessage());
				return e.getMessage();
			}
		}
		fillMissing(loaded);
		config = loaded;
		save();
		return null;
	}

	public static void save() {
		Path path = path();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			Olympus.LOGGER.error("Could not write {}", path, e);
		}
	}

	/** Gson leaves whole sections null if they are absent from an old file. */
	private static void fillMissing(OlympusConfig c) {
		OlympusConfig d = new OlympusConfig();
		if (c.general == null) c.general = d.general;
		if (c.loot == null) c.loot = d.loot;
		if (c.upgrades == null) c.upgrades = d.upgrades;
		if (c.lightning == null) c.lightning = d.lightning;
		if (c.aegis == null) c.aegis = d.aegis;
		if (c.conch == null) c.conch = d.conch;
		if (c.helmOfDarkness == null) c.helmOfDarkness = d.helmOfDarkness;
		if (c.owl == null) c.owl = d.owl;
		if (c.bladeOfAres == null) c.bladeOfAres = d.bladeOfAres;
		if (c.lyre == null) c.lyre = d.lyre;
		if (c.silverBow == null) c.silverBow = d.silverBow;
		if (c.talaria == null) c.talaria = d.talaria;
		if (c.hammerOfHephaestus == null) c.hammerOfHephaestus = d.hammerOfHephaestus;
		if (c.sickle == null) c.sickle = d.sickle;
		if (c.goblet == null) c.goblet = d.goblet;
		if (c.mirror == null) c.mirror = d.mirror;
		if (c.goblet.effectPool == null || c.goblet.effectPool.isEmpty()) c.goblet.effectPool = d.goblet.effectPool;
		if (c.general.starterTier == null) c.general.starterTier = d.general.starterTier;
	}
}
