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
		t.add(ModItems.LAUREL_OF_OLYMPUS, "Laurel of Olympus");
		t.add("olympus.hint.ambrosia", "Full heal, clears harmful effects, Regeneration III, Absorption IV, Resistance II, Strength, Fire Resistance, full hunger");
		t.add("olympus.hint.nectar", "Clears every harmful effect and grants Resistance I");
		t.add("olympus.hint.sun_arrow", "Sets targets on fire and lights up where it lands");
		t.add("olympus.hint.hoplite_helmet", "Immune to Blindness and Darkness");
		t.add("olympus.hint.hoplite_chestplate", "+2 hearts");
		t.add("olympus.hint.hoplite_leggings", "+10%% movement speed");
		t.add("olympus.hint.hoplite_boots", "Half fall damage");
		t.add("olympus.hint.hoplite_set", "Full set: Resistance I, extra knockback resistance, melee attackers take 15%% back");
		t.add("olympus.hint.laurel", "Right-click: permanently carry one more god item (max +%s)");
		t.add("olympus.message.laurel_used", "The gods honor you: you may now carry %s relics");
		t.add("olympus.message.laurel_maxed", "The gods' favor is already yours: you carry up to %s relics");
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
			"Battle Wisdom", "You and allies within %s blocks gain Strength I and Resistance I for %ss.");
		relic(t, ModItems.BLADE_OF_ARES, "Blade of Ares",
			"Fury: %s hits in a row grant Strength I. Kills grant Strength II for %ss.",
			"War Cry", "Mobs within %s blocks are weakened; you and nearby allies gain Speed II for %ss.");
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
			"Wild Party", "Allies within %s blocks get a random blessing for %ss; enemies stagger drunkenly.");
		relic(t, ModItems.MIRROR_OF_APHRODITE, "Mirror of Aphrodite",
			"Enchanting Gaze: the player you look at can't hurt you for %ss and is slowed. Nearby hostile mobs also leave you alone.",
			"Charm", "The mob you look at fights for you for %ss. Bosses and players resist.");

		// ─── Phase 3: craftable god items ───
		relic(t, ModItems.CADUCEUS, "Caduceus",
			"Right-click: blink up to %s blocks where you look (never through walls).",
			"Waypoint", "First press marks your spot; press again within %s minutes to return to it.");
		relic(t, ModItems.SPEAR_OF_ATHENA, "Spear of Athena",
			"A thrown spear that pierces up to %s enemies and always returns to you.",
			"Aegis Strike", "Your next throw lands with a shockwave that knocks back and slows enemies within %s blocks.");
		relic(t, ModItems.FORGE_HAMMER, "Forge Hammer",
			"Mines a 3x3 area (sneak to mine a single block).",
			"Forge Mode", "Switch between 3x3 and 5x5 mining.");
		relic(t, ModItems.HARPE, "Harpe",
			"Perseus' blade: %s more damage against bosses.",
			"Hero's Leap", "Leap at the creature you look at (up to %s blocks) and strike it for double damage.");
		relic(t, ModItems.THYRSUS, "Thyrsus",
			"Hits make enemies stagger: Slowness II and dizziness for %ss.",
			"Vine Snare", "Roots every enemy within %s blocks in place for %ss.");
		relic(t, ModItems.CORNUCOPIA, "Cornucopia",
			"Right-click: fully restores your hunger and saturation.",
			"Feast", "Fully feeds every ally within %s blocks and grants Regeneration.");
		t.add(ModItems.AMBROSIA, "Ambrosia");
		t.add(ModItems.NECTAR, "Nectar");
		t.add(ModItems.SUN_ARROW, "Sun Arrow");
		t.add(ModItems.HOPLITE_HELMET, "Hoplite Helmet");
		t.add(ModItems.HOPLITE_CHESTPLATE, "Hoplite Breastplate");
		t.add(ModItems.HOPLITE_LEGGINGS, "Hoplite Greaves");
		t.add(ModItems.HOPLITE_BOOTS, "Hoplite Sandals");
		t.add(ModItems.CODEX_OF_OLYMPUS, "Codex of Olympus");

		// ─── Phase 4: legendaries ───
		relic(t, ModItems.THUNDERBOLT_OF_ZEUS, "Thunderbolt of Zeus",
			"Throw it: %s lightning strikes rain around the impact, then it returns to your hand.",
			"Skybreaker", "Launch about %s blocks into the sky and land in a ring of lightning. No fall damage.");
		relic(t, ModItems.TRIDENT_OF_POSEIDON, "Trident of Poseidon",
			"Release to Riptide anywhere, even on dry land, with a wave that throws enemies back. Sneak to throw: Channeling is always on.",
			"Earthshaker", "Every enemy within %s blocks is hurled into the air.");
		relic(t, ModItems.BIDENT_OF_HADES, "Bident of Hades",
			"Right-click: %s skeleton warriors rise to fight for you for %ss.",
			"Soul Harvest", "For %ss, everything you kill rises as a skeleton ally.");
		relic(t, ModItems.HEAD_OF_MEDUSA, "Head of Medusa",
			"Look at a creature and right-click: it turns to stone for %ss (shorter on players). Wear it on your head.",
			"Gorgon's Gaze", "Petrifies everything in a %s-block cone in front of you for %ss.");
		relic(t, ModItems.GOLDEN_FLEECE, "Golden Fleece",
			"While worn: constant Regeneration and immunity to fire.",
			"Undying", "Once every %s minutes, a killing blow leaves you standing instead.");
		relic(t, ModItems.SCYTHE_OF_KRONOS, "Scythe of Kronos",
			"Right-click: time freezes for everything within %s blocks (Slowness IV, Mining Fatigue III) for %ss.",
			"Rewind", "Return to where you were %s seconds ago, with the health you had then.");

		// ─── Bosses & events ───
		t.add("entity.olympus.typhon", "Typhon");
		t.add("entity.olympus.cetus", "Cetus");
		t.add("entity.olympus.cerberus", "Cerberus");
		t.add("entity.olympus.medusa", "Medusa");
		t.add("entity.olympus.colchian_dragon", "The Colchian Dragon");
		t.add("entity.olympus.kronos", "Kronos, King of the Titans");
		t.add("entity.olympus.underworld_warrior", "Underworld Warrior");
		event(t, "wrath_of_zeus", "Wrath of Zeus", "Typhon tears through the storm clouds!");
		event(t, "rise_of_the_deep", "Rise of the Deep", "Cetus rises from the abyss!");
		event(t, "gates_of_the_underworld", "Gates of the Underworld", "Cerberus breaks his chains!");
		event(t, "gorgons_lair", "Gorgon's Lair", "Medusa awakens. Do not meet her gaze!");
		event(t, "quest_for_the_fleece", "Quest for the Fleece", "The sleepless dragon stirs!");
		event(t, "titanomachy", "Titanomachy", "Kronos returns to reclaim the heavens!");
		t.add("olympus.event.started.title", "%s");
		t.add("olympus.event.started.chat", "⚡ %s has begun! %s appears at %s, %s, %s. The greatest hero will claim a legendary relic.");
		t.add("olympus.event.won.title", "%s is over!");
		t.add("olympus.event.legendary_won", "🏆 %s dealt the most damage and claims the %s!");
		t.add("olympus.event.legendary_taken", "That legendary already belongs to someone. The gods reward you with bronze and a template instead.");
		t.add("olympus.event.your_share", "You dealt %s%% of the damage. The gods reward you.");
		t.add("olympus.event.legendary_lost", "The %s has fallen into the void. Its event may bring it back.");
		t.add("olympus.event.needs_ocean", "Rise of the Deep needs deep ocean water (10+ blocks) below you.");
		t.add("olympus.event.needs_nether", "Gates of the Underworld can only open in the Nether.");
		t.add("olympus.event.already_running", "%s is already running. Use /olympus event stop first.");
		t.add("olympus.event.none_running", "No event is running.");
		t.add("olympus.event.stopped", "The event was ended by an admin.");
		t.add("olympus.event.timeout", "Nobody defeated the boss in time. %s fades away...");
		t.add("olympus.event.vanished", "The boss of %s has vanished.");
		t.add("olympus.event.wrath_warning", "⚡ The sky darkens... Zeus' wrath will strike in %s seconds near %s (%s, %s, %s)!");
		t.add("olympus.message.look_away", "Medusa's eyes begin to glow... LOOK AWAY!");
		t.add("olympus.message.petrified", "You have been turned to stone!");
		t.add("olympus.message.soul_harvest", "Soul Harvest: your victims will rise to serve you");
		t.add("olympus.message.waypoint_set", "Waypoint marked. Press again to return here");
		t.add("olympus.message.waypoint_dimension", "Your waypoint is in another dimension");
		t.add("olympus.message.waypoint_blocked", "Your waypoint is blocked");
		t.add("olympus.message.spear_armed", "Your next throw will shake the earth");
		t.add("olympus.message.hammer_mode", "Forge Hammer: %s mining");
		t.add("olympus.message.legendary_duplicate", "A false %s crumbles to dust: only one exists");
		t.add("olympus.tooltip.legendary_of", "Legendary Relic of %s");
		t.add("olympus.tooltip.first_won_by", "First won by %s");
		t.add("olympus.command.legendary_reset", "%s can drop again from its event.");
		t.add("olympus.command.legendary_free", "%s: not yet won");
		t.add("olympus.command.legendary_owned", "%s: first won by %s, last seen with %s");

		codex(t);

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
		t.add("olympus.claim.title", "%s claims you!");
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

		t.add("olympus.message.limit", "The gods allow you only %s relics");
		t.add("olympus.message.needs_blessing", "%s must be Blessed ★ to use its Divine Power");
		t.add("olympus.message.no_divine_power", "Hold or wear a Blessed god item to use Divine Power");
		t.add("olympus.message.volley_ready", "Volley ready! Your next shot fires a spread of silver arrows");
		t.add("olympus.message.forge_nothing", "Hold something smeltable in your offhand");
		t.add("olympus.message.harvest_nothing", "No ripe crops nearby");
		t.add("olympus.message.charm_nothing", "Look at a mob to charm it");
		t.add("olympus.message.swapper_howto", "Hold the God Swapper in your main hand and your starter relic in your offhand, then hold right-click");
		t.add("key.olympus.divine_power", "Divine Power");
		t.add("key.category.olympus.relics", "Relics of Olympus");
		t.add("olympus.hint.blessing_core", "Smithing table: Blessing Core + god item + Block of Olympian Bronze → Blessed ★");
		t.add("olympus.hint.ascension_core", "Smithing table: Ascension Core + Blessed god item + Block of Olympian Bronze → Ascended ★★");
		t.add("olympus.hint.god_swapper", "Hold in main hand, starter relic in offhand, hold right-click for 3s to reroll your god");
		t.add("olympus.hint.blessed_unlocks", "Blessed ★: -25%% cooldowns, unlocks the Divine Power (R)");
		t.add("olympus.hint.ascended_unlocks", "Ascended ★★: -50%% cooldowns, +25%% durations and radii");

		t.add("olympus.ally.self", "You're always your own ally.");
		t.add("olympus.ally.added", "%s is now your ally: your relic buffs reach them and your powers spare them.");
		t.add("olympus.ally.removed", "%s is no longer your ally.");
		t.add("olympus.ally.added_you", "%s added you as an ally. Use /ally add to return the favor.");
		t.add("olympus.ally.list", "Your allies: %s (team: %s)");

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

	private static void event(TranslationBuilder t, String id, String name, String subtitle) {
		t.add("olympus.event." + id, name);
		t.add("olympus.event." + id + ".subtitle", subtitle);
	}

	/** Codex of Olympus pages (CodexItem.PAGES). Numbers use the default config. */
	private static void codex(TranslationBuilder t) {
		java.util.List<String> pages = new java.util.ArrayList<>();
		pages.add("§6§lCodex of Olympus§r\n\nThe gods walk among you. Each mortal is claimed by one of the twelve Olympians and receives their starter relic.\n\nCarry at most §l3§r god items. They drop when you die, and others may claim them.");
		pages.add("§lRelic Rules§r\n\n• God items never break.\n• Dropped relics glow and never despawn.\n• No ender chests, shulker boxes or bundles.\n• Enchant them like their base item (no Unbreaking or Mending).");
		pages.add("§lUpgrades§r\n\nSmithing table:\n\n§bBlessed ★§r: Blessing Core + god item + Block of Olympian Bronze.\n-25% cooldowns, unlocks the §lDivine Power§r (key R).\n\n§6Ascended ★★§r: Ascension Core + Blessed item + bronze block. -50% cooldowns, +25% reach.");
		pages.add("§lGod Swapper§r\n\nHold it in your main hand, your starter relic in your offhand, and hold right-click for 3 seconds.\n\nYour relic becomes another god's, keeping its level and every enchantment it can.");
		java.util.List<Item> relics = new java.util.ArrayList<>(ModItems.STARTER_RELICS);
		relics.addAll(java.util.List.of(ModItems.CADUCEUS, ModItems.SPEAR_OF_ATHENA, ModItems.FORGE_HAMMER, ModItems.HARPE, ModItems.THYRSUS, ModItems.CORNUCOPIA));
		relics.addAll(ModItems.LEGENDARIES);
		for (Item item : relics) {
			String key = item.getDescriptionId();
			Object[][] args = com.olympus.god.GodTooltip.argsFor(item);
			String source = ModItems.STARTER_RELICS.contains(item) ? "Starter relic (claiming / God Swapper)"
				: ModItems.LEGENDARIES.contains(item) ? "Legendary: boss event reward" : "Crafted (see RECIPES / recipe book)";
			pages.add("§l" + NAMES.get(item) + "§r\n§7" + source + "§r\n\n" + fmt(TEXTS.get(key + ".primary"), args[0])
				+ "\n\n§b✦ " + TEXTS.get(key + ".blessed.name") + ":§r " + fmt(TEXTS.get(key + ".blessed"), args[1]));
		}
		pages.add("§lDivine Events§r\n\nAdmins summon bosses with /olympus event start. Wrath of Zeus may strike in thunderstorms.\n\nThe top damage dealer wins the event's legendary. Everyone with 10%+ damage gets bronze, maybe a template.");
		pages.add("§lLegendaries§r\n\nEach exists once per server. Lost to the void, it can drop again.\n\nTyphon → Thunderbolt\nCetus → Poseidon's Trident\nCerberus → Bident\nMedusa → Her Head\nDragon → Golden Fleece\nKronos → Scythe");
		while (pages.size() < com.olympus.item.CodexItem.PAGES) {
			pages.add("§o\"Fortune favors the bold.\"§r\n\n— Virgil");
		}
		for (int i = 0; i < com.olympus.item.CodexItem.PAGES; i++) {
			t.add("olympus.codex.page." + (i + 1), pages.get(i));
		}
	}

	private static String fmt(String template, Object[] args) {
		if (template == null) return "";
		try {
			return String.format(java.util.Locale.ROOT, template.replace("%%", "%%"), args);
		} catch (Exception e) {
			return template;
		}
	}

	private static final java.util.Map<String, String> TEXTS = new java.util.HashMap<>();
	private static final java.util.Map<Item, String> NAMES = new java.util.HashMap<>();

	private static void god(TranslationBuilder t, God god, String name, String epithet) {
		t.add("god.olympus." + god.id(), name);
		t.add("god.olympus." + god.id() + ".epithet", epithet);
	}

	private static void relic(TranslationBuilder t, Item item, String name, String primary, String blessedName, String blessed) {
		t.add(item, name);
		String key = item.getDescriptionId();
		NAMES.put(item, name);
		TEXTS.put(key + ".primary", primary);
		TEXTS.put(key + ".blessed.name", blessedName);
		TEXTS.put(key + ".blessed", blessed);
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
