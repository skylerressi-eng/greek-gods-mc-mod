package com.olympus.ability;

import com.olympus.data.PlayerOlympusData;
import com.olympus.god.GodItem;
import com.olympus.god.UpgradeLevel;
import com.olympus.registry.ModComponents;
import com.olympus.registry.ModComponents.StackCooldowns;
import com.olympus.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

/**
 * Cooldowns are stored twice: on the stack (so handing the relic to a friend doesn't reset it)
 * and on the player (so carrying two copies doesn't let you chain casts). The effective cooldown
 * is the later of the two. Both use absolute overworld game time, so they survive relogs and
 * restarts. Primary cooldowns are mirrored to the vanilla hotbar overlay.
 */
public final class Cooldowns {
	public enum Kind {
		PRIMARY, BLESSED
	}

	private Cooldowns() {
	}

	public static long now(ServerPlayer player) {
		return player.level().getServer().overworld().getGameTime();
	}

	private static String key(ItemStack stack, Kind kind) {
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString() + "/" + (kind == Kind.PRIMARY ? "primary" : "blessed");
	}

	public static long readyAt(ServerPlayer player, ItemStack stack, Kind kind) {
		StackCooldowns sc = stack.getOrDefault(ModComponents.COOLDOWNS, StackCooldowns.NONE);
		long onStack = kind == Kind.PRIMARY ? sc.primaryReadyAt() : sc.blessedReadyAt();
		long onPlayer = PlayerOlympusData.get(player).cooldown(key(stack, kind));
		return Math.max(onStack, onPlayer);
	}

	public static long remainingTicks(ServerPlayer player, ItemStack stack, Kind kind) {
		return Math.max(0L, readyAt(player, stack, kind) - now(player));
	}

	public static boolean isReady(ServerPlayer player, ItemStack stack, Kind kind) {
		return remainingTicks(player, stack, kind) <= 0;
	}

	/**
	 * Checks the cooldown and, if not ready, tells the player how long is left.
	 * @return true if the ability may be used
	 */
	public static boolean checkReady(ServerPlayer player, ItemStack stack, Kind kind) {
		long remaining = remainingTicks(player, stack, kind);
		if (remaining <= 0) {
			return true;
		}
		player.displayClientMessage(Component.translatable("olympus.message.cooldown", stack.getHoverName(), formatSeconds(remaining)).withStyle(ChatFormatting.RED), true);
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.COOLDOWN_DENY, SoundSource.PLAYERS, 0.5f, 0.8f);
		return false;
	}

	/** Starts the cooldown, scaled by the stack's upgrade level. */
	public static void start(ServerPlayer player, ItemStack stack, Kind kind, double baseSeconds) {
		UpgradeLevel level = GodItem.level(stack);
		int ticks = level.cooldownTicks(baseSeconds);
		if (ticks <= 0) {
			return;
		}
		long now = now(player);
		long readyAt = now + ticks;
		StackCooldowns sc = stack.getOrDefault(ModComponents.COOLDOWNS, StackCooldowns.NONE);
		stack.set(ModComponents.COOLDOWNS, kind == Kind.PRIMARY ? sc.withPrimary(readyAt) : sc.withBlessed(readyAt));
		PlayerOlympusData.set(player, PlayerOlympusData.get(player).withCooldown(key(stack, kind), readyAt, now));
		if (kind == Kind.PRIMARY) {
			player.getCooldowns().addCooldown(stack, ticks);
		}
	}

	/**
	 * Makes sure the vanilla hotbar overlay matches our stored primary cooldown
	 * (after relogging, or when picking up a relic someone else used).
	 */
	public static void syncOverlay(ServerPlayer player, ItemStack stack) {
		long remaining = remainingTicks(player, stack, Kind.PRIMARY);
		if (remaining > 0 && !player.getCooldowns().isOnCooldown(stack)) {
			player.getCooldowns().addCooldown(stack, (int) Math.min(Integer.MAX_VALUE, remaining));
		}
	}

	public static String formatSeconds(long ticks) {
		long seconds = (ticks + 19) / 20;
		if (seconds >= 60) {
			return String.format("%d:%02d", seconds / 60, seconds % 60);
		}
		return seconds + "s";
	}
}
