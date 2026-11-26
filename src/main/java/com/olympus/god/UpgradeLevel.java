package com.olympus.god;

import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Mortal (0) → Blessed ★ (1) → Ascended ★★ (2). */
public enum UpgradeLevel {
	MORTAL(""),
	BLESSED(" ★"),
	ASCENDED(" ★★");

	private final String stars;

	UpgradeLevel(String stars) {
		this.stars = stars;
	}

	public static UpgradeLevel of(int level) {
		return level <= 0 ? MORTAL : level == 1 ? BLESSED : ASCENDED;
	}

	public int id() {
		return ordinal();
	}

	public String stars() {
		return stars;
	}

	public boolean atLeast(UpgradeLevel other) {
		return ordinal() >= other.ordinal();
	}

	public Component displayName() {
		ChatFormatting color = switch (this) {
			case MORTAL -> ChatFormatting.GRAY;
			case BLESSED -> ChatFormatting.AQUA;
			case ASCENDED -> ChatFormatting.GOLD;
		};
		return Component.translatable("olympus.level." + name().toLowerCase(java.util.Locale.ROOT)).append(stars).withStyle(color);
	}

	public double cooldownMultiplier() {
		OlympusConfig.Upgrades u = ConfigManager.get().upgrades;
		return switch (this) {
			case MORTAL -> 1.0;
			case BLESSED -> u.blessedCooldownMultiplier;
			case ASCENDED -> u.ascendedCooldownMultiplier;
		};
	}

	public double durationMultiplier() {
		return this == ASCENDED ? ConfigManager.get().upgrades.ascendedDurationMultiplier : 1.0;
	}

	public double radiusMultiplier() {
		return this == ASCENDED ? ConfigManager.get().upgrades.ascendedRadiusMultiplier : 1.0;
	}

	/** Seconds → ticks, scaled by duration multiplier. */
	public int durationTicks(double seconds) {
		return (int) Math.round(seconds * 20.0 * durationMultiplier());
	}

	public double radius(double base) {
		return base * radiusMultiplier();
	}

	public int cooldownTicks(double seconds) {
		return (int) Math.round(seconds * 20.0 * cooldownMultiplier());
	}
}
