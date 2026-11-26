package com.olympus.mixin;

import com.olympus.handler.SilverBowHandler;
import com.olympus.item.relic.SilverBowItem;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
	@Inject(method = "onHitBlock", at = @At("TAIL"))
	private void olympus$hitBlock(net.minecraft.world.phys.BlockHitResult result, CallbackInfo ci) {
		AbstractArrow self = (AbstractArrow) (Object) this;
		if (self instanceof net.minecraft.world.entity.projectile.arrow.ThrownTrident trident) {
			com.olympus.handler.LegendaryHandler.onTridentHitBlock(trident);
		}
		if (self.getTags().contains(com.olympus.item.SunArrowItem.TAG)) {
			com.olympus.handler.CraftedRelicHandler.onSunArrowHit(self, null);
		}
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void olympus$silverArrowTick(CallbackInfo ci) {
		AbstractArrow self = (AbstractArrow) (Object) this;
		if (self.getTags().contains(SilverBowItem.ARROW_TAG)) {
			SilverBowHandler.tickArrow(self);
		}
	}
}
