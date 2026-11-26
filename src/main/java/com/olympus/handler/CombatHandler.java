package com.olympus.handler;

import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import com.olympus.fx.Fx;
import com.olympus.god.GodItem;
import com.olympus.item.relic.SilverBowItem;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import com.olympus.util.Targets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;

/**
 * PvP-facing relic effects that hook into damage:
 * <ul>
 *   <li>Owl of Athena: revealed targets take bonus damage from the Owl's owner.</li>
 *   <li>Mirror of Aphrodite: a charmed player can't hurt the charmer.</li>
 *   <li>Blade of Ares: hits build Fury → Strength.</li>
 *   <li>Hammer of Hephaestus: hits ignite; fire resistance while held.</li>
 *   <li>Sickle of Demeter: hits cause Famine (hunger, slowness, food drain).</li>
 *   <li>Silver Bow: bonus arrow damage.</li>
 * </ul>
 */
public final class CombatHandler {
	private record Mark(UUID by, long until) {
	}

	private record Fury(int stacks, long lastHit) {
	}

	/** target → who marked them with the Owl. */
	private static final Map<UUID, Mark> OWL_MARKS = new ConcurrentHashMap<>();
	/** charmed attacker → the Mirror user they can't hurt. */
	private static final Map<UUID, Mark> CHARMED = new ConcurrentHashMap<>();
	private static final Map<UUID, Fury> FURY = new ConcurrentHashMap<>();

	private CombatHandler() {
	}

	private static OlympusConfig cfg() {
		return ConfigManager.get();
	}

	public static void markByOwl(LivingEntity target, ServerPlayer owner, int ticks) {
		OWL_MARKS.put(target.getUUID(), new Mark(owner.getUUID(), owner.level().getGameTime() + ticks));
	}

	public static void charm(Player victim, ServerPlayer charmer, int ticks) {
		CHARMED.put(victim.getUUID(), new Mark(charmer.getUUID(), charmer.level().getGameTime() + ticks));
	}

	/** Melee hit made directly by a player holding {@code item} in the main hand. */
	private static ServerPlayer meleeWith(DamageSource source, net.minecraft.world.item.Item item) {
		if (source.getDirectEntity() instanceof ServerPlayer player && source.getEntity() == player && player.getMainHandItem().is(item)) {
			return player;
		}
		return null;
	}

	/** Called from {@code LivingEntityMixin} at the start of hurtServer: adjusts incoming damage. */
	public static float modifyIncomingDamage(LivingEntity victim, DamageSource source, float amount) {
		if (amount <= 0 || !(victim.level() instanceof ServerLevel level)) {
			return amount;
		}
		long now = level.getGameTime();
		float result = amount;

		// Owl of Athena mark
		Mark mark = OWL_MARKS.get(victim.getUUID());
		if (mark != null && source.getEntity() != null && mark.by().equals(source.getEntity().getUUID()) && now < mark.until()) {
			result *= (float) (1.0 + cfg().owl.markedDamageBonus);
			level.sendParticles(ModParticles.OWL_FEATHER, victim.getX(), victim.getEyeY(), victim.getZ(), 3, 0.2, 0.2, 0.2, 0.02);
		}

		// Helm of Darkness (Blessed): ambush from invisibility
		if (source.getDirectEntity() instanceof ServerPlayer attacker && source.getEntity() == attacker && HelmHandler.isHidden(attacker)) {
			ItemStack helm = attacker.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
			if (helm.is(ModItems.HELM_OF_DARKNESS) && GodItem.level(helm).atLeast(com.olympus.god.UpgradeLevel.BLESSED)) {
				result *= (float) cfg().helmOfDarkness.ambushDamageMultiplier;
				level.sendParticles(ModParticles.SHADE, victim.getX(), victim.getEyeY(), victim.getZ(), 20, 0.3, 0.3, 0.3, 0.05);
				Fx.sound(attacker, ModSounds.HELM_REAPPEAR, 1.2f, 0.6f);
				HelmHandler.reveal(attacker);
			}
		}

		// Silver Bow arrows
		if (source.getDirectEntity() instanceof AbstractArrow arrow && arrow.getTags().contains(SilverBowItem.ARROW_TAG)) {
			result *= (float) (1.0 + cfg().silverBow.arrowDamageBonus);
		}
		return result;
	}

	/** ALLOW_DAMAGE: a player charmed by the Mirror can't hurt their charmer. */
	private static boolean allowDamage(LivingEntity victim, DamageSource source, float amount) {
		// Talaria (Blessed): no fall damage.
		if (victim instanceof Player p && source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
			ItemStack boots = p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
			if (boots.is(ModItems.TALARIA) && GodItem.level(boots).atLeast(com.olympus.god.UpgradeLevel.BLESSED)) {
				return false;
			}
		}
		if (source.getEntity() instanceof Player attacker) {
			Mark charm = CHARMED.get(attacker.getUUID());
			if (charm != null && charm.by().equals(victim.getUUID()) && victim.level().getGameTime() < charm.until()) {
				if (victim.level() instanceof ServerLevel level) {
					level.sendParticles(ModParticles.ROSE_PETAL, attacker.getX(), attacker.getEyeY() + 0.3, attacker.getZ(), 4, 0.2, 0.1, 0.2, 0.01);
				}
				return false;
			}
		}
		return true;
	}

	private static void afterDamage(LivingEntity victim, DamageSource source, float baseDamage, float taken, boolean blocked) {
		if (taken <= 0 || !(victim.level() instanceof ServerLevel level)) {
			return;
		}

		ServerPlayer ares = meleeWith(source, ModItems.BLADE_OF_ARES);
		if (ares != null && Targets.canHarm(ares, victim)) {
			buildFury(ares, ares.getMainHandItem(), level);
		}

		ServerPlayer smith = meleeWith(source, ModItems.HAMMER_OF_HEPHAESTUS);
		if (smith != null && Targets.canHarm(smith, victim)) {
			victim.igniteForSeconds(cfg().hammerOfHephaestus.igniteSeconds);
			level.sendParticles(ModParticles.FORGE_SPARK, victim.getX(), victim.getY() + victim.getBbHeight() * 0.6, victim.getZ(), 8, 0.25, 0.25, 0.25, 0.08);
		}

		ServerPlayer reaper = meleeWith(source, ModItems.SICKLE_OF_DEMETER);
		if (reaper != null && Targets.canHarm(reaper, victim)) {
			OlympusConfig.Sickle c = cfg().sickle;
			victim.addEffect(new MobEffectInstance(MobEffects.HUNGER, c.famineHungerSeconds * 20, 1), reaper);
			victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, c.famineSlownessSeconds * 20, 0), reaper);
			if (victim instanceof Player target) {
				var food = target.getFoodData();
				food.setFoodLevel(Math.max(0, food.getFoodLevel() - c.famineFoodDrain));
			}
			level.sendParticles(ModParticles.GRAIN_LEAF, victim.getX(), victim.getY() + victim.getBbHeight() * 0.6, victim.getZ(), 6, 0.25, 0.25, 0.25, 0.03);
		}
	}

	private static void buildFury(ServerPlayer player, ItemStack blade, ServerLevel level) {
		OlympusConfig.BladeOfAres c = cfg().bladeOfAres;
		long now = level.getGameTime();
		Fury fury = FURY.get(player.getUUID());
		int stacks = fury == null || now - fury.lastHit() > c.furyDecaySeconds * 20L ? 1 : fury.stacks() + 1;
		FURY.put(player.getUUID(), new Fury(stacks, now));
		level.sendParticles(ModParticles.WAR_EMBER, player.getX(), player.getY() + 1.0, player.getZ(), 2 + stacks * 2, 0.3, 0.4, 0.3, 0.05);
		if (stacks >= c.furyHits) {
			FURY.remove(player.getUUID());
			MobEffectInstance current = player.getEffect(MobEffects.STRENGTH);
			if (current == null || current.getAmplifier() < 1) {
				player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, GodItem.level(blade).durationTicks(c.furyStrengthSeconds), 0, false, true, true));
			}
			Fx.helix(level, ModParticles.WAR_EMBER, player.position(), 0.7, 2.0, 24);
			Fx.sound(player, ModSounds.ARES_FURY, 0.8f, 1.2f);
		}
	}

	/** Every second: Hephaestus fire resistance, forget expired entries. */
	public static void tick(MinecraftServer server) {
		if (cfg().hammerOfHephaestus.fireResistanceWhileHeld) {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.getMainHandItem().is(ModItems.HAMMER_OF_HEPHAESTUS)) {
					player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false, true));
				}
			}
		}
		long now = server.overworld().getGameTime();
		OWL_MARKS.values().removeIf(m -> now >= m.until());
		CHARMED.values().removeIf(m -> now >= m.until());
		FURY.values().removeIf(f -> now - f.lastHit() > 200);
	}

	public static void forget(UUID player) {
		FURY.remove(player);
		CHARMED.remove(player);
		OWL_MARKS.remove(player);
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(CombatHandler::allowDamage);
		ServerLivingEntityEvents.AFTER_DAMAGE.register(CombatHandler::afterDamage);
	}
}
