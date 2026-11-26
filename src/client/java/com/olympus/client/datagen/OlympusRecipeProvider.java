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

				shapeless(RecipeCategory.MISC, ModItems.GORGONEION_BANNER_PATTERN)
					.requires(Items.PAPER).requires(Items.GOLD_BLOCK).requires(Items.SPIDER_EYE)
					.unlockedBy(getHasName(Items.GOLD_BLOCK), has(Items.GOLD_BLOCK))
					.save(output);
				shapeless(RecipeCategory.MISC, ModItems.THUNDERBOLT_BANNER_PATTERN)
					.requires(Items.PAPER).requires(Items.GOLD_BLOCK).requires(Items.LIGHTNING_ROD)
					.unlockedBy(getHasName(Items.GOLD_BLOCK), has(Items.GOLD_BLOCK))
					.save(output);
			}
		};
	}

	@Override
	public String getName() {
		return "Olympus recipes";
	}
}
