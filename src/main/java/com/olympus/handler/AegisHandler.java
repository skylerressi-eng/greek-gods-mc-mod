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
	private static final java.util.Map<java.util.UUID, Integer> BLOCKS = new java.util.concurrent.ConcurrentHashMap<>();

	private AegisHandler() {
	}

	/** Blessed: every Nth blocked hit releases a lightning shockwave. */
	private static void countBlock(ServerPlayer player, ItemStack aegis) {
		if (!com.olympus.god.GodItem.level(aegis).atLeast(com.olympus.god.UpgradeLevel.BLESSED)) {
			return;
		}
		OlympusConfig.Aegis cfg = ConfigManager.get().aegis;
		int count = BLOCKS.merge(player.getUUID(), 1, Integer::sum);
		if (count < cfg.shockwaveEveryBlocks) {
			return;
		}
		BLOCKS.remove(player.getUUID());
		ServerLevel level = player.level();
		double radius = com.olympus.god.GodItem.level(aegis).radius(cfg.shockwaveRadius);
		for (var e : Targets.around(player, radius, e -> Targets.canHarm(player, e) && (Targets.isHostile(e) || e instanceof net.minecraft.world.entity.player.Player))) {
			net.minecraft.world.phys.Vec3 away = e.position().subtract(player.position()).normalize();
			e.knockback(cfg.shockwaveKnockback, -away.x, -away.z);
			e.hurtMarked = true;
			DivineLightning.strike(level, e.position(), player, cfg.shockwaveDamage, 0.8);
		}
		Fx.shockwave(level, ModParticles.STORM_SPARK, player.position().add(0, 0.3, 0), radius);
		Fx.sound(player, ModSounds.AEGIS_SMITE, 1.5f, 0.7f);
	}

	/** Called from {@code LivingEntityMixin} when a player blocks a melee attack with an item. */
	public static void onMeleeBlocked(ServerPlayer player, LivingEntity attacker) {
		onMeleeBlocked(player, attacker, player.getItemBlockingWith());
	}

	public static void onMeleeBlocked(ServerPlayer player, LivingEntity attacker, ItemStack blocking) {
		if (blocking == null || !blocking.is(ModItems.AEGIS)) {
			return;
		}
		OlympusConfig.Aegis cfg = ConfigManager.get().aegis;
		ServerLevel level = player.level();
		// Small golden flash on every block, so the shield feels divine even when lightning doesn't fire.
		level.sendParticles(ModParticles.DIVINE_GLINT, player.getX(), player.getY() + 1.1, player.getZ(), 6, 0.4, 0.3, 0.4, 0.05);
		countBlock(player, blocking);

		// Disabling the Aegis (e.g. with an axe) costs the attacker: Zeus strikes back.
		if (attacker.getSecondsToDisableBlocking() > 0 && cfg.disableRetaliationDamage > 0 && Targets.canHarm(player, attacker)) {
			var type = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getOrThrow(com.olympus.registry.ModKeys.DIVINE_LIGHTNING);
			attacker.hurtServer(level, new net.minecraft.world.damagesource.DamageSource(type, player), cfg.disableRetaliationDamage);
			level.sendParticles(ModParticles.STORM_SPARK, attacker.getX(), attacker.getY() + 1, attacker.getZ(), 15, 0.3, 0.4, 0.3, 0.1);
			Fx.sound(attacker, ModSounds.AEGIS_SMITE, 0.6f, 1.6f);
		}

		if (Targets.canHarm(player, attacker) && cfg.blockKnockback > 0) {
			attacker.knockback(cfg.blockKnockback, player.getX() - attacker.getX(), player.getZ() - attacker.getZ());
			attacker.hurtMarked = true;
		}
		if (!Targets.canHarm(player, attacker) || level.getRandom().nextDouble() >= cfg.lightningChance) {
			return;
		}
		DivineLightning.strike(level, attacker.position(), player, cfg.lightningDamage, 1.2);
		Fx.sound(player, ModSounds.AEGIS_SMITE, 1.2f, 1.0f);
		Fx.line(level, ModParticles.STORM_SPARK, player.position().add(0, 1.2, 0), attacker.position().add(0, 1.0, 0), 0.25);
	}
}
