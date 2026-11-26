package com.olympus.client.mixin;

import com.olympus.registry.ModItems;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Helm of Darkness: an invisible wearer shows nothing at all, not even armor, held items,
 * stuck arrows or cape (also in your own third-person view). Other players already receive no
 * equipment from the server while you're hidden; this covers your own client.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
	private void olympus$hideEverything(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
		if (avatar.isInvisible() && avatar.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HELM_OF_DARKNESS)) {
			state.headEquipment = ItemStack.EMPTY;
			state.chestEquipment = ItemStack.EMPTY;
			state.legsEquipment = ItemStack.EMPTY;
			state.feetEquipment = ItemStack.EMPTY;
			state.rightHandItemState.clear();
			state.leftHandItemState.clear();
			state.arrowCount = 0;
			state.showCape = false;
		}
	}
}
