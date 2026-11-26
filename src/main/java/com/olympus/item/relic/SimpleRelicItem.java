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
	private boolean blessedPassive;

	public SimpleRelicItem(Properties properties, God god, boolean starter, IntSupplier primaryCooldown, IntSupplier blessedCooldown) {
		super(properties);
		this.god = god;
		this.starter = starter;
		this.primaryCooldown = primaryCooldown;
		this.blessedCooldown = blessedCooldown;
	}

	/** Marks the Blessed ability as passive even though it has a cooldown (e.g. Golden Fleece). */
	public SimpleRelicItem passiveBlessed() {
		this.blessedPassive = true;
		return this;
	}

	@Override
	public boolean blessedIsPassive() {
		return blessedPassive || GodItemDefaults.passive(blessedCooldown.getAsInt());
	}

	private static final class GodItemDefaults {
		static boolean passive(int cooldown) {
			return cooldown <= 0;
		}
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
