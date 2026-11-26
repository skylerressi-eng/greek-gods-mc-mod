package com.olympus.item.relic;

import com.olympus.config.ConfigManager;
import com.olympus.god.God;
import com.olympus.god.GodItem;
import com.olympus.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Artemis — Silver Bow. Arrows fly perfectly straight and mark what they hit. */
public class SilverBowItem extends BowItem implements GodItem {
	/** Scoreboard tag put on arrows fired from the Silver Bow. */
	public static final String ARROW_TAG = "olympus.silver_arrow";

	public SilverBowItem(Properties properties) {
		super(properties);
	}

	@Override
	public God god() {
		return God.ARTEMIS;
	}

	@Override
	public boolean isStarterRelic() {
		return true;
	}

	@Override
	public int blessedCooldownSeconds() {
		return ConfigManager.get().silverBow.volleyCooldownSeconds;
	}

	@Override
	protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack projectile, boolean isCrit) {
		Projectile arrow = super.createProjectile(level, shooter, weapon, projectile, isCrit);
		arrow.setNoGravity(true);
		arrow.addTag(ARROW_TAG);
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), ModSounds.SILVER_SHOT, SoundSource.PLAYERS, 0.7f, 1.4f);
		}
		return arrow;
	}
}
