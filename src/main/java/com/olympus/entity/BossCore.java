package com.olympus.entity;

import com.olympus.event.EventManager;
import com.olympus.fx.Fx;
import com.olympus.handler.UnderworldAllies;
import com.olympus.registry.ModKeys;
import com.olympus.registry.ModParticles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Everything bosses share, used by composition because each boss extends a different vanilla
 * mob: boss bar, damage tracking for rewards, telegraphed attacks, anti-cheese and minions.
 * Boss attacks never break blocks.
 */
public final class BossCore {
	/** A telegraphed attack: shown for {@link #TELEGRAPH_TICKS}, then executed. */
	public record Attack(String name, Consumer<LivingEntity> telegraph, Consumer<LivingEntity> execute) {
	}

	public static final int TELEGRAPH_TICKS = 25;
	public static final String MINION_TAG = "olympus.minion";

	private final Mob boss;
	private final ServerBossEvent bar;
	private final Map<UUID, Float> damage = new HashMap<>();
	private final List<Attack> attacks = new ArrayList<>();
	private int cooldown = 60;
	private Attack pending;
	private LivingEntity pendingTarget;
	private int telegraphLeft;
	private int stuckTicks;
	private Vec3 lastPos = Vec3.ZERO;

	public BossCore(Mob boss, BossEvent.BossBarColor color) {
		this.boss = boss;
		this.bar = new ServerBossEvent(boss.getDisplayName(), color, BossEvent.BossBarOverlay.NOTCHED_10);
		this.bar.setDarkenScreen(true);
		this.bar.setPlayBossMusic(true);
		// The SCALE attribute enlarges the model, but the hitbox is sized before attributes exist:
		// recompute it so you can hit the whole body (runs on both server and client).
		boss.refreshDimensions();
	}

	public BossCore attack(String name, Consumer<LivingEntity> telegraph, Consumer<LivingEntity> execute) {
		attacks.add(new Attack(name, telegraph, execute));
		return this;
	}

	public Map<UUID, Float> damageDealt() {
		return damage;
	}

	/** Bosses ignore environmental damage they could be cheesed with. */
	public static boolean ignores(DamageSource source) {
		return source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_DROWNING) || source.is(DamageTypes.IN_WALL)
			|| source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.SWEET_BERRY_BUSH);
	}

	private float healthBeforeHit = -1;

	/** Call before applying a hit; {@link #afterHit} then credits the health actually removed. */
	public void beforeHit() {
		healthBeforeHit = boss.getHealth();
	}

	/** Credits the hit once (also called from die(), so the killing blow counts before rewards). */
	public void afterHit(DamageSource source) {
		if (healthBeforeHit >= 0) {
			record(source, healthBeforeHit - Math.max(0, boss.getHealth()));
			healthBeforeHit = -1;
		}
	}

	/** Records real damage (health actually removed) for rewards. Allies' damage counts for their owner. */
	public void record(DamageSource source, float healthLost) {
		if (healthLost <= 0) {
			return;
		}
		UUID credit = null;
		if (source.getEntity() instanceof Player p) {
			credit = p.getUUID();
		} else if (source.getEntity() instanceof Mob mob) {
			credit = UnderworldAllies.ownerOf(mob);
		}
		if (credit != null) {
			damage.merge(credit, healthLost, Float::sum);
		}
	}

	public void tick(ServerLevel level) {
		bar.setProgress(boss.getHealth() / boss.getMaxHealth());
		if (boss.tickCount % 20 == 0) {
			updateViewers(level);
			if (!EventManager.isActiveBoss(boss)) {
				boss.discard(); // reloaded after a restart, or the event was stopped
				return;
			}
			antiCheese(level);
		}

		if (pending != null) {
			if (--telegraphLeft % 5 == 0 && pendingTarget != null && pendingTarget.isAlive()) {
				pending.telegraph().accept(pendingTarget);
			}
			if (telegraphLeft <= 0) {
				if (pendingTarget != null && pendingTarget.isAlive()) {
					pending.execute().accept(pendingTarget);
				}
				pending = null;
				cooldown = 50 + boss.getRandom().nextInt(40);
			}
			return;
		}
		if (--cooldown <= 0 && !attacks.isEmpty()) {
			LivingEntity target = nearestPlayer(level, 32);
			if (target != null) {
				pending = attacks.get(boss.getRandom().nextInt(attacks.size()));
				pendingTarget = target;
				telegraphLeft = TELEGRAPH_TICKS;
				pending.telegraph().accept(target);
			} else {
				cooldown = 20;
			}
		}
	}

	private void updateViewers(ServerLevel level) {
		for (ServerPlayer player : new ArrayList<>(bar.getPlayers())) {
			if (player.level() != level || player.distanceToSqr(boss) > 96 * 96 || !player.isAlive()) {
				bar.removePlayer(player);
			}
		}
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(boss) <= 96 * 96 && !bar.getPlayers().contains(player)) {
				bar.addPlayer(player);
			}
		}
	}

	/** If no player can be reached for 10s (pillaring, boxing the boss in), the boss comes to them. */
	private void antiCheese(ServerLevel level) {
		LivingEntity target = nearestPlayer(level, 48);
		if (target == null) {
			stuckTicks = 0;
			return;
		}
		boss.setTarget(target);
		boolean moved = boss.position().distanceToSqr(lastPos) > 1.0;
		lastPos = boss.position();
		boolean far = boss.distanceToSqr(target) > 6 * 6;
		stuckTicks = !moved && far ? stuckTicks + 20 : 0;
		if (stuckTicks >= 200) {
			stuckTicks = 0;
			Vec3 to = target.position().add(level.getRandom().nextGaussian() * 2, 0.5, level.getRandom().nextGaussian() * 2);
			Fx.burst(level, ModParticles.TITAN_DUST, boss.position().add(0, 1, 0), 40, 0.6, 0.1);
			boss.teleportTo(to.x, to.y, to.z);
			Fx.burst(level, ModParticles.TITAN_DUST, boss.position().add(0, 1, 0), 40, 0.6, 0.1);
		}
	}

	public LivingEntity nearestPlayer(ServerLevel level, double range) {
		Player best = null;
		double bestDist = range * range;
		for (Player p : level.players()) {
			if (!p.isAlive() || p.isSpectator() || p.isCreative()) continue;
			double d = p.distanceToSqr(boss);
			if (d < bestDist) {
				bestDist = d;
				best = p;
			}
		}
		return best;
	}

	public List<Player> playersNear(Vec3 center, double radius) {
		return boss.level().getEntitiesOfClass(Player.class, new AABB(center, center).inflate(radius),
			p -> p.isAlive() && !p.isSpectator() && !p.isCreative() && p.position().distanceTo(center) <= radius);
	}

	public DamageSource bossDamage() {
		return boss.damageSources().mobAttack(boss);
	}

	/** Lightning strike from a boss: never hurts the boss or its minions, never starts fires. */
	public void lightning(Vec3 pos, float amount) {
		ServerLevel level = (ServerLevel) boss.level();
		LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
		if (bolt != null) {
			bolt.setVisualOnly(true);
			bolt.snapTo(pos.x, pos.y, pos.z);
			level.addFreshEntity(bolt);
		}
		var type = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ModKeys.DIVINE_LIGHTNING);
		for (Player p : playersNear(pos, 1.8)) {
			p.hurtServer(level, new DamageSource(type, boss), amount);
		}
	}

	/** A ring of particles on the ground warning where an attack will land. */
	public void warnCircle(Vec3 center, double radius, ParticleOptions particle) {
		Fx.ring((ServerLevel) boss.level(), particle, center.add(0, 0.15, 0), radius, (int) (8 + radius * 6), 0.0);
	}

	public void summonMinions(EntityType<? extends Mob> type, int count) {
		ServerLevel level = (ServerLevel) boss.level();
		for (int i = 0; i < count; i++) {
			Mob minion = type.create(level, EntitySpawnReason.MOB_SUMMONED);
			if (minion == null) continue;
			double a = Math.PI * 2 * i / count;
			minion.snapTo(boss.getX() + Math.cos(a) * 3, boss.getY(), boss.getZ() + Math.sin(a) * 3, 0, 0);
			minion.addTag(MINION_TAG);
			minion.setPersistenceRequired();
			level.addFreshEntity(minion);
			Fx.burst(level, ModParticles.TITAN_DUST, minion.position().add(0, 1, 0), 15, 0.3, 0.05);
		}
	}

	public void onRemoved() {
		bar.removeAllPlayers();
	}

	public ServerBossEvent bar() {
		return bar;
	}

	public static Component title(String id) {
		return Component.translatable("entity.olympus." + id);
	}
}
