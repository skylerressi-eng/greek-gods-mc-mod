package com.olympus.client.datagen;

import com.olympus.god.GodItem;
import com.olympus.registry.ModBlocks;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModKeys;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BannerPattern;

public final class OlympusTagProviders {
	private OlympusTagProviders() {
	}

	private static ResourceKey<Item> key(Item item) {
		return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
	}

	public static class Blocks extends FabricTagsProvider.BlockTagsProvider {
		public Blocks(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		protected void addTags(HolderLookup.Provider registries) {
			var block = BuiltInRegistries.BLOCK.getResourceKey(ModBlocks.OLYMPIAN_BRONZE_BLOCK).orElseThrow();
			builder(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
			builder(BlockTags.NEEDS_DIAMOND_TOOL).add(block);
			builder(BlockTags.BEACON_BASE_BLOCKS).add(block);
		}
	}

	public static class Items extends FabricTagsProvider.ItemTagsProvider {
		public Items(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries, Blocks blocks) {
			super(output, registries, blocks);
		}

		@Override
		protected void addTags(HolderLookup.Provider registries) {
			var godItems = builder(ModKeys.GOD_ITEMS);
			var starters = builder(ModKeys.STARTER_RELICS);
			var charms = builder(ModKeys.CHARMS);
			for (Item item : ModItems.ALL) {
				if (item instanceof GodItem god) {
					godItems.add(key(item));
					if (god.isStarterRelic()) {
						starters.add(key(item));
					}
					if (god.isCharm()) {
						charms.add(key(item));
					}
				}
			}
			builder(net.minecraft.tags.ItemTags.BEACON_PAYMENT_ITEMS).add(key(ModItems.OLYMPIAN_BRONZE_INGOT));
		}
	}

	public static class BannerPatterns extends FabricTagsProvider<BannerPattern> {
		public BannerPatterns(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, Registries.BANNER_PATTERN, registries);
		}

		@Override
		protected void addTags(HolderLookup.Provider registries) {
			builder(ModKeys.PATTERN_ITEM_GORGONEION).add(ModKeys.GORGONEION);
			builder(ModKeys.PATTERN_ITEM_THUNDERBOLT).add(ModKeys.THUNDERBOLT);
		}
	}

	public static class EntityTypes extends FabricTagsProvider<EntityType<?>> {
		public EntityTypes(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, Registries.ENTITY_TYPE, registries);
		}

		@Override
		protected void addTags(HolderLookup.Provider registries) {
			// Olympus bosses are added in Phase 4. Vanilla bosses are also checked in code.
			builder(ModKeys.BOSSES)
				.add(BuiltInRegistries.ENTITY_TYPE.getResourceKey(net.minecraft.world.entity.EntityTypes.ENDER_DRAGON).orElseThrow())
				.add(BuiltInRegistries.ENTITY_TYPE.getResourceKey(net.minecraft.world.entity.EntityTypes.WITHER).orElseThrow())
				.add(BuiltInRegistries.ENTITY_TYPE.getResourceKey(net.minecraft.world.entity.EntityTypes.WARDEN).orElseThrow())
				.add(BuiltInRegistries.ENTITY_TYPE.getResourceKey(net.minecraft.world.entity.EntityTypes.ELDER_GUARDIAN).orElseThrow());
		}
	}
}
