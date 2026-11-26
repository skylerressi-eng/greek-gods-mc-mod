package com.olympus.item.relic;

import com.olympus.ability.Cooldowns;
import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.god.GodItem;
import com.olympus.god.UpgradeLevel;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Demeter — Sickle of Demeter. A netherite hoe; right-click crops to make everything in a 5x5 area
 * ripen instantly. Right-clicking anything that isn't near crops tills like a normal hoe.
 */
public class SickleItem extends Item implements GodItem {
	public SickleItem(Properties properties) {
		super(properties);
	}

	@Override
	public God god() {
		return God.DEMETER;
	}

	@Override
	public boolean isStarterRelic() {
		return true;
	}

	@Override
	public int primaryCooldownSeconds() {
		return ConfigManager.get().sickle.cooldownSeconds;
	}

	@Override
	public int blessedCooldownSeconds() {
		return ConfigManager.get().sickle.harvestCooldownSeconds;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos center = context.getClickedPos();
		// Clicking the farmland under a crop counts as clicking the crop.
		if (!isGrowable(level.getBlockState(center)) && isGrowable(level.getBlockState(center.above()))) {
			center = center.above();
		}
		int radius = GodItem.level(context.getItemInHand()) == UpgradeLevel.ASCENDED
			? (int) Math.round(UpgradeLevel.ASCENDED.radius(ConfigManager.get().sickle.growRadius))
			: ConfigManager.get().sickle.growRadius;
		if (!hasGrowableCrops(level, center, radius) || context.getPlayer() == null) {
			return super.useOn(context); // normal hoe tilling
		}
		return tryBloom(level, context.getPlayer(), context.getItemInHand(), center, radius);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		int radius = ConfigManager.get().sickle.growRadius;
		BlockPos center = player.blockPosition();
		if (!hasGrowableCrops(level, center, radius)) {
			return InteractionResult.PASS;
		}
		return tryBloom(level, player, player.getItemInHand(hand), center, radius);
	}

	private InteractionResult tryBloom(Level level, Player player, ItemStack stack, BlockPos center, int radius) {
		if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		if (!Cooldowns.checkReady(serverPlayer, stack, Cooldowns.Kind.PRIMARY)) {
			return InteractionResult.FAIL;
		}
		int grown = 0;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -1, -radius), center.offset(radius, 1, radius))) {
			if (growFully(serverLevel, pos.immutable())) {
				grown++;
				serverLevel.sendParticles(ModParticles.GRAIN_LEAF, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.02);
				serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0);
			}
		}
		if (grown == 0) {
			return InteractionResult.PASS;
		}
		Vec3 c = Vec3.atBottomCenterOf(center);
		Fx.sound(serverLevel, c, ModSounds.SICKLE_BLOOM, 1.0f, 1.0f);
		Fx.shockwave(serverLevel, ModParticles.GRAIN_LEAF, c.add(0, 0.3, 0), radius + 0.5);
		Cooldowns.start(serverPlayer, stack, Cooldowns.Kind.PRIMARY, primaryCooldownSeconds());
		return InteractionResult.SUCCESS_SERVER;
	}

	private static boolean hasGrowableCrops(Level level, BlockPos center, int radius) {
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -1, -radius), center.offset(radius, 1, radius))) {
			BlockState state = level.getBlockState(pos);
			if (isGrowable(state) && !isMature(level, pos, state)) {
				return true;
			}
		}
		return false;
	}

	/** Crops only: never saplings, grass or other bonemealable blocks that could change the world. */
	public static boolean isGrowable(BlockState state) {
		return state.is(BlockTags.CROPS) || state.is(Blocks.NETHER_WART) || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.COCOA);
	}

	private static boolean isMature(Level level, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof CropBlock crop) {
			return crop.isMaxAge(state);
		}
		if (state.is(Blocks.NETHER_WART)) {
			return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
		}
		return !(state.getBlock() instanceof BonemealableBlock b) || !b.isValidBonemealTarget(level, pos, state, BonemealSource.INTERACTION);
	}

	public static boolean growFully(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!isGrowable(state) || isMature(level, pos, state)) {
			return false;
		}
		if (state.getBlock() instanceof CropBlock crop) {
			level.setBlock(pos, crop.getStateForAge(crop.getMaxAge()), 2);
			return true;
		}
		if (state.is(Blocks.NETHER_WART)) {
			level.setBlock(pos, state.setValue(NetherWartBlock.AGE, NetherWartBlock.MAX_AGE), 2);
			return true;
		}
		if (state.getBlock() instanceof BonemealableBlock b) {
			// Stems, berries, cocoa: apply growth until ripe (bounded).
			for (int i = 0; i < 8; i++) {
				BlockState current = level.getBlockState(pos);
				if (!(current.getBlock() instanceof BonemealableBlock bb) || !bb.isValidBonemealTarget(level, pos, current, BonemealSource.INTERACTION)) {
					break;
				}
				bb.performBonemeal(level, level.getRandom(), pos, current, BonemealSource.INTERACTION);
			}
			return true;
		}
		return false;
	}
}
