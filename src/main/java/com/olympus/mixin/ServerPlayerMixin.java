package com.olympus.mixin;

import com.olympus.god.GodItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Relics can't be thrown away with the drop key (they only drop when you die; use /trade to give one). */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
	@Inject(method = "drop(Z)V", at = @At("HEAD"), cancellable = true)
	private void olympus$noRelicToss(boolean fullStack, CallbackInfo ci) {
		ServerPlayer self = (ServerPlayer) (Object) this;
		if (!self.getAbilities().instabuild && GodItem.is(self.getInventory().getSelectedItem())) {
			ci.cancel();
			self.containerMenu.sendAllDataToRemote(); // undo any client-side prediction
			self.displayClientMessage(Component.translatable("olympus.message.no_drop").withStyle(ChatFormatting.RED), true);
		}
	}
}
