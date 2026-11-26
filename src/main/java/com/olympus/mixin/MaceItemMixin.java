package com.olympus.mixin;

import com.olympus.handler.LegendaryHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.MaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** No mace smash bonus while launched by a divine power (Skybreaker): no 15-block mace one-shots. */
@Mixin(MaceItem.class)
public abstract class MaceItemMixin {
	@Inject(method = "getAttackDamageBonus", at = @At("HEAD"), cancellable = true)
	private void olympus$noDivineSmash(Entity target, float damage, DamageSource source, CallbackInfoReturnable<Float> cir) {
		if (source.getEntity() != null && LegendaryHandler.isDivinelyBorne(source.getEntity())) {
			cir.setReturnValue(0.0f);
		}
	}
}
