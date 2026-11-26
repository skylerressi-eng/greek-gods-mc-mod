package com.olympus.event;

import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import com.olympus.data.LegendaryRegistry;
import com.olympus.entity.BossCore;
import com.olympus.fx.Fx;
import com.olympus.god.GodItem;
import com.olympus.limit.GodItemLimit;
import com.olympus.registry.ModComponents;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Runs divine events: one at a time, started by an admin (or a thunderstorm for Wrath of Zeus).
 * Also guards legendary uniqueness. Active events are not saved: a server restart ends them cleanly.
 */
public final class EventManager {
	private record Active(OlympusEvent event, UUID boss, ServerLevel level, long startedAt) {
	}

	private record Pending(OlympusEvent event, UUID player, long at) {
	}

	private static Active active;
	private static Pending pendingWrath;
	private static boolean wasThundering;

	private EventManager() {
	}

	public static Optional<OlympusEvent> current() {
		return active == null ? Optional.empty() : Optional.of(active.event());
	}

	public static boolean isActiveBoss(Entity entity) {
		return active != null && active.boss().equals(entity.getUUID());
	}

	/** Starts an event at a position. @return an error message, or empty on success. */
	public static Optional<Component> start(OlympusEvent event, ServerLevel level, BlockPos pos) {
		if (active != null) {
			return Optional.of(Component.translatable("olympus.event.already_running", active.event().displayName()));
		}
		Optional<Component> where = event.checkLocation(level, pos);
		if (where.isPresent()) {
			return where;
		}
		Mob boss = event.bossType().create(level, EntitySpawnReason.EVENT);
		if (boss == null) {
			return Optional.of(Component.literal("Could not create the boss"));
		}
		OlympusConfig.Events cfg = ConfigManager.get().events;
		int players = Math.max(1, level.getEntitiesOfClass(ServerPlayer.class, boss.getBoundingBox().move(pos.getX(), pos.getY(), pos.getZ()).inflate(64)).size());
		float health = (float) (event.baseHealth() * (1.0 + cfg.healthPerExtraPlayer * (players - 1)));
		var maxHealth = boss.getAttribute(Attributes.MAX_HEALTH);
		if (maxHealth != null) {
			maxHealth.setBaseValue(health);
		}
		boss.setHealth(health);
		boss.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
		// Never spawn inside a wall: find room a little higher, else use the requested spot's surface.
		for (int up = 0; up < 8 && !level.noCollision(boss); up++) {
			boss.snapTo(pos.getX() + 0.5, pos.getY() + up + 1, pos.getZ() + 0.5, 0, 0);
		}
		boss.setCustomName(Component.translatable("entity.olympus." + BuiltInRegistries.ENTITY_TYPE.getKey(event.bossType()).getPath()).withStyle(event.god.style()).withStyle(ChatFormatting.BOLD));
		boss.setCustomNameVisible(true);
		level.addFreshEntity(boss);
		active = new Active(event, boss.getUUID(), level, level.getGameTime());

		Fx.pillar(level, ModParticles.DIVINE_GLINT, boss.position(), 30, 80);
		Fx.shockwave(level, event.god.particle(), boss.position().add(0, 0.2, 0), 8);
		broadcastTitle(level.getServer(), Component.translatable("olympus.event.started.title", event.displayName()),
			Component.translatable("olympus.event." + event.id() + ".subtitle").withStyle(ChatFormatting.GRAY));
		level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("olympus.event.started.chat", event.displayName(),
			boss.getDisplayName(), pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GOLD), false);
		for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
			p.level().playSound(null, p.getX(), p.getY(), p.getZ(), ModSounds.CLAIM, net.minecraft.sounds.SoundSource.MASTER, 0.6f, 0.6f);
		}
		return Optional.empty();
	}

	public static boolean stop(MinecraftServer server, Component reason) {
		if (active == null) {
			return false;
		}
		Entity boss = active.level().getEntity(active.boss());
		if (boss != null) {
			Fx.burst(active.level(), ModParticles.TITAN_DUST, boss.position().add(0, 1, 0), 60, 1, 0.1);
			clearMinions(active.level(), boss);
			boss.discard();
		}
		server.getPlayerList().broadcastSystemMessage(reason.copy().withStyle(ChatFormatting.GRAY), false);
		active = null;
		return true;
	}

	private static void clearMinions(ServerLevel level, Entity boss) {
		for (Entity e : level.getEntities((Entity) null, boss.getBoundingBox().inflate(64), e -> e.getTags().contains(BossCore.MINION_TAG))) {
			e.discard();
		}
	}

	private static void broadcastTitle(MinecraftServer server, Component title, Component subtitle) {
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 25));
			p.connection.send(new ClientboundSetTitleTextPacket(title));
			p.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
		}
	}

	/** Boss died: hand out rewards by damage share. */
	public static void onBossDeath(Mob boss, BossCore core) {
		if (active == null || !active.boss().equals(boss.getUUID())) {
			return;
		}
		OlympusEvent event = active.event();
		ServerLevel level = (ServerLevel) boss.level();
		MinecraftServer server = level.getServer();
		active = null;
		clearMinions(level, boss);
		OlympusConfig.Events cfg = ConfigManager.get().events;

		Map<UUID, Float> dealt = core.damageDealt();
		float total = (float) dealt.values().stream().mapToDouble(Float::doubleValue).sum();
		List<Map.Entry<UUID, Float>> ranked = new ArrayList<>(dealt.entrySet());
		ranked.sort(Map.Entry.<UUID, Float>comparingByValue(Comparator.reverseOrder()));

		broadcastTitle(server, Component.translatable("olympus.event.won.title", event.displayName()), Component.empty());
		if (ranked.isEmpty() || total <= 0) {
			return;
		}
		ServerPlayer top = findPlayer(level, ranked.get(0).getKey());
		if (top != null) {
			String id = BuiltInRegistries.ITEM.getKey(event.legendary()).getPath();
			if (LegendaryRegistry.get(server, id).isEmpty()) {
				ItemStack legendary = new ItemStack(event.legendary());
				UUID instance = UUID.randomUUID();
				legendary.set(ModComponents.LEGENDARY, new ModComponents.Legendary(instance, top.getGameProfile().name()));
				LegendaryRegistry.register(server, id, new LegendaryRegistry.Entry(instance, top.getGameProfile().name(), top.getGameProfile().name()));
				giveOrDrop(top, legendary);
				server.getPlayerList().broadcastSystemMessage(Component.translatable("olympus.event.legendary_won", top.getDisplayName(),
					legendary.getHoverName()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
				Fx.pillar(level, ModParticles.DIVINE_GLINT, top.position(), 20, 60);
			} else {
				// The legendary already exists: a generous consolation instead.
				giveOrDrop(top, new ItemStack(ModItems.OLYMPIAN_BRONZE_INGOT, 4));
				giveOrDrop(top, new ItemStack(ModItems.DIVINE_SMITHING_TEMPLATE));
				top.sendSystemMessage(Component.translatable("olympus.event.legendary_taken").withStyle(ChatFormatting.GRAY));
			}
			if (event == OlympusEvent.TITANOMACHY) {
				giveOrDrop(top, new ItemStack(ModItems.ASCENSION_CORE));
			}
		}
		for (Map.Entry<UUID, Float> entry : ranked) {
			if (entry.getValue() / total < cfg.rewardMinDamageShare) {
				continue;
			}
			ServerPlayer p = findPlayer(level, entry.getKey());
			if (p == null) {
				continue;
			}
			int ingots = cfg.rewardIngotsMin + level.getRandom().nextInt(Math.max(1, cfg.rewardIngotsMax - cfg.rewardIngotsMin + 1));
			giveOrDrop(p, new ItemStack(ModItems.OLYMPIAN_BRONZE_INGOT, ingots));
			if (level.getRandom().nextDouble() < cfg.rewardTemplateChance) {
				giveOrDrop(p, new ItemStack(ModItems.DIVINE_SMITHING_TEMPLATE));
			}
			p.sendSystemMessage(Component.translatable("olympus.event.your_share", Math.round(entry.getValue() / total * 100)).withStyle(ChatFormatting.YELLOW));
		}
	}

	private static ServerPlayer findPlayer(ServerLevel level, UUID id) {
		ServerPlayer p = level.getServer().getPlayerList().getPlayer(id);
		if (p == null && level.getPlayerByUUID(id) instanceof ServerPlayer sp) {
			p = sp;
		}
		return p;
	}

	private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
		if (GodItem.is(stack) && !GodItemLimit.canTakeAnother(player)) {
			player.drop(stack, false);
			GodItemLimit.deny(player);
			return;
		}
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
	}

	public static void tick(MinecraftServer server) {
		OlympusConfig.Events cfg = ConfigManager.get().events;
		if (active != null) {
			Entity boss = active.level().getEntity(active.boss());
			long age = active.level().getGameTime() - active.startedAt();
			if (age > cfg.eventTimeoutMinutes * 1200L) {
				stop(server, Component.translatable("olympus.event.timeout", active.event().displayName()));
			} else if (boss == null && age > 200) {
				stop(server, Component.translatable("olympus.event.vanished", active.event().displayName()));
			}
		}

		// Wrath of Zeus can start by itself when a thunderstorm begins.
		ServerLevel overworld = server.overworld();
		boolean thundering = overworld.isThundering();
		if (thundering && !wasThundering && active == null && pendingWrath == null
			&& server.getPlayerCount() >= cfg.wrathAutoMinPlayers && overworld.getRandom().nextDouble() < cfg.wrathAutoStartChance) {
			List<ServerPlayer> candidates = overworld.players().stream()
				.filter(p -> !p.isSpectator() && !p.isCreative() && overworld.canSeeSky(p.blockPosition())).toList();
			if (!candidates.isEmpty()) {
				ServerPlayer chosen = candidates.get(overworld.getRandom().nextInt(candidates.size()));
				pendingWrath = new Pending(OlympusEvent.WRATH_OF_ZEUS, chosen.getUUID(), overworld.getGameTime() + cfg.wrathWarningSeconds * 20L);
				BlockPos p = chosen.blockPosition();
				server.getPlayerList().broadcastSystemMessage(Component.translatable("olympus.event.wrath_warning", cfg.wrathWarningSeconds,
					chosen.getDisplayName(), p.getX(), p.getY(), p.getZ()).withStyle(ChatFormatting.AQUA), false);
			}
		}
		wasThundering = thundering;
		if (pendingWrath != null && overworld.getGameTime() >= pendingWrath.at()) {
			ServerPlayer target = server.getPlayerList().getPlayer(pendingWrath.player());
			pendingWrath = null;
			if (target != null && target.level() == overworld) {
				BlockPos at = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.blockPosition().offset(6, 0, 6));
				start(OlympusEvent.WRATH_OF_ZEUS, overworld, at);
			}
		}
	}

	/**
	 * Legendary guard (every second per player): binds admin-given legendaries, deletes duplicates
	 * (creative copies, dupe glitches) and remembers who holds each one.
	 */
	public static void guardLegendaries(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty() || !ModItems.LEGENDARIES.contains(stack.getItem())) {
				continue;
			}
			String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
			String name = player.getGameProfile().name();
			ModComponents.Legendary tag = stack.get(ModComponents.LEGENDARY);
			Optional<LegendaryRegistry.Entry> entry = LegendaryRegistry.get(server, id);
			if (tag == null) {
				if (player.isCreative()) {
					continue; // creative-menu copies stay unbound and never claim the real legendary
				}
				if (entry.isEmpty()) {
					UUID instance = UUID.randomUUID();
					stack.set(ModComponents.LEGENDARY, new ModComponents.Legendary(instance, name));
					LegendaryRegistry.register(server, id, new LegendaryRegistry.Entry(instance, name, name));
				} else if (!player.isCreative()) {
					inv.setItem(i, ItemStack.EMPTY);
					player.sendSystemMessage(Component.translatable("olympus.message.legendary_duplicate", stack.getHoverName()).withStyle(ChatFormatting.RED));
				}
			} else if (entry.isPresent() && !entry.get().instance().equals(tag.instance())) {
				inv.setItem(i, ItemStack.EMPTY);
				player.sendSystemMessage(Component.translatable("olympus.message.legendary_duplicate", stack.getHoverName()).withStyle(ChatFormatting.RED));
			} else if (entry.isEmpty()) {
				LegendaryRegistry.register(server, id, new LegendaryRegistry.Entry(tag.instance(), tag.firstOwner(), name));
			} else {
				LegendaryRegistry.seen(server, id, name);
			}
		}
	}

	/** A legendary item entity fell into the void: it may drop again from its event. */
	public static void onLegendaryLost(MinecraftServer server, ItemStack stack) {
		ModComponents.Legendary tag = stack.get(ModComponents.LEGENDARY);
		if (tag == null) {
			return;
		}
		String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
		LegendaryRegistry.get(server, id).filter(e -> e.instance().equals(tag.instance())).ifPresent(e -> {
			LegendaryRegistry.remove(server, id);
			server.getPlayerList().broadcastSystemMessage(Component.translatable("olympus.event.legendary_lost", stack.getHoverName()).withStyle(ChatFormatting.DARK_PURPLE), false);
		});
	}

	public static void reset() {
		active = null;
		pendingWrath = null;
		wasThundering = false;
	}

	public static boolean isLevel(Level level) {
		return active != null && active.level() == level;
	}
}
