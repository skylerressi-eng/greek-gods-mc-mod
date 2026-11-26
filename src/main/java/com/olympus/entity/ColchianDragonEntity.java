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
