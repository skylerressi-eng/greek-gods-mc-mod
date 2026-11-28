package com.olympus.mixin;

import com.olympus.god.GodItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shelves can't hold relics: no placing one, and no hotbar swap while a relic is in the hotbar. */
@Mixin(ShelfBlock.class)
public abstract class ShelfBlockMixin {
	@Inject(method = "swapSingleItem", at = @At("HEAD"), cancellable = true)
	private static void olympus$noRelic(ItemStack stack, Player player, ShelfBlockEntity shelf, int slot, Inventory inventory,
		CallbackInfoReturnable<Boolean> cir) {
		if (GodItem.is(stack)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "swapHotbar", at = @At("HEAD"), cancellable = true)
	private void olympus$noRelicRow(Level level, BlockPos pos, Inventory inventory, CallbackInfoReturnable<Boolean> cir) {
		for (int i = 0; i < Inventory.getSelectionSize(); i++) {
			if (GodItem.is(inventory.getItem(i))) {
				cir.setReturnValue(false);
				return;
			}
		}
	}
}
