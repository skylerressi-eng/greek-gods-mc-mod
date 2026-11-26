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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Hermes — Caduceus. Right-click: blink forward (never through walls). */
public class CaduceusItem extends CharmItem {
	public CaduceusItem(Properties properties) {
		super(properties, God.HERMES, false);
	}

	@Override
	public int primaryCooldownSeconds() {
		return ConfigManager.get().caduceus.cooldownSeconds;
	}

	@Override
	public int blessedCooldownSeconds() {
		return ConfigManager.get().caduceus.waypointCooldownSeconds;
	}

	@Override
	protected boolean activate(ServerLevel level, ServerPlayer player, ItemStack stack, UpgradeLevel upgrade) {
		double distance = upgrade.radius(ConfigManager.get().caduceus.blinkDistance);
		Vec3 eye = player.getEyePosition();
		Vec3 end = eye.add(player.getLookAngle().scale(distance));
		BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		Vec3 target = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation().subtract(player.getLookAngle().scale(0.6));
		Vec3 feet = target.subtract(0, player.getEyeHeight(), 0);
		// Step up if the destination is inside a block.
		for (int i = 0; i < 3 && !level.noCollision(player, player.getBoundingBox().move(feet.subtract(player.position()))); i++) {
			feet = feet.add(0, 1, 0);
		}
		if (!level.noCollision(player, player.getBoundingBox().move(feet.subtract(player.position())))) {
			return false;
		}
		Vec3 from = player.position();
		Fx.burst(level, ModParticles.WIND_WISP, from.add(0, 1, 0), 30, 0.4, 0.1);
		player.teleportTo(feet.x, feet.y, feet.z);
		player.resetFallDistance();
		Fx.line(level, ModParticles.WIND_WISP, from.add(0, 1, 0), feet.add(0, 1, 0), 0.5);
		Fx.burst(level, ModParticles.WIND_WISP, feet.add(0, 1, 0), 30, 0.4, 0.1);
		Fx.sound(level, feet, ModSounds.TALARIA_JUMP, 1.0f, 1.6f);
		return true;
	}
}
