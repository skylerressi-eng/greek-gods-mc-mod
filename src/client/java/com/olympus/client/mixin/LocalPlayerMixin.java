package com.olympus.client.mixin;

import com.olympus.god.GodItem;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Don't even predict tossing a relic: the server refuses it. */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
	@Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
	private void olympus$noRelicToss(boolean fullStack, CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer self = (LocalPlayer) (Object) this;
		if (!self.getAbilities().instabuild && GodItem.is(self.getInventory().getSelectedItem())) {
			cir.setReturnValue(false);
		}
	}
}
