package com.olympus.item;

import com.olympus.config.ConfigManager;
import com.olympus.data.PlayerOlympusData;
import com.olympus.fx.Fx;
import com.olympus.limit.GodItemLimit;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Laurel of Olympus: the gods' favor. Right-click to permanently carry one more god item
 * (up to the configured maximum). Kept through death; used up on use.
 */
public class LaurelItem extends Item {
	public LaurelItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!(player instanceof ServerPlayer sp)) {
			return InteractionResult.SUCCESS;
		}
		PlayerOlympusData data = PlayerOlympusData.get(sp);
		int max = ConfigManager.get().general.laurelMaxBonusSlots;
		if (data.bonusSlots() >= max) {
			sp.displayClientMessage(Component.translatable("olympus.message.laurel_maxed", GodItemLimit.max(sp)).withStyle(ChatFormatting.GRAY), true);
			return InteractionResult.FAIL;
		}
		int slots = Math.min(max, data.bonusSlots() + Math.max(1, ConfigManager.get().general.laurelSlotsPerUse));
		PlayerOlympusData.set(sp, data.withBonusSlots(slots));
		player.getItemInHand(hand).consume(1, player);
		sp.displayClientMessage(Component.translatable("olympus.message.laurel_used", GodItemLimit.max(sp)).withStyle(ChatFormatting.GOLD), true);
		Fx.sound(sp, ModSounds.CLAIM_CHIME, 1.0f, 1.2f);
		Fx.helix(sp.level(), ModParticles.DIVINE_GLINT, sp.position(), 0.8, 2.4, 50);
		Fx.burst(sp.level(), ModParticles.GRAIN_LEAF, sp.position().add(0, 2.2, 0), 20, 0.4, 0.02);
		return InteractionResult.SUCCESS_SERVER;
	}
}
