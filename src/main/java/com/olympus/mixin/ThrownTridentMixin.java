package com.olympus.mixin;

import com.olympus.god.GodItem;
import com.olympus.handler.LegendaryHandler;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin {
	/** God tridents always come back to their owner (like Loyalty III), so they can't be lost or stolen mid-air. */
	@Inject(method = "getLoyaltyFromItem", at = @At("RETURN"), cancellable = true)
	private void olympus$alwaysReturn(ItemStack stack, CallbackInfoReturnable<Byte> cir) {
		if (GodItem.is(stack) && cir.getReturnValue() < 3) {
			cir.setReturnValue((byte) 3);
		}
	}

	@Inject(method = "onHitEntity", at = @At("TAIL"))
	private void olympus$godHit(EntityHitResult result, CallbackInfo ci) {
		LegendaryHandler.onTridentHitEntity((ThrownTrident) (Object) this, result.getEntity());
	}
}
