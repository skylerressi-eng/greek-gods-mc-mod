package com.olympus.ability;

import com.olympus.config.ConfigManager;
import com.olympus.config.OlympusConfig;
import com.olympus.fx.Fx;
import com.olympus.god.God;
import com.olympus.god.GodItem;
import com.olympus.god.UpgradeLevel;
import com.olympus.handler.MirrorHandler;
import com.olympus.item.relic.GobletItem;
import com.olympus.item.relic.SickleItem;
import com.olympus.registry.ModItems;
import com.olympus.registry.ModParticles;
import com.olympus.registry.ModSounds;
import com.olympus.util.Targets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Divine Power key (default R): fires the Blessed ability of a god item. The server picks
 * the item itself (main hand → offhand → worn armor) so the client can't spoof anything.
 */
public final class DivinePower {
	private static final Map<UUID, Long> LAST_PRESS = new ConcurrentHashMap<>();
	/** Silver Bow volley: player → game time the armed volley expires. */
	private static final Map<UUID, Long> VOLLEY = new ConcurrentHashMap<>();

	private DivinePower() {
	}

	private static OlympusConfig cfg() {
		return ConfigManager.get();
	}

	public static void onKey(ServerPlayer player) {
		long now = player.level().getGameTime();
		if (now - LAST_PRESS.getOrDefault(player.getUUID(), 0L) < 4) {
			return; // rate limit
		}
		LAST_PRESS.put(player.getUUID(), now);

		List<ItemStack> candidates = new ArrayList<>();
		candidates.add(player.getMainHandItem());
		candidates.add(player.getOffhandItem());
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			candidates.add(player.getItemBySlot(slot));
		}

		ItemStack lockedRelic = null;
		for (ItemStack stack : candidates) {
			if (!(stack.getItem() instanceof GodItem god) || god.blessedIsPassive()) {
				continue;
			}
			if (!GodItem.level(stack).atLeast(UpgradeLevel.BLESSED)) {
				if (lockedRelic == null) {
					lockedRelic = stack;
				}
				continue;
			}
			if (!Cooldowns.checkReady(player, stack, Cooldowns.Kind.BLESSED)) {
				return;
			}
			if (activate(player, stack, GodItem.level(stack))) {
				Cooldowns.start(player, stack, Cooldowns.Kind.BLESSED, god.blessedCooldownSeconds());
			}
			return;
		}
		Component msg = lockedRelic != null
			? Component.translatable("olympus.message.needs_blessing", lockedRelic.getHoverName())
			: Component.translatable("olympus.message.no_divine_power");
		player.displayClientMessage(msg.copy().withStyle(ChatFormatting.GRAY), true);
	}

	private static boolean activate(ServerPlayer player, ItemStack stack, UpgradeLevel lvl) {
		ServerLevel level = player.level();
		Item item = stack.getItem();
		if (item == ModItems.CONCH_OF_THE_DEEP) return riptideDash(player, level);
		if (item == ModItems.OWL_OF_ATHENA) return battleWisdom(player, level, lvl);
		if (item == ModItems.BLADE_OF_ARES) return warCry(player, level, lvl);
		if (item == ModItems.LYRE_OF_APOLLO) return sunburst(player, level, lvl);
		if (item == ModItems.SILVER_BOW) return armVolley(player, level);
		if (item == ModItems.HAMMER_OF_HEPHAESTUS) return forgeFire(player, level);
		if (item == ModItems.SICKLE_OF_DEMETER) return greatHarvest(player, level, lvl);
		if (item == ModItems.GOBLET_OF_DIONYSUS) return wildParty(player, level, lvl);
		if (item == ModItems.MIRROR_OF_APHRODITE) return charm(player, level, lvl);
		if (item == ModItems.CADUCEUS) return waypoint(player, level);
		if (item == ModItems.SPEAR_OF_ATHENA) return armSpear(player, level);
		if (item == ModItems.FORGE_HAMMER) return toggleHammer(player, stack);
		if (item == ModItems.HARPE) return heroLeap(player, level);
		if (item == ModItems.THYRSUS) return vineSnare(player, level, lvl);
		if (item == ModItems.CORNUCOPIA) return feast(player, level, lvl);
		if (item == ModItems.THUNDERBOLT_OF_ZEUS) {
			com.olympus.handler.LegendaryHandler.skybreaker(player, lvl);
			return true;
		}
		if (item == ModItems.TRIDENT_OF_POSEIDON) return earthshaker(player, level, lvl);
		if (item == ModItems.BIDENT_OF_HADES) {
			com.olympus.handler.UnderworldAllies.startSoulHarvest(player, lvl.durationTicks(cfg().bident.soulHarvestSeconds));
			Fx.helix(level, ModParticles.SHADE, player.position(), 0.9, 2.4, 40);
			Fx.sound(player, ModSounds.HELM_VANISH, 1.2f, 0.5f);
			player.displayClientMessage(Component.translatable("olympus.message.soul_harvest").withStyle(God.HADES.style()), true);
			return true;
		}
		if (item == ModItems.HEAD_OF_MEDUSA) return gorgonsGaze(player, level, lvl);
		if (item == ModItems.SCYTHE_OF_KRONOS) return com.olympus.handler.LegendaryHandler.rewind(player);
		return false;
	}

	// ─── Poseidon: Riptide Dash ───
	private static boolean riptideDash(ServerPlayer player, ServerLevel level) {
		double strength = cfg().conch.riptideStrength;
		Vec3 look = player.getLookAngle();
		player.setDeltaMovement(look.scale(strength).add(0, 0.25, 0));
		player.hurtMarked = true;
		player.startAutoSpinAttack(20, 8.0f, player.getMainHandItem());
		player.resetFallDistance();
		Fx.sound(player, net.minecraft.sounds.SoundEvents.TRIDENT_RIPTIDE_3.value(), 1.0f, 1.0f);
		Fx.burst(level, ModParticles.TIDE_DROP, player.position().add(0, 1, 0), 50, 0.5, 0.2);
		Fx.burst(level, ParticleTypes.SPLASH, player.position().add(0, 1, 0), 40, 0.6, 0.2);
		return true;
	}

	// ─── Athena: Battle Wisdom ───
	private static boolean battleWisdom(ServerPlayer player, ServerLevel level, UpgradeLevel lvl) {
		int ticks = lvl.durationTicks(cfg().owl.battleWisdomSeconds);
		for (ServerPlayer ally : Targets.alliesAround(player, lvl.radius(cfg().owl.battleWisdomRadius))) {
			ally.addEffect(new MobEffectInstance(MobEffects.STRENGTH, ticks, 0, false, true, true), player);
			ally.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, ticks, 0, false, true, true), player);
			Fx.helix(level, ModParticles.OWL_FEATHER, ally.position(), 0.7, 2.2, 24);
		}
		Fx.sound(player, ModSounds.OWL_HOOT, 1.4f, 0.8f);
		Fx.shockwave(level, ModParticles.OWL_FEATHER, player.position().add(0, 0.2, 0), 4.0);
		return true;
	}

	// ─── Ares: War Cry ───
	private static boolean warCry(ServerPlayer player, ServerLevel level, UpgradeLevel lvl) {
		double radius = lvl.radius(cfg().bladeOfAres.warCryRadius);
		int ticks = lvl.durationTicks(cfg().bladeOfAres.warCrySeconds);
		for (LivingEntity e : Targets.around(player, radius, e -> Targets.canHarm(player, e) && (Targets.isHostile(e) || e instanceof Player))) {
			e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 1), player);
			level.sendParticles(ParticleTypes.ANGRY_VILLAGER, e.getX(), e.getEyeY() + 0.4, e.getZ(), 2, 0.2, 0.1, 0.2, 0);
		}
		for (ServerPlayer ally : Targets.alliesAround(player, radius)) {
			ally.addEffect(new MobEffectInstance(MobEffects.SPEED, ticks, 1, false, true, true), player);
		}
		Fx.sound(player, ModSounds.ARES_FURY_II, 1.5f, 0.7f);
		Fx.shockwave(level, ModParticles.WAR_EMBER, player.position().add(0, 0.2, 0), radius);
		return true;
	}

	// ─── Apollo: Sunburst ───
	private static boolean sunburst(ServerPlayer player, ServerLevel level, UpgradeLevel lvl) {
		double radius = lvl.radius(cfg().lyre.sunburstRadius);
		int blind = lvl.durationTicks(cfg().lyre.sunburstBlindSeconds);
		for (LivingEntity e : Targets.around(player, radius, e -> Targets.canHarm(player, e) && (Targets.isHostile(e) || e instanceof Player))) {
			e.igniteForSeconds(cfg().lyre.sunburstFireSeconds);
			if (e instanceof Player) {
				e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blind, 0), player);
			} else if (e instanceof Mob mob && !Targets.isBoss(mob)) {
				MirrorHandler.pacify(mob, player, blind); // blinded mobs lose you
			}
		}
		Fx.sound(player, ModSounds.LYRE_SHIMMER, 1.5f, 0.6f);
		Fx.sphere(level, ModParticles.SUN_MOTE, player.position().add(0, 1, 0), radius * 0.6, 120);
		Fx.shockwave(level, ParticleTypes.FLAME, player.position().add(0, 0.2, 0), radius);
		return true;
	}

	// ─── Artemis: Volley ───
	private static boolean armVolley(ServerPlayer player, ServerLevel level) {
		VOLLEY.put(player.getUUID(), level.getGameTime() + cfg().silverBow.volleyWindowSeconds * 20L);
		Fx.sound(player, ModSounds.SILVER_MARK, 1.0f, 0.8f);
		Fx.helix(level, ModParticles.MOONBEAM, player.position(), 0.6, 2.0, 30);
		player.displayClientMessage(Component.translatable("olympus.message.volley_ready").withStyle(God.ARTEMIS.style()), true);
		return true;
	}

	/** Called by the Silver Bow after a shot: consumes an armed volley. */
	public static boolean consumeVolley(Player player) {
		Long until = VOLLEY.remove(player.getUUID());
		return until != null && player.level().getGameTime() <= until;
	}

	// ─── Hephaestus: Forge Fire ───
	private static boolean forgeFire(ServerPlayer player, ServerLevel level) {
		ItemStack off = player.getOffhandItem();
		var recipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(off), level);
		if (off.isEmpty() || recipe.isEmpty()) {
			player.displayClientMessage(Component.translatable("olympus.message.forge_nothing").withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		int count = Math.min(off.getCount(), cfg().hammerOfHephaestus.forgeFireMaxItems);
		ItemStack one = recipe.get().value().assemble(new SingleRecipeInput(off), level.registryAccess());
		float xp = recipe.get().value().experience() * count;
		off.shrink(count);
		ItemStack result = one.copyWithCount(one.getCount() * count);
		if (!player.getInventory().add(result)) {
			player.drop(result, false);
		}
		ExperienceOrb.award(level, player.position(), Math.round(xp));
		Fx.sound(player, ModSounds.HEPHAESTUS_SMELT, 1.2f, 0.8f);
		Fx.burst(level, ModParticles.FORGE_SPARK, player.position().add(0, 1, 0), 40, 0.5, 0.15);
		Fx.burst(level, ParticleTypes.LAVA, player.position().add(0, 1, 0), 8, 0.4, 0.1);
		return true;
	}

	// ─── Demeter: Great Harvest ───
	private static boolean greatHarvest(ServerPlayer player, ServerLevel level, UpgradeLevel lvl) {
		int r = (int) Math.round(lvl.radius(cfg().sickle.harvestRadius));
		BlockPos center = player.blockPosition();
		int harvested = 0;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -2, -r), center.offset(r, 2, r))) {
			BlockState state = level.getBlockState(pos);
			if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) {
				continue;
			}
			BlockPos at = pos.immutable();
			List<ItemStack> drops = Block.getDrops(state, level, at, null, player, player.getMainHandItem());
			Item seed = state.getBlock().asItem(); // the seed (wheat seeds, carrot, potato, ...)
			boolean replanted = false;
			for (ItemStack drop : drops) {
				if (!replanted && drop.is(seed)) {
					drop.shrink(1); // one seed goes back into the ground
					replanted = true;
				}
				if (!drop.isEmpty()) {
					Block.popResource(level, at, drop);
				}
			}
			level.setBlock(at, crop.getStateForAge(0), Block.UPDATE_ALL);
			level.sendParticles(ModParticles.GRAIN_LEAF, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.02);
			harvested++;
		}
		if (harvested == 0) {
			player.displayClientMessage(Component.translatable("olympus.message.harvest_nothing").withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		Fx.sound(player, ModSounds.SICKLE_BLOOM, 1.2f, 0.9f);
		Fx.shockwave(level, ModParticles.GRAIN_LEAF, player.position().add(0, 0.2, 0), r);
		return true;
	}

	// ─── Dionysus: Wild Party ───
	private static boolean wildParty(ServerPlayer player, ServerLevel level, UpgradeLevel lvl) {
		double radius = lvl.radius(cfg().goblet.wildPartyRadius);
		int ticks = lvl.durationTicks(cfg().goblet.wildPartySeconds);
		for (ServerPlayer ally : Targets.alliesAround(player, radius)) {
			ally.addEffect(new MobEffectInstance(GobletItem.randomEffect(level.getRandom()), ticks, 0, false, true, true), player);
		}
		for (LivingEntity e : Targets.around(player, radius, e -> Targets.canHarm(player, e) && (Targets.isHostile(e) || e instanceof Player))) {
			e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 1), player);
			e.addEffect(new MobEffectInstance(e instanceof Player ? MobEffects.NAUSEA : MobEffects.WEAKNESS, ticks, 0), player);
			level.sendParticles(ModParticles.WINE_DROP, e.getX(), e.getEyeY() + 0.3, e.getZ(), 6, 0.2, 0.2, 0.2, 0.02);
		}
		Fx.sound(player, ModSounds.GOBLET_HIC, 1.4f, 0.9f);
		Fx.shockwave(level, ModParticles.WINE_DROP, player.position().add(0, 0.3, 0), radius);
		Fx.burst(level, ParticleTypes.NOTE, player.position().add(0, 2.2, 0), 12, 1.2, 1);
		return true;
	}

	// ─── Aphrodite: Charm ───
	private static boolean charm(ServerPlayer player, ServerLevel level, UpgradeLevel lvl) {
		double range = cfg().mirror.charmRange;
		Vec3 eye = player.getEyePosition();
		Vec3 end = eye.add(player.getLookAngle().scale(range));
		var hit = ProjectileUtil.getEntityHitResult(player, eye, end, player.getBoundingBox().expandTowards(player.getLookAngle().scale(range)).inflate(1.0),
			e -> e instanceof Mob && e.isAlive(), range * range);
		if (hit == null || !(hit.getEntity() instanceof Mob mob) || Targets.isBoss(mob)) {
			player.displayClientMessage(Component.translatable("olympus.message.charm_nothing").withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		MirrorHandler.charmAlly(mob, player, lvl.durationTicks(cfg().mirror.charmSeconds));
		Fx.line(level, ModParticles.ROSE_PETAL, eye.subtract(0, 0.3, 0), mob.getEyePosition(), 0.4);
		Fx.burst(level, ParticleTypes.HEART, mob.getEyePosition().add(0, 0.5, 0), 8, 0.4, 0.05);
		Fx.sound(player, ModSounds.MIRROR_CHARM, 1.2f, 0.8f);
		return true;
	}

	// ─── Hermes: Caduceus Waypoint ───
	private static final Map<UUID, net.minecraft.core.GlobalPos> WAYPOINTS = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> WAYPOINT_UNTIL = new ConcurrentHashMap<>();

	private static boolean waypoint(ServerPlayer player, ServerLevel level) {
		long now = level.getGameTime();
		var mark = WAYPOINTS.get(player.getUUID());
		if (mark == null || now > WAYPOINT_UNTIL.getOrDefault(player.getUUID(), 0L)) {
			WAYPOINTS.put(player.getUUID(), net.minecraft.core.GlobalPos.of(level.dimension(), player.blockPosition()));
			WAYPOINT_UNTIL.put(player.getUUID(), now + cfg().caduceus.waypointWindowSeconds * 20L);
			Fx.pillar(level, ModParticles.WIND_WISP, player.position(), 3, 20);
			player.displayClientMessage(Component.translatable("olympus.message.waypoint_set").withStyle(God.HERMES.style()), true);
			return false; // marking is free; the cooldown starts when you return
		}
		WAYPOINTS.remove(player.getUUID());
		if (!mark.dimension().equals(level.dimension())) {
			player.displayClientMessage(Component.translatable("olympus.message.waypoint_dimension").withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		Fx.burst(level, ModParticles.WIND_WISP, player.position().add(0, 1, 0), 30, 0.4, 0.1);
		player.teleportTo(mark.pos().getX() + 0.5, mark.pos().getY(), mark.pos().getZ() + 0.5);
		player.resetFallDistance();
		Fx.burst(level, ModParticles.WIND_WISP, player.position().add(0, 1, 0), 30, 0.4, 0.1);
		Fx.sound(player, ModSounds.TALARIA_JUMP, 1.2f, 0.7f);
		return true;
	}

	private static boolean armSpear(ServerPlayer player, ServerLevel level) {
		com.olympus.handler.LegendaryHandler.armSpear(player);
		Fx.helix(level, ModParticles.OWL_FEATHER, player.position(), 0.6, 2.0, 24);
		player.displayClientMessage(Component.translatable("olympus.message.spear_armed").withStyle(God.ATHENA.style()), true);
		return true;
	}

	private static boolean toggleHammer(ServerPlayer player, ItemStack stack) {
		int next = stack.getOrDefault(com.olympus.registry.ModComponents.MINING_RADIUS, 1) == 1 ? 2 : 1;
		stack.set(com.olympus.registry.ModComponents.MINING_RADIUS, next);
		player.displayClientMessage(Component.translatable("olympus.message.hammer_mode", next == 1 ? "3x3" : "5x5").withStyle(God.HEPHAESTUS.style()), true);
		Fx.sound(player, net.minecraft.sounds.SoundEvents.ANVIL_USE, 0.6f, 1.4f);
		return true;
	}

	private static LivingEntity lookTarget(ServerPlayer player, double range) {
		Vec3 eye = player.getEyePosition();
		Vec3 end = eye.add(player.getLookAngle().scale(range));
		var hit = ProjectileUtil.getEntityHitResult(player, eye, end, player.getBoundingBox().expandTowards(player.getLookAngle().scale(range)).inflate(1.0),
			e -> e instanceof LivingEntity && e.isAlive() && e != player, range * range);
		return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
	}

	private static boolean heroLeap(ServerPlayer player, ServerLevel level) {
		LivingEntity target = lookTarget(player, cfg().harpe.leapRange);
		if (target == null || !Targets.canHarm(player, target)) {
			player.displayClientMessage(Component.translatable("olympus.message.charm_nothing").withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		com.olympus.handler.CraftedRelicHandler.startLeap(player, target);
		return true;
	}

	private static boolean vineSnare(ServerPlayer player, ServerLevel level, UpgradeLevel lvl) {
		int ticks = lvl.durationTicks(cfg().thyrsus.snareSeconds);
		for (LivingEntity e : Targets.around(player, lvl.radius(cfg().thyrsus.snareRadius), e -> Targets.canHarm(player, e))) {
			com.olympus.handler.PetrifyHandler.root(e, level, ticks); // can't move or jump, can still swing
			level.sendParticles(ModParticles.GRAIN_LEAF, e.getX(), e.getY() + 0.3, e.getZ(), 15, 0.3, 0.2, 0.3, 0.01);
		}
		Fx.sound(player, net.minecraft.sounds.SoundEvents.CAVE_VINES_PLACE, 1.5f, 0.7f);
		Fx.shockwave(level, ModParticles.GRAIN_LEAF, player.position().add(0, 0.2, 0), lvl.radius(cfg().thyrsus.snareRadius));
		return true;
	}

	private static boolean feast(ServerPlayer player, ServerLevel level, UpgradeLevel lvl) {
		for (ServerPlayer ally : Targets.alliesAround(player, lvl.radius(cfg().cornucopia.feastRadius))) {
			com.olympus.item.relic.CornucopiaItem.feed(ally);
			ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, lvl.durationTicks(cfg().cornucopia.feastRegenSeconds), 0), player);
		}
		Fx.sound(player, net.minecraft.sounds.SoundEvents.GENERIC_EAT.value(), 1.5f, 0.8f);
		Fx.shockwave(level, ModParticles.GRAIN_LEAF, player.position().add(0, 0.2, 0), 5);
		return true;
	}

	private static boolean earthshaker(ServerPlayer player, ServerLevel level, UpgradeLevel lvl) {
		double radius = lvl.radius(cfg().tridentOfPoseidon.earthshakerRadius);
		for (LivingEntity e : Targets.around(player, radius, e -> Targets.canHarm(player, e))) {
			e.setDeltaMovement(e.getDeltaMovement().add(0, 1.2, 0));
			e.hurtMarked = true;
			e.hurtServer(level, player.damageSources().playerAttack(player), cfg().tridentOfPoseidon.earthshakerDamage);
		}
		Fx.shockwave(level, ParticleTypes.SPLASH, player.position().add(0, 0.2, 0), radius);
		Fx.shockwave(level, ModParticles.TIDE_DROP, player.position().add(0, 0.4, 0), radius);
		Fx.sound(player, net.minecraft.sounds.SoundEvents.MACE_SMASH_GROUND_HEAVY, 1.5f, 0.6f);
		return true;
	}

	private static boolean gorgonsGaze(ServerPlayer player, ServerLevel level, UpgradeLevel lvl) {
		double range = cfg().headOfMedusa.gazeRange;
		Vec3 look = player.getLookAngle();
		int petrified = 0;
		for (LivingEntity e : Targets.around(player, range, e -> Targets.canHarm(player, e))) {
			Vec3 to = e.getEyePosition().subtract(player.getEyePosition()).normalize();
			if (to.dot(look) > 0.75 && com.olympus.handler.PetrifyHandler.petrify(e, player, lvl.durationTicks(cfg().headOfMedusa.gazeSeconds))) {
				petrified++;
			}
		}
		for (double d = 1; d <= range; d += 1) {
			Vec3 p = player.getEyePosition().add(look.scale(d));
			level.sendParticles(ModParticles.TITAN_DUST, p.x, p.y, p.z, (int) d, d * 0.2, d * 0.2, d * 0.2, 0);
		}
		Fx.sound(player, net.minecraft.sounds.SoundEvents.STONE_PLACE, 1.5f, 0.5f);
		return petrified > 0 || true;
	}

	public static void forget(UUID player) {
		WAYPOINTS.remove(player);
		WAYPOINT_UNTIL.remove(player);
		LAST_PRESS.remove(player);
		VOLLEY.remove(player);
	}

	public static Optional<Long> volleyExpiry(UUID player) {
		return Optional.ofNullable(VOLLEY.get(player));
	}
}
