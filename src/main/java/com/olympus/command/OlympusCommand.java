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
				.then(Commands.argument("player", EntityArgument.player())
					.then(Commands.argument("god", StringArgumentType.word())
						.suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(God.OLYMPIANS).map(God::id), b))
						.executes(OlympusCommand::claim))))
			.then(Commands.literal("give")
				.then(Commands.argument("player", EntityArgument.player())
					.then(Commands.argument("item", StringArgumentType.word())
						.suggests((c, b) -> SharedSuggestionProvider.suggest(ModItems.ALL.stream().map(i -> BuiltInRegistries.ITEM.getKey(i).getPath()), b))
						.executes(c -> give(c, 0))
						.then(Commands.argument("level", IntegerArgumentType.integer(0, 2))
							.executes(c -> give(c, IntegerArgumentType.getInteger(c, "level")))))))
			.then(Commands.literal("setlevel")
				.then(Commands.argument("level", IntegerArgumentType.integer(0, 2))
					.executes(OlympusCommand::setLevel)))
			.then(Commands.literal("reload")
				.executes(OlympusCommand::reload)));
	}

	private static int claim(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
		ServerPlayer target = EntityArgument.getPlayer(c, "player");
		String godId = StringArgumentType.getString(c, "god");
		God god = God.byId(godId).filter(g -> g != God.TITAN).orElseThrow(() -> UNKNOWN_GOD.create(godId));
		ClaimManager.claim(target, god, true);
		c.getSource().sendSuccess(() -> Component.translatable("olympus.command.claimed", target.getDisplayName(), god.displayName()), true);
		return 1;
	}

	private static int give(CommandContext<CommandSourceStack> c, int level) throws CommandSyntaxException {
		ServerPlayer target = EntityArgument.getPlayer(c, "player");
		String name = StringArgumentType.getString(c, "item");
		Item item = BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath(Olympus.MOD_ID, name))
			.filter(ModItems.ALL::contains)
			.orElseThrow(() -> UNKNOWN_ITEM.create(name));
		ItemStack stack = new ItemStack(item);
		if (item instanceof GodItem) {
			stack.set(ModComponents.UPGRADE_LEVEL, level);
		}
		ClaimManager.give(target, stack);
		c.getSource().sendSuccess(() -> Component.translatable("olympus.command.gave", stack.getDisplayName(), target.getDisplayName()), true);
		return 1;
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
