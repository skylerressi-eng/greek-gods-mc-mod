package com.olympus.item.relic;

import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.god.UpgradeLevel;
import com.olympus.handler.MirrorHandler;
import com.olympus.item.CharmItem;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import com.olympus.util.Targets;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

/** Aphrodite — Mirror of Aphrodite. Right-click: hostile mobs lose interest in you; animals fall in love. */
public class MirrorItem extends CharmItem {
	public MirrorItem(Properties properties) {
		super(properties, God.APHRODITE, true);
	}

	private static OlympusConfig.Mirror cfg() {
		return ConfigManager.get().mirror;
	}

	@Override
	public int primaryCooldownSeconds() {
		return cfg().cooldownSeconds;
	}

	@Override
	public int blessedCooldownSeconds() {
		return cfg().charmCooldownSeconds;
	}

	@Override
	protected boolean activate(ServerLevel level, ServerPlayer player, ItemStack stack, UpgradeLevel upgrade) {
		double radius = upgrade.radius(cfg().radius);
		int ticks = upgrade.durationTicks(cfg().pacifySeconds);
		for (LivingEntity e : Targets.around(player, radius, e -> true)) {
			if (e instanceof Mob mob && Targets.isHostile(mob) && !Targets.isBoss(mob)) {
				MirrorHandler.pacify(mob, player, ticks);
				level.sendParticles(ModParticles.ROSE_PETAL, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 6, 0.3, 0.2, 0.3, 0.01);
			} else if (cfg().animalsFallInLove && e instanceof Animal animal && animal.getAge() == 0 && animal.canFallInLove()) {
				animal.setInLove(player);
			}
		}

		Fx.sound(player, ModSounds.MIRROR_CHARM, 1.2f, 1.2f);
		var pos = player.position();
		Fx.ring(level, ModParticles.ROSE_PETAL, pos.add(0, 1.0, 0), 0.6, 36, Math.min(1.0, radius * 0.07));
		Fx.helix(level, ModParticles.ROSE_PETAL, pos, 0.8, 2.2, 30);
		Fx.burst(level, ParticleTypes.HEART, pos.add(0, 2.2, 0), 6, 0.6, 0.05);
		return true;
	}
}
