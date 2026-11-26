package com.olympus.item.relic;

import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.god.UpgradeLevel;
import com.olympus.item.CharmItem;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import com.olympus.util.Targets;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Athena — Owl of Athena. Right-click: everything nearby glows. Also reveals Helm of Darkness users. */
public class OwlItem extends CharmItem {
	public OwlItem(Properties properties) {
		super(properties, God.ATHENA, true);
	}

	private static OlympusConfig.Owl cfg() {
		return ConfigManager.get().owl;
	}

	@Override
	public int primaryCooldownSeconds() {
		return cfg().cooldownSeconds;
	}

	@Override
	public int blessedCooldownSeconds() {
		return cfg().battleWisdomCooldownSeconds;
	}

	@Override
	protected boolean activate(ServerLevel level, ServerPlayer player, ItemStack stack, UpgradeLevel upgrade) {
		double radius = upgrade.radius(cfg().radius);
		int ticks = upgrade.durationTicks(cfg().glowSeconds);
		// Revealing players is a "harmful" effect in PvP terms (it gives away positions).
		List<LivingEntity> targets = Targets.around(player, radius, e -> !(e instanceof Player) || Targets.canHarm(player, e));
		for (LivingEntity target : targets) {
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, false, true), player);
			com.olympus.handler.CombatHandler.markByOwl(target, player, ticks);
			level.sendParticles(ModParticles.OWL_FEATHER, target.getX(), target.getEyeY() + 0.4, target.getZ(), 3, 0.2, 0.1, 0.2, 0.01);
		}

		Fx.sound(player, ModSounds.OWL_HOOT, 1.4f, 1.0f);
		Fx.sphere(level, ModParticles.OWL_FEATHER, player.position().add(0, 1, 0), 3.0, 80);
		Fx.ring(level, ModParticles.OWL_FEATHER, player.position().add(0, 0.3, 0), 1.0, 32, 0.6);
		player.displayClientMessage(Component.translatable("olympus.message.owl_reveal", targets.size()).withStyle(God.ATHENA.style()).withStyle(ChatFormatting.ITALIC), true);
		return true;
	}
}
