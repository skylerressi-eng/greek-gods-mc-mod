package com.olympus.item;

import com.olympus.Olympus;
import com.olympus.config.ConfigManager;
import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;

/** Shared {@link Item.Properties} recipes for god items. */
public final class GodProperties {
	private GodProperties() {
	}

	public enum Tier {
		NETHERITE(ToolMaterial.NETHERITE, ArmorMaterials.NETHERITE),
		DIAMOND(ToolMaterial.DIAMOND, ArmorMaterials.DIAMOND),
		IRON(ToolMaterial.IRON, ArmorMaterials.IRON);

		public final ToolMaterial tool;
		public final ArmorMaterial armor;

		Tier(ToolMaterial tool, ArmorMaterial armor) {
			this.tool = tool;
			this.armor = armor;
		}
	}

	/** Tier for starter relics, from config (read once at startup). */
	public static Tier starterTier() {
		String value = ConfigManager.get().general.starterTier;
		try {
			return Tier.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			Olympus.LOGGER.warn("Unknown starterTier '{}', using netherite", value);
			return Tier.NETHERITE;
		}
	}

	/** Base properties every god item shares: single stack, fire-proof, rare. */
	public static Item.Properties base() {
		return new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.RARE).component(com.olympus.registry.ModComponents.UPGRADE_LEVEL, 0);
	}

	/**
	 * Removes durability entirely: no damage bar, can never break, can't be repaired.
	 * Must be applied after tool/armor properties, which add durability.
	 */
	public static Item.Properties unbreakable(Item.Properties properties) {
		return properties
			.component(DataComponents.MAX_DAMAGE, null)
			.component(DataComponents.DAMAGE, null)
			.component(DataComponents.REPAIRABLE, null);
	}
}
