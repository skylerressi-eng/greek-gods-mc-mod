package com.olympus.god;

import com.olympus.registry.ModComponents;
import net.minecraft.world.item.ItemStack;

/**
 * Implemented by every god item (starter relics, craftable power items, legendaries).
 * God items count toward the carry limit, are unbreakable and can be upgraded.
 */
public interface GodItem {
	God god();

	/** Starter relics come from claiming / the God Swapper only. */
	default boolean isStarterRelic() {
		return false;
	}

	/** Charm-type items cannot be enchanted. */
	default boolean isCharm() {
		return false;
	}

	/** Base primary cooldown in seconds (0 = passive / no cooldown). Read live from config. */
	default int primaryCooldownSeconds() {
		return 0;
	}

	/** Base Blessed ability cooldown in seconds (0 = passive). Read live from config. */
	default int blessedCooldownSeconds() {
		return 0;
	}

	/** True if the Blessed ability is passive (no keybind). */
	default boolean blessedIsPassive() {
		return blessedCooldownSeconds() <= 0;
	}

	static UpgradeLevel level(ItemStack stack) {
		return UpgradeLevel.of(stack.getOrDefault(ModComponents.UPGRADE_LEVEL, 0));
	}

	static boolean is(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof GodItem;
	}
}
