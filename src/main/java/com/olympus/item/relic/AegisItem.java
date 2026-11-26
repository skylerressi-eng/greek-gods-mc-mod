package com.olympus.item.relic;

import com.olympus.god.God;
import com.olympus.god.GodItem;
import com.olympus.registry.ModKeys;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.jspecify.annotations.Nullable;

/**
 * Zeus — Aegis. A shield that never breaks and carries the Gorgoneion emblem.
 * Blocking a melee hit can call lightning on the attacker (see {@link com.olympus.handler.AegisHandler}).
 */
public class AegisItem extends Item implements GodItem {
	public AegisItem(Properties properties) {
		super(properties);
	}

	@Override
	public God god() {
		return God.ZEUS;
	}

	@Override
	public boolean isStarterRelic() {
		return true;
	}

	/**
	 * Paints the Aegis emblem (gold field, Gorgoneion, border). Banner patterns live in a datapack
	 * registry, so they are applied once a world is loaded rather than at item registration.
	 */
	public static void applyEmblem(ItemStack stack, HolderLookup.Provider registries) {
		if (!(stack.getItem() instanceof AegisItem)) {
			return;
		}
		BannerPatternLayers current = stack.get(DataComponents.BANNER_PATTERNS);
		if (current != null && !current.layers().isEmpty()) {
			return;
		}
		var patterns = registries.lookupOrThrow(Registries.BANNER_PATTERN);
		var gorgoneion = patterns.get(ModKeys.GORGONEION);
		if (gorgoneion.isEmpty()) {
			return;
		}
		stack.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(List.of(
			new BannerPatternLayers.Layer(patterns.getOrThrow(BannerPatterns.GRADIENT_UP), DyeColor.ORANGE),
			new BannerPatternLayers.Layer(gorgoneion.get(), DyeColor.BROWN),
			new BannerPatternLayers.Layer(patterns.getOrThrow(BannerPatterns.BORDER), DyeColor.ORANGE))));
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
		applyEmblem(stack, level.registryAccess());
	}
}
