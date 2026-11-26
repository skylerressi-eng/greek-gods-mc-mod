package com.olympus.client.datagen;

import com.olympus.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

/** Phase 1 recipes (materials). Layouts are documented in RECIPES.md. */
public class OlympusRecipeProvider extends FabricRecipeProvider {
	public OlympusRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
		return new RecipeProvider(registries, output) {
			@Override
			public void buildRecipes() {
				// Base material: everything else is built from it, so it is only moderately harder.
				shaped(RecipeCategory.MISC, ModItems.OLYMPIAN_BRONZE_INGOT)
					.pattern("GDG").pattern("CSC").pattern("GDG")
					.define('G', Items.GOLD_BLOCK).define('D', Items.DIAMOND)
					.define('C', Ingredient.of(Items.COPPER_BLOCK, Items.WAXED_COPPER_BLOCK))
					.define('S', Items.NETHERITE_SCRAP)
					.unlockedBy(getHasName(Items.NETHERITE_SCRAP), has(Items.NETHERITE_SCRAP))
					.save(output, "olympian_bronze_ingot_from_alloy");

				nineBlockStorageRecipes(RecipeCategory.MISC, ModItems.OLYMPIAN_BRONZE_INGOT, RecipeCategory.BUILDING_BLOCKS, ModItems.OLYMPIAN_BRONZE_BLOCK);

				shaped(RecipeCategory.MISC, ModItems.DIVINE_SMITHING_TEMPLATE, 2)
					.pattern("KdK").pattern("dTd").pattern("KIK")
					.define('K', Items.DIAMOND_BLOCK).define('d', Items.DIAMOND)
					.define('T', ModItems.DIVINE_SMITHING_TEMPLATE).define('I', ModItems.OLYMPIAN_BRONZE_INGOT)
					.unlockedBy(getHasName(ModItems.DIVINE_SMITHING_TEMPLATE), has(ModItems.DIVINE_SMITHING_TEMPLATE))
					.save(output);

				shaped(RecipeCategory.MISC, ModItems.BLESSING_CORE)
					.pattern("IDI").pattern("DED").pattern("NDN")
					.define('I', ModItems.OLYMPIAN_BRONZE_INGOT).define('D', Items.DIAMOND_BLOCK)
					.define('E', Items.ENCHANTED_GOLDEN_APPLE).define('N', Items.NETHERITE_INGOT)
					.unlockedBy(getHasName(ModItems.OLYMPIAN_BRONZE_INGOT), has(ModItems.OLYMPIAN_BRONZE_INGOT))
					.save(output);

				shaped(RecipeCategory.MISC, ModItems.ASCENSION_CORE)
					.pattern("KEK").pattern("BSB").pattern("NEN")
					.define('K', Items.NETHERITE_BLOCK).define('N', Items.NETHERITE_INGOT).define('E', Items.ENCHANTED_GOLDEN_APPLE)
					.define('B', ModItems.OLYMPIAN_BRONZE_BLOCK).define('S', Items.NETHER_STAR)
					.unlockedBy(getHasName(Items.NETHER_STAR), has(Items.NETHER_STAR))
					.save(output);

				shaped(RecipeCategory.MISC, ModItems.GOD_SWAPPER)
					.pattern("IEI").pattern("ETE").pattern("DGD")
					.define('I', ModItems.OLYMPIAN_BRONZE_INGOT).define('E', Items.ECHO_SHARD).define('T', Items.TOTEM_OF_UNDYING)
					.define('D', Items.DIAMOND_BLOCK).define('G', Items.ENCHANTED_GOLDEN_APPLE)
					.unlockedBy(getHasName(Items.TOTEM_OF_UNDYING), has(Items.TOTEM_OF_UNDYING))
					.save(output);

				// Upgrades (smithing table): core + god item + Block of Olympian Bronze
				upgrade("blessed_upgrade", ModItems.BLESSING_CORE, 0, 1);
				upgrade("ascended_upgrade", ModItems.ASCENSION_CORE, 1, 2);

				// Phase 3: craftable god items. Each consumes its god's starter relic.
				shaped(RecipeCategory.COMBAT, ModItems.CADUCEUS)
					.pattern("DHD").pattern("ETE").pattern("RBR")
					.define('D', Items.DIAMOND_BLOCK).define('H', Items.HEAVY_CORE).define('E', Items.ENCHANTED_GOLDEN_APPLE)
					.define('T', ModItems.TALARIA).define('R', Items.BREEZE_ROD).define('B', ModItems.OLYMPIAN_BRONZE_BLOCK)
					.unlockedBy(getHasName(ModItems.TALARIA), has(ModItems.TALARIA)).save(output);
				shaped(RecipeCategory.COMBAT, ModItems.THYRSUS)
					.pattern("GOG").pattern("VSV").pattern("DBD")
					.define('G', Items.GLOW_BERRIES).define('O', ModItems.GOBLET_OF_DIONYSUS).define('V', Items.VINE)
					.define('S', Items.NETHERITE_SWORD).define('D', Items.DIAMOND_BLOCK).define('B', ModItems.OLYMPIAN_BRONZE_BLOCK)
					.unlockedBy(getHasName(ModItems.GOBLET_OF_DIONYSUS), has(ModItems.GOBLET_OF_DIONYSUS)).save(output);
				shaped(RecipeCategory.FOOD, ModItems.CORNUCOPIA)
					.pattern("ASA").pattern("CHC").pattern("DBD")
					.define('A', Items.GOLDEN_APPLE).define('S', ModItems.SICKLE_OF_DEMETER).define('C', Items.GOLDEN_CARROT)
					.define('H', Items.GOAT_HORN).define('D', Items.DIAMOND_BLOCK).define('B', ModItems.OLYMPIAN_BRONZE_BLOCK)
					.unlockedBy(getHasName(ModItems.SICKLE_OF_DEMETER), has(ModItems.SICKLE_OF_DEMETER)).save(output);
				divineWeapon(ModItems.SPEAR_OF_ATHENA, ModItems.OWL_OF_ATHENA, Items.TRIDENT);
				divineWeapon(ModItems.FORGE_HAMMER, ModItems.HAMMER_OF_HEPHAESTUS, Items.NETHERITE_PICKAXE);
				divineWeapon(ModItems.HARPE, ModItems.HELM_OF_DARKNESS, Items.NETHERITE_SWORD);

				// Hoplite armor (smithing; not god items)
				hoplite(Items.NETHERITE_HELMET, ModItems.HOPLITE_HELMET);
				hoplite(Items.NETHERITE_CHESTPLATE, ModItems.HOPLITE_CHESTPLATE);
				hoplite(Items.NETHERITE_LEGGINGS, ModItems.HOPLITE_LEGGINGS);
				hoplite(Items.NETHERITE_BOOTS, ModItems.HOPLITE_BOOTS);

				shaped(RecipeCategory.FOOD, ModItems.AMBROSIA)
					.pattern("KDK").pattern("HAH").pattern("KMK")
					.define('K', Items.GOLD_BLOCK).define('D', Items.DIAMOND_BLOCK).define('H', Items.HONEY_BOTTLE)
					.define('A', Items.GOLDEN_APPLE).define('M', Items.GLISTERING_MELON_SLICE)
					.unlockedBy(getHasName(Items.GOLDEN_APPLE), has(Items.GOLDEN_APPLE)).save(output);
				shaped(RecipeCategory.FOOD, ModItems.NECTAR)
					.pattern("TKT").pattern("CHC").pattern("KDK")
					.define('T', Items.GHAST_TEAR).define('K', Items.GOLD_BLOCK).define('C', Items.GOLDEN_CARROT)
					.define('H', Items.HONEY_BOTTLE).define('D', Items.DIAMOND_BLOCK)
					.unlockedBy(getHasName(Items.GHAST_TEAR), has(Items.GHAST_TEAR)).save(output);
				shaped(RecipeCategory.COMBAT, ModItems.SUN_ARROW, 8)
					.pattern("AAA").pattern("AIA").pattern("AAA")
					.define('A', Items.SPECTRAL_ARROW).define('I', ModItems.OLYMPIAN_BRONZE_INGOT)
					.unlockedBy(getHasName(ModItems.OLYMPIAN_BRONZE_INGOT), has(ModItems.OLYMPIAN_BRONZE_INGOT)).save(output);
				shapeless(RecipeCategory.MISC, ModItems.CODEX_OF_OLYMPUS)
					.requires(Items.BOOK).requires(Items.GOLD_INGOT)
					.unlockedBy(getHasName(Items.BOOK), has(Items.BOOK)).save(output);

				shapeless(RecipeCategory.MISC, ModItems.GORGONEION_BANNER_PATTERN)
					.requires(Items.PAPER).requires(Items.GOLD_BLOCK).requires(Items.SPIDER_EYE)
					.unlockedBy(getHasName(Items.GOLD_BLOCK), has(Items.GOLD_BLOCK))
					.save(output);
				shapeless(RecipeCategory.MISC, ModItems.THUNDERBOLT_BANNER_PATTERN)
					.requires(Items.PAPER).requires(Items.GOLD_BLOCK).requires(Items.LIGHTNING_ROD)
					.unlockedBy(getHasName(Items.GOLD_BLOCK), has(Items.GOLD_BLOCK))
					.save(output);
			}

			private void divineWeapon(net.minecraft.world.item.Item result, net.minecraft.world.item.Item relic, net.minecraft.world.item.Item base) {
				shaped(RecipeCategory.COMBAT, result)
					.pattern("DRD").pattern("BWB").pattern("DTD")
					.define('D', Items.DIAMOND_BLOCK).define('R', relic).define('B', ModItems.OLYMPIAN_BRONZE_BLOCK)
					.define('W', base).define('T', ModItems.DIVINE_SMITHING_TEMPLATE)
					.unlockedBy(getHasName(relic), has(relic)).save(output);
			}

			private void hoplite(net.minecraft.world.item.Item base, net.minecraft.world.item.Item result) {
				net.minecraft.data.recipes.SmithingTransformRecipeBuilder.smithing(
						net.minecraft.world.item.crafting.Ingredient.of(ModItems.DIVINE_SMITHING_TEMPLATE),
						net.minecraft.world.item.crafting.Ingredient.of(base),
						net.minecraft.world.item.crafting.Ingredient.of(ModItems.OLYMPIAN_BRONZE_BLOCK),
						RecipeCategory.COMBAT, result)
					.unlocks("has_template", has(ModItems.DIVINE_SMITHING_TEMPLATE))
					.save(output, net.minecraft.data.recipes.RecipeProvider.getItemName(result) + "_smithing");
			}

			private void upgrade(String name, net.minecraft.world.item.Item core, int from, int to) {
				var recipe = new com.olympus.recipe.GodUpgradeRecipe(
					net.minecraft.world.item.crafting.Ingredient.of(core),
					tag(com.olympus.registry.ModKeys.GOD_ITEMS),
					net.minecraft.world.item.crafting.Ingredient.of(ModItems.OLYMPIAN_BRONZE_BLOCK),
					from, to);
				output.accept(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, com.olympus.Olympus.id(name)), recipe, null);
			}
		};
	}

	@Override
	public String getName() {
		return "Olympus recipes";
	}
}
