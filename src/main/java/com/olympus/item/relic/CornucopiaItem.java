package com.olympus.item.relic;

import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.god.UpgradeLevel;
import com.olympus.item.CharmItem;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Demeter — Cornucopia. Right-click: hunger and saturation fully restored. */
public class CornucopiaItem extends CharmItem {
	public CornucopiaItem(Properties properties) {
		super(properties, God.DEMETER, false);
	}

	@Override
	public int primaryCooldownSeconds() {
		return ConfigManager.get().cornucopia.cooldownSeconds;
	}

	@Override
	public int blessedCooldownSeconds() {
		return ConfigManager.get().cornucopia.feastCooldownSeconds;
	}

	public static void feed(ServerPlayer player) {
		player.getFoodData().setFoodLevel(20);
		player.getFoodData().setSaturation(20.0f);
		Fx.burst(player.level(), ModParticles.GRAIN_LEAF, player.position().add(0, 1.2, 0), 20, 0.4, 0.05);
	}

	@Override
	protected boolean activate(ServerLevel level, ServerPlayer player, ItemStack stack, UpgradeLevel upgrade) {
		feed(player);
		Fx.sound(player, net.minecraft.sounds.SoundEvents.GENERIC_EAT.value(), 1.0f, 1.0f);
		Fx.sound(player, ModSounds.SICKLE_BLOOM, 0.8f, 1.3f);
		Fx.helix(level, ModParticles.GRAIN_LEAF, player.position(), 0.7, 2.0, 24);
		return true;
	}
}
