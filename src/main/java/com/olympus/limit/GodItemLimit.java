package com.olympus.limit;

import com.olympus.config.ConfigManager;
import com.olympus.god.GodItem;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * "The gods allow you only 3 relics." Counts god items in the inventory, hotbar, armor, offhand,
 * the 2x2 crafting grid and the cursor. Creative-mode operators are exempt.
 */
public final class GodItemLimit {
	private static final Map<UUID, Long> LAST_MESSAGE = new ConcurrentHashMap<>();

	private GodItemLimit() {
	}

	public static int max() {
		return Math.max(0, ConfigManager.get().general.maxGodItems);
	}

	public static boolean bypasses(Player player) {
		return player.canUseGameMasterBlocks();
	}

	public static int count(Player player) {
		int n = 0;
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (GodItem.is(inv.getItem(i))) {
				n += inv.getItem(i).getCount();
			}
		}
		if (GodItem.is(player.containerMenu.getCarried())) {
			n += player.containerMenu.getCarried().getCount();
		}
		var grid = player.inventoryMenu.getCraftSlots();
		for (int i = 0; i < grid.getContainerSize(); i++) {
			if (GodItem.is(grid.getItem(i))) {
				n++;
			}
		}
		return n;
	}

	/** May this player gain one more god item right now? */
	public static boolean canTakeAnother(Player player) {
		return bypasses(player) || count(player) < max();
	}

	public static void deny(Player player) {
		if (!(player instanceof ServerPlayer sp)) {
			return;
		}
		long now = sp.level().getGameTime();
		if (now - LAST_MESSAGE.getOrDefault(sp.getUUID(), 0L) >= 40) {
			LAST_MESSAGE.put(sp.getUUID(), now);
			sp.displayClientMessage(Component.translatable("olympus.message.limit", max()).withStyle(ChatFormatting.GOLD), true);
		}
	}

	/** If a player is over the limit (config change, /give...), drop the extras at their feet. */
	public static void sweep(ServerPlayer player) {
		if (bypasses(player)) {
			return;
		}
		int over = count(player) - max();
		if (over <= 0) {
			return;
		}
		// Cursor first, then the main inventory from the back; armor, hands and hotbar are kept longest.
		ItemStack carried = player.containerMenu.getCarried();
		if (over > 0 && GodItem.is(carried)) {
			player.drop(carried.copy(), false);
			player.containerMenu.setCarried(ItemStack.EMPTY);
			over--;
		}
		Inventory inv = player.getInventory();
		for (int i = Inventory.INVENTORY_SIZE - 1; i >= 0 && over > 0; i--) {
			if (i == inv.getSelectedSlot()) {
				continue;
			}
			ItemStack stack = inv.getItem(i);
			if (GodItem.is(stack)) {
				player.drop(inv.removeItemNoUpdate(i), false);
				over--;
			}
		}
		for (int i = 0; i < inv.getContainerSize() && over > 0; i++) {
			ItemStack stack = inv.getItem(i);
			if (GodItem.is(stack)) {
				player.drop(inv.removeItemNoUpdate(i), false);
				over--;
			}
		}
		deny(player);
	}

	public static void forget(UUID player) {
		LAST_MESSAGE.remove(player);
	}
}
