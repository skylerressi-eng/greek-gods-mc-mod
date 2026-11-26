package com.olympus.handler;

import com.olympus.Olympus;
import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.god.GodItem;
import com.olympus.item.SunArrowItem;
import com.olympus.registry.ModBlocks;
import com.olympus.registry.ModComponents;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModParticles;
import com.olympus.util.Targets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Phase 3 craftables: Forge Hammer, Harpe, Thyrsus, Hoplite Armor, Sun Arrows. */
public final class CraftedRelicHandler {
	private static final Identifier HOPLITE_KB = Olympus.id("hoplite_knockback");
	private static final ThreadLocal<Boolean> BREAKING_AREA = ThreadLocal.withInitial(() -> false);
	/** Harpe Hero's Leap: player → (target, ticks in flight). */
	private record Leap(UUID target, int ticks) {
	}

	private static final Map<UUID, Leap> LEAPS = new ConcurrentHashMap<>();

	private CraftedRelicHandler() {
	}

	public static void register() {
		// Forge Hammer: break a 3x3 (or 5x5) face around the mined block. Sneak to mine one block.
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (BREAKING_AREA.get() || !(player instanceof ServerPlayer sp) || player.isShiftKeyDown()) {
				return;
			}
			ItemStack tool = player.getMainHandItem();
			if (!tool.is(ModItems.FORGE_HAMMER) || !tool.isCorrectToolForDrops(state)) {
				return;
			}
			int r = tool.getOrDefault(ModComponents.MINING_RADIUS, 1);
			Direction face = facing(sp);
			float centerHardness = state.getDestroySpeed(level, pos);
			BREAKING_AREA.set(true);
			try {
				for (int a = -r; a <= r; a++) {
					for (int b = -r; b <= r; b++) {
						if (a == 0 && b == 0) continue;
						BlockPos target = switch (face.getAxis()) {
							case X -> pos.offset(0, a, b);
							case Y -> pos.offset(a, 0, b);
							case Z -> pos.offset(a, b, 0);
						};
						BlockState other = level.getBlockState(target);
						float hardness = other.getDestroySpeed(level, target);
						if (other.isAir() || hardness < 0 || hardness > centerHardness * ConfigManager.get().forgeHammer.maxHardnessRatio + 0.5f
							|| !tool.isCorrectToolForDrops(other)) {
							continue;
						}
						sp.gameMode.destroyBlock(target); // fires break events: claims/protection mods are respected
					}
				}
			} finally {
				BREAKING_AREA.set(false);
			}
		});

		// Thyrsus hits, Harpe vs bosses (damage bonus handled in modifyDamage).
		ServerLivingEntityEvents.AFTER_DAMAGE.register((victim, source, base, taken, blocked) -> {
			if (taken > 0 && source.getDirectEntity() instanceof AbstractArrow arrow) {
				onSunArrowHit(arrow, victim);
			}
			if (taken <= 0 || !(source.getDirectEntity() instanceof ServerPlayer player) || source.getEntity() != player) {
				return;
			}
			if (player.getMainHandItem().is(ModItems.THYRSUS) && Targets.canHarm(player, victim)) {
				int t = ConfigManager.get().thyrsus.hitEffectSeconds * 20;
				victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, t, 1), player);
				victim.addEffect(new MobEffectInstance(victim instanceof Player ? MobEffects.NAUSEA : MobEffects.WEAKNESS, t, 0), player);
				((ServerLevel) victim.level()).sendParticles(ModParticles.WINE_DROP, victim.getX(), victim.getEyeY(), victim.getZ(), 6, 0.2, 0.2, 0.2, 0.02);
			}
		});
	}

	/** Extra damage multiplier for the Harpe against bosses. */
	public static float harpeBonus(LivingEntity victim, net.minecraft.world.damagesource.DamageSource source, float amount) {
		if (source.getDirectEntity() instanceof Player p && p.getMainHandItem().is(ModItems.HARPE) && Targets.isBoss(victim)) {
			return amount * (float) (1.0 + ConfigManager.get().harpe.bossDamageBonus);
		}
		return amount;
	}

	private static Direction facing(ServerPlayer player) {
		Vec3 eye = player.getEyePosition();
		BlockHitResult hit = player.level().clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(6)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		return hit.getType() == HitResult.Type.BLOCK ? hit.getDirection() : Direction.orderedByNearest(player)[0];
	}

	public static void startLeap(ServerPlayer player, LivingEntity target) {
		Vec3 to = target.position().subtract(player.position());
		Vec3 horizontal = new Vec3(to.x, 0, to.z);
		double dist = horizontal.length();
		Vec3 vel = horizontal.normalize().scale(Math.min(2.2, 0.3 + dist * 0.16)).add(0, 0.55 + Math.max(0, to.y) * 0.12, 0);
		player.setDeltaMovement(vel);
		player.hurtMarked = true;
		LEAPS.put(player.getUUID(), new Leap(target.getUUID(), 0));
		Fx.burst(player.level(), ModParticles.WIND_WISP, player.position(), 20, 0.4, 0.1);
		Fx.sound(player, net.minecraft.sounds.SoundEvents.BREEZE_JUMP, 1.0f, 0.8f);
	}

	public static void tick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			// Harpe leap strike: hit when close, give up after 2s.
			Leap leap = LEAPS.get(player.getUUID());
			if (leap != null) {
				var target = player.level().getEntity(leap.target());
				if (!(target instanceof LivingEntity living) || !living.isAlive() || leap.ticks() > 40) {
					LEAPS.remove(player.getUUID());
				} else if (player.distanceToSqr(living) < 9) {
					LEAPS.remove(player.getUUID());
					float dmg = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * ConfigManager.get().harpe.leapDamageMultiplier);
					living.hurtServer(player.level(), player.damageSources().playerAttack(player), dmg);
					player.resetFallDistance();
					Fx.burst(player.level(), ModParticles.WIND_WISP, living.position().add(0, 1, 0), 30, 0.4, 0.2);
					Fx.sound(player, net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT, 1.2f, 0.8f);
				} else {
					LEAPS.put(player.getUUID(), new Leap(leap.target(), leap.ticks() + 1));
				}
			}

			// Hoplite set bonus, checked once a second.
			if (player.tickCount % 20 == 0) {
				boolean full = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HOPLITE_HELMET)
					&& player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HOPLITE_CHESTPLATE)
					&& player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.HOPLITE_LEGGINGS)
					&& player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.HOPLITE_BOOTS);
				var kb = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
				if (full) {
					player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, false, true));
					if (kb != null && !kb.hasModifier(HOPLITE_KB)) {
						kb.addTransientModifier(new AttributeModifier(HOPLITE_KB, ConfigManager.get().hoplite.extraKnockbackResistance, AttributeModifier.Operation.ADD_VALUE));
					}
				} else if (kb != null) {
					kb.removeModifier(HOPLITE_KB);
				}
			}
		}
	}

	/** Sun Arrow landed (entity or block): burn the target, light up the spot. */
	public static void onSunArrowHit(AbstractArrow arrow, LivingEntity target) {
		if (!(arrow.level() instanceof ServerLevel level) || !arrow.getTags().contains(SunArrowItem.TAG)) {
			return;
		}
		if (target != null) {
			target.igniteForSeconds(ConfigManager.get().consumables.sunArrowFireSeconds);
		}
		BlockPos pos = arrow.blockPosition();
		for (BlockPos p : new BlockPos[]{pos, pos.above()}) {
			if (level.getBlockState(p).isAir()) {
				level.setBlockAndUpdate(p, ModBlocks.SUN_GLOW.defaultBlockState());
				break;
			}
		}
		Fx.burst(level, ModParticles.SUN_MOTE, arrow.position(), 20, 0.4, 0.05);
		arrow.removeTag(SunArrowItem.TAG);
	}

	public static void forget(UUID player) {
		LEAPS.remove(player);
	}
}
