package com.olympus.client.datagen;

import com.olympus.registry.ModBlocks;
import com.olympus.registry.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class OlympusModelProvider extends FabricModelProvider {
	public OlympusModelProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators generators) {
		generators.createTrivialCube(ModBlocks.OLYMPIAN_BRONZE_BLOCK);
	}

	@Override
	public void generateItemModels(ItemModelGenerators generators) {
		for (Item item : new Item[]{
			ModItems.OLYMPIAN_BRONZE_INGOT, ModItems.DIVINE_SMITHING_TEMPLATE, ModItems.BLESSING_CORE, ModItems.ASCENSION_CORE,
			ModItems.GOD_SWAPPER, ModItems.GORGONEION_BANNER_PATTERN, ModItems.THUNDERBOLT_BANNER_PATTERN,
			ModItems.CONCH_OF_THE_DEEP, ModItems.HELM_OF_DARKNESS, ModItems.OWL_OF_ATHENA, ModItems.LYRE_OF_APOLLO,
			ModItems.TALARIA, ModItems.GOBLET_OF_DIONYSUS, ModItems.MIRROR_OF_APHRODITE}) {
			generators.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
		}
		for (Item item : new Item[]{ModItems.BLADE_OF_ARES, ModItems.HAMMER_OF_HEPHAESTUS, ModItems.SICKLE_OF_DEMETER}) {
			generators.generateFlatItem(item, ModelTemplates.FLAT_HANDHELD_ITEM);
		}
		generators.generateBow(ModItems.SILVER_BOW);

		// The Aegis reuses the vanilla shield model and renderer; its emblem comes from its banner patterns.
		generators.generateBooleanDispatch(ModItems.AEGIS,
			ItemModelUtils.isUsingItem(),
			ItemModelUtils.specialModel(Identifier.withDefaultNamespace("item/shield_blocking"), new ShieldSpecialRenderer.Unbaked()),
			ItemModelUtils.specialModel(Identifier.withDefaultNamespace("item/shield"), new ShieldSpecialRenderer.Unbaked()));
	}
}
