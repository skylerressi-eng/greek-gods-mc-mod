package com.olympus.handler;

import com.olympus.config.ConfigManager;
import com.olympus.fx.Fx;
import com.olympus.registry.ModParticles;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Bident of Hades: skeleton warriors that fight for their summoner. They never drop loot or XP
 * (no farms), don't burn in daylight, never attack their owner and crumble when their time is up.
 */
public final class UnderworldAllies {
	public static final String TAG = "olympus.underworld_ally";
	private record Ally(UUID owner, long until, ServerLevel level) {
	}

	private static final Map<UUID, Ally> ALLIES = new ConcurrentHashMap<>();
	/** Soul Harvest: owner → game time it ends. */
	private static final Map<UUID, Long> HARVEST = new ConcurrentHashMap<>();

	private UnderworldAllies() {
	}

	private static long countFor(UUID owner) {
		return ALLIES.values().stream().filter(a -> a.owner().equals(owner)).count();
	}

	public static Skeleton summon(ServerPlayer owner, Vec3 pos, int ticks) {
		if (countFor(owner.getUUID()) >= ConfigManager.get().bident.maxAlliesPerPlayer) {
			return null;
		}
		ServerLevel level = owner.level();
		Skeleton skeleton = EntityType.SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (skeleton == null) {
			return null;
		}
		skeleton.snapTo(pos.x, pos.y, pos.z, owner.getYRot(), 0);
		skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET)); // no burning in daylight
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			skeleton.setDropChance(slot, 0.0f);
		}
		skeleton.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, ticks + 40, 0, false, false));
		skeleton.addTag(TAG);
		skeleton.setPersistenceRequired();
		skeleton.setCustomName(net.minecraft.network.chat.Component.translatable("entity.olympus.underworld_warrior").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
		level.addFreshEntity(skeleton);
		ALLIES.put(skeleton.getUUID(), new Ally(owner.getUUID(), level.getGameTime() + ticks, level));
		MirrorHandler.charmAlly(skeleton, owner, ticks);
		Fx.burst(level, ModParticles.SHADE, pos.add(0, 1, 0), 30, 0.4, 0.05);
		Fx.sound(level, pos, net.minecraft.sounds.SoundEvents.SKELETON_AMBIENT, 1.0f, 0.6f);
		return skeleton;
	}

	/** The player who summoned this ally, or null. */
	public static UUID ownerOf(net.minecraft.world.entity.Mob mob) {
		Ally ally = ALLIES.get(mob.getUUID());
		return ally == null ? null : ally.owner();
	}

	public static void startSoulHarvest(ServerPlayer owner, int ticks) {
		HARVEST.put(owner.getUUID(), owner.level().getGameTime() + ticks);
	}

	private static void crumble(Entity entity) {
		if (entity.level() instanceof ServerLevel level) {
			Fx.burst(level, ModParticles.SHADE, entity.position().add(0, 1, 0), 25, 0.3, 0.03);
		}
		entity.discard();
	}

	public static void register() {
		// Allies never die normally: they crumble (no loot, no XP).
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (entity.getTags().contains(TAG) || entity.getTags().contains(com.olympus.entity.BossCore.MINION_TAG)) {
				ALLIES.remove(entity.getUUID());
				crumble(entity);
				return false;
			}
			return true;
		});
		// Soul Harvest: kills rise as allies.
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((level, killer, killed, source) -> {
			if (killer instanceof ServerPlayer player && !killed.getTags().contains(TAG) && !(killed instanceof net.minecraft.world.entity.player.Player)) {
				Long until = HARVEST.get(player.getUUID());
				if (until != null && level.getGameTime() < until) {
					summon(player, killed.position(), ConfigManager.get().bident.allySeconds * 20);
				}
			}
		});
	}

	public static void tick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, Ally>> it = ALLIES.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Ally> e = it.next();
			Entity entity = e.getValue().level().getEntity(e.getKey());
			if (entity == null || !entity.isAlive()) {
				it.remove();
			} else if (e.getValue().level().getGameTime() >= e.getValue().until() || server.getPlayerList().getPlayer(e.getValue().owner()) == null) {
				it.remove();
				crumble(entity);
			}
		}
		long now = server.overworld().getGameTime();
		HARVEST.values().removeIf(t -> now >= t);
	}
}
