package com.olympus.trade;

import com.olympus.fx.Fx;
import com.olympus.god.GodItem;
import com.olympus.limit.GodItemLimit;
import com.olympus.registry.ModComponents;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Safe player-to-player trades of legendaries (and any other god item).
 * The seller holds the relic and offers it to a player, for free or for a price; the buyer gets
 * clickable [Accept] / [Decline] buttons. Everything is checked again at the moment of accepting
 * and the swap happens in one step on the server, so nobody can be scammed or dupe anything.
 */
public final class TradeManager {
	public static final int OFFER_SECONDS = 60;

	/** A pending offer. {@code legendary} identifies the exact legendary; otherwise item + level. */
	public record Offer(UUID seller, UUID buyer, Item item, int level, UUID legendary, Item price, int priceCount, long expiresAt) {
	}

	/** buyer → offer (one incoming offer at a time). */
	private static final Map<UUID, Offer> OFFERS = new ConcurrentHashMap<>();

	private TradeManager() {
	}

	public static Map<UUID, Offer> offers() {
		return OFFERS;
	}

	private static Component err(String key, Object... args) {
		return Component.translatable(key, args).withStyle(ChatFormatting.RED);
	}

	/** @return null on success, otherwise why the offer can't be made. */
	public static Component offer(ServerPlayer seller, ServerPlayer buyer, Item price, int priceCount) {
		if (seller == buyer) {
			return err("olympus.trade.self");
		}
		ItemStack held = seller.getMainHandItem();
		if (!GodItem.is(held)) {
			return err("olympus.trade.hold_relic");
		}
		if (price != null && (priceCount < 1 || priceCount > 64 * 36)) {
			return err("olympus.trade.bad_price");
		}
		if (price != null && price instanceof GodItem && priceCount != 1) {
			return err("olympus.trade.bad_price");
		}
		ModComponents.Legendary tag = held.get(ModComponents.LEGENDARY);
		long now = seller.level().getServer().overworld().getGameTime();
		// Replace any older offer this seller made.
		OFFERS.values().removeIf(o -> o.seller().equals(seller.getUUID()));
		Offer offer = new Offer(seller.getUUID(), buyer.getUUID(), held.getItem(), held.getOrDefault(ModComponents.UPGRADE_LEVEL, 0),
			tag == null ? null : tag.instance(), price, price == null ? 0 : priceCount, now + OFFER_SECONDS * 20L);
		OFFERS.put(buyer.getUUID(), offer);

		Component relic = held.getDisplayName();
		Component cost = price == null ? Component.translatable("olympus.trade.free").withStyle(ChatFormatting.GREEN)
			: Component.literal(priceCount + "x ").append(new ItemStack(price).getHoverName()).withStyle(ChatFormatting.AQUA);
		MutableComponent accept = Component.translatable("olympus.trade.button_accept").withStyle(s -> s.withColor(ChatFormatting.GREEN).withBold(true)
			.withClickEvent(new ClickEvent.RunCommand("/trade accept"))
			.withHoverEvent(new HoverEvent.ShowText(Component.translatable("olympus.trade.hover_accept"))));
		MutableComponent decline = Component.translatable("olympus.trade.button_decline").withStyle(s -> s.withColor(ChatFormatting.RED).withBold(true)
			.withClickEvent(new ClickEvent.RunCommand("/trade decline")));
		buyer.sendSystemMessage(Component.translatable("olympus.trade.incoming", seller.getDisplayName(), relic, cost, OFFER_SECONDS).withStyle(ChatFormatting.GOLD));
		buyer.sendSystemMessage(Component.literal("   ").append(accept).append("   ").append(decline));
		Fx.sound(buyer, ModSounds.CLAIM_CHIME, 0.8f, 1.4f);
		seller.sendSystemMessage(Component.translatable("olympus.trade.sent", relic, buyer.getDisplayName(), cost).withStyle(ChatFormatting.GRAY));
		return null;
	}

	/** Finds the offered relic in the seller's inventory (or -1 if it's gone / changed). */
	private static int findRelic(ServerPlayer seller, Offer offer) {
		Inventory inv = seller.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (!s.is(offer.item())) {
				continue;
			}
			ModComponents.Legendary tag = s.get(ModComponents.LEGENDARY);
			if (offer.legendary() != null ? tag != null && tag.instance().equals(offer.legendary())
				: tag == null && s.getOrDefault(ModComponents.UPGRADE_LEVEL, 0) == offer.level()) {
				return i;
			}
		}
		return -1;
	}

	private static int countPrice(ServerPlayer buyer, Item price) {
		int n = 0;
		Inventory inv = buyer.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (inv.getItem(i).is(price)) {
				n += inv.getItem(i).getCount();
			}
		}
		return n;
	}

	/** The buyer accepts. @return null on success, otherwise why it failed. */
	public static Component accept(ServerPlayer buyer) {
		Offer offer = OFFERS.remove(buyer.getUUID());
		MinecraftServer server = buyer.level().getServer();
		if (offer == null || server.overworld().getGameTime() > offer.expiresAt()) {
			return err("olympus.trade.none");
		}
		ServerPlayer seller = server.getPlayerList().getPlayer(offer.seller());
		if (seller == null || !seller.isAlive() || !buyer.isAlive()) {
			return err("olympus.trade.seller_gone");
		}
		int slot = findRelic(seller, offer);
		if (slot < 0) {
			seller.sendSystemMessage(err("olympus.trade.failed_seller", buyer.getDisplayName()));
			return err("olympus.trade.relic_gone");
		}
		boolean payWithRelic = offer.price() instanceof GodItem;
		// The buyer gains a relic (and loses one if paying with a relic).
		if (!payWithRelic && !GodItemLimit.canTakeAnother(buyer)) {
			return err("olympus.trade.buyer_full", GodItemLimit.max(buyer));
		}
		if (offer.price() != null && countPrice(buyer, offer.price()) < offer.priceCount()) {
			return err("olympus.trade.cant_afford", offer.priceCount(), new ItemStack(offer.price()).getHoverName());
		}

		// All checks passed: swap in one step.
		ItemStack relic = seller.getInventory().removeItemNoUpdate(slot);
		if (offer.price() != null) {
			int left = offer.priceCount();
			Inventory inv = buyer.getInventory();
			for (int i = 0; i < inv.getContainerSize() && left > 0; i++) {
				ItemStack s = inv.getItem(i);
				if (s.is(offer.price())) {
					int take = Math.min(left, s.getCount());
					ItemStack paid = s.split(take);
					left -= take;
					if (!seller.getInventory().add(paid)) {
						seller.drop(paid, false);
					}
				}
			}
		}
		if (!buyer.getInventory().add(relic)) {
			buyer.drop(relic, false);
		}
		seller.getInventory().setChanged();
		buyer.getInventory().setChanged();
		seller.containerMenu.broadcastChanges();
		buyer.containerMenu.broadcastChanges();

		// Legendary registry: remember the new holder right away.
		ModComponents.Legendary tag = relic.get(ModComponents.LEGENDARY);
		if (tag != null) {
			String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(relic.getItem()).getPath();
			com.olympus.data.LegendaryRegistry.seen(server, id, buyer.getGameProfile().name());
		}

		Component name = relic.getDisplayName();
		if (ModItems.LEGENDARIES.contains(relic.getItem())) {
			server.getPlayerList().broadcastSystemMessage(Component.translatable("olympus.trade.broadcast", seller.getDisplayName(), name, buyer.getDisplayName())
				.withStyle(ChatFormatting.GOLD), false);
		} else {
			seller.sendSystemMessage(Component.translatable("olympus.trade.done", name, buyer.getDisplayName()).withStyle(ChatFormatting.GREEN));
			buyer.sendSystemMessage(Component.translatable("olympus.trade.done", name, buyer.getDisplayName()).withStyle(ChatFormatting.GREEN));
		}
		for (ServerPlayer p : new ServerPlayer[]{seller, buyer}) {
			Fx.helix(p.level(), ModParticles.DIVINE_GLINT, p.position(), 0.8, 2.2, 40);
			Fx.sound(p, net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 0.8f, 1.3f);
		}
		return null;
	}

	public static Component decline(ServerPlayer buyer) {
		Offer offer = OFFERS.remove(buyer.getUUID());
		if (offer == null) {
			return err("olympus.trade.none");
		}
		ServerPlayer seller = buyer.level().getServer().getPlayerList().getPlayer(offer.seller());
		if (seller != null) {
			seller.sendSystemMessage(err("olympus.trade.declined", buyer.getDisplayName()));
		}
		buyer.sendSystemMessage(Component.translatable("olympus.trade.you_declined").withStyle(ChatFormatting.GRAY));
		return null;
	}

	public static Component cancel(ServerPlayer seller) {
		var it = OFFERS.entrySet().iterator();
		while (it.hasNext()) {
			var e = it.next();
			if (e.getValue().seller().equals(seller.getUUID())) {
				it.remove();
				ServerPlayer buyer = seller.level().getServer().getPlayerList().getPlayer(e.getKey());
				if (buyer != null) {
					buyer.sendSystemMessage(Component.translatable("olympus.trade.cancelled", seller.getDisplayName()).withStyle(ChatFormatting.GRAY));
				}
				seller.sendSystemMessage(Component.translatable("olympus.trade.you_cancelled").withStyle(ChatFormatting.GRAY));
				return null;
			}
		}
		return err("olympus.trade.none_out");
	}

	/** Once a second: expire old offers. */
	public static void tick(MinecraftServer server) {
		long now = server.overworld().getGameTime();
		var it = OFFERS.entrySet().iterator();
		while (it.hasNext()) {
			var e = it.next();
			if (now > e.getValue().expiresAt()) {
				it.remove();
				ServerPlayer seller = server.getPlayerList().getPlayer(e.getValue().seller());
				if (seller != null) {
					seller.sendSystemMessage(Component.translatable("olympus.trade.expired").withStyle(ChatFormatting.GRAY));
				}
			}
		}
	}

	public static void forget(UUID player) {
		OFFERS.remove(player);
		OFFERS.values().removeIf(o -> Objects.equals(o.seller(), player));
	}
}
