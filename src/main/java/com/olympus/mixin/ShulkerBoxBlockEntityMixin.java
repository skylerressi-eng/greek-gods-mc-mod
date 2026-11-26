package com.olympus.mixin;

import com.olympus.god.GodItem;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hoppers can't push relics into shulker boxes either. */
@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin {
	@Inject(method = "canPlaceItemThroughFace", at = @At("HEAD"), cancellable = true)
	private void olympus$noRelics(int slot, ItemStack stack, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (GodItem.is(stack)) {
			cir.setReturnValue(false);
		}
	}
}
