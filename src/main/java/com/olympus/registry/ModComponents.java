package com.olympus.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.olympus.Olympus;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class ModComponents {
	/** 0 = Mortal, 1 = Blessed, 2 = Ascended. */
	public static final DataComponentType<Integer> UPGRADE_LEVEL = register("upgrade_level",
		DataComponentType.<Integer>builder().persistent(Codec.intRange(0, 2)).networkSynchronized(ByteBufCodecs.VAR_INT).build());

	/** Absolute game-time ticks when each ability of this specific stack is ready again. */
	public static final DataComponentType<StackCooldowns> COOLDOWNS = register("cooldowns",
		DataComponentType.<StackCooldowns>builder().persistent(StackCooldowns.CODEC).networkSynchronized(StackCooldowns.STREAM_CODEC).build());

	/** Legendary identity: one instance per server, and who first won it. */
	public static final DataComponentType<Legendary> LEGENDARY = register("legendary",
		DataComponentType.<Legendary>builder().persistent(Legendary.CODEC).networkSynchronized(Legendary.STREAM_CODEC).build());

	/** Forge Hammer mining mode: 1 = 3x3, 2 = 5x5. */
	public static final DataComponentType<Integer> MINING_RADIUS = register("mining_radius",
		DataComponentType.<Integer>builder().persistent(Codec.intRange(1, 2)).networkSynchronized(ByteBufCodecs.VAR_INT).build());

	public record Legendary(java.util.UUID instance, String firstOwner) {
		public static final Codec<Legendary> CODEC = RecordCodecBuilder.create(i -> i.group(
			net.minecraft.core.UUIDUtil.CODEC.fieldOf("instance").forGetter(Legendary::instance),
			Codec.STRING.fieldOf("first_owner").forGetter(Legendary::firstOwner)
		).apply(i, Legendary::new));
		public static final StreamCodec<io.netty.buffer.ByteBuf, Legendary> STREAM_CODEC = StreamCodec.composite(
			net.minecraft.core.UUIDUtil.STREAM_CODEC, Legendary::instance,
			ByteBufCodecs.STRING_UTF8, Legendary::firstOwner,
			Legendary::new);
	}

	private ModComponents() {
	}

	private static <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Olympus.id(name), type);
	}

	public static void init() {
		net.minecraft.core.Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Olympus.id("god_upgrade"), com.olympus.recipe.GodUpgradeRecipe.Serializer.INSTANCE);
	}

	public record StackCooldowns(long primaryReadyAt, long blessedReadyAt) {
		public static final StackCooldowns NONE = new StackCooldowns(0L, 0L);
		public static final Codec<StackCooldowns> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.LONG.optionalFieldOf("primary", 0L).forGetter(StackCooldowns::primaryReadyAt),
			Codec.LONG.optionalFieldOf("blessed", 0L).forGetter(StackCooldowns::blessedReadyAt)
		).apply(i, StackCooldowns::new));
		public static final StreamCodec<io.netty.buffer.ByteBuf, StackCooldowns> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, StackCooldowns::primaryReadyAt,
			ByteBufCodecs.VAR_LONG, StackCooldowns::blessedReadyAt,
			StackCooldowns::new);

		public StackCooldowns withPrimary(long t) {
			return new StackCooldowns(t, blessedReadyAt);
		}

		public StackCooldowns withBlessed(long t) {
			return new StackCooldowns(primaryReadyAt, t);
		}
	}
}
