package com.olympus.mixin;

import com.olympus.god.GodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleContents.class)
public abstract class BundleContentsMixin {
	@Inject(method = "canItemBeInBundle", at = @At("HEAD"), cancellable = true)
	private static void olympus$noRelics(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (GodItem.is(stack)) {
			cir.setReturnValue(false);
		}
	}
}
