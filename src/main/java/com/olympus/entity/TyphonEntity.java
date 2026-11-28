package com.olympus.entity;

import com.olympus.entity.BossCore;
import com.olympus.event.EventManager;
import com.olympus.fx.Fx;
import com.olympus.registry.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.monster.Blaze;

/** Typhon, father of monsters, the storm giant of Wrath of Zeus. Placeholder model: the vanilla Blaze, tinted and scaled up (see BossRenderers). */
public class TyphonEntity extends Blaze implements OlympusBoss {
	public TyphonEntity(EntityType<? extends Blaze> type, Level level) {
		super(type, level);
		this.core = new BossCore(this, BossEvent.BossBarColor.YELLOW);
		setPersistenceRequired();
		this.xpReward = 200;

		// Typhon fights in melee and with storms. (The blaze's fireball goal is removed: fireballs could burn blocks.)
		core.attack("lightning_ring", t -> {
			for (Player p : core.playersNear(t.position(), 24)) core.warnCircle(p.position(), 1.5, ModParticles.STORM_SPARK);
		}, t -> {
			for (Player p : core.playersNear(t.position(), 24)) core.lightning(p.position(), 8f);
		}).attack("storm_pull", t -> core.warnCircle(position(), 20, ParticleTypes.CLOUD), t -> {
			for (Player p : core.playersNear(position(), 20)) {
				Vec3 pull = position().subtract(p.position()).normalize().scale(1.4);
				p.setDeltaMovement(pull.x, 0.4, pull.z);
				p.hurtMarked = true;
				p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), this);
			}
			Fx.sound(this, net.minecraft.sounds.SoundEvents.BREEZE_INHALE, 2f, 0.5f);
		}).attack("thunder_slam", t -> core.warnCircle(t.position(), 4, ModParticles.STORM_SPARK), t -> {
			for (int i = 0; i < 5; i++) {
				double a = Math.PI * 2 * i / 5;
				core.lightning(t.position().add(Math.cos(a) * 2.5, 0, Math.sin(a) * 2.5), 6f);
			}
			core.lightning(t.position(), 6f);
		});

		// Chain Lightning: a bolt that jumps from player to player.
		core.attack("chain_lightning", t -> Fx.burst((ServerLevel) level(), ModParticles.STORM_SPARK, t.position().add(0, 1, 0), 15, 0.4, 0.1), t -> {
			java.util.Set<Player> hit = new java.util.HashSet<>();
			LivingEntity current = t;
			for (int hop = 0; hop < 4 && current != null; hop++) {
				final LivingEntity strike = current;
				core.schedule(hop * 5 + 1, () -> core.lightning(strike.position(), 7f));
				if (strike instanceof Player sp) hit.add(sp);
				LivingEntity next = null;
				double best = 10 * 10;
				for (Player p : core.playersNear(strike.position(), 10)) {
					if (!hit.contains(p) && p.distanceToSqr(strike) < best) {
						best = p.distanceToSqr(strike);
						next = p;
					}
				}
				if (next != null) Fx.line((ServerLevel) level(), ModParticles.STORM_SPARK, strike.position().add(0, 1, 0), next.position().add(0, 1, 0), 0.3);
				current = next;
			}
		});
		// ULTIMATE: Eye of the Storm, rings of lightning rolling outward.
		core.rageAttack("eye_of_the_storm", t -> {
			core.warnCircle(position(), 3, ModParticles.STORM_SPARK);
			Fx.pillar((ServerLevel) level(), ModParticles.STORM_SPARK, position(), 12, 30);
		}, t -> {
			core.expandingRings(position(), 6, 3.0, 9f);
			Fx.sound(this, net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER, 3f, 0.6f);
		});
	}

	private final BossCore core;

	public BossCore core() {
		return core;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		core.tick(level);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (BossCore.ignores(source)) {
			return false;
		}
		core.beforeHit();
		boolean hurt = super.hurtServer(level, source, amount);
		core.afterHit(source);
		return hurt;
	}

	@Override
	public void die(DamageSource source) {
		core.afterHit(source); // credit the killing blow before rewards
		super.die(source);
		if (level() instanceof ServerLevel) {
			EventManager.onBossDeath(this, core);
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		core.onRemoved();
		super.remove(reason);
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	protected boolean canRide(Entity entity) {
		return false;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.2, true));
		this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 16f));
		this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
	}
}
