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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import com.olympus.util.Targets;

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
		OlympusConfig.Conch c = cfg();
		int ticks = upgrade.durationTicks(c.durationSeconds);
		player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, ticks, 0, false, true, true));
		player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, ticks, 0, false, true, true));

		// Tidal Surge: a 90° wave in front of you.
		double range = upgrade.radius(c.waveRange);
		Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
		var damage = player.damageSources().playerAttack(player);
		for (LivingEntity target : Targets.around(player, range, e -> Targets.canHarm(player, e))) {
			Vec3 to = target.position().subtract(player.position()).multiply(1, 0, 1);
			if (to.lengthSqr() < 1.0E-4 || to.normalize().dot(look) < 0.7) {
				continue;
			}
			target.hurtServer(level, damage, c.waveDamage);
			target.knockback(c.waveKnockback, -look.x, -look.z);
			target.hurtMarked = true;
			target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, upgrade.durationTicks(c.waveSlownessSeconds), 1), player);
			level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1, target.getZ(), 20, 0.3, 0.5, 0.3, 0.1);
		}

		Fx.sound(player, ModSounds.CONCH_BLOW, 1.6f, 1.0f);
		Vec3 eye = player.getEyePosition().subtract(0, 0.4, 0);
		for (double d = 1.5; d <= range; d += 0.8) {
			Vec3 p = eye.add(look.scale(d));
			Vec3 side = new Vec3(-look.z, 0, look.x).scale(d * 0.7);
			Fx.line(level, ModParticles.TIDE_DROP, p.subtract(side), p.add(side), 0.5);
		}
		Fx.burst(level, player.isInWater() ? ParticleTypes.BUBBLE_COLUMN_UP : ParticleTypes.SPLASH, eye.add(look.scale(2)), 40, 0.8, 0.2);
		return true;
	}
}
