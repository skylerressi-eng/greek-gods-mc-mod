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
import net.minecraft.world.level.block.entity.BannerPatterns;

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
	public static final Item GOD_SWAPPER = register("god_swapper", Item::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final Item GORGONEION_BANNER_PATTERN = register("gorgoneion_banner_pattern", Item::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.RARE).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, c -> c.getOrThrow(ModKeys.PATTERN_ITEM_GORGONEION)));
	public static final Item THUNDERBOLT_BANNER_PATTERN = register("thunderbolt_banner_pattern", Item::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.RARE).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, c -> c.getOrThrow(ModKeys.PATTERN_ITEM_THUNDERBOLT)));

	// ─── Starter relics (one per Olympian) ──────────────────────────────────
	public static final Item AEGIS = starter("aegis", AegisItem::new, GodProperties.base()
		.equippableUnswappable(EquipmentSlot.OFFHAND)
		.component(DataComponents.BASE_COLOR, DyeColor.YELLOW)
		.delayedComponent(DataComponents.BANNER_PATTERNS, c -> new BannerPatternLayers(List.of(
			new BannerPatternLayers.Layer(c.getOrThrow(BannerPatterns.GRADIENT_UP), DyeColor.ORANGE),
			new BannerPatternLayers.Layer(c.getOrThrow(ModKeys.GORGONEION), DyeColor.BROWN),
			new BannerPatternLayers.Layer(c.getOrThrow(BannerPatterns.BORDER), DyeColor.ORANGE))))
		.delayedComponent(DataComponents.BLOCKS_ATTACKS, c -> new BlocksAttacks(
			0.25F, 1.0F,
			List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
			new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
			Optional.of(c.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
			Optional.of(SoundEvents.SHIELD_BLOCK),
			Optional.of(SoundEvents.SHIELD_BREAK))));

	public static final Item CONCH_OF_THE_DEEP = starter("conch_of_the_deep", ConchItem::new, GodProperties.base());

	public static final Item HELM_OF_DARKNESS = starter("helm_of_darkness",
		p -> new SimpleRelicItem(p, God.HADES, true, () -> ConfigManager.get().helmOfDarkness.cooldownSeconds, () -> 0),
		GodProperties.unbreakable(GodProperties.base()
			.humanoidArmor(STARTER.armor, ArmorType.HELMET)
			.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
				.setEquipSound(STARTER.armor.equipSound()).setAsset(equipmentAsset("helm_of_darkness")).build())));

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
			.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.FEET)
				.setEquipSound(STARTER.armor.equipSound()).setAsset(equipmentAsset("talaria")).build())));

	public static final Item HAMMER_OF_HEPHAESTUS = starter("hammer_of_hephaestus",
		p -> new SimpleRelicItem(p, God.HEPHAESTUS, true, () -> 0, () -> ConfigManager.get().hammerOfHephaestus.forgeFireCooldownSeconds),
		GodProperties.unbreakable(GodProperties.base().pickaxe(STARTER.tool, 1.0F, -2.8F)));

	public static final Item SICKLE_OF_DEMETER = starter("sickle_of_demeter", SickleItem::new,
		GodProperties.unbreakable(GodProperties.base().hoe(STARTER.tool, -4.0F, 0.0F)));

	public static final Item GOBLET_OF_DIONYSUS = starter("goblet_of_dionysus", GobletItem::new, GodProperties.base());

	public static final Item MIRROR_OF_APHRODITE = starter("mirror_of_aphrodite", MirrorItem::new, GodProperties.base());

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
