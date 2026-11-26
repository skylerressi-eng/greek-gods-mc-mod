package com.olympus.client.datagen;

import com.olympus.registry.ModBlocks;
import com.olympus.registry.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * Every Olympus item looks like the vanilla item it is built on (netherite sword, leather boots,
 * potion, ...). Only the names, glint and powers set them apart. To give an item its own art
 * later, point its model at an olympus texture here.
 */
public class OlympusModelProvider extends FabricModelProvider {
	/** Talaria's default leather color: sun-bleached gold. */
	public static final int TALARIA_COLOR = 0xD8B45A;
	/** Wine in the Goblet of Dionysus. */
	public static final int WINE_COLOR = 0x7A1450;

	public OlympusModelProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators generators) {
		generators.createTrivialCube(ModBlocks.OLYMPIAN_BRONZE_BLOCK);
	}

	private static Identifier vanilla(String path) {
		return Identifier.withDefaultNamespace(path);
	}

	private static void looksLike(ItemModelGenerators g, Item item, String vanillaModel) {
		g.itemModelOutput.accept(item, ItemModelUtils.plainModel(vanilla(vanillaModel)));
	}

	@Override
	public void generateItemModels(ItemModelGenerators g) {
		// Materials
		looksLike(g, ModItems.OLYMPIAN_BRONZE_INGOT, "item/copper_ingot");
		looksLike(g, ModItems.DIVINE_SMITHING_TEMPLATE, "item/netherite_upgrade_smithing_template");
		looksLike(g, ModItems.BLESSING_CORE, "item/heart_of_the_sea");
		looksLike(g, ModItems.ASCENSION_CORE, "item/nether_star");
		looksLike(g, ModItems.GOD_SWAPPER, "item/ender_eye");
		looksLike(g, ModItems.GORGONEION_BANNER_PATTERN, "item/skull_banner_pattern");
		looksLike(g, ModItems.THUNDERBOLT_BANNER_PATTERN, "item/flow_banner_pattern");

		// Starter relics, each on its base item
		looksLike(g, ModItems.CONCH_OF_THE_DEEP, "item/nautilus_shell");
		looksLike(g, ModItems.HELM_OF_DARKNESS, "item/netherite_helmet");
		looksLike(g, ModItems.OWL_OF_ATHENA, "item/feather");
		looksLike(g, ModItems.BLADE_OF_ARES, "item/netherite_sword");
		looksLike(g, ModItems.LYRE_OF_APOLLO, "block/note_block");
		looksLike(g, ModItems.HAMMER_OF_HEPHAESTUS, "item/netherite_pickaxe");
		looksLike(g, ModItems.SICKLE_OF_DEMETER, "item/netherite_hoe");
		looksLike(g, ModItems.MIRROR_OF_APHRODITE, "item/spyglass");
		g.itemModelOutput.accept(ModItems.TALARIA, ItemModelUtils.tintedModel(vanilla("item/leather_boots"), new Dye(0xFF000000 | TALARIA_COLOR)));
		g.itemModelOutput.accept(ModItems.GOBLET_OF_DIONYSUS, ItemModelUtils.tintedModel(vanilla("item/potion"), new Constant(0xFF000000 | WINE_COLOR)));

		// Silver Bow: the vanilla bow with its pull animation
		g.itemModelOutput.accept(ModItems.SILVER_BOW, ItemModelUtils.conditional(
			ItemModelUtils.isUsingItem(),
			ItemModelUtils.rangeSelect(new UseDuration(false), 0.05F,
				ItemModelUtils.plainModel(vanilla("item/bow_pulling_0")),
				ItemModelUtils.override(ItemModelUtils.plainModel(vanilla("item/bow_pulling_1")), 0.65F),
				ItemModelUtils.override(ItemModelUtils.plainModel(vanilla("item/bow_pulling_2")), 0.9F)),
			ItemModelUtils.plainModel(vanilla("item/bow"))));

		// Aegis: the vanilla shield; its emblem comes from its banner patterns.
		g.generateBooleanDispatch(ModItems.AEGIS,
			ItemModelUtils.isUsingItem(),
			ItemModelUtils.specialModel(vanilla("item/shield_blocking"), new ShieldSpecialRenderer.Unbaked()),
			ItemModelUtils.specialModel(vanilla("item/shield"), new ShieldSpecialRenderer.Unbaked()));
	}
}
