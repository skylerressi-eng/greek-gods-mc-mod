package com.olympus.mixin;

import com.olympus.god.GodItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Relics keep their divine names: an anvil can still enchant them, but never rename them. */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
	@Inject(method = "createResult", at = @At("TAIL"))
	private void olympus$noRename(CallbackInfo ci) {
		ItemCombinerMenuAccessor self = (ItemCombinerMenuAccessor) this;
		ItemStack input = self.olympus$inputSlots().getItem(0);
		ItemStack result = self.olympus$resultSlots().getItem(0);
		if (!GodItem.is(input) || result.isEmpty()) {
			return;
		}
		result.remove(DataComponents.CUSTOM_NAME);
		if (ItemStack.isSameItemSameComponents(result, input)) {
			self.olympus$resultSlots().setItem(0, ItemStack.EMPTY); // renaming was the only change
		}
	}
}
