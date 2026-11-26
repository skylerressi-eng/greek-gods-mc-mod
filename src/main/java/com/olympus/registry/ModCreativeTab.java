package com.olympus.registry;

import com.olympus.Olympus;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
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
		CreativeModeTab tab = FabricCreativeModeTab.builder()
			.title(Component.translatable("itemGroup.olympus.relics_of_olympus"))
			.icon(() -> new ItemStack(ModItems.AEGIS))
			.displayItems((params, output) -> {
				for (Item item : ModItems.ALL) {
					output.accept(item);
				}
			})
			.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KEY, tab);
	}
}
