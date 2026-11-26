package com.olympus.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** Apollo — Sun Arrow: sets targets on fire and lights up where it lands. */
public class SunArrowItem extends ArrowItem {
	public static final String TAG = "olympus.sun_arrow";

	public SunArrowItem(Properties properties) {
		super(properties);
	}

	@Override
	public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
		AbstractArrow arrow = super.createArrow(level, ammo, shooter, weapon);
		arrow.addTag(TAG);
		arrow.igniteForSeconds(100);
		return arrow;
	}
}
