package com.olympus.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Options;

public class JumpProbe implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getClientWorld().waitForChunksRender();
			world.getServer().runCommand("gamemode survival @a");
			world.getServer().runCommand("item replace entity @a armor.feet with olympus:talaria");
			context.waitTicks(20);
			double ground = context.computeOnClient(mc -> mc.player.getY());
			context.getInput().holdKeyFor((Options o) -> o.keyJump, 2);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 6; i++) { context.waitTick(); sb.append(String.format("%.2f ", context.computeOnClient(mc -> mc.player.getY()) - ground)); }
			context.getInput().holdKeyFor((Options o) -> o.keyJump, 2);
			for (int i = 0; i < 14; i++) { context.waitTick(); sb.append(String.format("%.2f ", context.computeOnClient(mc -> mc.player.getY()) - ground)); }
			System.out.println("JUMPPROBE heights: " + sb);
			System.out.println("JUMPPROBE boots: " + context.computeOnClient(mc -> mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).toString()));
		}
	}
}
