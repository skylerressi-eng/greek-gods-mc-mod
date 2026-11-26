package com.olympus.god;

import com.olympus.registry.ModParticles;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/** The twelve Olympians, plus the Titan faction used by the Scythe of Kronos. */
public enum God {
	ZEUS(0x7FD4FF),
	POSEIDON(0x2EB8B0),
	HADES(0x8A5CC9),
	ATHENA(0xC9CED6),
	ARES(0xD13B2E),
	APOLLO(0xFFC93C),
	ARTEMIS(0xB8D8F0),
	HERMES(0xF0E6A8),
	HEPHAESTUS(0xFF7A1A),
	DEMETER(0x6CBF3A),
	DIONYSUS(0xA03CBF),
	APHRODITE(0xFF7AB6),
	TITAN(0xB08A3C);

	public static final God[] OLYMPIANS = {ZEUS, POSEIDON, HADES, ATHENA, ARES, APOLLO, ARTEMIS, HERMES, HEPHAESTUS, DEMETER, DIONYSUS, APHRODITE};

	private final int color;

	God(int color) {
		this.color = color;
	}

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public int color() {
		return color;
	}

	public Style style() {
		return Style.EMPTY.withColor(TextColor.fromRgb(color));
	}

	/** "Zeus", colored. */
	public MutableComponent displayName() {
		return Component.translatable("god.olympus." + id()).withStyle(style());
	}

	/** "God of the Sky and Thunder". */
	public MutableComponent epithet() {
		return Component.translatable("god.olympus." + id() + ".epithet");
	}

	public SimpleParticleType particle() {
		return ModParticles.forGod(this);
	}

	public static Optional<God> byId(String id) {
		for (God god : values()) {
			if (god.id().equalsIgnoreCase(id)) {
				return Optional.of(god);
			}
		}
		return Optional.empty();
	}
}
