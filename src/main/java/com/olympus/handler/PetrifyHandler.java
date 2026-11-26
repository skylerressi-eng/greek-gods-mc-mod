package com.olympus.handler;

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
	private record Stone(long until, ServerLevel level, boolean wasNoAi) {
	}

	private static final Map<UUID, Stone> PETRIFIED = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> IMMUNE_UNTIL = new ConcurrentHashMap<>();

	private PetrifyHandler() {
	}

	public static boolean isPetrified(Entity entity) {
		return PETRIFIED.containsKey(entity.getUUID());
	}

	public static boolean petrify(LivingEntity target, ServerPlayer source, int ticks) {
		return petrifyBy(target, source.level(), ticks);
	}

	/** Petrify from any source (players with the Head of Medusa, or Medusa herself). */
	public static boolean petrifyBy(LivingEntity target, ServerLevel level, int ticks) {
		if (Targets.isBoss(target) || isPetrified(target)) {
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
		PETRIFIED.put(target.getUUID(), new Stone(now + ticks, level, wasNoAi));
		addModifier(target.getAttribute(Attributes.MOVEMENT_SPEED));
		addModifier(target.getAttribute(Attributes.JUMP_STRENGTH));
		if (target instanceof Mob mob) {
			mob.setNoAi(true);
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
		var speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		var jump = entity.getAttribute(Attributes.JUMP_STRENGTH);
		if (speed != null) speed.removeModifier(STONE);
		if (jump != null) jump.removeModifier(STONE);
		if (entity instanceof Mob mob) {
			mob.setNoAi(stone.wasNoAi());
		}
	}

	public static void register() {
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
			} else if (e.getValue().level().getGameTime() % 5 == 0) {
				e.getValue().level().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
					living.getX(), living.getY() + 0.2, living.getZ(), 3, 0.2, 0.1, 0.2, 0.0);
			}
		}
		long now = server.overworld().getGameTime();
		IMMUNE_UNTIL.values().removeIf(t -> now >= t);
	}
}
