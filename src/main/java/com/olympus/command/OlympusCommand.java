package com.olympus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.olympus.Olympus;
import com.olympus.claim.ClaimManager;
import com.olympus.config.ConfigManager;
import com.olympus.god.God;
import com.olympus.god.GodItem;
import com.olympus.god.UpgradeLevel;
import com.olympus.network.OlympusNetwork;
import com.olympus.registry.ModComponents;
import com.olympus.registry.ModItems;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** /olympus … admin commands (permission level 2). */
public final class OlympusCommand {
	private static final DynamicCommandExceptionType UNKNOWN_GOD = new DynamicCommandExceptionType(id -> Component.translatable("olympus.command.unknown_god", id));
	private static final DynamicCommandExceptionType UNKNOWN_ITEM = new DynamicCommandExceptionType(id -> Component.translatable("olympus.command.unknown_item", id));
	private static final SimpleCommandExceptionType NOT_GOD_ITEM = new SimpleCommandExceptionType(Component.translatable("olympus.command.not_god_item"));

	private OlympusCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("olympus")
			.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
			.then(Commands.literal("claim")
				.then(Commands.argument("players", EntityArgument.players())
					.then(Commands.argument("god", StringArgumentType.word())
						.suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(God.OLYMPIANS).map(God::id), b))
						.executes(OlympusCommand::claim))))
			.then(Commands.literal("give")
				.then(Commands.argument("players", EntityArgument.players())
					.then(Commands.argument("item", StringArgumentType.word())
						.suggests((c, b) -> SharedSuggestionProvider.suggest(ModItems.ALL.stream().map(i -> BuiltInRegistries.ITEM.getKey(i).getPath()), b))
						.executes(c -> give(c, 0))
						.then(Commands.argument("level", IntegerArgumentType.integer(0, 2))
							.executes(c -> give(c, IntegerArgumentType.getInteger(c, "level")))))))
			.then(Commands.literal("setlevel")
				.then(Commands.argument("level", IntegerArgumentType.integer(0, 2))
					.executes(OlympusCommand::setLevel)))
			.then(Commands.literal("reload")
				.executes(OlympusCommand::reload))
			.then(Commands.literal("event")
				.then(Commands.literal("start")
					.then(Commands.argument("event", StringArgumentType.word())
						.suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(com.olympus.event.OlympusEvent.values()).map(com.olympus.event.OlympusEvent::id), b))
						.executes(OlympusCommand::startEvent)))
				.then(Commands.literal("stop").executes(c -> {
					if (!com.olympus.event.EventManager.stop(c.getSource().getServer(), Component.translatable("olympus.event.stopped"))) {
						c.getSource().sendFailure(Component.translatable("olympus.event.none_running"));
						return 0;
					}
					return 1;
				})))
			.then(Commands.literal("legendaries")
				.executes(OlympusCommand::listLegendaries)
				.then(Commands.literal("reset")
					.then(Commands.argument("legendary", StringArgumentType.word())
						.suggests((c, b) -> SharedSuggestionProvider.suggest(ModItems.LEGENDARIES.stream().map(i -> BuiltInRegistries.ITEM.getKey(i).getPath()), b))
						.executes(c -> {
							String id = StringArgumentType.getString(c, "legendary");
							com.olympus.data.LegendaryRegistry.remove(c.getSource().getServer(), id);
							c.getSource().sendSuccess(() -> Component.translatable("olympus.command.legendary_reset", id), true);
							return 1;
						})))));
	}

	private static int claim(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
		var targets = EntityArgument.getPlayers(c, "players");
		String godId = StringArgumentType.getString(c, "god");
		God god = God.byId(godId).filter(g -> g != God.TITAN).orElseThrow(() -> UNKNOWN_GOD.create(godId));
		for (ServerPlayer target : targets) {
			ClaimManager.claim(target, god, true);
			c.getSource().sendSuccess(() -> Component.translatable("olympus.command.claimed", target.getDisplayName(), god.displayName()), true);
		}
		return targets.size();
	}

	private static int give(CommandContext<CommandSourceStack> c, int level) throws CommandSyntaxException {
		var targets = EntityArgument.getPlayers(c, "players");
		String name = StringArgumentType.getString(c, "item");
		Item item = BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath(Olympus.MOD_ID, name))
			.filter(ModItems.ALL::contains)
			.orElseThrow(() -> UNKNOWN_ITEM.create(name));
		for (ServerPlayer target : targets) {
			ItemStack stack = new ItemStack(item);
			if (item instanceof GodItem) {
				stack.set(ModComponents.UPGRADE_LEVEL, level);
			}
			com.olympus.item.relic.AegisItem.applyEmblem(stack, target.level().registryAccess());
			Component name2 = stack.getDisplayName();
			ClaimManager.give(target, stack);
			c.getSource().sendSuccess(() -> Component.translatable("olympus.command.gave", name2, target.getDisplayName()), true);
		}
		return targets.size();
	}

	private static int setLevel(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
		ServerPlayer player = c.getSource().getPlayerOrException();
		ItemStack stack = player.getMainHandItem();
		if (!GodItem.is(stack)) {
			throw NOT_GOD_ITEM.create();
		}
		int level = IntegerArgumentType.getInteger(c, "level");
		stack.set(ModComponents.UPGRADE_LEVEL, level);
		c.getSource().sendSuccess(() -> Component.translatable("olympus.command.level_set", stack.getDisplayName(), UpgradeLevel.of(level).displayName()), true);
		return 1;
	}

	private static int startEvent(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
		String id = StringArgumentType.getString(c, "event");
		var event = com.olympus.event.OlympusEvent.byId(id).orElseThrow(() -> UNKNOWN_ITEM.create(id));
		// The boss rises a few blocks in front of the admin rather than on top of them.
		var source = c.getSource();
		var pos = source.getPosition();
		if (source.getEntity() != null) {
			var look = source.getEntity().getLookAngle().multiply(1, 0, 1);
			if (look.lengthSqr() > 1.0E-4) {
				pos = pos.add(look.normalize().scale(8));
			}
		}
		var target = net.minecraft.core.BlockPos.containing(pos);
		if (!source.getLevel().getBlockState(target).getCollisionShape(source.getLevel(), target).isEmpty()) {
			target = net.minecraft.core.BlockPos.containing(source.getPosition()); // wall in front: spawn at the admin instead
		}
		var error = com.olympus.event.EventManager.start(event, source.getLevel(), target);
		if (error.isPresent()) {
			c.getSource().sendFailure(error.get());
			return 0;
		}
		return 1;
	}

	private static int listLegendaries(CommandContext<CommandSourceStack> c) {
		var all = com.olympus.data.LegendaryRegistry.all(c.getSource().getServer());
		for (var item : ModItems.LEGENDARIES) {
			String id = BuiltInRegistries.ITEM.getKey(item).getPath();
			var entry = all.get(id);
			Component line = entry == null
				? Component.translatable("olympus.command.legendary_free", Component.translatable(item.getDescriptionId()))
				: Component.translatable("olympus.command.legendary_owned", Component.translatable(item.getDescriptionId()), entry.firstOwner(), entry.lastHolder());
			c.getSource().sendSuccess(() -> line, false);
		}
		return all.size();
	}

	private static int reload(CommandContext<CommandSourceStack> c) {
		String error = ConfigManager.load();
		if (error != null) {
			c.getSource().sendFailure(Component.translatable("olympus.command.reload_failed", error));
			return 0;
		}
		for (ServerPlayer player : c.getSource().getServer().getPlayerList().getPlayers()) {
			OlympusNetwork.syncConfig(player);
		}
		c.getSource().sendSuccess(() -> Component.translatable("olympus.command.reloaded"), true);
		return 1;
	}
}
