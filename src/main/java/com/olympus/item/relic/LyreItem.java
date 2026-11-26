package com.olympus.item.relic;

import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.god.UpgradeLevel;
import com.olympus.item.CharmItem;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import com.olympus.util.Targets;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

/** Apollo — Lyre of Apollo. Right-click: Regeneration for you and nearby players. */
public class LyreItem extends CharmItem {
	public LyreItem(Properties properties) {
		super(properties, God.APOLLO, true);
	}

	private static OlympusConfig.Lyre cfg() {
		return ConfigManager.get().lyre;
	}

	@Override
	public int primaryCooldownSeconds() {
		return cfg().cooldownSeconds;
	}

	@Override
	public int blessedCooldownSeconds() {
		return cfg().sunburstCooldownSeconds;
	}

	@Override
	protected boolean activate(ServerLevel level, ServerPlayer player, ItemStack stack, UpgradeLevel upgrade) {
		double radius = upgrade.radius(cfg().radius);
		int ticks = upgrade.durationTicks(cfg().regenSeconds);
		for (ServerPlayer ally : Targets.alliesAround(player, radius)) {
			ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ticks, cfg().regenAmplifier, false, true, true), player);
			Fx.helix(level, ModParticles.SUN_MOTE, ally.position(), 0.6, 2.2, 24);
		}

		// A major chord: root, third, fifth.
		Fx.sound(player, ModSounds.LYRE_CHORD, 1.2f, 0.84f);
		Fx.sound(player, ModSounds.LYRE_CHORD, 1.0f, 1.06f);
		Fx.sound(player, ModSounds.LYRE_CHORD, 1.0f, 1.26f);
		Fx.sound(player, ModSounds.LYRE_SHIMMER, 0.8f, 1.2f);
		var pos = player.position();
		Fx.ring(level, ModParticles.SUN_MOTE, pos.add(0, 0.4, 0), 0.8, 40, Math.min(0.9, radius * 0.08));
		for (int i = 0; i < 12; i++) {
			level.sendParticles(ParticleTypes.NOTE, pos.x, pos.y + 2.2, pos.z, 0, level.getRandom().nextDouble(), 0, 0, 1);
		}
		return true;
	}
}
