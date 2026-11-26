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
			for (Player p : core.playersNear(position(), 24)) {
				Vec3 toMedusa = getEyePosition().subtract(p.getEyePosition()).normalize();
				if (p.getLookAngle().dot(toMedusa) > 0.8) {
					com.olympus.handler.PetrifyHandler.petrifyBy(p, (ServerLevel) level(), 50);
				}
			}
		}).attack("serpent_volley", t -> core.warnCircle(t.position(), 2, ParticleTypes.ITEM_SLIME), t -> {
			for (Player p : core.playersNear(position(), 14)) {
				Fx.line((ServerLevel) level(), ParticleTypes.ITEM_SLIME, getEyePosition(), p.getEyePosition(), 0.5);
				p.hurtServer((ServerLevel) level(), core.bossDamage(), 4f);
				p.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1), this);
			}
		}).attack("stone_snakes", t -> core.warnCircle(position(), 3, ModParticles.TITAN_DUST),
			t -> core.summonMinions(EntityType.SILVERFISH, 4));
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
