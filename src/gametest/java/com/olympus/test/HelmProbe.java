package com.olympus.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** Screenshots the player in third person, before and after the Helm of Darkness vanish. */
public class HelmProbe implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getClientWorld().waitForChunksRender();
			var server = world.getServer();
			server.runCommand("gamemode survival @a");
			server.runCommand("item replace entity @a armor.head with olympus:helm_of_darkness");
			server.runCommand("item replace entity @a armor.chest with minecraft:diamond_chestplate");
			server.runCommand("item replace entity @a armor.legs with minecraft:golden_leggings");
			server.runCommand("item replace entity @a weapon.mainhand with olympus:blade_of_ares");
			context.runOnClient(mc -> mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
			context.waitTicks(20);
			context.takeScreenshot("helm_visible");
			server.runCommand("effect give @a minecraft:invisibility 30 0 true");
			context.waitTicks(20);
			context.takeScreenshot("helm_invisible");
		}
	}
}
