package com.olympus.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.olympus.Olympus;
import com.olympus.god.God;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;

/**
 * Persistent per-player state, stored with the player's save data and kept across deaths.
 *
 * @param god       the god that currently claims this player (empty until claimed)
 * @param claimed   true once the starter relic has been granted (never granted twice)
 * @param cooldowns per-player cooldowns: "item_id/primary" or "item_id/blessed" → ready game time
 */
public record PlayerOlympusData(Optional<String> god, boolean claimed, Map<String, Long> cooldowns, int bonusSlots) {
	public static final PlayerOlympusData EMPTY = new PlayerOlympusData(Optional.empty(), false, Map.of(), 0);

	public static final Codec<PlayerOlympusData> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.optionalFieldOf("god").forGetter(PlayerOlympusData::god),
		Codec.BOOL.optionalFieldOf("claimed", false).forGetter(PlayerOlympusData::claimed),
		Codec.unboundedMap(Codec.STRING, Codec.LONG).optionalFieldOf("cooldowns", Map.of()).forGetter(PlayerOlympusData::cooldowns),
		Codec.INT.optionalFieldOf("bonus_slots", 0).forGetter(PlayerOlympusData::bonusSlots)
	).apply(i, PlayerOlympusData::new));

	public static final AttachmentType<PlayerOlympusData> TYPE = AttachmentRegistry.create(Olympus.id("player_data"),
		builder -> builder.persistent(CODEC).copyOnDeath().initializer(() -> EMPTY));

	public static PlayerOlympusData get(Player player) {
		return player.getAttachedOrCreate(TYPE);
	}

	public static void set(Player player, PlayerOlympusData data) {
		player.setAttached(TYPE, data);
	}

	public Optional<God> claimedGod() {
		return god.flatMap(God::byId);
	}

	public PlayerOlympusData withClaim(God newGod) {
		return new PlayerOlympusData(Optional.of(newGod.id()), true, cooldowns, bonusSlots);
	}

	public long cooldown(String key) {
		return cooldowns.getOrDefault(key, 0L);
	}

	/** Returns a copy with this cooldown set, dropping entries that already expired. */
	public PlayerOlympusData withCooldown(String key, long readyAt, long now) {
		Map<String, Long> map = new HashMap<>();
		cooldowns.forEach((k, v) -> {
			if (v > now) {
				map.put(k, v);
			}
		});
		map.put(key, readyAt);
		return new PlayerOlympusData(god, claimed, Map.copyOf(map), bonusSlots);
	}

	public PlayerOlympusData withBonusSlots(int slots) {
		return new PlayerOlympusData(god, claimed, cooldowns, slots);
	}

	public static void init() {
	}
}
