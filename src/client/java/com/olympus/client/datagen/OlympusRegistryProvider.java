package com.olympus.client.datagen;

import com.olympus.registry.ModKeys;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;

/** Writes the banner patterns and damage type declared in {@link OlympusDataGenerator#buildRegistry}. */
public class OlympusRegistryProvider extends FabricDynamicRegistryProvider {
	public OlympusRegistryProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		var patterns = registries.lookupOrThrow(Registries.BANNER_PATTERN);
		entries.add(patterns.getOrThrow(ModKeys.GORGONEION));
		entries.add(patterns.getOrThrow(ModKeys.THUNDERBOLT));
		entries.add(registries.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ModKeys.DIVINE_LIGHTNING));
	}

	@Override
	public String getName() {
		return "Olympus dynamic registries";
	}
}
