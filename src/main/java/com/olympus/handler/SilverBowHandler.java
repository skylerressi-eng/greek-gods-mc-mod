package com.olympus.handler;

import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.item.relic.SilverBowItem;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import com.olympus.util.Targets;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

/** Silver Bow primary: anything hit by a silver arrow glows (Artemis never loses her quarry). */
public final class SilverBowHandler {
	private SilverBowHandler() {
	}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (!(source.getDirectEntity() instanceof AbstractArrow arrow) || !arrow.getTags().contains(SilverBowItem.ARROW_TAG)) {
				return;
			}
			if (!(arrow.getOwner() instanceof ServerPlayer shooter)) {
				return;
			}
			if (entity instanceof Player && !Targets.canHarm(shooter, entity)) {
				return;
			}
			int ticks = ConfigManager.get().silverBow.glowSeconds * 20;
			entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, false, true), shooter);
			ServerLevel level = (ServerLevel) entity.level();
			Fx.burst(level, ModParticles.MOONBEAM, entity.position().add(0, entity.getBbHeight() * 0.6, 0), 16, 0.3, 0.05);
			Fx.sound(entity, ModSounds.SILVER_MARK, 0.7f, 1.6f);
		});
	}

	/** Called every tick for silver arrows (from {@code AbstractArrowMixin}). */
	public static void tickArrow(AbstractArrow arrow) {
		if (!(arrow.level() instanceof ServerLevel level)) {
			return;
		}
		if (arrow.isNoGravity() && arrow.tickCount > ConfigManager.get().silverBow.straightFlightSeconds * 20) {
			arrow.setNoGravity(false); // after a while, physics wins again
		}
		if (arrow.isNoGravity() && arrow.tickCount % 2 == 0 && arrow.getDeltaMovement().lengthSqr() > 0.01) {
			level.sendParticles(ModParticles.MOONBEAM, arrow.getX(), arrow.getY(), arrow.getZ(), 1, 0, 0, 0, 0);
		}
	}
}
