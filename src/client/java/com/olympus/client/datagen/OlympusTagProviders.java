package com.olympus.client.datagen;

import com.olympus.god.GodItem;
import com.olympus.registry.ModBlocks;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModKeys;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
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

	public static class Blocks extends FabricTagProvider.BlockTagProvider {
		public Blocks(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
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

	public static class Items extends FabricTagProvider.ItemTagProvider {
		public Items(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries, Blocks blocks) {
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

			// Enchanting: god items join the specific enchantable/* tags, NOT the tool tags
			// (#minecraft:swords etc.), so Unbreaking and Mending (#enchantable/durability) never
			// apply, and Curse of Vanishing (#enchantable/vanishing) can't delete a relic on death.
			ench("melee_weapon", ModItems.BLADE_OF_ARES);
			ench("sweeping", ModItems.BLADE_OF_ARES);
			ench("mining", ModItems.HAMMER_OF_HEPHAESTUS, ModItems.SICKLE_OF_DEMETER);
			ench("mining_loot", ModItems.HAMMER_OF_HEPHAESTUS, ModItems.SICKLE_OF_DEMETER);
			ench("head_armor", ModItems.HELM_OF_DARKNESS);
			ench("foot_armor", ModItems.TALARIA);
			ench("equippable", ModItems.HELM_OF_DARKNESS, ModItems.TALARIA);
			ench("bow", ModItems.SILVER_BOW);
		}

		private void ench(String tag, Item... items) {
			var builder = builder(net.minecraft.tags.TagKey.create(Registries.ITEM, net.minecraft.resources.Identifier.withDefaultNamespace("enchantable/" + tag)));
			for (Item item : items) {
				builder.add(key(item));
			}
		}
	}

	public static class BannerPatterns extends FabricTagProvider<BannerPattern> {
		public BannerPatterns(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, Registries.BANNER_PATTERN, registries);
		}

		@Override
		protected void addTags(HolderLookup.Provider registries) {
			builder(ModKeys.PATTERN_ITEM_GORGONEION).add(ModKeys.GORGONEION);
			builder(ModKeys.PATTERN_ITEM_THUNDERBOLT).add(ModKeys.THUNDERBOLT);
		}
	}

	public static class EntityTypes extends FabricTagProvider<EntityType<?>> {
		public EntityTypes(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, Registries.ENTITY_TYPE, registries);
		}

		@Override
		protected void addTags(HolderLookup.Provider registries) {
			// Olympus bosses are added in Phase 4. Vanilla bosses are also checked in code.
			builder(ModKeys.BOSSES)
				.add(BuiltInRegistries.ENTITY_TYPE.getResourceKey(net.minecraft.world.entity.EntityType.ENDER_DRAGON).orElseThrow())
				.add(BuiltInRegistries.ENTITY_TYPE.getResourceKey(net.minecraft.world.entity.EntityType.WITHER).orElseThrow())
				.add(BuiltInRegistries.ENTITY_TYPE.getResourceKey(net.minecraft.world.entity.EntityType.WARDEN).orElseThrow())
				.add(BuiltInRegistries.ENTITY_TYPE.getResourceKey(net.minecraft.world.entity.EntityType.ELDER_GUARDIAN).orElseThrow());
		}
	}
}
