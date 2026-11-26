package com.olympus.registry;

import com.olympus.Olympus;
import com.olympus.entity.CerberusEntity;
import com.olympus.entity.CetusEntity;
import com.olympus.entity.ColchianDragonEntity;
import com.olympus.entity.KronosEntity;
import com.olympus.entity.MedusaEntity;
import com.olympus.entity.TyphonEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;

/** Event bosses. Each reuses a vanilla mob model for now (see BossRenderers to swap in real models). */
public final class ModEntities {
	public static final EntityType<TyphonEntity> TYPHON = register("typhon",
		EntityType.Builder.of(TyphonEntity::new, MobCategory.MONSTER).sized(0.6F, 1.8F).fireImmune().clientTrackingRange(16));
	public static final EntityType<CetusEntity> CETUS = register("cetus",
		EntityType.Builder.of(CetusEntity::new, MobCategory.MONSTER).sized(1.9975F, 1.9975F).clientTrackingRange(16));
	public static final EntityType<CerberusEntity> CERBERUS = register("cerberus",
		EntityType.Builder.of(CerberusEntity::new, MobCategory.MONSTER).sized(1.3965F, 1.4F).fireImmune().clientTrackingRange(16));
	public static final EntityType<MedusaEntity> MEDUSA = register("medusa",
		EntityType.Builder.of(MedusaEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(16));
	public static final EntityType<ColchianDragonEntity> COLCHIAN_DRAGON = register("colchian_dragon",
		EntityType.Builder.of(ColchianDragonEntity::new, MobCategory.MONSTER).sized(1.3965F, 1.4F).fireImmune().clientTrackingRange(16));
	public static final EntityType<KronosEntity> KRONOS = register("kronos",
		EntityType.Builder.of(KronosEntity::new, MobCategory.MONSTER).sized(0.7F, 2.4F).fireImmune().clientTrackingRange(16));

	private ModEntities() {
	}

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Olympus.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	private static AttributeSupplier.Builder boss(AttributeSupplier.Builder base, double scale, double damage, double speed) {
		return base.add(Attributes.MAX_HEALTH, 500.0).add(Attributes.SCALE, scale).add(Attributes.ARMOR, 12.0)
			.add(Attributes.ARMOR_TOUGHNESS, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.9).add(Attributes.FOLLOW_RANGE, 48.0)
			.add(Attributes.ATTACK_DAMAGE, damage).add(Attributes.MOVEMENT_SPEED, speed).add(Attributes.STEP_HEIGHT, 1.5);
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(TYPHON, boss(Blaze.createAttributes(), 3.0, 12, 0.3));
		FabricDefaultAttributeRegistry.register(CETUS, boss(ElderGuardian.createAttributes(), 2.0, 10, 0.4));
		FabricDefaultAttributeRegistry.register(CERBERUS, boss(Hoglin.createAttributes(), 2.0, 14, 0.35));
		FabricDefaultAttributeRegistry.register(MEDUSA, boss(Witch.createAttributes(), 1.6, 8, 0.3));
		FabricDefaultAttributeRegistry.register(COLCHIAN_DRAGON, boss(Zoglin.createAttributes(), 2.6, 16, 0.32));
		FabricDefaultAttributeRegistry.register(KRONOS, boss(WitherSkeleton.createAttributes(), 2.5, 18, 0.3));
	}
}
