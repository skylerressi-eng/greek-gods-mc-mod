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
import net.minecraft.world.entity.monster.Witch;

/** Medusa, whose gaze turns heroes to stone. Look away when she glares! Placeholder model: the vanilla Witch, tinted and scaled up (see BossRenderers). */
public class MedusaEntity extends Witch implements OlympusBoss {
	public MedusaEntity(EntityType<? extends Witch> type, Level level) {
		super(type, level);
		this.core = new BossCore(this, BossEvent.BossBarColor.GREEN);
		setPersistenceRequired();
		this.xpReward = 200;

		core.attack("petrifying_gaze", t -> {
			for (Player p : core.playersNear(position(), 24)) {
				if (p instanceof net.minecraft.server.level.ServerPlayer sp) {
					sp.displayClientMessage(net.minecraft.network.chat.Component.translatable("olympus.message.look_away").withStyle(net.minecraft.ChatFormatting.DARK_GREEN), true);
				}
			}
			Fx.burst((ServerLevel) level(), ModParticles.TITAN_DUST, getEyePosition(), 10, 0.3, 0.02);
		}, t -> {
			// Her eyes blaze for 4 seconds: anyone who looks at her during it turns to stone.
			for (int tick = 0; tick <= 80; tick += 2) {
				final int at = tick;
				core.schedule(tick + 1, () -> {
					ServerLevel sl = (ServerLevel) level();
					sl.sendParticles(ParticleTypes.GLOW, getEyePosition().x, getEyePosition().y, getEyePosition().z, 3, 0.25, 0.1, 0.25, 0.01);
					for (Player p : core.playersNear(position(), 24)) {
						if (at % 20 == 0 && p instanceof net.minecraft.server.level.ServerPlayer sp) {
							sp.displayClientMessage(net.minecraft.network.chat.Component.translatable("olympus.message.gaze_left", (80 - at) / 20)
								.withStyle(net.minecraft.ChatFormatting.DARK_GREEN, net.minecraft.ChatFormatting.BOLD), true);
						}
						Vec3 toMedusa = getEyePosition().subtract(p.getEyePosition()).normalize();
						if (p.getLookAngle().dot(toMedusa) > 0.8 && p.hasLineOfSight(this)) {
							com.olympus.handler.PetrifyHandler.petrifyBy(p, sl, 50, this);
						}
					}
				});
			}
			Fx.sound(this, net.minecraft.sounds.SoundEvents.ELDER_GUARDIAN_CURSE, 2f, 0.8f);
		}).attack("serpent_volley", t -> core.warnCircle(t.position(), 2, ParticleTypes.ITEM_SLIME), t -> {
			for (Player p : core.playersNear(position(), 14)) {
				Fx.line((ServerLevel) level(), ParticleTypes.ITEM_SLIME, getEyePosition(), p.getEyePosition(), 0.5);
				p.hurtServer((ServerLevel) level(), core.bossDamage(), 4f);
				p.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1), this);
			}
		}).attack("stone_snakes", t -> core.warnCircle(position(), 3, ModParticles.TITAN_DUST),
			t -> core.summonMinions(EntityType.SILVERFISH, 4));

		// Venom Rain: poison pools burst under every hero.
		core.attack("venom_rain", t -> {
			for (Player p : core.playersNear(position(), 24)) core.warnCircle(p.position(), 2.5, ParticleTypes.ITEM_SLIME);
		}, t -> {
			for (Player p : core.playersNear(position(), 24)) {
				core.delayedBlast(p.position(), 2.5, 7f, 20, ParticleTypes.ITEM_SLIME, ParticleTypes.ITEM_SLIME,
					v -> v.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1), this));
			}
		});
		// ULTIMATE: Gorgon's Ambush. She vanishes and strikes from behind the farthest hero.
		core.rageAttack("gorgons_ambush", t -> Fx.burst((ServerLevel) level(), ModParticles.TITAN_DUST, position().add(0, 1, 0), 30, 0.6, 0.05), t -> {
			Player far = null;
			for (Player p : core.playersNear(position(), 28)) if (far == null || p.distanceToSqr(this) > far.distanceToSqr(this)) far = p;
			if (far == null) return;
			Vec3 behind = far.position().subtract(far.getLookAngle().multiply(1, 0, 1).normalize().scale(2.5));
			Fx.burst((ServerLevel) level(), ModParticles.TITAN_DUST, position().add(0, 1, 0), 40, 0.6, 0.1);
			teleportTo(behind.x, far.getY(), behind.z);
			Fx.burst((ServerLevel) level(), ModParticles.TITAN_DUST, position().add(0, 1, 0), 40, 0.6, 0.1);
			Fx.sound(this, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, 2f, 0.6f);
			core.schedule(10, () -> {
				for (Player p : core.playersNear(position(), 7)) {
					p.hurtServer((ServerLevel) level(), core.bossDamage(), 9f);
					p.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1), this);
					Vec3 toMedusa = getEyePosition().subtract(p.getEyePosition()).normalize();
					if (p.getLookAngle().dot(toMedusa) > 0.6) com.olympus.handler.PetrifyHandler.petrifyBy(p, (ServerLevel) level(), 40);
				}
				Fx.shockwave((ServerLevel) level(), ParticleTypes.ITEM_SLIME, position(), 7);
			});
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
