package com.olympus.handler;

import com.olympus.ability.Cooldowns;
import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.god.GodItem;
import com.olympus.god.UpgradeLevel;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import com.olympus.util.DivineLightning;
import com.olympus.util.Targets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Legendary passives and thrown-trident effects. */
public final class LegendaryHandler {
	private record Snapshot(Vec3 pos, float health, ResourceKey<Level> dimension) {
	}

	/** Scythe of Kronos Rewind: last ~5 seconds of position and health. */
	private static final Map<UUID, Deque<Snapshot>> HISTORY = new ConcurrentHashMap<>();
	/** Skybreaker: player → launched (lands with lightning, no fall damage). */
	private static final Map<UUID, Integer> SKYBREAKER = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> CHANNELING_READY = new ConcurrentHashMap<>();
	/** Spear of Athena Blessed: next throw makes a shockwave. */
	private static final Map<UUID, Boolean> SPEAR_ARMED = new ConcurrentHashMap<>();
	public static final String IMPACT_TAG = "olympus.impacted";

	private LegendaryHandler() {
	}

	public static boolean isDivinelyBorne(Entity entity) {
		return SKYBREAKER.containsKey(entity.getUUID());
	}

	public static void armSpear(ServerPlayer player) {
		SPEAR_ARMED.put(player.getUUID(), true);
	}

	public static void skybreaker(ServerPlayer player, UpgradeLevel lvl) {
		double h = ConfigManager.get().thunderbolt.skybreakerHeight;
		player.setDeltaMovement(player.getDeltaMovement().x, Math.sqrt(2 * 0.08 * h) + 0.3, player.getDeltaMovement().z);
		player.hurtMarked = true;
		SKYBREAKER.put(player.getUUID(), 0);
		Fx.burst(player.level(), ModParticles.STORM_SPARK, player.position(), 40, 0.5, 0.2);
		Fx.sound(player, ModSounds.AEGIS_SMITE, 1.0f, 1.4f);
	}

	/** Rewind to where (and how healthy) the player was a few seconds ago. */
	public static boolean rewind(ServerPlayer player) {
		Deque<Snapshot> history = HISTORY.get(player.getUUID());
		if (history == null || history.isEmpty()) {
			return false;
		}
		Snapshot then = history.peekFirst();
		if (!then.dimension().equals(player.level().dimension())) {
			return false;
		}
		Fx.burst(player.level(), ModParticles.TITAN_DUST, player.position().add(0, 1, 0), 40, 0.4, 0.05);
		Fx.line(player.level(), ModParticles.TITAN_DUST, player.position().add(0, 1, 0), then.pos().add(0, 1, 0), 0.5);
		player.teleportTo(then.pos().x, then.pos().y, then.pos().z);
		player.resetFallDistance();
		player.setHealth(Math.max(player.getHealth(), then.health()));
		history.clear();
		Fx.sound(player, net.minecraft.sounds.SoundEvents.BELL_RESONATE, 1.2f, 1.5f);
		Fx.burst(player.level(), ModParticles.TITAN_DUST, then.pos().add(0, 1, 0), 40, 0.4, 0.05);
		return true;
	}

	/** Called from ThrownTridentMixin when a thrown god trident hits an entity. */
	public static void onTridentHitEntity(ThrownTrident trident, Entity hit) {
		if (!(trident.level() instanceof ServerLevel level) || !(trident.getOwner() instanceof ServerPlayer owner)) {
			return;
		}
		ItemStack stack = trident.getPickupItemStackOrigin();
		if (stack.is(ModItems.SPEAR_OF_ATHENA)) {
			// Pierce: the spear's force carries through to more enemies behind the first.
			Vec3 dir = trident.getDeltaMovement().lengthSqr() > 1.0E-4 ? trident.getDeltaMovement().normalize() : owner.getLookAngle();
			int extra = ConfigManager.get().spearOfAthena.pierceTargets - 1;
			var source = level.damageSources().trident(trident, owner);
			for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().expandTowards(dir.scale(6)).inflate(1.0),
				e -> e != hit && e != owner && Targets.canHarm(owner, e))) {
				if (extra-- <= 0) break;
				e.hurtServer(level, source, ConfigManager.get().spearOfAthena.throwDamage * 0.75f);
				Fx.line(level, ModParticles.OWL_FEATHER, hit.position().add(0, 1, 0), e.position().add(0, 1, 0), 0.4);
			}
			impact(trident, owner, stack, hit.position());
		} else if (stack.is(ModItems.THUNDERBOLT_OF_ZEUS)) {
			impact(trident, owner, stack, hit.position());
		} else if (stack.is(ModItems.TRIDENT_OF_POSEIDON)) {
			long now = level.getGameTime();
			if (now >= CHANNELING_READY.getOrDefault(owner.getUUID(), 0L) && Targets.canHarm(owner, hit)) {
				CHANNELING_READY.put(owner.getUUID(), now + ConfigManager.get().tridentOfPoseidon.channelingCooldownSeconds * 20L);
				DivineLightning.strike(level, hit.position(), owner, 5.0f, 1.0);
			}
		}
	}

	/** Called from AbstractArrowMixin when a thrown god trident hits a block. */
	public static void onTridentHitBlock(ThrownTrident trident) {
		if (trident.level() instanceof ServerLevel && trident.getOwner() instanceof ServerPlayer owner) {
			ItemStack stack = trident.getPickupItemStackOrigin();
			if (stack.is(ModItems.THUNDERBOLT_OF_ZEUS) || stack.is(ModItems.SPEAR_OF_ATHENA)) {
				impact(trident, owner, stack, trident.position());
			}
		}
	}

	private static void impact(ThrownTrident trident, ServerPlayer owner, ItemStack stack, Vec3 at) {
		if (trident.getTags().contains(IMPACT_TAG)) {
			return;
		}
		trident.addTag(IMPACT_TAG);
		ServerLevel level = owner.level();
		UpgradeLevel lvl = GodItem.level(stack);
		if (stack.is(ModItems.THUNDERBOLT_OF_ZEUS)) {
			int strikes = ConfigManager.get().thunderbolt.strikes;
			for (int i = 0; i < strikes; i++) {
				double a = Math.PI * 2 * i / strikes;
				double r = i == 0 ? 0 : lvl.radius(2.5);
				DivineLightning.strike(level, at.add(Math.cos(a) * r, 0, Math.sin(a) * r), owner, ConfigManager.get().thunderbolt.strikeDamage, 1.5);
			}
		} else if (stack.is(ModItems.SPEAR_OF_ATHENA) && SPEAR_ARMED.remove(owner.getUUID()) != null) {
			var cfg = ConfigManager.get().spearOfAthena;
			double radius = lvl.radius(cfg.shockwaveRadius);
			for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(at, at).inflate(radius),
				e -> Targets.canHarm(owner, e) && e.position().distanceTo(at) <= radius)) {
				Vec3 away = e.position().subtract(at).normalize();
				e.knockback(1.5, -away.x, -away.z);
				e.hurtMarked = true;
				e.hurtServer(level, level.damageSources().trident(trident, owner), cfg.shockwaveDamage);
				e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, lvl.durationTicks(cfg.shockwaveSlownessSeconds), 1), owner);
			}
			Fx.shockwave(level, ModParticles.OWL_FEATHER, at.add(0, 0.2, 0), radius);
			Fx.burst(level, ParticleTypes.EXPLOSION, at.add(0, 0.5, 0), 3, 0.5, 0);
			Fx.sound(level, at, net.minecraft.sounds.SoundEvents.MACE_SMASH_GROUND_HEAVY, 1.2f, 1.0f);
		}
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) ->
			!(source.is(DamageTypeTags.IS_FALL) && isDivinelyBorne(victim)));
		// Golden Fleece (Blessed): once per cooldown, a killing blow leaves you standing.
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
				return true;
			}
			ItemStack fleece = player.getItemBySlot(EquipmentSlot.CHEST);
			if (!fleece.is(ModItems.GOLDEN_FLEECE) || !GodItem.level(fleece).atLeast(UpgradeLevel.BLESSED)
				|| !Cooldowns.isReady(player, fleece, Cooldowns.Kind.BLESSED)) {
				return true;
			}
			player.setHealth(ConfigManager.get().goldenFleece.cheatDeathHealth);
			player.removeAllEffects();
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
			Cooldowns.start(player, fleece, Cooldowns.Kind.BLESSED, ConfigManager.get().goldenFleece.cheatDeathCooldownSeconds);
			player.level().broadcastEntityEvent(player, (byte) 35); // totem animation
			Fx.burst(player.level(), ModParticles.DIVINE_GLINT, player.position().add(0, 1, 0), 60, 0.5, 0.2);
			return false;
		});
	}

	public static void tickPlayer(ServerPlayer player) {
		// Rewind history (only kept for Scythe holders).
		if (player.getMainHandItem().is(ModItems.SCYTHE_OF_KRONOS) || player.getOffhandItem().is(ModItems.SCYTHE_OF_KRONOS)) {
			Deque<Snapshot> history = HISTORY.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>());
			history.addLast(new Snapshot(player.position(), player.getHealth(), player.level().dimension()));
			while (history.size() > ConfigManager.get().scythe.rewindSeconds * 20) {
				history.removeFirst();
			}
		} else {
			HISTORY.remove(player.getUUID());
		}

		// Skybreaker landing
		Integer air = SKYBREAKER.get(player.getUUID());
		if (air != null) {
			SKYBREAKER.put(player.getUUID(), air + 1);
			if (air > 5 && (player.onGround() || player.isInWater())) {
				SKYBREAKER.remove(player.getUUID());
				player.resetFallDistance();
				ItemStack bolt = player.getMainHandItem();
				UpgradeLevel lvl = GodItem.level(bolt);
				for (int i = 0; i < 8; i++) {
					double a = Math.PI * 2 * i / 8;
					DivineLightning.strike(player.level(), player.position().add(Math.cos(a) * lvl.radius(4), 0, Math.sin(a) * lvl.radius(4)),
						player, ConfigManager.get().thunderbolt.strikeDamage, 1.5);
				}
				Fx.shockwave(player.level(), ModParticles.STORM_SPARK, player.position().add(0, 0.2, 0), lvl.radius(4));
			} else if (air > 200) {
				SKYBREAKER.remove(player.getUUID());
			}
		}

		// Golden Fleece: Regeneration I + fire immunity while worn
		if (player.tickCount % 20 == 0 && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.GOLDEN_FLEECE)) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
			player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false, true));
		}
	}

	public static void forget(UUID player) {
		HISTORY.remove(player);
		SKYBREAKER.remove(player);
		CHANNELING_READY.remove(player);
		SPEAR_ARMED.remove(player);
	}

	public static void tick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tickPlayer(player);
		}
	}
}
