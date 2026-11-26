package com.olympus.mixin;

import com.olympus.god.GodItem;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxSlot.class)
public abstract class ShulkerBoxSlotMixin {
	@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void olympus$noRelics(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (GodItem.is(stack)) {
			cir.setReturnValue(false);
		}
	}
}
