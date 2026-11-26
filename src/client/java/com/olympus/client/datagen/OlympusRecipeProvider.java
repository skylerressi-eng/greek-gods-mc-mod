package com.olympus.client.datagen;

import com.olympus.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

/** Phase 1 recipes (materials). Layouts are documented in RECIPES.md. */
public class OlympusRecipeProvider extends FabricRecipeProvider {
	public OlympusRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
		return new RecipeProvider(recipes, advancements) {
			@Override
			public void buildRecipes() {
				shaped(RecipeCategory.MISC, ModItems.OLYMPIAN_BRONZE_INGOT)
					.pattern("GCG").pattern("CSC").pattern("GCG")
					.define('G', Items.GOLD_BLOCK).define('C', Ingredient.of(Items.COPPER_BLOCK.weathering().unaffected(), Items.COPPER_BLOCK.waxed().unaffected())).define('S', Items.NETHERITE_SCRAP)
					.unlockedBy(getHasName(Items.NETHERITE_SCRAP), has(Items.NETHERITE_SCRAP))
					.save(output, "olympian_bronze_ingot_from_alloy");

				nineBlockStorageRecipes(RecipeCategory.MISC, ModItems.OLYMPIAN_BRONZE_INGOT, RecipeCategory.BUILDING_BLOCKS, ModItems.OLYMPIAN_BRONZE_BLOCK);

				copySmithingTemplate(ModItems.DIVINE_SMITHING_TEMPLATE, ModItems.OLYMPIAN_BRONZE_INGOT);

				shaped(RecipeCategory.MISC, ModItems.BLESSING_CORE)
					.pattern("IDI").pattern("DED").pattern("IDI")
					.define('I', ModItems.OLYMPIAN_BRONZE_INGOT).define('D', Items.DIAMOND_BLOCK).define('E', Items.ENCHANTED_GOLDEN_APPLE)
					.unlockedBy(getHasName(ModItems.OLYMPIAN_BRONZE_INGOT), has(ModItems.OLYMPIAN_BRONZE_INGOT))
					.save(output);

				shaped(RecipeCategory.MISC, ModItems.ASCENSION_CORE)
					.pattern("NEN").pattern("BSB").pattern("NEN")
					.define('N', Items.NETHERITE_INGOT).define('E', Items.ENCHANTED_GOLDEN_APPLE)
					.define('B', ModItems.OLYMPIAN_BRONZE_BLOCK).define('S', Items.NETHER_STAR)
					.unlockedBy(getHasName(Items.NETHER_STAR), has(Items.NETHER_STAR))
					.save(output);

				shaped(RecipeCategory.MISC, ModItems.GOD_SWAPPER)
					.pattern("IEI").pattern("ETE").pattern("IEI")
					.define('I', ModItems.OLYMPIAN_BRONZE_INGOT).define('E', Items.ECHO_SHARD).define('T', Items.TOTEM_OF_UNDYING)
					.unlockedBy(getHasName(Items.TOTEM_OF_UNDYING), has(Items.TOTEM_OF_UNDYING))
					.save(output);

				shapeless(RecipeCategory.MISC, ModItems.GORGONEION_BANNER_PATTERN)
					.requires(Items.PAPER).requires(Items.GOLD_INGOT).requires(Items.SPIDER_EYE)
					.unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
					.save(output);
				shapeless(RecipeCategory.MISC, ModItems.THUNDERBOLT_BANNER_PATTERN)
					.requires(Items.PAPER).requires(Items.GOLD_INGOT).requires(Items.LIGHTNING_ROD.weathering().unaffected())
					.unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
					.save(output);
			}
		};
	}

	@Override
	public String getName() {
		return "Olympus recipes";
	}
}
