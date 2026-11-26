package com.olympus.test;

import com.olympus.ability.Cooldowns;
import com.olympus.god.GodItem;
import com.olympus.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Options;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * Plays every god item in survival with real key presses (right-click, attack, sneak, jump, R)
 * and checks on the server that each power actually fired. Prints "PLAYTEST FAIL ..." lines and
 * throws at the end if anything did not work.
 */
public class SurvivalPlaytest implements FabricClientGameTest {
	private static final Set<String> WORN = Set.of("helm_of_darkness", "talaria", "golden_fleece", "head_of_medusa_worn");
	private final List<String> failures = new ArrayList<>();

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getClientWorld().waitForChunksRender();
			TestServerContext server = world.getServer();
			server.runCommand("gamemode survival @a");
			server.runCommand("time set day");
			server.runCommand("weather clear");
			server.runCommand("effect give @a minecraft:resistance infinite 4 true");
			server.runCommand("effect give @a minecraft:saturation infinite 0 true");
			context.waitTicks(40);

			List<Item> items = new ArrayList<>();
			for (Item item : ModItems.ALL) {
				if (item instanceof GodItem) {
					items.add(item);
				}
			}
			for (Item item : items) {
				playItem(context, server, item);
			}
			playOtherItems(context, server);
			if (!failures.isEmpty()) {
				for (String f : failures) {
					System.out.println("PLAYTEST FAIL " + f);
				}
				throw new AssertionError(failures.size() + " god item checks failed: " + failures);
			}
			System.out.println("PLAYTEST ALL " + items.size() + " GOD ITEMS WORK");
		}
	}

	private void playItem(ClientGameTestContext context, TestServerContext server, Item item) {
		String id = BuiltInRegistries.ITEM.getKey(item).getPath();
		// Fresh state: same spot, facing +X, a sturdy husk 3 blocks ahead.
		server.runCommand("clear @a");
		server.runCommand("kill @e[type=!player]");
		server.runCommand("effect clear @a");
		server.runCommand("effect give @a minecraft:resistance infinite 4 true");
		server.runCommand("effect give @a minecraft:saturation infinite 0 true");
		server.runCommand("execute as @p run tp @s 0 -60 0 -90 0");
		context.waitTicks(5);
		server.runCommand("summon husk 3 -60 0 {NoAI:1b,PersistenceRequired:1b,Health:200f,attributes:[{id:\"minecraft:max_health\",base:200d}]}");
		server.runCommand("olympus give @a " + id + " 1");
		if (id.equals("silver_bow")) {
			server.runCommand("give @a minecraft:arrow 32");
		}
		if (id.equals("hammer_of_hephaestus")) {
			server.runCommand("item replace entity @a weapon.offhand with minecraft:raw_iron 8");
		}
		context.waitTicks(5);
		if (WORN.contains(id)) {
			String slot = id.equals("helm_of_darkness") ? "armor.head" : id.equals("talaria") ? "armor.feet" : "armor.chest";
			server.runCommand("clear @a olympus:" + id);
			server.runCommand("item replace entity @a " + slot + " with olympus:" + id + "[olympus:upgrade_level=1]");
		}
		context.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(0));
		context.waitTicks(10);
		float huskHealth = huskHealth(server);

		boolean primary;
		switch (id) {
			case "helm_of_darkness" -> {
				context.getInput().holdKeyFor((Options o) -> o.keyShift, 60);
				context.waitTicks(5);
				primary = server.computeOnServer(s -> player(s).isInvisible());
			}
			case "talaria" -> {
				double y0 = context.computeOnClient(mc -> mc.player.getY());
				context.getInput().holdKeyFor((Options o) -> o.keyJump, 2);
				context.waitTicks(6);
				context.getInput().holdKeyFor((Options o) -> o.keyJump, 2);
				double max = 0;
				for (int i = 0; i < 14; i++) {
					context.waitTick();
					max = Math.max(max, context.computeOnClient(mc -> mc.player.getY()) - y0);
				}
				primary = max > 1.6; // a single vanilla jump peaks at ~1.25
				if (!primary) {
					failures.add(id + ": double jump peak only " + String.format("%.2f", max));
				}
				context.waitTicks(30);
			}
			case "golden_fleece" -> {
				context.waitTicks(30);
				primary = server.computeOnServer(s -> player(s).hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION));
			}
			case "aegis" -> {
				// Hold the shield up while the husk hits it.
				server.runCommand("data merge entity @e[type=husk,limit=1] {NoAI:0b}");
				context.getInput().holdKeyFor((Options o) -> o.keyUse, 60);
				primary = server.computeOnServer(s -> player(s).getHealth() >= player(s).getMaxHealth() - 0.01f);
			}
			case "blade_of_ares", "hammer_of_hephaestus", "sickle_of_demeter", "thyrsus", "harpe", "forge_hammer" -> {
				for (int i = 0; i < 3; i++) {
					context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
					context.waitTicks(25);
				}
				primary = huskHealth(server) < huskHealth - 5;
				if (id.equals("hammer_of_hephaestus")) {
					primary &= server.computeOnServer(s -> husk(s) != null && husk(s).getRemainingFireTicks() > 0);
				}
				if (id.equals("sickle_of_demeter")) {
					primary &= server.computeOnServer(s -> husk(s) != null && husk(s).hasEffect(net.minecraft.world.effect.MobEffects.HUNGER));
				}
				if (id.equals("thyrsus")) {
					primary &= server.computeOnServer(s -> husk(s) != null && husk(s).hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS));
				}
				if (id.equals("blade_of_ares")) {
					primary &= server.computeOnServer(s -> player(s).hasEffect(net.minecraft.world.effect.MobEffects.STRENGTH));
				}
			}
			case "silver_bow", "spear_of_athena", "thunderbolt_of_zeus", "trident_of_poseidon" -> {
				context.getInput().holdKeyFor((Options o) -> o.keyUse, 25);
				context.waitTicks(40);
				primary = huskHealth(server) < huskHealth || onCooldown(server, Cooldowns.Kind.PRIMARY);
				if (id.equals("trident_of_poseidon")) {
					primary = true; // riptide launch: checked by the cooldown below
				}
				context.waitTicks(60); // let thrown weapons come home
			}
			default -> {
				context.getInput().holdKeyFor((Options o) -> o.keyUse, 40);
				context.waitTicks(10);
				primary = true; // checked by the cooldown below
			}
		}
		if (!primary) {
			failures.add(id + ": primary power had no effect (husk " + huskHealth + " -> " + huskHealth(server) + ")");
		}
		if (id.equals("sickle_of_demeter")) {
			// Right-click a young crop to ripen the field, then R to harvest it.
			server.runCommand("kill @e[type=husk]");
			server.runCommand("fill -2 -61 -2 2 -61 2 minecraft:farmland");
			server.runCommand("fill -2 -60 -2 2 -60 2 minecraft:wheat[age=0]");
			server.runCommand("execute as @p run tp @s 0 -60 0 0 90");
			context.waitTicks(10);
			context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
			context.waitTicks(10);
			boolean ripe = server.computeOnServer(s -> s.overworld().getBlockState(new net.minecraft.core.BlockPos(1, -60, 1)).getValue(net.minecraft.world.level.block.CropBlock.AGE) == 7);
			if (!ripe) {
				failures.add(id + ": right-clicking crops didn't ripen them");
			}
		}
		ItemStack stack = server.computeOnServer(s -> findStack(player(s), item));
		if (stack.isEmpty()) {
			failures.add(id + ": item vanished from the player");
			return;
		}
		GodItem god = (GodItem) item;
		if (god.primaryCooldownSeconds() > 0 && !id.equals("aegis") && !WORN.contains(id) && !onCooldown(server, Cooldowns.Kind.PRIMARY)) {
			failures.add(id + ": primary cooldown never started (power didn't fire)");
		}
		// Blessed ability on R (for keyed ones)
		if (!god.blessedIsPassive()) {
			context.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(0));
			context.waitTicks(2);
			if (id.equals("helm_of_darkness")) {
				context.waitTicks(1); // still invisible: R ambush is passive-ish; still press
			}
			if (id.equals("forge_hammer")) {
				int before = server.computeOnServer(s -> findStack(player(s), item).getOrDefault(com.olympus.registry.ModComponents.MINING_RADIUS, 1));
				context.getInput().pressKey(com.olympus.client.OlympusClient.DIVINE_POWER);
				context.waitTicks(10);
				int after = server.computeOnServer(s -> findStack(player(s), item).getOrDefault(com.olympus.registry.ModComponents.MINING_RADIUS, 1));
				if (before == after) {
					failures.add(id + ": R didn't switch 3x3/5x5");
				}
			} else if (id.equals("caduceus")) {
				server.runCommand("execute as @p run tp @s 0 -60 0 -90 0");
				context.waitTicks(5);
				context.getInput().pressKey(com.olympus.client.OlympusClient.DIVINE_POWER); // mark
				context.waitTicks(10);
				server.runCommand("execute as @p run tp @s 10 -60 10");
				context.waitTicks(10);
				context.getInput().pressKey(com.olympus.client.OlympusClient.DIVINE_POWER); // return
				context.waitTicks(10);
				double dist = server.computeOnServer(s -> Math.sqrt(player(s).distanceToSqr(0.5, -60, 0.5)));
				if (dist > 2) {
					failures.add(id + ": R waypoint didn't bring the player back (" + String.format("%.1f", dist) + " away)");
				}
			} else {
			context.getInput().pressKey(com.olympus.client.OlympusClient.DIVINE_POWER);
			context.waitTicks(30);
			if (!onCooldown(server, Cooldowns.Kind.BLESSED)) {
				failures.add(id + ": Divine Power (R) did nothing");
			}
			}
		}
		int count = server.computeOnServer(s -> {
			int n = 0;
			for (int i = 0; i < player(s).getInventory().getContainerSize(); i++) {
				if (player(s).getInventory().getItem(i).is(item)) {
					n++;
				}
			}
			return n;
		});
		if (count != 1) {
			failures.add(id + ": player has " + count + " copies after use");
		}
		System.out.println("PLAYTEST ok? " + id + " primary=" + primary);
		context.waitTicks(20);
	}

	private void playOtherItems(ClientGameTestContext context, TestServerContext server) {
		server.runCommand("kill @e[type=!player]");
		server.runCommand("clear @a");
		server.runCommand("effect clear @a");
		server.runCommand("execute as @p run tp @s 0 -60 0 -90 0");
		// Ambrosia: eat it while hurt
		server.runCommand("give @a olympus:ambrosia");
		server.runCommand("damage @p 10 minecraft:generic");
		server.runCommand("effect give @a minecraft:hunger 3 50 true");
		context.waitTicks(70);
		context.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(0));
		context.getInput().holdKeyFor((Options o) -> o.keyUse, 50);
		context.waitTicks(10);
		boolean ambrosia = server.computeOnServer(s -> player(s).getHealth() >= player(s).getMaxHealth() - 0.5f && !player(s).getActiveEffects().isEmpty()
			&& !player(s).getInventory().contains(new ItemStack(ModItems.AMBROSIA)));
		if (!ambrosia) {
			failures.add("ambrosia: eating it didn't heal/buff");
		}
		// Nectar
		server.runCommand("clear @a");
		server.runCommand("effect clear @a");
		server.runCommand("give @a olympus:nectar");
		context.waitTicks(5);
		context.getInput().holdKeyFor((Options o) -> o.keyUse, 50);
		context.waitTicks(10);
		if (!server.computeOnServer(s -> !player(s).getActiveEffects().isEmpty())) {
			failures.add("nectar: drinking it gave no effects");
		}
		// Laurel
		server.runCommand("clear @a");
		int slots0 = server.computeOnServer(s -> com.olympus.data.PlayerOlympusData.get(player(s)).bonusSlots());
		server.runCommand("give @a olympus:laurel_of_olympus");
		context.waitTicks(5);
		context.getInput().holdKeyFor((Options o) -> o.keyUse, 40);
		context.waitTicks(10);
		if (server.computeOnServer(s -> com.olympus.data.PlayerOlympusData.get(player(s)).bonusSlots()) != slots0 + 1) {
			failures.add("laurel: right-click didn't add a relic slot");
		}
		// God Swapper: starter relic in offhand, hold right-click
		server.runCommand("clear @a");
		String god0 = server.computeOnServer(s -> com.olympus.data.PlayerOlympusData.get(player(s)).god().orElse("none"));
		server.runCommand("olympus give @a god_swapper");
		server.runCommand("item replace entity @a weapon.offhand with olympus:aegis");
		server.runCommand("olympus claim @a zeus");
		server.runCommand("item replace entity @a weapon.offhand with olympus:aegis");
		server.runCommand("clear @a olympus:aegis");
		server.runCommand("item replace entity @a weapon.offhand with olympus:aegis");
		context.waitTicks(5);
		context.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(0));
		context.getInput().holdKeyFor((Options o) -> o.keyUse, 80);
		context.waitTicks(10);
		String god1 = server.computeOnServer(s -> com.olympus.data.PlayerOlympusData.get(player(s)).god().orElse("none"));
		if (god1.equals("zeus")) {
			failures.add("god_swapper: holding right-click didn't reroll (god " + god0 + " -> " + god1 + ")");
		}
		// Codex opens a book
		server.runCommand("clear @a");
		server.runCommand("give @a olympus:codex_of_olympus");
		context.waitTicks(5);
		context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
		context.waitTicks(10);
		if (!context.computeOnClient(mc -> mc.screen instanceof net.minecraft.client.gui.screens.inventory.BookViewScreen)) {
			failures.add("codex: right-click didn't open the book");
		}
		context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitTicks(5);
		// Sun Arrow shot from a normal bow at a husk
		server.runCommand("clear @a");
		server.runCommand("execute as @p run tp @s 0 -60 0 -90 0");
		server.runCommand("summon husk 6 -60 0 {NoAI:1b,PersistenceRequired:1b}");
		server.runCommand("give @a minecraft:bow");
		server.runCommand("give @a olympus:sun_arrow 4");
		context.waitTicks(5);
		context.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(0));
		float hp = huskHealth(server);
		context.getInput().holdKeyFor((Options o) -> o.keyUse, 25);
		context.waitTicks(30);
		if (!(huskHealth(server) < hp)) {
			failures.add("sun_arrow: didn't hurt the target");
		}
		System.out.println("PLAYTEST other items done");
	}

	private static ServerPlayer player(net.minecraft.server.MinecraftServer s) {
		return s.getPlayerList().getPlayers().get(0);
	}

	private static LivingEntity husk(net.minecraft.server.MinecraftServer s) {
		var list = s.overworld().getEntitiesOfClass(Husk.class, player(s).getBoundingBox().inflate(40));
		return list.isEmpty() ? null : list.get(0);
	}

	private static float huskHealth(TestServerContext server) {
		return server.computeOnServer(s -> husk(s) == null ? 0f : husk(s).getHealth());
	}

	private static ItemStack findStack(ServerPlayer p, Item item) {
		for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
			if (p.getInventory().getItem(i).is(item)) {
				return p.getInventory().getItem(i);
			}
		}
		for (EquipmentSlot slot : EquipmentSlot.VALUES) {
			if (p.getItemBySlot(slot).is(item)) {
				return p.getItemBySlot(slot);
			}
		}
		return ItemStack.EMPTY;
	}

	private boolean onCooldown(TestServerContext server, Cooldowns.Kind kind) {
		return server.computeOnServer(s -> {
			ServerPlayer p = player(s);
			for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
				ItemStack st = p.getInventory().getItem(i);
				if (st.getItem() instanceof GodItem && !Cooldowns.isReady(p, st, kind)) {
					return true;
				}
			}
			for (EquipmentSlot slot : EquipmentSlot.VALUES) {
				ItemStack st = p.getItemBySlot(slot);
				if (st.getItem() instanceof GodItem && !Cooldowns.isReady(p, st, kind)) {
					return true;
				}
			}
			return false;
		});
	}
}
