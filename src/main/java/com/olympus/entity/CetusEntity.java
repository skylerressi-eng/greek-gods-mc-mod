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
import net.minecraft.world.entity.monster.ElderGuardian;

/** Cetus, the sea monster of Rise of the Deep. Placeholder model: the vanilla ElderGuardian, tinted and scaled up (see BossRenderers). */
public class CetusEntity extends ElderGuardian implements OlympusBoss {
	public CetusEntity(EntityType<? extends ElderGuardian> type, Level level) {
		super(type, level);
		this.core = new BossCore(this, BossEvent.BossBarColor.BLUE);
		setPersistenceRequired();
		this.xpReward = 200;

		core.attack("tidal_charge", t -> core.warnCircle(t.position(), 2, ModParticles.TIDE_DROP), t -> {
			Vec3 dir = t.position().subtract(position()).normalize();
			setDeltaMovement(dir.scale(2.2));
			hurtMarked = true;
			for (Player p : core.playersNear(position().add(dir.scale(4)), 5)) {
				p.hurtServer((ServerLevel) level(), core.bossDamage(), 10f);
				p.knockback(1.5, -dir.x, -dir.z);
				p.hurtMarked = true;
			}
		}).attack("whirlpool", t -> core.warnCircle(position(), 16, ParticleTypes.BUBBLE), t -> {
			for (Player p : core.playersNear(position(), 16)) {
				Vec3 pull = position().subtract(p.position()).normalize().scale(1.0);
				p.setDeltaMovement(pull);
				p.hurtMarked = true;
				p.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 120, 2), this);
			}
			Fx.shockwave((ServerLevel) level(), ParticleTypes.BUBBLE_COLUMN_UP, position(), 10);
		}).attack("call_the_drowned", t -> core.warnCircle(position(), 3, ParticleTypes.SPLASH),
			t -> core.summonMinions(EntityType.DROWNED, 3));

		// Tidal Wave: a wall of water rolls from Cetus toward you. Sidestep it.
		core.attack("tidal_wave", t -> {
			Vec3 dir = t.position().subtract(position()).multiply(1, 0, 1).normalize();
			for (int d = 2; d <= 16; d += 2) core.warnCircle(position().add(dir.scale(d)), 1.2, ParticleTypes.SPLASH);
		}, t -> {
			Vec3 dir = t.position().subtract(position()).multiply(1, 0, 1).normalize();
			Vec3 side = new Vec3(-dir.z, 0, dir.x);
			Vec3 start = position();
			java.util.Set<Player> hit = new java.util.HashSet<>();
			for (int step = 1; step <= 10; step++) {
				final Vec3 front = start.add(dir.scale(step * 2.0));
				core.schedule(step * 2, () -> {
					for (int w = -3; w <= 3; w++) Fx.burst((ServerLevel) level(), ParticleTypes.SPLASH, front.add(side.scale(w)).add(0, 1, 0), 8, 0.3, 0.2);
					for (Player p : core.playersNear(front, 4.0)) {
						if (hit.add(p)) {
							p.hurtServer((ServerLevel) level(), core.bossDamage(), 9f);
							p.setDeltaMovement(dir.x * 1.2, 0.8, dir.z * 1.2);
							p.hurtMarked = true;
						}
					}
				});
			}
			Fx.sound(this, net.minecraft.sounds.SoundEvents.GENERIC_SPLASH, 3f, 0.5f);
		});
		// ULTIMATE: Maelstrom, a whirlpool that drags everyone in for 4 seconds.
		core.rageAttack("maelstrom", t -> core.warnCircle(position(), 18, ParticleTypes.BUBBLE), t -> {
			for (int pulse = 0; pulse < 8; pulse++) {
				core.schedule(pulse * 10 + 1, () -> {
					for (Player p : core.playersNear(position(), 20)) {
						Vec3 in = position().subtract(p.position()).normalize();
						Vec3 swirl = new Vec3(-in.z, 0, in.x);
						p.setDeltaMovement(in.scale(0.6).add(swirl.scale(0.5)));
						p.hurtMarked = true;
						if (p.distanceToSqr(this) < 5 * 5) p.hurtServer((ServerLevel) level(), core.bossDamage(), 4f);
					}
					Fx.ring((ServerLevel) level(), ParticleTypes.BUBBLE_COLUMN_UP, position(), 6, 40, 0.0);
					Fx.ring((ServerLevel) level(), ParticleTypes.SPLASH, position(), 12, 50, 0.0);
				});
			}
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
}
