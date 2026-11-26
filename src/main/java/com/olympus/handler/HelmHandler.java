package com.olympus.handler;

import com.mojang.datafixers.util.Pair;
import com.olympus.ability.Cooldowns;
import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import com.olympus.fx.Fx;
import com.olympus.god.GodItem;
import com.olympus.god.UpgradeLevel;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Helm of Darkness primary: sneak (standing still) for 2s while wearing the helm to vanish.
 * Unlike vanilla Invisibility, armor and held items are hidden too: the server stops sending
 * the player's equipment to other clients, so even modified clients can't see it.
 */
public final class HelmHandler {
	private static final Map<UUID, Integer> CHARGE = new ConcurrentHashMap<>();
	/** player → game time when Hades' invisibility ends. */
	private static final Map<UUID, Long> HIDDEN_UNTIL = new ConcurrentHashMap<>();
	private static final Set<UUID> HIDDEN = ConcurrentHashMap.newKeySet();
	/** Last tick's position: the server doesn't know client velocity, so stillness is measured by position. */
	private static final Map<UUID, net.minecraft.world.phys.Vec3> LAST_POS = new ConcurrentHashMap<>();

	private HelmHandler() {
	}

	public static boolean isHidden(Player player) {
		return HIDDEN.contains(player.getUUID());
	}

	public static void tickPlayer(ServerPlayer player) {
		UUID id = player.getUUID();
		long now = player.level().getGameTime();

		if (HIDDEN.contains(id)) {
			Long until = HIDDEN_UNTIL.get(id);
			boolean wearing = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HELM_OF_DARKNESS);
			if (until == null || now >= until || !wearing || !player.hasEffect(MobEffects.INVISIBILITY)) {
				reveal(player);
			} else if (now % 10 == 0) {
				// A faint shadow at the feet: visible up close if you know what to look for.
				player.level().sendParticles(ModParticles.SHADE, player.getX(), player.getY() + 0.1, player.getZ(), 1, 0.2, 0.0, 0.2, 0.0);
			}
			return;
		}

		ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
		OlympusConfig.HelmOfDarkness cfg = ConfigManager.get().helmOfDarkness;
		net.minecraft.world.phys.Vec3 pos = player.position();
		net.minecraft.world.phys.Vec3 last = LAST_POS.put(id, pos);
		double moved = last == null ? 0 : (pos.x - last.x) * (pos.x - last.x) + (pos.z - last.z) * (pos.z - last.z);
		boolean still = !cfg.requireStandingStill || moved < 9.0E-4; // < 0.03 blocks per tick
		if (!helm.is(ModItems.HELM_OF_DARKNESS) || !player.isShiftKeyDown() || !still || !player.onGround()) {
			if (CHARGE.remove(id) != null) {
				player.displayClientMessage(Component.empty(), true);
			}
			return;
		}
		if (!Cooldowns.isReady(player, helm, Cooldowns.Kind.PRIMARY)) {
			if (!CHARGE.containsKey(id)) {
				CHARGE.put(id, -1); // only nag once per sneak
				Cooldowns.checkReady(player, helm, Cooldowns.Kind.PRIMARY);
			}
			return;
		}
		int needed = Math.max(1, (int) Math.round(cfg.sneakSeconds * 20));
		int charge = Math.max(0, CHARGE.getOrDefault(id, 0)) + 1;
		CHARGE.put(id, charge);

		ServerLevel level = player.level();
		if (charge % 4 == 0) {
			double a = charge * 0.6;
			level.sendParticles(ModParticles.SHADE, player.getX() + Math.cos(a) * 0.7, player.getY() + 0.2 + charge / (double) needed, player.getZ() + Math.sin(a) * 0.7, 2, 0.05, 0.05, 0.05, 0.0);
			player.displayClientMessage(progressBar(charge, needed), true);
		}
		if (charge == 1) {
			Fx.sound(player, ModSounds.HELM_CHARGE, 0.5f, 0.6f);
		}
		if (charge >= needed) {
			CHARGE.remove(id);
			vanish(player, helm);
		}
	}

	private static Component progressBar(int charge, int needed) {
		int filled = Math.min(10, charge * 10 / needed);
		return Component.literal("▮".repeat(filled)).withStyle(ChatFormatting.DARK_PURPLE)
			.append(Component.literal("▯".repeat(10 - filled)).withStyle(ChatFormatting.DARK_GRAY));
	}

	private static void vanish(ServerPlayer player, ItemStack helm) {
		UpgradeLevel upgrade = GodItem.level(helm);
		int ticks = upgrade.durationTicks(ConfigManager.get().helmOfDarkness.invisibilitySeconds);
		player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, ticks, 0, false, false, true));
		if (ConfigManager.get().helmOfDarkness.speedWhileHidden) {
			player.addEffect(new MobEffectInstance(MobEffects.SPEED, ticks, 0, false, false, true));
		}
		HIDDEN_UNTIL.put(player.getUUID(), player.level().getGameTime() + ticks);
		HIDDEN.add(player.getUUID());
		sendEquipment(player, true);
		Cooldowns.start(player, helm, Cooldowns.Kind.PRIMARY, ConfigManager.get().helmOfDarkness.cooldownSeconds);

		ServerLevel level = player.level();
		Fx.sound(player, ModSounds.HELM_VANISH, 1.0f, 0.7f);
		Fx.burst(level, ModParticles.SHADE, player.position().add(0, 1, 0), 60, 0.4, 0.04);
		Fx.ring(level, ModParticles.SHADE, player.position().add(0, 0.1, 0), 0.3, 24, 0.25);
		player.displayClientMessage(Component.translatable("olympus.message.helm_vanish").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
	}

	/** Ends Hades' invisibility early (also used by the Blessed ambush hit). */
	public static void reveal(ServerPlayer player) {
		if (!HIDDEN.remove(player.getUUID())) {
			return;
		}
		HIDDEN_UNTIL.remove(player.getUUID());
		player.removeEffect(MobEffects.INVISIBILITY);
		sendEquipment(player, false);
		Fx.sound(player, ModSounds.HELM_REAPPEAR, 0.8f, 1.1f);
		Fx.burst(player.level(), ModParticles.SHADE, player.position().add(0, 1, 0), 30, 0.35, 0.02);
	}

	/** Tells everyone tracking {@code player} what they wear (or nothing at all while hidden). */
	public static void sendEquipment(ServerPlayer player, boolean hidden) {
		List<Pair<EquipmentSlot, ItemStack>> slots = new ArrayList<>();
		for (EquipmentSlot slot : EquipmentSlot.VALUES) {
			slots.add(Pair.of(slot, hidden ? ItemStack.EMPTY : player.getItemBySlot(slot).copy()));
		}
		player.level().getChunkSource().sendToTrackingPlayers(player, new ClientboundSetEquipmentPacket(player.getId(), slots));
	}

	/** A player who just started tracking a hidden player must not see their gear. */
	public static void onStartTracking(net.minecraft.world.entity.Entity tracked, ServerPlayer viewer) {
		if (tracked instanceof ServerPlayer player && isHidden(player)) {
			List<Pair<EquipmentSlot, ItemStack>> slots = new ArrayList<>();
			for (EquipmentSlot slot : EquipmentSlot.VALUES) {
				slots.add(Pair.of(slot, ItemStack.EMPTY));
			}
			viewer.connection.send(new ClientboundSetEquipmentPacket(player.getId(), slots));
		}
	}

	public static void forget(ServerPlayer player) {
		CHARGE.remove(player.getUUID());
		LAST_POS.remove(player.getUUID());
		if (HIDDEN.contains(player.getUUID())) {
			reveal(player);
		}
	}

	public static void tickServer(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tickPlayer(player);
		}
	}
}
