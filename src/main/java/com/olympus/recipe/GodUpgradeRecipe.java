package com.olympus.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.olympus.god.GodItem;
import com.olympus.registry.ModComponents;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.level.Level;

/**
 * Smithing-table upgrade: core (template slot) + god item at {@code fromLevel} + addition →
 * the same item at {@code toLevel}. Everything else on the item (enchantments, name, cooldowns)
 * is kept. Stored as a normal datapack JSON file, type {@code olympus:god_upgrade}:
 * <pre>{"type":"olympus:god_upgrade","template":"olympus:blessing_core","base":"#olympus:god_items",
 *  "addition":"olympus:olympian_bronze_block","from_level":0,"to_level":1}</pre>
 */
public class GodUpgradeRecipe implements SmithingRecipe {
	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;
	final int fromLevel;
	final int toLevel;
	private PlacementInfo placementInfo;

	public GodUpgradeRecipe(Ingredient template, Ingredient base, Ingredient addition, int fromLevel, int toLevel) {
		this.template = template;
		this.base = base;
		this.addition = addition;
		this.fromLevel = fromLevel;
		this.toLevel = toLevel;
	}

	@Override
	public boolean matches(SmithingRecipeInput input, Level level) {
		return SmithingRecipe.super.matches(input, level)
			&& input.base().getItem() instanceof GodItem
			&& input.base().getOrDefault(ModComponents.UPGRADE_LEVEL, 0) == fromLevel;
	}

	@Override
	public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
		ItemStack result = input.base().copyWithCount(1);
		result.set(ModComponents.UPGRADE_LEVEL, toLevel);
		return result;
	}

	@Override
	public Optional<Ingredient> templateIngredient() {
		return Optional.of(template);
	}

	@Override
	public Ingredient baseIngredient() {
		return base;
	}

	@Override
	public Optional<Ingredient> additionIngredient() {
		return Optional.of(addition);
	}

	@Override
	public RecipeSerializer<GodUpgradeRecipe> getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public PlacementInfo placementInfo() {
		if (placementInfo == null) {
			placementInfo = PlacementInfo.create(List.of(template, base, addition));
		}
		return placementInfo;
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of(new SmithingRecipeDisplay(template.display(), base.display(), addition.display(),
			base.display(), new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
	}

	public static class Serializer implements RecipeSerializer<GodUpgradeRecipe> {
		public static final Serializer INSTANCE = new Serializer();
		private static final MapCodec<GodUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Ingredient.CODEC.fieldOf("template").forGetter(r -> r.template),
			Ingredient.CODEC.fieldOf("base").forGetter(r -> r.base),
			Ingredient.CODEC.fieldOf("addition").forGetter(r -> r.addition),
			Codec.intRange(0, 1).fieldOf("from_level").forGetter(r -> r.fromLevel),
			Codec.intRange(1, 2).fieldOf("to_level").forGetter(r -> r.toLevel)
		).apply(i, GodUpgradeRecipe::new));
		private static final StreamCodec<RegistryFriendlyByteBuf, GodUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.template,
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.base,
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.addition,
			ByteBufCodecs.VAR_INT, r -> r.fromLevel,
			ByteBufCodecs.VAR_INT, r -> r.toLevel,
			GodUpgradeRecipe::new);

		@Override
		public MapCodec<GodUpgradeRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, GodUpgradeRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
