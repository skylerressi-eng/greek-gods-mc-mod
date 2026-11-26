package com.olympus.util;

import com.olympus.config.ConfigManager;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** Who an ability may affect. All harmful-to-player checks go through here. */
public final class Targets {
	private Targets() {
	}

	/** May a harmful effect from {@code caster} land on {@code target}? */
	public static boolean canHarm(ServerPlayer caster, Entity target) {
		if (target == caster || !target.isAlive() || target instanceof ArmorStand) {
			return false;
		}
		if (target instanceof Player p) {
			if (p.isCreative() || p.isSpectator() || isAlly(caster, p)) {
				return false;
			}
			return ConfigManager.get().general.abilitiesAffectPlayers && caster.canHarmPlayer(p);
		}
		// Never hurt the caster's own pets.
		if (target instanceof OwnableEntity ownable && ownable.getOwner() == caster) {
			return false;
		}
		return true;
	}

	/** Bosses resist control effects (charm, pacify, petrify). */
	public static boolean isBoss(Entity entity) {
		return entity instanceof EnderDragon || entity instanceof WitherBoss || entity instanceof Warden
			|| entity.getType().builtInRegistryHolder().is(com.olympus.registry.ModKeys.BOSSES);
	}

	public static boolean isHostile(Entity entity) {
		return entity instanceof Enemy;
	}

	public static List<LivingEntity> around(Entity center, double radius, Predicate<LivingEntity> filter) {
		AABB box = center.getBoundingBox().inflate(radius);
		double r2 = radius * radius;
		return center.level().getEntitiesOfClass(LivingEntity.class, box,
			e -> e != center && e.isAlive() && e.distanceToSqr(center) <= r2 && filter.test(e));
	}

	/**
	 * Is {@code other} an ally of {@code player}? Yourself, players on your scoreboard team, and
	 * players you added with /ally. Allies get your buffs and are never hit by your powers.
	 */
	public static boolean isAlly(Player player, Player other) {
		if (player == other) {
			return true;
		}
		if (player.getTeam() != null && player.getTeam() == other.getTeam()) {
			return true;
		}
		return com.olympus.data.PlayerOlympusData.get(player).allies().contains(other.getUUID());
	}

	/** Allied players near the caster, including the caster (for buffs). Enemies never get them. */
	public static List<ServerPlayer> alliesAround(ServerPlayer caster, double radius) {
		double r2 = radius * radius;
		return caster.level().getEntitiesOfClass(ServerPlayer.class, caster.getBoundingBox().inflate(radius),
			p -> p.isAlive() && !p.isSpectator() && p.distanceToSqr(caster) <= r2 && isAlly(caster, p));
	}
}
