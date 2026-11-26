package com.olympus.mixin;

import com.olympus.handler.AegisHandler;
import com.olympus.handler.HelmHandler;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Shadow
	@Final
	private Map<EquipmentSlot, ItemStack> lastEquipmentItems;

	/** Aegis: react to blocked melee hits. */
	@Inject(method = "blockUsingItem", at = @At("HEAD"))
	private void olympus$onBlock(ServerLevel level, LivingEntity attacker, CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayer player) {
			AegisHandler.onMeleeBlocked(player, attacker);
		}
	}

	/** Helm of Darkness: truly invisible, armor or not. */
	@Inject(method = "getVisibilityPercent", at = @At("RETURN"), cancellable = true)
	private void olympus$helmVisibility(Entity targetingEntity, CallbackInfoReturnable<Double> cir) {
		if ((Object) this instanceof Player player && HelmHandler.isHidden(player)) {
			cir.setReturnValue(Math.min(cir.getReturnValue(), 0.07));
		}
	}

	/** Helm of Darkness: don't leak equipment changes while hidden (they are resent on reveal). */
	@Inject(method = "handleEquipmentChanges", at = @At("HEAD"), cancellable = true)
	private void olympus$hideEquipment(Map<EquipmentSlot, ItemStack> changed, CallbackInfo ci) {
		if ((Object) this instanceof Player player && HelmHandler.isHidden(player)) {
			changed.forEach((slot, stack) -> lastEquipmentItems.put(slot, stack.copy()));
			ci.cancel();
		}
	}
}
