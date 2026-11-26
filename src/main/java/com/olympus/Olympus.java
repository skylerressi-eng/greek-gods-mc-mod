package com.olympus;

import com.olympus.ability.Cooldowns;
import com.olympus.claim.ClaimManager;
import com.olympus.command.OlympusCommand;
import com.olympus.config.ConfigManager;
import com.olympus.data.PlayerOlympusData;
import com.olympus.god.GodItem;
import com.olympus.handler.AresHandler;
import com.olympus.handler.HelmHandler;
import com.olympus.handler.MirrorHandler;
import com.olympus.handler.SilverBowHandler;
import com.olympus.handler.TalariaHandler;
import com.olympus.network.OlympusNetwork;
import com.olympus.registry.ModBlocks;
import com.olympus.registry.ModComponents;
import com.olympus.registry.ModCreativeTab;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModLoot;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Olympus implements ModInitializer {
	public static final String MOD_ID = "olympus";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Config first: item stats (starterTier, Talaria speed) are read during registration.
		ConfigManager.load();

		ModComponents.init();
		ModParticles.init();
		ModSounds.init();
		ModBlocks.init();
		ModItems.init();
		PlayerOlympusData.init();
		ModCreativeTab.register();
		ModLoot.register();
		OlympusNetwork.register();

		AresHandler.register();
		SilverBowHandler.register();
		com.olympus.handler.CombatHandler.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> OlympusCommand.register(dispatcher));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.getPlayer();
			OlympusNetwork.syncConfig(player);
			ClaimManager.tryAutoClaim(player);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayer player = handler.getPlayer();
			HelmHandler.forget(player);
			TalariaHandler.forget(player.getUUID());
			AresHandler.forget(player.getUUID());
			com.olympus.handler.CombatHandler.forget(player.getUUID());
		});
		EntityTrackingEvents.START_TRACKING.register(HelmHandler::onStartTracking);

		ServerTickEvents.END_SERVER_TICK.register(Olympus::tick);

		LOGGER.info("The gods of Olympus awaken.");
	}

	private static void tick(MinecraftServer server) {
		HelmHandler.tickServer(server);
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			TalariaHandler.tickPlayer(player);
		}
		if (server.getTickCount() % 20 == 0) {
			MirrorHandler.tick(server);
			com.olympus.handler.CombatHandler.tick(server);
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				syncCooldownOverlays(player);
				ClaimManager.tryAutoClaim(player);
			}
		}
	}

	/** Keeps the hotbar cooldown overlay in sync with stored cooldowns (relogs, stolen relics). */
	private static void syncCooldownOverlays(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (GodItem.is(stack)) {
				Cooldowns.syncOverlay(player, stack);
			}
		}
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
