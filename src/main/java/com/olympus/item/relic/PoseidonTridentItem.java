package com.olympus.item.relic;

import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.registry.ModParticles;
import com.olympus.util.Targets;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Poseidon — Trident of Poseidon. Release: Riptide launch anywhere, even on dry land, with a
 * wave that knocks enemies away. Sneak + release: throw it (Channeling is always on).
 */
public class PoseidonTridentItem extends GodTridentItem {
	public PoseidonTridentItem(Properties properties) {
		super(properties, God.POSEIDON, () -> 0, () -> ConfigManager.get().tridentOfPoseidon.earthshakerCooldownSeconds);
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
		if (!(entity instanceof Player player) || player.isShiftKeyDown()) {
			return super.releaseUsing(stack, level, entity, remainingTime);
		}
		if (getUseDuration(stack, entity) - remainingTime < 10) {
			return false;
		}
		double strength = ConfigManager.get().tridentOfPoseidon.riptideStrength;
		Vec3 look = player.getLookAngle();
		player.push(look.x * strength, look.y * strength, look.z * strength);
		player.startAutoSpinAttack(20, 8.0F, stack);
		if (player.onGround()) {
			player.move(net.minecraft.world.entity.MoverType.SELF, new Vec3(0.0, 1.2, 0.0));
		}
		level.playSound(null, player, SoundEvents.TRIDENT_RIPTIDE_3.value(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
		if (player instanceof ServerPlayer sp && level instanceof ServerLevel server) {
			double radius = ConfigManager.get().tridentOfPoseidon.waveRadius;
			for (LivingEntity e : Targets.around(sp, radius, e -> Targets.canHarm(sp, e))) {
				Vec3 away = e.position().subtract(sp.position()).normalize();
				e.knockback(1.2, -away.x, -away.z);
				e.hurtMarked = true;
			}
			Fx.shockwave(server, ModParticles.TIDE_DROP, sp.position().add(0, 0.2, 0), radius);
			Fx.burst(server, ParticleTypes.SPLASH, sp.position().add(0, 1, 0), 40, 0.8, 0.2);
		}
		return true;
	}
}
