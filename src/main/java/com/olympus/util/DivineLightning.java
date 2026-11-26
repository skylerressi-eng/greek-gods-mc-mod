package com.olympus.util;

import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.registry.ModKeys;
import com.olympus.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.Vec3;

/**
 * Lightning from Olympus items. The bolt itself is visual only; damage is applied here so that it
 * never hurts the caster, never converts mobs (no charged creepers / witches), respects
 * {@code abilitiesAffectPlayers}, credits kills to the caster and only starts fires if
 * {@code lightning.lightningFire} is enabled.
 */
public final class DivineLightning {
	private DivineLightning() {
	}

	public static void strike(ServerLevel level, Vec3 pos, ServerPlayer caster, float damage, double radius) {
		LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
		if (bolt != null) {
			bolt.setVisualOnly(true);
			bolt.snapTo(pos.x, pos.y, pos.z);
			level.addFreshEntity(bolt);
		}
		Fx.burst(level, ModParticles.STORM_SPARK, pos.add(0, 0.5, 0), 30, 0.6, 0.15);

		DamageSource source = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ModKeys.DIVINE_LIGHTNING), caster);
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(pos, pos).inflate(radius, 3.0, radius))) {
			if (!Targets.canHarm(caster, target)) {
				continue;
			}
			target.hurtServer(level, source, damage);
			target.igniteForSeconds(3.0f);
		}

		if (ConfigManager.get().lightning.lightningFire) {
			BlockPos firePos = BlockPos.containing(pos);
			if (level.getBlockState(firePos).isAir()) {
				var fire = BaseFireBlock.getState(level, firePos);
				if (fire.canSurvive(level, firePos)) {
					level.setBlockAndUpdate(firePos, fire);
				}
			}
		}
	}
}
