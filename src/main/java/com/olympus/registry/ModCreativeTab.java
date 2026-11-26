package com.olympus.registry;

import com.olympus.Olympus;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTab {
	public static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Olympus.id("relics_of_olympus"));

	private ModCreativeTab() {
	}

	public static void register() {
		CreativeModeTab tab = FabricItemGroup.builder()
			.title(Component.translatable("itemGroup.olympus.relics_of_olympus"))
			.icon(() -> new ItemStack(ModItems.AEGIS))
			.displayItems((params, output) -> {
				for (Item item : ModItems.ALL) {
					ItemStack stack = new ItemStack(item);
					com.olympus.item.relic.AegisItem.applyEmblem(stack, params.holders());
					output.accept(stack);
				}
			})
			.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KEY, tab);
	}
}
