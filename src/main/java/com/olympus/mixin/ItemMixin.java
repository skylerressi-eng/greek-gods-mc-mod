package com.olympus.mixin;

import com.olympus.god.GodItem;
import com.olympus.god.UpgradeLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Upgrade stars in god item names, a gold name and enchant glint at Ascended. */
@Mixin(Item.class)
public abstract class ItemMixin {
	@Inject(method = "getName(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/chat/Component;", at = @At("RETURN"), cancellable = true)
	private void olympus$decorateName(ItemStack stack, CallbackInfoReturnable<Component> cir) {
		if (this instanceof GodItem) {
			UpgradeLevel level = GodItem.level(stack);
			if (level == UpgradeLevel.MORTAL) {
				return;
			}
			var name = cir.getReturnValue().copy().append(level.stars());
			cir.setReturnValue(level == UpgradeLevel.ASCENDED ? name.withStyle(ChatFormatting.GOLD) : name);
		}
	}

	@Inject(method = "isFoil", at = @At("RETURN"), cancellable = true)
	private void olympus$ascendedGlint(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (this instanceof GodItem && GodItem.level(stack) == UpgradeLevel.ASCENDED) {
			cir.setReturnValue(true);
		}
	}
}
