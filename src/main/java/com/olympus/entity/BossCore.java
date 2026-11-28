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
import net.minecraft.core.particles.ParticleTypes;
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
	/** Ultimate attacks unlocked in the rage phase (below half health). */
	private final List<Attack> rageAttacks = new ArrayList<>();
	private boolean enraged;
	private Attack last;
	/** Delayed follow-ups (waves, aftershocks, rewinds): ticks left → action. */
	private final List<Object[]> scheduled = new ArrayList<>();
	private static final net.minecraft.resources.Identifier RAGE_SPEED = com.olympus.Olympus.id("boss_rage_speed");
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

	/** An ultimate only used once the boss is enraged (below half health). */
	public BossCore rageAttack(String name, Consumer<LivingEntity> telegraph, Consumer<LivingEntity> execute) {
		rageAttacks.add(new Attack(name, telegraph, execute));
		return this;
	}

	/** Runs {@code action} after {@code ticks} ticks (if the boss is still alive). */
	public void schedule(int ticks, Runnable action) {
		scheduled.add(new Object[]{ticks, action});
	}

	/** All attack names (normal + rage). */
	public List<String> attackNames() {
		List<String> names = new ArrayList<>();
		for (Attack a : attacks) names.add(a.name());
		for (Attack a : rageAttacks) names.add(a.name());
		return names;
	}

	/** Runs one attack right now on {@code target} (tests and debugging). */
	public void forceAttack(String name, LivingEntity target) {
		for (List<Attack> list : List.of(attacks, rageAttacks)) {
			for (Attack a : list) {
				if (a.name().equals(name)) {
					a.telegraph().accept(target);
					a.execute().accept(target);
					return;
				}
			}
		}
		throw new IllegalArgumentException("No attack " + name);
	}

	/** Runs every pending follow-up now, as if {@code ticks} ticks had passed. */
	public void flushScheduled(int ticks) {
		for (int i = 0; i < ticks && !scheduled.isEmpty(); i++) {
			List<Object[]> due = new ArrayList<>();
			for (Object[] task : new ArrayList<>(scheduled)) {
				task[0] = (Integer) task[0] - 1;
				if ((Integer) task[0] <= 0) due.add(task);
			}
			scheduled.removeAll(due);
			for (Object[] task : due) ((Runnable) task[1]).run();
		}
	}

	public boolean enraged() {
		return enraged;
	}

	public Mob boss() {
		return boss;
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

		// Delayed follow-ups
		if (!scheduled.isEmpty()) {
			List<Object[]> due = new ArrayList<>();
			for (Object[] task : scheduled) {
				task[0] = (Integer) task[0] - 1;
				if ((Integer) task[0] <= 0) due.add(task);
			}
			scheduled.removeAll(due);
			for (Object[] task : due) {
				if (boss.isAlive()) ((Runnable) task[1]).run();
			}
		}

		// Rage phase at half health
		if (!enraged && boss.getHealth() <= boss.getMaxHealth() * 0.5f) {
			enrage(level);
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
				cooldown = enraged ? 30 + boss.getRandom().nextInt(25) : 50 + boss.getRandom().nextInt(40);
			}
			return;
		}
		if (--cooldown <= 0 && !attacks.isEmpty()) {
			LivingEntity target = nearestPlayer(level, 32);
			if (target != null) {
				List<Attack> pool = new ArrayList<>(attacks);
				if (enraged) {
					pool.addAll(rageAttacks);
					pool.addAll(rageAttacks); // ultimates come up often once enraged
				}
				if (pool.size() > 1 && last != null) pool.removeIf(a -> a == last); // never the same attack twice in a row
				pending = pool.get(boss.getRandom().nextInt(pool.size()));
				last = pending;
				pendingTarget = target;
				telegraphLeft = TELEGRAPH_TICKS;
				announce(pending, rageAttacks.contains(pending));
				pending.telegraph().accept(target);
			} else {
				cooldown = 20;
			}
		}
	}

	/** Shows the incoming attack's name to everyone fighting, so it can be dodged. */
	private void announce(Attack attack, boolean ultimate) {
		Component name = Component.translatable("olympus.boss_attack." + attack.name())
			.withStyle(ultimate ? net.minecraft.ChatFormatting.DARK_RED : net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD);
		Component line = Component.literal("⚠ ").withStyle(net.minecraft.ChatFormatting.RED).append(name);
		for (ServerPlayer p : bar.getPlayers()) {
			p.displayClientMessage(line, true);
		}
	}

	private void enrage(ServerLevel level) {
		enraged = true;
		bar.setColor(BossEvent.BossBarColor.RED);
		var speed = boss.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
		if (speed != null && !speed.hasModifier(RAGE_SPEED)) {
			speed.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(RAGE_SPEED, 0.3,
				net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		}
		for (ServerPlayer p : bar.getPlayers()) {
			p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(5, 40, 15));
			p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.empty()));
			p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
				Component.translatable("olympus.boss.enraged", boss.getDisplayName()).withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD)));
		}
		// A roar that throws everyone close back.
		for (Player p : playersNear(boss.position(), 7)) {
			Vec3 away = p.position().subtract(boss.position()).normalize();
			p.knockback(1.6, -away.x, -away.z);
			p.hurtMarked = true;
		}
		Fx.shockwave(level, ParticleTypes.FLAME, boss.position().add(0, 0.3, 0), 7);
		Fx.sphere(level, ModParticles.DIVINE_GLINT, boss.position().add(0, boss.getBbHeight() / 2, 0), boss.getBbWidth() + 1.5, 120);
		Fx.sound(boss, net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL, 3f, 0.7f);
		cooldown = 20;
	}

	/** A ring of lightning that expands outward in waves: stand in the gaps or get out. */
	public void expandingRings(Vec3 center, int waves, double spacing, float damage) {
		for (int w = 1; w <= waves; w++) {
			final double r = w * spacing;
			schedule(w * 8, () -> {
				int points = (int) Math.max(6, r * 2.2);
				for (int i = 0; i < points; i++) {
					double a = Math.PI * 2 * i / points;
					Vec3 at = center.add(Math.cos(a) * r, 0, Math.sin(a) * r);
					if (i % 3 == 0) lightning(at, damage);
					else for (Player p : playersNear(at, 1.4)) p.hurtServer((ServerLevel) boss.level(), bossDamage(), damage * 0.6f);
				}
				Fx.ring((ServerLevel) boss.level(), ModParticles.STORM_SPARK, center.add(0, 0.2, 0), r, points * 2, 0.0);
			});
		}
	}

	/** Damages players standing in a circle after a delay (meteors, eruptions, venom pools). */
	public void delayedBlast(Vec3 at, double radius, float damage, int delay, ParticleOptions warn, ParticleOptions boom,
		Consumer<Player> extra) {
		warnCircle(at, radius, warn);
		schedule(delay / 2, () -> warnCircle(at, radius, warn));
		schedule(delay, () -> {
			for (Player p : playersNear(at, radius)) {
				p.hurtServer((ServerLevel) boss.level(), bossDamage(), damage);
				if (extra != null) extra.accept(p);
			}
			Fx.burst((ServerLevel) boss.level(), boom, at.add(0, 0.5, 0), 25, radius * 0.4, 0.05);
			Fx.sound((ServerLevel) boss.level(), at, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(), 0.8f, 1.2f);
		});
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
