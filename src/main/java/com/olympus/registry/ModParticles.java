package com.olympus.registry;

import com.olympus.Olympus;
import com.olympus.god.God;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

/** One signature particle per god, plus a generic golden "divine glint". */
public final class ModParticles {
	/** name → type, in registration order (used by the client to register providers). */
	public static final Map<String, SimpleParticleType> ALL = new LinkedHashMap<>();
	private static final Map<God, SimpleParticleType> BY_GOD = new EnumMap<>(God.class);

	public static final SimpleParticleType DIVINE_GLINT = register("divine_glint");
	public static final SimpleParticleType STORM_SPARK = god(God.ZEUS, "storm_spark");
	public static final SimpleParticleType TIDE_DROP = god(God.POSEIDON, "tide_drop");
	public static final SimpleParticleType SHADE = god(God.HADES, "shade");
	public static final SimpleParticleType OWL_FEATHER = god(God.ATHENA, "owl_feather");
	public static final SimpleParticleType WAR_EMBER = god(God.ARES, "war_ember");
	public static final SimpleParticleType SUN_MOTE = god(God.APOLLO, "sun_mote");
	public static final SimpleParticleType MOONBEAM = god(God.ARTEMIS, "moonbeam");
	public static final SimpleParticleType WIND_WISP = god(God.HERMES, "wind_wisp");
	public static final SimpleParticleType FORGE_SPARK = god(God.HEPHAESTUS, "forge_spark");
	public static final SimpleParticleType GRAIN_LEAF = god(God.DEMETER, "grain_leaf");
	public static final SimpleParticleType WINE_DROP = god(God.DIONYSUS, "wine_drop");
	public static final SimpleParticleType ROSE_PETAL = god(God.APHRODITE, "rose_petal");
	public static final SimpleParticleType TITAN_DUST = god(God.TITAN, "titan_dust");

	private ModParticles() {
	}

	private static SimpleParticleType god(God god, String name) {
		SimpleParticleType type = register(name);
		BY_GOD.put(god, type);
		return type;
	}

	private static SimpleParticleType register(String name) {
		// alwaysShow = true so ability effects render even with "decreased" particle settings.
		SimpleParticleType type = Registry.register(BuiltInRegistries.PARTICLE_TYPE, Olympus.id(name), FabricParticleTypes.simple(true));
		ALL.put(name, type);
		return type;
	}

	public static SimpleParticleType forGod(God god) {
		return BY_GOD.getOrDefault(god, DIVINE_GLINT);
	}

	public static void init() {
	}
}
