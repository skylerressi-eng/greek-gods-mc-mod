package com.olympus.item.relic;

import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.god.UpgradeLevel;
import com.olympus.item.CharmItem;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

/** Poseidon — Conch of the Deep. Right-click: Water Breathing + Dolphin's Grace. */
public class ConchItem extends CharmItem {
	public ConchItem(Properties properties) {
		super(properties, God.POSEIDON, true);
	}

	private static OlympusConfig.Conch cfg() {
		return ConfigManager.get().conch;
	}

	@Override
	public int primaryCooldownSeconds() {
		return cfg().cooldownSeconds;
	}

	@Override
	public int blessedCooldownSeconds() {
		return cfg().riptideCooldownSeconds;
	}

	@Override
	protected boolean activate(ServerLevel level, ServerPlayer player, ItemStack stack, UpgradeLevel upgrade) {
		int ticks = upgrade.durationTicks(cfg().durationSeconds);
		player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, ticks, 0, false, true, true));
		player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, ticks, 0, false, true, true));

		Fx.sound(player, ModSounds.CONCH_BLOW, 1.6f, 1.0f);
		var pos = player.position();
		Fx.shockwave(level, ModParticles.TIDE_DROP, pos.add(0, 0.2, 0), 4.0);
		Fx.helix(level, ModParticles.TIDE_DROP, pos, 0.9, 2.4, 40);
		Fx.burst(level, player.isInWater() ? ParticleTypes.BUBBLE_COLUMN_UP : ParticleTypes.SPLASH, pos.add(0, 1, 0), 60, 0.8, 0.2);
		return true;
	}
}
