package com.olympus.mixin;

import com.olympus.handler.HephaestusHandler;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hammer of Hephaestus: smelt ore drops as they are rolled. */
@Mixin(Block.class)
public abstract class BlockMixin {
	@Inject(
		method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;",
		at = @At("RETURN"), cancellable = true)
	private static void olympus$autoSmelt(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity, Entity breaker, ItemInstance tool,
		CallbackInfoReturnable<List<ItemStack>> cir) {
		if (tool instanceof ItemStack stack && breaker != null) {
			cir.setReturnValue(HephaestusHandler.smeltDrops(state, level, pos, stack, cir.getReturnValue()));
		}
	}
}
