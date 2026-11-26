package com.olympus.client.datagen;

import com.olympus.god.God;
import com.olympus.registry.ModBlocks;
import com.olympus.registry.ModCreativeTab;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModSounds;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;

public class OlympusLangProvider extends FabricLanguageProvider {
	public OlympusLangProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder t) {
		t.add(ModCreativeTab.KEY, "Relics of Olympus");

		// ─── Gods ───
		god(t, God.ZEUS, "Zeus", "King of the Gods, Lord of the Sky and Thunder");
		god(t, God.POSEIDON, "Poseidon", "Lord of the Seas, Earthshaker");
		god(t, God.HADES, "Hades", "Lord of the Underworld, the Unseen One");
		god(t, God.ATHENA, "Athena", "Goddess of Wisdom and Strategy");
		god(t, God.ARES, "Ares", "God of War and Bloodlust");
		god(t, God.APOLLO, "Apollo", "God of the Sun, Music and Healing");
		god(t, God.ARTEMIS, "Artemis", "Goddess of the Hunt and the Moon");
		god(t, God.HERMES, "Hermes", "Messenger of the Gods, Patron of Travelers");
		god(t, God.HEPHAESTUS, "Hephaestus", "God of the Forge and Fire");
		god(t, God.DEMETER, "Demeter", "Goddess of the Harvest");
		god(t, God.DIONYSUS, "Dionysus", "God of Wine and Revelry");
		god(t, God.APHRODITE, "Aphrodite", "Goddess of Love and Beauty");
		god(t, God.TITAN, "the Titans", "Enemies of Olympus");

		// ─── Materials ───
		t.add(ModItems.OLYMPIAN_BRONZE_INGOT, "Olympian Bronze Ingot");
		t.add(ModBlocks.OLYMPIAN_BRONZE_BLOCK, "Block of Olympian Bronze");
		t.add(ModItems.DIVINE_SMITHING_TEMPLATE, "Smithing Template");
		t.add("item.olympus.divine_smithing_template.applies_to", "Netherite Gear & Tridents");
		t.add("item.olympus.divine_smithing_template.ingredients", "Olympian Bronze Block");
		t.add("item.olympus.divine_smithing_template.base_slot_description", "Add netherite gear or a trident");
		t.add("item.olympus.divine_smithing_template.additions_slot_description", "Add a Block of Olympian Bronze");
		t.add("item.olympus.divine_smithing_template.title", "Divine Upgrade");
		t.add(ModItems.BLESSING_CORE, "Blessing Core");
		t.add(ModItems.ASCENSION_CORE, "Ascension Core");
		t.add(ModItems.GOD_SWAPPER, "God Swapper");
		t.add(ModItems.GORGONEION_BANNER_PATTERN, "Banner Pattern");
		t.add("item.olympus.gorgoneion_banner_pattern.desc", "Gorgoneion");
		t.add(ModItems.THUNDERBOLT_BANNER_PATTERN, "Banner Pattern");
		t.add("item.olympus.thunderbolt_banner_pattern.desc", "Thunderbolt");
		for (String color : new String[]{"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"}) {
			String name = titleCase(color.replace('_', ' '));
			t.add("block.olympus.banner.gorgoneion." + color, name + " Gorgoneion");
			t.add("block.olympus.banner.thunderbolt." + color, name + " Thunderbolt");
		}

		// ─── Starter relics ───
		relic(t, ModItems.AEGIS, "Aegis",
			"Blocked melee hits push the attacker back, with a %s chance to strike them with lightning.",
			"Divine Shockwave", "Every %sth blocked hit releases a shockwave that knocks back and smites hostile mobs within %s blocks.");
		relic(t, ModItems.CONCH_OF_THE_DEEP, "Conch of the Deep",
			"Tidal Surge: a wave crashes %s blocks ahead, knocking enemies back and slowing them for %ss. You gain Water Breathing and Dolphin's Grace.",
			"Riptide Dash", "Launch forward like Riptide, even on dry land.");
		relic(t, ModItems.HELM_OF_DARKNESS, "Helm of Darkness",
			"Sneak in place for %ss to vanish completely, armor and all, with Speed I, for %ss.",
			"Ambush", "While invisible, your first hit deals %sx damage and ends the invisibility.");
		relic(t, ModItems.OWL_OF_ATHENA, "Owl of Athena",
			"Reveal: everything within %s blocks glows for %ss and takes %s more damage from you.",
			"Battle Wisdom", "You and players within %s blocks gain Strength I and Resistance I for %ss.");
		relic(t, ModItems.BLADE_OF_ARES, "Blade of Ares",
			"Fury: %s hits in a row grant Strength I. Kills grant Strength II for %ss.",
			"War Cry", "Mobs within %s blocks are weakened; you and nearby players gain Speed II for %ss.");
		relic(t, ModItems.LYRE_OF_APOLLO, "Lyre of Apollo",
			"Right-click: you and allies within %s blocks get Regeneration II and Speed I for %ss, and are cleansed of Poison and Wither.",
			"Sunburst", "Sets hostile mobs within %s blocks ablaze and blinds enemies for %ss.");
		relic(t, ModItems.SILVER_BOW, "Silver Bow",
			"Arrows fly perfectly straight, deal %s more damage, and anything they hit glows for %ss.",
			"Volley", "Your next shot fires %s arrows in a spread for the cost of one (within %ss).");
		relic(t, ModItems.TALARIA, "Talaria",
			"Permanent Speed I and a double jump while worn.",
			"Wings of Hermes", "Triple jump, and you never take fall damage.");
		relic(t, ModItems.HAMMER_OF_HEPHAESTUS, "Hammer of Hephaestus",
			"Hits set enemies ablaze for %ss, you're immune to fire while holding it, and mined ores come out smelted.",
			"Forge Fire", "Instantly smelts up to %s items from the stack in your offhand.");
		relic(t, ModItems.SICKLE_OF_DEMETER, "Sickle of Demeter",
			"Famine: hits cause Hunger and Slowness and drain %s food from players. Right-click crops to ripen a %sx%s area.",
			"Great Harvest", "Harvests and replants every ripe crop in a %sx%s area.");
		relic(t, ModItems.GOBLET_OF_DIONYSUS, "Goblet of Dionysus",
			"Drink: a random level II combat blessing for %ss (%s chance of a hangover instead). Never empties.",
			"Wild Party", "Players within %s blocks get a random blessing for %ss; mobs stagger drunkenly.");
		relic(t, ModItems.MIRROR_OF_APHRODITE, "Mirror of Aphrodite",
			"Enchanting Gaze: the player you look at can't hurt you for %ss and is slowed. Nearby hostile mobs also leave you alone.",
			"Charm", "The mob you look at fights for you for %ss. Bosses and players resist.");

		// ─── Tooltips ───
		t.add("olympus.tooltip.relic_of", "Relic of %s");
		t.add("olympus.tooltip.starter_relic_of", "Starter Relic of %s");
		t.add("olympus.tooltip.level", "Level: %s");
		t.add("olympus.tooltip.primary", "Primary Power");
		t.add("olympus.tooltip.passive", "(Passive)");
		t.add("olympus.tooltip.key_hint", "Divine Power key");
		t.add("olympus.tooltip.requires_blessed", "Requires Blessed ★");
		t.add("olympus.tooltip.cooldown", "Cooldown: %s");
		t.add("olympus.tooltip.unbreakable", "✦ Unbreakable · Forged by the gods");
		t.add("olympus.level.mortal", "Mortal");
		t.add("olympus.level.blessed", "Blessed");
		t.add("olympus.level.ascended", "Ascended");

		// ─── Messages ───
		t.add("olympus.claim.title", "You have been claimed by %s!");
		t.add("olympus.claim.chat", "⚡ %s has claimed you as their champion. Wield the %s well.");
		t.add("olympus.claim.broadcast", "%s has been claimed by %s.");
		t.add("olympus.message.cooldown", "%s recharges in %s");
		t.add("olympus.message.owl_reveal", "Athena's owl reveals %s creatures");
		t.add("olympus.message.helm_vanish", "You fade into the shadows of the Underworld…");
		t.add("olympus.message.goblet_blessing", "Dionysus blesses you with %s!");
		t.add("olympus.message.goblet_hangover", "Hic! Perhaps one cup too many…");
		t.add("olympus.message.mirror_charmed", "You are enchanted by %s and cannot bring yourself to strike them!");
		t.add("death.attack.olympus.divine_lightning", "%1$s was smitten by divine lightning");
		t.add("death.attack.olympus.divine_lightning.player", "%1$s was smitten by %2$s's divine lightning");
		t.add("death.attack.olympus.divine_lightning.item", "%1$s was smitten by %2$s wielding %3$s");

		// ─── Commands ───
		t.add("olympus.command.unknown_god", "Unknown god: %s");
		t.add("olympus.command.unknown_item", "Unknown Olympus item: %s");
		t.add("olympus.command.not_god_item", "Hold a god item in your main hand.");
		t.add("olympus.command.claimed", "%s has been claimed by %s");
		t.add("olympus.command.gave", "Gave %s to %s");
		t.add("olympus.command.level_set", "%s is now %s");
		t.add("olympus.command.reloaded", "Relics of Olympus config reloaded.");
		t.add("olympus.command.reload_failed", "Config error, previous values kept: %s");

		// ─── Sound subtitles ───
		for (String name : ModSounds.ALL.keySet()) {
			t.add("subtitles.olympus." + name, subtitle(name));
		}
	}

	private static void god(TranslationBuilder t, God god, String name, String epithet) {
		t.add("god.olympus." + god.id(), name);
		t.add("god.olympus." + god.id() + ".epithet", epithet);
	}

	private static void relic(TranslationBuilder t, Item item, String name, String primary, String blessedName, String blessed) {
		t.add(item, name);
		String key = item.getDescriptionId();
		t.add(key + ".primary", primary);
		t.add(key + ".blessed.name", blessedName);
		t.add(key + ".blessed", blessed);
	}

	private static String titleCase(String s) {
		StringBuilder b = new StringBuilder();
		for (String w : s.split(" ")) {
			if (!b.isEmpty()) b.append(' ');
			b.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
		}
		return b.toString();
	}

	private static String subtitle(String name) {
		return switch (name) {
			case "event.claim" -> "Thunder of the gods";
			case "event.claim_chime" -> "Divine chime";
			case "ability.deny" -> "Relic still recharging";
			case "ability.aegis.smite" -> "Aegis calls lightning";
			case "ability.conch.blow" -> "Conch sounds";
			case "ability.helm.vanish" -> "Someone vanishes";
			case "ability.helm.reappear" -> "Someone reappears";
			case "ability.helm.charge" -> "Shadows gather";
			case "ability.owl.reveal" -> "Owl hoots";
			case "ability.ares.fury" -> "War fury";
			case "ability.ares.fury_ii" -> "War fury grows";
			case "ability.lyre.chord" -> "Lyre plays";
			case "ability.lyre.shimmer" -> "Healing light";
			case "ability.silver_bow.shot" -> "Silver arrow flies";
			case "ability.silver_bow.mark" -> "Quarry marked";
			case "ability.talaria.jump" -> "Winged leap";
			case "ability.hephaestus.smelt" -> "Ore smelts";
			case "ability.sickle.bloom" -> "Crops ripen";
			case "ability.goblet.drink" -> "Goblet drunk";
			case "ability.goblet.hic" -> "Hic!";
			case "ability.mirror.charm" -> "Mirror enchants";
			default -> name;
		};
	}
}
