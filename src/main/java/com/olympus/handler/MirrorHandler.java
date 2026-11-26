package com.olympus.handler;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/**
 * Mirror of Aphrodite primary: pacified mobs refuse to target the player who used the mirror
 * until the effect wears off ({@code MobMixin} blocks {@code setTarget}).
 */
public final class MirrorHandler {
	private record Pacify(UUID player, long until, ServerLevel level) {
	}

	private static final Map<UUID, Pacify> PACIFIED = new ConcurrentHashMap<>();

	private MirrorHandler() {
	}

	public static void pacify(Mob mob, ServerPlayer player, int ticks) {
		PACIFIED.put(mob.getUUID(), new Pacify(player.getUUID(), player.level().getGameTime() + ticks, player.level()));
		dropTarget(mob, player);
	}

	/** @return true if {@code mob} must not target {@code target} right now. */
	public static boolean isPacifiedAgainst(Mob mob, LivingEntity target) {
		if (target == null || PACIFIED.isEmpty()) {
			return false;
		}
		Pacify p = PACIFIED.get(mob.getUUID());
		return p != null && p.player().equals(target.getUUID()) && mob.level().getGameTime() < p.until();
	}

	private static void dropTarget(Mob mob, ServerPlayer player) {
		if (mob.getTarget() == player) {
			mob.setTarget(null);
		}
		if (mob instanceof NeutralMob neutral) {
			neutral.stopBeingAngry();
		}
		// Brain-driven mobs (piglins, hoglins, zoglins...) keep their target in memory.
		if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
			&& mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter(t -> t == player).isPresent()) {
			mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
		}
	}

	/** Runs once a second: keeps brain-driven mobs calm and forgets expired entries. */
	public static void tick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, Pacify>> it = PACIFIED.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Pacify> entry = it.next();
			Pacify p = entry.getValue();
			Entity entity = p.level().getEntity(entry.getKey());
			if (!(entity instanceof Mob mob) || !mob.isAlive() || p.level().getGameTime() >= p.until()) {
				it.remove();
				continue;
			}
			ServerPlayer player = server.getPlayerList().getPlayer(p.player());
			if (player != null) {
				dropTarget(mob, player);
			}
		}
	}
}
