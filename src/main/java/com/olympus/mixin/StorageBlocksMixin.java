package com.olympus.mixin;

import com.olympus.god.GodItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hoppers (and hopper minecarts) never suck a dropped relic into storage. */
@Mixin(HopperBlockEntity.class)
public abstract class StorageBlocksMixin {
	@Inject(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z", at = @At("HEAD"), cancellable = true)
	private static void olympus$noRelics(Container container, ItemEntity item, CallbackInfoReturnable<Boolean> cir) {
		if (GodItem.is(item.getItem())) {
			cir.setReturnValue(false);
		}
	}
}
