package com.olympus.item.relic;

import com.olympus.ability.Cooldowns;
import com.olympus.god.God;
import com.olympus.god.GodItem;
import java.util.function.IntSupplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;

/**
 * A thrown god trident (Spear of Athena, Thunderbolt of Zeus, Trident of Poseidon). Thrown god
 * tridents always return to their owner ({@code ThrownTridentMixin}).
 */
public class GodTridentItem extends TridentItem implements GodItem {
	private final God god;
	private final IntSupplier primaryCooldown;
	private final IntSupplier blessedCooldown;

	public GodTridentItem(Properties properties, God god, IntSupplier primaryCooldown, IntSupplier blessedCooldown) {
		super(properties);
		this.god = god;
		this.primaryCooldown = primaryCooldown;
		this.blessedCooldown = blessedCooldown;
	}

	@Override
	public God god() {
		return god;
	}

	@Override
	public int primaryCooldownSeconds() {
		return primaryCooldown.getAsInt();
	}

	@Override
	public int blessedCooldownSeconds() {
		return blessedCooldown.getAsInt();
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer sp && primaryCooldownSeconds() > 0 && !Cooldowns.checkReady(sp, stack, Cooldowns.Kind.PRIMARY)) {
			return InteractionResult.FAIL;
		}
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
		ItemStack before = stack.copy();
		boolean thrown = super.releaseUsing(stack, level, entity, remainingTime);
		if (thrown && entity instanceof ServerPlayer player && primaryCooldownSeconds() > 0) {
			// The stack itself left with the trident; the cooldown is stored on the player and the thrown copy.
			Cooldowns.start(player, before, Cooldowns.Kind.PRIMARY, primaryCooldownSeconds());
		}
		return thrown;
	}
}
