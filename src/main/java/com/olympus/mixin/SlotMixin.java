package com.olympus.mixin;

import com.olympus.god.GodItem;
import com.olympus.limit.GodItemLimit;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
	@Shadow
	@Final
	public Container container;

	@Shadow
	public abstract ItemStack getItem();

	/** No hiding relics in the ender chest. */
	@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void olympus$noEnderChest(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (container instanceof PlayerEnderChestContainer && GodItem.is(stack)) {
			cir.setReturnValue(false);
		}
	}

	/** Taking a god item out of a chest / crafting / smithing output counts toward the limit. */
	@Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
	private void olympus$limit(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (!(container instanceof Inventory) && GodItem.is(getItem()) && !GodItemLimit.canTakeAnother(player)) {
			GodItemLimit.deny(player);
			cir.setReturnValue(false);
		}
	}
}
