package com.olympus.item.relic;

import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.god.UpgradeLevel;
import com.olympus.handler.PetrifyHandler;
import com.olympus.item.CharmItem;
import com.olympus.registry.ModParticles;
import com.olympus.util.Targets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Athena — Head of Medusa. Wearable (never placeable). Right-click: turn what you look at to stone. */
public class HeadOfMedusaItem extends CharmItem {
	public HeadOfMedusaItem(Properties properties) {
		super(properties, God.ATHENA, false);
	}

	@Override
	public int primaryCooldownSeconds() {
		return ConfigManager.get().headOfMedusa.cooldownSeconds;
	}

	@Override
	public int blessedCooldownSeconds() {
		return ConfigManager.get().headOfMedusa.gazeCooldownSeconds;
	}

	@Override
	protected boolean activate(ServerLevel level, ServerPlayer player, ItemStack stack, UpgradeLevel upgrade) {
		double range = ConfigManager.get().headOfMedusa.range;
		Vec3 eye = player.getEyePosition();
		Vec3 end = eye.add(player.getLookAngle().scale(range));
		var hit = ProjectileUtil.getEntityHitResult(player, eye, end, player.getBoundingBox().expandTowards(player.getLookAngle().scale(range)).inflate(1.0),
			e -> e instanceof LivingEntity && e.isAlive() && e != player, range * range);
		if (hit == null || !(hit.getEntity() instanceof LivingEntity target) || !Targets.canHarm(player, target) || Targets.isBoss(target)) {
			return false;
		}
		Fx.line(level, ModParticles.TITAN_DUST, eye.subtract(0, 0.2, 0), target.getEyePosition(), 0.4);
		return PetrifyHandler.petrify(target, player, upgrade.durationTicks(ConfigManager.get().headOfMedusa.petrifySeconds));
	}
}
