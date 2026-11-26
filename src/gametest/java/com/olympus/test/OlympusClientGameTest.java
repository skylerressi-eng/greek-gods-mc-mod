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
			// Phase 3 craftables and Phase 4 legendaries, in creative (ops in creative bypass the 3-item limit)
			server.runCommand("gamemode creative @a");
			List<Item> more = new java.util.ArrayList<>(List.of(ModItems.CADUCEUS, ModItems.SPEAR_OF_ATHENA, ModItems.FORGE_HAMMER,
				ModItems.HARPE, ModItems.THYRSUS, ModItems.CORNUCOPIA));
			more.addAll(ModItems.LEGENDARIES);
			for (int batch = 0; batch < more.size(); batch += 9) {
				server.runCommand("clear @a");
				for (int i = batch; i < Math.min(batch + 9, more.size()); i++) {
					server.runCommand("olympus give @a " + BuiltInRegistries.ITEM.getKey(more.get(i)).getPath() + " 1");
				}
				server.runCommand("summon zombie ~4 ~ ~");
				context.waitTicks(5);
				for (int slot = 0; slot < 9 && batch + slot < more.size(); slot++) {
					final int s2 = slot;
					context.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(s2));
					context.waitTicks(2);
					context.getInput().holdKeyFor((Options o) -> o.keyUse, 30);
					context.getInput().pressKey(com.olympus.client.OlympusClient.DIVINE_POWER);
					context.waitTicks(30);
					context.takeScreenshot("olympus_" + BuiltInRegistries.ITEM.getKey(more.get(batch + slot)).getPath());
				}
			}
			server.runCommand("gamemode survival @a");
			server.runCommand("effect give @a minecraft:resistance infinite 4 true");
			server.runCommand("effect give @a minecraft:instant_health 1 10 true");
			for (String event : List.of("wrath_of_zeus", "gorgons_lair", "quest_for_the_fleece", "titanomachy")) {
				server.runCommand("execute as @p at @s run olympus event start " + event);
				context.waitTicks(120);
				context.takeScreenshot("olympus_event_" + event);
				server.runCommand("olympus event stop");
				context.waitTicks(10);
			}
			server.runCommand("clear @a");
			server.runCommand("give @a olympus:codex_of_olympus");
			context.waitTicks(5);
			context.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(0));
			context.getInput().pressMouse(1);
			context.waitTicks(20);
			context.takeScreenshot("olympus_codex");
			context.getInput().pressKey(org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE);
			context.waitTicks(5);

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
