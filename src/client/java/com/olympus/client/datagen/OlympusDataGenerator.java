package com.olympus.client.datagen;

import com.olympus.registry.ModKeys;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.block.entity.BannerPattern;

/** Run with {@code ./gradlew runDatagen}; output goes to {@code src/main/generated}. */
public class OlympusDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();
		pack.addProvider(OlympusRegistryProvider::new);
		pack.addProvider(OlympusModelProvider::new);
		pack.addProvider(OlympusRecipeProvider::new);
		pack.addProvider(OlympusLootProvider::new);
		pack.addProvider(OlympusLangProvider::new);
		OlympusTagProviders.Blocks blocks = pack.addProvider(OlympusTagProviders.Blocks::new);
		pack.addProvider((output, registries) -> new OlympusTagProviders.Items(output, registries, blocks));
		pack.addProvider(OlympusTagProviders.BannerPatterns::new);
		pack.addProvider(OlympusTagProviders.EntityTypes::new);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder builder) {
		builder.add(Registries.BANNER_PATTERN, context -> {
			context.register(ModKeys.GORGONEION, new BannerPattern(ModKeys.GORGONEION.identifier(), "block.olympus.banner.gorgoneion"));
			context.register(ModKeys.THUNDERBOLT, new BannerPattern(ModKeys.THUNDERBOLT.identifier(), "block.olympus.banner.thunderbolt"));
		});
		builder.add(Registries.DAMAGE_TYPE, context ->
			context.register(ModKeys.DIVINE_LIGHTNING, new DamageType("olympus.divine_lightning", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1f)));
	}
}
