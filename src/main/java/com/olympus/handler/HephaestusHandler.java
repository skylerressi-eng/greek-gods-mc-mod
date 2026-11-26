package com.olympus.handler;

import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Hammer of Hephaestus primary: ores come out of the ground already smelted. */
public final class HephaestusHandler {
	private HephaestusHandler() {
	}

	public static boolean isOre(BlockState state) {
		return state.is(ConventionalBlockTags.ORES) || state.is(Blocks.ANCIENT_DEBRIS);
	}

	/** Called from {@code BlockMixin} with the drops of a block broken by a player's tool. */
	public static List<ItemStack> smeltDrops(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, List<ItemStack> drops) {
		if (!tool.is(ModItems.HAMMER_OF_HEPHAESTUS) || !isOre(state) || drops.isEmpty()) {
			return drops;
		}
		var silkTouch = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
		if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, tool) > 0) {
			return drops; // silk touch keeps the ore block
		}
		List<ItemStack> result = new ArrayList<>(drops.size());
		float xp = 0;
		boolean smelted = false;
		for (ItemStack drop : drops) {
			Optional<RecipeHolder<SmeltingRecipe>> recipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(drop), level);
			if (recipe.isEmpty()) {
				result.add(drop);
				continue;
			}
			ItemStack out = recipe.get().value().assemble(new SingleRecipeInput(drop));
			if (out.isEmpty()) {
				result.add(drop);
				continue;
			}
			out.setCount(out.getCount() * drop.getCount());
			xp += recipe.get().value().experience() * drop.getCount();
			result.add(out);
			smelted = true;
		}
		if (smelted) {
			Vec3 c = Vec3.atCenterOf(pos);
			level.sendParticles(ModParticles.FORGE_SPARK, c.x, c.y, c.z, 14, 0.3, 0.3, 0.3, 0.08);
			level.sendParticles(ParticleTypes.FLAME, c.x, c.y, c.z, 6, 0.25, 0.25, 0.25, 0.01);
			Fx.sound(level, c, ModSounds.HEPHAESTUS_SMELT, 0.6f, 1.2f + level.getRandom().nextFloat() * 0.3f);
			if (ConfigManager.get().hammerOfHephaestus.grantSmeltingXp) {
				int whole = (int) xp;
				if (level.getRandom().nextFloat() < xp - whole) {
					whole++;
				}
				if (whole > 0) {
					ExperienceOrb.award(level, c, whole);
				}
			}
		}
		return result;
	}
}
