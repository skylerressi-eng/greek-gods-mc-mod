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

	@GameTest
	public void phase3RecipesLoad(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (String id : List.of("caduceus", "thyrsus", "cornucopia", "spear_of_athena", "forge_hammer", "harpe", "ambrosia", "nectar",
			"sun_arrow", "codex_of_olympus", "hoplite_helmet_smithing", "hoplite_chestplate_smithing", "hoplite_leggings_smithing",
			"hoplite_boots_smithing", "blessed_upgrade", "ascended_upgrade")) {
			helper.assertTrue(level.getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, Olympus.id(id))).isPresent(), "missing recipe " + id);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 100)
	public void bossEventAwardsTheLegendaryOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var server = level.getServer();
		com.olympus.data.LegendaryRegistry.remove(server, "head_of_medusa");
		com.olympus.event.EventManager.stop(server, net.minecraft.network.chat.Component.empty());
		ServerPlayer hero = helper.makeMockServerPlayerInLevel();
		hero.getInventory().clearContent();
		BlockPos at = helper.absolutePos(new BlockPos(1, 1, 1));
		var error = com.olympus.event.EventManager.start(com.olympus.event.OlympusEvent.GORGONS_LAIR, level, at);
		helper.assertTrue(error.isEmpty(), "event did not start: " + error);
		var boss = level.getEntitiesOfClass(com.olympus.entity.MedusaEntity.class, new net.minecraft.world.phys.AABB(at).inflate(4)).stream().findFirst().orElseThrow();
		helper.assertTrue(boss.getMaxHealth() >= 500f, "boss health not scaled");
		boss.hurtServer(level, hero.damageSources().playerAttack(hero), 100000f);
		helper.assertTrue(boss.isDeadOrDying(), "boss did not die");
		helper.assertTrue(hero.getInventory().contains(st -> st.is(ModItems.HEAD_OF_MEDUSA)), "top damage dealer did not get the legendary");
		helper.assertTrue(com.olympus.data.LegendaryRegistry.get(server, "head_of_medusa").isPresent(), "legendary not registered");

		// A second victory must not create a second Head of Medusa.
		hero.getInventory().clearContent();
		com.olympus.event.EventManager.start(com.olympus.event.OlympusEvent.GORGONS_LAIR, level, at);
		var boss2 = level.getEntitiesOfClass(com.olympus.entity.MedusaEntity.class, new net.minecraft.world.phys.AABB(at).inflate(4), e -> e.isAlive()).stream().findFirst().orElseThrow();
		boss2.hurtServer(level, hero.damageSources().playerAttack(hero), 100000f);
		helper.assertFalse(hero.getInventory().contains(st -> st.is(ModItems.HEAD_OF_MEDUSA)), "a second legendary was created");
		helper.assertTrue(hero.getInventory().contains(st -> st.is(ModItems.OLYMPIAN_BRONZE_INGOT)), "no consolation reward");
		com.olympus.data.LegendaryRegistry.remove(server, "head_of_medusa");
		helper.succeed();
	}

	@GameTest
	public void cetusNeedsTheOcean(GameTestHelper helper) {
		var error = com.olympus.event.EventManager.start(com.olympus.event.OlympusEvent.RISE_OF_THE_DEEP, helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)));
		helper.assertTrue(error.isPresent(), "Cetus spawned on dry land");
		var nether = com.olympus.event.OlympusEvent.GATES_OF_THE_UNDERWORLD.checkLocation(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)));
		helper.assertTrue(nether.isPresent(), "Cerberus allowed outside the Nether");
		helper.succeed();
	}

	@GameTest
	public void duplicateLegendariesCrumble(GameTestHelper helper) {
		var server = helper.getLevel().getServer();
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
		player.getInventory().clearContent();
		com.olympus.data.LegendaryRegistry.register(server, "scythe_of_kronos", new com.olympus.data.LegendaryRegistry.Entry(java.util.UUID.randomUUID(), "Original", "Original"));
		ItemStack fake = new ItemStack(ModItems.SCYTHE_OF_KRONOS);
		fake.set(ModComponents.LEGENDARY, new ModComponents.Legendary(java.util.UUID.randomUUID(), "Copycat"));
		player.getInventory().add(fake);
		com.olympus.event.EventManager.guardLegendaries(player);
		helper.assertFalse(player.getInventory().contains(st -> st.is(ModItems.SCYTHE_OF_KRONOS)), "duplicate legendary survived");
		com.olympus.data.LegendaryRegistry.remove(server, "scythe_of_kronos");
		helper.succeed();
	}

	@GameTest
	public void petrifyFreezesMobs(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		helper.assertTrue(com.olympus.handler.PetrifyHandler.petrify(zombie, player, 80), "petrify failed");
		helper.assertTrue(zombie.isNoAi() && zombie.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) == 0, "zombie can still move");
		helper.succeed();
	}

	@GameTest
	public void forgeHammerMinesThreeByThree(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ModItems.FORGE_HAMMER));
		for (int x = 0; x < 3; x++) for (int y = 1; y < 4; y++) helper.setBlock(new BlockPos(x, y, 2), Blocks.STONE);
		var eye = helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2.5 - 1.62, 0.5));
		player.snapTo(eye.x, eye.y, eye.z, 0f, 0f); // facing +Z, looking at the wall
		player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(1, 2, 2)));
		for (int x = 0; x < 3; x++) for (int y = 1; y < 4; y++) helper.assertBlockNotPresent(Blocks.STONE, new BlockPos(x, y, 2));
		helper.succeed();
	}

	@GameTest
	public void laurelRaisesTheLimit(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
		helper.assertTrue(com.olympus.limit.GodItemLimit.max(player) == 3, "base limit should be 3");
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ModItems.LAUREL_OF_OLYMPUS, 3));
		for (int i = 0; i < 3; i++) {
			ModItems.LAUREL_OF_OLYMPUS.use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
		}
		helper.assertTrue(com.olympus.limit.GodItemLimit.max(player) == 5, "limit should cap at 5, got " + com.olympus.limit.GodItemLimit.max(player));
		helper.assertTrue(player.getMainHandItem().getCount() == 1, "the third Laurel should not be used up");
		helper.succeed();
	}

	@GameTest
	public void buffsOnlyReachAllies(GameTestHelper helper) {
		ServerPlayer caster = helper.makeMockServerPlayerInLevel();
		ServerPlayer friend = helper.makeMockServerPlayerInLevel();
		ServerPlayer enemy = helper.makeMockServerPlayerInLevel();
		// Stand them together inside the (loaded) test area.
		for (ServerPlayer p : List.of(caster, friend, enemy)) {
			var at = helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 1, 1.5));
			p.snapTo(at.x, at.y, at.z, 0, 0);
		}
		helper.assertFalse(com.olympus.util.Targets.alliesAround(caster, 10).contains(enemy), "a non-ally got buffs");
		helper.assertTrue(com.olympus.util.Targets.alliesAround(caster, 10).contains(caster), "caster should buff themselves");
		PlayerOlympusData.set(caster, PlayerOlympusData.get(caster).withAllies(List.of(friend.getUUID())));
		helper.assertTrue(com.olympus.util.Targets.alliesAround(caster, 10).contains(friend), "ally did not get buffs");
		helper.assertFalse(com.olympus.util.Targets.alliesAround(caster, 10).contains(enemy), "enemy got buffs");
		helper.assertFalse(com.olympus.util.Targets.canHarm(caster, friend), "powers hurt an ally");
		helper.succeed();
	}

	@GameTest(maxTicks = 120)
	public void vineSnareRootsWithoutLaunching(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		double startY = zombie.getY();
		com.olympus.handler.PetrifyHandler.root(zombie, helper.getLevel(), 60);
		helper.assertTrue(zombie.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH) == 0, "rooted mob can still jump");
		helper.assertTrue(zombie.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) == 0, "rooted mob can still walk");
		helper.assertFalse(zombie.hasEffect(net.minecraft.world.effect.MobEffects.JUMP_BOOST), "jump boost would launch them");
		helper.runAfterDelay(80, () -> {
			helper.assertTrue(zombie.getY() < startY + 2, "rooted mob flew into the air");
			helper.assertTrue(zombie.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) > 0, "root never wore off");
			helper.succeed();
		});
	}

	@GameTest
	public void disablingTheAegisHurtsTheAttacker(GameTestHelper helper) {
		ServerPlayer defender = helper.makeMockServerPlayerInLevel();
		var vindicator = helper.spawn(EntityType.VINDICATOR, new BlockPos(1, 1, 1));
		vindicator.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.NETHERITE_AXE));
		float before = vindicator.getHealth();
		com.olympus.handler.AegisHandler.onMeleeBlocked(defender, vindicator, new ItemStack(ModItems.AEGIS));
		helper.assertTrue(vindicator.getSecondsToDisableBlocking() <= 0 || vindicator.getHealth() <= before - 1.9f,
			"axe attacker was not hurt for disabling the Aegis");
		helper.succeed();
	}

	@GameTest
	public void sickleTillsAndStaysUnbreakable(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		ItemStack sickle = new ItemStack(ModItems.SICKLE_OF_DEMETER);
		helper.assertFalse(sickle.isDamageableItem(), "Sickle has durability");
		helper.assertTrue(ModItems.SICKLE_OF_DEMETER instanceof net.minecraft.world.item.HoeItem, "Sickle is not a hoe");
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.DIRT);
		helper.setBlock(new BlockPos(1, 2, 1), Blocks.AIR);
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sickle);
		BlockPos dirt = helper.absolutePos(new BlockPos(1, 1, 1));
		var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(dirt).add(0, 0.5, 0), net.minecraft.core.Direction.UP, dirt, false);
		sickle.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit));
		helper.assertBlockPresent(Blocks.FARMLAND, new BlockPos(1, 1, 1));
		helper.succeed();
	}

	@GameTest
	public void creativeLegendaryCopiesDontClaimTheRealOne(GameTestHelper helper) {
		var server = helper.getLevel().getServer();
		com.olympus.data.LegendaryRegistry.remove(server, "golden_fleece");
		ServerPlayer admin = helper.makeMockServerPlayerInLevel();
		admin.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
		admin.getInventory().add(new ItemStack(ModItems.GOLDEN_FLEECE));
		com.olympus.event.EventManager.guardLegendaries(admin);
		helper.assertTrue(com.olympus.data.LegendaryRegistry.get(server, "golden_fleece").isEmpty(), "a creative copy claimed the legendary");
		helper.succeed();
	}

	@GameTest
	public void summonsSpareTheOwnersAllies(GameTestHelper helper) {
		ServerPlayer owner = helper.makeMockServerPlayerInLevel();
		ServerPlayer friend = helper.makeMockServerPlayerInLevel();
		PlayerOlympusData.set(owner, PlayerOlympusData.get(owner).withAllies(List.of(friend.getUUID())));
		Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		com.olympus.handler.MirrorHandler.charmAlly(zombie, owner, 200);
		zombie.setTarget(friend);
		helper.assertTrue(zombie.getTarget() != friend, "charmed mob attacked the owner's ally");
		zombie.setTarget(owner);
		helper.assertTrue(zombie.getTarget() != owner, "charmed mob attacked its owner");
		helper.succeed();
	}

	private static double sum(ItemStack stack, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
		double total = 0;
		for (var entry : stack.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS).modifiers()) {
			if (entry.attribute().equals(attribute)) {
				total += entry.modifier().amount();
			}
		}
		return total;
	}

	@GameTest
	public void harpeOutDamagesNetherite(GameTestHelper helper) {
		var dmg = net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE;
		double netherite = sum(new ItemStack(Items.NETHERITE_SWORD), dmg);
		double harpe = sum(new ItemStack(ModItems.HARPE), dmg);
		helper.assertTrue(harpe >= netherite + 1.9, "Harpe (" + harpe + ") should beat a netherite sword (" + netherite + ")");
		helper.succeed();
	}

	@GameTest
	public void ambrosiaFullyHeals(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
		player.setHealth(2f);
		player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 200));
		ItemStack ambrosia = new ItemStack(ModItems.AMBROSIA, 2);
		ModItems.AMBROSIA.finishUsingItem(ambrosia, helper.getLevel(), player);
		helper.assertTrue(player.getHealth() >= player.getMaxHealth(), "not fully healed");
		helper.assertFalse(player.hasEffect(net.minecraft.world.effect.MobEffects.POISON), "poison not cleared");
		helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.RESISTANCE), "no Resistance");
		helper.succeed();
	}

	@GameTest
	public void hoplitePiecesHaveBonuses(GameTestHelper helper) {
		helper.assertTrue(sum(new ItemStack(ModItems.HOPLITE_CHESTPLATE), net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH) == 4, "breastplate: no extra hearts");
		helper.assertTrue(sum(new ItemStack(ModItems.HOPLITE_LEGGINGS), net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) > 0, "greaves: no speed");
		helper.assertTrue(sum(new ItemStack(ModItems.HOPLITE_BOOTS), net.minecraft.world.entity.ai.attributes.Attributes.FALL_DAMAGE_MULTIPLIER) < 0, "sandals: no fall protection");
		helper.assertTrue(sum(new ItemStack(ModItems.HOPLITE_CHESTPLATE), net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) == 8, "breastplate lost netherite armor");
		helper.succeed();
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
