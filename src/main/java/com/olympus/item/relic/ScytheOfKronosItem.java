package com.olympus.item.relic;

import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.god.UpgradeLevel;
import com.olympus.item.CharmItem;
import com.olympus.registry.ModParticles;
import com.olympus.util.Targets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Titan — Scythe of Kronos. Right-click: freeze time around you. */
public class ScytheOfKronosItem extends CharmItem {
	public ScytheOfKronosItem(Properties properties) {
		super(properties, God.TITAN, false);
	}

	@Override
	public boolean isCharm() {
		return false; // a weapon: enchantable
	}

	@Override
	public int primaryCooldownSeconds() {
		return ConfigManager.get().scythe.cooldownSeconds;
	}

	@Override
	public int blessedCooldownSeconds() {
		return ConfigManager.get().scythe.rewindCooldownSeconds;
	}

	@Override
	protected boolean activate(ServerLevel level, ServerPlayer player, ItemStack stack, UpgradeLevel upgrade) {
		double radius = upgrade.radius(ConfigManager.get().scythe.freezeRadius);
		int ticks = upgrade.durationTicks(ConfigManager.get().scythe.freezeSeconds);
		for (LivingEntity e : Targets.around(player, radius, e -> Targets.canHarm(player, e))) {
			e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 3), player);
			e.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, ticks, 2), player);
			level.sendParticles(ModParticles.TITAN_DUST, e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ(), 10, 0.3, 0.5, 0.3, 0.0);
		}
		Fx.sound(player, net.minecraft.sounds.SoundEvents.BELL_RESONATE, 1.5f, 0.5f);
		Fx.sphere(level, ModParticles.TITAN_DUST, player.position().add(0, 1, 0), Math.min(radius, 8), 150);
		Fx.shockwave(level, ModParticles.TITAN_DUST, player.position().add(0, 0.2, 0), radius);
		return true;
	}
}
