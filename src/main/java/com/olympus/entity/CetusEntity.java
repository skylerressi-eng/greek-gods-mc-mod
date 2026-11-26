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
