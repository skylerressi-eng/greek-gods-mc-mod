package com.olympus.item;

import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import com.olympus.fx.Fx;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import java.util.ArrayList;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Ambrosia and Nectar, food and drink of the gods. */
public final class ConsumableItems {
	private ConsumableItems() {
	}

	private static OlympusConfig.Consumables cfg() {
		return ConfigManager.get().consumables;
	}

	/** Ambrosia: heals 6 hearts, Regeneration II and Absorption II. Has a use cooldown so it can't be spammed in PvP. */
	public static class Ambrosia extends Item {
		public Ambrosia(Properties properties) {
			super(properties);
		}

		@Override
		public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
			ItemStack result = super.finishUsingItem(stack, level, entity);
			if (level instanceof ServerLevel server) {
				OlympusConfig.Consumables c = cfg();
				// Food of the gods: full heal, harmful effects gone, then a god-tier buff set.
				entity.setHealth(entity.getMaxHealth());
				for (var effect : new ArrayList<>(entity.getActiveEffects())) {
					if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
						entity.removeEffect(effect.getEffect());
					}
				}
				entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, c.ambrosiaRegenSeconds * 20, c.ambrosiaRegenAmplifier));
				entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, c.ambrosiaAbsorptionSeconds * 20, c.ambrosiaAbsorptionAmplifier));
				entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, c.ambrosiaResistanceSeconds * 20, 1));
				entity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, c.ambrosiaStrengthSeconds * 20, 0));
				entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, c.ambrosiaFireResistanceSeconds * 20, 0));
				if (entity instanceof Player p) {
					p.getFoodData().setFoodLevel(20);
					p.getFoodData().setSaturation(20.0f);
				}
				Fx.helix(server, ModParticles.DIVINE_GLINT, entity.position(), 0.8, 2.4, 50);
				Fx.burst(server, ModParticles.DIVINE_GLINT, entity.position().add(0, 1.2, 0), 25, 0.4, 0.05);
				Fx.sound(entity, ModSounds.LYRE_SHIMMER, 0.8f, 1.4f);
				if (entity instanceof Player player) {
					player.getCooldowns().addCooldown(stack, cfg().ambrosiaCooldownSeconds * 20);
				}
			}
			return result;
		}
	}

	/** Nectar: removes every harmful effect and grants Resistance I. */
	public static class Nectar extends Item {
		public Nectar(Properties properties) {
			super(properties);
		}

		@Override
		public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
			if (level instanceof ServerLevel server) {
				for (var effect : new ArrayList<>(entity.getActiveEffects())) {
					Holder<MobEffect> type = effect.getEffect();
					if (type.value().getCategory() == MobEffectCategory.HARMFUL) {
						entity.removeEffect(type);
					}
				}
				entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, cfg().nectarResistanceSeconds * 20, 0));
				Fx.burst(server, ModParticles.SUN_MOTE, entity.position().add(0, 1.2, 0), 20, 0.4, 0.05);
			}
			ItemStack result = super.finishUsingItem(stack, level, entity);
			if (entity instanceof Player player && !player.hasInfiniteMaterials()) {
				if (result.isEmpty()) {
					return new ItemStack(Items.GLASS_BOTTLE);
				}
				player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
			}
			return result;
		}
	}
}
