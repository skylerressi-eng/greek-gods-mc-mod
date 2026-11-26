package com.olympus.item.relic;

import com.olympus.Olympus;
import com.olympus.ability.Cooldowns;
import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.god.GodItem;
import com.olympus.god.UpgradeLevel;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/** Dionysus — Goblet of Dionysus. Drink (never empties): a random blessing, or a rare hangover. */
public class GobletItem extends Item implements GodItem {
	public GobletItem(Properties properties) {
		super(properties);
	}

	private static OlympusConfig.Goblet cfg() {
		return ConfigManager.get().goblet;
	}

	@Override
	public God god() {
		return God.DIONYSUS;
	}

	@Override
	public boolean isStarterRelic() {
		return true;
	}

	@Override
	public boolean isCharm() {
		return true;
	}

	@Override
	public int primaryCooldownSeconds() {
		return cfg().cooldownSeconds;
	}

	@Override
	public int blessedCooldownSeconds() {
		return cfg().wildPartyCooldownSeconds;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer sp && !Cooldowns.checkReady(sp, stack, Cooldowns.Kind.PRIMARY)) {
			return InteractionResult.FAIL;
		}
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.DRINK;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return 32;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (entity instanceof ServerPlayer player && level instanceof ServerLevel serverLevel
			&& Cooldowns.isReady(player, stack, Cooldowns.Kind.PRIMARY)) {
			UpgradeLevel upgrade = GodItem.level(stack);
			var random = serverLevel.getRandom();
			Fx.sound(player, ModSounds.GOBLET_DRINK, 1.0f, 0.9f + random.nextFloat() * 0.2f);
			if (random.nextDouble() < cfg().nauseaChance) {
				player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, cfg().nauseaSeconds * 20, 0));
				Fx.sound(player, ModSounds.GOBLET_HIC, 1.0f, 1.3f);
				player.displayClientMessage(Component.translatable("olympus.message.goblet_hangover").withStyle(ChatFormatting.DARK_PURPLE), true);
			} else {
				Holder<MobEffect> effect = randomEffect(random);
				player.addEffect(new MobEffectInstance(effect, upgrade.durationTicks(cfg().effectSeconds), cfg().effectAmplifier, false, true, true));
				player.displayClientMessage(Component.translatable("olympus.message.goblet_blessing", effect.value().getDisplayName()).withStyle(God.DIONYSUS.style()), true);
			}
			Fx.burst(serverLevel, ModParticles.WINE_DROP, player.getEyePosition(), 25, 0.35, 0.05);
			Fx.helix(serverLevel, ModParticles.WINE_DROP, player.position(), 0.7, 2.0, 24);
			Cooldowns.start(player, stack, Cooldowns.Kind.PRIMARY, primaryCooldownSeconds());
		}
		return stack; // the Goblet never empties
	}

	public static Holder<MobEffect> randomEffect(net.minecraft.util.RandomSource random) {
		List<Holder<MobEffect>> pool = new ArrayList<>();
		for (String id : cfg().effectPool) {
			Identifier parsed = Identifier.tryParse(id);
			if (parsed == null) {
				continue;
			}
			BuiltInRegistries.MOB_EFFECT.get(parsed).ifPresentOrElse(pool::add,
				() -> Olympus.LOGGER.warn("Goblet effect pool: unknown effect '{}'", id));
		}
		if (pool.isEmpty()) {
			return MobEffects.REGENERATION;
		}
		return pool.get(random.nextInt(pool.size()));
	}
}
