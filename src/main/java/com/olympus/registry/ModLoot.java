package com.olympus.registry;

import com.olympus.config.ConfigManager;
import java.util.Set;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

/** Divine Smithing Templates in the treasure chests of the most dangerous structures. */
public final class ModLoot {
	private static final Set<ResourceKey<LootTable>> TEMPLATE_CHESTS = Set.of(
		BuiltInLootTables.ANCIENT_CITY,
		BuiltInLootTables.END_CITY_TREASURE,
		BuiltInLootTables.BASTION_TREASURE);

	private ModLoot() {
	}

	public static void register() {
		LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
			// Only touch the built-in tables, so a datapack that replaces them stays in control.
			if (!source.isBuiltin() || !TEMPLATE_CHESTS.contains(key)) {
				return;
			}
			float chance = (float) ConfigManager.get().loot.divineTemplateChance;
			if (chance <= 0) {
				return;
			}
			builder.withPool(LootPool.lootPool()
				.when(LootItemRandomChanceCondition.randomChance(chance))
				.add(LootItem.lootTableItem(ModItems.DIVINE_SMITHING_TEMPLATE)));
		});
	}
}
