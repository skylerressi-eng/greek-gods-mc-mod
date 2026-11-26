package com.olympus.handler;

import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.god.GodItem;
import com.olympus.god.UpgradeLevel;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Talaria: double jump. The client predicts the jump for responsiveness and sends a packet; the
 * server validates it (wearing Talaria, airborne, not flying/gliding/swimming/riding, jumps left,
 * rate-limited), resets fall distance and plays the effects for everyone.
 */
public final class TalariaHandler {
	private static final Map<UUID, Integer> JUMPS_USED = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> LAST_JUMP = new ConcurrentHashMap<>();

	private TalariaHandler() {
	}

	/** Air jumps allowed for this pair of sandals (Blessed+ adds one: triple jump). */
	public static int allowedJumps(ItemStack boots) {
		int base = ConfigManager.get().talaria.airJumps;
		return GodItem.level(boots).atLeast(UpgradeLevel.BLESSED) ? base + 1 : base;
	}

	public static void onAirJump(ServerPlayer player) {
		ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
		if (!boots.is(ModItems.TALARIA) || player.onGround() || player.getAbilities().flying || player.isFallFlying()
			|| player.isInWater() || player.isInLava() || player.isPassenger() || player.onClimbable() || player.isSpectator()) {
			return;
		}
		long now = player.level().getGameTime();
		if (now - LAST_JUMP.getOrDefault(player.getUUID(), 0L) < 4) {
			return; // rate limit
		}
		int used = JUMPS_USED.getOrDefault(player.getUUID(), 0);
		if (used >= allowedJumps(boots)) {
			return;
		}
		JUMPS_USED.put(player.getUUID(), used + 1);
		LAST_JUMP.put(player.getUUID(), now);

		// The client already applied the velocity; the server only makes the jump "count".
		player.resetFallDistance();

		ServerLevel level = player.level();
		var feet = player.position();
		Fx.ring(level, ModParticles.WIND_WISP, feet, 0.4, 16, 0.18);
		Fx.burst(level, ParticleTypes.CLOUD, feet, 6, 0.2, 0.02);
		Fx.sound(player, ModSounds.TALARIA_JUMP, 0.8f, 1.2f + used * 0.2f);
	}

	/** Reset the jump counter whenever the player touches the ground (called every tick). */
	public static void tickPlayer(ServerPlayer player) {
		if (player.onGround() || player.isInWater() || player.onClimbable()) {
			JUMPS_USED.remove(player.getUUID());
		}
	}

	public static void forget(UUID player) {
		JUMPS_USED.remove(player);
		LAST_JUMP.remove(player);
	}
}
