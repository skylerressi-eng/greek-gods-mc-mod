package com.olympus.client.input;

import com.olympus.network.OlympusNetwork;
import com.olympus.registry.ModItems;
import com.olympus.config.ConfigManager;
import com.olympus.handler.TalariaHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Client half of the Talaria double jump: predicts the jump locally (so it feels instant) and
 * tells the server, which validates it and plays the effects.
 */
public final class AirJumpInput {
	private static boolean wasJumpDown;
	private static int jumpsUsed;

	private AirJumpInput() {
	}

	public static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) {
			wasJumpDown = false;
			return;
		}
		boolean jumpDown = player.input.keyPresses.jump();
		boolean pressed = jumpDown && !wasJumpDown;
		wasJumpDown = jumpDown;

		if (player.onGround() || player.isInWater() || player.onClimbable()) {
			jumpsUsed = 0;
			return;
		}
		ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
		if (!pressed || !boots.is(ModItems.TALARIA) || client.screen != null) {
			return;
		}
		if (player.getAbilities().flying || player.isFallFlying() || player.isPassenger() || player.isInLava() || player.isSpectator()) {
			return;
		}
		// Same rule as the server; ConfigManager returns the server's synced values here.
		int allowed = TalariaHandler.allowedJumps(boots);
		if (jumpsUsed >= allowed) {
			return;
		}
		jumpsUsed++;

		// A little push in the direction you're moving makes it feel like flying, not hopping.
		Vec3 motion = player.getDeltaMovement();
		Vec3 look = Vec3.directionFromRotation(0, player.getYRot());
		double push = player.input.keyPresses.forward() ? 0.15 : 0.0;
		double velocity = ConfigManager.get().talaria.airJumpVelocity;
		player.setDeltaMovement(motion.x + look.x * push, velocity, motion.z + look.z * push);
		player.resetFallDistance();
		ClientPlayNetworking.send(OlympusNetwork.AirJumpPayload.INSTANCE);
	}
}
