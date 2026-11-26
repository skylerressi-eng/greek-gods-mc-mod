package com.olympus.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Every tunable number in Relics of Olympus. Serialized to {@code config/olympus.json}.
 * <p>
 * Times are in seconds, distances in blocks, chances in 0..1, effect amplifiers are
 * 0-based like vanilla (0 = level I, 1 = level II).
 * Fields marked "restart" are read once at startup (they shape item attributes).
 */
public class OlympusConfig {
	public General general = new General();
	public Loot loot = new Loot();
	public Upgrades upgrades = new Upgrades();
	public Lightning lightning = new Lightning();

	public Aegis aegis = new Aegis();
	public Conch conch = new Conch();
	public HelmOfDarkness helmOfDarkness = new HelmOfDarkness();
	public Owl owl = new Owl();
	public BladeOfAres bladeOfAres = new BladeOfAres();
	public Lyre lyre = new Lyre();
	public SilverBow silverBow = new SilverBow();
	public Talaria talaria = new Talaria();
	public HammerOfHephaestus hammerOfHephaestus = new HammerOfHephaestus();
	public Sickle sickle = new Sickle();
	public Goblet goblet = new Goblet();
	public Mirror mirror = new Mirror();

	// Phase 3: craftables
	public Caduceus caduceus = new Caduceus();
	public SpearOfAthena spearOfAthena = new SpearOfAthena();
	public ForgeHammer forgeHammer = new ForgeHammer();
	public Harpe harpe = new Harpe();
	public Thyrsus thyrsus = new Thyrsus();
	public Cornucopia cornucopia = new Cornucopia();
	public Consumables consumables = new Consumables();
	public Hoplite hoplite = new Hoplite();

	// Phase 4: events, bosses, legendaries
	public Events events = new Events();
	public Thunderbolt thunderbolt = new Thunderbolt();
	public TridentOfPoseidon tridentOfPoseidon = new TridentOfPoseidon();
	public Bident bident = new Bident();
	public HeadOfMedusa headOfMedusa = new HeadOfMedusa();
	public GoldenFleece goldenFleece = new GoldenFleece();
	public Scythe scythe = new Scythe();

	public static class General {
		/** Maximum god items a player may carry. */
		public int maxGodItems = 3;
		/** Each Laurel of Olympus permanently adds this many relic slots... */
		public int laurelSlotsPerUse = 1;
		/** ...up to this many extra slots per player. */
		public int laurelMaxBonusSlots = 2;
		/** Stat tier of starter relics: netherite, diamond or iron. Restart required. */
		public String starterTier = "netherite";
		/** If false, abilities never apply harmful effects to other players (buffs still work). */
		public boolean abilitiesAffectPlayers = true;
		/** Minutes a new player must play before the gods claim them (anti alt-farming). 0 = on first join. */
		public int claimDelayMinutes = 0;
		/** Announce new claims to the whole server in chat. */
		public boolean announceClaims = true;
	}

	public static class Loot {
		/** Chance per Ancient City / End City / Bastion treasure chest to contain a Divine Smithing Template. */
		public double divineTemplateChance = 0.08;
	}

	public static class Upgrades {
		public double blessedCooldownMultiplier = 0.75;
		public double ascendedCooldownMultiplier = 0.5;
		public double ascendedDurationMultiplier = 1.25;
		public double ascendedRadiusMultiplier = 1.25;
	}

	public static class Lightning {
		/** Whether divine lightning sets blocks on fire. Off by default: no griefing. */
		public boolean lightningFire = false;
	}

	public static class Aegis {
		public double lightningChance = 0.30;
		public float lightningDamage = 5.0f;
		/** Every blocked melee hit pushes the attacker back (PvP spacing). */
		public double blockKnockback = 0.6;
		/** Blessed: every Nth blocked hit releases a shockwave. */
		public int shockwaveEveryBlocks = 5;
		public double shockwaveRadius = 5.0;
		public float shockwaveDamage = 5.0f;
		public double shockwaveKnockback = 1.2;
	}

	public static class Conch {
		/** Tidal Surge: a wave in front of you that knocks enemies back and slows them. */
		public double waveRange = 7.0;
		public double waveKnockback = 1.4;
		public float waveDamage = 3.0f;
		public int waveSlownessSeconds = 3;
		/** You also get Water Breathing + Dolphin's Grace for this long. */
		public int durationSeconds = 30;
		public int cooldownSeconds = 40;
		public int riptideCooldownSeconds = 20;
		public double riptideStrength = 2.5;
	}

	public static class HelmOfDarkness {
		public double sneakSeconds = 2.0;
		/** Only trigger if the player stands still while sneaking (prevents accidental casts while bridging). */
		public boolean requireStandingStill = true;
		public int invisibilitySeconds = 8;
		public int cooldownSeconds = 90;
		/** Speed I while vanished, so you can reposition. */
		public boolean speedWhileHidden = true;
		/** Blessed: damage multiplier of the first hit while invisible. */
		public double ambushDamageMultiplier = 2.0;
	}

	public static class Owl {
		public double radius = 30.0;
		public int glowSeconds = 10;
		public int cooldownSeconds = 75;
		/** Revealed enemies take this much extra damage from the Owl's owner (0.15 = +15%). */
		public double markedDamageBonus = 0.15;
		public double battleWisdomRadius = 10.0;
		public int battleWisdomSeconds = 10;
		public int battleWisdomCooldownSeconds = 120;
	}

	public static class BladeOfAres {
		/** Hits needed to build full Fury (Strength I). Stacks fade after furyDecaySeconds without a hit. */
		public int furyHits = 3;
		public int furyDecaySeconds = 5;
		public int furyStrengthSeconds = 6;
		/** Kills grant Strength II for this long. */
		public int strengthSeconds = 8;
		public double warCryRadius = 8.0;
		public int warCrySeconds = 10;
		public int warCryCooldownSeconds = 90;
	}

	public static class Lyre {
		public double radius = 8.0;
		public int regenSeconds = 5;
		public int regenAmplifier = 1;
		public int cooldownSeconds = 50;
		/** Allies also get Speed I and are cleansed of Poison and Wither. */
		public boolean cleanse = true;
		public double sunburstRadius = 8.0;
		public int sunburstFireSeconds = 5;
		public int sunburstBlindSeconds = 3;
		public int sunburstCooldownSeconds = 90;
	}

	public static class SilverBow {
		/** Silver arrows deal this much extra damage (0.15 = +15%). */
		public double arrowDamageBonus = 0.15;
		public int glowSeconds = 5;
		/** Arrows fly without gravity for this long, then fall normally (prevents arrows flying forever). */
		public int straightFlightSeconds = 5;
		public int volleyArrows = 5;
		public int volleyWindowSeconds = 10;
		public int volleyCooldownSeconds = 30;
	}

	public static class Talaria {
		/** Movement speed bonus while worn (0.2 = Speed I). Restart required. */
		public double speedBonus = 0.2;
		public double airJumpVelocity = 0.65;
		/** Extra jumps in the air at Mortal level (Blessed gets one more). */
		public int airJumps = 1;
	}

	public static class HammerOfHephaestus {
		/** Grant the smelting XP for auto-smelted ores. */
		public boolean grantSmeltingXp = true;
		/** Melee hits set the target on fire (like Fire Aspect). */
		public int igniteSeconds = 4;
		/** Fire Resistance while the hammer is in your main hand. */
		public boolean fireResistanceWhileHeld = true;
		public int forgeFireMaxItems = 16;
		public int forgeFireCooldownSeconds = 180;
	}

	public static class Sickle {
		/** Famine: melee hits give Hunger and Slowness, and drain the target player's food. */
		public int famineHungerSeconds = 5;
		public int famineSlownessSeconds = 2;
		public int famineFoodDrain = 2;
		/** Radius around the target: 2 = 5x5. */
		public int growRadius = 2;
		public int cooldownSeconds = 30;
		public int harvestRadius = 4;
		public int harvestCooldownSeconds = 60;
	}

	public static class Goblet {
		public int effectSeconds = 30;
		/** Effect level (0 = I, 1 = II). Combat effects at II make the Goblet a real fight drink. */
		public int effectAmplifier = 1;
		public double nauseaChance = 0.10;
		public int nauseaSeconds = 6;
		public int cooldownSeconds = 90;
		/** Positive effects the Goblet can grant, as effect ids. */
		public List<String> effectPool = new ArrayList<>(List.of(
			"minecraft:speed", "minecraft:strength", "minecraft:jump_boost", "minecraft:regeneration",
			"minecraft:resistance", "minecraft:absorption", "minecraft:fire_resistance", "minecraft:haste"
		));
		public double wildPartyRadius = 8.0;
		public int wildPartySeconds = 30;
		public int wildPartyCooldownSeconds = 180;
	}

	public static class Mirror {
		/** Enchanting Gaze: the player you look at can't hurt you and is slowed. */
		public double gazeRange = 16.0;
		public int playerCharmSeconds = 3;
		/** Hostile mobs around you also stop targeting you. */
		public double radius = 10.0;
		public int pacifySeconds = 8;
		public int cooldownSeconds = 60;
		public boolean animalsFallInLove = true;
		public int charmSeconds = 15;
		public int charmCooldownSeconds = 120;
		public double charmRange = 24.0;
	}

	public static class Caduceus {
		public double blinkDistance = 10.0;
		public int cooldownSeconds = 8;
		public int waypointWindowSeconds = 300;
		public int waypointCooldownSeconds = 180;
	}

	public static class SpearOfAthena {
		public float throwDamage = 10.0f;
		public int pierceTargets = 3;
		public double shockwaveRadius = 4.0;
		public float shockwaveDamage = 6.0f;
		public int shockwaveSlownessSeconds = 4;
		public int shockwaveCooldownSeconds = 30;
	}

	public static class ForgeHammer {
		/** Blocks harder than the center block times this are not broken by the area. */
		public double maxHardnessRatio = 1.5;
	}

	public static class Harpe {
		public double bossDamageBonus = 0.5;
		public double leapRange = 12.0;
		public double leapDamageMultiplier = 2.0;
		public int leapCooldownSeconds = 30;
	}

	public static class Thyrsus {
		public int hitEffectSeconds = 5;
		public double snareRadius = 6.0;
		public int snareSeconds = 3;
		public int snareCooldownSeconds = 60;
	}

	public static class Cornucopia {
		public int cooldownSeconds = 600;
		public double feastRadius = 10.0;
		public int feastRegenSeconds = 10;
		public int feastCooldownSeconds = 900;
	}

	public static class Consumables {
		public float ambrosiaHeal = 12.0f;
		public int ambrosiaRegenSeconds = 10;
		public int ambrosiaAbsorptionSeconds = 120;
		public int ambrosiaCooldownSeconds = 30;
		public int nectarResistanceSeconds = 30;
		public int sunArrowFireSeconds = 5;
		public int sunArrowLightSeconds = 5;
	}

	public static class Hoplite {
		public double extraKnockbackResistance = 0.5;
	}

	public static class Events {
		/** Chance that a thunderstorm starts Wrath of Zeus by itself. */
		public double wrathAutoStartChance = 0.15;
		public int wrathAutoMinPlayers = 2;
		public int wrathWarningSeconds = 60;
		public int eventTimeoutMinutes = 30;
		/** Boss health ×(1 + this × (players nearby − 1)). */
		public double healthPerExtraPlayer = 0.5;
		public double rewardRadius = 64.0;
		public double rewardMinDamageShare = 0.10;
		public int rewardIngotsMin = 2;
		public int rewardIngotsMax = 4;
		public double rewardTemplateChance = 0.25;
		public float typhonHealth = 600f;
		public float cetusHealth = 700f;
		public float cerberusHealth = 650f;
		public float medusaHealth = 500f;
		public float dragonHealth = 800f;
		public float kronosHealth = 1200f;
	}

	public static class Thunderbolt {
		public float strikeDamage = 6.0f;
		public int strikes = 5;
		public int cooldownSeconds = 30;
		public double skybreakerHeight = 15.0;
		public int skybreakerCooldownSeconds = 60;
	}

	public static class TridentOfPoseidon {
		public double riptideStrength = 3.0;
		public double waveRadius = 4.0;
		public int channelingCooldownSeconds = 10;
		public double earthshakerRadius = 10.0;
		public float earthshakerDamage = 6.0f;
		public int earthshakerCooldownSeconds = 60;
	}

	public static class Bident {
		public int skeletons = 3;
		public int allySeconds = 30;
		public int cooldownSeconds = 90;
		public int soulHarvestSeconds = 15;
		public int soulHarvestCooldownSeconds = 180;
		public int maxAlliesPerPlayer = 8;
	}

	public static class HeadOfMedusa {
		public double range = 20.0;
		public int petrifySeconds = 4;
		/** Petrify on players lasts this fraction as long (stun-lock protection). */
		public double playerDurationMultiplier = 0.5;
		public int playerImmunitySeconds = 10;
		public int cooldownSeconds = 60;
		public double gazeRange = 10.0;
		public int gazeSeconds = 3;
		public int gazeCooldownSeconds = 120;
	}

	public static class GoldenFleece {
		public int cheatDeathCooldownSeconds = 600;
		public float cheatDeathHealth = 8.0f;
	}

	public static class Scythe {
		public double freezeRadius = 15.0;
		public int freezeSeconds = 6;
		public int cooldownSeconds = 120;
		public int rewindSeconds = 5;
		public int rewindCooldownSeconds = 180;
	}
}
