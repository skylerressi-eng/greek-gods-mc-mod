package com.olympus.event;

import com.olympus.config.ConfigManager;
import com.olympus.god.God;
import com.olympus.registry.ModEntities;
import com.olympus.registry.ModItems;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;

/** The six divine events, their bosses, legendaries and where they may happen. */
public enum OlympusEvent {
	WRATH_OF_ZEUS(God.ZEUS, () -> ModEntities.TYPHON, () -> ModItems.THUNDERBOLT_OF_ZEUS, () -> ConfigManager.get().events.typhonHealth),
	RISE_OF_THE_DEEP(God.POSEIDON, () -> ModEntities.CETUS, () -> ModItems.TRIDENT_OF_POSEIDON, () -> ConfigManager.get().events.cetusHealth),
	GATES_OF_THE_UNDERWORLD(God.HADES, () -> ModEntities.CERBERUS, () -> ModItems.BIDENT_OF_HADES, () -> ConfigManager.get().events.cerberusHealth),
	GORGONS_LAIR(God.ATHENA, () -> ModEntities.MEDUSA, () -> ModItems.HEAD_OF_MEDUSA, () -> ConfigManager.get().events.medusaHealth),
	QUEST_FOR_THE_FLEECE(God.ARES, () -> ModEntities.COLCHIAN_DRAGON, () -> ModItems.GOLDEN_FLEECE, () -> ConfigManager.get().events.dragonHealth),
	TITANOMACHY(God.TITAN, () -> ModEntities.KRONOS, () -> ModItems.SCYTHE_OF_KRONOS, () -> ConfigManager.get().events.kronosHealth);

	public final God god;
	private final Supplier<EntityType<? extends Mob>> boss;
	private final Supplier<Item> legendary;
	private final Supplier<Float> health;

	OlympusEvent(God god, Supplier<EntityType<? extends Mob>> boss, Supplier<Item> legendary, Supplier<Float> health) {
		this.god = god;
		this.boss = boss;
		this.legendary = legendary;
		this.health = health;
	}

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public EntityType<? extends Mob> bossType() {
		return boss.get();
	}

	public Item legendary() {
		return legendary.get();
	}

	public float baseHealth() {
		return health.get();
	}

	public Component displayName() {
		return Component.translatable("olympus.event." + id()).withStyle(god.style());
	}

	/** @return an error message if the event can't start here, empty if it can. */
	public Optional<Component> checkLocation(ServerLevel level, BlockPos pos) {
		return switch (this) {
			case RISE_OF_THE_DEEP -> {
				if (!level.getBiome(pos).is(BiomeTags.IS_OCEAN) || waterDepth(level, pos) < 10) {
					yield Optional.of(Component.translatable("olympus.event.needs_ocean"));
				}
				yield Optional.empty();
			}
			case GATES_OF_THE_UNDERWORLD -> level.dimension() == Level.NETHER ? Optional.empty()
				: Optional.of(Component.translatable("olympus.event.needs_nether"));
			default -> Optional.empty();
		};
	}

	private static int waterDepth(ServerLevel level, BlockPos pos) {
		BlockPos p = pos;
		// Find the water surface (the admin may be on a boat or flying above it).
		for (int i = 0; i < 16 && !level.getFluidState(p).is(Fluids.WATER); i++) {
			p = p.below();
		}
		int depth = 0;
		while (depth < 64 && level.getFluidState(p).is(Fluids.WATER)) {
			depth++;
			p = p.below();
		}
		return depth;
	}

	public static Optional<OlympusEvent> byId(String id) {
		for (OlympusEvent e : values()) {
			if (e.id().equalsIgnoreCase(id)) return Optional.of(e);
		}
		return Optional.empty();
	}
}
