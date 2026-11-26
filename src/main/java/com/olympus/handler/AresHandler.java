package com.olympus.handler;

import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.god.GodItem;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

/** Blade of Ares primary: kills feed Strength I, and a kill during the fury escalates it to Strength II. */
public final class AresHandler {
	/** player → game time when the blade's own Strength buff ends. */
	private static final java.util.Map<java.util.UUID, Long> FURY_UNTIL = new java.util.concurrent.ConcurrentHashMap<>();

	private AresHandler() {
	}

	public static void forget(java.util.UUID player) {
		FURY_UNTIL.remove(player);
	}

	public static void register() {
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((level, killer, killed, source) -> {
			if (!(killer instanceof ServerPlayer player)) {
				return;
			}
			ItemStack blade = player.getMainHandItem();
			if (!blade.is(ModItems.BLADE_OF_ARES)) {
				return;
			}
			int ticks = GodItem.level(blade).durationTicks(ConfigManager.get().bladeOfAres.strengthSeconds);
			MobEffectInstance current = player.getEffect(MobEffects.STRENGTH);
			long now = level.getGameTime();
			boolean escalate = current != null && FURY_UNTIL.getOrDefault(player.getUUID(), 0L) > now;
			int amplifier = escalate ? 1 : 0;
			if (current != null && current.getAmplifier() > amplifier) {
				return; // a stronger Strength (e.g. a potion) is already active; don't downgrade it
			}
			player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, ticks, amplifier, false, true, true));
			FURY_UNTIL.put(player.getUUID(), now + ticks);

			var pos = killed.position().add(0, killed.getBbHeight() * 0.5, 0);
			Fx.burst(level, ModParticles.WAR_EMBER, pos, escalate ? 40 : 20, 0.4, 0.12);
			Fx.helix(level, ModParticles.WAR_EMBER, player.position(), 0.7, 2.0, escalate ? 40 : 20);
			if (escalate) {
				Fx.burst(level, ParticleTypes.ANGRY_VILLAGER, player.getEyePosition().add(0, 0.5, 0), 3, 0.3, 0);
				Fx.sound(player, ModSounds.ARES_FURY_II, 1.0f, 0.8f);
			} else {
				Fx.sound(player, ModSounds.ARES_FURY, 0.9f, 1.1f);
			}
		});
	}
}
