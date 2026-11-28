package com.olympus.handler;

import net.minecraft.world.phys.Vec3;

import com.olympus.Olympus;
import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.util.Targets;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

/** Head of Medusa: petrified creatures can't move, jump or attack. Players get a shorter stun and immunity after. */
public final class PetrifyHandler {
	private static final Identifier STONE = Olympus.id("petrified");
	private static final String STONE_TAG = "olympus.petrified";
	private record Stone(long until, ServerLevel level, boolean wasNoAi, UUID source) {
	}

	private static final Map<UUID, Stone> PETRIFIED = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> IMMUNE_UNTIL = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> LAST_BLOCK_FX = new ConcurrentHashMap<>();

	private PetrifyHandler() {
	}

	/** Vine Snare roots: can't move or jump, but can still fight. */
	private static final Map<UUID, Stone> ROOTED = new ConcurrentHashMap<>();

	public static void root(LivingEntity target, ServerLevel level, int ticks) {
		Stone existing = ROOTED.get(target.getUUID());
		long until = level.getGameTime() + ticks;
		ROOTED.put(target.getUUID(), new Stone(existing != null ? Math.max(existing.until(), until) : until, level, false, null));
		addModifier(target.getAttribute(Attributes.MOVEMENT_SPEED));
		addModifier(target.getAttribute(Attributes.JUMP_STRENGTH));
		target.setDeltaMovement(0, Math.min(0, target.getDeltaMovement().y), 0);
		target.hurtMarked = true;
	}

	public static boolean isPetrified(Entity entity) {
		return PETRIFIED.containsKey(entity.getUUID());
	}

	public static boolean petrify(LivingEntity target, ServerPlayer source, int ticks) {
		return petrifyBy(target, source.level(), ticks, source);
	}

	public static boolean petrifyBy(LivingEntity target, ServerLevel level, int ticks) {
		return petrifyBy(target, level, ticks, null);
	}

	/**
	 * A raised shield facing the gaze reflects it (as Perseus did). Plays the block effect.
	 * @return true if the target's shield blocked it
	 */
	public static boolean shieldBlocks(LivingEntity target, Entity source) {
		if (source == null || !target.isBlocking()) {
			return false;
		}
		Vec3 toSource = source.position().subtract(target.position()).multiply(1, 0, 1);
		Vec3 facing = target.getViewVector(1.0f).multiply(1, 0, 1);
		if (toSource.lengthSqr() < 1.0E-4 || facing.lengthSqr() < 1.0E-4 || toSource.normalize().dot(facing.normalize()) <= 0) {
			return false;
		}
		long now = target.level().getGameTime();
		if (now - LAST_BLOCK_FX.getOrDefault(target.getUUID(), -100L) < 10) {
			return true; // still blocked, effects already shown
		}
		LAST_BLOCK_FX.put(target.getUUID(), now);
		Fx.sound(target, net.minecraft.sounds.SoundEvents.SHIELD_BLOCK.value(), 1.2f, 0.8f);
		if (target.level() instanceof ServerLevel level) {
			level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getEyeY() - 0.3, target.getZ(), 15, 0.3, 0.3, 0.3, 0.1);
		}
		if (target instanceof ServerPlayer sp) {
			sp.displayClientMessage(Component.translatable("olympus.message.gaze_blocked").withStyle(net.minecraft.ChatFormatting.AQUA), true);
		}
		if (source instanceof ServerPlayer caster) {
			caster.displayClientMessage(Component.translatable("olympus.message.gaze_blocked_caster", target.getDisplayName()).withStyle(net.minecraft.ChatFormatting.GRAY), true);
		}
		return true;
	}

	/** Petrify from any source (players with the Head of Medusa, or Medusa herself). */
	public static boolean petrifyBy(LivingEntity target, ServerLevel level, int ticks, Entity source) {
		if (Targets.isBoss(target) || isPetrified(target) || shieldBlocks(target, source)) {
			return false;
		}
		long now = level.getGameTime();
		if (target instanceof Player) {
			if (now < IMMUNE_UNTIL.getOrDefault(target.getUUID(), 0L)) {
				return false;
			}
			ticks = (int) Math.max(1, ticks * ConfigManager.get().headOfMedusa.playerDurationMultiplier);
			IMMUNE_UNTIL.put(target.getUUID(), now + ticks + ConfigManager.get().headOfMedusa.playerImmunitySeconds * 20L);
			if (target instanceof ServerPlayer sp) {
				sp.displayClientMessage(Component.translatable("olympus.message.petrified").withStyle(net.minecraft.ChatFormatting.GRAY), true);
			}
		}
		boolean wasNoAi = target instanceof Mob mob && mob.isNoAi();
		PETRIFIED.put(target.getUUID(), new Stone(now + ticks, level, wasNoAi, source == null ? null : source.getUUID()));
		addModifier(target.getAttribute(Attributes.MOVEMENT_SPEED));
		addModifier(target.getAttribute(Attributes.JUMP_STRENGTH));
		if (target instanceof Mob mob) {
			mob.setNoAi(true);
			if (!wasNoAi) {
				mob.addTag(STONE_TAG); // NoAI is saved: if the chunk unloads mid-stun, undo it on reload
			}
		}
		target.setDeltaMovement(0, Math.min(0, target.getDeltaMovement().y), 0);
		target.hurtMarked = true;
		level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
			target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 40, 0.3, 0.5, 0.3, 0.1);
		Fx.sound(target, net.minecraft.sounds.SoundEvents.STONE_PLACE, 1.5f, 0.6f);
		return true;
	}

	private static void addModifier(AttributeInstance attribute) {
		if (attribute != null && !attribute.hasModifier(STONE)) {
			attribute.addTransientModifier(new AttributeModifier(STONE, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
	}

	private static void release(LivingEntity entity, Stone stone) {
		if (ROOTED.containsKey(entity.getUUID())) {
			if (entity instanceof Mob mob) {
				mob.setNoAi(stone.wasNoAi());
				mob.removeTag(STONE_TAG);
			}
			return; // still rooted by a Vine Snare; the root releases the attributes
		}
		var speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		var jump = entity.getAttribute(Attributes.JUMP_STRENGTH);
		if (speed != null) speed.removeModifier(STONE);
		if (jump != null) jump.removeModifier(STONE);
		if (entity instanceof Mob mob) {
			mob.setNoAi(stone.wasNoAi());
			mob.removeTag(STONE_TAG);
		}
	}

	/** Breaking free of the stone hurts: 4 hearts by default (headOfMedusa.shatterDamage). */
	private static void shatter(LivingEntity living, Stone stone) {
		float damage = (float) ConfigManager.get().headOfMedusa.shatterDamage;
		if (damage <= 0) {
			return;
		}
		ServerLevel level = stone.level();
		Entity source = stone.source() == null ? null : level.getEntity(stone.source());
		living.hurtServer(level, level.damageSources().indirectMagic(source, source), damage);
		level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
			living.getX(), living.getY() + living.getBbHeight() / 2, living.getZ(), 60, 0.35, 0.6, 0.35, 0.15);
		Fx.sound(living, net.minecraft.sounds.SoundEvents.STONE_BREAK, 1.5f, 0.7f);
	}

	public static void register() {
		// A mob saved while petrified (chunk unloaded, server stopped) wakes up when it loads again.
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof Mob mob && mob.getTags().contains(STONE_TAG) && !isPetrified(mob)) {
				mob.removeTag(STONE_TAG);
				mob.setNoAi(false);
			}
		});
		// Petrified attackers can't deal damage.
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) ->
			!(source.getEntity() != null && source.getDirectEntity() == source.getEntity() && isPetrified(source.getEntity())));
	}

	public static void tick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, Stone>> it = PETRIFIED.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Stone> e = it.next();
			Entity entity = e.getValue().level().getEntity(e.getKey());
			if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
				it.remove();
				continue;
			}
			if (e.getValue().level().getGameTime() >= e.getValue().until()) {
				release(living, e.getValue());
				it.remove();
				shatter(living, e.getValue());
			} else if (e.getValue().level().getGameTime() % 5 == 0) {
				e.getValue().level().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
					living.getX(), living.getY() + 0.2, living.getZ(), 3, 0.2, 0.1, 0.2, 0.0);
			}
		}
		Iterator<Map.Entry<UUID, Stone>> roots = ROOTED.entrySet().iterator();
		while (roots.hasNext()) {
			Map.Entry<UUID, Stone> e = roots.next();
			Entity entity = e.getValue().level().getEntity(e.getKey());
			if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
				roots.remove();
			} else if (e.getValue().level().getGameTime() >= e.getValue().until()) {
				if (!PETRIFIED.containsKey(e.getKey())) {
					var speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
					var jump = living.getAttribute(Attributes.JUMP_STRENGTH);
					if (speed != null) speed.removeModifier(STONE);
					if (jump != null) jump.removeModifier(STONE);
				}
				roots.remove();
			}
		}
		long now = server.overworld().getGameTime();
		IMMUNE_UNTIL.values().removeIf(t -> now >= t);
		LAST_BLOCK_FX.values().removeIf(t -> now - t > 200);
	}
}
