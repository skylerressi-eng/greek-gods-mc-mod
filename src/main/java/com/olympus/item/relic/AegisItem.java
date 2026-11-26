package com.olympus.item.relic;

import com.olympus.god.God;
import com.olympus.god.GodItem;
import net.minecraft.world.item.Item;

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
}
