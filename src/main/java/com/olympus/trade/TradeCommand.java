package com.olympus.trade;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Function;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /trade offer &lt;player&gt; [&lt;price item&gt; &lt;count&gt;] — hold the relic you're trading.
 * /trade accept | decline (buyer), /trade cancel (seller). Everyone can use it.
 */
public final class TradeCommand {
	private TradeCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(Commands.literal("trade")
			.then(Commands.literal("offer")
				.then(Commands.argument("player", EntityArgument.player())
					.executes(c -> offer(c, false))
					.then(Commands.argument("price", ItemArgument.item(context))
						.then(Commands.argument("count", IntegerArgumentType.integer(1, 2304))
							.executes(c -> offer(c, true))))))
			.then(Commands.literal("accept").executes(c -> run(c, TradeManager::accept)))
			.then(Commands.literal("decline").executes(c -> run(c, TradeManager::decline)))
			.then(Commands.literal("cancel").executes(c -> run(c, TradeManager::cancel))));
	}

	private static int offer(CommandContext<CommandSourceStack> c, boolean priced) throws CommandSyntaxException {
		ServerPlayer seller = c.getSource().getPlayerOrException();
		ServerPlayer buyer = EntityArgument.getPlayer(c, "player");
		var error = priced
			? TradeManager.offer(seller, buyer, ItemArgument.getItem(c, "price").getItem(), IntegerArgumentType.getInteger(c, "count"))
			: TradeManager.offer(seller, buyer, null, 0);
		if (error != null) {
			c.getSource().sendFailure(error);
			return 0;
		}
		return 1;
	}

	private static int run(CommandContext<CommandSourceStack> c, Function<ServerPlayer, Component> action) throws CommandSyntaxException {
		Component error = action.apply(c.getSource().getPlayerOrException());
		if (error != null) {
			c.getSource().sendFailure(error);
			return 0;
		}
		return 1;
	}
}
