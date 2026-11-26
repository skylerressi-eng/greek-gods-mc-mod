package com.olympus.claim;

import com.olympus.config.ConfigManager;
import com.olympus.data.PlayerOlympusData;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** "You have been claimed by Zeus!" — first-join god assignment and starter relic. */
public final class ClaimManager {
	private ClaimManager() {
	}

	public static Item starterRelic(God god) {
		int index = java.util.Arrays.asList(God.OLYMPIANS).indexOf(god);
		return index < 0 ? ModItems.AEGIS : ModItems.STARTER_RELICS.get(index);
	}

	public static God godOfRelic(Item item) {
		int index = ModItems.STARTER_RELICS.indexOf(item);
		return index < 0 ? null : God.OLYMPIANS[index];
	}

	/** On join, and every few seconds for players still waiting out {@code claimDelayMinutes}. */
	public static void tryAutoClaim(ServerPlayer player) {
		if (PlayerOlympusData.get(player).claimed()) {
			return;
		}
		int delayMinutes = ConfigManager.get().general.claimDelayMinutes;
		if (delayMinutes > 0 && player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) < delayMinutes * 60 * 20) {
			return;
		}
		God god = God.OLYMPIANS[player.getRandom().nextInt(God.OLYMPIANS.length)];
		claim(player, god, true);
	}

	/** Claims the player for {@code god}, announces it and (optionally) gives the starter relic. */
	public static void claim(ServerPlayer player, God god, boolean giveRelic) {
		PlayerOlympusData.set(player, PlayerOlympusData.get(player).withClaim(god));
		announce(player, god);
		if (giveRelic) {
			give(player, new ItemStack(starterRelic(god)));
		}
	}

	public static void announce(ServerPlayer player, God god) {
		player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 25));
		player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("olympus.claim.title", god.displayName()).withStyle(god.style())));
		player.connection.send(new ClientboundSetSubtitleTextPacket(god.epithet().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
		player.sendSystemMessage(Component.translatable("olympus.claim.chat", god.displayName(), Component.translatable(starterRelic(god).getDescriptionId()).withStyle(god.style()))
			.withStyle(ChatFormatting.GOLD));

		ServerLevel level = player.level();
		Fx.sound(player, ModSounds.CLAIM, 1.0f, 1.0f);
		Fx.sound(player, ModSounds.CLAIM_CHIME, 1.0f, 1.0f);
		Fx.pillar(level, god.particle(), player.position().add(0, 2.4, 0), 10.0, 50); // starts above the head
		Fx.helix(level, ModParticles.DIVINE_GLINT, player.position(), 1.0, 2.6, 60);
		Fx.shockwave(level, god.particle(), player.position().add(0, 0.2, 0), 3.0);

		MinecraftServer server = level.getServer();
		if (ConfigManager.get().general.announceClaims) {
			Component broadcast = Component.translatable("olympus.claim.broadcast", player.getDisplayName(), god.displayName()).withStyle(ChatFormatting.GRAY);
			for (ServerPlayer other : server.getPlayerList().getPlayers()) {
				if (other != player) {
					other.sendSystemMessage(broadcast);
				}
			}
		}
	}

	/** Puts the stack in the inventory, or drops it at the player's feet if there is no room. */
	public static void give(ServerPlayer player, ItemStack stack) {
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
	}
}
