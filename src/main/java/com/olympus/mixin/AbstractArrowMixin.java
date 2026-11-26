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
	@Inject(method = "tick", at = @At("HEAD"))
	private void olympus$silverArrowTick(CallbackInfo ci) {
		AbstractArrow self = (AbstractArrow) (Object) this;
		if (self.getTags().contains(SilverBowItem.ARROW_TAG)) {
			SilverBowHandler.tickArrow(self);
		}
	}
}
