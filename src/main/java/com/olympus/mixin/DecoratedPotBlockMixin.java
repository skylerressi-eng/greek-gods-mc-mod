package com.olympus.mixin;

import com.olympus.god.GodItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Relics can't be stuffed into decorated pots. */
@Mixin(DecoratedPotBlock.class)
public abstract class DecoratedPotBlockMixin {
	@Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
	private void olympus$noRelics(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		if (GodItem.is(stack)) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}
}
