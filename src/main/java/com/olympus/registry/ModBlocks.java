package com.olympus.registry;

import com.olympus.Olympus;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
	public static final Block OLYMPIAN_BRONZE_BLOCK = register("olympian_bronze_block",
		BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).requiresCorrectToolForDrops().strength(25.0F, 1200.0F).sound(SoundType.NETHERITE_BLOCK));

	public static final Block SUN_GLOW = Registry.register(BuiltInRegistries.BLOCK, ResourceKey.create(Registries.BLOCK, com.olympus.Olympus.id("sun_glow")),
		new com.olympus.block.SunGlowBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, com.olympus.Olympus.id("sun_glow")))
			.replaceable().noCollision().noLootTable().air().lightLevel(s -> 15).instabreak().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

	private ModBlocks() {
	}

	private static Block register(String name, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Olympus.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new Block(properties.setId(key)));
	}

	public static void init() {
	}
}
