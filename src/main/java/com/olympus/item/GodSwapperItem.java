package com.olympus.item;

import com.olympus.claim.ClaimManager;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.god.GodItem;
import com.olympus.registry.ModComponents;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

/**
 * God Swapper: hold it in the main hand with a starter relic in the offhand and hold right-click
 * for 3 seconds. The relic becomes a different god's starter relic, keeping its upgrade level and
 * every enchantment the new item can take. The swapper is used up.
 */
public class GodSwapperItem extends Item {
	public static final int CHANNEL_TICKS = 60;

	public GodSwapperItem(Properties properties) {
		super(properties);
	}

	private static boolean hasStarterInOffhand(Player player) {
		ItemStack off = player.getOffhandItem();
		return off.getItem() instanceof GodItem god && god.isStarterRelic();
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND || !hasStarterInOffhand(player)) {
			if (player instanceof ServerPlayer sp) {
				sp.displayClientMessage(Component.translatable("olympus.message.swapper_howto").withStyle(ChatFormatting.YELLOW), true);
			}
			return InteractionResult.FAIL;
		}
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return CHANNEL_TICKS;
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
		if (!(level instanceof ServerLevel server) || !(entity instanceof Player player)) {
			return;
		}
		if (!hasStarterInOffhand(player)) {
			player.releaseUsingItem();
			return;
		}
		int elapsed = CHANNEL_TICKS - remaining;
		double angle = elapsed * 0.5;
		double r = 1.2 - elapsed / (double) CHANNEL_TICKS * 0.8;
		var p = player.position();
		server.sendParticles(ModParticles.DIVINE_GLINT, p.x + Math.cos(angle) * r, p.y + 0.2 + elapsed * 0.03, p.z + Math.sin(angle) * r, 1, 0, 0, 0, 0);
		server.sendParticles(ModParticles.DIVINE_GLINT, p.x - Math.cos(angle) * r, p.y + 0.2 + elapsed * 0.03, p.z - Math.sin(angle) * r, 1, 0, 0, 0, 0);
		if (elapsed % 20 == 0) {
			Fx.sound(player, ModSounds.CLAIM_CHIME, 0.5f, 0.6f + elapsed / 60f);
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack swapper, Level level, LivingEntity entity) {
		if (!(entity instanceof ServerPlayer player) || !hasStarterInOffhand(player)) {
			return swapper;
		}
		ItemStack oldRelic = player.getOffhandItem();
		God oldGod = ((GodItem) oldRelic.getItem()).god();
		List<God> choices = new ArrayList<>(List.of(God.OLYMPIANS));
		choices.remove(oldGod);
		God newGod = choices.get(player.getRandom().nextInt(choices.size()));

		ItemStack newRelic = new ItemStack(ClaimManager.starterRelic(newGod));
		newRelic.set(ModComponents.UPGRADE_LEVEL, oldRelic.getOrDefault(ModComponents.UPGRADE_LEVEL, 0));
		ItemEnchantments oldEnchants = oldRelic.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		ItemEnchantments.Mutable kept = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		for (var entry : oldEnchants.entrySet()) {
			Holder<Enchantment> enchantment = entry.getKey();
			if (enchantment.value().canEnchant(newRelic)) {
				kept.set(enchantment, entry.getIntValue());
			}
		}
		newRelic.set(DataComponents.ENCHANTMENTS, kept.toImmutable());
		if (oldRelic.has(DataComponents.CUSTOM_NAME)) {
			newRelic.set(DataComponents.CUSTOM_NAME, oldRelic.get(DataComponents.CUSTOM_NAME));
		}
		com.olympus.item.relic.AegisItem.applyEmblem(newRelic, player.level().registryAccess());
		player.setItemInHand(InteractionHand.OFF_HAND, newRelic);

		ClaimManager.claim(player, newGod, false);
		Fx.burst(player.level(), newGod.particle(), player.position().add(0, 1, 0), 60, 0.6, 0.1);
		swapper.shrink(1);
		return swapper;
	}
}
