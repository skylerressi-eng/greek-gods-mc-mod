package com.olympus.registry;

import com.olympus.Olympus;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

/**
 * Every Olympus sound has its own id so resource packs can replace it. By default each one
 * points at a vanilla sound event (see {@code assets/olympus/sounds.json}).
 */
public final class ModSounds {
	public static final Map<String, SoundEvent> ALL = new LinkedHashMap<>();

	public static final SoundEvent CLAIM = register("event.claim");
	public static final SoundEvent CLAIM_CHIME = register("event.claim_chime");
	public static final SoundEvent COOLDOWN_DENY = register("ability.deny");
	public static final SoundEvent AEGIS_SMITE = register("ability.aegis.smite");
	public static final SoundEvent CONCH_BLOW = register("ability.conch.blow");
	public static final SoundEvent HELM_VANISH = register("ability.helm.vanish");
	public static final SoundEvent HELM_REAPPEAR = register("ability.helm.reappear");
	public static final SoundEvent HELM_CHARGE = register("ability.helm.charge");
	public static final SoundEvent OWL_HOOT = register("ability.owl.reveal");
	public static final SoundEvent ARES_FURY = register("ability.ares.fury");
	public static final SoundEvent ARES_FURY_II = register("ability.ares.fury_ii");
	public static final SoundEvent LYRE_CHORD = register("ability.lyre.chord");
	public static final SoundEvent LYRE_SHIMMER = register("ability.lyre.shimmer");
	public static final SoundEvent SILVER_SHOT = register("ability.silver_bow.shot");
	public static final SoundEvent SILVER_MARK = register("ability.silver_bow.mark");
	public static final SoundEvent TALARIA_JUMP = register("ability.talaria.jump");
	public static final SoundEvent HEPHAESTUS_SMELT = register("ability.hephaestus.smelt");
	public static final SoundEvent SICKLE_BLOOM = register("ability.sickle.bloom");
	public static final SoundEvent GOBLET_DRINK = register("ability.goblet.drink");
	public static final SoundEvent GOBLET_HIC = register("ability.goblet.hic");
	public static final SoundEvent MIRROR_CHARM = register("ability.mirror.charm");

	private ModSounds() {
	}

	private static SoundEvent register(String name) {
		SoundEvent event = Registry.register(BuiltInRegistries.SOUND_EVENT, Olympus.id(name), SoundEvent.createVariableRangeEvent(Olympus.id(name)));
		ALL.put(name, event);
		return event;
	}

	public static Holder<SoundEvent> holder(SoundEvent event) {
		return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event);
	}

	public static void init() {
	}
}
