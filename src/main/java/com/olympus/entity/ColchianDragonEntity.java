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
import net.minecraft.world.entity.monster.Zoglin;

/** The sleepless Colchian Dragon, guardian of the Golden Fleece. Placeholder model: the vanilla Zoglin, tinted and scaled up (see BossRenderers). */
public class ColchianDragonEntity extends Zoglin implements OlympusBoss {
	public ColchianDragonEntity(EntityType<? extends Zoglin> type, Level level) {
		super(type, level);
		this.core = new BossCore(this, BossEvent.BossBarColor.GREEN);
		setPersistenceRequired();
		this.xpReward = 200;

		core.attack("ground_slam", t -> core.warnCircle(position(), 6, ParticleTypes.CRIT), t -> {
			for (Player p : core.playersNear(position(), 6)) {
				p.hurtServer((ServerLevel) level(), core.bossDamage(), 10f);
				p.setDeltaMovement(p.getDeltaMovement().add(0, 0.9, 0));
				p.hurtMarked = true;
			}
			Fx.shockwave((ServerLevel) level(), ParticleTypes.EXPLOSION, position(), 5);
			Fx.sound(this, net.minecraft.sounds.SoundEvents.MACE_SMASH_GROUND_HEAVY, 2f, 0.6f);
		}).attack("venom_breath", t -> core.warnCircle(t.position(), 3, ParticleTypes.ITEM_SLIME), t -> {
			for (Player p : core.playersNear(t.position(), 4)) {
				p.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1), this);
				p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0), this);
			}
			Fx.burst((ServerLevel) level(), ParticleTypes.ITEM_SLIME, t.position().add(0, 1, 0), 60, 2, 0.05);
		}).attack("tail_sweep", t -> core.warnCircle(position(), 5, ParticleTypes.SWEEP_ATTACK), t -> {
			for (Player p : core.playersNear(position(), 5)) {
				Vec3 away = p.position().subtract(position()).normalize();
				p.knockback(2.5, -away.x, -away.z);
				p.hurtMarked = true;
				p.hurtServer((ServerLevel) level(), core.bossDamage(), 6f);
			}
		});

		// Burrow: dives underground and erupts beneath you.
		core.attack("burrow", t -> core.warnCircle(t.position(), 3.5, ParticleTypes.CRIT), t -> {
			Vec3 at = t.position();
			var dirt = new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
			Fx.burst((ServerLevel) level(), dirt, position().add(0, 0.5, 0), 60, 1.2, 0.1);
			setInvisible(true);
			core.schedule(20, () -> {
				teleportTo(at.x, at.y, at.z);
				setInvisible(false);
				for (Player p : core.playersNear(at, 3.5)) {
					p.hurtServer((ServerLevel) level(), core.bossDamage(), 11f);
					p.setDeltaMovement(p.getDeltaMovement().add(0, 1.1, 0));
					p.hurtMarked = true;
				}
				Fx.burst((ServerLevel) level(), dirt, at.add(0, 0.5, 0), 80, 1.5, 0.3);
				Fx.sound(this, net.minecraft.sounds.SoundEvents.RAVAGER_ROAR, 2f, 0.8f);
			});
		});
		// ULTIMATE: Sleepless Rampage, three charges in a row.
		core.rageAttack("sleepless_rampage", t -> core.warnCircle(position(), 4, ParticleTypes.ANGRY_VILLAGER), t -> {
			for (int c = 0; c < 3; c++) {
				core.schedule(c * 20 + 1, () -> {
					LivingEntity target = core.nearestPlayer((ServerLevel) level(), 30);
					if (target == null) return;
					Vec3 dir = target.position().subtract(position()).multiply(1, 0, 1).normalize();
					setDeltaMovement(dir.scale(2.4).add(0, 0.2, 0));
					hurtMarked = true;
					Fx.sound(this, net.minecraft.sounds.SoundEvents.HOGLIN_ATTACK, 2f, 0.6f);
					core.schedule(7, () -> {
						for (Player p : core.playersNear(position(), 3.5)) {
							p.hurtServer((ServerLevel) level(), core.bossDamage(), 9f);
							p.knockback(2.0, -dir.x, -dir.z);
							p.hurtMarked = true;
						}
						Fx.burst((ServerLevel) level(), ParticleTypes.EXPLOSION, position().add(0, 1, 0), 3, 0.5, 0);
					});
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

	@Override
	public void aiStep() {
		super.aiStep();
		// Sleepless guardian: hardens its scales when badly hurt.
		if (!level().isClientSide() && tickCount % 40 == 0 && getHealth() < getMaxHealth() * 0.3f) {
			addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 1, false, true));
		}
	}
}
