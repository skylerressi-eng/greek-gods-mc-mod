package com.olympus.client;

import com.olympus.god.GodItem;
import com.olympus.god.UpgradeLevel;
import com.olympus.registry.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Small indicator left of the hotbar: the relic that answers the Divine Power key, with its
 * Blessed cooldown counting down (or "R" when ready).
 */
public final class DivinePowerHud {
	private DivinePowerHud() {
	}

	private static ItemStack activeRelic(Player player) {
		ItemStack[] order = {player.getMainHandItem(), player.getOffhandItem(), player.getItemBySlot(EquipmentSlot.HEAD),
			player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET)};
		for (ItemStack stack : order) {
			if (stack.getItem() instanceof GodItem god && !god.blessedIsPassive() && GodItem.level(stack).atLeast(UpgradeLevel.BLESSED)) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	public static void render(GuiGraphics graphics, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui || mc.level == null) {
			return;
		}
		ItemStack relic = activeRelic(mc.player);
		if (relic.isEmpty()) {
			return;
		}
		int x = graphics.guiWidth() / 2 - 91 - 29 - 24;
		int y = graphics.guiHeight() - 22;
		graphics.fill(x, y, x + 22, y + 22, 0x90000000);
		graphics.renderItem(relic, x + 3, y + 3);

		long readyAt = relic.getOrDefault(ModComponents.COOLDOWNS, ModComponents.StackCooldowns.NONE).blessedReadyAt();
		long remaining = readyAt - mc.level.getGameTime();
		String key = OlympusClient.DIVINE_POWER.getTranslatedKeyMessage().getString();
		if (remaining > 0) {
			// Darken bottom-up as the cooldown runs down.
			int total = Math.max(1, (int) Math.min(remaining + 1, 20L * 600));
			graphics.fill(x, y, x + 22, y + 22, 0x80000000);
			String text = remaining >= 1200 ? (remaining / 1200) + "m" : ((remaining + 19) / 20) + "s";
			graphics.drawString(mc.font, text, x + 11 - mc.font.width(text) / 2, y + 7, 0xFFFF5555, true);
		} else {
			graphics.drawString(mc.font, key, x + 22 - mc.font.width(key), y + 14, 0xFF55FFFF, true);
		}
	}
}
