package com.olympus.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Marks the non-player slots of every storage screen, on the client and the server alike, so
 * relics can't be put in chests, barrels, ender chests, hoppers, dispensers, crafters, furnaces,
 * brewing stands, shulker boxes or donkey/llama/boat/minecart chests. Crafting, smithing and
 * anvil screens are untouched.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
	/** No throwing relics out of an inventory screen (Q on a slot, or clicking outside the window). */
	@Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
	private void olympus$noRelicToss(int slotId, int button, net.minecraft.world.inventory.ClickType type, net.minecraft.world.entity.player.Player player,
		org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
		if (player.getAbilities().instabuild) {
			return;
		}
		AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;
		boolean toss = (type == net.minecraft.world.inventory.ClickType.THROW && slotId >= 0 && slotId < self.slots.size()
			&& com.olympus.god.GodItem.is(self.slots.get(slotId).getItem()))
			|| (slotId == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE && type == net.minecraft.world.inventory.ClickType.PICKUP
			&& com.olympus.god.GodItem.is(self.getCarried()));
		if (toss) {
			ci.cancel();
			if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
				sp.displayClientMessage(net.minecraft.network.chat.Component.translatable("olympus.message.no_drop").withStyle(net.minecraft.ChatFormatting.RED), true);
			}
		}
	}

	@Inject(method = "addSlot", at = @At("HEAD"))
	private void olympus$markStorage(Slot slot, CallbackInfoReturnable<Slot> cir) {
		Object self = this;
		boolean storageMenu = self instanceof ChestMenu || self instanceof ShulkerBoxMenu || self instanceof HopperMenu
			|| self instanceof DispenserMenu || self instanceof CrafterMenu || self instanceof HorseInventoryMenu
			|| self instanceof AbstractFurnaceMenu || self instanceof BrewingStandMenu;
		if (storageMenu && !(slot.container instanceof Inventory)) {
			((com.olympus.util.StorageSlot) slot).olympus$setStorage(true);
		}
	}
}
