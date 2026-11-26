package com.olympus.test;

import com.olympus.registry.ModItems;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Options;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

/**
 * Plays the mod in a real client: opens a world, gives every relic, holds and uses each one,
 * opens the inventory and the creative tab. Run with: xvfb-run ./gradlew runClientGameTest
 */
public class OlympusClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getClientWorld().waitForChunksRender();
			var server = world.getServer();
			server.runCommand("gamemode survival @a");
			server.runCommand("olympus reload");
			server.runCommand("effect give @a minecraft:resistance infinite 4 true");
			server.runCommand("time set day");
			server.runCommand("summon zombie ~3 ~ ~");
			server.runCommand("summon cow ~-3 ~ ~");
			context.waitTicks(40); // first-join claim fires here

			List<Item> relics = ModItems.STARTER_RELICS;
			for (int batch = 0; batch < relics.size(); batch += 9) {
				server.runCommand("clear @a");
				for (int i = batch; i < Math.min(batch + 9, relics.size()); i++) {
					String id = BuiltInRegistries.ITEM.getKey(relics.get(i)).getPath();
					server.runCommand("olympus give @a " + id + " " + (1 + i % 2));
				}
				server.runCommand("give @a minecraft:arrow 16");
				server.runCommand("give @a minecraft:wheat_seeds 4");
				context.waitTicks(5);
				for (int slot = 0; slot < 9 && batch + slot < relics.size(); slot++) {
					final int s = slot;
					context.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(s));
					context.waitTicks(2);
					context.getInput().holdKeyFor((Options o) -> o.keyUse, 40);
					context.getInput().holdKeyFor((Options o) -> o.keyShift, 50);
					context.getInput().pressKey(com.olympus.client.OlympusClient.DIVINE_POWER);
					context.waitTicks(10);
					context.getInput().pressKey((Options o) -> o.keyJump);
					context.waitTicks(3);
					context.getInput().pressKey((Options o) -> o.keyJump);
					context.waitTicks(20);
					context.takeScreenshot("olympus_" + BuiltInRegistries.ITEM.getKey(relics.get(batch + slot)).getPath());
				}
			}
			// Inventory with tooltips
			context.getInput().pressKey((Options o) -> o.keyInventory);
			context.waitTicks(10);
			context.getInput().setCursorPos(0.5 * context.computeOnClient(mc -> mc.getWindow().getScreenWidth()), 0.5 * context.computeOnClient(mc -> mc.getWindow().getScreenHeight()));
			context.waitTicks(10);
			context.takeScreenshot("olympus_inventory");
			context.getInput().pressKey((Options o) -> o.keyInventory);
			server.runCommand("gamemode creative @a");
			context.waitTicks(10);
			context.getInput().pressKey((Options o) -> o.keyInventory);
			context.waitTicks(20);
			context.takeScreenshot("olympus_creative");
		}
	}
}
