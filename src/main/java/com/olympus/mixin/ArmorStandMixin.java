package com.olympus.mixin;

import com.olympus.god.GodItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Armor stands can't hold relics either. */
@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin {
	@Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
	private void olympus$noRelics(Player player, Vec3 at, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (!player.getAbilities().instabuild && GodItem.is(player.getItemInHand(hand))) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}
}
