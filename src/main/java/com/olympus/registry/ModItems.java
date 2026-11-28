package com.olympus.registry;

import com.olympus.Olympus;
import com.olympus.config.ConfigManager;
import com.olympus.god.God;
import com.olympus.item.GodProperties;
import com.olympus.item.relic.AegisItem;
import com.olympus.item.relic.ConchItem;
import com.olympus.item.relic.GobletItem;
import com.olympus.item.relic.LyreItem;
import com.olympus.item.relic.MirrorItem;
import com.olympus.item.relic.OwlItem;
import com.olympus.item.relic.SickleItem;
import com.olympus.item.relic.SilverBowItem;
import com.olympus.item.relic.SimpleRelicItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public final class ModItems {
	/** Every item in creative-tab order. */
	public static final List<Item> ALL = new ArrayList<>();
	/** The 12 starter relics, in God.OLYMPIANS order. */
	public static final List<Item> STARTER_RELICS = new ArrayList<>();

	private static final GodProperties.Tier STARTER = GodProperties.starterTier();

	// ─── Materials ───────────────────────────────────────────────────────────
	public static final Item OLYMPIAN_BRONZE_INGOT = register("olympian_bronze_ingot", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant());
	public static final Item OLYMPIAN_BRONZE_BLOCK = register("olympian_bronze_block",
		p -> new BlockItem(ModBlocks.OLYMPIAN_BRONZE_BLOCK, p), new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant().useBlockDescriptionPrefix());
	public static final Item DIVINE_SMITHING_TEMPLATE = register("divine_smithing_template", ModItems::divineTemplate, new Item.Properties().rarity(Rarity.EPIC));
	public static final Item BLESSING_CORE = register("blessing_core", Item::new,
		new Item.Properties().rarity(Rarity.EPIC).fireResistant().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final Item ASCENSION_CORE = register("ascension_core", Item::new,
		new Item.Properties().rarity(Rarity.EPIC).fireResistant().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final Item GOD_SWAPPER = register("god_swapper", com.olympus.item.GodSwapperItem::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final Item LAUREL_OF_OLYMPUS = register("laurel_of_olympus", com.olympus.item.LaurelItem::new,
		new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).fireResistant().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final Item GORGONEION_BANNER_PATTERN = register("gorgoneion_banner_pattern", Item::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.RARE).component(DataComponents.PROVIDES_BANNER_PATTERNS, ModKeys.PATTERN_ITEM_GORGONEION));
	public static final Item THUNDERBOLT_BANNER_PATTERN = register("thunderbolt_banner_pattern", Item::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.RARE).component(DataComponents.PROVIDES_BANNER_PATTERNS, ModKeys.PATTERN_ITEM_THUNDERBOLT));

	// ─── Starter relics (one per Olympian) ──────────────────────────────────
	public static final Item AEGIS = starter("aegis", AegisItem::new, GodProperties.base()
		.equippableUnswappable(EquipmentSlot.OFFHAND)
		.component(DataComponents.BASE_COLOR, DyeColor.YELLOW)
		.component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY) // emblem added by AegisItem.applyEmblem
		.component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
			0.25F, 1.0F,
			List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
			new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
			Optional.of(DamageTypeTags.BYPASSES_SHIELD),
			Optional.of(SoundEvents.SHIELD_BLOCK),
			Optional.of(SoundEvents.SHIELD_BREAK))));

	public static final Item CONCH_OF_THE_DEEP = starter("conch_of_the_deep", ConchItem::new, GodProperties.base());

	public static final Item HELM_OF_DARKNESS = starter("helm_of_darkness",
		p -> new SimpleRelicItem(p, God.HADES, true, () -> ConfigManager.get().helmOfDarkness.cooldownSeconds, () -> 0),
		GodProperties.unbreakable(GodProperties.base()
			.humanoidArmor(STARTER.armor, ArmorType.HELMET)
			.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
				.setEquipSound(STARTER.armor.equipSound()).setAsset(EquipmentAssets.NETHERITE).build())));

	public static final Item OWL_OF_ATHENA = starter("owl_of_athena", OwlItem::new, GodProperties.base());

	public static final Item BLADE_OF_ARES = starter("blade_of_ares",
		p -> new SimpleRelicItem(p, God.ARES, true, () -> 0, () -> ConfigManager.get().bladeOfAres.warCryCooldownSeconds),
		GodProperties.unbreakable(GodProperties.base().sword(STARTER.tool, 3.0F, -2.4F)));

	public static final Item LYRE_OF_APOLLO = starter("lyre_of_apollo", LyreItem::new, GodProperties.base());

	public static final Item SILVER_BOW = starter("silver_bow", SilverBowItem::new, GodProperties.base().enchantable(STARTER.tool.enchantmentValue()));

	public static final Item TALARIA = starter("talaria",
		p -> new SimpleRelicItem(p, God.HERMES, true, () -> 0, () -> 0),
		GodProperties.unbreakable(GodProperties.base()
			.humanoidArmor(STARTER.armor, ArmorType.BOOTS)
			.attributes(STARTER.armor.createAttributes(ArmorType.BOOTS).withModifierAdded(
				Attributes.MOVEMENT_SPEED,
				new AttributeModifier(Olympus.id("talaria_speed"), ConfigManager.get().talaria.speedBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
				EquipmentSlotGroup.FEET))
			// Looks like leather boots (dyed gold); keeps the starter tier's armor stats.
			.component(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(0xD8B45A))
			.component(DataComponents.TOOLTIP_DISPLAY, net.minecraft.world.item.component.TooltipDisplay.DEFAULT.withHidden(DataComponents.DYED_COLOR, true))
			.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.FEET)
				.setEquipSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER).setAsset(EquipmentAssets.LEATHER).build())));

	public static final Item HAMMER_OF_HEPHAESTUS = starter("hammer_of_hephaestus",
		p -> new SimpleRelicItem(p, God.HEPHAESTUS, true, () -> 0, () -> ConfigManager.get().hammerOfHephaestus.forgeFireCooldownSeconds),
		GodProperties.unbreakable(GodProperties.base().pickaxe(STARTER.tool, 1.0F, -2.8F)));

	// A real HoeItem (so it tills), with durability stripped after the hoe stats are applied.
	public static final Item SICKLE_OF_DEMETER = starter("sickle_of_demeter", p -> new SickleItem(STARTER.tool, p), GodProperties.unbreakableHoe());

	public static final Item GOBLET_OF_DIONYSUS = starter("goblet_of_dionysus", GobletItem::new, GodProperties.base());

	public static final Item MIRROR_OF_APHRODITE = starter("mirror_of_aphrodite", MirrorItem::new, GodProperties.base());

	// ─── Phase 3: craftable god items (each needs its god's starter relic) ─────
	public static final List<Item> LEGENDARIES = new ArrayList<>();

	public static final Item CADUCEUS = register("caduceus", com.olympus.item.relic.CaduceusItem::new, GodProperties.base());
	public static final Item SPEAR_OF_ATHENA = register("spear_of_athena",
		p -> new com.olympus.item.relic.GodTridentItem(p, God.ATHENA, () -> 0, () -> ConfigManager.get().spearOfAthena.shockwaveCooldownSeconds),
		tridentProperties());
	public static final Item FORGE_HAMMER = register("forge_hammer",
		p -> new SimpleRelicItem(p, God.HEPHAESTUS, false, () -> 0, () -> 1),
		GodProperties.unbreakable(GodProperties.base().pickaxe(net.minecraft.world.item.ToolMaterial.NETHERITE, 1.0F, -2.8F)));
	public static final Item HARPE = register("harpe",
		p -> new SimpleRelicItem(p, God.HERMES, false, () -> 0, () -> ConfigManager.get().harpe.leapCooldownSeconds),
		// 10 attack damage: hits harder than an unenchanted netherite sword (8).
		GodProperties.unbreakable(GodProperties.base().sword(net.minecraft.world.item.ToolMaterial.NETHERITE, 5.0F, -2.4F)));
	public static final Item THYRSUS = register("thyrsus",
		p -> new SimpleRelicItem(p, God.DIONYSUS, false, () -> 0, () -> ConfigManager.get().thyrsus.snareCooldownSeconds),
		GodProperties.unbreakable(GodProperties.base().sword(net.minecraft.world.item.ToolMaterial.NETHERITE, 3.0F, -2.4F)));
	public static final Item CORNUCOPIA = register("cornucopia", com.olympus.item.relic.CornucopiaItem::new, GodProperties.base());

	// ─── Phase 3: consumables, ammo, armor (not god items) ─────────────────
	public static final Item AMBROSIA = register("ambrosia", com.olympus.item.ConsumableItems.Ambrosia::new,
		new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).food(new net.minecraft.world.food.FoodProperties(10, 12.0F, true))
			.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final Item NECTAR = register("nectar", com.olympus.item.ConsumableItems.Nectar::new,
		new Item.Properties().stacksTo(16).rarity(Rarity.RARE).component(DataComponents.CONSUMABLE, net.minecraft.world.item.component.Consumables.DEFAULT_DRINK));
	public static final Item SUN_ARROW = register("sun_arrow", com.olympus.item.SunArrowItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item HOPLITE_HELMET = hoplite("hoplite_helmet", ArmorType.HELMET);
	public static final Item HOPLITE_CHESTPLATE = hoplite("hoplite_chestplate", ArmorType.CHESTPLATE);
	public static final Item HOPLITE_LEGGINGS = hoplite("hoplite_leggings", ArmorType.LEGGINGS);
	public static final Item HOPLITE_BOOTS = hoplite("hoplite_boots", ArmorType.BOOTS);

	// ─── Phase 4: legendaries (boss drops, one of each per server) ─────────
	public static final Item THUNDERBOLT_OF_ZEUS = legendary("thunderbolt_of_zeus",
		p -> new com.olympus.item.relic.GodTridentItem(p, God.ZEUS, () -> ConfigManager.get().thunderbolt.cooldownSeconds, () -> ConfigManager.get().thunderbolt.skybreakerCooldownSeconds),
		tridentProperties());
	public static final Item TRIDENT_OF_POSEIDON = legendary("trident_of_poseidon", com.olympus.item.relic.PoseidonTridentItem::new, tridentProperties());
	public static final Item BIDENT_OF_HADES = legendary("bident_of_hades", com.olympus.item.relic.BidentItem::new,
		GodProperties.base().attributes(net.minecraft.world.item.TridentItem.createAttributes())
			.component(DataComponents.WEAPON, new net.minecraft.world.item.component.Weapon(1)).enchantable(15));
	public static final Item HEAD_OF_MEDUSA = legendary("head_of_medusa", com.olympus.item.relic.HeadOfMedusaItem::new,
		GodProperties.base().component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setSwappable(false).build())
			.attributes(net.minecraft.world.item.component.ItemAttributeModifiers.builder()
				.add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,
					new net.minecraft.world.entity.ai.attributes.AttributeModifier(com.olympus.Olympus.id("medusa_head_health"), 2.0,
						net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
					net.minecraft.world.entity.EquipmentSlotGroup.HEAD)
				.build()));
	public static final Item GOLDEN_FLEECE = legendary("golden_fleece",
		p -> new SimpleRelicItem(p, God.ARES, false, () -> 0, () -> ConfigManager.get().goldenFleece.cheatDeathCooldownSeconds).passiveBlessed(),
		GodProperties.unbreakable(GodProperties.base()
			.humanoidArmor(net.minecraft.world.item.equipment.ArmorMaterials.NETHERITE, ArmorType.CHESTPLATE)
			.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
				.setEquipSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_GOLD).setAsset(EquipmentAssets.GOLD).build())));
	public static final Item SCYTHE_OF_KRONOS = legendary("scythe_of_kronos", com.olympus.item.relic.ScytheOfKronosItem::new,
		GodProperties.unbreakable(GodProperties.base().sword(net.minecraft.world.item.ToolMaterial.NETHERITE, 4.0F, -2.6F)));

	public static final Item CODEX_OF_OLYMPUS = register("codex_of_olympus", com.olympus.item.CodexItem::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).component(DataComponents.WRITTEN_BOOK_CONTENT, com.olympus.item.CodexItem.content()));

	private static Item.Properties tridentProperties() {
		return GodProperties.base()
			.attributes(net.minecraft.world.item.TridentItem.createAttributes())
			.component(DataComponents.TOOL, net.minecraft.world.item.TridentItem.createToolProperties())
			.component(DataComponents.WEAPON, new net.minecraft.world.item.component.Weapon(1))
			.enchantable(15);
	}

	/**
	 * Hoplite armor: netherite stats plus a bonus per piece. Helmet: immune to Blindness and
	 * Darkness (handled in CraftedRelicHandler). Breastplate: +2 hearts. Greaves: +10% speed.
	 * Sandals: half fall damage. Full set: Resistance I, more knockback resistance, and attackers
	 * take back part of their melee damage.
	 */
	private static Item hoplite(String name, ArmorType type) {
		var material = net.minecraft.world.item.equipment.ArmorMaterials.NETHERITE;
		var attributes = material.createAttributes(type);
		var group = EquipmentSlotGroup.bySlot(type.getSlot());
		var id = Olympus.id(name + "_bonus");
		attributes = switch (type) {
			case CHESTPLATE -> attributes.withModifierAdded(Attributes.MAX_HEALTH, new AttributeModifier(id, 4.0, AttributeModifier.Operation.ADD_VALUE), group);
			case LEGGINGS -> attributes.withModifierAdded(Attributes.MOVEMENT_SPEED, new AttributeModifier(id, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group);
			case BOOTS -> attributes.withModifierAdded(Attributes.FALL_DAMAGE_MULTIPLIER, new AttributeModifier(id, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group);
			default -> attributes;
		};
		return register(name, Item::new, new Item.Properties().humanoidArmor(material, type).attributes(attributes).fireResistant().rarity(Rarity.RARE));
	}

	private static <T extends Item> T legendary(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		T item = register(name, factory, properties.rarity(Rarity.EPIC));
		LEGENDARIES.add(item);
		return item;
	}

	private ModItems() {
	}

	private static ResourceKey<EquipmentAsset> equipmentAsset(String name) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, Olympus.id(name));
	}

	private static <T extends Item> T starter(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		T item = register(name, factory, properties);
		STARTER_RELICS.add(item);
		return item;
	}

	private static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Olympus.id(name));
		T item = factory.apply(properties.setId(key));
		Registry.register(BuiltInRegistries.ITEM, key, item);
		if (item instanceof BlockItem blockItem) {
			blockItem.registerBlocks(Item.BY_BLOCK, item);
		}
		ALL.add(item);
		return item;
	}

	private static SmithingTemplateItem divineTemplate(Item.Properties properties) {
		ChatFormatting title = ChatFormatting.GRAY;
		ChatFormatting desc = ChatFormatting.BLUE;
		return new SmithingTemplateItem(
			Component.translatable("item.olympus.divine_smithing_template.applies_to").withStyle(desc),
			Component.translatable("item.olympus.divine_smithing_template.ingredients").withStyle(desc),
			Component.translatable("item.olympus.divine_smithing_template.base_slot_description"),
			Component.translatable("item.olympus.divine_smithing_template.additions_slot_description"),
			List.of(Identifier.withDefaultNamespace("container/slot/sword"), Identifier.withDefaultNamespace("container/slot/pickaxe"),
				Identifier.withDefaultNamespace("container/slot/helmet"), Identifier.withDefaultNamespace("container/slot/chestplate"),
				Identifier.withDefaultNamespace("container/slot/leggings"), Identifier.withDefaultNamespace("container/slot/boots")),
			List.of(Identifier.withDefaultNamespace("container/slot/ingot")),
			properties);
	}

	public static void init() {
	}
}
