package com.olympus.handler;

import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import com.olympus.fx.Fx;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import com.olympus.util.DivineLightning;
import com.olympus.util.Targets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Aegis primary: blocking a melee hit can call Zeus's lightning down on the attacker. */
public final class AegisHandler {
	private AegisHandler() {
	}

	/** Called from {@code LivingEntityMixin} when a player blocks a melee attack with an item. */
	public static void onMeleeBlocked(ServerPlayer player, LivingEntity attacker) {
		ItemStack blocking = player.getItemBlockingWith();
		if (blocking == null || !blocking.is(ModItems.AEGIS)) {
			return;
		}
		OlympusConfig.Aegis cfg = ConfigManager.get().aegis;
		ServerLevel level = player.level();
		// Small golden flash on every block, so the shield feels divine even when lightning doesn't fire.
		level.sendParticles(ModParticles.DIVINE_GLINT, player.getX(), player.getY() + 1.1, player.getZ(), 6, 0.4, 0.3, 0.4, 0.05);

		if (!Targets.canHarm(player, attacker) || level.getRandom().nextDouble() >= cfg.lightningChance) {
			return;
		}
		DivineLightning.strike(level, attacker.position(), player, cfg.lightningDamage, 1.2);
		Fx.sound(player, ModSounds.AEGIS_SMITE, 1.2f, 1.0f);
		Fx.line(level, ModParticles.STORM_SPARK, player.position().add(0, 1.2, 0), attacker.position().add(0, 1.0, 0), 0.25);
	}
}
