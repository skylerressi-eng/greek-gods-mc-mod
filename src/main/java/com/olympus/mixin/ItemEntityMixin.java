package com.olympus.mixin;

import com.olympus.god.GodItem;
import com.olympus.limit.GodItemLimit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Dropped god items: never despawn, glow, survive everything except the void, respect the carry limit. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Shadow
	public abstract ItemStack getItem();

	@Shadow
	public abstract void setUnlimitedLifetime();

	@Unique
	private boolean olympus$protected;

	@Inject(method = "tick", at = @At("HEAD"))
	private void olympus$protect(CallbackInfo ci) {
		if (!olympus$protected && GodItem.is(getItem())) {
			olympus$protected = true;
			setUnlimitedLifetime();
			((ItemEntity) (Object) this).setGlowingTag(true);
		}
	}

	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void olympus$indestructible(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (GodItem.is(getItem()) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
	private void olympus$limit(Player player, CallbackInfo ci) {
		if (!player.level().isClientSide() && GodItem.is(getItem()) && !GodItemLimit.canTakeAnother(player)) {
			GodItemLimit.deny(player);
			ci.cancel();
		}
	}
}
