package com.olympus.mixin;

import com.olympus.god.GodItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Item frames can't hold relics (no stashing or handing them over that way). */
@Mixin(ItemFrame.class)
public abstract class DisplayEntitiesMixin {
	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void olympus$noRelics(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (!player.getAbilities().instabuild && GodItem.is(player.getItemInHand(hand))) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}
}
