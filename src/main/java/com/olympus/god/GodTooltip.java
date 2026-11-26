package com.olympus.god;

import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import com.olympus.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Tooltip for god items: god, upgrade level, primary power, Blessed ability (greyed out until
 * unlocked) and cooldowns already adjusted for the item's level. Numbers come from the config
 * (the server's synced copy on clients), so they always match what the item really does.
 */
public final class GodTooltip {
	private GodTooltip() {
	}

	public static void append(ItemStack stack, List<Component> lines) {
		int at = Math.min(1, lines.size());
		if (stack.is(ModItems.BLESSING_CORE)) {
			lines.add(at, Component.translatable("olympus.hint.blessed_unlocks").withStyle(ChatFormatting.AQUA));
			lines.add(at, Component.translatable("olympus.hint.blessing_core").withStyle(ChatFormatting.GRAY));
			return;
		}
		if (stack.is(ModItems.ASCENSION_CORE)) {
			lines.add(at, Component.translatable("olympus.hint.ascended_unlocks").withStyle(ChatFormatting.GOLD));
			lines.add(at, Component.translatable("olympus.hint.ascension_core").withStyle(ChatFormatting.GRAY));
			return;
		}
		if (stack.is(ModItems.LAUREL_OF_OLYMPUS)) {
			lines.add(at, Component.translatable("olympus.hint.laurel", ConfigManager.get().general.laurelMaxBonusSlots).withStyle(ChatFormatting.GOLD));
			return;
		}
		if (stack.is(ModItems.GOD_SWAPPER)) {
			lines.add(at, Component.translatable("olympus.hint.god_swapper").withStyle(ChatFormatting.GRAY));
			return;
		}
		if (!(stack.getItem() instanceof GodItem godItem)) {
			return;
		}
		UpgradeLevel level = GodItem.level(stack);
		God god = godItem.god();
		String key = "item.olympus." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
		Object[][] args = args(stack.getItem(), level);

		List<Component> out = new ArrayList<>();
		out.add(Component.translatable(ModItems.LEGENDARIES.contains(stack.getItem()) ? "olympus.tooltip.legendary_of"
			: godItem.isStarterRelic() ? "olympus.tooltip.starter_relic_of" : "olympus.tooltip.relic_of", god.displayName())
			.withStyle(ChatFormatting.GRAY));
		var legendary = stack.get(com.olympus.registry.ModComponents.LEGENDARY);
		if (legendary != null) {
			out.add(Component.translatable("olympus.tooltip.first_won_by", legendary.firstOwner()).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
		}
		out.add(Component.translatable("olympus.tooltip.level", level.displayName()).withStyle(ChatFormatting.DARK_GRAY));
		out.add(Component.empty());

		// Primary power
		out.add(Component.literal("⚡ ").withStyle(god.style())
			.append(Component.translatable("olympus.tooltip.primary").withStyle(ChatFormatting.GOLD)));
		out.add(indent(Component.translatable(key + ".primary", args[0]).withStyle(ChatFormatting.GRAY)));
		if (godItem.primaryCooldownSeconds() > 0) {
			out.add(indent(cooldownLine(godItem.primaryCooldownSeconds(), level)));
		}

		// Blessed ability
		boolean unlocked = level.atLeast(UpgradeLevel.BLESSED);
		out.add(Component.literal("✦ ").withStyle(unlocked ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY)
			.append(Component.translatable(key + ".blessed.name").withStyle(unlocked ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY))
			.append(Component.literal(godItem.blessedIsPassive() ? " " : " [").withStyle(ChatFormatting.DARK_GRAY))
			.append(godItem.blessedIsPassive()
				? Component.translatable("olympus.tooltip.passive").withStyle(ChatFormatting.DARK_GRAY)
				: Component.translatable("olympus.tooltip.key_hint").withStyle(ChatFormatting.DARK_GRAY).append("]")));
		out.add(indent(Component.translatable(key + ".blessed", args[1]).withStyle(unlocked ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY)));
		if (!unlocked) {
			out.add(indent(Component.translatable("olympus.tooltip.requires_blessed").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)));
		} else if (!godItem.blessedIsPassive()) {
			out.add(indent(cooldownLine(godItem.blessedCooldownSeconds(), level)));
		}

		out.add(Component.empty());
		out.add(Component.translatable("olympus.tooltip.unbreakable").withStyle(ChatFormatting.DARK_AQUA));

		int insertAt = Math.min(1, lines.size());
		lines.addAll(insertAt, out);
	}

	private static Component indent(Component c) {
		return Component.literal("  ").append(c);
	}

	private static MutableComponent cooldownLine(int baseSeconds, UpgradeLevel level) {
		int ticks = level.cooldownTicks(baseSeconds);
		return Component.translatable("olympus.tooltip.cooldown", com.olympus.ability.Cooldowns.formatSeconds(ticks)).withStyle(ChatFormatting.DARK_GRAY);
	}

	private static String pct(double chance) {
		return Math.round(chance * 100) + "%";
	}

	private static String num(double value) {
		return value == Math.floor(value) ? String.valueOf((long) value) : String.format(java.util.Locale.ROOT, "%.1f", value);
	}

	/** Format arguments for "{item}.primary" and "{item}.blessed" lang strings. */
	private static Object[][] args(Item item, UpgradeLevel l) {
		OlympusConfig c = ConfigManager.get();
		if (item == ModItems.AEGIS) {
			return new Object[][]{{pct(c.aegis.lightningChance)}, {c.aegis.shockwaveEveryBlocks, num(l.radius(c.aegis.shockwaveRadius))}};
		} else if (item == ModItems.CONCH_OF_THE_DEEP) {
			return new Object[][]{{num(l.radius(c.conch.waveRange)), num(l.durationTicks(c.conch.waveSlownessSeconds) / 20.0)}, {}};
		} else if (item == ModItems.HELM_OF_DARKNESS) {
			return new Object[][]{{num(c.helmOfDarkness.sneakSeconds), num(l.durationTicks(c.helmOfDarkness.invisibilitySeconds) / 20.0)}, {num(c.helmOfDarkness.ambushDamageMultiplier)}};
		} else if (item == ModItems.OWL_OF_ATHENA) {
			return new Object[][]{{num(l.radius(c.owl.radius)), num(l.durationTicks(c.owl.glowSeconds) / 20.0), pct(c.owl.markedDamageBonus)},
				{num(l.radius(c.owl.battleWisdomRadius)), num(l.durationTicks(c.owl.battleWisdomSeconds) / 20.0)}};
		} else if (item == ModItems.BLADE_OF_ARES) {
			return new Object[][]{{c.bladeOfAres.furyHits, num(l.durationTicks(c.bladeOfAres.strengthSeconds) / 20.0)},
				{num(l.radius(c.bladeOfAres.warCryRadius)), num(l.durationTicks(c.bladeOfAres.warCrySeconds) / 20.0)}};
		} else if (item == ModItems.LYRE_OF_APOLLO) {
			return new Object[][]{{num(l.radius(c.lyre.radius)), num(l.durationTicks(c.lyre.regenSeconds) / 20.0)},
				{num(l.radius(c.lyre.sunburstRadius)), num(l.durationTicks(c.lyre.sunburstBlindSeconds) / 20.0)}};
		} else if (item == ModItems.SILVER_BOW) {
			return new Object[][]{{pct(c.silverBow.arrowDamageBonus), num(c.silverBow.glowSeconds)}, {c.silverBow.volleyArrows, num(c.silverBow.volleyWindowSeconds)}};
		} else if (item == ModItems.TALARIA) {
			return new Object[][]{{}, {}};
		} else if (item == ModItems.HAMMER_OF_HEPHAESTUS) {
			return new Object[][]{{num(c.hammerOfHephaestus.igniteSeconds)}, {c.hammerOfHephaestus.forgeFireMaxItems}};
		} else if (item == ModItems.SICKLE_OF_DEMETER) {
			int grow = l == UpgradeLevel.ASCENDED ? (int) Math.round(l.radius(c.sickle.growRadius)) : c.sickle.growRadius;
			int harvest = (int) Math.round(l.radius(c.sickle.harvestRadius));
			return new Object[][]{{c.sickle.famineFoodDrain, grow * 2 + 1, grow * 2 + 1}, {harvest * 2 + 1, harvest * 2 + 1}};
		} else if (item == ModItems.GOBLET_OF_DIONYSUS) {
			return new Object[][]{{num(l.durationTicks(c.goblet.effectSeconds) / 20.0), pct(c.goblet.nauseaChance)},
				{num(l.radius(c.goblet.wildPartyRadius)), num(l.durationTicks(c.goblet.wildPartySeconds) / 20.0)}};
		} else if (item == ModItems.MIRROR_OF_APHRODITE) {
			return new Object[][]{{num(l.durationTicks(c.mirror.playerCharmSeconds) / 20.0)},
				{num(l.durationTicks(c.mirror.charmSeconds) / 20.0)}};
		}
		if (item == ModItems.CADUCEUS) {
			return new Object[][]{{num(l.radius(c.caduceus.blinkDistance))}, {num(c.caduceus.waypointWindowSeconds / 60.0)}};
		} else if (item == ModItems.SPEAR_OF_ATHENA) {
			return new Object[][]{{c.spearOfAthena.pierceTargets}, {num(l.radius(c.spearOfAthena.shockwaveRadius))}};
		} else if (item == ModItems.HARPE) {
			return new Object[][]{{pct(c.harpe.bossDamageBonus)}, {num(c.harpe.leapRange)}};
		} else if (item == ModItems.THYRSUS) {
			return new Object[][]{{num(c.thyrsus.hitEffectSeconds)}, {num(l.radius(c.thyrsus.snareRadius)), num(l.durationTicks(c.thyrsus.snareSeconds) / 20.0)}};
		} else if (item == ModItems.CORNUCOPIA) {
			return new Object[][]{{}, {num(l.radius(c.cornucopia.feastRadius))}};
		} else if (item == ModItems.THUNDERBOLT_OF_ZEUS) {
			return new Object[][]{{c.thunderbolt.strikes}, {num(c.thunderbolt.skybreakerHeight)}};
		} else if (item == ModItems.TRIDENT_OF_POSEIDON) {
			return new Object[][]{{}, {num(l.radius(c.tridentOfPoseidon.earthshakerRadius))}};
		} else if (item == ModItems.BIDENT_OF_HADES) {
			return new Object[][]{{c.bident.skeletons, num(l.durationTicks(c.bident.allySeconds) / 20.0)}, {num(l.durationTicks(c.bident.soulHarvestSeconds) / 20.0)}};
		} else if (item == ModItems.HEAD_OF_MEDUSA) {
			return new Object[][]{{num(l.durationTicks(c.headOfMedusa.petrifySeconds) / 20.0)}, {num(c.headOfMedusa.gazeRange), num(l.durationTicks(c.headOfMedusa.gazeSeconds) / 20.0)}};
		} else if (item == ModItems.GOLDEN_FLEECE) {
			return new Object[][]{{}, {num(c.goldenFleece.cheatDeathCooldownSeconds / 60.0)}};
		} else if (item == ModItems.SCYTHE_OF_KRONOS) {
			return new Object[][]{{num(l.radius(c.scythe.freezeRadius)), num(l.durationTicks(c.scythe.freezeSeconds) / 20.0)}, {num(c.scythe.rewindSeconds)}};
		}
		return new Object[][]{{}, {}};
	}

	/** Public for the Codex datagen: the formatted power texts' arguments. */
	public static Object[][] argsFor(Item item) {
		return args(item, UpgradeLevel.MORTAL);
	}
}
