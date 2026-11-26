package com.olympus.client;

import com.olympus.client.input.AirJumpInput;
import com.olympus.client.particle.DivineParticle;
import com.olympus.client.particle.DivineParticle.Style;
import com.olympus.config.ConfigManager;
import com.olympus.god.GodTooltip;
import com.olympus.network.OlympusNetwork;
import com.olympus.registry.ModParticles;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.core.particles.SimpleParticleType;

public class OlympusClient implements ClientModInitializer {
	private static final Map<String, Style> PARTICLE_STYLES = Map.ofEntries(
		Map.entry("divine_glint", Style.MOTE),
		Map.entry("storm_spark", Style.CRACKLE),
		Map.entry("tide_drop", Style.DROP),
		Map.entry("shade", Style.SMOKE),
		Map.entry("owl_feather", Style.FLUTTER),
		Map.entry("war_ember", Style.SPARK),
		Map.entry("sun_mote", Style.MOTE),
		Map.entry("moonbeam", Style.MOTE),
		Map.entry("wind_wisp", Style.WISP),
		Map.entry("forge_spark", Style.SPARK),
		Map.entry("grain_leaf", Style.FLUTTER),
		Map.entry("wine_drop", Style.DROP),
		Map.entry("rose_petal", Style.FLUTTER),
		Map.entry("titan_dust", Style.SMOKE));

	@Override
	public void onInitializeClient() {
		ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
		for (Map.Entry<String, SimpleParticleType> entry : ModParticles.ALL.entrySet()) {
			Style style = PARTICLE_STYLES.getOrDefault(entry.getKey(), Style.MOTE);
			registry.register(entry.getValue(), sprites -> new DivineParticle.Provider(sprites, style));
		}

		ClientPlayNetworking.registerGlobalReceiver(OlympusNetwork.ConfigSyncPayload.TYPE,
			(payload, context) -> ConfigManager.setClientView(payload.json(), Thread.currentThread()));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ConfigManager.clearClientView());
		ClientTickEvents.END_CLIENT_TICK.register(AirJumpInput::tick);

		// God-item tooltips are inserted right under the item name.
		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> GodTooltip.append(stack, lines));
	}
}
