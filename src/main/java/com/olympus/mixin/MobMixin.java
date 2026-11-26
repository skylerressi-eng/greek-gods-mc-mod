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
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void olympus$pacified(LivingEntity target, CallbackInfo ci) {
		if (MirrorHandler.isPacifiedAgainst((Mob) (Object) this, target)) {
			ci.cancel();
		}
	}
}
