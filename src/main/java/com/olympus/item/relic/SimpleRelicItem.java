package com.olympus.item.relic;

import com.olympus.god.God;
import com.olympus.god.GodItem;
import java.util.function.IntSupplier;
import net.minecraft.world.item.Item;

/**
 * A god item whose powers live in event handlers (weapons, armor, passives).
 * Cooldowns are suppliers so they always reflect the live config.
 */
public class SimpleRelicItem extends Item implements GodItem {
	private final God god;
	private final boolean starter;
	private final IntSupplier primaryCooldown;
	private final IntSupplier blessedCooldown;

	public SimpleRelicItem(Properties properties, God god, boolean starter, IntSupplier primaryCooldown, IntSupplier blessedCooldown) {
		super(properties);
		this.god = god;
		this.starter = starter;
		this.primaryCooldown = primaryCooldown;
		this.blessedCooldown = blessedCooldown;
	}

	@Override
	public God god() {
		return god;
	}

	@Override
	public boolean isStarterRelic() {
		return starter;
	}

	@Override
	public int primaryCooldownSeconds() {
		return primaryCooldown.getAsInt();
	}

	@Override
	public int blessedCooldownSeconds() {
		return blessedCooldown.getAsInt();
	}
}
