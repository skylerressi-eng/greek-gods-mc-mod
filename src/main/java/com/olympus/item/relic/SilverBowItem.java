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
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
		boolean shot = super.releaseUsing(stack, level, entity, remainingTime);
		if (shot && level instanceof ServerLevel serverLevel && entity instanceof net.minecraft.world.entity.player.Player player
			&& com.olympus.ability.DivinePower.consumeVolley(player)) {
			float power = getPowerForTime(getUseDuration(stack, entity) - remainingTime) * 3.0F;
			int extra = Math.max(0, ConfigManager.get().silverBow.volleyArrows - 1);
			for (int i = 0; i < extra; i++) {
				float yawOffset = ((i / 2) + 1) * 6.0F * (i % 2 == 0 ? 1 : -1);
				var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(serverLevel, player, new ItemStack(net.minecraft.world.item.Items.ARROW), stack);
				arrow.pickup = net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.CREATIVE_ONLY; // no arrow duplication
				arrow.setNoGravity(true);
				arrow.addTag(ARROW_TAG);
				arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + yawOffset, 0.0F, power, 1.0F);
				serverLevel.addFreshEntity(arrow);
			}
			serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SILVER_MARK, SoundSource.PLAYERS, 1.0f, 0.7f);
		}
		return shot;
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
