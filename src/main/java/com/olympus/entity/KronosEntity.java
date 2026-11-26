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
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;

/** Kronos, King of the Titans, final foe of the Titanomachy. Placeholder model: the vanilla WitherSkeleton, tinted and scaled up (see BossRenderers). */
public class KronosEntity extends WitherSkeleton implements OlympusBoss {
	public KronosEntity(EntityType<? extends WitherSkeleton> type, Level level) {
		super(type, level);
		this.core = new BossCore(this, BossEvent.BossBarColor.PURPLE);
		setPersistenceRequired();
		this.xpReward = 200;

		setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_HOE));
		setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0f);
		core.attack("time_stop", t -> core.warnCircle(position(), 16, ModParticles.TITAN_DUST), t -> {
			for (Player p : core.playersNear(position(), 16)) {
				p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 3), this);
				p.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 70, 2), this);
			}
			Fx.sphere((ServerLevel) level(), ModParticles.TITAN_DUST, position().add(0, 1, 0), 6, 150);
			Fx.sound(this, net.minecraft.sounds.SoundEvents.BELL_RESONATE, 3f, 0.4f);
		}).attack("scythe_sweep", t -> core.warnCircle(position(), 5, ParticleTypes.SWEEP_ATTACK), t -> {
			for (Player p : core.playersNear(position(), 5)) {
				p.hurtServer((ServerLevel) level(), core.bossDamage(), 12f);
				Vec3 away = p.position().subtract(position()).normalize();
				p.knockback(1.5, -away.x, -away.z);
				p.hurtMarked = true;
			}
			Fx.shockwave((ServerLevel) level(), ParticleTypes.SWEEP_ATTACK, position().add(0, 1, 0), 4);
		}).attack("echoes", t -> core.warnCircle(position(), 3, ModParticles.TITAN_DUST),
			t -> core.summonMinions(EntityType.WITHER_SKELETON, 3));
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

	private boolean rewound;

	@Override
	public void aiStep() {
		super.aiStep();
		// Once, at 30% health, Kronos turns back his own clock.
		if (!level().isClientSide() && !rewound && getHealth() < getMaxHealth() * 0.3f) {
			rewound = true;
			heal(getMaxHealth() * 0.2f);
			Fx.sphere((ServerLevel) level(), ModParticles.TITAN_DUST, position().add(0, 1.5, 0), 3, 120);
			Fx.sound(this, net.minecraft.sounds.SoundEvents.BELL_RESONATE, 3f, 1.5f);
		}
	}
}
