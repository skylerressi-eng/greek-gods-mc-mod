package com.olympus.item.relic;

import com.olympus.config.ConfigManager;
import com.olympus.god.God;
import com.olympus.god.UpgradeLevel;
import com.olympus.handler.UnderworldAllies;
import com.olympus.item.CharmItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Hades — Bident (melee only). Right-click: raise skeleton warriors that fight for you. */
public class BidentItem extends CharmItem {
	public BidentItem(Properties properties) {
		super(properties, God.HADES, false);
	}

	@Override
	public boolean isCharm() {
		return false; // it's a weapon: enchantable
	}

	@Override
	public int primaryCooldownSeconds() {
		return ConfigManager.get().bident.cooldownSeconds;
	}

	@Override
	public int blessedCooldownSeconds() {
		return ConfigManager.get().bident.soulHarvestCooldownSeconds;
	}

	@Override
	protected boolean activate(ServerLevel level, ServerPlayer player, ItemStack stack, UpgradeLevel upgrade) {
		int ticks = upgrade.durationTicks(ConfigManager.get().bident.allySeconds);
		int spawned = 0;
		for (int i = 0; i < ConfigManager.get().bident.skeletons; i++) {
			double a = Math.PI * 2 * i / ConfigManager.get().bident.skeletons;
			if (UnderworldAllies.summon(player, player.position().add(Math.cos(a) * 2, 0, Math.sin(a) * 2), ticks) != null) {
				spawned++;
			}
		}
		return spawned > 0;
	}
}
