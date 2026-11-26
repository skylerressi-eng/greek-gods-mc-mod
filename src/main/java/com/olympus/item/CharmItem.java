package com.olympus.item;

import com.olympus.ability.Cooldowns;
import com.olympus.god.God;
import com.olympus.god.GodItem;
import com.olympus.god.UpgradeLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A right-click relic: activates instantly on use, then goes on its primary cooldown. */
public abstract class CharmItem extends Item implements GodItem {
	private final God god;
	private final boolean starter;

	protected CharmItem(Properties properties, God god, boolean starter) {
		super(properties);
		this.god = god;
		this.starter = starter;
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
	public boolean isCharm() {
		return true;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}
		if (!Cooldowns.checkReady(serverPlayer, stack, Cooldowns.Kind.PRIMARY)) {
			return InteractionResult.FAIL;
		}
		if (activate((ServerLevel) level, serverPlayer, stack, GodItem.level(stack))) {
			Cooldowns.start(serverPlayer, stack, Cooldowns.Kind.PRIMARY, primaryCooldownSeconds());
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.FAIL;
	}

	/** Server-side power. @return true if it fired (starts the cooldown). */
	protected abstract boolean activate(ServerLevel level, ServerPlayer player, ItemStack stack, UpgradeLevel upgrade);
}
