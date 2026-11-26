package com.olympus.mixin;

import com.olympus.handler.MirrorHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mirror of Aphrodite: pacified mobs can't pick the charmer as a target. */
@Mixin(Mob.class)
public abstract class MobMixin {
	/** Mobs never pick up god items (a zombie could despawn with someone's relic). */
	@Inject(method = "wantsToPickUp", at = @At("HEAD"), cancellable = true)
	private void olympus$noRelics(net.minecraft.server.level.ServerLevel level, net.minecraft.world.item.ItemStack stack,
		org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
		if (com.olympus.god.GodItem.is(stack)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void olympus$pacified(LivingEntity target, CallbackInfo ci) {
		if (MirrorHandler.isPacifiedAgainst((Mob) (Object) this, target)) {
			ci.cancel();
		}
	}
}
