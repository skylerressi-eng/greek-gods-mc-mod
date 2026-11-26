package com.olympus.network;

import com.olympus.Olympus;
import com.olympus.config.ConfigManager;
import com.olympus.handler.TalariaHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * All Olympus packets. The client never tells the server *what* to do, only that a key was
 * pressed; the server checks everything itself.
 */
public final class OlympusNetwork {
	private OlympusNetwork() {
	}

	/** C2S: the player pressed jump while airborne. */
	public record AirJumpPayload() implements CustomPacketPayload {
		public static final Type<AirJumpPayload> TYPE = new Type<>(Olympus.id("air_jump"));
		public static final AirJumpPayload INSTANCE = new AirJumpPayload();
		public static final StreamCodec<RegistryFriendlyByteBuf, AirJumpPayload> CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** C2S: the Divine Power key was pressed. The server decides which relic answers. */
	public record DivinePowerPayload() implements CustomPacketPayload {
		public static final Type<DivinePowerPayload> TYPE = new Type<>(Olympus.id("divine_power"));
		public static final DivinePowerPayload INSTANCE = new DivinePowerPayload();
		public static final StreamCodec<RegistryFriendlyByteBuf, DivinePowerPayload> CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/**
	 * S2C: the server's config, so tooltips, HUD and movement prediction show the server's
	 * numbers rather than the client's local file. Sent on join and on /olympus reload.
	 */
	public record ConfigSyncPayload(String json) implements CustomPacketPayload {
		public static final Type<ConfigSyncPayload> TYPE = new Type<>(Olympus.id("config_sync"));
		public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.stringUtf8(262144), ConfigSyncPayload::json,
			ConfigSyncPayload::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public static void register() {
		PayloadTypeRegistry.playC2S().register(AirJumpPayload.TYPE, AirJumpPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);

		PayloadTypeRegistry.playC2S().register(DivinePowerPayload.TYPE, DivinePowerPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(AirJumpPayload.TYPE, (payload, context) -> TalariaHandler.onAirJump(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(DivinePowerPayload.TYPE, (payload, context) -> com.olympus.ability.DivinePower.onKey(context.player()));
	}

	public static void syncConfig(ServerPlayer player) {
		if (ServerPlayNetworking.canSend(player, ConfigSyncPayload.TYPE)) {
			ServerPlayNetworking.send(player, new ConfigSyncPayload(ConfigManager.toJson()));
		}
	}
}
