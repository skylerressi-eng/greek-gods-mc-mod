package com.olympus.test;

import com.olympus.Olympus;
import com.olympus.ability.Cooldowns;
import com.olympus.claim.ClaimManager;
import com.olympus.config.ConfigManager;
import com.olympus.data.PlayerOlympusData;
import com.olympus.god.God;
import com.olympus.god.GodItem;
import com.olympus.handler.HephaestusHandler;
import com.olympus.item.relic.GobletItem;
import com.olympus.item.relic.SickleItem;
import com.olympus.registry.ModComponents;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModKeys;
import com.olympus.util.DivineLightning;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;

/** Run with {@code ./gradlew runGameTest}. Each test checks one Phase 1 promise. */
public class OlympusGameTests {
	@GameTest
	public void starterRelicsAreUnbreakableGodItems(GameTestHelper helper) {
		helper.assertTrue(ModItems.STARTER_RELICS.size() == 12, "expected 12 starter relics");
		for (Item item : ModItems.STARTER_RELICS) {
			ItemStack stack = new ItemStack(item);
			helper.assertTrue(item instanceof GodItem, item + " is not a GodItem");
			helper.assertFalse(stack.has(DataComponents.MAX_DAMAGE), item + " has durability");
			helper.assertFalse(stack.isDamageableItem(), item + " can be damaged");
			helper.assertTrue(stack.getOrDefault(ModComponents.UPGRADE_LEVEL, -1) == 0, item + " is not Mortal by default");
			helper.assertTrue(stack.is(ModKeys.GOD_ITEMS), item + " missing from #olympus:god_items");
			helper.assertTrue(stack.is(ModKeys.STARTER_RELICS), item + " missing from #olympus:starter_relics");
		}
		helper.succeed();
	}

	@GameTest
	public void netheriteStatsOnWeaponsAndArmor(GameTestHelper helper) {
		ItemStack blade = new ItemStack(ModItems.BLADE_OF_ARES);
		ItemStack vanilla = new ItemStack(Items.NETHERITE_SWORD);
		helper.assertTrue(blade.get(DataComponents.ATTRIBUTE_MODIFIERS).equals(vanilla.get(DataComponents.ATTRIBUTE_MODIFIERS)),
			"Blade of Ares should match netherite sword attributes");
		helper.assertTrue(new ItemStack(ModItems.HELM_OF_DARKNESS).get(DataComponents.ATTRIBUTE_MODIFIERS)
			.equals(new ItemStack(Items.NETHERITE_HELMET).get(DataComponents.ATTRIBUTE_MODIFIERS)), "Helm should match netherite helmet");
		helper.succeed();
	}

	@GameTest
	public void aegisCarriesTheGorgoneion(GameTestHelper helper) {
		ItemStack aegis = new ItemStack(ModItems.AEGIS);
		com.olympus.item.relic.AegisItem.applyEmblem(aegis, helper.getLevel().registryAccess());
		var patterns = aegis.get(DataComponents.BANNER_PATTERNS);
		helper.assertTrue(patterns != null && patterns.layers().stream().anyMatch(l -> l.pattern().is(ModKeys.GORGONEION)),
			"Aegis should carry the Gorgoneion banner pattern");
		helper.assertTrue(new ItemStack(ModItems.AEGIS).has(DataComponents.BLOCKS_ATTACKS), "Aegis must block like a shield");
		helper.succeed();
	}

	@GameTest
	public void recipesAndRegistriesLoad(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (String id : List.of("olympian_bronze_ingot_from_alloy", "olympian_bronze_block", "olympian_bronze_ingot",
			"divine_smithing_template", "blessing_core", "ascension_core", "god_swapper")) {
			ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, Olympus.id(id));
			helper.assertTrue(level.getServer().getRecipeManager().byKey(key).isPresent(), "missing recipe " + id);
		}
		helper.assertTrue(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(ModKeys.DIVINE_LIGHTNING).isPresent(), "missing damage type");
		helper.assertTrue(level.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN).get(ModKeys.THUNDERBOLT).isPresent(), "missing banner pattern");
		helper.succeed();
	}

	@GameTest
	public void claimingGivesTheStarterRelicOnce(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		ClaimManager.tryAutoClaim(player);
		PlayerOlympusData data = PlayerOlympusData.get(player);
		helper.assertTrue(data.claimed() && data.claimedGod().isPresent(), "player was not claimed");
		Item relic = ClaimManager.starterRelic(data.claimedGod().get());
		helper.assertTrue(player.getInventory().contains(new ItemStack(relic)), "player did not receive " + relic);
		int before = countGodItems(player);
		ClaimManager.tryAutoClaim(player); // second join
		helper.assertTrue(countGodItems(player) == before, "a second relic was granted");
		helper.succeed();
	}

	@GameTest
	public void everyGodMapsToItsOwnRelic(GameTestHelper helper) {
		for (God god : God.OLYMPIANS) {
			Item relic = ClaimManager.starterRelic(god);
			helper.assertTrue(((GodItem) relic).god() == god, relic + " does not belong to " + god);
			helper.assertTrue(ClaimManager.godOfRelic(relic) == god, "reverse lookup failed for " + god);
		}
		helper.succeed();
	}

	@GameTest
	public void divineLightningSparesTheCaster(GameTestHelper helper) {
		ServerPlayer caster = helper.makeMockServerPlayerInLevel();
		Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		caster.snapTo(zombie.getX(), zombie.getY(), zombie.getZ());
		float casterHealth = caster.getHealth();
		float zombieHealth = zombie.getHealth();
		DivineLightning.strike(helper.getLevel(), zombie.position(), caster, 6.0f, 1.5);
		helper.assertTrue(caster.getHealth() == casterHealth, "lightning hurt its caster");
		helper.assertTrue(zombie.getHealth() < zombieHealth, "lightning did not hurt the zombie");
		helper.assertBlockNotPresent(Blocks.FIRE, helper.relativePos(zombie.blockPosition()));
		helper.succeed();
	}

	@GameTest
	public void hammerAutoSmeltsOres(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		List<ItemStack> out = HephaestusHandler.smeltDrops(Blocks.IRON_ORE.defaultBlockState(), level, pos,
			new ItemStack(ModItems.HAMMER_OF_HEPHAESTUS), List.of(new ItemStack(Items.RAW_IRON, 2)));
		helper.assertTrue(out.size() == 1 && out.get(0).is(Items.IRON_INGOT) && out.get(0).getCount() == 2, "expected 2 iron ingots, got " + out);
		List<ItemStack> stone = HephaestusHandler.smeltDrops(Blocks.STONE.defaultBlockState(), level, pos,
			new ItemStack(ModItems.HAMMER_OF_HEPHAESTUS), List.of(new ItemStack(Items.COBBLESTONE)));
		helper.assertTrue(stone.get(0).is(Items.COBBLESTONE), "non-ores must not be smelted");
		helper.succeed();
	}

	@GameTest
	public void sickleRipensCropsButNotSaplings(GameTestHelper helper) {
		BlockPos farmland = new BlockPos(1, 1, 1);
		helper.setBlock(farmland, Blocks.FARMLAND);
		helper.setBlock(farmland.above(), Blocks.WHEAT);
		helper.setBlock(new BlockPos(3, 1, 1), Blocks.GRASS_BLOCK);
		helper.setBlock(new BlockPos(3, 2, 1), Blocks.OAK_SAPLING);
		SickleItem.growFully(helper.getLevel(), helper.absolutePos(farmland.above()));
		helper.assertBlockProperty(farmland.above(), CropBlock.AGE, 7);
		helper.assertFalse(SickleItem.growFully(helper.getLevel(), helper.absolutePos(new BlockPos(3, 2, 1))), "sapling was grown");
		helper.assertBlockPresent(Blocks.OAK_SAPLING, new BlockPos(3, 2, 1));
		helper.succeed();
	}

	@GameTest
	public void cooldownsScaleWithLevelAndFollowTheStack(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		ItemStack mortal = new ItemStack(ModItems.OWL_OF_ATHENA);
		Cooldowns.start(player, mortal, Cooldowns.Kind.PRIMARY, 100);
		helper.assertFalse(Cooldowns.isReady(player, mortal, Cooldowns.Kind.PRIMARY), "cooldown did not start");
		helper.assertTrue(Cooldowns.remainingTicks(player, mortal, Cooldowns.Kind.PRIMARY) == 2000, "Mortal cooldown should be 100s");

		ServerPlayer friend = helper.makeMockServerPlayerInLevel();
		helper.assertFalse(Cooldowns.isReady(friend, mortal, Cooldowns.Kind.PRIMARY), "handing the relic over reset its cooldown");

		ItemStack blessed = new ItemStack(ModItems.LYRE_OF_APOLLO);
		blessed.set(ModComponents.UPGRADE_LEVEL, 1);
		Cooldowns.start(friend, blessed, Cooldowns.Kind.PRIMARY, 100);
		helper.assertTrue(Cooldowns.remainingTicks(friend, blessed, Cooldowns.Kind.PRIMARY) == 1500, "Blessed cooldown should be 75s");
		helper.succeed();
	}

	@GameTest
	public void gobletPoolIsValid(GameTestHelper helper) {
		for (int i = 0; i < 50; i++) {
			var effect = GobletItem.randomEffect(helper.getLevel().getRandom());
			helper.assertTrue(effect.value().isBeneficial(), "Goblet rolled a harmful effect: " + effect);
		}
		helper.assertTrue(ConfigManager.toJson().contains("maxGodItems"), "config JSON incomplete");
		helper.succeed();
	}

	@GameTest
	public void owlMarkAddsDamage(GameTestHelper helper) {
		ServerPlayer owner = helper.makeMockServerPlayerInLevel();
		Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		com.olympus.handler.CombatHandler.markByOwl(zombie, owner, 200);
		float out = com.olympus.handler.CombatHandler.modifyIncomingDamage(zombie, owner.damageSources().playerAttack(owner), 10f);
		helper.assertTrue(Math.abs(out - 11.5f) < 0.01f, "Owl mark should add 15%, got " + out);
		ServerPlayer stranger = helper.makeMockServerPlayerInLevel();
		float other = com.olympus.handler.CombatHandler.modifyIncomingDamage(zombie, stranger.damageSources().playerAttack(stranger), 10f);
		helper.assertTrue(other == 10f, "mark bonus must only apply to the Owl's owner");
		helper.succeed();
	}

	@GameTest
	public void mirrorCharmStopsPlayerDamage(GameTestHelper helper) {
		ServerPlayer charmer = helper.makeMockServerPlayerInLevel();
		ServerPlayer attacker = helper.makeMockServerPlayerInLevel();
		charmer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
		attacker.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
		com.olympus.handler.CombatHandler.charm(attacker, charmer, 200);
		float before = charmer.getHealth();
		charmer.hurtServer(helper.getLevel(), attacker.damageSources().playerAttack(attacker), 5f);
		helper.assertTrue(charmer.getHealth() == before, "charmed player still hurt the charmer");
		helper.succeed();
	}

	@GameTest
	public void hammerSetsEnemiesOnFire(GameTestHelper helper) {
		ServerPlayer smith = helper.makeMockServerPlayerInLevel();
		smith.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ModItems.HAMMER_OF_HEPHAESTUS));
		Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		zombie.hurtServer(helper.getLevel(), smith.damageSources().playerAttack(smith), 2f);
		helper.assertTrue(zombie.getRemainingFireTicks() > 0, "Hammer hit did not ignite");
		helper.succeed();
	}

	@GameTest
	public void smithingUpgradesKeepEnchantments(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ItemStack blade = new ItemStack(ModItems.BLADE_OF_ARES);
		var sharpness = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS);
		blade.enchant(sharpness, 5);
		var input = new net.minecraft.world.item.crafting.SmithingRecipeInput(new ItemStack(ModItems.BLESSING_CORE), blade, new ItemStack(ModItems.OLYMPIAN_BRONZE_BLOCK));
		var match = level.recipeAccess().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMITHING, input, level);
		helper.assertTrue(match.isPresent(), "no smithing recipe for Blessing Core + Blade");
		ItemStack blessed = match.get().value().assemble(input, level.registryAccess());
		helper.assertTrue(GodItem.level(blessed) == com.olympus.god.UpgradeLevel.BLESSED, "not Blessed after smithing");
		helper.assertTrue(net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(sharpness, blessed) == 5, "lost Sharpness");
		// Ascension needs a Blessed item; a Mortal one must not match.
		var wrong = new net.minecraft.world.item.crafting.SmithingRecipeInput(new ItemStack(ModItems.ASCENSION_CORE), new ItemStack(ModItems.BLADE_OF_ARES), new ItemStack(ModItems.OLYMPIAN_BRONZE_BLOCK));
		helper.assertTrue(level.recipeAccess().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMITHING, wrong, level).isEmpty(), "Mortal item could be Ascended");
		var ascend = new net.minecraft.world.item.crafting.SmithingRecipeInput(new ItemStack(ModItems.ASCENSION_CORE), blessed, new ItemStack(ModItems.OLYMPIAN_BRONZE_BLOCK));
		ItemStack ascended = level.recipeAccess().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMITHING, ascend, level).orElseThrow().value().assemble(ascend, level.registryAccess());
		helper.assertTrue(GodItem.level(ascended) == com.olympus.god.UpgradeLevel.ASCENDED, "not Ascended");
		helper.succeed();
	}

	@GameTest
	public void noUnbreakingOrMendingOnGodItems(GameTestHelper helper) {
		var enchants = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		ItemStack blade = new ItemStack(ModItems.BLADE_OF_ARES);
		helper.assertTrue(enchants.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS).value().canEnchant(blade), "Sharpness should apply");
		helper.assertFalse(enchants.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING).value().canEnchant(blade), "Unbreaking applied");
		helper.assertFalse(enchants.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING).value().canEnchant(blade), "Mending applied");
		helper.assertFalse(enchants.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.VANISHING_CURSE).value().canEnchant(blade), "Vanishing applied");
		helper.assertTrue(enchants.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.INFINITY).value().canEnchant(new ItemStack(ModItems.SILVER_BOW)), "Infinity should apply to the Silver Bow");
		helper.succeed();
	}

	@GameTest
	public void godSwapperRerollsAndKeepsLevel(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		ItemStack relic = new ItemStack(ModItems.OWL_OF_ATHENA);
		relic.set(ModComponents.UPGRADE_LEVEL, 2);
		player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, relic);
		ItemStack swapper = new ItemStack(ModItems.GOD_SWAPPER);
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, swapper);
		swapper.getItem().finishUsingItem(swapper, helper.getLevel(), player);
		ItemStack result = player.getOffhandItem();
		helper.assertTrue(result.getItem() instanceof GodItem g && g.isStarterRelic() && !result.is(ModItems.OWL_OF_ATHENA), "relic was not rerolled: " + result);
		helper.assertTrue(GodItem.level(result) == com.olympus.god.UpgradeLevel.ASCENDED, "upgrade level lost");
		helper.assertTrue(swapper.isEmpty(), "swapper was not used up");
		helper.assertTrue(PlayerOlympusData.get(player).claimedGod().orElse(null) == ((GodItem) result.getItem()).god(), "claimed god not updated");
		helper.succeed();
	}

	@GameTest
	public void carryLimitIsThree(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
		player.getInventory().clearContent(); // the mock player was just claimed and holds a starter relic
		player.getInventory().add(new ItemStack(ModItems.AEGIS));
		player.getInventory().add(new ItemStack(ModItems.OWL_OF_ATHENA));
		helper.assertTrue(com.olympus.limit.GodItemLimit.canTakeAnother(player), "should allow a third");
		player.getInventory().add(new ItemStack(ModItems.LYRE_OF_APOLLO));
		helper.assertFalse(com.olympus.limit.GodItemLimit.canTakeAnother(player), "allowed a fourth");
		player.getInventory().add(new ItemStack(ModItems.TALARIA)); // e.g. via /give
		com.olympus.limit.GodItemLimit.sweep(player);
		helper.assertTrue(com.olympus.limit.GodItemLimit.count(player) == 3, "sweep did not drop the extra");
		helper.succeed();
	}

	@GameTest
	public void droppedRelicsAreIndestructible(GameTestHelper helper) {
		var item = new net.minecraft.world.entity.item.ItemEntity(helper.getLevel(), helper.absoluteVec(new net.minecraft.world.phys.Vec3(1, 2, 1)).x,
			helper.absoluteVec(new net.minecraft.world.phys.Vec3(1, 2, 1)).y, helper.absoluteVec(new net.minecraft.world.phys.Vec3(1, 2, 1)).z, new ItemStack(ModItems.BLADE_OF_ARES));
		helper.getLevel().addFreshEntity(item);
		item.hurtServer(helper.getLevel(), helper.getLevel().damageSources().explosion(null, null), 100f);
		item.hurtServer(helper.getLevel(), helper.getLevel().damageSources().cactus(), 100f);
		item.hurtServer(helper.getLevel(), helper.getLevel().damageSources().lava(), 100f);
		helper.assertTrue(item.isAlive(), "relic item entity was destroyed");
		helper.assertFalse(net.minecraft.world.item.component.BundleContents.canItemBeInBundle(new ItemStack(ModItems.BLADE_OF_ARES)), "relic fits in a bundle");
		helper.succeed();
	}

	@GameTest
	public void divinePowerUsesBlessedAbility(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		ItemStack owl = new ItemStack(ModItems.OWL_OF_ATHENA);
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, owl);
		com.olympus.ability.DivinePower.onKey(player);
		helper.assertFalse(player.hasEffect(net.minecraft.world.effect.MobEffects.STRENGTH), "Mortal relic used its Blessed ability");
		owl.set(ModComponents.UPGRADE_LEVEL, 1);
		helper.runAfterDelay(5, () -> {
			com.olympus.ability.DivinePower.onKey(player);
			helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.STRENGTH), "Battle Wisdom did not grant Strength");
			helper.assertFalse(Cooldowns.isReady(player, owl, Cooldowns.Kind.BLESSED), "Blessed cooldown not started");
			helper.succeed();
		});
	}

	private static int countGodItems(ServerPlayer player) {
		int n = 0;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			if (GodItem.is(player.getInventory().getItem(i))) {
				n++;
			}
		}
		return n;
	}
}
