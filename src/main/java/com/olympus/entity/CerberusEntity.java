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
import net.minecraft.world.entity.monster.hoglin.Hoglin;

/** Cerberus, the three-headed hound guarding the Gates of the Underworld. Placeholder model: the vanilla Hoglin, tinted and scaled up (see BossRenderers). */
public class CerberusEntity extends Hoglin implements OlympusBoss {
	public CerberusEntity(EntityType<? extends Hoglin> type, Level level) {
		super(type, level);
		this.core = new BossCore(this, BossEvent.BossBarColor.RED);
		setPersistenceRequired();
		this.xpReward = 200;

		setImmuneToZombification(true);
		core.attack("hellfire_breath", t -> {
			Vec3 dir = t.position().subtract(position()).normalize();
			for (int d = 2; d <= 8; d += 2) core.warnCircle(position().add(dir.scale(d)), 1.0, ParticleTypes.FLAME);
		}, t -> {
			Vec3 dir = t.position().subtract(position()).normalize();
			for (Player p : core.playersNear(position(), 9)) {
				if (p.position().subtract(position()).normalize().dot(dir) > 0.7) {
					p.hurtServer((ServerLevel) level(), core.bossDamage(), 7f);
					p.igniteForSeconds(5);
				}
			}
			for (int d = 1; d <= 8; d++) Fx.burst((ServerLevel) level(), ParticleTypes.FLAME, position().add(dir.scale(d)).add(0, 1, 0), 12, 0.4, 0.05);
		}).attack("triple_bite", t -> core.warnCircle(t.position(), 1.5, ParticleTypes.CRIT), t -> {
			if (distanceToSqr(t) < 25) {
				for (int i = 0; i < 3; i++) t.hurtServer((ServerLevel) level(), core.bossDamage(), 5f);
			}
		}).attack("howl", t -> core.warnCircle(position(), 12, ModParticles.SHADE), t -> {
			for (Player p : core.playersNear(position(), 12)) p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1), this);
			core.summonMinions(EntityType.WITHER_SKELETON, 2);
			Fx.sound(this, net.minecraft.sounds.SoundEvents.RAVAGER_ROAR, 3f, 0.5f);
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
	public boolean isFood(net.minecraft.world.item.ItemStack stack) {
		return false; // no breeding the hound of Hades
	}
}
